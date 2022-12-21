package org.mifos.connector.ams.paygops.paygopsDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

public class PaygopsResponseDto {

    /*"transaction_id": "simon_nationalID_test2",
    "amount": "123",
    "sender_name": "Simon Test2 National ID",
    "sender_phone_number": "+25512345676",
    "sent_datetime": "2022-04-25T12:07:58.048847Z",
    "memo": "abd1",
    "wallet_operator": "",
    "country": "",
    "currency": "",
    "warnings": [],
    "reconciled": false
    "destination_type": "contract_repayment",
    "destination": "C391270014"
    */
    @JsonProperty("transaction_id")
    private String transaction_id;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("sender_name")
    private String sender_name;

    @JsonProperty("sender_phone_number")
    private String sender_phone_number;

    @JsonProperty("sent_datetime")
    private String sent_datetime;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("wallet_operator")
    private String wallet_operator;
    @JsonProperty("wallet_name")
    private String wallet_name;
    @JsonProperty("wallet_msisdn")
    private String wallet_msisdn;
    @JsonProperty("reception_datetime")
    private String reception_datetime;

    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("warnings")
    private String [] warnings = new String[1];

    @JsonProperty("reconciled")
    private Boolean reconciled;

    @JsonProperty("destination_type")
    private String destination_type;

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("destination")
    private String destination;
    @JsonProperty("destinations")
    private Object[] destinations = new Object[1];

    PaygopsResponseDto(){

    }

    @Override
    public String toString() {
        return "PaygopsResponseDto{" +
                "transaction_id='" + transaction_id + '\'' +
                ", amount='" + amount + '\'' +
                ", sender_name='" + sender_name + '\'' +
                ", sender_phone_number='" + sender_phone_number + '\'' +
                ", sent_datetime='" + sent_datetime + '\'' +
                ", memo='" + memo + '\'' +
                ", wallet_operator='" + wallet_operator + '\'' +
                ", wallet_name='" + wallet_name + '\'' +
                ", wallet_msisdn='" + wallet_msisdn + '\'' +
                ", reception_datetime='" + reception_datetime + '\'' +
                ", country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", warnings=" + Arrays.toString(warnings) +
                ", reconciled=" + reconciled +
                ", destination_type='" + destination_type + '\'' +
                ", destination='" + destination + '\'' +
                ", destinations=" + Arrays.toString(destinations) +
                '}';
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_phone_number() {
        return sender_phone_number;
    }

    public void setSender_phone_number(String sender_phone_number) {
        this.sender_phone_number = sender_phone_number;
    }

    public String getSent_datetime() {
        return sent_datetime;
    }

    public void setSent_datetime(String sent_datetime) {
        this.sent_datetime = sent_datetime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getWallet_operator() {
        return wallet_operator;
    }

    public void setWallet_operator(String wallet_operator) {
        this.wallet_operator = wallet_operator;
    }

    public String getWallet_name() {
        return wallet_name;
    }

    public void setWallet_name(String wallet_name) {
        this.wallet_name = wallet_name;
    }

    public String getWallet_msisdn() {
        return wallet_msisdn;
    }

    public void setWallet_msisdn(String wallet_msisdn) {
        this.wallet_msisdn = wallet_msisdn;
    }

    public String getReception_datetime() {
        return reception_datetime;
    }

    public void setReception_datetime(String reception_datetime) {
        this.reception_datetime = reception_datetime;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String[] getWarnings() {
        return warnings;
    }

    public void setWarnings(String[] warnings) {
        this.warnings = warnings;
    }

    public Boolean getReconciled() {
        return reconciled;
    }

    public void setReconciled(Boolean reconciled) {
        this.reconciled = reconciled;
    }

    public String getDestination_type() {
        return destination_type;
    }

    public void setDestination_type(String destination_type) {
        this.destination_type = destination_type;
    }

    public Object[] getDestinations() {
        return destinations;
    }

    public void setDestinations(Object[] destinations) {
        this.destinations = destinations;
    }
}
