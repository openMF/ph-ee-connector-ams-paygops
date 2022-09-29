package org.mifos.connector.ams.paygops.paygopsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jdk.nashorn.internal.objects.annotations.Property;

import java.util.List;

//{
//    "primaryIdentifier": {
//        "key": "MSISDN",
//        "value": "27710101999"
//    },
//    "secondaryIdentifier":{
//        "key": "foundationalID",
//        "value": "12345"
//    },
//    "customData": [
//        {
//        "key": "transactionId",
//        "value": "670d65bd-4efd-4a6c-ae2c-7fdaa8cb4d60"
//        },
//        {
//        "key": "currency",
//        "value": "KES"
//        },
//        {
//        "key": "memo",
//        "value": "1234"
//        },
//        {
//        "key": "wallet_name",
//        "value": "254797668592"
//        }
//    ]
//}
public class PaybillRequestDTO {
    @JsonProperty("wallet_name")
    private String wallet_name;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("memo")
    private String memo;

    @JsonProperty("primaryIdentifier")
    private JsonElement primaryIdentifier;

    @JsonProperty("secondaryIdentifier")
    private JsonElement secondaryIdentifier;

    @JsonProperty("customData")
    private List<JsonElement> customData;

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setWallet_name(String wallet_name) {
        this.wallet_name = wallet_name;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public String getMemo() {
        return memo;
    }

    public String getWallet_name() {
        return wallet_name;
    }

    public String getCurrency() {
        return currency;
    }

    public JsonElement getPrimaryIdentifier() {
        return primaryIdentifier;
    }

    public void setPrimaryIdentifier(JsonElement primaryIdentifier) {
        this.primaryIdentifier = primaryIdentifier;
    }

    public JsonElement getSecondaryIdentifier() {
        return secondaryIdentifier;
    }

    public void setSecondaryIdentifier(JsonElement secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    public List<JsonElement> getCustomData() {
        return customData;
    }

    public void setCustomData(List<JsonElement> customData) {
        this.customData = customData;
    }

    public String getSecondaryIdentierValue() {
        return secondaryIdentifier.getValue();
    }
}
