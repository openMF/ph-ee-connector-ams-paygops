package org.mifos.connector.ams.paygops.paygopsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
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
}
