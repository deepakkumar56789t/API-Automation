package com.lcm.test.steps;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.steps.RESTAssSteps;
import com.lcm.core.utilities.*;
import com.lcm.test.database.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
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
import org.junit.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("unchecked")
public class VTSStep {

    private static final Logger log;

    static {
        log = LoggerFactory.getLogger(VTSStep.class.getName());
    }

    @Steps
    CSVDataManipulator csvDataManipulator;
    @Steps
    RESTAssuredAPI restAssuredAPI;
    @Steps
    RESTAssSteps restAssSteps;
    @Steps
    DatabaseSteps databaseSteps;
    private boolean testCardsEnabledAtScenarioLevel = true;
    private String scenarioName = "";
    private String endpoint;
    private String encrypted_data = null;

    private JSONObject encryptionRequestBody = new JSONObject();
    private final JSONObject ceEncryptionRequestBody = new JSONObject();
    private final JSONObject apEncryptionRequestBody = new JSONObject();
    private final JSONObject ctEncryptionRequestBody = new JSONObject();
    private final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String ALPHASPECIALNUMERIC = "!\"#$%&'()*+,-./:;<=>?@[\\]^_{|}~`¬£abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
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
    private final String galleryIdEnabled = System.getProperty("galleryIdEnabled") != null ? System.getProperty("galleryIdEnabled") : runner.getProperty("GALLERY_ID_ENABLED");
    String[] walletProviderReasonCodesRedList = {"0G,0B,03,01,0A", "0G,0B,03,01,09", "0G,0B,03,04,0A", "0G,0B,03,04,09"};
    private final String dataDriveFilePath = "src/main/resources/data/";
    private final String authHeader = "/valid/headers/authHeader_" + environment + ".csv";
    private final String VTSRequestFilePath = "VTS/request/";
    private JSONObject postRequestObject = new JSONObject();
    private final JSONObject cePostRequestObject = new JSONObject();
    static JSONObject apPostRequestObject = new JSONObject();
    private final JSONObject ctPostRequestObject = new JSONObject();
    private final JSONObject utPostRequestObject = new JSONObject();
    private final JSONObject dbPostRequestObject = new JSONObject();
    private final JSONObject cvmPostRequestObject = new JSONObject();
    private final JSONObject spPostRequestObject = new JSONObject();

    private JSONObject postResponseObject = new JSONObject();
    private JsonObject postResponseObj = null;
    private JSONObject eventLogObject = new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private final String commonLogFilePath = "Database/CommonLog/";
    private final JSONObject addlExternalLogObject = new JSONObject();
    public static Map<String, String> csvProperties;
    private String token_type;
    private String accountID;
    private String profileID;
    private String virtualAccountID;
    private String deviceID;
    static String tokenRequesterName;
    private String tokenRequesterID;
    public static String tokenReferenceID = null;
    private String smsIdentifier;
    private String emailIdentifier;
    private String galleryID;
    static AccountInfo accountInfo;
    private AccountAdditionalInfo accountAdditionalInfo;
    private BinRange binRange;
    private BinRangeLCMService binRangeLCMService;
    private CodeMapping codeMapping;
    static DeviceInfo deviceInfo;
    static Issuer issuer;
    static IDVMethod idvMethod;
    private IssuerConfig issuerConfig;
    static IssuerIDVConfig issuerIDVConfig;
    static TokenInfo tokenInfo;
    static VirtualAccount virtualAccount;
    private VirtualRiskInfo virtualRiskInfo;
    private WalletConfig walletConfig;

//    @After(order = 1)
    public void pushSoftFailures() {
        restAssSteps.softAssert.assertAll();
    }

    @Before(order = 1)
    public void establishConnectToLCMDatabase() throws Exception {
        databaseSteps.iEstablishConnectionToLCMDatabase();
    }

