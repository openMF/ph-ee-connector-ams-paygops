package org.mifos.connector.ams.paygops.zeebe;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.*;
import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.AMS_REQUEST;
import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.CHANNEL_REQUEST;
import static org.mifos.connector.ams.paygops.zeebe.ZeebeVariables.*;

@Component
public class ZeebeWorkers {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ObjectMapper objectMapper;

    // This value determines if Pesa API calls are to be made
    @Value("${ams.local.enabled:false}")
    private boolean isAmsLocalEnabled;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {

        zeebeClient.newWorker()
                .jobType("transfer-validation-paygops")
                .handler((client, job) -> {
                    logWorkerDetails(job);

                    Map<String, Object> variables;
                    if (isAmsLocalEnabled) {
                        Exchange ex = new DefaultExchange(camelContext);
                        // Do stuff here
                        variables = job.getVariablesAsMap();

                        JSONObject channelRequest = new JSONObject((String) variables.get("channelRequest"));
                        String transactionId = (String) variables.get(TRANSACTION_ID);
                        logger.info("Channel Request :" + ex.getProperty(CHANNEL_REQUEST));
                        ex.setProperty(CHANNEL_REQUEST, channelRequest);
                        ex.setProperty(TRANSACTION_ID, transactionId);

                        producerTemplate.send("direct:transfer-validation-base", ex);

                        boolean isPartyLookUpFailed = ex.getProperty(PARTY_LOOKUP_FAILED, boolean.class);
                        if(isPartyLookUpFailed) {
                            variables.put(PARTY_LOOKUP_FAILED, true);
                            setErrorInfo(variables,ex);
                        } else {
                            variables.put(PARTY_LOOKUP_FAILED, false);
                        }

                    } else {
                        variables = new HashMap<>();
                        variables.put(PARTY_LOOKUP_FAILED, false);
                    }

                    zeebeClient.newCompleteCommand(job.getKey())
                            .variables(variables)
                            .send()
                            .join();
                })
                .name("transfer-validation-paygops")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("transfer-settlement-paygops")
                .handler((client, job) -> {
                    logWorkerDetails(job);

                    Map<String, Object> variables;
                    if (isAmsLocalEnabled) {
                        Exchange ex = new DefaultExchange(camelContext);
                        // Do stuff here
                        variables = job.getVariablesAsMap();
                        JSONObject channelRequest = new JSONObject((String) variables.get("channelRequest"));
                        String transactionId = (String) variables.get(TRANSACTION_ID);
                        ex.setProperty(TRANSACTION_ID, variables.getOrDefault(EXTERNAL_ID, transactionId));
                        ex.setProperty(CHANNEL_REQUEST, channelRequest);
                        logger.info("Channel Request :" + ex.getProperty(CHANNEL_REQUEST));
                        producerTemplate.send("direct:transfer-settlement-base", ex);
                        boolean isSettlementFailed = ex.getProperty(TRANSFER_SETTLEMENT_FAILED, boolean.class);
                        if (isSettlementFailed) {
                            variables.put(TRANSFER_SETTLEMENT_FAILED, true);
                            setErrorInfo(variables,ex);

                        } else {
                            variables.put(ZeebeVariables.AMS_REQUEST,ex.getProperty(AMS_REQUEST));
                            variables.put(TRANSFER_SETTLEMENT_FAILED, false);
                        }

                    } else {
                        variables = new HashMap<>();
                        variables.put(TRANSFER_SETTLEMENT_FAILED, false);
                    }

                    zeebeClient.newCompleteCommand(job.getKey())
                            .variables(variables)
                            .send();
                })
                .name("transfer-settlement-paygops")
                .maxJobsActive(workerMaxJobs)
                .open();

    }

    private void logWorkerDetails(ActivatedJob job) {
        JSONObject jsonJob = new JSONObject();
        jsonJob.put("bpmnProcessId", job.getBpmnProcessId());
        jsonJob.put("elementInstanceKey", job.getElementInstanceKey());
        jsonJob.put("jobKey", job.getKey());
        jsonJob.put("jobType", job.getType());
        jsonJob.put("workflowElementId", job.getElementId());
        jsonJob.put("workflowDefinitionVersion", job.getProcessDefinitionVersion());
        jsonJob.put("workflowKey", job.getProcessDefinitionKey());
        jsonJob.put("workflowInstanceKey", job.getProcessInstanceKey());
        logger.info("Job started: {}", jsonJob.toString(4));
    }

    private void setErrorInfo(Map<String, Object> variables,Exchange ex ){
        variables.put(ERROR_INFORMATION, ex.getProperty(ERROR_INFORMATION, String.class));
        variables.put(ERROR_CODE, ex.getProperty(ERROR_CODE, String.class));
        variables.put(ERROR_DESCRIPTION, ex.getProperty(ERROR_DESCRIPTION, String.class));
    }

}
