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
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.YearMonth;

import javax.json.JsonObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class MDESStep {
    private static final Logger log;
    private WalletConfig walletConfig;

    static {
        log = LoggerFactory.getLogger(RESTAssSteps.class.getName());
    }

    @Steps
    CSVDataManipulator csvDataManipulator;
    @Steps
    RESTAssuredAPI restAssuredAPI;
    @Steps
    RESTAssSteps restAssuredAPISteps;
    @Steps
    DatabaseSteps databaseSteps;
    private boolean testCardsEnabledAtScenarioLevel = true;
    private String scenarioName = "";
    private Map<String, String> queryParamsAsMap;
    private final JSONObject encryptionRequestBody = new JSONObject();
    private final JSONObject asEncryptionRequestBody = new JSONObject();
    private final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String NUMERIC = "0123456789";
    private final LCMProperties apiProperties = new LCMProperties("api.properties");
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final LCMProperties slaConfig = new LCMProperties("SLA.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();
    private final String cardScheme = System.getProperty("cardScheme") != null ? System.getProperty("cardScheme").toUpperCase() : runner.getProperty("CARD_SCHEME").toUpperCase();

    private String issuerName = System.getProperty("issuer") != null ? System.getProperty("issuer") : runner.getProperty("ISSUER");
    private final String testCardsEnabledAtSuiteLevel = System.getProperty("testCardsEnabled") != null ? System.getProperty("testCardsEnabled") : runner.getProperty("TEST_CARDS_ENABLED");
    private final String allScenarios = System.getProperty("allScenarios") != null ? System.getProperty("allScenarios") : runner.getProperty("ALL_SCENARIOS");
    private final String commonLogging = System.getProperty("commonLoggingEnabled") != null ? System.getProperty("commonLoggingEnabled") : runner.getProperty("COMMON_LOGGING_ENABLED");
    private final String dataDriveFilePath = "src/main/resources/data/";
    private final String mdesRequestFilePath = "MDES/request/";
    private final String commonLogFilePath = "Database/CommonLog/";
    private final JSONObject addlexternalLogObject = new JSONObject();
    JSONArray recommendationReasons = new JSONArray();
    private String endpoint;
    private String encrypted_key = null;
    private String encrypted_data = null;
    private String encrypted_public_key_fingerprint = null;
    private String encrypted_iv = null;
    private JSONObject postRequestObject = new JSONObject();

    static JSONObject AuthorizeServiceRequestObject = new JSONObject();
    private JSONObject previousRequestObject = new JSONObject();
    private JSONObject postResponseObject = new JSONObject();
    private JSONObject eventLogObject = new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private String accountID;
    private String deviceID;
    private String tokenRequesterID;
    private BinRange binRange;
    public static String requestId = null;
    public static String apiCall = null;
    public static String tokenUniqueReference = null;
    public static String tokenReferenceID = null;

    public static String panUniqueReference = null;
    static String tokenRequesterName;
    static AccountInfo accountInfo;

    static IssuerIDVConfig issuerIDVConfig;
    static IDVMethod idvMethod;

    private String smsIdentifier;
    private String emailIdentifier;

    static DeviceInfo deviceInfo;
    static VirtualRiskInfo virtualRiskInfo;
    static Issuer issuer;
    static TokenInfo tokenInfo;
    static VirtualAccount virtualAccount;
    private CodeMapping codeMapping;


    @Given("^I have the default MDES headers$")
    public void iHaveTheDefaultMDESHeadersAsDefinedIn() {
        DatabaseSteps.headersAsMap = new HashMap<>();
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        DatabaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + mdesRequestFilePath + defaultHeader));
    //  DatabaseSteps.headersAsMap.put("SM_USER", "visatokenservicescertout.visa.com");
    }

    @Given("^I have the MDES headers$")
    public void iHaveTheMDESHeadersAsDefinedIn() {
        iHaveTheDefaultMDESHeadersAsDefinedIn();
    }

    @Given("^I have the MDES Encryption headers as defined in \"([^\"]*)\"$")
    public void iHaveTheMDESEncryptionHeadersAsDefinedIn(String headersPath) {
        DatabaseSteps.headersAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    public void validateTokenRequester(String tokenRequester, boolean hasHugeLimit) throws Exception {

        String query;
        if (tokenRequester.equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            tokenRequesterID = "400100" + CommonUtil.generateString(new Random(), NUMERIC, 5).replaceFirst("^0+(?!$)", "1");
            tokenRequesterName = CommonUtil.generateString(new Random(), ALPHA, 32);
        } else {
            if (tokenRequester.equalsIgnoreCase("FETCH_FROM_DATABASE")) {
                boolean virtualCardLimit = false;
                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and internal_value != 'Google Pay' and internal_value != 'Apple pay' ORDER BY DBMS_RANDOM.RANDOM";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                codeMapping = new CodeMapping();
                codeMapping.fetchTokenRequesters(databaseSteps.result);

                String hugeLimit = hasHugeLimit ? " and no_of_virtual_card != '0'" : " and no_of_virtual_card = '0'";
                query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "')" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                if (DBConnection.recordCount() == 0) {
                    query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '*'" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }

                if (DBConnection.recordCount() == 0) {
                    query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '*'" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }

                if (DBConnection.recordCount() == 0) {
                    query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "')" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }

                if (DBConnection.recordCount() > 0) {
                    WalletConfig walletConfig = new WalletConfig(databaseSteps.result);

                    if (hasHugeLimit && !walletConfig.NO_OF_VIRTUAL_CARD.equalsIgnoreCase("0")) {
                        if (!Objects.equals(walletConfig.TOKEN_REQUESTER, "*"))
                            query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and external_value = '" + walletConfig.TOKEN_REQUESTER + "'";
                        else {
                            String zeroNonzeroVAs = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + walletConfig.BIN_RANGE_LOW + "' and no_of_virtual_card = '0'";
                            databaseSteps.result = databaseSteps.dbConnection.runQuery(zeroNonzeroVAs);

                            if (DBConnection.recordCount() == 0) {
                                query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and no_of_virtual_card = '0'";
                                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                            }

                            walletConfig.fetchTokenRequesters(databaseSteps.result);
                            if (DBConnection.recordCount() > 0) {
                                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and external_value in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "') and external_value not in ('" + StringUtils.join(walletConfig.TOKEN_REQUESTERS, "','") + "') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                            } else {
                                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and external_value in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "')  ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                            }
                        }

                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        codeMapping = new CodeMapping(databaseSteps.result);
                        virtualCardLimit = true;
                    } else if (!hasHugeLimit) {
                        query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and no_of_virtual_card = '0' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        walletConfig = new WalletConfig(databaseSteps.result);

                        query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and external_value = '" + walletConfig.TOKEN_REQUESTER + "'";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        if (DBConnection.recordCount() != 0)
                            codeMapping = new CodeMapping(databaseSteps.result);
                        virtualCardLimit = true;
                    }
                }

                Assert.assertTrue("No valid token requester found for the given account and bin range", virtualCardLimit);
            } else {
                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'MC%' and internal_value = '" + tokenRequester + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                codeMapping = new CodeMapping(databaseSteps.result);
            }

            tokenRequesterID = codeMapping.EXTERNAL_VALUE;
            tokenRequesterName = codeMapping.INTERNAL_VALUE;
        }

        MDESStep.log.info(tokenRequesterName + " - " + tokenRequesterID);
    }

    @Given("^Pre-requisite: To validate authorize service with the given details token requester as \"([^\"]*)\", \"([^\"]*)\", account state as \"([^\"]*)\", account ref type as \"([^\"]*)\" and approval based on \"([^\"]*)\" for \"([^\"]*)\" flow with \"([^\"]*)\" and \"([^\"]*)\" with \"([^\"]*)\" null field$")
    public void toValidateTheAuthorizeServiceEndpoint(String tokenRequester, String panSource, String accountState, String accRefType, String approvalBy, String flow, String tokenType, String decision, String nullField) throws Exception {
        createAPEncryptedData(tokenRequester, panSource, accountState, accRefType);
        iHaveTheMDESHeadersAsDefinedIn();
        iHaveAuthorizeServiceRequestBodyAsDefinedIn(approvalBy, flow, tokenType, decision, nullField);
        iPostTheDetailsToAuthorizeServiceAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyTableEntriesAfterAS(flow);
        verifyASEntriesInCL();
    }

    @When("^I Verify that Request Activation Methods is successfull for the given details$")
    public void toValidateTheRequestActMethodEndpoint() throws Exception {
        iHaveTheRequestActivationMethodsRequestBodyAsDefinedIn();
        iPostTheDetailsToRequestActivationMethodsAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyActivationMethodsAsExpected();
        iverifyRAMEntriesInCL();
    }

    @When("^I Verify that Deliver Activation code is successfull for the given details$")
    public void toValidateTheDeliverActCodeEndpoint() throws Exception {
        iHaveTheDeliverActivationCodeRequestBodyAsDefinedIn();
        iPostTheDetailsToDeliverActivationCodeAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyActivationCodeDeliveredAsExpected();
        iverifyDACEntriesInCL();
    }

    @Then("^I Verify that Notify Service is activated as expected for the given details \"([^\"]*)\" and \"([^\"]*)\"$")
    public void toValidateTheNotifyServiceActivatedEndpoint(String tokenType, String decision) throws Exception {
        iHaveTheNotifyServiceActivatedRequestBodyAsDefinedIn(tokenType, decision);
        iPostTheDetailsToNotifyServiceActivatedAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyNotifyServiceActivatedAsExpected();
        iverifyNSAEntriesInCL();
    }

    @And("^I Verify that Notify Token is Updated as \"([^\"]*)\" for the given details$")
    public void toValidateTheNotifyTokenUpdatedEndpoint(String status) throws Exception {
        iHaveTheNotifyServiceUpdatedRequestBodyAsDefinedIn(status);
        iPostTheDetailsToNotifyServiceUpdatedAPIEndpoint();
        restAssuredAPISteps.iVerifyTheStatusCode(200);
        iVerifyNotifyTokenUpdatedAsExpected();
        iverifyNTUEntriesInCL();
        iVerifyTableEntriesAfterNTU(status);
    }

    @And("^I verify that the Request Activation Methods are retrieved successfully as expected$")
    public void iVerifyTheRetrieveCVMAsExpected() throws Exception {
        String query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '" + tokenRequesterID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        if (DBConnection.recordCount() == 0) {
            query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '*'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() == 0) {
                query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '" + tokenRequesterID + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() == 0) {
                    query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '*'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }
            }
        }

        Assert.assertTrue("No valid issuer idv config found in database for the bin range '" + binRange.BIN_RANGE_LOW + "' and token requester '" + tokenRequesterID + "'", DBConnection.recordCount() > 0);
        issuerIDVConfig = new IssuerIDVConfig(databaseSteps.result);

        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("No cardholder verification methods retrieved for the given token details. Please refer the response payload", postResponseObject);

        JSONArray postResponseArrayObject = JSONHelper.parseJSONArray(postResponseObject.get("activationMethods").toString());
        Assert.assertTrue("No cardholder verification methods retrieved for the given token details. Please refer the response payload", postResponseObject != null && !postResponseArrayObject.isEmpty());

        for (Object o : postResponseArrayObject) {
            JSONObject obj = (JSONObject) o;
            if (obj.get("type").toString().contains("SMS")) {
                Assert.assertTrue("SMS idv is enabled in issuer idv config but returned in the cvm response payload", issuerIDVConfig.SMS_IDV_ENABLED.equalsIgnoreCase("y"));
                smsIdentifier = obj.get("value").toString();

                query = "select * from idv_method where account_id = '" + accountID + "' and channel = 'SMS'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertTrue("OTP type is not as expected for sms in cvm response payload", obj.get("type").toString().equalsIgnoreCase("otpsms"));
                String expectedContactInfo = idvMethod.CONTACT_INFO.substring(0, 4) + StringUtils.repeat("*", idvMethod.CONTACT_INFO.length() - 6) + idvMethod.CONTACT_INFO.substring(idvMethod.CONTACT_INFO.length() - 2);
                if (idvMethod.CONTACT_INFO.contains(" "))
                    expectedContactInfo = idvMethod.CONTACT_INFO.replaceAll(" ", "*");
                Assert.assertEquals("Contact info is not as expected for sms in cvm response payload", obj.get("value"), expectedContactInfo);
            } else if (obj.get("type").toString().contains("EMAIL")) {
                emailIdentifier = obj.get("value").toString();

                query = "select * from idv_method where account_id = '" + accountID + "' and channel = 'EMAIL'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertTrue("OTP type is not as expected for email in cvm response payload", obj.get("type").toString().equalsIgnoreCase("otpemail"));
                String[] expectedContactInfoList = idvMethod.CONTACT_INFO.split("@");
                String expectedContactInfo = (expectedContactInfoList[0].length() > 3 ? expectedContactInfoList[0].substring(0, 3) : expectedContactInfoList[0]) + StringUtils.repeat("*", expectedContactInfoList[0].length() - 3) + "@" + expectedContactInfoList[1];
                Assert.assertEquals("Contact info is not as expected for email in cvm response payload", expectedContactInfo, obj.get("value"));
            } else if (obj.get("type").toString().contains("APP_TO_APP")) {
                Assert.assertEquals("App2App Identifier is not as expected in cvm response payload", obj.get("identifier"), issuerIDVConfig.APP_APP_IDENTIFIER);
            } else if (obj.get("type").toString().contains("CUSTOMERCARE") || obj.get("type").toString().contains("CUSTOMERSERVICE")) {
                if (issuerIDVConfig.CC_IDV_ENABLED.equalsIgnoreCase("y") && issuerIDVConfig.CC_IDV_DEFAULT_VALUE.equalsIgnoreCase("y"))
                    Assert.assertEquals("Customer care identifier is not as expected in cvm response payload", obj.get("value"), issuerIDVConfig.CC_IDV_DEFAULT_VALUE);
            } else
                Assert.fail("Unexpected identifier present for the given account " + obj.get("type"));
        }
    }

    @And("^I have the request activation methods request body$")
    public void iHaveTheRequestActivationMethodsRequestBodyAsDefinedIn() throws IOException, ParseException {

        String requestBodyPath = "valid/body/valid_request_body_RAM";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + mdesRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        //   previousRequestObject.putAll(postRequestObject);
        //   Assert.assertNotNull(AuthorizeServiceRequestObject);
        //    AuthorizeServiceRequestObject.remove("deviceInfo");
        //   AuthorizeServiceRequestObject.remove("walletProviderDecisioningInfo");
        //   AuthorizeServiceRequestObject.remove("activeTokenCount");
        //  JSONArray arr = new JSONArray();
        //  arr.add("ADD_CARD");
        // AuthorizeServiceRequestObject.put("reasonCodes", arr);

        if (postRequestObject.get("requestId") != null && postRequestObject.get("requestId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("requestId", AuthorizeServiceRequestObject.get("requestId"));
        if (postRequestObject.get("correlationId") != null && postRequestObject.get("correlationId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("correlationId", AuthorizeServiceRequestObject.get("correlationId"));
        if (postRequestObject.get("walletId") != null && postRequestObject.get("walletId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("walletId", AuthorizeServiceRequestObject.get("walletId"));
        if (postRequestObject.get("tokenRequestorId") != null && postRequestObject.get("tokenRequestorId").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("tokenRequestorId", AuthorizeServiceRequestObject.get("tokenRequestorId"));
        if (postRequestObject.get("tokenType") != null && postRequestObject.get("tokenType").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("tokenType",AuthorizeServiceRequestObject.get("tokenType"));

        HashMap<Object, Object> fundingAccountInfo = (HashMap<Object, Object>) postRequestObject.get("fundingAccountInfo");
        Assert.assertNotNull(fundingAccountInfo);

        if (fundingAccountInfo.get("panUniqueReference") != null && fundingAccountInfo.get("panUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            fundingAccountInfo.put("panUniqueReference", panUniqueReference);

        if (fundingAccountInfo.get("tokenUniqueReference") != null && fundingAccountInfo.get("tokenUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            fundingAccountInfo.put("tokenUniqueReference", tokenUniqueReference);

        HashMap<Object, Object> encryptedPayload = (HashMap<Object, Object>) fundingAccountInfo.get("encryptedPayload");
        Assert.assertNotNull(encryptedPayload);

        if (encryptedPayload.get("encryptedData") != null && encryptedPayload.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("encryptedData", encrypted_data);

        if (encryptedPayload.get("encryptedKey") != null && encryptedPayload.get("encryptedKey").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("encryptedKey", encrypted_key);

        if (encryptedPayload.get("publicKeyFingerprint") != null && encryptedPayload.get("publicKeyFingerprint").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("publicKeyFingerprint", encrypted_public_key_fingerprint);

        if (encryptedPayload.get("iv") != null && encryptedPayload.get("iv").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("iv", encrypted_iv);
    }


    @And("^I have the deliver activation code request body as defined$")
    public void iHaveTheDeliverActivationCodeRequestBodyAsDefinedIn() throws Exception {
        String requestBodyPath = "valid/body/valid_request_body_DAC";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + mdesRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("requestId") != null && postRequestObject.get("requestId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("requestId", AuthorizeServiceRequestObject.get("requestId"));
        if (postRequestObject.get("correlationId") != null && postRequestObject.get("correlationId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("correlationId", AuthorizeServiceRequestObject.get("correlationId"));
        if (postRequestObject.get("activationCode") != null && postRequestObject.get("activationCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("activationCode", CommonUtil.generateString(new Random(), ALPHANUMERIC, 8));
       System.out.println(postRequestObject.get("activationCode"));
        if (postRequestObject.get("tokenUniqueReference") != null && postRequestObject.get("tokenUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenUniqueReference", tokenUniqueReference);

        HashMap<Object, Object> activationMethod = (HashMap<Object, Object>) postRequestObject.get("activationMethod");
        Assert.assertNotNull(activationMethod);

        if (activationMethod.get("value") != null && activationMethod.get("value").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            activationMethod.put("value", "+" + CommonUtil.generateString(new Random(), NUMERIC, 20));
        System.out.println(activationMethod.get("value"));
    }

    @And("^I have the notify service activated request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheNotifyServiceActivatedRequestBodyAsDefinedIn( String tokenType,String decision) throws Exception {

        String requestBodyPath = "valid/body/valid_request_body_NSA";

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + mdesRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);
        postRequestObject.putAll(AuthorizeServiceRequestObject);
        postRequestObject.remove("reasonCodes");
        postRequestObject.remove("accountIdHash");
        postRequestObject.remove("mobileNumberSuffix");
        postRequestObject.remove("walletProviderDecisioningInfo");
        postRequestObject.remove("activeTokenCount");

        if (postRequestObject.get("decision") != null && postRequestObject.get("decision").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("decision", decision.toUpperCase());

        if (postRequestObject.get("tokenType") != null && postRequestObject.get("tokenType").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("tokenType", tokenType);

        String serviceRequesttimestamp = String.valueOf(java.time.LocalDateTime.now());
        String serviceRequestDateTime = serviceRequesttimestamp.substring(0, 23);

        if (postRequestObject.get("termsAndConditionsAcceptedTimestamp") != null && postRequestObject.get("termsAndConditionsAcceptedTimestamp").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("termsAndConditionsAcceptedTimestamp",serviceRequestDateTime+"Z");

        String termsAndConditionstimestamp = String.valueOf(java.time.LocalDateTime.now());
        String termsAndConditionsDateTime = termsAndConditionstimestamp.substring(0, 23);

        if (postRequestObject.get("termsAndConditionsAcceptedTimestamp") != null && postRequestObject.get("termsAndConditionsAcceptedTimestamp").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("termsAndConditionsAcceptedTimestamp",termsAndConditionsDateTime+"Z");

        String tokenActivattimestamp = String.valueOf(java.time.LocalDateTime.now());
        String tokenActivatedDateTime = tokenActivattimestamp.substring(0, 23);

        if (postRequestObject.get("tokenActivatedDateTime") != null && postRequestObject.get("tokenActivatedDateTime").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
              postRequestObject.put("tokenActivatedDateTime",tokenActivatedDateTime+"Z");

        HashMap<Object, Object> fundingAccountInfo = (HashMap<Object, Object>) postRequestObject.get("fundingAccountInfo");
        Assert.assertNotNull(fundingAccountInfo);

        if (fundingAccountInfo.get("panUniqueReference") != null && fundingAccountInfo.get("panUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            fundingAccountInfo.put("panUniqueReference", panUniqueReference);
            fundingAccountInfo.put("tokenUniqueReference", tokenUniqueReference);
        }

    }

    @And("^I have the notify service updated request body as defined in \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheNotifyServiceUpdatedRequestBodyAsDefinedIn(String status) throws Exception {
        previousRequestObject = postRequestObject;
        String requestBodyPath="valid/body/valid_request_body_NTU";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + mdesRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("requestId") != null && postRequestObject.get("requestId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("requestId", AuthorizeServiceRequestObject.get("requestId"));

        HashMap<Object, Object> tokens = (HashMap<Object, Object>) ((JSONArray) postRequestObject.get("tokens")).get(0);
        Assert.assertNotNull(tokens);

        HashMap<Object, Object> fundingAccountInfo = (HashMap<Object, Object>) AuthorizeServiceRequestObject.get("fundingAccountInfo");
        Assert.assertNotNull(fundingAccountInfo);

        if (tokens.get("tokenUniqueReference") != null && tokens.get("tokenUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokens.put("tokenUniqueReference", fundingAccountInfo.get("tokenUniqueReference"));
        if (tokens.get("status") != null && tokens.get("status").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokens.put("status", status);
    }

    @When("^I post the details to authorize service endpoint$")
    public void iPostTheDetailsToAuthorizeServiceAPIEndpoint() {
        endpoint = apiProperties.getProperty("MDES_AUTHORIZE_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));

        apiCall="AUTHORIZE SERVICE";
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to request activation methods endpoint$")
    public void iPostTheDetailsToRequestActivationMethodsAPIEndpoint() {
        endpoint = apiProperties.getProperty("MDES_REQUEST_ACTIVATION_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));

        apiCall="GET CARDHOLDER VERIFICATION METHODS";
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to deliver activation code endpoint$")
    public void iPostTheDetailsToDeliverActivationCodeAPIEndpoint() {
        endpoint = apiProperties.getProperty("MDES_DELIVER_ACTIVATION_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));

        apiCall="SEND PASSCODE";
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to notify service activated endpoint$")
    public void iPostTheDetailsToNotifyServiceActivatedAPIEndpoint() {
        endpoint = apiProperties.getProperty("MDES_NOTIFY_SERVICE_ACTIVATED_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));

        apiCall="TOKEN ACTIVATED";
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to notify service updated endpoint$")
    public void iPostTheDetailsToNotifyServiceUpdatedAPIEndpoint() {
        endpoint = apiProperties.getProperty("MDES_NOTIFY_SERVICE_UPDATED_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));

        apiCall="TOKEN UPDATED";
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @And("^I verify that the decision is as expected \"([^\"]*)\"$")
    public void iVerifyDecisionAsExpected(String decision) throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        System.out.println(postResponseObject);
        String actualDecision = postResponseObject.get("decision") != null ? postResponseObject.get("decision").toString() : null;
        Assert.assertEquals("The decision of MDES authorize service is not as expected", decision, actualDecision);
    }

    @And("^I verify that the activation methods are retrieved as expected$")
    public void iVerifyActivationMethodsAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        JSONArray activationMethods = (JSONArray) postResponseObject.get("activationMethods");
        System.out.println(postResponseObject);
        Assert.assertEquals("Both Email & Text to card holder methods are not available for the given request", 2, activationMethods.size());
        String activationMethod1 = ((JSONObject) activationMethods.get(0)).get("type").toString();
        String activationMethod2 = ((JSONObject) activationMethods.get(1)).get("type").toString();

        Assert.assertTrue("The activation method 1 is not as expected for given request as " + activationMethod1, activationMethod1.equals("TEXT_TO_CARDHOLDER_NUMBER") || activationMethod1.equals("EMAIL_TO_CARDHOLDER_ADDRESS"));
        Assert.assertTrue("The activation method 2 is not as expected for given request as " + activationMethod2, activationMethod2.equals("TEXT_TO_CARDHOLDER_NUMBER") || activationMethod2.equals("EMAIL_TO_CARDHOLDER_ADDRESS"));
    }

    @And("^I verify that the activation code delivered as expected$")
    public void iVerifyActivationCodeDeliveredAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        System.out.println(postResponseObject);
        Assert.assertNotNull("The activation code is not delivered unexpectedly. Please refer the response payload", postResponseObject.get("responseId"));
        Assert.assertTrue("The activation by email option is not as expected for given request", StringUtils.isNumericSpace(postResponseObject.get("responseId").toString()));
    }

    @And("^I verify that the Notify Service is activated as expected$")
    public void iVerifyNotifyServiceActivatedAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        System.out.println(postResponseObject);
        Assert.assertNotNull("The notify service isn't activated unexpectedly. Please refer the response payload", postResponseObject.get("responseId"));
        Assert.assertTrue("The reponse id is not as expected for given request", StringUtils.isNumericSpace(postResponseObject.get("responseId").toString()));
    }

    @And("^I verify that the notify token is updated as expected$")
    public void iVerifyNotifyTokenUpdatedAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("The notify Token isn't updated unexpectedly. Please refer the response payload", postResponseObject.get("responseId"));
        Assert.assertTrue("The response id is not as expected for given request", StringUtils.isNumericSpace(postResponseObject.get("responseId").toString()));
    }
    @And("^I verify that table entries are as expected after notify token updated for tokenType as \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterNTU(String status) throws Exception {
        String query = "select * from virtual_account where MC_REQUEST_ID = '" + requestId + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records created into virtual account table after Notify Token Updated", DBConnection.recordCount() > 0);
        String virtualAccountID = virtualAccount.VIRTUAL_ACCOUNT_ID;

        Assert.assertEquals("API_CALL field in virtual account table is not as expected after Notify Token Updated", "TOKEN_CREATED", virtualAccount.API_CALL); // currently token created, it should be token updated
        Assert.assertEquals("Account ID field in virtual account table is not as expected after Notify Token Updated", accountID, virtualAccount.ACCOUNT_ID);
        Assert.assertEquals("Status field in virtual account table is not as expected after Notify Token Updated", status, virtualAccount.STATUS);
        Assert.assertEquals("Card org field in virtual account table is not as expected after Notify Token Updated", "MC", virtualAccount.CARD_ORG);
        Assert.assertNotNull("Pan source field in virtual account table is not as expected after Notify Token Updated", virtualAccount.PAN_SOURCE);
        // Assert.assertEquals("Enrolment flow field in virtual account table is not as expected after approve provisioning", flowType.toUpperCase(), virtualAccount.ENROLMENT_FLOW);

        query = "select * from token_info where virtual_Account_ID = '" + virtualAccountID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);

        HashMap<Object, Object> tokenData = (HashMap<Object, Object>) encryptionRequestBody.get("tokenData");
        Assert.assertNotNull(tokenData);

        Assert.assertEquals("Token field in token info table is not as expected after Notify Token Updated", tokenData.get("token"), tokenInfo.TOKEN);
        Assert.assertEquals("API_CALL field in token info table is not as expected after Notify Token Updated", "TOKEN_CREATED", tokenInfo.API_CALL); // currently token created, it should be token updated
        Assert.assertEquals("Token requestor field in token info table is not as expected after Notify Token Updated", tokenRequesterID, tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertNotNull("Token Type field in token info table is not as expected after Notify Token Updated", tokenInfo.TOKEN_TYPE);

        query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records created into device info table after authorize service", DBConnection.recordCount() > 0);
        Assert.assertNotNull("Device id in device info table is not as expected authorize service", deviceInfo.DEVICE_ID);
        deviceID = deviceID == null || deviceID.isEmpty() ? deviceInfo.DEVICE_ID : deviceID;
        Assert.assertEquals("Device id in device info table is not as expected after authorize service", deviceID, deviceInfo.DEVICE_ID);
    }

    @Then("^I verify that MDES authorize service entries are created to event and external logs of Common logging service$")
    public void verifyASEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            mdesEventLog("eventlog_table.json");
            mdesExternalLog("externallog_table.json");

            authorizeServiceEventLog();
            authorizeServiceExternalLog();

            databaseSteps.verifyMDESCommonLoggingService();
            eventLogObject.clear();
            externalLogObject.clear();
            databaseSteps.eventLogArrayObject.clear();
            databaseSteps.externalLogArrayObject.clear();
            }
    }

    @And("^I have the MDES event log request body as defined in \"([^\"]*)\"$")
    public void mdesEventLog(String requestBodyPath) throws Exception {
        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);
        String actualErrorCode = postResponseObject.get("errorCode") != null ? postResponseObject.get("errorCode").toString() : null;

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", requestId);
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", null);
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty())
            eventLogObject.put("STATUS", "SUCCESS");
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "AUTHORIZE SERVICE");
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "MDES_API");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "LCM_SERVICE");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("400") ) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        }
        if ((actualErrorCode !=null)){
            eventLogObject.put("STATUS", "FAILED");
        }
    }

    public void authorizeServiceEventLog() {
        eventLogObject.put("ISSUER_ID", "UK-10057184001");
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void authorizeServiceExternalLog() {

        String serviceEndpoint = apiProperties.getProperty("MDES_AUTHORIZE_DATA");
        serviceEndpoint = serviceEndpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "AUTHORIZE_SERVICE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "AUTHORIZE SERVICE");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void NotifyServiceActivatedEventLog() {

        eventLogObject.put("ACTION", "TOKEN ACTIVATED");
        eventLogObject.put("ISSUER_ID", "UK-10057184001");
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void NotifyServiceActivatedExternalLog() {

        String serviceEndpoint = apiProperties.getProperty("MDES_NOTIFY_SERVICE_ACTIVATED_DATA");
        serviceEndpoint = serviceEndpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "NOTIFY_SERVICE_ACTIVATED");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "TOKEN ACTIVATED");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Then("^I verify that MDES Notify Service Activated entries are created to event and external logs of Common logging service$")
    public void iverifyNSAEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            mdesEventLog("eventlog_table.json");
            mdesExternalLog("externallog_table.json");

            NotifyServiceActivatedEventLog();
            NotifyServiceActivatedExternalLog();

            databaseSteps.verifyMDESCommonLoggingService();
            databaseSteps.eventLogArrayObject.clear();
            databaseSteps.externalLogArrayObject.clear();
        }
    }

    public void NotifyTokenUpdatedEventLog() {

        eventLogObject.put("ACTION", "TOKEN UPDATED");
        eventLogObject.put("ISSUER_ID", issuer.ISSUER_ID);
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void NotifyTokenUpdatedExternalLog() {

        String serviceEndpoint = apiProperties.getProperty("MDES_NOTIFY_SERVICE_UPDATED_DATA");
        serviceEndpoint = serviceEndpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "NOTIFY_SERVICE_UPDATED");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "TOKEN UPDATED");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Then("^I verify that MDES Notify Token Updated entries are created to event and external logs of Common logging service$")
    public void iverifyNTUEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            mdesEventLog("eventlog_table.json");
            mdesExternalLog("externallog_table.json");

            NotifyTokenUpdatedEventLog();
            NotifyTokenUpdatedExternalLog();

            databaseSteps.verifyMDESCommonLoggingService();
            databaseSteps.eventLogArrayObject.clear();
            databaseSteps.externalLogArrayObject.clear();
        }
    }

    public void RequestActivationMethodEventLog() {
        eventLogObject.put("ACTION", "GET CARDHOLDER VERIFICATION METHODS");
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void RequestActivationMethodExternalLog() {

        String serviceEndpoint = apiProperties.getProperty("MDES_REQUEST_ACTIVATION_DATA");
        serviceEndpoint = serviceEndpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET_REQUEST_ACTIVATION_METHOD");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET CARDHOLDER VERIFICATION METHODS");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Then("^I verify that MDES Request Activation Method entries are created to event and external logs of Common logging service$")
    public void iverifyRAMEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            mdesEventLog("eventlog_table.json");
            mdesExternalLog("externallog_table.json");

            RequestActivationMethodEventLog();
            RequestActivationMethodExternalLog();

            databaseSteps.verifyMDESCommonLoggingService();
            databaseSteps.eventLogArrayObject.clear();
            databaseSteps.externalLogArrayObject.clear();
        }
    }

    public void DeliverActivationCodeEventLog() {
        eventLogObject.put("ACTION", "SEND PASSCODE");
        eventLogObject.put("ISSUER_ID", issuer.ISSUER_ID);
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void DeliverActivationCodeExternalLog() {

        String serviceEndpoint = apiProperties.getProperty("MDES_DELIVER_ACTIVATION_DATA");
        serviceEndpoint = serviceEndpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "DELIVER_ACTIVATION_CODE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "SEND PASSCODE");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);

        databaseSteps.externalLogArrayObject.add(externalLogObject);

        addlexternalLogObject.putAll(externalLogObject);
        addlexternalLogObject.put("ACTION", "DELIVER_ACTIVATION_CODE");
        addlexternalLogObject.put("API_CALL", "DELIVER_ACTIVATION_CODE");

        databaseSteps.externalLogArrayObject.add(addlexternalLogObject);
    }

    @Then("^I verify that MDES Deliver Activation Code entries are created to event and external logs of Common logging service$")
    public void iverifyDACEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            mdesEventLog("eventlog_table.json");
            mdesExternalLog("externallog_table.json");

            DeliverActivationCodeEventLog();
            DeliverActivationCodeExternalLog();

            databaseSteps.verifyMDESCommonLoggingService();
            databaseSteps.eventLogArrayObject.clear();
            databaseSteps.externalLogArrayObject.clear();
        }
    }

    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    public void iHaveTheMDESExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }

    @And("^I have the MDES external log request body as defined in \"([^\"]*)\"$")
    public void mdesExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheMDESExternalLogRequestBodyAsDefinedIn(requestBodyPath);

        postRequestObject.remove("fundingAccountInfo");
        postRequestObject.remove("deviceInfo");
        postRequestObject.put("apiCall", "AUTHORIZE_SERVICE");
        if (postRequestObject.containsKey("walletProviderDecisioningInfo")) {
            JSONObject obj = (JSONObject) postRequestObject.get("walletProviderDecisioningInfo");
            obj.remove("accountLifeTime");
        }

        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", requestId);
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty())
            externalLogObject.put("STATUS", "SUCCESS");
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", null);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
    String actualErrorCode = postResponseObject.get("errorCode") != null ? postResponseObject.get("errorCode").toString() : null;
        if ((actualErrorCode !=null)){
            externalLogObject.put("STATUS", "FAILED");
        }

    }

    @And("^I encrypt PAN for MDES when token requester as \"([^\"]*)\", \"([^\"]*)\", account state as \"([^\"]*)\", account ref type as \"([^\"]*)\"$")
    public void createAPEncryptedData(String tokenRequester, String panSource, String accountState, String accRefType) throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String mdesBody = "valid_request_body_MDES_authorizeService";
        String bodyPath = "Encryption/valid/body/" + mdesBody + ".json";

        iHaveTheMDESEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheASEncryptionRequestBodyAsDefinedIn(bodyPath, tokenRequester, panSource, accountState, accRefType);
        iPostDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            JsonObject postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_key = postResponseObj.get("encryptedKey").toString().replace("\"", "");
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
            encrypted_public_key_fingerprint = postResponseObj.get("publicKeyFingerprint").toString().replace("\"", "");
            String encrypted_oaep_hashing_algorithm = postResponseObj.get("oaepHashingAlgorithm").toString().replace("\"", "");
            encrypted_iv = postResponseObj.get("iv").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I have the AS Encryption request body as defined in  \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\",\"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheASEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String tokenRequester, String panSource, String accountState, String accRefType) throws Exception {

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(postRequestObject);
        String query;

        databaseSteps.iEstablishConnectionToLCMDatabase();

        query = "select * from issuer where lower(issuer_name) like lower ('"+ issuerName + "%')";

        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        } else {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
            issuer.ISSUER_ID = issuerName.contains("Savings") ? "FI-10057876401" : issuer.ISSUER_ID;
        }

        HashMap<Object, Object> cardAccountData = (HashMap<Object, Object>) postRequestObject.get("cardAccountData");
        Assert.assertNotNull(cardAccountData);

        if (cardAccountData.get("accountNumber") != null && cardAccountData.get("accountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            if (accountID == null)
                fetchValidMdesTSPAccountFromDatabase(tokenRequester, accRefType, accountState);

            cardAccountData.put("accountNumber", accountInfo.ACCOUNT);
        }
        if (cardAccountData.get("expiryMonth") != null && cardAccountData.get("expiryMonth").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            cardAccountData.put("expiryMonth", accountInfo.ACCOUNT_EXPIRY.substring(0, 2));

        if (cardAccountData.get("expiryYear") != null && cardAccountData.get("expiryYear").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            cardAccountData.put("expiryYear", accountInfo.ACCOUNT_EXPIRY.substring(4, 6));

        HashMap<Object, Object> accountHolderData = (HashMap<Object, Object>) postRequestObject.get("accountHolderData");
        Assert.assertNotNull(accountHolderData);

        if (accountHolderData.get("accountHolderName") != null && accountHolderData.get("accountHolderName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderData.put("accountHolderName", CommonUtil.generateString(new Random(), ALPHANUMERIC, 27));

        HashMap<Object, Object> accountHolderAddress = (HashMap<Object, Object>) accountHolderData.get("accountHolderAddress");
        Assert.assertNotNull(accountHolderAddress);

        if (accountHolderAddress.get("line1") != null && accountHolderAddress.get("line1").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("line1", CommonUtil.generateString(new Random(), ALPHANUMERIC, 50));

        if (accountHolderAddress.get("line2") != null && accountHolderAddress.get("line2").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("line2", CommonUtil.generateString(new Random(), ALPHANUMERIC, 50));

        if (accountHolderAddress.get("city") != null && accountHolderAddress.get("city").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("city", CommonUtil.generateString(new Random(), ALPHANUMERIC, 20));

        if (accountHolderAddress.get("country") != null && accountHolderAddress.get("country").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("country", CommonUtil.generateString(new Random(), ALPHA, 3));

        if (accountHolderAddress.get("postalCode") != null && accountHolderAddress.get("postalCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("postalCode", CommonUtil.generateString(new Random(), NUMERIC, 6));

        if (accountHolderAddress.get("countrySubdivision") != null && accountHolderAddress.get("countrySubdivision").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountHolderAddress.put("countrySubdivision", CommonUtil.generateString(new Random(), ALPHA, 2));

        HashMap<Object, Object> tokenData = (HashMap<Object, Object>) postRequestObject.get("tokenData");
        Assert.assertNotNull(tokenData);

        if (tokenData.get("token") != null && tokenData.get("token").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokenData.put("token", CommonUtil.generateString(new Random(), NUMERIC, 15));

        int currentYear = YearMonth.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        Random r = new Random();
        int year,month;
        do {
            year = currentYear + r.nextInt(5);
            month = r.nextInt(12) + 1;
        } while (year == currentYear && month < currentMonth);
        String monthString = String.format("%02d", month);
        String yearString = String.valueOf(year % 100);

        if (tokenData.get("expiryMonth") != null && tokenData.get("expiryMonth").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokenData.put("expiryMonth",monthString);

        if (tokenData.get("expiryYear") != null && tokenData.get("expiryYear").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokenData.put("expiryYear",yearString);

        System.out.println("Token ID " + tokenData.get("token"));

        if (postRequestObject.get("source") != null && postRequestObject.get("source").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("source", panSource.toUpperCase());

        encryptionRequestBody.putAll(postRequestObject);
        asEncryptionRequestBody.clear();
        asEncryptionRequestBody.putAll(postRequestObject);
    }

    public void fetchValidMdesTSPAccountFromDatabase(String tokenRequester, String accRefType, String accountState) throws Exception {
        if (Objects.equals(accRefType, "null"))
            accRefType = "account_ref_type is null";
        else
            accRefType = "account_ref_type = '" + accRefType + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        String state = null;
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/MASTERCARD/" +  issuerName.toLowerCase() + ".json";

            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are added for Mastercard", testCardSets);

            if (accountState.equalsIgnoreCase("ACTIVE")){
                state="mdesTestCards";
            }else if (accountState.equalsIgnoreCase("DELETED")){
                state="deletedTestCard";
            }else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                state = "mdesSuspendedTestCards";
            }
            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(state).toString());
            Assert.assertTrue("No test cards are added for Mastercard", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));

            String query = "select * from bin_range where issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            binRange = new BinRange();
            binRange.fetchBinRanges(databaseSteps.result);

            int totalBinRanges = binRange.BIN_RANGES_LOW.size();
            int idx1;
            String searchAccount = null;
            if (scenarioName.contains("invalid cvv") && issuerName.equalsIgnoreCase("lunar bank dk"))
                searchAccount = "account = '4871450319565359'";
            else if (testCard.get("account") != null) {
                searchAccount = "account = '" + testCard.get("account").toString() + "'";
            }

            query = "select * from account_info where " + searchAccount + " and issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() > 0) {
                query = "select * from account_info where " + searchAccount + " and issuer_id = '" + issuer.ISSUER_ID + "' and account_state = '" + accountState + "' and " + accRefType + " ";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                Assert.assertTrue("Account '" + testCard.get("account") + "' is not found in the LCM database", DBConnection.recordCount() > 0);
                accountInfo = new AccountInfo(databaseSteps.result);
            } else {
                accountInfo = new AccountInfo();
                accountInfo.ACCOUNT = testCard.get("account").toString();
                accountInfo.ACCOUNT_EXPIRY = testCard.get("accountExpiry").toString();
            }

            for (idx1 = 0; idx1 < totalBinRanges; idx1++) {
                query = "select * from bin_range_lcm_service where lcm_service = 'TSP' and bin_range_low = '" + binRange.BIN_RANGES_LOW.get(idx1) + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() > 0) {
                    BinRangeLCMService binRangeLCMService = new BinRangeLCMService(databaseSteps.result);
                    String binRangeLow = binRange.BIN_RANGES_LOW.get(idx1);
                    String binRangeHigh = binRange.BIN_RANGES_HIGH.get(idx1);
                    String panLength = binRange.PANS_LENGTH.get(idx1);
                    long lowBinRangeExpected, highBinRangeExpected, panActual;

                    binRangeLow = binRangeLow.substring(0, Integer.parseInt(panLength));
                    binRangeHigh = binRangeHigh.substring(0, Integer.parseInt(panLength));
                    lowBinRangeExpected = Long.parseLong(binRangeLow);
                    highBinRangeExpected = Long.parseLong(binRangeHigh);
                    panActual = Long.parseLong(accountInfo.ACCOUNT);

                    if (lowBinRangeExpected < panActual && panActual < highBinRangeExpected) {
                        query = "select * from bin_range where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGES_LOW.get(idx1) + "'";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        binRange = new BinRange(databaseSteps.result);

                        if (tokenRequesterID == null)
                            validateTokenRequester(tokenRequester, true);
                        break;
                    }
                }
            }

            Assert.assertTrue("No valid TSP bin range or account found in the database for the given issuer " + issuerName, idx1 < totalBinRanges);

            accountID = accountInfo.ACCOUNT_ID;
        } else {
            String query = "select * from bin_range where issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            BinRange binRanges = new BinRange();
            binRanges.fetchBinRanges(databaseSteps.result);

            int totalBinRanges = binRanges.BIN_RANGES_LOW.size();
            int totalAccounts;
            int idx1, idx2;

            for (idx1 = 0; idx1 < totalBinRanges; idx1++) {
                query = "select * from bin_range_lcm_service where lcm_service = 'TSP' and bin_range_low = '" + binRanges.BIN_RANGES_LOW.get(idx1) + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() > 0) {
                    String binRangeLow = binRanges.BIN_RANGES_LOW.get(idx1);
                    binRangeLow = binRangeLow.replaceAll("0+$", "");

                    query = "select * from bin_range where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRanges.BIN_RANGES_LOW.get(idx1) + "'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    binRange = new BinRange(databaseSteps.result);

                    query = "select * from account_info where account like '" + binRangeLow + "%' and issuer_id = '" + issuer.ISSUER_ID + "' and account_state = '" + accountState + "' and " + accRefType;
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    accountInfo = new AccountInfo();
                    accountInfo.fetchAccounts(databaseSteps.result);
                    totalAccounts = accountInfo.ACCOUNTS.size();
                    List<String> accounts = accountInfo.ACCOUNTS;
                    totalAccounts = Math.min(totalAccounts, 25);

                    for (idx2 = 0; idx2 < totalAccounts; idx2++) {
                        Random random = new Random();
                        int randomIdx = random.nextInt(totalAccounts - idx2);
                        String randomAccount = accounts.get(randomIdx);
                        accounts.remove(randomIdx);

                        query = "select * from account_info where account = '" + randomAccount + "' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                        if (DBConnection.recordCount() > 0) {
                            accountInfo = new AccountInfo(databaseSteps.result);

                            query = "select * from issuer_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "'";
                            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                            if (DBConnection.recordCount() == 0) {
                                query = "select * from issuer_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*'";
                                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                            }

                            if (DBConnection.recordCount() > 0) {
                                IssuerConfig issuerConfig = new IssuerConfig(databaseSteps.result);
                                if (!accRefType.contains("null") && issuerConfig.CALL_ISSUER_ALWAYS.equalsIgnoreCase("N")) {
                                    query = "select * from account_lcm_service where account_id = '" + accountInfo.ACCOUNT_ID + "' and lcm_service = 'TSP'";
                                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                                    if (DBConnection.recordCount() > 0) {
                                        Date dNow = new Date();
                                        SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                                        Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                                        if (dNow.before(dActual)) {
                                            validateTokenRequester(tokenRequester, true);

                                            accountID = accountInfo.ACCOUNT_ID;
                                            break;
                                        }
                                    }
                                } else {
                                    Date dNow = new Date();
                                    SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                                    Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                                    if (dNow.before(dActual)) {
                                        validateTokenRequester(tokenRequester, true);

                                        accountID = accountInfo.ACCOUNT_ID;
                                        break;
                                    }
                                }
                            } else {
                                Assert.fail("Issuer and its bin range combination is not configured to issuer config table");
                            }
                        }
                    }

                    if (accountID != null)
                        break;
                }
            }

            Assert.assertTrue("No valid TSP bin range or account found in the database for the given issuer " + issuerName, idx1 < totalBinRanges);
        }
        MDESStep.log.info(accountInfo.ACCOUNT + " - " + accountID);
    }

    @When("^I post details to MDES Encryption endpoint$")
    public void iPostDetailsToEncryptionAPIEndpoint() {
        endpoint = apiProperties.getProperty("ENCRYPTION_DECRYPTION_MDES_DATA");

        restAssuredAPI.post(encryptionRequestBody, DatabaseSteps.headersAsMap, endpoint);
    }

    @And("^I have authorize service request body as defined and approval based on \"([^\"]*)\" for \"([^\"]*)\" flow with \"([^\"]*)\" and \"([^\"]*)\" with \"([^\"]*)\" null field$")
    public void iHaveAuthorizeServiceRequestBodyAsDefinedIn(String approvalBy, String flow, String tokenType, String decision,String nullField) throws Exception {
        String requestBodyPath ="valid/body/valid_request_body_AS";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + mdesRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        requestId = "";
        requestId = CommonUtil.generateString(new Random(), NUMERIC, 50);
        Random r = new Random();
        if (postRequestObject.get("requestId") != null && postRequestObject.get("requestId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("requestId", requestId);
        System.out.println("Mastercard Request ID " + postRequestObject.get("requestId"));

        if (postRequestObject.get("correlationId") != null && postRequestObject.get("correlationId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("correlationId", CommonUtil.generateString(new Random(), NUMERIC, 7));
        if (postRequestObject.get("walletId") != null && postRequestObject.get("walletId").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("walletId", CommonUtil.generateString(new Random(), NUMERIC, 3));
        if (postRequestObject.get("tokenRequestorId") != null && postRequestObject.get("tokenRequestorId").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("tokenRequestorId", tokenRequesterID);
        if (postRequestObject.get("tokenType") != null && postRequestObject.get("tokenType").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("tokenType", tokenType.toUpperCase());

        HashMap<Object, Object> fundingAccountInfo = (HashMap<Object, Object>) postRequestObject.get("fundingAccountInfo");
        Assert.assertNotNull(fundingAccountInfo);

        panUniqueReference="";
        panUniqueReference=CommonUtil.generateString(new Random(), ALPHANUMERIC, 20);
        if (fundingAccountInfo.get("panUniqueReference") != null && fundingAccountInfo.get("panUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            fundingAccountInfo.put("panUniqueReference", panUniqueReference);

        tokenUniqueReference="";
        tokenUniqueReference=CommonUtil.generateString(new Random(), ALPHANUMERIC, 20);
        if (fundingAccountInfo.get("tokenUniqueReference") != null && fundingAccountInfo.get("tokenUniqueReference").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            fundingAccountInfo.put("tokenUniqueReference", tokenUniqueReference);

        HashMap<Object, Object> encryptedPayload = (HashMap<Object, Object>) fundingAccountInfo.get("encryptedPayload");
        Assert.assertNotNull(encryptedPayload);

        if (encryptedPayload.get("encryptedData") != null && encryptedPayload.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("encryptedData", encrypted_data);

        if (encryptedPayload.get("encryptedKey") != null && encryptedPayload.get("encryptedKey").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("encryptedKey", encrypted_key);

        if (encryptedPayload.get("publicKeyFingerprint") != null && encryptedPayload.get("publicKeyFingerprint").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("publicKeyFingerprint", encrypted_public_key_fingerprint);

        if (encryptedPayload.get("iv") != null && encryptedPayload.get("iv").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            encryptedPayload.put("iv", encrypted_iv);

        HashMap<Object, Object> deviceInfo = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfo);

        if (deviceInfo.get("deviceName") != null && deviceInfo.get("deviceName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            String deviceName = CommonUtil.generateString(new Random(), ALPHANUMERIC, 64);
            Base64.Encoder enc = Base64.getEncoder();
            String encodedDeviceName = enc.encodeToString(deviceName.getBytes());
            deviceInfo.put("deviceName", encodedDeviceName);
        }
        if (deviceInfo.get("serialNumber") != null && deviceInfo.get("serialNumber").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfo.put("serialNumber", CommonUtil.generateString(new Random(), ALPHANUMERIC, 6));

        if (deviceInfo.get("imei") != null && deviceInfo.get("imei").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfo.put("imei", CommonUtil.generateString(new Random(), NUMERIC, 15));

        if (deviceInfo.get("msisdn") != null && deviceInfo.get("msisdn").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfo.put("msisdn", CommonUtil.generateString(new Random(), NUMERIC, 10));

        HashMap<Object, Object> walletProviderDecisioningInfo = (HashMap<Object, Object>) postRequestObject.get("walletProviderDecisioningInfo");
        Assert.assertNotNull(walletProviderDecisioningInfo);

        if (walletProviderDecisioningInfo.get("recommendedDecision") != null && walletProviderDecisioningInfo.get("recommendedDecision").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            walletProviderDecisioningInfo.put("recommendedDecision", decision);

        recommendationReasons = (JSONArray) walletProviderDecisioningInfo.get("recommendationReasons");

        if (recommendationReasons.contains("FETCH_FROM_DATATABLE") && flow.equalsIgnoreCase("green")||flow.equalsIgnoreCase("red")||flow.equalsIgnoreCase("yellow")) {
            recommendationReasons.clear();
            recommendationReasons.add("ACCOUNT_TOO_NEW");
        } else if (recommendationReasons.contains("FETCH_FROM_DATATABLE") && flow.equalsIgnoreCase("orange") && approvalBy.equalsIgnoreCase("recommendation")) {
            recommendationReasons.clear();
            recommendationReasons.add("HIGH_RISK");
        }

        if (approvalBy.equalsIgnoreCase("recommendationReasons") && flow.equalsIgnoreCase("green") || flow.equalsIgnoreCase("red")) {
            String[] WPRC1 = {"HIGH_RISK", "TOO_MANY_DIFFERENT_CARDHOLDERS", "ACCOUNT_CARD_TOO_NEW", "ACCOUNT_TOO_NEW_SINCE_LAUNCH", "TOO_MANY_RECENT_TOKENS"};
            String[] WPRC2 = {"HIGH_RISK", "TOO_MANY_DIFFERENT_CARDHOLDERS", "ACCOUNT_CARD_TOO_NEW", "ACCOUNT_TOO_NEW_SINCE_LAUNCH", "TOO_MANY_RECENT_ATTEMPTS"};
            String[] WPRC3 = {"HIGH_RISK", "TOO_MANY_DIFFERENT_CARDHOLDERS", "ACCOUNT_CARD_TOO_NEW", "ACCOUNT_RECENTLY_CHANGED", "TOO_MANY_RECENT_TOKENS"};
            String[] WPRC4 = {"HIGH_RISK", "TOO_MANY_DIFFERENT_CARDHOLDERS", "ACCOUNT_CARD_TOO_NEW", "ACCOUNT_RECENTLY_CHANGED", "TOO_MANY_RECENT_ATTEMPTS"};

            String[][] reasoncodes = {WPRC1, WPRC2, WPRC3, WPRC4};
            int randomIndex = r.nextInt(reasoncodes.length);
            String[] randomArray = reasoncodes[randomIndex];
            JSONArray codes=new JSONArray();
            Collections.addAll(codes, randomArray);
            recommendationReasons.clear();
            walletProviderDecisioningInfo.put("recommendationReasons",codes);
        }
        int deviceScore = r.nextInt(5 - 2) + 2;
        if (walletProviderDecisioningInfo.get("deviceScore") != null && walletProviderDecisioningInfo.get("deviceScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            walletProviderDecisioningInfo.put("deviceScore", Integer.toString(deviceScore));
        if (walletProviderDecisioningInfo.get("deviceScore") != null && approvalBy.equalsIgnoreCase("DeviceScore") && flow.equalsIgnoreCase("red"))
            walletProviderDecisioningInfo.put("deviceScore", "1");
        if (nullField.equalsIgnoreCase("walletRisks")|| approvalBy.equalsIgnoreCase("walletRisks")) {
            walletProviderDecisioningInfo.put("recommendedDecision", null);
            walletProviderDecisioningInfo.put("deviceScore", null);
        }
        if (nullField.equalsIgnoreCase("walletProviderDecisioningInfo")|| approvalBy.equalsIgnoreCase("walletProviderDecisioningInfo")) {
            postRequestObject.put("walletProviderDecisioningInfo",null);
        }
        AuthorizeServiceRequestObject.putAll(postRequestObject);
        }

    @And("^I verify that the given pan is declined for authorize service as expected$")
    public void iVerifyPanIsDeclinedForAuthorizeServiceAsExpected() throws Exception {

        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        System.out.println(postResponseObject);
        String actualDecision = postResponseObject.get("decision") != null ? postResponseObject.get("decision").toString() : null;
        Assert.assertEquals("The action code is not as expected for red flow", "DECLINED", actualDecision);
    }

    @And("^I verify that the given pan is required additional authentication for authorize service as expected$")
    public void iVerifyPanIsPartiallyApprovedForAuthorizeServiceAsExpected() throws Exception {

        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        String actualDecision = postResponseObject.get("decision") != null ? postResponseObject.get("decision").toString() : null;
        Assert.assertEquals("The action code is not as expected for yellow flow", "REQUIRE_ADDITIONAL_AUTHENTICATION", actualDecision);
    }

    @And("^I verify that table entries are as expected after authorize service for \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterAS(String flowType) throws Exception {
        String query = "select * from virtual_account where MC_REQUEST_ID = '" + requestId + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records created into virtual account table after authorize service", DBConnection.recordCount() > 0);
        String virtualAccountID = virtualAccount.VIRTUAL_ACCOUNT_ID;

        Assert.assertEquals("API_CALL field in virtual account table is not as expected after authorize service", "AUTHORIZE_SERVICE", virtualAccount.API_CALL);
        Assert.assertEquals("Account ID field in virtual account table is not as expected after authorize service", accountID, virtualAccount.ACCOUNT_ID);
        Assert.assertEquals("Status field in virtual account table is not as expected after authorize service", "INACTIVE", virtualAccount.STATUS);
        Assert.assertEquals("Card org field in virtual account table is not as expected after authorize service", "MC", virtualAccount.CARD_ORG);
        Assert.assertNotNull("Pan source field in virtual account table is not as expected after authorize service", virtualAccount.PAN_SOURCE);
     // Assert.assertEquals("Enrolment flow field in virtual account table is not as expected after approve provisioning", flowType.toUpperCase(), virtualAccount.ENROLMENT_FLOW);

        query = "select * from token_info where virtual_Account_ID = '" + virtualAccountID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);
        tokenReferenceID=tokenInfo.TOKEN_REFERENCE_ID;

        HashMap<Object, Object> tokenData = (HashMap<Object, Object>) encryptionRequestBody.get("tokenData");
        Assert.assertNotNull(tokenData);

        Assert.assertEquals("Token field in token info table is not as expected after authorize service", tokenData.get("token"), tokenInfo.TOKEN);
        Assert.assertEquals("API_CALL field in token info table is not as expected after authorize service", "AUTHORIZE_SERVICE", tokenInfo.API_CALL);
    //  Assert.assertEquals("Token requestor field in token info table is not as expected after authorize service", tokenRequesterID, tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertEquals("Virtual account id field in token info table is not as expected after authorize service", virtualAccountID, tokenInfo.VIRTUAL_ACCOUNT_ID);
        Assert.assertNotNull("Token Type field in token info table is not as expected after authorize service", tokenInfo.TOKEN_TYPE);

        query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records created into device info table after authorize service", DBConnection.recordCount() > 0);
        Assert.assertNotNull("Device id in device info table is not as expected authorize service", deviceInfo.DEVICE_ID);
        deviceID = deviceID == null || deviceID.isEmpty() ? deviceInfo.DEVICE_ID : deviceID;
        Assert.assertEquals("Device id in device info table is not as expected after authorize service", deviceID, deviceInfo.DEVICE_ID);

        HashMap<Object, Object> DeviceInfo = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(DeviceInfo);

        String encodedDeviceName = DeviceInfo.get("deviceName").toString();
        Assert.assertEquals("Device name field in device info table is not as expected after create token", encodedDeviceName, deviceInfo.DEVICE_NAME);
        Assert.assertEquals("Device Name field in device info table is not as expected after authorize service", DeviceInfo.get("deviceName"), deviceInfo.DEVICE_NAME);
        Assert.assertEquals("OS Type field in device info table is not as expected after authorize service", DeviceInfo.get("osName"), deviceInfo.OS_TYPE);
        Assert.assertEquals("Device ID Type field in device info table is not as expected after authorize service", DeviceInfo.get("formFactor"), deviceInfo.DEVICE_ID_TYPE);
        Assert.assertEquals("OS version Type field in device info table is not as expected after authorize service", DeviceInfo.get("osVersion"), deviceInfo.OS_VERSION);

        query = "select * from virtual_risk_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualRiskInfo = new VirtualRiskInfo(databaseSteps.result);

        Assert.assertTrue("No records created into virtual risk info table after authorize service", DBConnection.recordCount() > 0);

        if(!flowType.equalsIgnoreCase("yellow")) {
            HashMap<Object, Object> walletProviderDecisioningInfo = (HashMap<Object, Object>) postRequestObject.get("walletProviderDecisioningInfo");
            Assert.assertNotNull(walletProviderDecisioningInfo);

            Assert.assertNotNull("Wallet provider account score in virtual risk info table is not as expected after authorize service", virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
            Assert.assertNotNull("Wallet provider device score in virtual risk info table is not as expected after authorize service", virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
            Assert.assertNotNull("Wallet provider Reason Code in virtual risk info table is not as expected after authorize service", virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
            Assert.assertEquals("Wallet provider account score field in virtual risk info table is not as expected after authorize service", walletProviderDecisioningInfo.get("accountScore"), virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
            // Assert.assertEquals("Wallet provider device score field in virtual risk info table is not as expected after authorize service", walletProviderDecisioningInfo.get("deviceScore"), virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
            Assert.assertEquals("IMEI field in virtual risk info table is not as expected after authorize service", DeviceInfo.get("imei"), virtualRiskInfo.DEVICE_IMEI);
            Assert.assertEquals("serial Number field in virtual risk info table is not as expected after authorize service", DeviceInfo.get("serialNumber"), virtualRiskInfo.DEVICE_SERIAL_NUMBER);
        }
    }

    @And("^I verify that token provisioning is declined and error code is as expected \"([^\"]*)\"$")
    public void iVerifyErrorCodeIsAsExpected(String errorCode) throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        String actualErrorCode = postResponseObject.get("errorCode") != null ? postResponseObject.get("errorCode").toString() : null;
        Assert.assertEquals("The error code for authorize service is not as expected", errorCode.toUpperCase(), actualErrorCode);
    }

    @And("^I verify that the virtual card limit of given issuer bin range and token requester for MDES$")
    public void iVerifyVirtualCardLimit() throws Exception {
        validateTokenRequester("FETCH_FROM_DATABASE", false);
        String query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '" + tokenRequesterID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        if (DBConnection.recordCount() == 0) {
            query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '" + tokenRequesterID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            Assert.assertEquals("No virtual card limit configuration is added for the issuerName id '" + issuer.ISSUER_ID + "' and token requester '" + tokenRequesterID + "'", 1, DBConnection.recordCount());
        }
        walletConfig = new WalletConfig(databaseSteps.result);
        Assert.assertTrue("Virtual card limit is configured with huge for the issuer id '" + issuer.ISSUER_ID + "' and token requester '" + tokenRequesterID + "'", Integer.parseInt(walletConfig.NO_OF_VIRTUAL_CARD) < 20);
    }

    @When("^I post the details to authorize service endpoint exceeding virtual card limit$")
    public void iPostTheDetailsToAuthorizeServiceAPIEndpointexceedingVirtualCardLimit() {
        endpoint = apiProperties.getProperty("MDES_AUTHORIZE_DATA");
        endpoint = endpoint.replace("${MDES_DATA}", apiProperties.getProperty("MDES_" + environment + "_DATA"));
        // endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        postRequestObject.put("tokenRequestorId",tokenRequesterID);

        for (int idx=0; idx <= Integer.parseInt(walletConfig.NO_OF_VIRTUAL_CARD); idx++) {
            restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
        }
    }

    @When("^I Verify that Deliver Activation code is unsuccessfull for the given details with incorrect activation method \"([^\"]*)\" and \"([^\"]*)\"$")
    public void toValidatetheDeliverActCodeEndpointwithInvalidactivationMethod( String activationMethodType,String activationMethodValue) throws Exception {
        iHaveTheDeliverActivationCodeRequestBodyAsDefinedIn();
        iaddActivationMethodTypeinRequestBody(activationMethodType,activationMethodValue);
        iPostTheDetailsToDeliverActivationCodeAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
    }
    private void iaddActivationMethodTypeinRequestBody(String activationMethod, String activationMethodValue) {
        String actMethod;
        String actMethodVal;
        if(activationMethod.equalsIgnoreCase("define_at_runtime"))
        {
            actMethod = CommonUtil.generateString(new Random(), ALPHANUMERIC, 20);
        }else {
            actMethod=activationMethod;
        }

        if(activationMethodValue.equalsIgnoreCase("define_at_runtime"))
        {
            actMethodVal = CommonUtil.generateString(new Random(), ALPHANUMERIC, 20);
        }else {
            actMethodVal=activationMethodValue;
        }
        HashMap<Object, Object> activationMethod1 = (HashMap<Object, Object>) postRequestObject.get("activationMethod");
        activationMethod1.put("type",actMethod);
        activationMethod1.put("value",actMethodVal);
        System.out.println(activationMethod1.get("type"));
        System.out.println(activationMethod1.get("value"));
    }
}