package com.lcm.test.steps;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.steps.RESTAssSteps;
import com.lcm.core.utilities.*;
import com.lcm.test.database.StopPayment;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SubscriptionStep {

    @Steps
    CSVDataManipulator csvDataManipulator;

    @Steps
    RESTAssuredAPI restAssuredAPI;

    @Steps
    RESTAssSteps restAssuredAPIsteps;

    @Steps
    DatabaseSteps databaseSteps;

    private boolean testCardsEnabledAtScenarioLevel = true;
    private String scenarioName = "";
    private Map<String, String> queryParamsAsMap;
    private String pathParams;
    private String urlParams;
    private String endpoint;
    private String access_token = null;
    private String encrypted_data = null;
    private String stopPaymentID = null;
    private final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String ALPHASPECIALNUMERIC = "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~`¬£abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String NUMERIC = "0123456789";
    private final LCMProperties apiProperties = new LCMProperties("api.properties");
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();
    private final String issuerName = System.getProperty("issuer") != null ? System.getProperty("issuer") : runner.getProperty("ISSUER");

    private final JSONObject encryptionRequestBody = new JSONObject();

    private final String dataDriveFilePath = "src/main/resources/data/";
    private final String authHeader = "/valid/headers/authHeader.csv";
    private final String requestHeader = "/valid/headers/requestHeader.csv";
    private final String subscriptionRequestFilePath = "Subscription/request/";
    private final String commonLogFilePath = "Database/CommonLog/";
    private JSONObject postRequestObject = new JSONObject();
    private JSONObject putRequestObject = new JSONObject();
    private JSONObject postResponseObject = new JSONObject();
    private JSONArray postResponseArrayObject = new JSONArray();
    private JSONObject postResponseObjectExpected = new JSONObject();
    private JSONObject eventLogObject = new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private final JSONObject sandboxObject = new JSONObject();
    private StopPayment stopPayment;

    @And("^I create the encrypted data for issuer auxiliary for merchant info$")
    public void createIAEncryptedDataMerchantInfo() throws Exception {
        iPostTheDetailsToIssuerAuxiliaryEncryptionAPIEndpoint("valid_request_body_IA_1.1");
        restAssuredAPIsteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = RESTAssuredAPI.globalStaticResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for issuer auxiliary for create stop payment$")
    public void createIAEncryptedDataCreateStopPayment() throws Exception {
        iPostTheDetailsToIssuerAuxiliaryEncryptionAPIEndpoint("valid_request_body_IA_1.7");
        restAssuredAPIsteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = RESTAssuredAPI.globalStaticResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for issuer auxiliary for search stop payment$")
    public void createIAEncryptedDataSearchStopPayment() throws Exception {
        iPostTheDetailsToIssuerAuxiliaryEncryptionAPIEndpoint("valid_request_body_IA_1.13");
        restAssuredAPIsteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = RESTAssuredAPI.globalStaticResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @When("^I post the details to Issuer Auxiliary Encryption endpoint$")
    public void iPostTheDetailsToIssuerAuxiliaryEncryptionAPIEndpoint(String body) throws Exception {
        endpoint = apiProperties.getProperty("ENCRYPTION_IA_DATA");
        endpoint = endpoint.replace("${ENCRYPTION_DECRYPTION_DATA}", apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA"));
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String bodyPath = "Encryption/valid/body/" + body + ".json";

        iHaveTheEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheEncryptionRequestBodyAsDefinedIn(bodyPath);

        restAssuredAPI.post(encryptionRequestBody, databaseSteps.headersAsMap, endpoint);
    }

    @And("^I create a valid bearer token for subscription service$")
    public void createAccessTokenData() throws Exception {
        iPostTheDetailsToOAuthTokenEndpoint();
        restAssuredAPIsteps.iVerifyTheStatusCode(200);

        try {
            postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        } catch (Exception e) {
            throw new Exception(e);
        }

        access_token = postResponseObject.get("access_token").toString();
    }

    @When("^I post the details to OAUTH2 token endpoint$")
    public void iPostTheDetailsToOAuthTokenEndpoint() {
        endpoint = apiProperties.getProperty("OAUTH2_DATA");
        String queryParamPath = "Subscription/request" + authHeader;

        iHaveTheQueryParamsAsDefinedIn(queryParamPath);
        String username = null;
        String password = null;
        String key1 = null;
        String key2 = null;

        for (var entry : queryParamsAsMap.entrySet()) {
            if (entry.getKey().equals("client_id")) {
                username = entry.getValue();
                key1 = entry.getKey();
            }
            if (entry.getKey().equals("client_secret")) {
                password = entry.getValue();
                key2 = entry.getKey();
            }
        }

        if (key1 != null)
            queryParamsAsMap.remove(key1);
        if (key2 != null)
            queryParamsAsMap.remove(key2);

        restAssuredAPI.post(endpoint, null, queryParamsAsMap, username, password);
    }

    @Given("^I have the query parameters as defined in \"([^\"]*)\"$")
    public void iHaveTheQueryParamsAsDefinedIn(String headersPath) {
        queryParamsAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @Given("^I have the Encryption headers as defined in \"([^\"]*)\"$")
    public void iHaveTheEncryptionHeadersAsDefinedIn(String headersPath) {
        databaseSteps.headersAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @Given("^I have the default Subscription headers$")
    public void iHaveTheDefaultSubscriptionHeadersAsDefinedIn() {
        databaseSteps.headersAsMap = new HashMap<>();
        databaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + subscriptionRequestFilePath + requestHeader));

        if (databaseSteps.headersAsMap.get("X-Request-ID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            databaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());

        databaseSteps.headersAsMap.put("Authorization", "Bearer " + access_token);
    }

    @Given("^I have the valid path parameters$")
    public void iHaveTheValidPathParameters() {
        if (databaseSteps.headersAsMap.get("stop_payment_id") != null) {
            pathParams = databaseSteps.headersAsMap.get("stop_payment_id");
            databaseSteps.headersAsMap.remove("stop_payment_id");
        }
        else if (stopPaymentID != null)
            pathParams = stopPaymentID;
    }

    @Given("^I have the Subscription headers as defined in \"([^\"]*)\"$")
    public void iHaveTheSubscriptionHeadersAsDefinedIn(String headersPath) {
        iHaveTheDefaultSubscriptionHeadersAsDefinedIn();
        databaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + subscriptionRequestFilePath + headersPath + ".csv"));
    }

    @Given("^I clear the existing bearer token$")
    public void clearBearerToken() {
        access_token = "";
    }

    @Given("^I have the stop payment by id headers as defined in \"([^\"]*)\"$")
    public void iHaveTheStopPaymentByIDHeadersAsDefinedIn(String headersPath) {
        iHaveTheDefaultSubscriptionHeadersAsDefinedIn();

        databaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + subscriptionRequestFilePath + headersPath + ".csv"));

        if (databaseSteps.headersAsMap.get("X-Request-ID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            databaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());

        if (databaseSteps.headersAsMap.get("realTimeData") != null) {
            queryParamsAsMap = new HashMap<>();
            queryParamsAsMap.put("realTimeData", databaseSteps.headersAsMap.get("realTimeData"));
            databaseSteps.headersAsMap.remove("realTimeData");
        }
    }

    @And("^I have the encryption request body as defined in \"([^\"]*)\"$")
    public void iHaveTheEncryptionRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(postRequestObject);

        encryptionRequestBody.putAll(postRequestObject);
    }

    @And("^I have the subscription response body as defined in \"([^\"]*)\"$")
    public void iHaveTheSubscriptionResponseBodyAsDefinedIn(String responseBodyPath) throws ParseException, IOException {
        String subscriptionResponseFilePath = "Subscription/response/";
        postResponseObjectExpected = JSONHelper.messageAsSimpleJson(dataDriveFilePath + subscriptionResponseFilePath + responseBodyPath + ".json");
        Assert.assertNotNull(postResponseObjectExpected);
    }

    @And("^I have the merchant list request body as defined in \"([^\"]*)\"$")
    public void iHaveTheMerchantListRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + subscriptionRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        encryptionRequestBody.putAll(postRequestObject);

        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equals("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);
    }

    @And("^I have the create stop payment request body as defined in \"([^\"]*)\"$")
    public void iHaveTheCreateStopPaymentRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        int randomStartDate = 0;
        int randomEndDate;
        postRequestObject = new JSONObject();

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + subscriptionRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("startDate") != null && postRequestObject.get("startDate").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            randomStartDate = CommonUtil.generateRandomNumber();
            String date = CommonUtil.generateDate(randomStartDate, "ddMMyyyy");
            postRequestObject.put("startDate", date);
        }

        if (postRequestObject.get("endDate") != null && postRequestObject.get("endDate").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            randomEndDate = randomStartDate + CommonUtil.generateRandomNumber();
            String date = CommonUtil.generateDate(randomEndDate, "ddMMyyyy");
            postRequestObject.put("endDate", date);
        }

        if (postRequestObject.get("accountInfo") != null) {
            HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
            if (account.get("encryptedData") != null && account.get("encryptedData").equals("DEFINE_AT_RUNTIME")) {
                account.put("encryptedData", encrypted_data);
            }
        }
    }

    @And("^I have the extend stop payment request body as defined in \"([^\"]*)\"$")
    public void iHaveTheExtendStopPaymentRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException, java.text.ParseException, SQLException, ClassNotFoundException {
        int randomEndDate;
        int randomStartDate;
        String query = "select * from stop_payment where stop_payment_id = " + stopPaymentID;
        putRequestObject = new JSONObject();

        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + subscriptionRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);

        if (putRequestObject.get("endDate") != null && putRequestObject.get("endDate").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            randomStartDate = CommonUtil.generateRandomNumber();
            randomEndDate = randomStartDate + CommonUtil.generateRandomNumber();
            String date = CommonUtil.generateDate(randomEndDate, "ddMMyyyy");
            putRequestObject.put("endDate", date);
        }
        else if (putRequestObject.get("endDate") != null && putRequestObject.get("endDate").toString().equalsIgnoreCase("FETCH_FROM_DB")) {
            databaseSteps.iEstablishConnectionToSubscriptionDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            StopPayment.buildExtendStopPaymentRequest(databaseSteps.result, putRequestObject);
        }
    }

    @When("^I post the details to merchant list endpoint$")
    public void iPostTheDetailsToMerchantListAPIEndpoint() throws Exception {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_MERCHANTLIST_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");

        restAssuredAPI.post(postRequestObject, databaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to create stop payment endpoint$")
    public void iPostTheDetailsToCreateStopPaymentAPIEndpoint() {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_STOPPAYMENT_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");


        restAssuredAPI.post(postRequestObject, databaseSteps.headersAsMap, endpoint);
    }

    @And("^Verify that the response has a valid stop payment ID$")
    public void verifyThatResponseHasValidStopPaymentID() throws ParseException {
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        stopPaymentID = postResponseObject.get("stopPaymentId").toString();
        String strREGEX = "[0-9]{26}";
        Assert.assertTrue("Invalid stop payment ID has been generated", stopPaymentID.matches(strREGEX));
    }

    @When("^I retrieve the stop payment details by stop payment id$")
    public void iRetrieveTheStopPaymentDetailsFromAPIEndpoint() throws Exception {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_STOPPAYMENT_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");

        restAssuredAPI.get(endpoint, databaseSteps.headersAsMap, queryParamsAsMap, pathParams);
    }

    @When("^I put the details to extend stop payment endpoint$")
    public void iPutTheDetailsToExtendStopPaymentAPIEndpoint() {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_STOPPAYMENT_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");

        restAssuredAPI.put(endpoint, putRequestObject, databaseSteps.headersAsMap, pathParams);
    }

    @When("^I post the details to search by PAN stop payment endpoint$")
    public void iPostTheDetailsToSearchStopPaymentAPIEndpoint() {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_STOPPAYMENT_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");

        //endpoint += "s";
        urlParams = "search";

        restAssuredAPI.post(endpoint, postRequestObject, databaseSteps.headersAsMap, urlParams, queryParamsAsMap);
    }

    @When("^I delete the stop payment details based on ID$")
    public void iDeleteTheDetailsToStopPaymentAPIEndpoint() {
        endpoint = apiProperties.getProperty("SUBSCRIPTION_STOPPAYMENT_DATA");
        endpoint = endpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription-pp");
        else if (environment.equalsIgnoreCase("demo"))
            endpoint = endpoint.replace("lcmsubscription-${ENVIRONMENT}", "lcmsubscription");

        restAssuredAPI.delete(endpoint, databaseSteps.headersAsMap, pathParams);
    }

    @And("^I verify that the response has a valid stop payment list$")
    public void verifyThatResponseHasValidMerchantList() throws ParseException {
        restAssuredAPIsteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject);
        postResponseArrayObject = (JSONArray) postResponseObject.get("stopPayments");
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject);
        Assert.assertTrue("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject.size() > 0);

        for (Object o : postResponseArrayObject) {
            JSONObject obj = (JSONObject) o;
            stopPaymentID = obj.get("stopPaymentId").toString();
            String strREGEX = "[0-9]{26}";
            Assert.assertTrue("Invalid stop payment ID has been generated", stopPaymentID.matches(strREGEX));

            Assert.assertTrue("Pan details are not masked unexpectedly", obj.get("maskedPan").toString().contains("xxxxxx"));
            Assert.assertTrue("Invalid status has been generated", obj.get("status").toString().equalsIgnoreCase("active")
                    || obj.get("status").toString().equalsIgnoreCase("pending"));
        }
    }

    @Then("^I verify the response body as expected in database \"(.*?)\"$")
    public void verifyResponseBody(String query) throws Exception {
        databaseSteps.iEstablishConnectionToSubscriptionDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        StopPayment.buildStopPaymentResponse(databaseSteps.result, postResponseObjectExpected);
        RESTAssuredAPI.verifyResponse(postResponseObjectExpected);
    }

    @Then("^I verify the response body as expected in \"(.*?)\"$")
    public void verifyResponseBody() throws Exception {
        RESTAssuredAPI.verifyResponseFields(postResponseObjectExpected);
    }

    @Then("^I verify the dates in database \"(.*?)\"$")
    public void verifyDates(String query, String requestType) throws Exception {
        databaseSteps.iEstablishConnectionToSubscriptionDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        stopPayment = new StopPayment(databaseSteps.result);

        String actualEndDate = stopPayment.END_DATE.replace(".", "");
        String expectedEndDate = null;
        String actualStartDate = stopPayment.START_DATE.replace(".", "");
        String expectedStartDate = postRequestObject.get("startDate").toString();

        if (requestType.equalsIgnoreCase("post"))
            expectedEndDate = CommonUtil.lastDate(postRequestObject.get("endDate").toString(), "ddMMyyyy");
        else if (requestType.equalsIgnoreCase("put"))
            expectedEndDate = CommonUtil.lastDate(putRequestObject.get("endDate").toString(), "ddMMyyyy");

        Assert.assertEquals("Start date is not updated in the database correctly", expectedStartDate, actualStartDate);
        Assert.assertEquals("End date is not updated in the database correctly", expectedEndDate, actualEndDate);
    }

    @Then("^I verify the retrieve stop payment response body as expected in \"(.*?)\"$")
    public void verifyRetrievedInfo(String responseBodyPath) throws Exception {
        iHaveTheSubscriptionResponseBodyAsDefinedIn(responseBodyPath);
        String query = "select * from stop_payment where stop_payment_id = " + stopPaymentID;

        verifyResponseBody(query);
    }

    @Then("^I verify the dates for create stop payment in database$")
    public void verifyEndDateCreated() throws Exception {
        String query = "select * from stop_payment where stop_payment_id = " + stopPaymentID;

        verifyDates(query, "post");
    }

    @Then("^I verify the dates for extend stop payment in database$")
    public void verifyDatesUpdated() throws Exception {
        String query = "select * from stop_payment where stop_payment_id = " + stopPaymentID;

        verifyDates(query, "put");
    }

    @Then("^I verify the get merchant list response body as expected in \"(.*?)\"$")
    public void verifyGetMerchantList(String responseBodyPath) throws Exception {
        iHaveTheSubscriptionResponseBodyAsDefinedIn(responseBodyPath);
        verifyResponseBody();
    }

    @And("^I have the event log request body as defined in \"([^\"]*)\"$")
    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    @And("^I have the external log request body as defined in \"([^\"]*)\"$")
    public void iHaveTheExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }

    @And("^I have the subscription event log request body as defined in \"([^\"]*)\"$")
    public void subscriptionEventLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", databaseSteps.headersAsMap.get("X-Request-ID"));
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", databaseSteps.headersAsMap.get("X-Issuer-ID"));
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "ISSUER");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "SUBSCRIPTION_SERVICE");
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty()) {
            if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("20")) {
                eventLogObject.put("STATUS", "SUCCESS");
            } else {
                eventLogObject.put("STATUS", "FAILED");
            }
        }
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", null);

        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    @And("^I have the subscription external log request body as defined in \"([^\"]*)\"$")
    public void subscriptionExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheExternalLogRequestBodyAsDefinedIn(requestBodyPath);

        // Service level log
        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", databaseSteps.headersAsMap.get("X-Request-ID"));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty()) {
            if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("20")) {
                externalLogObject.put("STATUS", "SUCCESS");
            } else {
                externalLogObject.put("STATUS", "FAILED");
            }
        }
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty()) {
            if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("20") && !RESTAssuredAPI.globalStaticResponse.getBody().asString().isEmpty()) {
                externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));
            } else if (!RESTAssuredAPI.globalStaticResponse.getBody().asString().isEmpty()) {
                externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()).get("errorCode"));
            }
            else if (RESTAssuredAPI.globalStaticResponse.getBody().asString().isEmpty()) {
                externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);            }
        }
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));

        databaseSteps.externalLogArrayObject.add(externalLogObject);

        //Sandbox log
        sandboxObject.putAll(externalLogObject);
        sandboxObject.put("EXTERNAL_REQUEST_PAYLOAD", JSONHelper.parseJSONObject("{\"encData\":\"Encrypted request payload\"}"));
        sandboxObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject("{\"encData\":\"Encrypted response payload\"}"));
        sandboxObject.put("HTTP_RESPONSE", "200");

        databaseSteps.externalLogArrayObject.add(sandboxObject);
    }

    @And("^I have the merchant info event log request body$")
    public void merchantInfoEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "GET MERCHANT INFO");
    }

    @And("^I have the merchant info external log request body$")
    public void merchantInfoExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("LCM_SUBSCRIPTION_MERCHANT_DATA");
        serviceEndpoint = serviceEndpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String sandBoxEndpoint = apiProperties.getProperty("SANDBOX_MERCHANT_DATA");
        sandBoxEndpoint = sandBoxEndpoint.replace("${SANDBOX_DATA}", apiProperties.getProperty("SANDBOX_DATA"));
        sandBoxEndpoint = sandBoxEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String addlRequestData = null;
        if (encryptionRequestBody.get("account") != null) {
            String pan = encryptionRequestBody.get("account").toString();
            pan = pan.substring(0, 6) + "xxxxxx" + pan.substring(12);
            addlRequestData = "{\"account\":\"" + pan + "\",\"accountType\":\"PAN\"}";
        }

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", addlRequestData);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET MERCHANT INFO");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET MERCHANT INFO");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        //Sandbox log
        if (sandboxObject.get("ENDPOINT") != null && sandboxObject.get("ENDPOINT").toString().isEmpty())
            sandboxObject.put("ENDPOINT", sandBoxEndpoint);
        if (sandboxObject.get("API_CALL") != null && sandboxObject.get("API_CALL").toString().isEmpty())
            sandboxObject.put("API_CALL", "GET MERCHANT INFO");
        if (sandboxObject.get("ACTION") != null && sandboxObject.get("ACTION").toString().isEmpty())
            sandboxObject.put("ACTION", "GET MERCHANT INFO");
    }

    @And("^I have the retrieve stop payment event log request body$")
    public void retrieveStopPaymentEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "SEARCH BY STOP PAYMENT ID");
    }

    @And("^I have the retrieve stop payment external log request body$")
    public void retrieveStopPaymentExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("LCM_SUBSCRIPTION_STOPPAMYENT_DATA");
        serviceEndpoint = serviceEndpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());
        if (pathParams != null)
            serviceEndpoint += "/" + pathParams;
        if (queryParamsAsMap != null) {
            String queryParam = queryParamsAsMap.toString().replace("{", "").replace("}", "");
            serviceEndpoint += "?" + queryParam;
        }

        String sandBoxEndpoint = apiProperties.getProperty("SANDBOX_RETRIEVE_STOPPAYMENT_DATA");
        sandBoxEndpoint = sandBoxEndpoint.replace("${SANDBOX_DATA}", apiProperties.getProperty("SANDBOX_DATA"));
        sandBoxEndpoint = sandBoxEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", null);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "SEARCH BY STOP PAYMENT ID");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "SEARCH BY STOP PAYMENT ID");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        //Sandbox log
        if (sandboxObject.get("ENDPOINT") != null && sandboxObject.get("ENDPOINT").toString().isEmpty())
            sandboxObject.put("ENDPOINT", sandBoxEndpoint);
        if (sandboxObject.get("API_CALL") != null && sandboxObject.get("API_CALL").toString().isEmpty())
            sandboxObject.put("API_CALL", "SEARCH BY STOP PAYMENT ID");
        if (sandboxObject.get("ACTION") != null && sandboxObject.get("ACTION").toString().isEmpty())
            sandboxObject.put("ACTION", "SEARCH BY STOP PAYMENT ID");
    }

    @And("^I have the create stop payment event log request body$")
    public void createStopPaymentEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "CREATE STOP PAYMENT");
    }

    @And("^I have the create stop payment external log request body$")
    public void createStopPaymentExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("LCM_SUBSCRIPTION_STOPPAMYENT_DATA");
        serviceEndpoint = serviceEndpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String sandBoxEndpoint = apiProperties.getProperty("SANDBOX_CREATE_STOPPAYMENT_DATA");
        sandBoxEndpoint = sandBoxEndpoint.replace("${SANDBOX_DATA}", apiProperties.getProperty("SANDBOX_DATA"));
        sandBoxEndpoint = sandBoxEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String pan = encryptionRequestBody.get("account").toString();
        pan = pan.substring(0, 6) + "xxxxxx" + pan.substring(12);
        String addlRequestData = "{\"account\":\"" + pan + "\",\"accountType\":\"PAN\"}";

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", addlRequestData);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "CREATE STOP PAYMENT");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "CREATE STOP PAYMENT");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        //Sandbox log
        if (sandboxObject.get("ENDPOINT") != null && sandboxObject.get("ENDPOINT").toString().isEmpty())
            sandboxObject.put("ENDPOINT", sandBoxEndpoint);
        if (sandboxObject.get("API_CALL") != null && sandboxObject.get("API_CALL").toString().isEmpty())
            sandboxObject.put("API_CALL", "CREATE STOP PAYMENT");
        if (sandboxObject.get("ACTION") != null && sandboxObject.get("ACTION").toString().isEmpty())
            sandboxObject.put("ACTION", "CREATE STOP PAYMENT");
    }

    @And("^I have the extend stop payment event log request body$")
    public void extendStopPaymentEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "EXTEND STOP PAYMENT");
    }

    @And("^I have the extend stop payment external log request body$")
    public void extendStopPaymentExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("LCM_SUBSCRIPTION_STOPPAMYENT_DATA");
        serviceEndpoint = serviceEndpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());
        if (pathParams != null)
            serviceEndpoint += "/" + pathParams;

        String sandBoxEndpoint = apiProperties.getProperty("SANDBOX_EXTEND_STOPPAYMENT_DATA");
        sandBoxEndpoint = sandBoxEndpoint.replace("${SANDBOX_DATA}", apiProperties.getProperty("SANDBOX_DATA"));
        sandBoxEndpoint = sandBoxEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String pan = encryptionRequestBody.get("account").toString();
        pan = pan.substring(0, 6) + "xxxxxx" + pan.substring(12);
        String addlRequestData = "{\"account\":\"" + pan + "\",\"accountType\":\"PAN\"}";

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", putRequestObject);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", addlRequestData);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "EXTEND STOP PAYMENT");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "EXTEND STOP PAYMENT");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        //Sandbox log
        if (sandboxObject.get("ENDPOINT") != null && sandboxObject.get("ENDPOINT").toString().isEmpty())
            sandboxObject.put("ENDPOINT", sandBoxEndpoint);
        if (sandboxObject.get("API_CALL") != null && sandboxObject.get("API_CALL").toString().isEmpty())
            sandboxObject.put("API_CALL", "EXTEND STOP PAYMENT");
        if (sandboxObject.get("ACTION") != null && sandboxObject.get("ACTION").toString().isEmpty())
            sandboxObject.put("ACTION", "EXTEND STOP PAYMENT");
    }

    @And("^I have the search stop payment event log request body$")
    public void searchStopPaymentEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "SEARCH BY PAN");
    }

    @And("^I have the search stop payment external log request body$")
    public void searchStopPaymentExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("LCM_SUBSCRIPTION_STOPPAMYENT_DATA");
        serviceEndpoint = serviceEndpoint.replace("${LCM_SUBSCRIPTION_DATA}", apiProperties.getProperty("LCM_SUBSCRIPTION_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());
        if (urlParams != null)
            serviceEndpoint += "/" + urlParams;
        if (queryParamsAsMap != null) {
            String queryParam = queryParamsAsMap.toString().replace("{", "").replace("}", "");
            serviceEndpoint += "?" + queryParam;
        }

        String sandBoxEndpoint = apiProperties.getProperty("SANDBOX_SEARCH_STOPPAYMENT_DATA");
        sandBoxEndpoint = sandBoxEndpoint.replace("${SANDBOX_DATA}", apiProperties.getProperty("SANDBOX_DATA"));
        sandBoxEndpoint = sandBoxEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        String pan = encryptionRequestBody.get("account").toString();
        pan = pan.substring(0, 6) + "xxxxxx" + pan.substring(12);
        String addlRequestData = "{\"account\":\"" + pan + "\",\"accountType\":\"PAN\"}";

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", addlRequestData);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "SEARCH BY PAN");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "SEARCH BY PAN");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));

        //Sandbox log
        if (sandboxObject.get("ENDPOINT") != null && sandboxObject.get("ENDPOINT").toString().isEmpty())
            sandboxObject.put("ENDPOINT", sandBoxEndpoint);
        if (sandboxObject.get("API_CALL") != null && sandboxObject.get("API_CALL").toString().isEmpty())
            sandboxObject.put("API_CALL", "SEARCH BY PAN");
        if (sandboxObject.get("ACTION") != null && sandboxObject.get("ACTION").toString().isEmpty())
            sandboxObject.put("ACTION", "SEARCH BY PAN");
    }

    @Then("^I verify that merchant info entries are created to event and external logs of Common logging service$")
    public void verifyMerchantInfoEntriesInCL() throws Exception {
        subscriptionEventLog("eventlog_table.json");
        subscriptionExternalLog("externallog_table.json");

        merchantInfoEventLog();
        merchantInfoExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    @Then("^I verify that retrieve stop payment entries are created to event and external logs of Common logging service$")
    public void verifyRetrieveStopPaymentEntriesInCL() throws Exception {
        subscriptionEventLog("eventlog_table.json");
        subscriptionExternalLog("externallog_table.json");

        retrieveStopPaymentEventLog();
        retrieveStopPaymentExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    @Then("^I verify that create stop payment entries are created to event and external logs of Common logging service$")
    public void verifyCreateStopPaymentEntriesInCL() throws Exception {
        subscriptionEventLog("eventlog_table.json");
        subscriptionExternalLog("externallog_table.json");

        createStopPaymentEventLog();
        createStopPaymentExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    @Then("^I verify that extend stop payment entries are created to event and external logs of Common logging service$")
    public void verifyExtendStopPaymentEntriesInCL() throws Exception {
        subscriptionEventLog("eventlog_table.json");
        subscriptionExternalLog("externallog_table.json");

        extendStopPaymentEventLog();
        extendStopPaymentExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    @Then("^I verify that search stop payment by PAN entries are created to event and external logs of Common logging service$")
    public void verifySearchStopPaymentByPANEntriesInCL() throws Exception {
        subscriptionEventLog("eventlog_table.json");
        subscriptionExternalLog("externallog_table.json");

        searchStopPaymentEventLog();
        searchStopPaymentExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    @Given("Prerequisite: I create a valid stop payment details")
    public void iCreateStopPaymentDetailsAsPrerequisite() throws Exception {
        createIAEncryptedDataCreateStopPayment();
        createAccessTokenData();
        iHaveTheSubscriptionHeadersAsDefinedIn("valid/headers/valid_header_1.4");
        iHaveTheCreateStopPaymentRequestBodyAsDefinedIn("valid/body/valid_request_body_1.7");
        iPostTheDetailsToCreateStopPaymentAPIEndpoint();
        restAssuredAPIsteps.iVerifyTheStatusCode(201);
        verifyThatResponseHasValidStopPaymentID();
    }
}