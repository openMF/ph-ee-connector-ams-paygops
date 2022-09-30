package org.mifos.connector.ams.paygops.camel.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.ams.paygops.paygopsDTO.PaygopsRequestDTO;
import org.mifos.connector.ams.paygops.paygopsDTO.PaygopsResponseDto;
import org.mifos.connector.ams.paygops.utils.ConnectionUtils;
import org.mifos.connector.ams.paygops.utils.ErrorCodeEnum;
import org.mifos.connector.ams.paygops.utils.PayloadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.*;
import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.AMS_REQUEST;
import static org.mifos.connector.ams.paygops.zeebe.ZeebeVariables.*;

@Component
public class PaygopsRouteBuilder extends RouteBuilder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${paygops.base-url}")
    private String paygopsBaseUrl;

    @Value("${paygops.endpoint.verification}")
    private String verificationEndpoint;

    @Value("${paygops.endpoint.confirmation}")
    private String confirmationEndpoint;

    @Value("${paygops.auth-header}")
    private String accessToken;

    @Value("${ams.timeout}")
    private Integer amsTimeout;


    @Override
    public void configure() {

        from("rest:POST:/api/v1/payments/validate")
                .process(exchange -> {
                    JSONObject channelRequest = new JSONObject(exchange.getIn().getBody(String.class));
                    String transactionId = "123";
                    log.info(channelRequest.toString());
                    exchange.setProperty(CHANNEL_REQUEST, channelRequest);
                    exchange.setProperty(TRANSACTION_ID, transactionId);
                })
                .to("direct:transfer-validation-base");

        from("rest:POST:/api/paymentHub/Confirmation")
                .process(exchange -> {
                    JSONObject channelRequest = new JSONObject(exchange.getIn().getBody(String.class));
                    String transactionId = "123";
                    exchange.setProperty(CHANNEL_REQUEST, channelRequest);
                    exchange.setProperty(TRANSACTION_ID, transactionId);
                })
                .to("direct:transfer-settlement-base");

        from("direct:transfer-validation-base")
                .id("transfer-validation-base")
                .log(LoggingLevel.INFO, "## Starting Paygops Validation base route")
                .to("direct:transfer-validation")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Paygops Validation Response Received")
                .process(exchange -> {
                    // processing success case
                    try {
                        String body = exchange.getIn().getBody(String.class);
                        ObjectMapper mapper = new ObjectMapper();
                        PaygopsResponseDto result = mapper.readValue(body, PaygopsResponseDto.class);
                        if (result.getReconciled()) {
                            logger.info("Paygops Validation Successful");
                            exchange.setProperty(PARTY_LOOKUP_FAILED, false);
                            exchange.setProperty("accountStatus","ACTIVE");
                            exchange.setProperty("subStatus", "");
                        } else {
                            setErrorCamelInfo(exchange,"Validation Unsuccessful: Reconciled field returned false",
                                    ErrorCodeEnum.RECONCILIATION.getCode(), result.toString());

                            exchange.setProperty(PARTY_LOOKUP_FAILED, true);
                            exchange.setProperty("accountStatus","REJECTED");
                            exchange.setProperty("subStatus", "");
                        }
                    } catch (Exception e) {
                        logger.info("Body could not be passed due to : {} ", String.valueOf(e));
                        setErrorCamelInfo(exchange,"Body data could not be parsed,setting validation as failed",
                                ErrorCodeEnum.DEFAULT.getCode(),exchange.getIn().getBody(String.class));
                        exchange.setProperty(PARTY_LOOKUP_FAILED, true);
                        exchange.setProperty("accountStatus","REJECTED");
                        exchange.setProperty("subStatus", "");
                    }
                })
                .otherwise()
                .log(LoggingLevel.ERROR, "Paygops Validation unsuccessful")
                .process(exchange -> {
                    // processing unsuccessful case
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject jsonObject = new JSONObject(body);
                    Integer errorCode = jsonObject.getInt("error");
                    String errorDescription   = jsonObject.getString("error_message");
                    String errorInfo = jsonObject.toString(1);
                    setErrorCamelInfo(exchange,errorDescription,errorCode,errorInfo);
                    exchange.setProperty(PARTY_LOOKUP_FAILED, true);
                });

        from("direct:transfer-validation")
                .id("transfer-validation")
                .log(LoggingLevel.INFO, "## Starting Paygops Validation route")
                .log(LoggingLevel.INFO, "Bearer token is - " + accessToken)
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Authorization", simple("Bearer "+ accessToken))
                .setHeader("Content-Type", constant("application/json"))
                .setBody(exchange -> {
                    if(exchange.getProperty(CHANNEL_REQUEST) != null)
                    {
                        JSONObject channelRequest = (JSONObject) exchange.getProperty(CHANNEL_REQUEST);
                        String transactionId = exchange.getProperty(TRANSACTION_ID, String.class);
                        PaygopsRequestDTO verificationRequestDTO = getPaygopsDtoFromChannelRequest(channelRequest,
                                transactionId);
                        logger.info("Validation request DTO: \n\n\n" + verificationRequestDTO);
                        return verificationRequestDTO;
                    }
                    else {
                        JSONObject paybillRequest = new JSONObject(exchange.getIn().getBody(String.class));
                        PaygopsRequestDTO paygopsRequestDTO = PayloadUtils.convertPaybillPayloadToAmsPaygopsPayload(paybillRequest);

                        String transactionId = paygopsRequestDTO.getTransactionId();
                        log.info(paygopsRequestDTO.toString());
                        exchange.setProperty(TRANSACTION_ID, transactionId);
                        logger.info("Validation request DTO: \n\n\n" + paygopsRequestDTO);
                        return paygopsRequestDTO;
                    }
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(getVerificationEndpoint() + "?bridgeEndpoint=true&throwExceptionOnFailure=false&"+
                        ConnectionUtils.getConnectionTimeoutDsl(amsTimeout))
                .log(LoggingLevel.TRACE, "Paygops validation api response: \n\n..\n\n..\n\n.. ${body}");

        from("direct:transfer-settlement-base")
                .id("transfer-settlement-base")
                .log(LoggingLevel.INFO, "## Transfer Settlement route")
                .to("direct:transfer-settlement")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Settlement Response Received")
                .process(exchange -> {
                    // processing success case
                    try {
                        String body = exchange.getIn().getBody(String.class);
                        JSONObject jsonObject = new JSONObject(body);
                        exchange.setProperty(TRANSFER_SETTLEMENT_FAILED, false);
                    } catch (Exception e) {
                        logger.info("Body could not be passed due to : {} ", String.valueOf(e));
                        setErrorCamelInfo(exchange,"Body data could not be parsed,setting confirmation as failed",
                                ErrorCodeEnum.DEFAULT.getCode(), exchange.getIn().getBody(String.class));
                        exchange.setProperty(TRANSFER_SETTLEMENT_FAILED, true);
                    }


                })
                .otherwise()
                .log(LoggingLevel.ERROR, "Settlement unsuccessful")
                .process(exchange -> {
                    // processing unsuccessful case
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject jsonObject = new JSONObject(body);
                    Integer errorCode = jsonObject.getInt("error");
                    String errorDescription = jsonObject.getString("error_message");
                    String errorInfo = jsonObject.toString(1);
                    setErrorCamelInfo(exchange,errorDescription,errorCode,errorInfo);
                    exchange.setProperty(TRANSFER_SETTLEMENT_FAILED, true);
                });

        from("rest:POST:/api/v1/paybill/paygops")
                .id("validate-user")
                .log(LoggingLevel.INFO, "## Paygops user validation")
                .setBody(e -> {
                    String body=e.getIn().getBody(String.class);
                    logger.info("Body : {}",body);
                    return body;
                })
                .to("direct:transfer-validation-base")
                .process(e->{
                    logger.info("Response received from validation base : {}",e.getProperty("accountStatus"));
                });

        from("direct:transfer-settlement")
                .id("transfer-settlement")
                .log(LoggingLevel.INFO, "## Starting transfer settlement route")
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Authorization", simple("Bearer "+ accessToken))
                .setHeader("Content-Type", constant("application/json"))
                .setBody(exchange -> {
                    JSONObject channelRequest = (JSONObject) exchange.getProperty(CHANNEL_REQUEST);
                    String transactionId = exchange.getProperty(TRANSACTION_ID, String.class);
                    PaygopsRequestDTO confirmationRequestDTO = getPaygopsDtoFromChannelRequest(channelRequest,
                            transactionId);
                    logger.info("Confirmation request DTO: \n\n\n" + confirmationRequestDTO);
                    exchange.setProperty(AMS_REQUEST,confirmationRequestDTO.toString());
                    return confirmationRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(getConfirmationEndpoint() + "?bridgeEndpoint=true&throwExceptionOnFailure=false&" +
                        ConnectionUtils.getConnectionTimeoutDsl(amsTimeout))
                .log(LoggingLevel.TRACE, "Paygops verification api response: \n ${body}");

    }


    // returns the complete URL for verification request
    private String getVerificationEndpoint() {
        return paygopsBaseUrl + verificationEndpoint;
    }

    // returns the complete URL for confirmation request
    private String getConfirmationEndpoint() {
        return paygopsBaseUrl + confirmationEndpoint;
    }

    private PaygopsRequestDTO getPaygopsDtoFromChannelRequest(JSONObject channelRequest, String transactionId) {
        PaygopsRequestDTO verificationRequestDTO = new PaygopsRequestDTO();

        String phoneNumber = channelRequest.getJSONObject("payer")
                .getJSONObject("partyIdInfo").getString("partyIdentifier");
        String memoId = channelRequest.getJSONObject("payee")
                .getJSONObject("partyIdInfo").getString("partyIdentifier"); // instead of account id this value corresponds to national id
        JSONObject amountJson = channelRequest.getJSONObject("amount");
        String operatorName = "MPESA";


        Long amount = amountJson.getLong("amount");
        String currency = amountJson.getString("currency");
        String country = "KE";

        verificationRequestDTO.setTransactionId(transactionId);
        verificationRequestDTO.setAmount(amount);
        verificationRequestDTO.setPhoneNumber(phoneNumber);
        verificationRequestDTO.setCurrency(currency);
        verificationRequestDTO.setOperator(operatorName);
        verificationRequestDTO.setMemo(memoId);
        verificationRequestDTO.setCountry(country);
        verificationRequestDTO.setWalletName(phoneNumber);

        return verificationRequestDTO;
    }

    private void setErrorCamelInfo(Exchange exchange, String errorDesc, Integer errorCode, String errorInfo) {
        logger.info(errorInfo);
        exchange.setProperty(ERROR_CODE, errorCode);
        exchange.setProperty(ERROR_INFORMATION, errorInfo);
        exchange.setProperty(ERROR_DESCRIPTION, errorDesc);

    }
}