    @Before(value = "@VTS_Comdirect", order = 1)
    public void verifySMVEnabledForIssuer(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.contains(issuerName.trim())) {
            if (allScenarios.equalsIgnoreCase("Yes"))
                issuerName = "Comdirect";
            else
                throw new AssumptionViolatedException("Issuer is not enabled for sim verification functionality. Hence, the test case is skipped.");
        }
    }

    @Before(value = "@VTS_LunarBankDk", order = 1)
    public void verifySMVDisabledorIssuer(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.contains(issuerName.trim())) {
            if (allScenarios.equalsIgnoreCase("Yes"))
                issuerName = "Lunar Bank DK";
            else
                throw new AssumptionViolatedException("Issuer is already enabled for sim verification functionality. Hence, the test case is skipped.");
        }
    }

    @Before(value = "@VTS_SavingsBankFI", order = 1)
    public void verifyAPP2APPEnabledForIssuer(final Scenario scenario) throws Exception {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.toString().contains(issuerName.replace(" ", ""))) {
            if (allScenarios.equalsIgnoreCase("Yes"))
                issuerName = "Savings Bank FI";
            else
                throw new AssumptionViolatedException("Issuer is not supported for APP2APP idv functionality. Hence, the test case is skipped.");
        }
    }

    @Before(value = "@VTS_Bonum", order = 1)
    public void verifyCCPEnabledForIssuer(final Scenario scenario) throws Exception {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.contains(issuerName.trim())) {
            if (allScenarios.equalsIgnoreCase("Yes"))
                issuerName = "Bonum";
            else
                throw new AssumptionViolatedException("Issuer is not supported for customer care idv functionality. Hence, the test case is skipped.");
        }
    }

    @Before(value = "@VerifyThresholdRangeGreen", order = 2)
    public void verifyThresholdLimitGreenAsExpected() throws Exception {
        String query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";

        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        }
        else {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
            issuer.ISSUER_ID = issuerName.contains("Savings") ? "FI-10057876401" : issuer.ISSUER_ID;
        }

        fetchValidTSPAccountFromDatabase("Google Pay", "any", "CARDID", "ACTIVE", "any");

        if (binRange.RISK_SCR_THRESHOLD_1 == null && allScenarios.equalsIgnoreCase("No"))
            throw new AssumptionViolatedException("The given test card is not eligible for tsp approval based on risk assessment score, as minimum threshold value of its bin range is null. Hence, the test case is skipped.");
        else if (binRange.RISK_SCR_THRESHOLD_1 == null || binRange.RISK_SCR_THRESHOLD_1.equalsIgnoreCase("0"))
            throw new AssumptionViolatedException("As per issuer(" + issuer.ISSUER_NAME + ") and its bin range(" + binRange.BIN_RANGE_LOW + ") configuration('Minimum threshold value is " + binRange.RISK_SCR_THRESHOLD_1 + "') green flow by risk assessment score is not applicable. Hence, the test case is skipped.");
    }

    @Before(value = "@VerifyThresholdRangeRed", order = 2)
    public void verifyThresholdLimitRedAsExpected() throws Exception {
        String query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";

        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        }
        else {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
            issuer.ISSUER_ID = issuerName.contains("Savings") ? "FI-10057876401" : issuer.ISSUER_ID;
        }

        fetchValidTSPAccountFromDatabase("Google Pay", "any", "CARDID", "ACTIVE", "any");

        if (binRange.RISK_SCR_THRESHOLD_2 == null && allScenarios.equalsIgnoreCase("No"))
            throw new AssumptionViolatedException("The given test card is not eligible for tsp approval based on risk assessment score, as maximum threshold value of its bin range is null. Hence, the test case is skipped.");
        else if (binRange.RISK_SCR_THRESHOLD_2 == null || Objects.requireNonNull(binRange.RISK_SCR_THRESHOLD_2).equalsIgnoreCase("99"))
            throw new AssumptionViolatedException("As per issuer(" + issuer.ISSUER_NAME + ") and its bin range(" + binRange.BIN_RANGE_LOW + ") configuration('Maximum threshold value is " + binRange.RISK_SCR_THRESHOLD_2 + "') red flow by risk assessment score is not applicable. Hence, the test case is skipped.");
    }

    @Before(value = "@SkipTestCards", order = 3)
    @Given("Verify test cards are enabled or not")
    public void verifyTestCardsEnabled(Scenario scenario) {
        testCardsEnabledAtScenarioLevel = !scenario.getSourceTagNames().contains("@SkipTestCards");
        scenarioName = scenario.getName();
    }

    @Given("^I have the default VTS headers$")
    public void iHaveTheDefaultVTSHeadersAsDefinedIn() {
        DatabaseSteps.headersAsMap = new HashMap<>();
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        DatabaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + VTSRequestFilePath + defaultHeader));

        if (DatabaseSteps.headersAsMap.get("X-Request-ID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());

        csvProperties = new HashMap<>();
        csvProperties.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + VTSRequestFilePath + authHeader));
    }

    @And("^I encrypt PAN for check eligibility when token requester as \"([^\"]*)\", account state as \"([^\"]*)\", profile id as \"([^\"]*)\", account ref type as \"([^\"]*)\" with \"([^\"]*)\" null field and idvMethod as \"([^\"]*)\"$")
    public void createCEEncryptedData(String tokenRequester, String accountState, String profileId, String accRefType, String nullField, String idvMethod) throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String ceBody = "valid_request_body_VTS_checkEligibility";
        String bodyPath = "Encryption/valid/body/" + ceBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheCEEncryptionRequestBodyAsDefinedIn(bodyPath, tokenRequester, profileId, accRefType, accountState, nullField, idvMethod);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I encrypt PAN for approve provisioning when token requester as \"([^\"]*)\", account state as \"([^\"]*)\", profile id as \"([^\"]*)\", account ref type as \"([^\"]*)\", idvMethod as \"([^\"]*)\" and approval based on \"([^\"]*)\" for \"([^\"]*)\" flow with \"([^\"]*)\" null field$")
    public void createAPEncryptedData(String tokenRequester, String accountState, String profileId, String accRefType, String idvMethod, String approvalBy, String flow, String nullField) throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String apBody = "valid_request_body_VTS_approveProvisioning";
        String bodyPath = "Encryption/valid/body/" + apBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheAPEncryptionRequestBodyAsDefinedIn(bodyPath, flow, tokenRequester, profileId, accountState, accRefType, approvalBy, nullField, idvMethod);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for create token endpoint for the given details \"([^\"]*)\"$")
    public void createCTEncryptedData(String tokenType) throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String ctBody = "valid_request_body_VTS_createToken";
        String bodyPath = "Encryption/valid/body/" + ctBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheCTEncryptionRequestBodyAsDefinedIn(bodyPath, tokenType);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for update token endpoint and status as \"([^\"]*)\"$")
    public void createUTEncryptedData(String tokenStatus) throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String utBody = "valid_request_body_VTS_updateToken";
        String bodyPath = "Encryption/valid/body/" + utBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheUTEncryptionRequestBodyAsDefinedIn(bodyPath, tokenStatus);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for get cvm endpoint$")
    public void createGetCVMEncryptedData() throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String cvmBody = "valid_request_body_VTS_getCVM";
        String bodyPath = "Encryption/valid/body/" + cvmBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheGetCVMEncryptionRequestBodyAsDefinedIn(bodyPath);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }


    @And("^I create the encrypted data for send passcode endpoint$")
    public void createSPEncryptedData() throws Exception {
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String spBody = "valid_request_body_VTS_sendPasscode";
        String bodyPath = "Encryption/valid/body/" + spBody + ".json";

        iHaveTheTSPEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheSPEncryptionRequestBodyAsDefinedIn(bodyPath);
        iPostTheDetailsToEncryptionAPIEndpoint();

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^Pre-requisite: I verify check eligibility for given PAN when token requester as \"([^\"]*)\", account state as \"([^\"]*)\", profile id as \"([^\"]*)\", account ref type as \"([^\"]*)\", idvMethod as \"([^\"]*)\" and pan source as \"([^\"]*)\" with \"([^\"]*)\" null field$")
    public void iVerifyCheckEligibility(String tokenRequester, String accountState, String profileId, String accRefType, String idvMethod, String panSource, String nullField) throws Exception {
        createCEEncryptedData(tokenRequester, accountState, profileId, accRefType, nullField, idvMethod);
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheCheckEligibilityRequestBodyAsDefinedIn(panSource);
        iPostTheDetailsToCEAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyPanIsEligibleForVisaTokenAsExpected();
        iVerifyTableEntriesAfterCE(tokenRequester);
        verifyCEEntriesInCL();
        iVerifyCESLA();
    }

    @And("^I verify that approve provisioning is successful for given PAN when token requester as \"([^\"]*)\", account state as \"([^\"]*)\", profile id as \"([^\"]*)\", account ref type as \"([^\"]*)\", idvMethod as \"([^\"]*)\", pan source as \"([^\"]*)\", token type as \"([^\"]*)\", approval based on \"([^\"]*)\" for \"([^\"]*)\" flow with \"([^\"]*)\" null field and action code expected is \"([^\"]*)\"$")
    public void iVerifyApproveProvisioning(String tokenRequester, String accountState, String profileId, String accRefType, String idvMethod, String panSource, String tokenType, String approvalBy, String flow, String nullField, String actionCode) throws Exception {
        token_type = tokenType;
        createAPEncryptedData(tokenRequester, accountState, profileId, accRefType, idvMethod, approvalBy, flow, nullField);
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheApproveProvisioningRequestBodyAsDefinedIn(panSource, tokenType, "null", idvMethod);
        iPostTheDetailsToAPAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyPanIsApprovedForVisaTokenAsExpected(actionCode);
        if (panSource.equalsIgnoreCase("mobile_banking_app") &&
                approvalBy.equalsIgnoreCase("riskAssessmentScore") &&
                (tokenRequester.equalsIgnoreCase("Google Pay") ||
                        tokenRequester.equalsIgnoreCase("Apple pay")))
            flow = "green";
        iVerifyTableEntriesAfterAP(tokenType, flow);
        verifyAPEntriesInCL();
        iVerifyAPSLA();
    }

    @And("^I verify that create token is done for the given account details \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iVerifyCreateToken(String panSource, String tokenType, String flow) throws Exception {
        createCTEncryptedData(tokenType);
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheCreateTokenRequestBodyAsDefinedIn("valid/body/valid_request_body_CT", panSource, "TOKEN_CREATED");
        iPostTheDetailsToCTAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyTableEntriesAfterCT(tokenType, "TOKEN_CREATED", flow);
        verifyCTEntriesInCL();
        iVerifyCTSLA();
    }

    @And("^I verify that get CVM is done for the given account details$")
    public void iVerifyGetCVM() throws Exception {
        createGetCVMEncryptedData();
        createGetCVMEncryptedData();
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheGetCVMRequestBodyAsDefinedIn("valid/body/valid_request_body_CVM");
        iPostTheDetailsToCVMAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyRetrieveCVMAsExpected();
        verifyCVMEntriesInCL();
        iVerifyCVMSLA();
    }

    @And("^I verify that send passcode is done for the given account details with otp identifier as \"([^\"]*)\"$")
    public void iVerifySP(String identifier) throws Exception {
        createSPEncryptedData();
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheSPRequestBodyAsDefinedIn("valid/body/valid_request_body_SP", identifier);
        iPostTheDetailsToSPAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        RESTAssSteps.iVerifyTheVoidResponseBody();
        verifySPEntriesInCL();
        iVerifySPSLA();
    }

    @And("^I verify that send passcode is failed for the given account details with invalid otp identifier as \"([^\"]*)\" and the error code is \"([^\"]*)\"$")
    public void iVerifySPFailure(String identifier, String errorCode) throws Exception {
        createSPEncryptedData();
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheSPRequestBodyAsDefinedIn("valid/body/valid_request_body_SP", identifier);

        postRequestObject.put("otpMethodIdentifier", CommonUtil.generateString(new Random(), ALPHANUMERIC, 32));

        iPostTheDetailsToSPAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        RESTAssSteps.iVerifyResponseForErrorCode(errorCode);
        iVerifySPSLA();
    }

    @And("^I verify that update token is done for the given account details \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iVerifyUpdateToken(String tokenStatus, String messageReasonCode, String flowType, String tokenType) throws Exception {
        createUTEncryptedData(tokenStatus);
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheUpdateTokenRequestBodyAsDefinedIn(messageReasonCode);
        iPostTheDetailsToUTAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyTableEntriesAfterUT(tokenType, tokenStatus, messageReasonCode, flowType);
        verifyUTEntriesInCL();
        iVerifyUTSLA();
    }

    @And("^I verify that device binding is done for the given account details$")
    public void iVerifyDeviceBinding() throws Exception {
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheDeviceBindingRequestBodyAsDefinedIn("valid/body/valid_request_body_DB");
        iPostTheDetailsToDBAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyDeviceBindingIsAsExpected();
        verifyDBEntriesInCL();
        iVerifyDBSLA();
    }

    @And("^I initiate device binding for the given account details$")
    public void iInitiateDeviceBinding() throws Exception {
        iHaveTheDefaultVTSHeadersAsDefinedIn();
        iHaveTheDeviceBindingRequestBodyAsDefinedIn("valid/body/valid_request_body_DB");
        iPostTheDetailsToDBAPIEndpoint();
    }

    @When("^I post the details to Encryption endpoint$")
    public void iPostTheDetailsToEncryptionAPIEndpoint() {
        endpoint = apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA");

        restAssuredAPI.post(encryptionRequestBody, DatabaseSteps.headersAsMap, endpoint);
    }

    @Given("^I have the TSP Encryption headers as defined in \"([^\"]*)\"$")
    public void iHaveTheTSPEncryptionHeadersAsDefinedIn(String headersPath) {
        DatabaseSteps.headersAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @And("^I have the CE Encryption request body as defined in \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheCEEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String tokenRequester, String profileId, String accRefType, String accountState, String nullField, String idvMethod) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(postRequestObject);
        String query = null;

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) postRequestObject.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";

        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        }
        else {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
            issuer.ISSUER_ID = issuerName.contains("Savings") ? "FI-10057876401" : issuer.ISSUER_ID;
        }

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            fetchValidTSPAccountFromDatabase(tokenRequester, profileId, accRefType, accountState, idvMethod);
            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }

        if (cardholderInfo.get("name") != null && cardholderInfo.get("name").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            cardholderInfo.put("name", CommonUtil.generateString(new Random(), ALPHANUMERIC, 256));

        if (cardholderInfo.get("riskAssessmentScore") != null && cardholderInfo.get("riskAssessmentScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            cardholderInfo.put("riskAssessmentScore", null);

        if (nullField.equalsIgnoreCase("walletRisks"))
            cardholderInfo.put("riskAssessmentScore", null);

        HashMap<Object, Object> expirationDate = (HashMap<Object, Object>) cardholderInfo.get("expirationDate");
        Assert.assertNotNull(expirationDate);

        if (accountInfo.ACCOUNT_EXPIRY != null) {
            if (expirationDate.get("month") != null && expirationDate.get("month").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
                expirationDate.put("month", accountInfo.ACCOUNT_EXPIRY.substring(0, 2));

            if (expirationDate.get("year") != null && expirationDate.get("year").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
                expirationDate.put("year", accountInfo.ACCOUNT_EXPIRY.substring(2, 6));
        }

        encryptionRequestBody.putAll(postRequestObject);
        ceEncryptionRequestBody.clear();
        ceEncryptionRequestBody.putAll(encryptionRequestBody);
    }

    public void fetchValidTSPAccountFromDatabase(String tokenRequester, String profileId, String accRefType, String accountState, String idvMethod) throws Exception {
        if (Objects.equals(accRefType, "null"))
            accRefType = "account_ref_type is null";
        else
            accRefType = "account_ref_type = '" + accRefType + "'";

        if (profileId.equalsIgnoreCase("any"))
            profileId = "";
        else
            profileId = "and profile_id is " + profileId;

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/VISA/" + issuerName.toLowerCase() + ".json";

            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are added for VTS", testCardSets);

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("vtsTestCards").toString());
            Assert.assertTrue("No test cards are added for VTS", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));

            String query = "select * from bin_range where issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            binRange = new BinRange();
            binRange.fetchBinRanges(databaseSteps.result);

            int totalBinRanges = binRange.BIN_RANGES_LOW.size();
            int idx1;
            String searchAccount = null;
            if(scenarioName.contains("invalid cvv") && issuerName.equalsIgnoreCase("lunar bank dk"))
                searchAccount = "account = '4871450319565359'";
            else if (testCard.get("account") != null) {
                searchAccount = "account = '" + testCard.get("account").toString() + "'";
            }

            query = "select * from account_info where " + searchAccount + " and issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() > 0) {
                query = "select * from account_info where " + searchAccount + " and issuer_id = '" + issuer.ISSUER_ID + "' and account_state = '" + accountState + "' and account_ref_type = '" + testCard.get("accountRefType").toString().toUpperCase() + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if(DBConnection.recordCount() == 0)
                    throw new AssumptionViolatedException("Account '" + testCard.get("account") + "' is not found in the LCM database. Hence, the test case is skipped.");

                accountInfo = new AccountInfo(databaseSteps.result);
            }
            else {
                accountInfo = new AccountInfo();
                accountInfo.ACCOUNT = testCard.get("account").toString();
                accountInfo.ACCOUNT_EXPIRY = testCard.get("accountExpiry").toString();
            }

            for (idx1 = 0; idx1 < totalBinRanges; idx1++) {
                query = "select * from bin_range_lcm_service where lcm_service = 'TSP' and bin_range_low = '" + binRange.BIN_RANGES_LOW.get(idx1) + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() > 0) {
                    binRangeLCMService = new BinRangeLCMService(databaseSteps.result);
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

            if(idx1 >= totalBinRanges)
                throw new AssumptionViolatedException("No valid TSP bin range or account found in the database for the given issuer " + issuerName + ". Hence, the test case is skipped.");

            profileID = accountInfo.PROFILE_ID;
            if (profileID == null)
                profileID = binRange.PROFILE_ID;

            accountID = accountInfo.ACCOUNT_ID;
        }
        else {
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

                    query = "select * from account_info where account like '" + binRangeLow + "%' and issuer_id = '" + issuer.ISSUER_ID + "' and account_state = '" + accountState + "' and " + accRefType + profileId;
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
                        if (idvMethod.equalsIgnoreCase("email"))
                            query = "select * from account_info where issuer_id = '" + issuer.ISSUER_ID + "' and account_state = '" + accountState  + "' and account like '" + binRangeLow + "%' and account_id in (SELECT t1.account_id FROM idv_method t1 INNER JOIN idv_method t2\n" +
                                    "  ON t1.account_id=t2.account_id WHERE\n" +
                                    "  t1.Channel!='SMS' AND t2.Channel='EMAIL'\n" +
                                    "  ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only)";

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
                                issuerConfig = new IssuerConfig(databaseSteps.result);
                                if (!accRefType.contains("null") && issuerConfig.CALL_ISSUER_ALWAYS.equalsIgnoreCase("N")) {
                                    query = "select * from account_lcm_service where account_id = '" + accountInfo.ACCOUNT_ID + "' and lcm_service = 'TSP'";
                                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                                    if (DBConnection.recordCount() > 0) {
                                        Date dNow = new Date();
                                        SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                                        Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                                        if (dNow.before(dActual)) {
                                            validateTokenRequester(tokenRequester, true);
                                            if ((idvMethod.equalsIgnoreCase("email")) || (idvMethod.equalsIgnoreCase("no")) || (idvMethod.equalsIgnoreCase("any")) ||
                                                    (idvMethod.equalsIgnoreCase("app2app") && iVerifyApp2AppEnabled(binRange.BIN_RANGE_LOW)) ||
                                                    (idvMethod.equalsIgnoreCase("customercare") && iVerifyCCEnabled(binRange.BIN_RANGE_LOW))) {
                                                accountID = accountInfo.ACCOUNT_ID;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    Date dNow = new Date();
                                    SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                                    Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                                    if (dNow.before(dActual)) {
                                        validateTokenRequester(tokenRequester, true);
                                        if ((idvMethod.equalsIgnoreCase("email")) || (idvMethod.equalsIgnoreCase("no")) || (idvMethod.equalsIgnoreCase("any")) ||
                                                (idvMethod.equalsIgnoreCase("app2app") && iVerifyApp2AppEnabled(binRange.BIN_RANGE_LOW)) ||
                                                (idvMethod.equalsIgnoreCase("customercare") && iVerifyCCEnabled(binRange.BIN_RANGE_LOW))) {
                                            accountID = accountInfo.ACCOUNT_ID;
                                            break;
                                        }
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

            if(idx1 >= totalBinRanges)
                throw new AssumptionViolatedException("No valid TSP bin range or account found in the database for the given issuer " + issuerName + ". Hence, the test case is skipped.");

            profileID = accountInfo.PROFILE_ID;
            if (profileID == null)
                profileID = binRange.PROFILE_ID;
        }

        log.info(accountInfo.ACCOUNT + " - " + accountID);
    }

    public void validateTokenRequester(String tokenRequester, boolean hasHugeLimit) throws SQLException {
        String query;
        if (tokenRequester.equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            tokenRequesterID = "400100" + CommonUtil.generateString(new Random(), NUMERIC, 5).replaceFirst("^0+(?!$)", "");
            tokenRequesterName = CommonUtil.generateString(new Random(), ALPHA, 32);
        } else {
            if (tokenRequester.equalsIgnoreCase("FETCH_FROM_DATABASE")) {
                boolean virtualCardLimit = false;
                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and internal_value != 'Google Pay' and internal_value != 'Apple pay' ORDER BY DBMS_RANDOM.RANDOM";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                codeMapping = new CodeMapping();
                codeMapping.fetchTokenRequesters(databaseSteps.result);

                String hugeLimit = hasHugeLimit ? " and no_of_virtual_card != '0'" : " and no_of_virtual_card = '0'";
                query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '*'" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                if (DBConnection.recordCount() == 0) {
                    query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "')" + hugeLimit + " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
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
                    walletConfig = new WalletConfig(databaseSteps.result);

                    if (hasHugeLimit && !walletConfig.NO_OF_VIRTUAL_CARD.equalsIgnoreCase("0")) {
                        if (!Objects.equals(walletConfig.TOKEN_REQUESTER, "*"))
                            query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and external_value = '" + walletConfig.TOKEN_REQUESTER + "'";
                        else {
                            String zeroNonzeroVAs = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + walletConfig.BIN_RANGE_LOW + "' and no_of_virtual_card = '0'";
                            databaseSteps.result = databaseSteps.dbConnection.runQuery(zeroNonzeroVAs);
                            walletConfig.fetchTokenRequesters(databaseSteps.result);

                            query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and external_value in ('" + StringUtils.join(codeMapping.EXTERNAL_VALUES, "','") + "') and external_value not in (' " + StringUtils.join(walletConfig.TOKEN_REQUESTERS, "','") + "') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                        }

                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        codeMapping = new CodeMapping(databaseSteps.result);
                        virtualCardLimit = true;
                    }
                    else if (!hasHugeLimit) {
                        query = "select * from wallet_config where issuer_id = '" + issuer.ISSUER_ID + "' and no_of_virtual_card = '0' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        walletConfig = new WalletConfig(databaseSteps.result);

                        query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and external_value = '" + walletConfig.TOKEN_REQUESTER + "'";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        codeMapping = new CodeMapping(databaseSteps.result);
                        virtualCardLimit = true;
                    }
                }

                Assert.assertTrue("No valid token requester found for the given account and bin range", virtualCardLimit);
            } else {
                query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and lower(internal_value) like lower('%" + tokenRequester + "%')";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                codeMapping = new CodeMapping(databaseSteps.result);
            }

            tokenRequesterID = codeMapping.EXTERNAL_VALUE;
            tokenRequesterName = codeMapping.INTERNAL_VALUE;
        }

        VTSStep.log.info(tokenRequesterName + " - " + tokenRequesterID);
    }

    @And("^I have the AP Encryption request body as defined in \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheAPEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String flow, String tokenRequester, String profileId, String accountState, String accRefType, String approvalBy, String nullField, String idvMethod) throws Exception {
        encryptionRequestBody = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(encryptionRequestBody);

        databaseSteps.iEstablishConnectionToLCMDatabase();
        String query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";

        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        }
        else {
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
            issuer.ISSUER_ID = issuerName.contains("Savings") ? "FI-10057876401" : issuer.ISSUER_ID;
        }

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        int threshold1 = 1;
        int threshold2 = 99;
        String riskAssessmentScore = null;
        Random r = new Random();

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            if (accountID == null)
                fetchValidTSPAccountFromDatabase(tokenRequester, profileId, accRefType, accountState, idvMethod);

            if (token_type != null && (token_type.equalsIgnoreCase("card_on_file") || token_type.equalsIgnoreCase("ecommerce")) && approvalBy.equalsIgnoreCase("riskAssessmentScore"))
                riskAssessmentScore = "any";
            else if (binRange.RISK_SCR_THRESHOLD_1 == null && approvalBy.equalsIgnoreCase("riskAssessmentScore") && allScenarios.equalsIgnoreCase("No"))
                throw new AssumptionViolatedException("The given test card is not eligible for tsp approval based on risk assessment score, as threshold_1 of its bin range is null. Hence, the test case is skipped.");
            else if (binRange.RISK_SCR_THRESHOLD_1 != null && flow.equalsIgnoreCase("green") && approvalBy.equalsIgnoreCase("riskAssessmentScore") && binRange.RISK_SCR_THRESHOLD_1.equalsIgnoreCase("0") && allScenarios.equalsIgnoreCase("No"))
                throw new AssumptionViolatedException("The given test card is not eligible for tsp green flow based on risk assessment score, as threshold_1 of its bin range is 0. Hence, the test case is skipped.");
            else if (binRange.RISK_SCR_THRESHOLD_2 != null && flow.equalsIgnoreCase("red") && approvalBy.equalsIgnoreCase("riskAssessmentScore") && binRange.RISK_SCR_THRESHOLD_2.equalsIgnoreCase("99") && allScenarios.equalsIgnoreCase("No"))
                throw new AssumptionViolatedException("The given test card is not eligible for tsp red flow based on risk assessment score, as threshold_2 of its bin range is 99. Hence, the test case is skipped.");
            else if ((binRange.RISK_SCR_THRESHOLD_1 == null || binRange.RISK_SCR_THRESHOLD_1.equalsIgnoreCase("0"))
                    && flow.equalsIgnoreCase("green")
                    && approvalBy.equalsIgnoreCase("riskAssessmentScore")) {
//                issuerName = "LUNAR BANK DK";
//                query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";
//                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
//                issuer = new Issuer(databaseSteps.result);
//                fetchValidTSPAccountFromDatabase(tokenRequester, profileId, accRefType, accountState, idvMethod);
                throw new AssumptionViolatedException("As per issuer and its bin range configuration('Threshold_1: " + binRange.RISK_SCR_THRESHOLD_1 + "') green flow by risk assessment score is not applicable. Hence, the test case is skipped.");
            }
            else if ((binRange.RISK_SCR_THRESHOLD_2 == null || Objects.requireNonNull(binRange.RISK_SCR_THRESHOLD_2).equalsIgnoreCase("99"))
                    && flow.equalsIgnoreCase("red")
                    && approvalBy.equalsIgnoreCase("riskAssessmentScore")) {
                throw new AssumptionViolatedException("As per issuer and its bin range configuration('Threshold_2: " + binRange.RISK_SCR_THRESHOLD_2 + "') red flow by risk assessment score is not applicable. Hence, the test case is skipped.");
            }

            threshold1 = binRange.RISK_SCR_THRESHOLD_1 == null ? 10 : Integer.parseInt(binRange.RISK_SCR_THRESHOLD_1);
            threshold2 = binRange.RISK_SCR_THRESHOLD_2 == null ? 89 : Integer.parseInt(binRange.RISK_SCR_THRESHOLD_2);

            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }
        if (cardholderInfo.get("cvv2") != null && cardholderInfo.get("cvv2").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            cardholderInfo.put("cvv2", CommonUtil.generateString(new Random(), NUMERIC, 4));
        if (cardholderInfo.get("name") != null && cardholderInfo.get("name").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            cardholderInfo.put("name", CommonUtil.generateString(new Random(), ALPHANUMERIC, 256));

        int score;
        if (cardholderInfo.get("riskAssessmentScore") != null && cardholderInfo.get("riskAssessmentScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            if (flow.equalsIgnoreCase("green")) {
                score = threshold1>0?Math.max(r.nextInt(threshold1), 0):0;
                riskAssessmentScore = Integer.toString(score);
            }
            else if ((flow.equalsIgnoreCase("yellow") || flow.equalsIgnoreCase("orange") && approvalBy.equalsIgnoreCase("riskAssessmentScore"))) {
                riskAssessmentScore = Integer.toString(r.nextInt(threshold2-threshold1) + threshold1);
            }
            else if ((flow.equalsIgnoreCase("red") && approvalBy.equalsIgnoreCase("riskAssessmentScore"))) {
                int random = threshold2+1 < 100?r.nextInt(99-threshold2):0;
                score = Math.max(random, 1) + threshold2;

                riskAssessmentScore = Integer.toString(score);
            }
            else {
                score = threshold1>0?Math.max(r.nextInt(threshold1), 0):0;
                riskAssessmentScore = Integer.toString(score);
            }

            if (nullField.equalsIgnoreCase("walletRisks"))
                riskAssessmentScore = null;

            cardholderInfo.put("riskAssessmentScore", riskAssessmentScore);
        }

        HashMap<Object, Object> billingAddress = (HashMap<Object, Object>) cardholderInfo.get("billingAddress");
        Assert.assertNotNull(billingAddress);

        if (billingAddress.get("postalCode") != null && billingAddress.get("postalCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            billingAddress.put("postalCode", "- " + CommonUtil.generateString(new Random(), NUMERIC, 10));

        if (billingAddress.get("line1") != null && billingAddress.get("line1").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            billingAddress.put("line1", CommonUtil.generateString(new Random(), ALPHASPECIALNUMERIC, 40));

        HashMap<Object, Object> expirationDate = (HashMap<Object, Object>) cardholderInfo.get("expirationDate");
        Assert.assertNotNull(expirationDate);

        if (expirationDate.get("month") != null && expirationDate.get("month").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            expirationDate.put("month", accountInfo.ACCOUNT_EXPIRY.substring(0, 2));

        if (expirationDate.get("year") != null && expirationDate.get("year").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            expirationDate.put("year", accountInfo.ACCOUNT_EXPIRY.substring(2, 6));

        HashMap<Object, Object> riskInformation = (HashMap<Object, Object>) encryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInformation);

        if (riskInformation.get("visaTokenScore") != null && riskInformation.get("visaTokenScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("visaTokenScore", CommonUtil.generateString(new Random(), NUMERIC, 2));

        if (riskInformation.get("visaTokenDecisioning") != null && riskInformation.get("visaTokenDecisioning").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("visaTokenDecisioning", CommonUtil.generateString(new Random(), NUMERIC, 2));

       riskInformation.put("riskAssessmentScore", riskAssessmentScore);

        int accountScore = r.nextInt(5-1) + 1;
        if (riskInformation.get("walletProviderAccountScore") != null && riskInformation.get("walletProviderAccountScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("walletProviderAccountScore", Integer.toString(accountScore));

        int deviceScore = r.nextInt(5-2) + 2;
        if (riskInformation.get("walletProviderDeviceScore") != null && riskInformation.get("walletProviderDeviceScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("walletProviderDeviceScore", Integer.toString(deviceScore));
        if (riskInformation.get("walletProviderDeviceScore") != null && approvalBy.equalsIgnoreCase("walletProviderDeviceScore") && flow.equalsIgnoreCase("red"))
            riskInformation.put("walletProviderDeviceScore", "1");

        if (riskInformation.get("walletProviderReasonCodes") != null && riskInformation.get("walletProviderReasonCodes").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE") && approvalBy.equalsIgnoreCase("riskAssessmentScore"))
            riskInformation.put("walletProviderReasonCodes", "A0,A2");
        if (riskInformation.get("walletProviderReasonCodes") != null && riskInformation.get("walletProviderReasonCodes").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE") && flow.equalsIgnoreCase("orange") && approvalBy.equalsIgnoreCase("walletProviderReasonCodes"))
            riskInformation.put("walletProviderReasonCodes", "0G");
        if (riskInformation.get("walletProviderReasonCodes") != null && riskInformation.get("walletProviderReasonCodes").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE") && flow.equalsIgnoreCase("green") && approvalBy.equalsIgnoreCase("walletProviderReasonCodes"))
            riskInformation.put("walletProviderReasonCodes", "A0,A2");
        if (riskInformation.get("walletProviderReasonCodes") != null && riskInformation.get("walletProviderReasonCodes").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE") && approvalBy.equalsIgnoreCase("walletProviderDeviceScore"))
            riskInformation.put("walletProviderReasonCodes", "A0,A2");
        if (riskInformation.get("walletProviderReasonCodes") != null && riskInformation.get("walletProviderReasonCodes").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE") && approvalBy.equalsIgnoreCase("walletProviderReasonCodes"))
            riskInformation.put("walletProviderReasonCodes", walletProviderReasonCodesRedList[r.nextInt(walletProviderReasonCodesRedList.length)]);

        if (riskInformation.get("accountHolderName") != null && riskInformation.get("accountHolderName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("accountHolderName", CommonUtil.generateString(new Random(), ALPHANUMERIC, 64));

        if (nullField.equalsIgnoreCase("walletRisks")) {
            riskInformation.put("walletProviderDeviceScore", null);
            riskInformation.put("walletProviderReasonCodes", null);
        }

        apEncryptionRequestBody.clear();
        apEncryptionRequestBody.putAll(encryptionRequestBody);
    }

    @And("^I have the CT Encryption request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheCTEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String tokenType) throws Exception {
        encryptionRequestBody = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(encryptionRequestBody);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }

        if (cardholderInfo.get("name") != null && cardholderInfo.get("name").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            cardholderInfo.put("name", CommonUtil.generateString(new Random(), ALPHANUMERIC, 256));

        HashMap<Object, Object> billingAddress = (HashMap<Object, Object>) cardholderInfo.get("billingAddress");
        Assert.assertNotNull(billingAddress);

        if (billingAddress.get("postalCode") != null && billingAddress.get("postalCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            billingAddress.put("postalCode", "- " + CommonUtil.generateString(new Random(), NUMERIC, 10));

        if (billingAddress.get("line1") != null && billingAddress.get("line1").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            billingAddress.put("line1", CommonUtil.generateString(new Random(), ALPHASPECIALNUMERIC, 40));

        HashMap<Object, Object> expirationDate = (HashMap<Object, Object>) cardholderInfo.get("expirationDate");
        Assert.assertNotNull(expirationDate);

        if (expirationDate.get("month") != null && expirationDate.get("month").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            expirationDate.put("month", accountInfo.ACCOUNT_EXPIRY.substring(0, 2));

        if (expirationDate.get("year") != null && expirationDate.get("year").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            expirationDate.put("year", accountInfo.ACCOUNT_EXPIRY.substring(2, 6));

        HashMap<Object, Object> riskInformation = (HashMap<Object, Object>) encryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInformation);

        HashMap<Object, Object> riskInformationPrevious = (HashMap<Object, Object>) apEncryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInformationPrevious);

        if (riskInformation.get("visaTokenScore") != null && riskInformation.get("visaTokenScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("visaTokenScore", CommonUtil.generateString(new Random(), NUMERIC, 2));

        if (riskInformation.get("visaTokenDecisioning") != null && riskInformation.get("visaTokenDecisioning").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            riskInformation.put("visaTokenDecisioning", CommonUtil.generateString(new Random(), NUMERIC, 2));

        riskInformation.clear();
        riskInformation.putAll(riskInformationPrevious);
        riskInformation.remove("riskAssessmentScore");
        encryptionRequestBody.remove("riskInformation");
        encryptionRequestBody.put("riskInformation", riskInformation);

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) encryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfoObj);

        if (tokenInfoObj.get("token") != null && tokenInfoObj.get("token").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            tokenInfoObj.put("token", CommonUtil.generateString(new Random(), NUMERIC, 19));
        if (tokenInfoObj.get("tokenRequestorName") != null && tokenInfoObj.get("tokenRequestorName").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            tokenInfoObj.put("tokenRequestorName", tokenRequesterName);
        if (tokenInfoObj.get("tokenType") != null && tokenInfoObj.get("tokenType").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            tokenInfoObj.put("tokenType", tokenType.toUpperCase());

        HashMap<Object, Object> tokenExpirationDate = (HashMap<Object, Object>) tokenInfoObj.get("tokenExpirationDate");
        Assert.assertNotNull(expirationDate);

        if (tokenExpirationDate.get("month") != null && tokenExpirationDate.get("month").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            tokenExpirationDate.put("month", accountInfo.ACCOUNT_EXPIRY.substring(0, 2));

        if (tokenExpirationDate.get("year") != null && tokenExpirationDate.get("year").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            tokenExpirationDate.put("year", accountInfo.ACCOUNT_EXPIRY.substring(2, 6));

        ctEncryptionRequestBody.clear();
        ctEncryptionRequestBody.putAll(encryptionRequestBody);
    }

    @And("^I have the UT Encryption request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheUTEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String tokenStatus) throws Exception {
        encryptionRequestBody = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(encryptionRequestBody);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }

        encryptionRequestBody.remove("tokenInfo");
        encryptionRequestBody.put("tokenInfo", ctEncryptionRequestBody.get("tokenInfo"));
        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) encryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfoObj);

        tokenInfoObj.put("tokenStatus", tokenStatus);
    }

    @And("^I have the Get CVM Encryption request body as defined in \"([^\"]*)\"$")
    public void iHaveTheGetCVMEncryptionRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        encryptionRequestBody = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(encryptionRequestBody);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }

        HashMap<Object, Object> riskInformation = (HashMap<Object, Object>) encryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInformation);

        HashMap<Object, Object> riskInformationPrevious = (HashMap<Object, Object>) ctEncryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInformationPrevious);

        if (riskInformation.get("accountHolderName") != null && riskInformation.get("accountHolderName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("accountHolderName", riskInformationPrevious.get("accountHolderName").toString());
        }

        if (riskInformation.get("visaTokenScore") != null && riskInformation.get("visaTokenScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("visaTokenScore", riskInformationPrevious.get("visaTokenScore").toString());
        }

        if (riskInformation.get("visaTokenDecisioning") != null && riskInformation.get("visaTokenDecisioning").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("visaTokenDecisioning", riskInformationPrevious.get("visaTokenDecisioning").toString());
        }

        if (riskInformation.get("walletProviderAccountScore") != null && riskInformation.get("walletProviderAccountScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("walletProviderAccountScore", riskInformationPrevious.get("walletProviderAccountScore").toString());
        }

        if (riskInformation.get("walletProviderDeviceScore") != null && riskInformation.get("walletProviderDeviceScore").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("walletProviderDeviceScore", riskInformationPrevious.get("walletProviderDeviceScore"));
        }

        if (riskInformation.get("deviceBluetoothMac") != null && riskInformation.get("deviceBluetoothMac").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("deviceBluetoothMac", CommonUtil.generateString(new Random(), ALPHASPECIALNUMERIC, 24));
        }

        if (riskInformation.get("deviceIMEI") != null && riskInformation.get("deviceIMEI").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("deviceIMEI", CommonUtil.generateString(new Random(), ALPHANUMERIC, 24));
        }

        if (riskInformation.get("deviceSerialNumber") != null && riskInformation.get("deviceSerialNumber").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("deviceSerialNumber", CommonUtil.generateString(new Random(), ALPHANUMERIC, 24));
        }

        if (riskInformation.get("accountToDeviceBindingAge") != null && riskInformation.get("accountToDeviceBindingAge").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("accountToDeviceBindingAge", CommonUtil.generateString(new Random(), NUMERIC, 4));
        }

        if (riskInformation.get("userAccountFirstCreated") != null && riskInformation.get("userAccountFirstCreated").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("userAccountFirstCreated", CommonUtil.generateString(new Random(), NUMERIC, 4));
        }

        if (riskInformation.get("provisioningAttemptsOnDeviceIn24Hours") != null && riskInformation.get("provisioningAttemptsOnDeviceIn24Hours").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("provisioningAttemptsOnDeviceIn24Hours", CommonUtil.generateString(new Random(), NUMERIC, 2));
        }

        if (riskInformation.get("distinctCardholderNames") != null && riskInformation.get("distinctCardholderNames").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("distinctCardholderNames", CommonUtil.generateString(new Random(), NUMERIC, 2));
        }

        if (riskInformation.get("suspendedCardsInAccount") != null && riskInformation.get("suspendedCardsInAccount").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("suspendedCardsInAccount", CommonUtil.generateString(new Random(), NUMERIC, 2));
        }

        if (riskInformation.get("daysSinceLastAccountActivity") != null && riskInformation.get("daysSinceLastAccountActivity").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("daysSinceLastAccountActivity", CommonUtil.generateString(new Random(), NUMERIC, 4));
        }

        if (riskInformation.get("numberOfTransactionsInLast12months") != null && riskInformation.get("numberOfTransactionsInLast12months").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("numberOfTransactionsInLast12months", CommonUtil.generateString(new Random(), NUMERIC, 4));
        }

        if (riskInformation.get("numberOfActiveTokens") != null && riskInformation.get("numberOfActiveTokens").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("numberOfActiveTokens", CommonUtil.generateString(new Random(), NUMERIC, 2));
        }

        if (riskInformation.get("deviceWithActiveTokens") != null && riskInformation.get("deviceWithActiveTokens").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("deviceWithActiveTokens", CommonUtil.generateString(new Random(), NUMERIC, 2));
        }

        if (riskInformation.get("activeTokensOnAllDeviceForAccount") != null && riskInformation.get("activeTokensOnAllDeviceForAccount").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            riskInformation.put("activeTokensOnAllDeviceForAccount", CommonUtil.generateString(new Random(), NUMERIC, 4));
        }
    }

    @And("^I have the send passcode Encryption request body as defined in \"([^\"]*)\"$")
    public void iHaveTheSPEncryptionRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        encryptionRequestBody = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(encryptionRequestBody);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        if (cardholderInfo.get("primaryAccountNumber") != null && cardholderInfo.get("primaryAccountNumber").toString().equalsIgnoreCase("FETCH_FROM_DATABASE")) {
            cardholderInfo.put("primaryAccountNumber", accountInfo.ACCOUNT);
        }

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) ctEncryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfoObj);

        if (encryptionRequestBody.get("tokenRequestorName") != null && encryptionRequestBody.get("tokenRequestorName").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            encryptionRequestBody.put("tokenRequestorName", tokenInfoObj.get("tokenRequestorName"));
    }

    @And("^I verify that the virtual card limit of given issuer bin range and token requester$")
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

    @And("^I have the check eligibility request body as defined and pan source as \"([^\"]*)\"$")
    public void iHaveTheCheckEligibilityRequestBodyAsDefinedIn(String panSource) throws Exception {
        String requestBodyPath = "valid/body/valid_request_body_CE";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        if (postRequestObject.get("tokenRequestorID") != null && postRequestObject.get("tokenRequestorID").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("tokenRequestorID", tokenRequesterID);
        if (postRequestObject.get("tokenReferenceID") != null && postRequestObject.get("tokenReferenceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenReferenceID", tokenReferenceID);
        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", csvProperties.get("panReferenceID"));
        if (postRequestObject.get("panSource") != null && postRequestObject.get("panSource").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("panSource", panSource.toUpperCase());
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceID") != null && deviceInfoObj.get("deviceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 48));

    }

    @And("^I have the approve provisioning request body as defined \"([^\"]*)\", \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheApproveProvisioningRequestBodyAsDefinedIn(String panSource, String tokenType, String deviceType, String idv) throws Exception {
        String requestBodyPath = "valid/body/valid_request_body_AP";
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", csvProperties.get("panReferenceID"));
        if (postRequestObject.get("walletAccountEmailAddressHash") != null && postRequestObject.get("walletAccountEmailAddressHash").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("walletAccountEmailAddressHash", csvProperties.get("walletAccountEmailAddressHash"));
        if (postRequestObject.get("clientWalletAccountID") != null && postRequestObject.get("clientWalletAccountID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("clientWalletAccountID", csvProperties.get("clientWalletAccountID"));
        if (postRequestObject.get("panSource") != null && postRequestObject.get("panSource").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("panSource", panSource.toUpperCase());
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceType") != null && deviceInfoObj.get("deviceType").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            if (deviceType.equalsIgnoreCase("null"))
                deviceInfoObj.put("deviceType", null);
            else
                deviceInfoObj.put("deviceType", deviceType.toUpperCase());
        }

        if (deviceInfoObj.get("deviceNumber") != null && deviceInfoObj.get("deviceNumber").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            if (deviceType.equalsIgnoreCase("null"))
                deviceInfoObj.put("deviceNumber", null);
            else if (deviceType.equalsIgnoreCase("mobile_phone") && !idv.equalsIgnoreCase("email")){
                String query = "select * from idv_method where account_id = '" + accountID + "' and channel = 'SMS'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                Assert.assertTrue("SMS idv method is not enabled for the given PAN", DBConnection.recordCount() > 0);

                idvMethod = new IDVMethod(databaseSteps.result);
                deviceInfoObj.put("deviceNumber", idvMethod.CONTACT_INFO);
            }
            else if(idv.equalsIgnoreCase("email"))
                deviceInfoObj.put("deviceNumber", CommonUtil.generateString(new Random(), NUMERIC, 13));
        }

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) postRequestObject.get("tokenInfo");
        Assert.assertNotNull(tokenInfoObj);

        if (tokenInfoObj.get("tokenType") != null && tokenInfoObj.get("tokenType").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            tokenInfoObj.put("tokenType", tokenType.toUpperCase());

        if (tokenReferenceID == null) {
            tokenReferenceID = "";
            tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        }
        if (tokenRequesterName == null)
            tokenRequesterName = CommonUtil.generateString(new Random(), ALPHA,32);
    }

    @And("^I update invalid mobile number as device number in approve provisioning request$")
    public void iUpdateInvalidMobileNumberAsDeviceNumber() {
        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        deviceInfoObj.put("deviceNumber", CommonUtil.generateString(new Random(), NUMERIC, 13));
    }

    @And("^I have the create token request body as defined in \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheCreateTokenRequestBodyAsDefinedIn(String requestBodyPath, String panSource, String messageReasonCode) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", apPostRequestObject.get("panReferenceID"));
        if (postRequestObject.get("walletAccountEmailAddressHash") != null && postRequestObject.get("walletAccountEmailAddressHash").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("walletAccountEmailAddressHash", csvProperties.get("walletAccountEmailAddressHash"));
        if (postRequestObject.get("clientWalletAccountID") != null && postRequestObject.get("clientWalletAccountID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("clientWalletAccountID", csvProperties.get("clientWalletAccountID"));
        if (postRequestObject.get("panSource") != null && postRequestObject.get("panSource").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("panSource", panSource.toUpperCase());
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);
        if (postRequestObject.get("messageReasonCode") != null && postRequestObject.get("messageReasonCode").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("messageReasonCode", messageReasonCode.toUpperCase());

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceID") != null && deviceInfoObj.get("deviceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 48));

        if (deviceInfoObj.get("deviceName") != null && deviceInfoObj.get("deviceName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            String deviceName = CommonUtil.generateString(new Random(), ALPHANUMERIC, 64);
            Base64.Encoder enc = Base64.getEncoder();
            String encodedDeviceName = enc.encodeToString(deviceName.getBytes());
            deviceInfoObj.put("deviceName", encodedDeviceName);
        }
    }

    @And("^I have the get CVM request body as defined in \"([^\"]*)\"$")
    public void iHaveTheGetCVMRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", csvProperties.get("panReferenceID"));
        if (postRequestObject.get("clientWalletAccountID") != null && postRequestObject.get("clientWalletAccountID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("clientWalletAccountID", csvProperties.get("clientWalletAccountID"));
        if (postRequestObject.get("tokenRequestorID") != null && postRequestObject.get("tokenRequestorID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenRequestorID", tokenRequesterID);
        if (postRequestObject.get("tokenReferenceID") != null && postRequestObject.get("tokenReferenceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenReferenceID", tokenReferenceID);
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);

        HashMap<Object, Object> deviceInfoPrevious = (HashMap<Object, Object>) ctPostRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoPrevious);

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceID") != null && deviceInfoObj.get("deviceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceID", deviceInfoPrevious.get("deviceID"));
    }

    @And("^I have the send passcode request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheSPRequestBodyAsDefinedIn(String requestBodyPath, String otpIdentifier) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", csvProperties.get("panReferenceID"));
        if (postRequestObject.get("clientWalletAccountID") != null && postRequestObject.get("clientWalletAccountID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("clientWalletAccountID", csvProperties.get("clientWalletAccountID"));
        if (postRequestObject.get("tokenRequestorID") != null && postRequestObject.get("tokenRequestorID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenRequestorID", tokenRequesterID);
        if (postRequestObject.get("tokenReferenceID") != null && postRequestObject.get("tokenReferenceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("tokenReferenceID", tokenReferenceID);
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);

        String otpMethod = postRequestObject.get("otpMethodIdentifier").toString();
        if (otpMethod != null && otpMethod.equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            Random r = new Random();
            int randomIdentifier;
            String identifier = null;

            if (smsIdentifier != null && emailIdentifier != null) {
                randomIdentifier = r.nextInt(2);
                identifier = randomIdentifier == 0 ? smsIdentifier : emailIdentifier;
            } else if (smsIdentifier != null)
                identifier = smsIdentifier;
            else if (emailIdentifier != null)
                identifier = emailIdentifier;

            postRequestObject.put("otpMethodIdentifier", identifier);
        }

        if (otpIdentifier != null && (otpIdentifier.equalsIgnoreCase("sms")))
            postRequestObject.put("otpMethodIdentifier", smsIdentifier);
        else if (otpIdentifier != null && (otpIdentifier.equalsIgnoreCase("email")))
            postRequestObject.put("otpMethodIdentifier", emailIdentifier);

        if (postRequestObject.get("otpValue") != null && postRequestObject.get("otpValue").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("otpValue", CommonUtil.generateString(new Random(), NUMERIC, 8));

        HashMap<Object, Object> deviceInfoPrevious = (HashMap<Object, Object>) ctPostRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoPrevious);

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceID") != null && deviceInfoObj.get("deviceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceID", deviceInfoPrevious.get("deviceID"));
    }

    @And("^I have the update token request body as defined in \"([^\"]*)\"$")
    public void iHaveTheUpdateTokenRequestBodyAsDefinedIn(String messageReasonCode) {
        postRequestObject.put("encryptedData", encrypted_data);
        postRequestObject.put("messageReasonCode", messageReasonCode.toUpperCase());
    }

    @And("^I have the device binding request body as defined in \"([^\"]*)\"$")
    public void iHaveTheDeviceBindingRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + VTSRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("panReferenceID") != null && postRequestObject.get("panReferenceID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("panReferenceID", ctPostRequestObject.get("panReferenceID"));
        if (postRequestObject.get("walletAccountEmailAddressHash") != null && postRequestObject.get("walletAccountEmailAddressHash").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("walletAccountEmailAddressHash", csvProperties.get("walletAccountEmailAddressHash"));
        if (postRequestObject.get("clientWalletAccountID") != null && postRequestObject.get("clientWalletAccountID").toString().equalsIgnoreCase("FETCH_FROM_CSV"))
            postRequestObject.put("clientWalletAccountID", csvProperties.get("clientWalletAccountID"));

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        if (deviceInfoObj.get("deviceID") != null && deviceInfoObj.get("deviceID").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 48));

        if (deviceInfoObj.get("deviceName") != null && deviceInfoObj.get("deviceName").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            String deviceName = CommonUtil.generateString(new Random(), ALPHANUMERIC, 64);
            Base64.Encoder enc = Base64.getEncoder();
            String encodedDeviceName = enc.encodeToString(deviceName.getBytes());
            deviceInfoObj.put("deviceName", encodedDeviceName);
        }

        if (deviceInfoObj.get("deviceIndex") != null && deviceInfoObj.get("deviceIndex").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            deviceInfoObj.put("deviceIndex", CommonUtil.generateString(new Random(), NUMERIC, 2));

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) ctEncryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfoObj);

        if (postRequestObject.get("tokenRequestorName") != null && postRequestObject.get("tokenRequestorName").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("tokenRequestorName", tokenInfoObj.get("tokenRequestorName"));
    }

    @When("^I post the details to check eligibility endpoint$")
    public void iPostTheDetailsToCEAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_CE_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        
        cePostRequestObject.clear();
        cePostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(cePostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to approve provisioning endpoint$")
    public void iPostTheDetailsToAPAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_AP_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);

        apPostRequestObject.clear();
        apPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to approve provisioning endpoint exceeding virtual card limit$")
    public void iPostTheDetailsToAPAPIEndpointExceedVirtualCardLimit() {
        endpoint = apiProperties.getProperty("VTS_AP_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);

        apPostRequestObject.clear();
        apPostRequestObject.putAll(postRequestObject);
        for (int idx=0; idx <= Integer.parseInt(walletConfig.NO_OF_VIRTUAL_CARD); idx++) {
            restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        }
    }

    @When("^I post the details to approve provisioning endpoint for more than 5 times$")
    public void iPostTheDetailsToAPAPIEndpointFiveTimes() {
        endpoint = apiProperties.getProperty("VTS_AP_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);

        postRequestObject.put("cvv2ResultsCode", "N");
        apPostRequestObject.clear();
        apPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        endpoint = endpoint.replace(tokenReferenceID, "");
        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        endpoint = endpoint.replace("tokens//", "tokens/" + tokenReferenceID + "/");
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        endpoint = endpoint.replace(tokenReferenceID, "");
        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        endpoint = endpoint.replace("tokens//", "tokens/" + tokenReferenceID + "/");
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        endpoint = endpoint.replace(tokenReferenceID, "");
        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        endpoint = endpoint.replace("tokens//", "tokens/" + tokenReferenceID + "/");
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        endpoint = endpoint.replace(tokenReferenceID, "");
        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        endpoint = endpoint.replace("tokens//", "tokens/" + tokenReferenceID + "/");
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
        endpoint = endpoint.replace(tokenReferenceID, "");
        tokenReferenceID = "";
        tokenReferenceID = CommonUtil.generateString(new Random(), ALPHANUMERIC, 32);
        endpoint = endpoint.replace("tokens//", "tokens/" + tokenReferenceID + "/");
        restAssuredAPI.post(apPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to create token endpoint$")
    public void iPostTheDetailsToCTAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_TN_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);
        endpoint += "CREATED";

        ctPostRequestObject.clear();
        ctPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(ctPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to get CVM endpoint$")
    public void iPostTheDetailsToCVMAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_CVM_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));

        cvmPostRequestObject.clear();
        cvmPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(cvmPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to send passcode endpoint$")
    public void iPostTheDetailsToSPAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_SP_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));

        spPostRequestObject.clear();
        spPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(spPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to update token endpoint$")
    public void iPostTheDetailsToUTAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_TN_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);
        endpoint += "STATUS_UPDATED";

        utPostRequestObject.clear();
        utPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(utPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to device binding endpoint$")
    public void iPostTheDetailsToDBAPIEndpoint() {
        endpoint = apiProperties.getProperty("VTS_DB_DATA");
        endpoint = endpoint.replace("${VTS_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorID}", tokenRequesterID);
        endpoint = endpoint.replace("${tokenReferenceID}", tokenReferenceID);

        dbPostRequestObject.clear();
        dbPostRequestObject.putAll(postRequestObject);
        restAssuredAPI.post(dbPostRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @And("^I verify that the response time is under the SLA for check eligibility endpoint$")
    public void iVerifyCESLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("CHECK_ELIGIBILITY"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Check eligibility");
    }

    @And("^I verify that the response time is under the SLA for approve provisioning endpoint$")
    public void iVerifyAPSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("APPROVE_PROVISIONING"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Approve provisioning");
    }

    @And("^I verify that the response time is under the SLA for create token endpoint$")
    public void iVerifyCTSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("CREATE_TOKEN"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Create token");
    }

    @And("^I verify that the response time is under the SLA for update token endpoint$")
    public void iVerifyUTSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("UPDATE_TOKEN"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Update token");
    }

    @And("^I verify that the response time is under the SLA for get CVM endpoint$")
    public void iVerifyCVMSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("GET_CVM"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Get cvm");
    }

    @And("^I verify that the response time is under the SLA for send passcode endpoint$")
    public void iVerifySPSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("SEND_PASSCODE"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Send passcode");
    }

    @And("^I verify that the response time is under the SLA for device binding endpoint$")
    public void iVerifyDBSLA() {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("DEVICE_BINDING"));
        restAssSteps.iVerifyTheSLA(expectedSLA, "Device binding");
    }

    @And("^I verify that the given pan is eligible for visa token provisioning as expected$")
    public void iVerifyPanIsEligibleForVisaTokenAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        JSONObject response = (JSONObject) postResponseObject.get("cardMetadataInfo");
        Assert.assertNotNull("The given pan is not eligible for visa token provisioning. Response payload is " + postResponseObject + " and the X-Request-ID is " + DatabaseSteps.headersAsMap.get("X-Request-ID"), response);
        Assert.assertNotNull("The profile id is not generated for given request", response.get("profileID"));

        if (galleryIdEnabled.equalsIgnoreCase("yes")) {
            String query = "select * from issuer_config where bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and issuer_id = '" + issuer.ISSUER_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() == 0) {
                query = "select * from issuer_config where bin_range_low = '*' and issuer_id = '" + issuer.ISSUER_ID + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            }
            issuerConfig = new IssuerConfig(databaseSteps.result);

            if (issuerConfig.ENABLE_PROFILE_ID_REF == null || issuerConfig.ENABLE_PROFILE_ID_REF.equalsIgnoreCase("n")) {
                Assert.fail("Gallery id reference is not configured for the given issuer " + issuerName);
            }
            else if (issuerConfig.ENABLE_PROFILE_ID_REF.equalsIgnoreCase("y")) {
                query = "select * from account_additional_info where account_id = '" + accountID + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                accountAdditionalInfo = new AccountAdditionalInfo(databaseSteps.result);
                String additionalParams = accountAdditionalInfo.ADDITIONAL_PARAMETERS_RESPONSE;
                JSONArray params = JSONHelper.parseJSONArray(additionalParams);
                for (Object param : params) {
                    JSONObject obj = (JSONObject) param;
                    if (obj.get("name").toString().equalsIgnoreCase("GalleryID"))
                        galleryID = obj.get("value").toString();
                }

                String internalValue = issuer.ISSUER_ID + "_" + binRange.BIN_RANGE_LOW + "_" + galleryID;

                query = "select * from code_mapping where internal_value = '" + internalValue + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                if (DBConnection.recordCount() == 0) {
                    query = query.replace(binRange.BIN_RANGE_LOW, "*");
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }

                if (DBConnection.recordCount() == 0)
                    Assert.fail("No profile id is configured for the given issuer(" + issuerName + ") and gallery id(" + galleryID + ") returned from CMS");

                codeMapping = new CodeMapping(databaseSteps.result);
                profileID = codeMapping.EXTERNAL_VALUE;
          }
        }

        Assert.assertEquals("The profile id is not as expected for given request", profileID, response.get("profileID"));
    }

    @And("^I verify that the given pan is approved for visa token provisioning as expected with action code as \"([^\"]*)\"$")
    public void iVerifyPanIsApprovedForVisaTokenAsExpected(String expectedActionCode) throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        Assert.assertNotNull("Unexpected error response for approve provisioning. Please refer the response payload", postResponseObject);
        Assert.assertNull("An error code is returned instead of action code unexpectedly and the x-request-id is " + DatabaseSteps.headersAsMap.get("X-Request-ID"), postResponseObject.get("errorCode"));
        Assert.assertNotNull("Action code is not returned in the response payload. Kindly refer the response payload", postResponseObject.get("actionCode"));
        String actualActionCode = postResponseObject.get("actionCode").toString();
        Assert.assertEquals("The action code is not as expected", expectedActionCode, actualActionCode);
    }

    @And("^I verify that the given pan is required additional authentication for visa token provisioning as given \"([^\"]*)\"$")
    public void iVerifyPanIsPartiallyApprovedForVisaTokenAsExpected(String flowType) throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        Assert.assertNotNull("Unexpected error response for approve provisioning. Please refer the response payload", postResponseObject);
        Assert.assertNotNull("Unexpected error response for approve provisioning. Please refer the response payload", postResponseObject.get("actionCode"));

        String actualActionCode = postResponseObject.get("actionCode").toString();
        String expectedActionCode = null;
        if (flowType.equalsIgnoreCase("orange") || flowType.equalsIgnoreCase("yellow"))
            expectedActionCode = "85";
        Assert.assertEquals("The action code is not as expected for " + flowType + " flow", expectedActionCode, actualActionCode);
    }

    @And("^I verify that the given pan is declined for visa token provisioning as expected$")
    public void iVerifyPanIsDeclinedForVisaTokenAsExpected() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        Assert.assertNotNull("Unexpected error response for approve provisioning. Please refer the response payload", postResponseObject);
        Assert.assertNull("An error code is returned instead of action code unexpectedly", postResponseObject.get("errorCode"));
        String actionCode = postResponseObject.get("actionCode").toString();
        Assert.assertEquals("The action code is not as expected for red flow", "05", actionCode);
    }

    @And("^I verify that the get cardholder verification methods are retrieved successfully as expected$")
    public void iVerifyRetrieveCVMAsExpected() throws Exception {
        String query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRange.BIN_RANGE_LOW + "' and token_requester = '" + tokenRequesterID + "'";
        databaseSteps.iEstablishConnectionToLCMDatabase();

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

        Assert.assertTrue("No valid issuer idv config found in database for the bin range '" + binRange.BIN_RANGE_LOW + "' and token requester '" + tokenRequesterID + "'" , DBConnection.recordCount() > 0);
        issuerIDVConfig = new IssuerIDVConfig(databaseSteps.result);

        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("No cardholder verification methods retrieved for the given token details. Please refer the response payload", postResponseObject);

        JSONArray postResponseArrayObject = JSONHelper.parseJSONArray(postResponseObject.get("stepUpMethods").toString());
        Assert.assertTrue("No cardholder verification methods retrieved for the given token details. Please refer the response payload", postResponseObject != null && !postResponseArrayObject.isEmpty());

        for (Object o : postResponseArrayObject) {
            JSONObject obj = (JSONObject) o;
            if (obj.get("type").toString().contains("SMS")) {
                Assert.assertTrue("SMS idv is enabled in issuer idv config but returned in the cvm response payload", issuerIDVConfig.SMS_IDV_ENABLED.equalsIgnoreCase("y"));
                smsIdentifier = obj.get("identifier").toString();

                query = "select * from idv_method where account_id = '" + accountID + "' and channel = 'SMS'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertTrue("OTP type is not as expected for sms in cvm response payload", obj.get("type").toString().equalsIgnoreCase("otpsms"));
                Assert.assertEquals("SMS Identifier is not as expected in cvm response payload", obj.get("identifier"), idvMethod.OTP_IDENTIFIER);
                String expectedContactInfo = idvMethod.CONTACT_INFO.substring(0,4) + StringUtils.repeat("*", idvMethod.CONTACT_INFO.length()-6) + idvMethod.CONTACT_INFO.substring(idvMethod.CONTACT_INFO.length()-2);
                if (idvMethod.CONTACT_INFO.contains(" "))
                    expectedContactInfo = idvMethod.CONTACT_INFO.replaceAll(" ", "*");
                Assert.assertEquals("Contact info is not as expected for sms in cvm response payload", obj.get("value"), expectedContactInfo);
            }
            else if (obj.get("type").toString().contains("EMAIL")) {
                emailIdentifier = obj.get("identifier").toString();

                query = "select * from idv_method where account_id = '" + accountID + "' and channel = 'EMAIL'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertTrue("OTP type is not as expected for email in cvm response payload", obj.get("type").toString().equalsIgnoreCase("otpemail"));
                Assert.assertEquals("Email Identifier is not as expected in cvm response payload", obj.get("identifier"), idvMethod.OTP_IDENTIFIER);
                String[] expectedContactInfoList = idvMethod.CONTACT_INFO.split("@");
                String expectedContactInfo = (expectedContactInfoList[0].length() > 3 ? expectedContactInfoList[0].substring(0, 3) : expectedContactInfoList[0]) + StringUtils.repeat("*", expectedContactInfoList[0].length()-3) + "@" + expectedContactInfoList[1];
                Assert.assertEquals("Contact info is not as expected for email in cvm response payload", expectedContactInfo, obj.get("value"));
            }
            else if (obj.get("type").toString().contains("APP_TO_APP")) {
                Assert.assertEquals("App2App Identifier is not as expected in cvm response payload", obj.get("identifier"), issuerIDVConfig.APP_APP_IDENTIFIER);
            }
            else if (obj.get("type").toString().contains("CUSTOMERCARE") || obj.get("type").toString().contains("CUSTOMERSERVICE")) {
                if (issuerIDVConfig.CC_IDV_ENABLED.equalsIgnoreCase("y") && issuerIDVConfig.CC_IDV_DEFAULT_VALUE.equalsIgnoreCase("y"))
                    Assert.assertEquals("Customer care identifier is not as expected in cvm response payload", obj.get("value"), issuerIDVConfig.CC_IDV_DEFAULT_VALUE);
            }
            else
                Assert.fail("Unexpected identifier present for the given account " + obj.get("type"));
        }
    }

    @And("^I verify that the APP2APP idv method is enabled for the given bin range \"([^\"]*)\"$")
    public boolean iVerifyApp2AppEnabled(String binRangeLow) {
        String query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRangeLow + "' and token_requester = '" + tokenRequesterID + "' and APP_2_APP_IDV_ENABLED = 'Y'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        if (DBConnection.recordCount() == 0) {
            query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRangeLow + "' and token_requester = '*' and APP_2_APP_IDV_ENABLED = 'Y'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() == 0) {
                query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '" + tokenRequesterID + "' and APP_2_APP_IDV_ENABLED = 'Y'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() == 0) {
                    query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '*' and APP_2_APP_IDV_ENABLED = 'Y'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }
            }
        }

        return DBConnection.recordCount() == 1;
    }

    @And("^I verify that the CC idv method is enabled for the given bin range \"([^\"]*)\"$")
    public boolean iVerifyCCEnabled(String binRangeLow) {
        String query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRangeLow + "' and token_requester = '" + tokenRequesterID + "' and CC_IDV_ENABLED = 'Y'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        if (DBConnection.recordCount() == 0) {
            query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '" + binRangeLow + "' and token_requester = '*' and CC_IDV_ENABLED = 'Y'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() == 0) {
                query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '" + tokenRequesterID + "' and CC_IDV_ENABLED = 'Y'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() == 0) {
                    query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID + "' and bin_range_low = '*' and token_requester = '*' and CC_IDV_ENABLED = 'Y'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                }
            }
        }

        return DBConnection.recordCount() == 1;
    }

    @And("^I verify that device binding is completed as expected$")
    public void iVerifyDeviceBindingIsAsExpected() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        Assert.assertNotNull("Action code is missing in the response. Please refer the response payload", postResponseObject.get("actionCode"));
        String actualActionCode = postResponseObject.get("actionCode").toString();
        Assert.assertEquals("The action code is not as expected for device binding", "85", actualActionCode);
    }

    @And("^I verify that table entries are as expected after check eligibility for \"([^\"]*)\" tokenRequester$")
    public void iVerifyTableEntriesAfterCE(String tokenRequester) throws Exception {
        String query = "select * from token_info where token_reference_id = '" + tokenReferenceID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);

        Assert.assertTrue("No records created into token info table after check eligibility call", DBConnection.recordCount() > 0);
        Assert.assertEquals("API_CALL field in token info table is not as expected after check eligibility", "CHECK ELIGIBILITY", tokenInfo.API_CALL);
        Assert.assertEquals("Token requestor field in token info table is not as expected after check eligibility", cePostRequestObject.get("tokenRequestorID"), tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertNotNull("Virtual account id in token info table is not as expected after check eligibility", tokenInfo.VIRTUAL_ACCOUNT_ID);
        virtualAccountID = tokenInfo.VIRTUAL_ACCOUNT_ID;

        query = "select * from virtual_account where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records created into virtual account table after check eligibility call", DBConnection.recordCount() > 0);
        if (accountID != null)
            Assert.assertEquals("Account ID field in virtual account table is not as expected after check eligibility", accountID, virtualAccount.ACCOUNT_ID);
        else
            accountID = accountInfo.ACCOUNT_ID;

        Assert.assertEquals("Pan reference field in virtual account table is not as expected after check eligibility", cePostRequestObject.get("panReferenceID"), virtualAccount.PAN_REFERENCE);
        Assert.assertEquals("Status field in virtual account table is not as expected after check eligibility", "INACTIVE", virtualAccount.STATUS);
        Assert.assertNull("Enrolment flow field in virtual account table is not as expected after check eligibility", virtualAccount.ENROLMENT_FLOW);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) ceEncryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        String cardHolderName = cardholderInfo.get("name").toString();
        if (tokenRequester.equalsIgnoreCase("apple pay")) {
            cardHolderName = cardHolderName.substring(0,cardHolderName.length()/4) + StringUtils.repeat("*", cardHolderName.length()/2) + cardHolderName.substring(cardHolderName.length()*3/4);
            Assert.assertEquals("Card holder name field in virtual account table is not as expected after check eligibility", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }
        else {
            Assert.assertEquals("Card holder name field in virtual account table is not as expected after check eligibility", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }

        Assert.assertEquals("Card org field in virtual account table is not as expected after check eligibility", "VI", virtualAccount.CARD_ORG);
        Assert.assertEquals("Pan source field in virtual account table is not as expected after check eligibility", cePostRequestObject.get("panSource"), virtualAccount.PAN_SOURCE);
        Assert.assertEquals("API_CALL field in virtual account table is not as expected after check eligibility", "CHECK ELIGIBILITY", virtualAccount.API_CALL);

        query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records created into device info table after check eligibility call", DBConnection.recordCount() > 0);
        Assert.assertNotNull("Device id in device info table is not as expected after check eligibility", deviceInfo.DEVICE_ID);
        deviceID = deviceInfo.DEVICE_ID;

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) postRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        Assert.assertEquals("Card scheme device id field in device info table is not as expected after check eligibility", deviceInfoObj.get("deviceID"), deviceInfo.CARD_SCHEME_DEVICE_ID);
        Assert.assertEquals("Language code field in device info table is not as expected after check eligibility", deviceInfoObj.get("deviceLanguageCode"), deviceInfo.DEVICE_LANGUAGE_CODE);
        Assert.assertEquals("Original device field in device info table is not as expected after check eligibility", "Y", deviceInfo.ORIGINAL_DEVICE);

        query = "select * from virtual_risk_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualRiskInfo = new VirtualRiskInfo(databaseSteps.result);

        Assert.assertTrue("No records created into virtual risk info table after check eligibility call", DBConnection.recordCount() > 0);
        Assert.assertEquals("Risk assessment score in virtual risk info table is not as expected after check eligibility", cardholderInfo.get("riskAssessmentScore"), virtualRiskInfo.CS_RISK_ASSESSMENT_SCORE);
    }

    @And("^I verify that table entries are as expected after approve provisioning for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterAP(String tokenType, String flowType) throws Exception {
        String query = "select * from token_info where token_reference_id = '" + tokenReferenceID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);

        Assert.assertTrue("No records created into token info table after approve provisioning", DBConnection.recordCount() > 0);
        virtualAccountID = tokenInfo.VIRTUAL_ACCOUNT_ID;

        if (!ceEncryptionRequestBody.isEmpty() && postResponseObject.get("actionCode").toString().equalsIgnoreCase("05"))
            Assert.assertEquals("API_CALL field in token info table is not as expected after approve provisioning", "CHECK ELIGIBILITY", tokenInfo.API_CALL);
        else
            Assert.assertEquals("API_CALL field in token info table is not as expected after approve provisioning", "APPROVE PROVISIONING", tokenInfo.API_CALL);
        Assert.assertEquals("Token requestor field in token info table is not as expected after approve provisioning", tokenRequesterID, tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertEquals("Virtual account id in token info table is not as expected after approve provisioning", virtualAccountID, tokenInfo.VIRTUAL_ACCOUNT_ID);

        query = "select * from virtual_account where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records created into virtual account table after approve provisioning", DBConnection.recordCount() > 0);
        Assert.assertEquals("Account ID field in virtual account table is not as expected after approve provisioning", accountID, virtualAccount.ACCOUNT_ID);
        Assert.assertEquals("Pan reference field in virtual account table is not as expected after approve provisioning", apPostRequestObject.get("panReferenceID"), virtualAccount.PAN_REFERENCE);
        Assert.assertEquals("Status field in virtual account table is not as expected after approve provisioning", "INACTIVE", virtualAccount.STATUS);
        if (flowType.equalsIgnoreCase("green") && postResponseObject.get("actionCode").toString().equalsIgnoreCase("05"))
            Assert.assertNull("Enrolment flow field in virtual account table is not as expected after approve provisioning", virtualAccount.ENROLMENT_FLOW);
        else
            Assert.assertEquals("Enrolment flow field in virtual account table is not as expected after approve provisioning", flowType.toUpperCase(), virtualAccount.ENROLMENT_FLOW);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) apEncryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        if (!ceEncryptionRequestBody.isEmpty() && postResponseObject.get("actionCode").toString().equalsIgnoreCase("05")) {
            cardholderInfo = (HashMap<Object, Object>) ceEncryptionRequestBody.get("cardholderInfo");
            Assert.assertNotNull(cardholderInfo);
        }

        String cardHolderName = cardholderInfo.get("name").toString();

        if (tokenType.equalsIgnoreCase("SECURE_ELEMENT"))
            cardHolderName = cardHolderName.substring(0,cardHolderName.length()/4) + StringUtils.repeat("*", cardHolderName.length()/2) + cardHolderName.substring(cardHolderName.length()*3/4);

        Assert.assertEquals("Cardholder name in virtual account table is not as expected after approve provisioning", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        Assert.assertEquals("Card org field in virtual account table is not as expected after approve provisioning", "VI", virtualAccount.CARD_ORG);
        Assert.assertEquals("Pan source field in virtual account table is not as expected after approve provisioning", apPostRequestObject.get("panSource"), virtualAccount.PAN_SOURCE);
        if (!ceEncryptionRequestBody.isEmpty() && postResponseObject.get("actionCode").toString().equalsIgnoreCase("05"))
            Assert.assertEquals("API_CALL field in virtual account table is not as expected after approve provisioning", "CHECK ELIGIBILITY", virtualAccount.API_CALL);
        else
            Assert.assertEquals("API_CALL field in virtual account table is not as expected after approve provisioning", "APPROVE PROVISIONING", virtualAccount.API_CALL);

        query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records created into device info table after approve provisioning", DBConnection.recordCount() > 0);

        Assert.assertNotNull("Device id in device info table is not as expected after approve provisioning", deviceInfo.DEVICE_ID);
        deviceID = deviceID == null || deviceID.isEmpty() ? deviceInfo.DEVICE_ID : deviceID;
        Assert.assertEquals("Device id in device info table is not as expected after approve provisioning", deviceID, deviceInfo.DEVICE_ID);
        Assert.assertEquals("Original device field in device info table is not as expected after approve provisioning", "Y", deviceInfo.ORIGINAL_DEVICE);

        HashMap<Object, Object> deviceInfoObj;
        if(!cePostRequestObject.isEmpty()) {
            deviceInfoObj = (HashMap<Object, Object>) cePostRequestObject.get("deviceInfo");
            Assert.assertNotNull(deviceInfoObj);
            Assert.assertEquals("Card scheme device id field in device info table is not as expected after approve provisioning", deviceInfoObj.get("deviceID"), deviceInfo.CARD_SCHEME_DEVICE_ID);
        }
        else {
            deviceInfoObj = (HashMap<Object, Object>) apPostRequestObject.get("deviceInfo");
            Assert.assertNotNull(deviceInfoObj);
//            if (deviceInfo.ORIGINAL_DEVICE.equalsIgnoreCase("y"))
//                Assert.assertNull("Card scheme device id field in device info table is not as expected after approve provisioning", deviceInfo.CARD_SCHEME_DEVICE_ID);
//            else
//                Assert.assertNotNull("Card scheme device id field in device info table is not as expected after approve provisioning", deviceInfo.CARD_SCHEME_DEVICE_ID);
        }

        Assert.assertEquals("Language code field in device info table is not as expected after approve provisioning", deviceInfoObj.get("deviceLanguageCode"), deviceInfo.DEVICE_LANGUAGE_CODE);
        Assert.assertEquals("Original device field in device info table is not as expected after approve provisioning", "Y", deviceInfo.ORIGINAL_DEVICE);

        query = "select * from virtual_risk_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualRiskInfo = new VirtualRiskInfo(databaseSteps.result);

        Assert.assertTrue("No records created into virtual risk info table after approve provisioning", DBConnection.recordCount() > 0);
        Assert.assertEquals("Risk assessment score in virtual risk info table is not as expected after approve provisioning", cardholderInfo.get("riskAssessmentScore"), virtualRiskInfo.CS_RISK_ASSESSMENT_SCORE);

        HashMap<Object, Object> riskInfo = (HashMap<Object, Object>) apEncryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInfo);

        String accHolderName = riskInfo.get("accountHolderName").toString();
        if (!ceEncryptionRequestBody.isEmpty() && postResponseObject.get("actionCode").toString().equalsIgnoreCase("05")) {
            Assert.assertNull("Account holder name score in virtual risk info table is not as expected after approve provisioning", virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertNull("Wallet provider account score in virtual risk info table is not as expected after approve provisioning", virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
            Assert.assertNull("Wallet provider device score in virtual risk info table is not as expected after approve provisioning", virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
            Assert.assertNull("Wallet provider reason codes in virtual risk info table are not as expected after approve provisioning", virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
            Assert.assertNull("Visa token score in virtual risk info table is not as expected after approve provisioning", virtualRiskInfo.VISA_TOKEN_SCORE);
            Assert.assertNull("Visa token decision in virtual risk info table is not as expected after approve provisioning", virtualRiskInfo.VISA_TOKEN_DECISION);
        }
        else if (tokenType.equalsIgnoreCase("SECURE_ELEMENT")) {
            accHolderName = accHolderName.substring(0,accHolderName.length()/4) + StringUtils.repeat("*", accHolderName.length()/2) + accHolderName.substring(accHolderName.length()*3/4);
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after approve provisioning", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after approve provisioning", "x", virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
            Assert.assertEquals("Wallet provider device score in virtual risk info table is not as expected after approve provisioning", riskInfo.get("walletProviderDeviceScore"), virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
            Assert.assertEquals("Wallet provider reason codes in virtual risk info table are not as expected after approve provisioning", riskInfo.get("walletProviderReasonCodes"), virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
            Assert.assertEquals("Visa token score in virtual risk info table is not as expected after approve provisioning", riskInfo.get("visaTokenScore"), virtualRiskInfo.VISA_TOKEN_SCORE);
            Assert.assertEquals("Visa token decision in virtual risk info table is not as expected after approve provisioning", riskInfo.get("visaTokenDecisioning"), virtualRiskInfo.VISA_TOKEN_DECISION);
        }
        else {
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after approve provisioning", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after approve provisioning", riskInfo.get("walletProviderAccountScore"), virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
            Assert.assertEquals("Wallet provider device score in virtual risk info table is not as expected after approve provisioning", riskInfo.get("walletProviderDeviceScore"), virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
            Assert.assertEquals("Wallet provider reason codes in virtual risk info table are not as expected after approve provisioning", riskInfo.get("walletProviderReasonCodes"), virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
            Assert.assertEquals("Visa token score in virtual risk info table is not as expected after approve provisioning", riskInfo.get("visaTokenScore"), virtualRiskInfo.VISA_TOKEN_SCORE);
            Assert.assertEquals("Visa token decision in virtual risk info table is not as expected after approve provisioning", riskInfo.get("visaTokenDecisioning"), virtualRiskInfo.VISA_TOKEN_DECISION);
        }
    }

    @And("^I verify that table entries are as expected after create token for tokenType as \"([^\"]*)\", message reason code as \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterCT(String tokenType, String messageReasonCode, String flowType) throws Exception {
        String query = "select * from token_info where token_reference_id = '" + tokenReferenceID + "'";
        databaseSteps.iEstablishConnectionToLCMDatabase();

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);

        Assert.assertTrue("No records created into token info table after create token", DBConnection.recordCount() > 0);
        virtualAccountID = virtualAccountID == null || virtualAccountID.isEmpty() ? tokenInfo.VIRTUAL_ACCOUNT_ID : virtualAccountID;

        Assert.assertEquals("API call field in token info table is not as expected after create token", "TOKEN CREATE", tokenInfo.API_CALL);
        Assert.assertEquals("Token requestor field in token info table is not as expected after create token", tokenRequesterID, tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertEquals("Virtual account id in token info table is not as expected after create token", virtualAccountID, tokenInfo.VIRTUAL_ACCOUNT_ID);

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) ctEncryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfo);

        Assert.assertEquals("Token field in token info table is not as expected after create token", tokenInfoObj.get("token"), tokenInfo.TOKEN);
        Assert.assertEquals("Token field in token info table is not as expected after create token", tokenInfoObj.get("tokenRequestorName"), tokenInfo.TOKEN_REQUESTOR_NAME);
        Assert.assertEquals("Token field in token info table is not as expected after create token", tokenInfoObj.get("tokenType"), tokenInfo.TOKEN_TYPE);
        Assert.assertEquals("Token status field in token info table is not as expected after create token", "INACTIVE", tokenInfo.TOKEN_STATUS);
        Assert.assertEquals("No. of active tokens field in token info table is not as expected after create token", tokenInfoObj.get("numberOfActiveTokensForPAN").toString(), tokenInfo.NO_OF_ACTIVE_TOKENS);
        Assert.assertEquals("No. of inactive tokens field in token info table is not as expected after create token", tokenInfoObj.get("numberOfInactiveTokensForPAN").toString(), tokenInfo.NO_OF_INACTIVE_TOKENS);
        Assert.assertEquals("No. of suspended tokens field in token info table is not as expected after create token", tokenInfoObj.get("numberOfSuspendedTokensForPAN").toString(), tokenInfo.NO_OF_SUSPENDED_TOKENS);
        Assert.assertEquals("Card org field in token info table is not as expected after create token", "VI", tokenInfo.CARD_ORG);
        Assert.assertEquals("Message reason code field in token info table is not as expected after create token", messageReasonCode.toUpperCase(), tokenInfo.MESSAGE_REASON_CODE);

        HashMap<Object, Object> tokenExpirationDate = (HashMap<Object, Object>) tokenInfoObj.get("tokenExpirationDate");
        Assert.assertNotNull(tokenExpirationDate);

        Assert.assertEquals("Token expiry date field in token info table is not as expected after create token", tokenExpirationDate.get("month") + String.valueOf(tokenExpirationDate.get("year")), tokenInfo.TOKEN_EXPIRY_DATE);

        query = "select * from virtual_account where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records created into virtual account table after create token", DBConnection.recordCount() > 0);
        Assert.assertEquals("Account ID field in virtual account table is not as expected after create token", accountID, virtualAccount.ACCOUNT_ID);
        Assert.assertEquals("Pan reference field in virtual account table is not as expected after create token", ctPostRequestObject.get("panReferenceID"), virtualAccount.PAN_REFERENCE);
        Assert.assertEquals("Status field in virtual account table is not as expected after create token", "INACTIVE", virtualAccount.STATUS);
        Assert.assertEquals("Enrolment flow field in virtual account table is not as expected after create token", flowType.toUpperCase(), virtualAccount.ENROLMENT_FLOW);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) encryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        String cardHolderName = cardholderInfo.get("name").toString();
        if (tokenType.equalsIgnoreCase("SECURE_ELEMENT")) {
            cardHolderName = cardHolderName.substring(0,cardHolderName.length()/4) + StringUtils.repeat("*", cardHolderName.length()/2) + cardHolderName.substring(cardHolderName.length()*3/4);
            Assert.assertEquals("Cardholder name in virtual account table is not as expected after create token", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }
        else {
            Assert.assertEquals("Cardholder name in virtual account table is not as expected after create token", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }

        Assert.assertEquals("Card org field in virtual account table is not as expected after create token", "VI", virtualAccount.CARD_ORG);
        Assert.assertEquals("Pan source field in virtual account table is not as expected after create token", ctPostRequestObject.get("panSource"), virtualAccount.PAN_SOURCE);
        Assert.assertEquals("API call field in virtual account table is not as expected after create token", "TOKEN CREATE", virtualAccount.API_CALL);

        query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records created into device info table after create token", DBConnection.recordCount() > 0);

        Assert.assertNotNull("Device id in device info table is not as expected after create token", deviceInfo.DEVICE_ID);
        deviceID = deviceID == null || deviceID.isEmpty() ? deviceInfo.DEVICE_ID : deviceID;
        Assert.assertEquals("Device id in device info table is not as expected after create token", deviceID, deviceInfo.DEVICE_ID);

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) ctPostRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        Assert.assertEquals("Card scheme device id field in device info table is not as expected after create token", deviceInfoObj.get("deviceID"), deviceInfo.CARD_SCHEME_DEVICE_ID);
        Assert.assertEquals("Language code field in device info table is not as expected after create token", deviceInfoObj.get("deviceLanguageCode"), deviceInfo.DEVICE_LANGUAGE_CODE);
        Assert.assertEquals("Original device field in device info table is not as expected after create token", "Y", deviceInfo.ORIGINAL_DEVICE);

        String encodedDeviceName = deviceInfoObj.get("deviceName").toString();
        Assert.assertEquals("Device name field in device info table is not as expected after create token", encodedDeviceName, deviceInfo.DEVICE_NAME);

        query = "select * from virtual_risk_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualRiskInfo = new VirtualRiskInfo(databaseSteps.result);

        Assert.assertTrue("No records created into virtual risk info table after create token", DBConnection.recordCount() > 0);

        HashMap<Object, Object> riskInfo = (HashMap<Object, Object>) ctEncryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInfo);

        String accHolderName = riskInfo.get("accountHolderName").toString();
        if (tokenType.equalsIgnoreCase("SECURE_ELEMENT")) {
            accHolderName = accHolderName.substring(0,accHolderName.length()/4) + StringUtils.repeat("*", accHolderName.length()/2) + accHolderName.substring(accHolderName.length()*3/4);
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after create token", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after create token", "x", virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
        }
        else {
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after create token", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after create token", riskInfo.get("walletProviderAccountScore"), virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
        }

        Assert.assertEquals("Wallet provider device score in virtual risk info table is not as expected after create token", riskInfo.get("walletProviderDeviceScore"), virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
        Assert.assertEquals("Wallet provider reason codes in virtual risk info table are not as expected after create token", riskInfo.get("walletProviderReasonCodes"), virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
        Assert.assertEquals("Visa token score in virtual risk info table is not as expected after create token", riskInfo.get("visaTokenScore"), virtualRiskInfo.VISA_TOKEN_SCORE);
        Assert.assertEquals("Visa token decision in virtual risk info table is not as expected after create token", riskInfo.get("visaTokenDecisioning"), virtualRiskInfo.VISA_TOKEN_DECISION);
    }

    @And("^I verify that table entries are as expected after update token for tokenType as \"([^\"]*)\", \"([^\"]*)\", message reason code as \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterUT(String tokenType, String tokenStatus, String messageReasonCode, String flowType) throws Exception {
        String query = "select * from token_info where token_reference_id = '" + tokenReferenceID + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);

        Assert.assertTrue("No records in token info table after update token", DBConnection.recordCount() > 0);
        virtualAccountID = virtualAccountID == null || virtualAccountID.isEmpty() ? tokenInfo.VIRTUAL_ACCOUNT_ID : virtualAccountID;

        Assert.assertEquals("API call field in token info table is not as expected after update token", "TOKEN UPDATE", tokenInfo.API_CALL);
        Assert.assertEquals("Token requestor field in token info table is not as expected after update token", tokenRequesterID, tokenInfo.TOKEN_REQUESTOR_ID);
        Assert.assertEquals("Virtual account id in token info table is not as expected after update token", virtualAccountID, tokenInfo.VIRTUAL_ACCOUNT_ID);

        HashMap<Object, Object> tokenInfoObj = (HashMap<Object, Object>) ctEncryptionRequestBody.get("tokenInfo");
        Assert.assertNotNull(tokenInfo);

        Assert.assertEquals("Token field in token info table is not as expected after update token", tokenInfoObj.get("token"), tokenInfo.TOKEN);
        Assert.assertEquals("Token requestor name field in token info table is not as expected after update token", tokenInfoObj.get("tokenRequestorName"), tokenInfo.TOKEN_REQUESTOR_NAME);
        Assert.assertEquals("Token type field in token info table is not as expected after update token", tokenInfoObj.get("tokenType"), tokenInfo.TOKEN_TYPE);
        Assert.assertEquals("Token status field in token info table is not as expected after update token", tokenStatus, tokenInfo.TOKEN_STATUS);
        Assert.assertEquals("No. of active tokens field in token info table is not as expected after update token", tokenInfoObj.get("numberOfActiveTokensForPAN").toString(), tokenInfo.NO_OF_ACTIVE_TOKENS);
        Assert.assertEquals("No. of inactive tokens field in token info table is not as expected after update token", tokenInfoObj.get("numberOfInactiveTokensForPAN").toString(), tokenInfo.NO_OF_INACTIVE_TOKENS);
        Assert.assertEquals("No. of suspended tokens field in token info table is not as expected after update token", tokenInfoObj.get("numberOfSuspendedTokensForPAN").toString(), tokenInfo.NO_OF_SUSPENDED_TOKENS);
        Assert.assertEquals("Card org field in token info table is not as expected after update token", "VI", tokenInfo.CARD_ORG);
        Assert.assertEquals("Message reason code field in token info table is not as expected after update token", messageReasonCode.toUpperCase(), tokenInfo.MESSAGE_REASON_CODE);

        HashMap<Object, Object> tokenExpirationDate = (HashMap<Object, Object>) tokenInfoObj.get("tokenExpirationDate");
        Assert.assertNotNull(tokenExpirationDate);

        Assert.assertEquals("Token expiry date field in token info table is not as expected after update token", tokenExpirationDate.get("month") + String.valueOf(tokenExpirationDate.get("year")), tokenInfo.TOKEN_EXPIRY_DATE);

        query = "select * from virtual_account where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        Assert.assertTrue("No records in virtual account table after update token", DBConnection.recordCount() > 0);
        Assert.assertEquals("Account ID field in virtual account table is not as expected after update token", accountID, virtualAccount.ACCOUNT_ID);
        Assert.assertEquals("Pan reference field in virtual account table is not as expected after update token", postRequestObject.get("panReferenceID"), virtualAccount.PAN_REFERENCE);
        if (tokenStatus.equalsIgnoreCase("Deactivated"))
            Assert.assertEquals("Status field in virtual account table is not as expected after update token", "DELETED", virtualAccount.STATUS);
        else
            Assert.assertEquals("Status field in virtual account table is not as expected after update token", tokenStatus, virtualAccount.STATUS);
        Assert.assertEquals("Enrolment flow field in virtual account table is not as expected after create token", flowType.toUpperCase(), virtualAccount.ENROLMENT_FLOW);

        HashMap<Object, Object> cardholderInfo = (HashMap<Object, Object>) ctEncryptionRequestBody.get("cardholderInfo");
        Assert.assertNotNull(cardholderInfo);

        String cardHolderName = cardholderInfo.get("name").toString();
        if (tokenType.equalsIgnoreCase("SECURE_ELEMENT")) {
            cardHolderName = cardHolderName.substring(0,cardHolderName.length()/4) + StringUtils.repeat("*", cardHolderName.length()/2) + cardHolderName.substring(cardHolderName.length()*3/4);
            Assert.assertEquals("Cardholder name in virtual account table is not as expected after create token", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }
        else {
            Assert.assertEquals("Cardholder name in virtual account table is not as expected after create token", cardHolderName, virtualAccount.CARD_HOLDER_NAME);
        }

        Assert.assertEquals("Card org field in virtual account table is not as expected after update token", "VI", virtualAccount.CARD_ORG);
        Assert.assertEquals("Pan source field in virtual account table is not as expected after update token", ctPostRequestObject.get("panSource"), virtualAccount.PAN_SOURCE);
        Assert.assertEquals("API call field in virtual account table is not as expected after update token", "TOKEN UPDATE", virtualAccount.API_CALL);

        if(deviceID == null || deviceID.isEmpty())
            query = "select * from device_info where virtual_account_id = '" + virtualAccountID + "'";
        else
            query = "select * from device_info where device_id = '" + deviceID + "'";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        deviceInfo = new DeviceInfo(databaseSteps.result);

        Assert.assertTrue("No records updated into device info table after update token", DBConnection.recordCount() > 0);

        Assert.assertNotNull("Device id in device info table is not as expected after update token", deviceInfo.DEVICE_ID);
        deviceID = deviceID == null || deviceID.isEmpty() ? deviceInfo.DEVICE_ID : deviceID;

        HashMap<Object, Object> deviceInfoObj = (HashMap<Object, Object>) ctPostRequestObject.get("deviceInfo");
        Assert.assertNotNull(deviceInfoObj);

        Assert.assertEquals("Card scheme device id field in device info table is not as expected after update token", deviceInfoObj.get("deviceID"), deviceInfo.CARD_SCHEME_DEVICE_ID);
        Assert.assertEquals("Language code field in device info table is not as expected after update token", deviceInfoObj.get("deviceLanguageCode"), deviceInfo.DEVICE_LANGUAGE_CODE);
        Assert.assertEquals("Original device field in device info table is not as expected after update token", "Y", deviceInfo.ORIGINAL_DEVICE);

        query = "select * from virtual_risk_info where virtual_account_id = '" + virtualAccountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualRiskInfo = new VirtualRiskInfo(databaseSteps.result);

        Assert.assertTrue("No records in virtual risk info table after update token", DBConnection.recordCount() > 0);

        HashMap<Object, Object> riskInfo = (HashMap<Object, Object>) ctEncryptionRequestBody.get("riskInformation");
        Assert.assertNotNull(riskInfo);

        String accHolderName = riskInfo.get("accountHolderName").toString();
        if (tokenType.equalsIgnoreCase("SECURE_ELEMENT")) {
            accHolderName = accHolderName.substring(0,accHolderName.length()/4) + StringUtils.repeat("*", accHolderName.length()/2) + accHolderName.substring(accHolderName.length()*3/4);
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after update token", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after update token", "x", virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
        }
        else {
            Assert.assertEquals("Account holder name in virtual risk info table is not as expected after update token", accHolderName, virtualRiskInfo.ACC_HOLDER_NAME);
            Assert.assertEquals("Wallet provider account score in virtual risk info table is not as expected after update token", riskInfo.get("walletProviderAccountScore"), virtualRiskInfo.WALLET_PRVDR_ACCT_SCORE);
        }

        Assert.assertEquals("Wallet provider device score in virtual risk info table is not as expected after update token", riskInfo.get("walletProviderDeviceScore"), virtualRiskInfo.WALLET_PRVDR_DEVICE_SCORE);
        Assert.assertEquals("Wallet provider reason codes in virtual risk info table are not as expected after update token", riskInfo.get("walletProviderReasonCodes"), virtualRiskInfo.WALLET_PRVDR_REASON_CODES);
        Assert.assertEquals("Visa token score in virtual risk info table is not as expected after update token", riskInfo.get("visaTokenScore"), virtualRiskInfo.VISA_TOKEN_SCORE);
        Assert.assertEquals("Visa token decision in virtual risk info table is not as expected after update token", riskInfo.get("visaTokenDecisioning"), virtualRiskInfo.VISA_TOKEN_DECISION);
    }

    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    @And("^I have the VTS event log request body as defined in \"([^\"]*)\"$")
    public void vtsEventLog(String requestBodyPath) throws Exception {
        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", issuer.ISSUER_ID);
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty()) {
            if (restAssuredAPI.globalResponse.getBody().asString()
                    .contains("errorCode"))
                eventLogObject.put("STATUS", "FAILED");
            else
                eventLogObject.put("STATUS", "SUCCESS");
        }
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "VISA_API");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "VISA TOKEN DATA SERVICE");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("400") ) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
    }

    public void iHaveTheExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }

    @And("^I have the VTS external log request body as defined in \"([^\"]*)\"$")
    public void vtsExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheExternalLogRequestBodyAsDefinedIn(requestBodyPath);

        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty())
            externalLogObject.put("STATUS", "SUCCESS");

        JSONObject externalReqOnj = new JSONObject();
        externalReqOnj.putAll(postRequestObject);
        externalReqOnj.put("encryptedData", "Encrypted request payload");
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", externalReqOnj);
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", encryptionRequestBody);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);
    }

    public void checkEligibilityEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "CHECK ELIGIBILITY");
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void checkEligibilityExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v1/checkEligibility";

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "CHECK ELIGIBILITY");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "CHECK ELIGIBILITY");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void approveProvisioningEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "APPROVE PROVISIONING");
        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void approveProvisioningExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v2/" + "tokenRequestors/" + tokenRequesterID + "/tokens/" + tokenReferenceID + "/approveprovisioning" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "APPROVE PROVISIONING");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "APPROVE PROVISIONING");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void createTokenEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "TOKEN_CREATED");

        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void createTokenExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v2/" + "tokenRequestors/" + tokenRequesterID + "/tokens/" + tokenReferenceID + "/tokenChanged" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "TOKEN CREATE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "TOKEN_CREATED");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void updateTokenEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "TOKEN_STATUS_UPDATED");

        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void updateTokenExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v2/" + "tokenRequestors/" + tokenRequesterID + "/tokens/" + tokenReferenceID + "/tokenChanged" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "TOKEN UPDATE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "TOKEN_STATUS_UPDATED");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void cvmEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "GET CVM");

        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void cvmExternalLog() throws ParseException {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v1/retrieveStepUpMethods" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET CVM");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET CVM");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void spEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "SEND PASSCODE");

        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void spExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v1/sendPasscode" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "SEND PASSCODE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "SEND PASSCODE");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    public void deviceBindingEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "DEVICE_BINDING");

        databaseSteps.eventLogArrayObject.clear();
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void deviceBindingExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("VISA_API_DATA") + "/v2/" + "tokenRequestors/" + tokenRequesterID + "/tokens/" + tokenReferenceID + "/deviceBinding" ;

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "DEVICE_BINDING");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "DEVICE_BINDING");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);

        databaseSteps.externalLogArrayObject.clear();
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Then("^I verify that entries are created for check eligibility request in event and external log tables of Common logging service$")
    public void verifyCEEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            checkEligibilityEventLog();
            checkEligibilityExternalLog();

//            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for approve provisioning request in event and external log tables of Common logging service$")
    public void verifyAPEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            approveProvisioningEventLog();
            approveProvisioningExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for create token request in event and external log tables of Common logging service$")
    public void verifyCTEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            createTokenEventLog();
            createTokenExternalLog();

            Thread.sleep(2000);
            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for update token request in event and external log tables of Common logging service$")
    public void verifyUTEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            updateTokenEventLog();
            updateTokenExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for card holder verification request in event and external log tables of Common logging service$")
    public void verifyCVMEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            cvmEventLog();
            cvmExternalLog();

//            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for send passcode request in event and external log tables of Common logging service$")
    public void verifySPEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            spEventLog();
            spExternalLog();

//            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that entries are created for device binding request in event and external log tables of Common logging service$")
    public void verifyDBEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            vtsEventLog("eventlog_table.json");
            vtsExternalLog("externallog_table.json");

            deviceBindingEventLog();
            deviceBindingExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }
}