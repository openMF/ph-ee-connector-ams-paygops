package org.mifos.connector.ams.paygops.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mifos.connector.ams.paygops.paygopsDTO.PaygopsRequestDTO;

public class PayloadUtils {

    public static PaygopsRequestDTO convertPaybillPayloadToAmsPaygopsPayload(JSONObject payload) {
        JSONObject content = new JSONObject();
        String transactionId = convertCustomData(payload.getJSONArray("customData"), "transactionId");
        String currency = convertCustomData(payload.getJSONArray("customData"), "currency");
        String memo = convertCustomData(payload.getJSONArray("customData"), "memo");
        String wallet_name = convertCustomData(payload.getJSONArray("customData"), "wallet_name");
        String wallet_msisdn=payload.getJSONObject("secondaryIdentifier").getString("value");
        PaygopsRequestDTO validationRequestDTO = new PaygopsRequestDTO();
        validationRequestDTO.setPhoneNumber(wallet_msisdn);
        validationRequestDTO.setTransactionId(transactionId);
        validationRequestDTO.setCurrency(currency);
        validationRequestDTO.setMemo(memo);
        validationRequestDTO.setWalletName(wallet_name);
        validationRequestDTO.setAmount(1L);
        return validationRequestDTO;
    }
    public static String convertCustomData(JSONArray customData, String key)
    {
        for(Object obj: customData)
        {
            JSONObject item = (JSONObject) obj;
            try {
                String filter = item.getString("key");
                if (filter != null && filter.equalsIgnoreCase(key)) {
                    return item.getString("value");
                }
            } catch (Exception e){
            }
        }
        return null;
    }

}
