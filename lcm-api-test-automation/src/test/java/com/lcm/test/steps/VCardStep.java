package com.lcm.test.steps;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.steps.RESTAssSteps;
import com.lcm.core.utilities.*;
import com.lcm.test.database.*;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VCardStep {

    @Steps
    CSVDataManipulator csvDataManipulator;

    @Steps
    RESTAssuredAPI restAssuredAPI;

    @Steps
    RESTAssSteps restAssuredAPIsteps;

    @Steps
    DatabaseSteps databaseSteps;

    private Map<String, String> queryParamsAsMap;
    private String endpoint;
    private String access_token = null;
    private String cardImageID = null;
    private final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final LCMProperties apiProperties = new LCMProperties("api.properties");
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();
    private final String dataDriveFilePath = "src/main/resources/data/";
    private final String authHeader = "/valid/headers/authHeader_" + environment + ".csv";
    private final String requestHeader = "/valid/headers/requestHeader.csv";
    private String issuerHeader = "/valid/headers/requestHeader_" + "<issuer>" + ".csv";
    private final String VCardRequestFilePath = "VCard/request/";
    private final String commonLogFilePath = "Database/CommonLog/";
    private JSONObject postRequestObject = new JSONObject();
    private JSONObject postResponseObject = new JSONObject();
    private JSONObject eventLogObject = new JSONObject();
    private JSONObject addlEventLogObject = new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private final JSONObject addlexternalLogObject = new JSONObject();
    private String panRefAccount;
    private String accountID;
    private AccountInfo accountInfo;
    private BinRange binRange;
    private BinRangeLCMService binRangeLCMService;
    private DeviceInfo deviceInfo;
    private Issuer issuer;
    private IDVMethod idvMethod;
    private IssuerConfig issuerConfig;
    private IssuerIDVConfig issuerIDVConfig;
    private TokenInfo tokenInfo;
    private VirtualAccount virtualAccount;
    private VirtualRiskInfo virtualRiskInfo;
    private WalletConfig walletConfig;

    @And("^I create a valid bearer token for VCard service$")
    public void createAccessTokenData() throws Exception {
        iPostTheDetailsToOAuthTokenEndpointVCard();
        restAssuredAPIsteps.iVerifyTheStatusCode(200);

        try {
            postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        } catch (Exception e) {
            throw new Exception(e);
        }

        access_token = postResponseObject.get("access_token").toString();
    }

    @When("^I post the details to OAUTH2 token endpoint for VCard$")
    public void iPostTheDetailsToOAuthTokenEndpointVCard() {
        endpoint = apiProperties.getProperty("OAUTH2_DATA");
        String queryParamPath = "VCard/request" + authHeader;

        if (environment.equalsIgnoreCase("Test"))
            endpoint = endpoint.replace("pp", "test");

        iHaveTheVCardQueryParamsAsDefinedIn(queryParamPath);
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

    @Given("^I have the query parameters for VCard as defined in \"([^\"]*)\"$")
    public void iHaveTheVCardQueryParamsAsDefinedIn(String headersPath) {
        queryParamsAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);

    }

    @Given("^I have the default VCard headers$")
    public void iHaveTheDefaultVCardHeadersAsDefinedIn() {
        databaseSteps.headersAsMap = new HashMap<>();
        databaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + VCardRequestFilePath + requestHeader));

        if (databaseSteps.headersAsMap.get("X-Request-ID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            databaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        databaseSteps.headersAsMap.put("Authorization", "Bearer " + access_token);
    }

    @Given("^I have the issuer specific headers as defined in \"([^\"]*)\"$")
    public void iHaveTheIssuerSpecificHeadersAsDefinedIn(String issuer) {
        iHaveTheDefaultVCardHeadersAsDefinedIn();
        issuerHeader = issuerHeader.replace("<issuer>", issuer);
        databaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + VCardRequestFilePath + issuerHeader));
    }

    @And("^I have the retrieve card image id request body as defined in \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheRetrieveCardIMageIdRequestBodyAsDefinedIn(String requestBodyPath, String accountType, String issuer, String imageProfile) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VCardRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("imageProfile") != null && postRequestObject.get("imageProfile").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE")) {
            postRequestObject.put("imageProfile", imageProfile);
        }

        if (postRequestObject.get("appSessionID") != null && postRequestObject.get("appSessionID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            postRequestObject.put("appSessionID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 15));
        }

        if (postRequestObject.get("accountInfo") != null) {
            HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
            if (account.get("accountType") != null && account.get("accountType").equals("FETCH_FROM_DATATABLE")) {
                account.put("accountType", accountType);
            }
            if (account.get("accountType").equalsIgnoreCase("Pan")) {
                if (issuer.equalsIgnoreCase("eika")) {

                    if (account.get("account") != null && account.get("account").equals("FETCH_FROM_DATABASE")) {
                        generateOrFetchValidAccount();
                        account.put("account", accountID);
                    }
                    if (account.get("accountExpiry") != null && account.get("accountExpiry").equals("FETCH_FROM_DATABASE")) {
                        account.put("accountExpiry", "022022");
                    }
                }
            }
            else if (account.get("accountType").equalsIgnoreCase("PanRef")) {
                if (issuer.equalsIgnoreCase("payally")) {
                    if (account.get("account") != null && account.get("account").equals("FETCH_FROM_DATABASE")) {
                        generateOrFetchValidAccount();
                        account.put("account", panRefAccount);
                    }
                }
            }
        }
    }

    @When("^I post the details to retrieve card image id endpoint$")
    public void iPostTheDetailsToRetrieveCardImageIdAPIEndpoint() {
        endpoint = apiProperties.getProperty("VCARD_CARDIMAGEID_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.post(postRequestObject, databaseSteps.headersAsMap, endpoint);
    }
    @And("^Verify that the response has a valid card image ID$")
    public void verifyThatResponseHasValidCardImageID() throws ParseException {
        restAssuredAPIsteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("cardImageID"));
        cardImageID = postResponseObject.get("cardImageID").toString();
        Assert.assertNotNull("No card image id has been generated", cardImageID);
    }

    @Then("^I verify the details on Account Info table in database$")
    public void verifyAccountInfoTable() throws Exception {
        String query = null;
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref"))
            query = "select * from account_info where account_ref = '" + panRefAccount + "'";
        else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan"))
            query = "select * from account_info where account = '" + account.get("account") + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);

        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref")) {
            Assert.assertEquals("Account ref '" + panRefAccount + "' is not found on Account Info table in database", accountInfo.ACCOUNT_REF, panRefAccount);
            Assert.assertTrue("Account type '" + accountInfo.ACCOUNT_REF_TYPE + "' is not as expected on Account Info table in database", account.get("accountType").equalsIgnoreCase(accountInfo.ACCOUNT_REF_TYPE));
            Assert.assertTrue("Account state 'Active' is not as expected on Account Info table in database", accountInfo.ACCOUNT_STATE.equalsIgnoreCase("active"));
        }
        else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan")) {
            Assert.assertEquals("Account '" + account.get("account") + "' is not found on Account Info table in database", accountInfo.ACCOUNT, account.get("account"));
            Assert.assertTrue("Account type '" + accountInfo.ACCOUNT_TYPE + "' is not as expected on Account Info table in database", account.get("accountType").equalsIgnoreCase(accountInfo.ACCOUNT_TYPE));
            Assert.assertTrue("Account state 'Active' is not as expected on Account Info table in database", accountInfo.ACCOUNT_STATE.equalsIgnoreCase("active"));
        }
    }

    @Then("^I generate or fetch a valid account$")
    public void generateOrFetchValidAccount() throws Exception {
        String query = null;
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref"))
            query = "SELECT * FROM (SELECT * FROM ACCOUNT_INFO ORDER BY DBMS_RANDOM.RANDOM) where account_ref_type = 'PANREF' fetch first 1 row only";
        else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan"))
            query = "SELECT * FROM (SELECT * FROM bin_range ORDER BY DBMS_RANDOM.RANDOM) where issuer_id = '" + databaseSteps.headersAsMap.get("X-Issuer-ID") + "' fetch first 1 row only";

        databaseSteps.iEstablishConnectionToLCMDatabase();

        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan")) {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            binRange = new BinRange(databaseSteps.result);

            String binRangeLow = binRange.BIN_RANGE_LOW;
            String binRangeHigh = binRange.BIN_RANGE_HIGH;
            int panLength = Integer.parseInt(binRange.PAN_LENGTH);
            binRangeLow = binRangeLow.substring(0,panLength);
            binRangeHigh = binRangeHigh.substring(0,panLength);
            long longBinRangeLow = Long.parseLong(binRangeLow);
            long longBinRangeHigh = Long.parseLong(binRangeHigh);
            long random = longBinRangeLow + (long) (Math.random() * (longBinRangeHigh - longBinRangeLow));
            accountID = Long.toString(random);
        }
        else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref")) {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            accountInfo = new AccountInfo(databaseSteps.result);

            panRefAccount = accountInfo.ACCOUNT_REF;
        }
    }

    @Then("^I verify that get card image id entries are created to event and external logs of Common logging service$")
    public void verifyGetCardImageIDEntriesInCL() throws Exception {
        sddEventLog("eventlog_table.json");
        sddExternalLog("externallog_table.json");

        if (postRequestObject.get("accountInfo") != null) {
            HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
            if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref"))
                panRefExternalLog();
            else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan"))
                panExternalLog();
        }

        databaseSteps.verifyCommonLoggingService();
    }

    @And("^I have the SDD event log request body as defined in \"([^\"]*)\"$")
    public void sddEventLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", databaseSteps.headersAsMap.get("X-Request-ID"));
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", databaseSteps.headersAsMap.get("X-Issuer-ID"));
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "LCM_SERVICE");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "VIRTUAL_IMAGE_SERVICE");
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty())
            eventLogObject.put("STATUS", "SUCCESS");
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "GET CARD IMAGE ID");

        databaseSteps.eventLogArrayObject.add(eventLogObject);

        addlEventLogObject.putAll(eventLogObject);
        addlEventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        addlEventLogObject.put("DESTINATION", "LCM_SERVICES");

        databaseSteps.eventLogArrayObject.add(addlEventLogObject);
    }

    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    public void iHaveTheSDDExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }

    @And("^I have the SDD external log request body as defined in \"([^\"]*)\"$")
    public void sddExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheSDDExternalLogRequestBodyAsDefinedIn(requestBodyPath);

        String serviceEndpoint = apiProperties.getProperty("VCARD_SERVICE_DATA");
        serviceEndpoint = serviceEndpoint.replace("${ISSUER_DATA}", apiProperties.getProperty("ISSUER_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        // Service level log
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", null);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET CARD IMAGE ID");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET CARD IMAGE ID");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", databaseSteps.headersAsMap.get("X-Request-ID"));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty())
            externalLogObject.put("STATUS", "SUCCESS");
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));

        databaseSteps.externalLogArrayObject.add(externalLogObject);

        //Additional external object
        addlexternalLogObject.putAll(externalLogObject);

        databaseSteps.externalLogArrayObject.add(addlexternalLogObject);
    }

    @And("^I have the PAN REF external log request body$")
    public void panRefExternalLog() throws ParseException {
        String tuumEndpoint = apiProperties.getProperty("TUUM_DATA");
        tuumEndpoint = tuumEndpoint.replace("${LCM_DATA}", apiProperties.getProperty("LCM_DATA"));
        tuumEndpoint = tuumEndpoint.replace("{panref}", panRefAccount);

        addlexternalLogObject.put("ENDPOINT", tuumEndpoint);
        addlexternalLogObject.put("API_CALL", "TUUM");
        addlexternalLogObject.put("ACTION", "TUUM");
        addlexternalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        addlexternalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
    }

    @And("^I have the PAN external log request body$")
    public void panExternalLog() {
        if (postRequestObject.get("accountInfo") != null) {
            HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
            String accountNumber = account.get("account");
            accountNumber = accountNumber.substring(0, 6) + "xxxxxx" + accountNumber.substring(12);
            account.put("account", accountNumber);
            account.put("accountExpiry", "MMYYYY");
            postRequestObject.put("accountInfo", account);
        }

        postRequestObject.put("withCId", false);
        postRequestObject.put("accountAlreadyExists", false);
    }
}