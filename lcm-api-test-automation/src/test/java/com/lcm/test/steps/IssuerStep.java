package com.lcm.test.steps;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.steps.RESTAssSteps;
import com.lcm.core.utilities.*;
import com.lcm.test.database.*;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.Before;
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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("unchecked")
public class IssuerStep {

    private static final Logger log;

    static {
        log = LoggerFactory.getLogger(IssuerStep.class.getName());
    }

    @Steps
    CSVDataManipulator csvDataManipulator;
    @Steps
    RESTAssSteps restAssSteps;
    @Steps
    RESTAssuredAPI restAssuredAPI;
    @Steps
    DatabaseSteps databaseSteps;
    private final boolean testCardsEnabledAtScenarioLevel = true;
    private Map<String, String> queryParamsAsMap;
    private Map<String, String> headersAsMap;

    private String endpoint;
    private String access_token = null;
    private String encrypted_data = null;
    private final JSONObject encryptionRequestBody = new JSONObject();
    private final String NUMERIC = "0123456789";
    private final LCMProperties apiProperties = new LCMProperties("api.properties");
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final LCMProperties slaConfig = new LCMProperties("SLA.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();
    private String issuerName = System.getProperty("issuer") != null ? System.getProperty("issuer") : runner.getProperty("ISSUER");
    private final String cardScheme = System.getProperty("cardScheme") != null ? System.getProperty("cardScheme").toUpperCase() : runner.getProperty("CARD_SCHEME").toUpperCase();
    private final String issuerVerification = System.getProperty("issuerVerification") != null ? System.getProperty("issuerVerification") : runner.getProperty("ISSUER_VERIFICATION");
    private final String allScenarios = System.getProperty("allScenarios") != null ? System.getProperty("allScenarios") : runner.getProperty("ALL_SCENARIOS");
    private final String commonLogging = System.getProperty("commonLoggingEnabled") != null ? System.getProperty("commonLoggingEnabled") : runner.getProperty("COMMON_LOGGING_ENABLED");

    private final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String dataDriveFilePath = "src/main/resources/data/";
    private final String authHeader = "/valid/headers/authHeader_" + environment + ".csv";
    private final String requestHeader = "/valid/headers/requestHeader.csv";
    private final String IssuerRequestFilePath = "Issuer/request/";
    private final String commonLogFilePath = "Database/CommonLog/";
    private JSONObject postRequestObject = new JSONObject();
    private JSONObject putRequestObject = new JSONObject();
    private JSONObject postResponseObject = new JSONObject();
    private final JSONObject putResponseObject = new JSONObject();
    private JsonObject postResponseObj = null;
    private JSONArray postResponseArrayObject = new JSONArray();
    private JSONObject eventLogObject = new JSONObject();
    private final JSONObject addlEventLogObject = new JSONObject();
    private final JSONObject addlEventLogObject2 = new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private final JSONObject addlexternalLogObject = new JSONObject();
    private String accountRef;
    private String accountID;
    private String account;
    private String walletId = null;
    private String accountExpiry;
    private String tokenRequestorId;
    private String c2pEnrolmentId;
    private int virtualAccounts;

    private AccountInfo accountInfo;
    private BinRange binRange;
    private BinRangeLCMService binRangeLCMService;
    private DeviceInfo deviceInfo;
    static private Issuer issuer;
    private IssuerConfig issuerConfig;
    private TokenInfo tokenInfo;
    private VirtualAccount virtualAccount;
    private C2PEnrolment c2PEnrolment;
    private IDVMethod idvMethod;
    Boolean virtualAccountFlag;
    private IssuerIDVConfig issuerIDVConfig;
    private CodeMapping codeMapping;
    private final String testCardsEnabledAtSuiteLevel = System.getProperty("testCardsEnabled") != null ? System.getProperty("testCardsEnabled") : runner.getProperty("TEST_CARDS_ENABLED");

//    @After(order = 1)
    public void throwSoftFailures() {
        restAssSteps.softAssert.assertAll();
    }

    @And("^I retrieve encrypted payload request body for create or register as defined in \"([^\"]*)\" for \"([^\"]*)\"$")
    public void iHaveTheGetPayloadReuestBodyCreateRegisterAsDefinedInForIssuer(String requestBodyPath, String accounTypeFeature) throws Exception {
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/CreateRegister.json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(issuerName).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());
            Random random = new Random();

            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = JSONHelper.parseJSONObject(testCard.toString()).get("account").toString();
            accountExpiry = JSONHelper.parseJSONObject(testCard.toString()).get("accountExpiry").toString();
            postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
            Assert.assertNotNull(postRequestObject);

            JSONObject jsonCreateAccount = (JSONObject) postRequestObject.get("accountInfo");
            if (jsonCreateAccount.get("account") != null && jsonCreateAccount.get("account").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                jsonCreateAccount.put("account", account);
            if (jsonCreateAccount.get("accountType") != null && jsonCreateAccount.get("accountType").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                jsonCreateAccount.put("accountType", accounTypeFeature.toUpperCase());
            if (jsonCreateAccount.get("accountExpiry") != null && jsonCreateAccount.get("accountExpiry").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                jsonCreateAccount.put("accountExpiry", accountExpiry);
            if (jsonCreateAccount.get("reasonCode") != null && jsonCreateAccount.get("reasonCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                jsonCreateAccount.put("reasonCode", "0000");
        }
    }

    @After(value = "@InAppPayload", order = 6)
    public void getEncryptedPayload(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            iVerifyGetEncryptedPayload();
        }
    }

    @Before(value = "@createAccountOrUpdateAccountState", order = 1)
    public void createAccountOrUpdateAccountState() throws Exception {
        String query = "select * from issuer where lower(issuer_name) like lower('%" + issuerName + "%')";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        issuer = new Issuer(databaseSteps.result);
        fetchAndMapValidTSPVirtualAccount("CARDID", false, false);
        query = "select * from account_info where account_id = '" + accountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        Assert.assertTrue("Account state 'Active' is not as expected on Account Info table in database", accountInfo.ACCOUNT_STATE.equalsIgnoreCase("active"));
        iVerifyUpdateAccountState("SUSPEND");
    }

    @Before(value = "@UpdateServicesNeeded")
    public void verifyTestcardEnabledForUpdateService(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.toString().contains(issuerName.replace(" ", ""))) {
            if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("no"))
                throw new AssumptionViolatedException("Testcards are not enabled. Hence, the Update Service scenario is skipped.");
        }
    }

    @Before(value = "@RenewAccount")
    public void verifyRAEnabledForIssuer(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.contains(issuerName.trim())) {
            if (allScenarios.equalsIgnoreCase("Yes"))
                issuerName = "PP BANK 1";
            else
                throw new AssumptionViolatedException("Renew account functionality is only enabled for PB issuer and it's disabled for all the other issuers incl. " + issuerName + ". Hence, the scenario is skipped.");
        }
    }

    @Before(value = "@GetMerchantListC2P or @EnrolMerchantC2P or @PushNotificationsC2P")
    public void verifyC2PEnabledForIssuer(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());
        if (!scenarioTags.contains(issuerName.trim())) {
            if (allScenarios.equalsIgnoreCase("No")) {
                if (issuerName.equalsIgnoreCase("lunar bank dk") || issuerName.equalsIgnoreCase("bonum")) {

                } else {
                    throw new AssumptionViolatedException("C2P functionality is only enabled for Lunar and Bonum issuer and it's disabled for all the other issuers incl. " + issuerName + ". Hence, the scenario is skipped.");
                }
            } else if (allScenarios.equalsIgnoreCase("Yes")) {
                if(cardScheme.equalsIgnoreCase("mastercard")){
                    throw new AssumptionViolatedException("C2P functionality is only enabled for VISA SCHEME and it's disabled for all the other issuers incl. " + issuerName + ". Hence, the scenario is skipped.");
                }
                else if (!issuerName.equalsIgnoreCase("lunar bank dk") && !issuerName.equalsIgnoreCase("bonum")) {
                    issuerName = "lunar bank dk";
                }
            } else if (cardScheme.equalsIgnoreCase("mastercard")){
                throw new AssumptionViolatedException("C2P functionality is only enabled for VISA SCHEME and it's disabled for all the other issuers incl. " + issuerName + ". Hence, the scenario is skipped.");
            }
        }
    }

    @After(value = "@GetToken", order = 7)
    public void getTokenDetails(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            createAccessTokenData();
            if (VTSStep.issuer != null) {
                iHaveTheIssuerHeadersFor(VTSStep.issuer.ISSUER_NAME);
                if (VTSStep.tokenRequesterName.equalsIgnoreCase("Google Pay"))
                    iVerifyGetToken();
                else if (VTSStep.tokenRequesterName.equalsIgnoreCase("Apple Pay")) {
                    if (VTSStep.accountInfo.ACCOUNT_REF_TYPE != null && VTSStep.accountInfo.ACCOUNT_REF_TYPE.equals("CARDID"))
                        iVerifyGetSchemeToken("CARDID");
                    else if (VTSStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("PAN"))
                        iVerifyGetSchemeTokenByPAN();
                    else if (VTSStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("panid"))
                        iVerifyGetSchemeToken("PANID");
                    else if (VTSStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("lcmid"))
                        iVerifyGetSchemeToken("LCMID");
                }
            }else if(MDESStep.issuer != null){
                iHaveTheIssuerHeadersFor(MDESStep.issuer.ISSUER_NAME);

                if (MDESStep.tokenRequesterName.equalsIgnoreCase("Google Pay"))
                    iVerifyGetToken();
                else if (MDESStep.tokenRequesterName.equalsIgnoreCase("Apple Pay")) {
                    if (MDESStep.accountInfo.ACCOUNT_REF_TYPE != null && MDESStep.accountInfo.ACCOUNT_REF_TYPE.equals("CARDID"))
                        iVerifyGetSchemeToken("CARDID");
                    else if (MDESStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("PAN"))
                        iVerifyGetSchemeTokenByPAN();
                    else if (MDESStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("panid"))
                        iVerifyGetSchemeToken("PANID");
                    else if (MDESStep.accountInfo.ACCOUNT_TYPE.equalsIgnoreCase("lcmid"))
                        iVerifyGetSchemeToken("LCMID");
                }
            }
        }
    }

    @After(value = "@UpdateAccountState", order = 5)
    public void updateAccountState(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            if(VTSStep.accountInfo!=null) {
                String query = "select * from virtual_account where account_id = '" + VTSStep.accountInfo.ACCOUNT_ID + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                virtualAccounts = DBConnection.recordCount();

                if (virtualAccounts < 20) {
                    iVerifyUpdateAccountState("SUSPEND");
                    iVerifyUpdateAccountState("RESUME");
                }
            }else if(MDESStep.accountInfo!=null){
                String query = "select * from virtual_account where account_id = '" + MDESStep.accountInfo.ACCOUNT_ID + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                virtualAccounts = DBConnection.recordCount();

                if (virtualAccounts < 20) {
                    iVerifyUpdateAccountState("SUSPEND");
                    iVerifyUpdateAccountState("RESUME");
                }
            }
        }
    }

    @After(value = "@UpdateVirtualAccountState", order = 4)
    public void updateVirtualAccount(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            if(VTSStep.virtualAccount!=null) {
                if(!VTSStep.virtualAccount.STATUS.equalsIgnoreCase("DELETED")) {
                    iVerifyUpdateVirtualAccountState("SUSPEND");
                    iVerifyUpdateVirtualAccountState("RESUME");
//                iVerifyUpdateVirtualAccountState("ACTIVATE");
                    iVerifyUpdateVirtualAccountState("DELETE");
                }
            }else if(MDESStep.virtualAccount!=null){
                if(!MDESStep.virtualAccount.STATUS.equalsIgnoreCase("DELETED")) {
                    iVerifyUpdateVirtualAccountState("SUSPEND");
                    iVerifyUpdateVirtualAccountState("RESUME");
//                iVerifyUpdateVirtualAccountState("ACTIVATE");
                    iVerifyUpdateVirtualAccountState("DELETE");
                }
            }
        }
    }

    @After(value = "@GetAccountAndVirtualAccount", order = 3)
    public void getAccountVirtualAccount(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            iVerifyGetVirtualAccountIdByTPAN();
            iVerifyGetAccountInformation();
            iVerifyGetVirtualAccountInformation();
        }
    }


    @After(value = "@UpdateProfile", order = 2)
    public void updateProfile(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            iVerifyUpdateProfileID();
            iVerifyUpdateProfileIDforAccountandVirtualAccounts("false");
            if (virtualAccounts < 20)
                iVerifyUpdateProfileIDforAccountandVirtualAccounts("true");
        }
    }

    @After(value = "@RenewReplace", order = 1)
    public void renewReplace(Scenario scenario) throws Exception {
        if (issuerVerification.equalsIgnoreCase("yes") &&
                scenario.getStatus().toString().equalsIgnoreCase("PASSED")) {
            iVerifyRenewAccount();
        }
    }

    @And("^I verify profile id for account is updated as expected")
    public void i_verify_profile_id_for_account_is_updated() throws Exception {
        if (VTSStep.accountInfo != null)
            accountID = VTSStep.accountInfo.ACCOUNT_ID;
        else if (MDESStep.accountInfo != null)
            accountID = MDESStep.accountInfo.ACCOUNT_ID;
        else
            accountID = accountInfo.ACCOUNT_ID;

        String query = "select * from account_info where account_id = '" + accountID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);
        Assert.assertEquals("Profile id is updated", putRequestObject.get("profileID"), accountInfo.PROFILE_ID);
    }

    @Given("I have invalid issuer headers for {string} and {string}")
    public void i_have_invalid_issuer_headers_for_and(String headers, String invalidvalue) throws Exception {
        DatabaseSteps.headersAsMap.put(headers, invalidvalue);
    }

    @Given("I provide invalid accountId {string}")
    public void i_provide_invalid_account_id(String invalidAccount) {
        accountInfo = new AccountInfo();
        accountInfo.ACCOUNT_ID = invalidAccount;
    }

    @And("I fetch {string} accountId of provided issuer from database")
    public void fetchAccountIdByAccountState(String state) throws Exception {
        String query = null;
        String stateTestCard = null;

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" +cardScheme+ "/" +  issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);

            if (state.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "issuerTestCards";
            } else if (state.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (state.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }
            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());
            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();
            query = "select * from account_info where account_id in (select account_id from\n" +
                    "(SELECT a.account_id,COUNT(va.virtual_account_id) FROM virtual_account va,account_info a\n" +
                    "where a.account_id=va.account_id and a.account_state='" + state + "' \n" +
                    "and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))\n" +
                    "GROUP BY (a.account_id)\n" +
                    "HAVING COUNT(va.virtual_account_id)<20 ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only))";
        } else {
            query = "select * from account_info where account_id in (select account_id from\n" +
                    "(SELECT a.account_id,COUNT(va.virtual_account_id) FROM virtual_account va,account_info a\n" +
                    "where a.account_id=va.account_id and a.account_state='" + state + "' \n" +
                    "and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))\n" +
                    "GROUP BY (a.account_id)\n" +
                    "HAVING COUNT(va.virtual_account_id)<20 ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only))";
        }

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " having virtualAccount less than 20 and account state " + state, DBConnection.recordCount() > 0);
        accountInfo = new AccountInfo(databaseSteps.result);
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @And("I fetch the deleted account of provided issuer from database")
    public void i_fetch_the_deleted_account_id_of_provided_issuer() throws Exception {
        String query = "select * from issuer i, account_info ai\n" +
                "where i.issuer_name='" + issuerName + "'\n" +
                "and ai.issuer_id=i.issuer_id\n" +
                "and Account_state  in ('DELETED') \n" +
                "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountID = null;
        if (databaseSteps.result.next()) {
            accountID = databaseSteps.result.getString("ACCOUNT_ID");
            IssuerStep.log.info("Deleted AccountId for this issuer is :" + accountID);
        }
        if (accountID == null) {
            IssuerStep.log.info("Deleted AccountId for this issuer is not present, skipping the scenarios");
            Assert.assertNotNull(accountID);
        } else {
            IssuerStep.log.info("Deleted AccountId for this issuer is :" + accountID);
        }
    }

    @And("I put the request details to profileId service endpoint with virtual account as {string}")
    public void i_put_the_request_details_to_profile_id_service_endpoint_with_virtual_account_as(String virtualAccountFlagParam) {
        if (VTSStep.accountInfo != null)
            accountID = VTSStep.accountInfo.ACCOUNT_ID;
        else if (MDESStep.accountInfo != null)
            accountID = MDESStep.accountInfo.ACCOUNT_ID;
        else
            accountID = accountInfo.ACCOUNT_ID;

        endpoint = apiProperties.getProperty("ISSUER_UPDATE_PROFILE_ACCOUNT_DATA");
        endpoint = endpoint.replace("${ISSUER_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
        endpoint = endpoint.replace("${accountID}", accountID);
        endpoint = endpoint.replace("${virtualAccountFlag}", virtualAccountFlagParam);
        virtualAccountFlag = Boolean.valueOf(virtualAccountFlagParam);
        endpoint = endpoint.replace("${ENVIRONMENT}", runner.getProperty("ENVIRONMENT").toLowerCase());

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.put(endpoint, putRequestObject, DatabaseSteps.headersAsMap, null);
    }

    @And("^I verify profile id for account is not updated")
    public void i_verify_profile_id_for_account_isNot_updated() throws Exception {
        if (accountID != null) {
            String query = "select * from account_info where account_id = '" + accountID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            accountInfo = new AccountInfo(databaseSteps.result);
            String actual = accountInfo.PROFILE_ID;
            if (actual == null) {
                actual = "";
            }
            Assert.assertNotEquals("Profile id is Not updated", postRequestObject.get("profileID"), actual);
        }
    }

    @And("I verify that profile id for virtual accounts is updated as expected when virtual account flag is {string}")
    public void iVerifyThatProfileIdUpdateIsAsExpectedWhenVirtualAccountFlagIs(String virtualAccountFlag) throws Exception {
        if (accountID != null) {
            String query = "select * from virtual_account where status not in ('DELETED') " +
                    " and account_id = '" + accountID + "'";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            ArrayList<String> virtualArray = new ArrayList<>();
            while (databaseSteps.result.next()) {
                virtualArray.add(databaseSteps.result.getString("VIRTUAL_ACCOUNT_ID"));
            }
            for (String loop : virtualArray) {
                String query1 = "select * from virtual_account where virtual_account_id = '" + loop + "'";
                query = "select * from token_info where token is not null and  virtual_account_id = '" + loop + "'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query1);
                virtualAccount = new VirtualAccount(databaseSteps.result);
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                ResultSet tokenInfo1 = databaseSteps.result;
                while (tokenInfo1.next()) {
                    String dbProfileId = tokenInfo1.getString("CARD_METADATA_PROFILE_ID");
                    if (virtualAccountFlag.equalsIgnoreCase("true") && !virtualAccount.STATUS.equals("DELETED") && !accountInfo.ACCOUNT_STATE.equals("DELETED")) {
                        Assert.assertEquals("Profile id is not matched unexpectedly",
                                putRequestObject.get("profileID"), dbProfileId);
                    } else if (virtualAccountFlag.equalsIgnoreCase("false") && !virtualAccount.STATUS.equals("DELETED") && dbProfileId != null) {
                        Assert.assertNotEquals("Profile id is not matched",
                                putRequestObject.get("profileID"), dbProfileId);
                    } else if (Objects.equals(virtualAccount.STATUS, "DELETED") && dbProfileId != null) {
                        Assert.assertNotEquals("Profile id is not matched",
                                putRequestObject.get("profileID"), dbProfileId);
                    }
                }
            }
        } else {
            IssuerStep.log.info("No Virtual Accounts Present");
        }
    }

    @When("^I verify that get encrypted payload is as expected$")
    public void iVerifyGetEncryptedPayload() throws Exception {
        iHaveTheGetEncryptedPayloadRequestBodyAsDefinedIn("valid/body/valid_request_body_InAppPayload");
        iPostTheDetailsToInAppPayloadAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyGetEncryptedPayloadResponse();
        iVerifySLA("Get Encrypted Payload");
    }

    @When("^I verify that get tokens API is able to identify the token created as expected$")
    public void iVerifyGetToken() throws Exception {
        iRetrieveTokensByWalletID();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyTokenDetails();
        iVerifySLA("Get token");
    }

    @When("^I verify that get scheme tokens by PAN API is able to identify the token created as expected$")
    public void iVerifyGetSchemeTokenByPAN() throws Exception {
        createEncryptedDataForPAN("PAN");
        iHaveTheSchemeTokenByPANRequestBodyAsDefinedIn("valid/body/valid_request_body_TPAN");
        iPostTheDetailsToRetrieveSchemeTokensAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifySchemeTokenDetails();
        iVerifySLA("Get scheme token by PAN");
    }

    @When("^I verify that get scheme tokens by \"([^\"]*)\" API is able to identify the token created as expected$")
    public void iVerifyGetSchemeToken(String accountType) throws Exception {
        iHaveTheSchemeTokenRequestBodyAsDefinedIn("valid/body/valid_request_body_ST", accountType);
        iPostTheDetailsToRetrieveSchemeTokensAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifySchemeTokenDetails();
        iVerifySLA("Get scheme token");
    }

    @When("^I verify that get virtual account id by TPAN API is able to identify the token created as expected$")
    public void iVerifyGetVirtualAccountIdByTPAN() throws Exception {
        createEncryptedDataForPAN("TPAN");
        iHaveTheTPANRequestBodyAsDefinedIn("valid/body/valid_request_body_TPAN");
        iPostTheDetailsToRetrieveVAIDByTPANAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        iVerifyVAIDByTPANDetails();
        iVerifySLA("Get account id by TPAN");
    }

    @When("^I verify that get account information for the token created as expected$")
    public void iVerifyGetAccountInformation() throws Exception {
        iRetrieveAccountInformation();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyAccountInfo();
        if (virtualAccounts < 20)
            iVerifySLA("Get account info");
    }

    @When("^I verify that get virtual account information for the token created as expected$")
    public void iVerifyGetVirtualAccountInformation() throws Exception {
        iRetrieveVirtualAccountInformation("false");
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyVirtualAccountInfo();
        iVerifySLA("Get virtual account info");

        iRetrieveVirtualAccountInformation("true");
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyVirtualAccountInfo();
        iVerifySLA("Get virtual account info");

        if (!VTSStep.tokenRequesterName.equalsIgnoreCase("Google Pay") &&
                !VTSStep.tokenRequesterName.equalsIgnoreCase("Apple Pay"))
            verifyDeviceBindingInfo();
    }

    @When("^I verify that update profile id for the token created as expected$")
    public void iVerifyUpdateProfileID() throws Exception {
        iHaveTheUpdateProfileIDRequestBodyAsDefinedIn("valid/body/valid_request_body_UP");
        iUpdateProfileID();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyVirtualAccountProfileID();
        iVerifySLA("Update profile id");
    }

    public void iVerifyUpdateProfileIDforAccountandVirtualAccounts(String virtualAccountFlag) throws Exception {
        iHaveTheUpdateProfileIDRequestBodyAsDefinedIn("valid/body/valid_request_body_UP");
        i_put_the_request_details_to_profile_id_service_endpoint_with_virtual_account_as(virtualAccountFlag);
        RESTAssSteps.iVerifyTheStatusCode(204);
        i_verify_profile_id_for_account_is_updated();
        iVerifyThatProfileIdUpdateIsAsExpectedWhenVirtualAccountFlagIs(virtualAccountFlag);
    }


    @When("^I verify that update account state as \"([^\"]*)\" for the token created as expected$")
    public void iVerifyUpdateAccountState(String accountState) throws Exception {
        iHaveTheUpdateAccountStateRequestBodyAsDefinedIn("valid/body/valid_request_body_AS", accountState);
        iUpdateAccountState();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyAccountState();
        iVerifySLA("Update account state");
    }

    @When("^I verify that update virtual account state as \"([^\"]*)\" for the token created as expected$")
    public void iVerifyUpdateVirtualAccountState(String accountState) throws Exception {
        iHaveTheUpdateVirtualAccountStateRequestBodyAsDefinedIn("valid/body/valid_request_body_VAS", accountState);
        iUpdateVirtualAccountState();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyVirtualAccountState();
        iVerifySLA("Update virtual account state");
    }

    @When("^I verify that renew account is as expected after token creation$")
    public void iVerifyRenewAccount() throws Exception {
        createAccessTokenData();
        iHaveTheIssuerHeadersFor(VTSStep.issuer.ISSUER_NAME);
        iHaveTheRenewRequestBodyAsDefinedIn("valid/body/valid_request_body_Renew");
        iRenewByAccountID();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyAccountInfoRenew();
        iVerifySLA("Renew account");
    }

    @And("^I verify that the response time is under the SLA for \"([^\"]*)\" request$")
    public void iVerifySLA(String request) {
        long expectedSLA = Long.parseLong(slaConfig.getProperty("ISSUER_API"));
        restAssSteps.iVerifyTheSLA(expectedSLA, request);
    }

    @And("^I create a valid bearer token for Issuer service$")
    public void createAccessTokenData() throws Exception {

        iPostTheDetailsToOAuthTokenEndpointIssuer();
        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        } catch (Exception e) {
            throw new Exception(e);
        }

        access_token = postResponseObject.get("access_token").toString();
    }

    public void iPostTheDetailsToOAuthTokenEndpointIssuer() {
        endpoint = apiProperties.getProperty("OAUTH2_DATA");

        if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        String queryParamPath = "Issuer/request" + authHeader;

        iHaveTheIssuerQueryParamsAsDefinedIn(queryParamPath);
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

    @Given("^I have the query parameters for Issuer as defined in \"([^\"]*)\"$")
    public void iHaveTheIssuerQueryParamsAsDefinedIn(String headersPath) {
        queryParamsAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @Given("^I clear the existing bearer token of given Issuer$")
    public void clearBearerToken() {
        access_token = "";
    }

    @Given("^I have the default issuer headers$")
    public void iHaveTheDefaultIssuerHeadersAsDefinedIn() {
        DatabaseSteps.headersAsMap = new HashMap<>();
        DatabaseSteps.headersAsMap.putAll(csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + IssuerRequestFilePath + requestHeader));

        if (DatabaseSteps.headersAsMap.get("X-Request-ID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        DatabaseSteps.headersAsMap.put("Authorization", "Bearer " + access_token);
    }

    public void iHaveTestCardsRetrivalDataC2P(String requestBodyPath, String accountType) throws Exception {
        String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" +cardScheme+ "/" + issuerName.toLowerCase() + ".json";
        JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
        Assert.assertNotNull("No test cards are present for issuer", testCardSets);

        JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("c2pTestCards").toString());
        Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

        Random random = new Random();
        JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
        account = testCard.get("account").toString();
        String query = "select * from account_info where account='" + account + "'";
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(account);
        if (accountType != null && accountType.equalsIgnoreCase("lcmid")) {
            accountID = accountInfo.ACCOUNT_ID;
            accountType = "LCMID";
            if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                account.put("account", accountID);
            if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                account.put("accountType", accountType);
        } else if (accountType != null && accountType.equalsIgnoreCase("panref")) {
            accountRef = accountInfo.ACCOUNT_REF;
            accountType = "PANREF";
            if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                account.put("account", accountRef);
            if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                account.put("accountType", accountType);
        } else {
            accountID = accountInfo.ACCOUNT_ID;
            if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                account.put("account", accountID);
            if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                account.put("accountType", accountType);
        }

    }

    @And("^I have the retrieve merchant list request body as defined in \"([^\"]*)\", \"([^\"]*)\"$")
    public void iHaveTheRetrieveMerchantListRequestBodyAsDefinedIn(String requestBodyPath, String accountType) throws Exception {
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            iHaveTestCardsRetrivalDataC2P(requestBodyPath, accountType);
        } else {
            postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
            Assert.assertNotNull(postRequestObject);
            HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
            Assert.assertNotNull(account);

            fetchAndMapValidC2PAccount(DatabaseSteps.headersAsMap.get("X-Issuer-ID"));

            if (accountType != null && accountType.equalsIgnoreCase("lcmid")) {
                accountID = accountInfo.ACCOUNT_ID;
                if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                    account.put("account", accountID);
                if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                    account.put("accountType", accountType);
            } else if (accountType != null && accountType.equalsIgnoreCase("panref")) {
                accountRef = accountInfo.ACCOUNT_REF;
                if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                    account.put("account", accountRef);
                if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                    account.put("accountType", accountType);
            } else {
                accountID = accountInfo.ACCOUNT_ID;
                if (account.get("account") != null && account.get("account").equalsIgnoreCase("FETCH_FROM_DATABASE"))
                    account.put("account", accountID);
                if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("FETCH_FROM_DATATABLE"))
                    account.put("accountType", accountType);
            }
        }
    }

    @Then("^I fetch or map a valid C2P account for an issuer id \"([^\"]*)\"$")
    public void fetchAndMapValidC2PAccount(String issuerID) throws Exception {
        int iteration1 = 0;

        String query;
        databaseSteps.iEstablishConnectionToLCMDatabase();

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes")) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test card file is present for the issuer " + issuerName, testCardSets);

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("c2pTestCards").toString());
            Assert.assertTrue("No C2P test cards are present for the issuer " + issuerName, testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            query = "select * from account_info where account like '" + account + "%' and issuer_id = '" + issuerID + "' and account_state = 'ACTIVE'";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account " + account, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
            accountID = accountInfo.ACCOUNT_ID;
        } else {
            do {
                query = "select * from bin_range_lcm_service where lcm_service = 'C2P' and bin_range_low = Any(select bin_range_low from bin_range where issuer_id = '" + issuerID + "') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                Assert.assertEquals("No valid C2P bin range found in the database for the given issuer " + issuerID, 1, DBConnection.recordCount());
                binRangeLCMService = new BinRangeLCMService(databaseSteps.result);

                String binRangeLow = binRangeLCMService.BIN_RANGE_LOW;
                binRangeLow = binRangeLow.replaceAll("0+$", "");
                query = "select * from account_info where account like '" + binRangeLow + "%' and issuer_id = '" + issuerID + "' and account_state = 'ACTIVE' and account_ref != 'null' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                if (DBConnection.recordCount() > 0) {
                    accountInfo = new AccountInfo(databaseSteps.result);

                    query = "select * from issuer_config where issuer_id = '" + issuerID + "' and bin_range_low = '" + binRangeLCMService.BIN_RANGE_LOW + "'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

                    if (DBConnection.recordCount() == 0) {
                        query = "select * from issuer_config where issuer_id = '" + issuerID + "' and bin_range_low = '*'";
                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    }

                    if (DBConnection.recordCount() > 0) {
                        issuerConfig = new IssuerConfig(databaseSteps.result);
                        if (issuerConfig.CALL_ISSUER_FOR_C2P.equalsIgnoreCase("N")) {
                            query = "select * from account_lcm_service where account_id = '" + accountInfo.ACCOUNT_ID + "' and lcm_service = 'C2P'";
                            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                            if (DBConnection.recordCount() > 0) {
                                Date dNow = new Date();
                                SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                                Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                                if (dNow.before(dActual))
                                    accountID = accountInfo.ACCOUNT_ID;
                            }
                        } else {
                            Date dNow = new Date();
                            SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                            Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                            if (dNow.before(dActual))
                                accountID = accountInfo.ACCOUNT_ID;
                        }
                    } else {
                        Assert.fail("Issuer and its bin range combination is not configured to issuer config table");
                    }
                }

            } while (accountID == null && iteration1++ < 100);
        }

        Assert.assertNotNull("No valid account found in the database for the given issuer id " + issuerID, accountID);
    }

    @And("^I fetch or map a valid TSP virtual account \"([^\"]*)\", \"([^\"]*)\" and \"([^\"]*)\"$")
    public void fetchAndMapValidTSPVirtualAccount(String accountRefType, boolean isExpired, boolean isInactive) throws Exception {
        String query = null;
        int totalBinRanges = 0;
        int totalAccounts = 0;
        String account_id = null;
        boolean dataFound = false;

        query = "select * from bin_range where issuer_id = '" + DatabaseSteps.headersAsMap.get("X-Issuer-ID") + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        binRange = new BinRange();
        binRange.fetchBinRanges(databaseSteps.result);
        totalBinRanges = binRange.BIN_RANGES_LOW.size();

        for (int idx1 = 0; idx1 < totalBinRanges; idx1++) {
            query = "select * from bin_range_lcm_service where lcm_service = 'TSP' and bin_range_low = '" + binRange.BIN_RANGES_LOW.get(idx1) + "'";

            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            if (DBConnection.recordCount() != 0) {
                String binRangeLow = binRange.BIN_RANGES_LOW.get(idx1);
                binRangeLow = binRangeLow.replaceAll("0+$", "");

                String expiry = "";
                if (isExpired)
                    expiry = " and account_expiry like '%2020' or account_expiry like '%2021' or account_expiry like '%2022'";

                String accRefType = "";
                if (!accountRefType.equals("null"))
                    accRefType = " and account_ref_type = '" + accountRefType + "'";

                query = "select * from account_info where issuer_id = '" + DatabaseSteps.headersAsMap.get("X-Issuer-ID") + "' and account like '" + binRangeLow + "%'" + expiry + accRefType + " and account_state = 'ACTIVE' ORDER BY DBMS_RANDOM.RANDOM";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                accountInfo = new AccountInfo();
                accountInfo.fetchAccounts(databaseSteps.result);
                totalAccounts = accountInfo.ACCOUNTS.size();
                List<String> accounts = accountInfo.ACCOUNTS;
                totalAccounts = Math.min(totalAccounts, 25);

                for (int idx2 = 0; idx2 < totalAccounts; idx2++) {
                    Random random = new Random();
                    int randomIdx = random.nextInt(totalAccounts);
                    String randomAccount = accounts.get(randomIdx);
                    query = "select * from account_info where account = '" + randomAccount + "'" + expiry + accRefType + " and account_state = 'ACTIVE'";

                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    accountInfo = new AccountInfo(databaseSteps.result);

                    Date dNow = new Date();
                    SimpleDateFormat sdformat = new SimpleDateFormat("MMyyyy");
                    Date dActual = sdformat.parse(accountInfo.ACCOUNT_EXPIRY);

                    if (!isExpired && dNow.before(dActual)) {
                        account_id = accountInfo.ACCOUNT_ID;
                        account = accountInfo.ACCOUNT;
                    } else if (isExpired && dNow.after(dActual)) {
                        account_id = accountInfo.ACCOUNT_ID;
                        account = accountInfo.ACCOUNT;
                    } else
                        account_id = null;

                    if (account_id != null) {
                        String status = isInactive ? "INACTIVE" : "ACTIVE";
                        query = "select * from virtual_account where account_id = '" + account_id + "' and status = '" + status + "' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

                        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                        if (DBConnection.recordCount() > 0) {
                            virtualAccount = new VirtualAccount(databaseSteps.result);
                            account = account.substring(0, 4) + StringUtils.repeat("X", account.length() - 8) + account.substring(account.length() - 4);
                            dataFound = true;
                            break;
                        }
                    }
                }
            }

            if (dataFound)
                break;
        }

        Assert.assertTrue("No valid virtual account found for any account in the database for the given issuer " + DatabaseSteps.headersAsMap.get("X-Issuer-ID"), dataFound);
    }

    @Given("^I have the issuer headers for \"([^\"]*)\"$")
    public void iHaveTheIssuerHeadersFor(String issuer) throws Exception {
        iHaveTheDefaultIssuerHeadersAsDefinedIn();
        fetchIssuerDetails(issuer);
    }

    @Given("^I have the issuer headers as defined")
    public void iHaveTheIssuerHeadersForProvidedIssuers() throws Exception {
        iHaveTheDefaultIssuerHeadersAsDefinedIn();
        fetchIssuerDetails(issuerName);
    }

    @Then("^I fetch or map an invalid TSP virtual account \"([^\"]*)\"$")
    public void fetchAndMapInvalidTSPVirtualAccount(String accountID, String virtualAccountID) {
        if (accountID != null && accountID.equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            virtualAccount.ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
            accountInfo.ACCOUNT_ID = virtualAccount.ACCOUNT_ID;
        } else if (accountID != null && accountID.equalsIgnoreCase("")) {
            virtualAccount.ACCOUNT_ID = "";
            accountInfo.ACCOUNT_ID = virtualAccount.ACCOUNT_ID;
        } else if (Objects.equals(accountID, "null")) {
            virtualAccount.ACCOUNT_ID = "null";
            accountInfo.ACCOUNT_ID = virtualAccount.ACCOUNT_ID;
        }

        if (virtualAccountID != null && virtualAccountID.equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            virtualAccount.VIRTUAL_ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        } else if (virtualAccountID != null && virtualAccountID.equalsIgnoreCase("")) {
            virtualAccount.VIRTUAL_ACCOUNT_ID = "";
        } else if (Objects.equals(virtualAccountID, "null")) {
            virtualAccount.VIRTUAL_ACCOUNT_ID = "null";
        }
    }

    @Then("^I fetch issuer details from database$")
    public void fetchIssuerDetails(String issuerName) throws Exception {
        if (issuerName.contains("Oma")) {
            issuer.ISSUER_ID = "FI-10057129101";
            issuer.PROVIDER_ID = "NETSCMS";
        } else {
            String query = "select * from issuer where lower(issuer_name) like lower('" + issuerName + "')";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            issuer = new Issuer(databaseSteps.result);
        }

        if (DatabaseSteps.headersAsMap.get("X-Issuer-ID") != null && DatabaseSteps.headersAsMap.get("X-Issuer-ID").equalsIgnoreCase("FETCH_FROM_DB"))
            DatabaseSteps.headersAsMap.put("X-Issuer-ID", issuer.ISSUER_ID);
        if (DatabaseSteps.headersAsMap.get("X-Provider-ID") != null && DatabaseSteps.headersAsMap.get("X-Provider-ID").equalsIgnoreCase("FETCH_FROM_DB"))
            DatabaseSteps.headersAsMap.put("X-Provider-ID", issuer.PROVIDER_ID);
    }

    @And("^I have enrol to click2pay request body as defined in \"([^\"]*)\", \"([^\"]*)\"$")
    public void iHaveEnrolClick2PayRequestBodyAsDefinedIn(String requestBodyPath, String accountType) throws Exception {
        iHaveTheRetrieveMerchantListRequestBodyAsDefinedIn(requestBodyPath, accountType);
        HashMap<String, String> provider = (HashMap<String, String>) postRequestObject.get("paymentInstrumentProvider");
        Assert.assertNotNull(provider);

        if (provider.get("tokenRequestorID") != null && provider.get("tokenRequestorID").equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            tokenRequestorId = CommonUtil.generateString(new Random(), NUMERIC, 12);
            provider.put("tokenRequestorID", tokenRequestorId);
        }
        if (provider.get("clientDeviceID") != null && provider.get("clientDeviceID").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            provider.put("clientDeviceID", CommonUtil.generateString(new Random(), NUMERIC, 25));
    }

    @And("^Pre-requisite: I enrol a merchant to click2pay feature for \"([^\"]*)\"$")
    public void iEnrolToClick2Pay(String accountType) throws Exception {
        createAccessTokenData();
        iHaveTheDefaultIssuerHeadersAsDefinedIn();
        fetchIssuerDetails(issuerName);
        iHaveEnrolClick2PayRequestBodyAsDefinedIn("valid/body/valid_request_body_1.4", accountType);
        iPostTheDetailsToEnrolClick2PayAPIEndpoint();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyThatResponseHasValidAccountID();
        verifyEnrolClick2PayEntriesInCL();
    }

    @And("^I fetch valid account and virtual account ids for account ref type as \"([^\"]*)\" and pan source as \"([^\"]*)\"$")
    public void iFetchVirtualAccountID(String accountRefType, String panSource) throws Exception {
        fetchAndMapValidTSPVirtualAccount(accountRefType, false, false);
    }

    @Given("I have TSP account id and virtual account ID for {string} and {string} and {string}")
    public void GetVirtualAccInfo(String accountValue, String accountState, String virtualaccountValue) throws Exception {
        String stateTestCard = null;
        String scheme;
        if(cardScheme.equalsIgnoreCase("visa"))
            scheme="vts";
        else scheme="mdes";

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" +cardScheme+ "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            if (accountState.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = scheme +"TestCards";
            } else if (accountState.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and ai.account in ('" + account + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);

            query = "select * from virtual_account where account_id=" + accountInfo.ACCOUNT_ID + " and status = 'ACTIVE' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            virtualAccount = new VirtualAccount(databaseSteps.result);
            Assert.assertTrue("There is no any ACTIVE virtual accounts found in virtual account info table matching the account id " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
        } else {
            String query = "select a.Issuer_id, v.* from virtual_account v join account_info a\n" +
                    "on v.account_id=a.account_id where v.status='" + accountState + "'\n" +
                    " and a.account_state='" + accountState + "' and\n" +
                    "a.issuer_id='" + issuer.ISSUER_ID + "' \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            virtualAccount = new VirtualAccount(databaseSteps.result);
            Assert.assertTrue("There is no any ACTIVE virtual accounts found in virtual account info table matching the account id " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);

            query = "select * from account_info where account_id='" + virtualAccount.ACCOUNT_ID + "'";

            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            accountInfo = new AccountInfo(databaseSteps.result);
            account = accountInfo.ACCOUNT;
        }
        if (accountValue.equalsIgnoreCase("invalid")) {
            virtualAccount.ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        } else if (virtualaccountValue.equalsIgnoreCase("invalid")) {
            virtualAccount.VIRTUAL_ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        }
        if (Objects.equals(virtualaccountValue, "null")) {
            virtualAccount.VIRTUAL_ACCOUNT_ID = "";
        }
        if (Objects.equals(accountValue, "null")) {
            virtualAccount.ACCOUNT_ID = "";
        }
        IssuerStep.log.info(virtualAccount.VIRTUAL_ACCOUNT_ID + " - " + virtualAccount.ACCOUNT_ID);
    }


    @And("^I prepare invalid details for account id as \"([^\"]*)\", virtual account id as \"([^\"]*)\", account ref type as \"([^\"]*)\" and pan source as \"([^\"]*)\"$")
    public void iFetchInvalidAccountDetails(String accountID, String virtualAccountID, String accountRefType, String panSource) throws Exception {
        boolean isExpired = false;
        boolean isInactive = false;
        if (accountID.equalsIgnoreCase("expired"))
            isExpired = true;
        if (virtualAccountID.equalsIgnoreCase("inactive"))
            isInactive = true;

        fetchAndMapValidTSPVirtualAccount(accountRefType, isExpired, isInactive);
        fetchAndMapInvalidTSPVirtualAccount(accountID, virtualAccountID);
    }

    @And("^I create the encrypted data for click2pay push provisioning status$")
    public void createClick2PayPushEncryptedData() throws Exception {
        iPostTheDetailsToC2PEncryptionAPIEndpoint("valid_request_body_C2P_1.8");
        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            postResponseObj = JSONHelper.parseLongJSONObject(restAssuredAPI.globalResponse.getBody().asString());
            encrypted_data = postResponseObj.get("encryptedValue").toString().replace("\"", "");
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("^I create the encrypted data for given \"([^\"]*)\"$")
    public void createEncryptedDataForPAN(String type) throws Exception {
        if (type.equalsIgnoreCase("PAN"))
            iPostTheDetailsToPANEncryptionAPIEndpoint(type, "valid_request_body_ST_PAN");
        else if (type.equalsIgnoreCase("TPAN"))
            iPostTheDetailsToPANEncryptionAPIEndpoint(type, "valid_request_body_IA");

        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = restAssuredAPI.globalResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @When("^I post the details to \"([^\"]*)\" Encryption endpoint for the request body \"([^\"]*)\"$")
    public void iPostTheDetailsToPANEncryptionAPIEndpoint(String type, String body) throws Exception {
        endpoint = apiProperties.getProperty("ENCRYPTION_LI_DATA");
        endpoint = endpoint.replace("${ENCRYPTION_DECRYPTION_DATA}", apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA"));
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String bodyPath = "Encryption/valid/body/" + body + ".json";

        iHaveTheDefaultEncryptionHeadersAsDefinedIn(headerPath);
        iHaveThePANEncryptionRequestBodyAsDefinedIn(bodyPath, type);
        restAssuredAPI.post(encryptionRequestBody, headersAsMap, endpoint);
    }

    @When("^I post the details to C2P Encryption endpoint$")
    public void iPostTheDetailsToC2PEncryptionAPIEndpoint(String body) throws Exception {
        endpoint = apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA");
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String bodyPath = "Encryption/valid/body/" + body + ".json";

        iHaveTheC2PEncryptionHeadersAsDefinedIn(headerPath);
        iHaveTheC2PEncryptionRequestBodyAsDefinedIn(bodyPath);
        restAssuredAPI.post(encryptionRequestBody, DatabaseSteps.headersAsMap, endpoint);
    }

    @Given("^I have the default Encryption headers as defined in \"([^\"]*)\"$")
    public void iHaveTheDefaultEncryptionHeadersAsDefinedIn(String headersPath) {
        headersAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @Given("^I have the C2P Encryption headers as defined in \"([^\"]*)\"$")
    public void iHaveTheC2PEncryptionHeadersAsDefinedIn(String headersPath) {
        DatabaseSteps.headersAsMap = csvDataManipulator.getAllRecordsAsMap(dataDriveFilePath + headersPath);
    }

    @And("^I have the C2P Encryption request body as defined in \"([^\"]*)\"$")
    public void iHaveTheC2PEncryptionRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(postRequestObject);

        encryptionRequestBody.putAll(postRequestObject);
    }

    @And("^I have the get encrypted payload request body as defined in \"([^\"]*)\"$")
    public void iHaveTheGetEncryptedPayloadRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {

        String tokenRequester="" ;

        if(VTSStep.tokenRequesterName!=null){
             tokenRequester = VTSStep.tokenRequesterName;
        }else {
            tokenRequester = MDESStep.tokenRequesterName;
        }

        if (tokenRequester.equalsIgnoreCase("Google Pay"))
            tokenRequester = "_GP";
        else if (tokenRequester.equalsIgnoreCase("Apple Pay"))
            tokenRequester = "_AP";

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + tokenRequester + ".json");
        Assert.assertNotNull(postRequestObject);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(accountInfoObj);

        if(VTSStep.accountInfo!=null) {
            if (accountInfoObj.get("account") != null && accountInfoObj.get("account").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("account", VTSStep.accountInfo.ACCOUNT);
            if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountType", VTSStep.accountInfo.ACCOUNT_TYPE);
            if (accountInfoObj.get("accountExpiry") != null && accountInfoObj.get("accountExpiry").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountExpiry", VTSStep.accountInfo.ACCOUNT_EXPIRY);
            if (accountInfoObj.get("reasonCode") != null && accountInfoObj.get("reasonCode").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                if (tokenRequester.contains("AP"))
                    accountInfoObj.put("reasonCode", "2001");
                else if (tokenRequester.contains("GP"))
                    accountInfoObj.put("reasonCode", "2000");

            HashMap<String, String> providerInfoObj = (HashMap<String, String>) postRequestObject.get("providerInfo");
            Assert.assertNotNull(providerInfoObj);

            if (providerInfoObj.get("clientWalletProvider") != null && providerInfoObj.get("clientWalletProvider").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                providerInfoObj.put("clientWalletProvider", VTSStep.tokenInfo.TOKEN_REQUESTOR_ID);

            if (tokenRequester.contains("GP")) {
                if (providerInfoObj.get("clientDeviceID") != null && providerInfoObj.get("clientDeviceID").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                    providerInfoObj.put("clientDeviceID", VTSStep.deviceInfo.CARD_SCHEME_DEVICE_ID);
            }
        }else if(MDESStep.accountInfo!=null){
            if (accountInfoObj.get("account") != null && accountInfoObj.get("account").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("account", MDESStep.accountInfo.ACCOUNT);
            if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountType", MDESStep.accountInfo.ACCOUNT_TYPE);
            if (accountInfoObj.get("accountExpiry") != null && accountInfoObj.get("accountExpiry").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountExpiry", MDESStep.accountInfo.ACCOUNT_EXPIRY);
            if (accountInfoObj.get("reasonCode") != null && accountInfoObj.get("reasonCode").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
                if (tokenRequester.contains("AP"))
                    accountInfoObj.put("reasonCode", "2001");
                else if (tokenRequester.contains("GP"))
                    accountInfoObj.put("reasonCode", "2000");

            HashMap<String, String> providerInfoObj = (HashMap<String, String>) postRequestObject.get("providerInfo");
            Assert.assertNotNull(providerInfoObj);

            if (providerInfoObj.get("clientWalletProvider") != null && providerInfoObj.get("clientWalletProvider").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                providerInfoObj.put("clientWalletProvider", MDESStep.tokenInfo.TOKEN_REQUESTOR_ID);

            if (tokenRequester.contains("GP")) {
                if (providerInfoObj.get("clientDeviceID") != null && providerInfoObj.get("clientDeviceID").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                    providerInfoObj.put("clientDeviceID", MDESStep.deviceInfo.CARD_SCHEME_DEVICE_ID);
            }
        }
    }

    @And("^I have the PAN Encryption request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveThePANEncryptionRequestBodyAsDefinedIn(String requestBodyPath, String type) throws ParseException, IOException {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + requestBodyPath);
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("account") != null && postRequestObject.get("account").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            if(VTSStep.accountInfo!=null) {

                if (type.equalsIgnoreCase("PAN"))
                    postRequestObject.put("account", VTSStep.accountInfo.ACCOUNT);
                else if (type.equalsIgnoreCase("TPAN"))
                    postRequestObject.put("account", VTSStep.tokenInfo.TOKEN);
            }else if(MDESStep.accountInfo!=null){
                if (type.equalsIgnoreCase("PAN"))
                    postRequestObject.put("account", MDESStep.accountInfo.ACCOUNT);
                else if (type.equalsIgnoreCase("TPAN"))
                    postRequestObject.put("account", MDESStep.tokenInfo.TOKEN);
            }

        if(VTSStep.accountInfo!=null) {
            if (postRequestObject.get("accountExpiry") != null && postRequestObject.get("accountExpiry").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                postRequestObject.put("accountExpiry", VTSStep.accountInfo.ACCOUNT_EXPIRY);
        }else if(MDESStep.accountInfo!=null){
            if (postRequestObject.get("accountExpiry") != null && postRequestObject.get("accountExpiry").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                postRequestObject.put("accountExpiry", MDESStep.accountInfo.ACCOUNT_EXPIRY);
        }
        encryptionRequestBody.putAll(postRequestObject);
    }

    @And("^I have the scheme token request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheSchemeTokenRequestBodyAsDefinedIn(String requestBodyPath, String accountType) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        if(VTSStep.issuer != null) {

            HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
            if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountType", VTSStep.accountInfo.ACCOUNT_REF_TYPE);
            if (accountInfoObj.get("account") != null && accountInfoObj.get("account").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                if (accountType.equalsIgnoreCase("cardid"))
                    accountInfoObj.put("account", VTSStep.accountInfo.ACCOUNT_REF);
                else if (accountType.equalsIgnoreCase("panid"))
                    accountInfoObj.put("account", VTSStep.accountInfo.ACCOUNT_ID);
                else if (accountType.equalsIgnoreCase("lcmid"))
                    accountInfoObj.put("account", VTSStep.accountInfo.ACCOUNT_ID);
        }else if(MDESStep.issuer != null)
        {
            HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
            if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                accountInfoObj.put("accountType", MDESStep.accountInfo.ACCOUNT_REF_TYPE);
            if (accountInfoObj.get("account") != null && accountInfoObj.get("account").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
                if (accountType.equalsIgnoreCase("cardid"))
                    accountInfoObj.put("account", MDESStep.accountInfo.ACCOUNT_REF);
                else if (accountType.equalsIgnoreCase("panid"))
                    accountInfoObj.put("account", MDESStep.accountInfo.ACCOUNT_ID);
                else if (accountType.equalsIgnoreCase("lcmid"))
                    accountInfoObj.put("account", MDESStep.accountInfo.ACCOUNT_ID);
        }
    }

    @And("^I have the scheme token by PAN request body as defined in \"([^\"]*)\"$")
    public void iHaveTheSchemeTokenByPANRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (accountInfoObj.get("encryptedData") != null && accountInfoObj.get("encryptedData").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("encryptedData", encrypted_data);
        if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("accountType", VTSStep.accountInfo.ACCOUNT_TYPE);
    }

    @And("^I have the TPAN request body as defined in \"([^\"]*)\"$")
    public void iHaveTheTPANRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (accountInfoObj.get("accountType") != null && accountInfoObj.get("accountType").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("accountType", "TPAN");
        if (accountInfoObj.get("encryptedData") != null && accountInfoObj.get("encryptedData").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("encryptedData", encrypted_data);
    }

    @And("^I have the update profile id request body as defined in \"([^\"]*)\"$")
    public void iHaveTheUpdateProfileIDRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);

        String[] updateReasonCodes = {"0501", "0502", "0503", "0504"};
        int randomIdx = new Random().nextInt(updateReasonCodes.length);
        String updateReasonCode = updateReasonCodes[randomIdx];

        if (putRequestObject.get("profileID") != null && putRequestObject.get("profileID").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            putRequestObject.put("profileID", CommonUtil.generateUUID());

        if (putRequestObject.get("reasonCode") != null && putRequestObject.get("reasonCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            putRequestObject.put("reasonCode", updateReasonCode);
    }

    @And("^I have the RENEW request body as defined in \"([^\"]*)\"$")
    public void iHaveTheRenewRequestBodyAsDefinedIn(String requestBodyPath) throws Exception {
        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);

        String renewReasonCode = "0001";

        int addMonths = new Random().nextInt(13);
        LocalDate date = LocalDate.of(Integer.parseInt(VTSStep.accountInfo.ACCOUNT_EXPIRY.substring(2)), Month.DECEMBER, 1);
        LocalDate future = date.plusMonths(addMonths);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MMyyyy");
        String futureDate = future.format(formatters);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) putRequestObject.get("accountInfo");
        if (accountInfoObj.get("reasonCode") != null && accountInfoObj.get("reasonCode").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountInfoObj.put("reasonCode", renewReasonCode);

        if (accountInfoObj.get("accountExpiry") != null && accountInfoObj.get("accountExpiry").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("accountExpiry", futureDate);
    }

    @And("^I have RENEW request body as defined in \"([^\"]*)\" for \"([^\"]*)\" for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveRenewRequestBodyAsDefinedIn(String requestBodyPath, String accountType, String accountValue, String accountState) throws Exception {
        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);

        String renewReasonCode = "0001";
        String stateTestCard = null;

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" +cardScheme+ "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards file is present for issuer", testCardSets);
            if (accountState.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "renewAccountTestCard";
            } else if (accountState.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer  " + issuerName, testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and ai.account in ('" + account + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        } else {

            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and Account_state in ('" + accountState + "') \n" +
                    "and ai.account like '%123457040%' ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        }
        if (accountValue.equalsIgnoreCase("invalid")) {
            accountInfo.ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        }

        int addMonths = new Random().nextInt(13);
        LocalDate date = LocalDate.of(Integer.parseInt(accountInfo.ACCOUNT_EXPIRY.substring(2)), Month.DECEMBER, 1);
        LocalDate future = date.plusMonths(addMonths);
        DateTimeFormatter formatters = DateTimeFormatter.ofPattern("MMyyyy");
        String futureDate = future.format(formatters);
        HashMap<String, String> accountInfoObj = (HashMap<String, String>) putRequestObject.get("accountInfo");
        if (accountInfoObj.get("reasonCode") != null && accountInfoObj.get("reasonCode").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountInfoObj.put("reasonCode", renewReasonCode);

        if (accountInfoObj.get("accountExpiry") != null && accountInfoObj.get("accountExpiry").equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            accountInfoObj.put("accountExpiry", futureDate);
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @Then("^I verify that Renew Account entries are created to event and external logs of common logging service as expected$")
    public void renewAccountNeededEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            renewAccountEventLog();
            renewAccountExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void renewAccountEventLog() throws ParseException, IOException {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "RENEW");

        String body = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
                eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);

        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            addlEventLogObject2.putAll(eventLogObject);
            addlEventLogObject2.put("SOURCE", "VISA TOKEN DATA SERVICE");
            addlEventLogObject2.put("DESTINATION", "EXT_VISA");
            addlEventLogObject2.put("ACTION", "PAN_LIFECYCLE");

            databaseSteps.eventLogArrayObject.add(addlEventLogObject2);
        }
    }

    public void renewAccountExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE ACCOUNT");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "RENEW");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", putRequestObject);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
        databaseSteps.externalLogArrayObject.add(externalLogObject);

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            addlexternalLogObject.putAll(externalLogObject);
            addlexternalLogObject.put("ACTION", "PAN_LIFECYCLE");
            addlexternalLogObject.put("HTTP_RESPONSE", "200 OK");
            addlexternalLogObject.put("API_CALL", "PAN_LIFECYCLE");
            databaseSteps.externalLogArrayObject.add(addlexternalLogObject);
        }
    }

    @And("^I verify inApp payload response is as expected for \"([^\"]*)\"$")
    public void iVerifyGetEncryptedPayloadResponsefor(String tokenRequesterName) throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("No response has been generated and the response is " + postResponseObject, postResponseObject);
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("payload"));
        if (tokenRequesterName.equalsIgnoreCase("Apple Pay")) {
            Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("activationData"));
            Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("ephemeralPublicKey"));
        }
    }


    public void getEncryptedPayloadEventLog() throws ParseException, IOException {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "IN APP PROVISIONING");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("400")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            if (accountInfo.ACCOUNT_ID == null) {
                eventLogObject.put("ACCOUNT_ID", null);
            } else {
                eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
            }

        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
                eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }


    @Then("^I verify that for get encrypted data entries are created to event and external logs of common logging service as expected$")
    public void verifyGetEncryptedDataEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            getEncryptedPayloadEventLog();
            getEncryptedPayloadExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    @And("^I retrieve invalid encrypted payload request body as defined in \"([^\"]*)\" for \"([^\"]*)\" and \"([^\"]*)\" with invalid values as \"([^\"]*)\",\"([^\"]*)\"$")
    public void iHaveTheGetinvalidEncryptedPayloadRequestBodyAsDefined(String requestBodyPath, String tokenRequesterName, String accountRefType, String invalidAttribute, String invalidValue) throws Exception {
        iHaveTheGetEncryptedPayloadRequestBodyAsDefinedInForIssuer(requestBodyPath, tokenRequesterName, accountRefType);
        HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (invalidAttribute.equalsIgnoreCase("account")) {
            accountInfoObj.put("account", invalidValue);
        }
        if (invalidAttribute.equalsIgnoreCase("accountExpiry")) {
            accountInfoObj.put("accountExpiry", invalidValue);
        }
    }

    public void getEncryptedPayloadExternalLog() throws ParseException, IOException {

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "IN APP PROVISIONING");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "IN APP PROVISIONING");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("^I have the update account state request body as defined in \"([^\"]*)\" and state as \"([^\"]*)\"$")
    public void iHaveTheUpdateAccountStateRequestBodyAsDefinedIn(String requestBodyPath, String accountState) throws Exception {
        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);
        String[] deleteReasonCodes = {"0005", "0006", "0007", "0008", "0009"};
        int randomIdx = new Random().nextInt(deleteReasonCodes.length);
        String deleteReasonCode = deleteReasonCodes[randomIdx];
        String suspendReasonCode = "0010";
        String activateReasonCode = "0011";
        String reasonCode = null;

        if (accountState.equalsIgnoreCase("resume"))
            reasonCode = activateReasonCode;
        else if (accountState.equalsIgnoreCase("delete"))
            reasonCode = deleteReasonCode;
        else if (accountState.equalsIgnoreCase("suspend"))
            reasonCode = suspendReasonCode;

        if (putRequestObject.get("accountState") != null && putRequestObject.get("accountState").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            putRequestObject.put("accountState", accountState);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) putRequestObject.get("accountInfo");
        if (accountInfoObj.get("reasonCode") != null && accountInfoObj.get("reasonCode").equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            accountInfoObj.put("reasonCode", reasonCode);
    }

    @And("^I have the update virtual account state request body as defined in \"([^\"]*)\" and state as \"([^\"]*)\"$")
    public void iHaveTheUpdateVirtualAccountStateRequestBodyAsDefinedIn(String requestBodyPath, String accountState) throws Exception {
        putRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(putRequestObject);

        String[] deleteReasonCodes = {"0105", "0106", "0107", "0108"};
        int randomIdx = new Random().nextInt(deleteReasonCodes.length);
        String deleteReasonCode = deleteReasonCodes[randomIdx];

        String[] suspendReasonCodes = {"0205", "0206", "0207"};
        randomIdx = new Random().nextInt(suspendReasonCodes.length);
        String suspendReasonCode = suspendReasonCodes[randomIdx];

        String[] resumeReasonCodes = {"0311", "0314"};
        randomIdx = new Random().nextInt(resumeReasonCodes.length);
        String resumeReasonCode = resumeReasonCodes[randomIdx];

        String[] activateReasonCodes = {"0401", "0402"};
        randomIdx = new Random().nextInt(activateReasonCodes.length);
        String activateReasonCode = activateReasonCodes[randomIdx];
        String reasonCode = null;

        if (accountState.equalsIgnoreCase("activate"))
            reasonCode = activateReasonCode;
        else if (accountState.equalsIgnoreCase("resume"))
            reasonCode = resumeReasonCode;
        else if (accountState.equalsIgnoreCase("delete"))
            reasonCode = deleteReasonCode;
        else if (accountState.equalsIgnoreCase("suspend"))
            reasonCode = suspendReasonCode;

        if (putRequestObject.get("accountState") != null && putRequestObject.get("accountState").toString().equalsIgnoreCase("FETCH_FROM_TSP_FLOW"))
            putRequestObject.put("accountState", accountState);

        if (putRequestObject.get("reasonCode") != null && putRequestObject.get("reasonCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            putRequestObject.put("reasonCode", reasonCode);
    }

    @And("^I have the click2pay push provisioning process request body as defined in \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheClick2payPushProvisioningRequestBodyAsDefinedIn(String requestBodyPath, String status) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        Assert.assertNotNull(postRequestObject);

        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        iHaveTheC2PEncryptionHeadersAsDefinedIn(headerPath);
        DatabaseSteps.headersAsMap.put("SM_USER", "visatokenservicescertout.visa.com");
        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        queryParamsAsMap = new HashMap<>();
        queryParamsAsMap.put("eventType", "PUSH_PROVISIONING_STATUS");
        String query = "select * from c2p_enrolment where TOKEN_REQUESTOR = '" + tokenRequestorId + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        c2PEnrolment = new C2PEnrolment(databaseSteps.result);

        if (postRequestObject.get("status") != null && postRequestObject.get("status").toString().equalsIgnoreCase("FETCH_FROM_DATATABLE"))
            postRequestObject.put("status", status.toUpperCase());
        if (postRequestObject.get("issuerTraceID") != null && postRequestObject.get("issuerTraceID").toString().equalsIgnoreCase("FETCH_FROM_DATABASE"))
            postRequestObject.put("issuerTraceID", C2PEnrolment.ISSUER_TRACE_ID);
        if (postRequestObject.get("encryptedData") != null && postRequestObject.get("encryptedData").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME"))
            postRequestObject.put("encryptedData", encrypted_data);
    }

    @And("^I verify that enrolment status is as expected \"([^\"]*)\"$")
    public void iVerifyEnrolmentStatusRemainsSame(String status) throws Exception {
        String query = "select * from c2p_enrolment where TOKEN_REQUESTOR = '" + tokenRequestorId + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        c2PEnrolment = new C2PEnrolment(databaseSteps.result);

        String state = status.contains("SUCCESS") ? "SUCCESS" : "FAILURE";
        Assert.assertEquals("The enrolment status is not as expected after pushing incorrect request payload", "ENROLMENT_" + state, C2PEnrolment.STATUS);
    }

    @And("^I verify that enrolment status still remains same as before$")
    public void iVerifyEnrolmentStatusRemainsSame() throws Exception {
        String query = "select * from c2p_enrolment where TOKEN_REQUESTOR = '" + tokenRequestorId + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        c2PEnrolment = new C2PEnrolment(databaseSteps.result);

        Assert.assertEquals("The enrolment status is not as expected after pushing incorrect request payload", "ENROLMENT_REQUEST_SEND", C2PEnrolment.STATUS);
    }

    @When("^I retrieve the virtual account information based on virtual account id along with realtime data as \"([^\"]*)\"$")
    public void iRetrieveVirtualAccountInformation(String realTime) {
        endpoint = apiProperties.getProperty("ISSUER_VIRTUAL_ACCOUNT_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
//        endpoint = endpoint.replace("${accountID}", virtualAccount == null ? VTSStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
//        endpoint = endpoint.replace("${virtualAccountID}", virtualAccount == null ? VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID : virtualAccount.VIRTUAL_ACCOUNT_ID);

        if(VTSStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}",  VTSStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID );
        }else if(MDESStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}",  MDESStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID );
        }else{
            endpoint = endpoint.replace("${accountID}", virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", virtualAccount.VIRTUAL_ACCOUNT_ID);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        queryParamsAsMap = new HashMap<>();
        queryParamsAsMap.put("realTimeData", realTime);

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.get(endpoint, DatabaseSteps.headersAsMap, queryParamsAsMap);
    }

    @When("^I retrieve the account information based on account id$")
    public void iRetrieveAccountInformation() {
        endpoint = apiProperties.getProperty("ISSUER_ACCOUNT_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if(VTSStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}", virtualAccount == null ? VTSStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
        }else if(MDESStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}", virtualAccount == null ? MDESStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
        }else{
            endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.get(DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I retrieve the tokens based on wallet id$")
    public void iRetrieveTokensByWalletID() {
        endpoint = apiProperties.getProperty("ISSUER_TOKENS_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));


        if (VTSStep.apPostRequestObject.get("clientWalletAccountID") != null) {
            endpoint = endpoint.replace("${walletID}", VTSStep.apPostRequestObject.get("clientWalletAccountID").toString());
        } else if(MDESStep.AuthorizeServiceRequestObject.get("walletId")!= null) {
            endpoint = endpoint.replace("${walletID}", MDESStep.AuthorizeServiceRequestObject.get("walletId").toString());
        }else {
            endpoint = endpoint.replace("${walletID}", walletId);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.get(DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I update the profile id based on account id and virtual account id$")
    public void iUpdateProfileID() {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_PROFILE_VIRTUAL_ACCOUNT_DATA");
        endpoint = endpoint.replace("${ISSUER_VIRTUAL_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_VIRTUAL_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
//        endpoint = endpoint.replace("${accountID}", virtualAccount == null ? VTSStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
//        endpoint = endpoint.replace("${virtualAccountID}", virtualAccount == null ? VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID : virtualAccount.VIRTUAL_ACCOUNT_ID);

        if(VTSStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}",  VTSStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID );
        }else if(MDESStep.accountInfo != null){
            endpoint = endpoint.replace("${accountID}",  MDESStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID );
        }else {
            endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", virtualAccount.VIRTUAL_ACCOUNT_ID);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.put(putRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I renew the account based on account id$")
    public void iRenewByAccountID() {
        endpoint = apiProperties.getProperty("ISSUER_RENEW_REPLACE_DATA");
        endpoint = endpoint.replace("${ISSUER_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (accountInfo != null) {
            endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);
        } else {
            endpoint = endpoint.replace("${accountID}", virtualAccount == null ? VTSStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
        }
        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.put(putRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I update the account state based on account id$")
    public void iUpdateAccountState() {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_ACCOUNT_DATA");
        endpoint = endpoint.replace("${ISSUER_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (VTSStep.virtualAccount != null) {
            endpoint = endpoint.replace("${accountID}", virtualAccount == null ? VTSStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
        }else if(MDESStep.virtualAccount != null) {
            endpoint = endpoint.replace("${accountID}", virtualAccount == null ? MDESStep.virtualAccount.ACCOUNT_ID : virtualAccount.ACCOUNT_ID);
        }else {
            endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.put(putRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I update the virtual account state based on account id$")
    public void iUpdateVirtualAccountState() {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_VIRTUAL_ACCOUNT_DATA");
        endpoint = endpoint.replace("${ISSUER_VIRTUAL_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_VIRTUAL_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (VTSStep.virtualAccount != null) {
            endpoint = endpoint.replace("${accountID}",  VTSStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID );
        }else if(MDESStep.virtualAccount != null) {
            endpoint = endpoint.replace("${accountID}", MDESStep.virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID);
        }else {
            endpoint = endpoint.replace("${accountID}", virtualAccount.ACCOUNT_ID);
            endpoint = endpoint.replace("${virtualAccountID}", virtualAccount.VIRTUAL_ACCOUNT_ID);
        }

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.put(putRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to create and Register payload endpoint$")
    public void iPostTheDetailsToCreateRegisterAPIEndpoint() {
        endpoint = apiProperties.getProperty("CREATE_ACCOUNT_DATA");
        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to inApp payload endpoint$")
    public void iPostTheDetailsToInAppPayloadAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_INAPP_PAYLOAD_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replaceFirst("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to retrieve scheme tokens endpoint$")
    public void iPostTheDetailsToRetrieveSchemeTokensAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_SCHEME_TOKENS_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to retrieve virtual account id by TPAN endpoint$")
    public void iPostTheDetailsToRetrieveVAIDByTPANAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_TPAN_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to retrieve C2P merchant list endpoint$")
    public void iPostTheDetailsToRetrieveMerchantListAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_MERCHANTS_DATA");
        endpoint = endpoint.replace("${ISSUER_CLICK2PAY_DATA}", apiProperties.getProperty("ISSUER_CLICK2PAY_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to enrol click2pay endpoint$")
    public void iPostTheDetailsToEnrolClick2PayAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_ENROL_CLICK2PAY_DATA");
        endpoint = endpoint.replace("${ISSUER_CLICK2PAY_DATA}", apiProperties.getProperty("ISSUER_CLICK2PAY_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @When("^I post the details to click2pay push provisioning endpoint$")
    public void iPostTheDetailsToPushClick2PayAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_PUSH_CLICK2PAY_DATA");
        endpoint = endpoint.replace("${VTS_${ENVIRONMENT}_DATA}", apiProperties.getProperty("VTS_" + environment + "_DATA"));
        endpoint = endpoint.replace("${tokenRequestorId}", tokenRequestorId);

        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @And("^Verify that the response has a valid merchant list$")
    public void verifyThatResponseHasValidMerchantList() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        postResponseArrayObject = (JSONArray) postResponseObject.get("profiles");
        Assert.assertTrue("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject.size() > 0);

        for (Object o : postResponseArrayObject) {
            JSONObject obj = (JSONObject) o;
            Assert.assertNotNull("Merchant name is not as expected and the response is " + obj, obj.get("merchantName"));
            Assert.assertNotNull("Token requester id is not as expected and the response is " + obj, obj.get("tokenRequestorID"));
            Assert.assertNotNull("Merchant logo url is not as expected and the response is " + obj, obj.get("merchantLogoURL"));
            Assert.assertNotNull("Token types are not as expected and the response is " + obj, obj.get("tokenTypes"));
        }
    }

    @And("^Verify that the response has a valid account id$")
    public void verifyThatResponseHasValidAccountID() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("C2P enrolment id is not as expected and the response is " + postResponseObject, postResponseObject.get("c2pEnrolmentId"));
        Assert.assertEquals("Merchant name is not as expected and the response is " + postResponseObject, postResponseObject.get("accountId"), accountInfo.ACCOUNT_ID);
        c2pEnrolmentId = postResponseObject.get("c2pEnrolmentId").toString();
    }

    @Then("^I verify the merchant details on Account Info table in database$")
    public void verifyAccountInfoTable() throws Exception {
        String query = null;
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref"))
            query = "select * from account_info where account_ref = '" + accountID + "'";
        else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan"))
            query = "select * from account_info where account = '" + account.get("account") + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);

        if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("panref")) {
            Assert.assertEquals("Account ref '" + accountID + "' is not found on Account Info table in database", accountInfo.ACCOUNT_REF, accountID);
            Assert.assertTrue("Account type '" + accountInfo.ACCOUNT_REF_TYPE + "' is not as expected on Account Info table in database", account.get("accountType").equalsIgnoreCase(accountInfo.ACCOUNT_REF_TYPE));
            Assert.assertTrue("Account state 'Active' is not as expected on Account Info table in database", accountInfo.ACCOUNT_STATE.equalsIgnoreCase("active"));
        } else if (account.get("accountType") != null && account.get("accountType").equalsIgnoreCase("pan")) {
            Assert.assertEquals("Account '" + account.get("account") + "' is not found on Account Info table in database", accountInfo.ACCOUNT, account.get("account"));
            Assert.assertTrue("Account type '" + accountInfo.ACCOUNT_TYPE + "' is not as expected on Account Info table in database", account.get("accountType").equalsIgnoreCase(accountInfo.ACCOUNT_TYPE));
            Assert.assertTrue("Account state 'Active' is not as expected on Account Info table in database", accountInfo.ACCOUNT_STATE.equalsIgnoreCase("active"));
        }
    }

    @And("^I verify token details as expected$")
    public void iVerifyTokenDetails() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        postResponseArrayObject = (JSONArray) postResponseObject.get("tokenInfoList");
        Assert.assertTrue("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject.size() > 0);
        Assert.assertEquals("No. of tokens retrieved is misleading in get token response", Integer.parseInt(postResponseObject.get("noOfTokens").toString()), postResponseArrayObject.size());

        if(VTSStep.tokenInfo!=null) {

            JSONObject tokenObject = null;
            int idx;
            for (idx = postResponseArrayObject.size() - 1; idx >= 0; idx--) {
                tokenObject = (JSONObject) postResponseArrayObject.get(idx);
                if (tokenObject.get("tokenReferenceID").equals(VTSStep.tokenReferenceID))
                    break;
            }

            Assert.assertTrue("Token created '" + VTSStep.tokenInfo.TOKEN + "' via TSP flow is not found in the response of get token by wallet id API unexpectedly", idx > -1);
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", VTSStep.tokenInfo.TOKEN_STATUS, tokenObject.get("tokenStatus").toString());

            JSONObject accountInfoObj = (JSONObject) tokenObject.get("accountInfo");
            if (VTSStep.accountInfo.ACCOUNT_REF_TYPE != null) {
                Assert.assertEquals("Account is mismatched for the created token in the response of get token by wallet id API", VTSStep.accountInfo.ACCOUNT_REF, accountInfoObj.get("account").toString());
                Assert.assertEquals("Account ref type is mismatched for the created token in the response of get token by wallet id API", VTSStep.accountInfo.ACCOUNT_REF_TYPE, accountInfoObj.get("accountType").toString());
            } else {
                String account = accountInfoObj.get("account").toString();
                account = account.substring(0, (VTSStep.accountInfo.ACCOUNT.length() - 4) / 2) + StringUtils.repeat("x", (VTSStep.accountInfo.ACCOUNT.length() - 4) / 2) + VTSStep.accountInfo.ACCOUNT.substring(VTSStep.accountInfo.ACCOUNT.length() - 4);
                Assert.assertEquals("Account is mismatched for the created token in the response of get token by wallet id API", account, accountInfoObj.get("account").toString());
                Assert.assertEquals("Account ref type is mismatched for the created token in the response of get token by wallet id API", VTSStep.accountInfo.ACCOUNT_TYPE, accountInfoObj.get("accountType").toString());
            }
        }else if(MDESStep.tokenInfo!=null){
            JSONObject tokenObject = null;
            int idx;
            for (idx = postResponseArrayObject.size() - 1; idx >= 0; idx--) {
                tokenObject = (JSONObject) postResponseArrayObject.get(idx);
                if (tokenObject.get("tokenReferenceID").equals(MDESStep.tokenReferenceID))
                    break;
            }

            Assert.assertTrue("Token created '" + MDESStep.tokenInfo.TOKEN + "' via TSP flow is not found in the response of get token by wallet id API unexpectedly", idx > -1);
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", MDESStep.tokenInfo.TOKEN_STATUS, tokenObject.get("tokenStatus").toString());

            JSONObject accountInfoObj = (JSONObject) tokenObject.get("accountInfo");
            if (MDESStep.accountInfo.ACCOUNT_REF_TYPE != null) {
                Assert.assertEquals("Account is mismatched for the created token in the response of get token by wallet id API", MDESStep.accountInfo.ACCOUNT_REF, accountInfoObj.get("account").toString());
                Assert.assertEquals("Account ref type is mismatched for the created token in the response of get token by wallet id API", MDESStep.accountInfo.ACCOUNT_REF_TYPE, accountInfoObj.get("accountType").toString());
            } else {
                String account = accountInfoObj.get("account").toString();
                account = account.substring(0, (MDESStep.accountInfo.ACCOUNT.length() - 4) / 2) + StringUtils.repeat("x", (MDESStep.accountInfo.ACCOUNT.length() - 4) / 2) + MDESStep.accountInfo.ACCOUNT.substring(MDESStep.accountInfo.ACCOUNT.length() - 4);
                Assert.assertEquals("Account is mismatched for the created token in the response of get token by wallet id API", account, accountInfoObj.get("account").toString());
                Assert.assertEquals("Account ref type is mismatched for the created token in the response of get token by wallet id API", MDESStep.accountInfo.ACCOUNT_TYPE, accountInfoObj.get("accountType").toString());
            }
        }
    }

    @And("^I verify inApp payload response is as expected$")
    public void iVerifyGetEncryptedPayloadResponse() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("No response has been generated and the response is " + postResponseObject, postResponseObject);
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("payload"));
        if(VTSStep.tokenRequesterName!=null) {
            if (VTSStep.tokenRequesterName.equalsIgnoreCase("Apple Pay")) {
                Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("activationData"));
                Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("ephemeralPublicKey"));
            }
        }if(MDESStep.tokenRequesterName!=null) {
            if (MDESStep.tokenRequesterName.equalsIgnoreCase("Apple Pay")) {
                Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("activationData"));
                Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject.get("ephemeralPublicKey"));
            }
        }
    }

    @And("^I verify scheme token details as expected$")
    public void iVerifySchemeTokenDetails() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        postResponseArrayObject = (JSONArray) postResponseObject.get("tokenInfo");
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject);
        Assert.assertEquals("No. of tokens retrieved is misleading in get scheme token response", Integer.parseInt(postResponseObject.get("noOfTokens").toString()), postResponseArrayObject.size());

        if (VTSStep.issuer != null) {
            JSONObject tokenObject = null;
            int idx;
            for (idx = postResponseArrayObject.size() - 1; idx >= 0; idx--) {
                tokenObject = (JSONObject) postResponseArrayObject.get(idx);
                if (tokenObject.get("tokenReferenceID").equals(VTSStep.tokenReferenceID))
                    break;
            }

            Assert.assertTrue("Token created via TSP flow is not found in the response of get token by wallet id API unexpectedly", idx > -1);
            Assert.assertEquals("Token created doesn't have expected token requester id in the response of get scheme token", VTSStep.tokenInfo.TOKEN_REQUESTOR_ID, tokenObject.get("tokenRequestorID").toString());
            Assert.assertEquals("Token created doesn't have expected wallet id in the response of get scheme token", VTSStep.virtualAccount.CLIENT_WALLET_ID, tokenObject.get("walletID").toString());
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", VTSStep.tokenInfo.TOKEN_STATUS, tokenObject.get("tokenStatus").toString());
        }else if (MDESStep.issuer != null)
        {
            JSONObject tokenObject = null;
            int idx;
            for (idx = postResponseArrayObject.size() - 1; idx >= 0; idx--) {
                tokenObject = (JSONObject) postResponseArrayObject.get(idx);
                if (tokenObject.get("tokenReferenceID").equals(MDESStep.tokenReferenceID))
                    break;
            }

           // Assert.assertTrue("Token created via MDES flow is not found in the response of get scheme token API unexpectedly", idx > -1);
           // Assert.assertEquals("Token created doesn't have expected token requester id in the response of get scheme token", MDESStep.tokenInfo.TOKEN_REQUESTOR_ID, tokenObject.get("tokenRequestorID").toString()); //in db 4--- in reponse 5---
           // Assert.assertEquals("Token created doesn't have expected wallet id in the response of get scheme token", MDESStep.virtualAccount.CLIENT_WALLET_ID, tokenObject.get("walletID").toString());
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", MDESStep.tokenInfo.TOKEN_STATUS, tokenObject.get("tokenStatus").toString());

        }
    }

    @And("^I verify the virtual account id as expected$")
    public void iVerifyVAIDByTPANDetails() throws ParseException {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertNotNull("Unexpected response has been generated and the response is " + postResponseObject, postResponseObject);
        if (VTSStep.tokenInfo != null) {
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", VTSStep.tokenInfo.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID").toString());
        }else if(MDESStep.tokenInfo != null) {
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", MDESStep.tokenInfo.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID").toString());
        }else
        {
            Assert.assertEquals("Token created doesn't have expected status in the response of get token by wallet id API", tokenInfo.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID").toString());
        }
    }

    @Then("^I verify the virtual account information on Virtual Account table in database$")
    public void verifyVirtualAccountTable() throws Exception {
        String query = "select * from virtual_account where virtual_account_id = '" + virtualAccount.VIRTUAL_ACCOUNT_ID + "'";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertEquals("Virtual account id '" + postResponseObject.get("virtualAccountID") + "' is not matched unexpectedly", virtualAccount.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID"));
        //   Assert.assertEquals("Pan number '" + postResponseObject.get("pan") + "' is not matched unexpectedly for the given virtual account id '" + virtualAccount.VIRTUAL_ACCOUNT_ID + "'", account, postResponseObject.get("pan"));
        Assert.assertEquals("Pan source '" + postResponseObject.get("panSource") + "' is not matched unexpectedly for the given virtual account id '" + virtualAccount.VIRTUAL_ACCOUNT_ID + "'", virtualAccount.PAN_SOURCE, postResponseObject.get("panSource"));
    }

    @Then("^I verify the account info as expected after RenewAccount$")
    public void verifyAccountInfoRenew() throws Exception {
        JSONObject accountInfoObj = (JSONObject) putRequestObject.get("accountInfo");
        Assert.assertNotNull(accountInfoObj);

        if (accountInfo != null) {
            accountInfo.ACCOUNT_ID = accountInfo.ACCOUNT_ID;
        } else {
            accountInfo.ACCOUNT_ID = VTSStep.accountInfo.ACCOUNT_ID;
        }

        String query = "select * from account_info where account_id like '" + accountInfo.ACCOUNT_ID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);

        Assert.assertEquals("Account expiry is not matched unexpectedly", accountInfo.ACCOUNT_EXPIRY, accountInfoObj.get("accountExpiry"));
    }

    @Then("^I verify the account information as expected$")
    public void verifyAccountInfo() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());

        JSONObject accountInfo = (JSONObject) postResponseObject.get("accountInfo");
        Assert.assertNotNull(accountInfo);

        if (VTSStep.issuer != null) {
            Assert.assertEquals("Issuer id '" + accountInfo.get("issuerId") + "' is not matched unexpectedly", VTSStep.issuer.ISSUER_ID, accountInfo.get("issuerId"));
            Assert.assertEquals("Issuer name '" + accountInfo.get("issuerName") + "' is not matched unexpectedly for the given account id'", VTSStep.issuer.ISSUER_NAME, accountInfo.get("issuerName"));
            Assert.assertEquals("Account type '" + accountInfo.get("accountType") + "' is not matched unexpectedly for the given account id '", VTSStep.accountInfo.ACCOUNT_TYPE, accountInfo.get("accountType"));
            Assert.assertEquals("Account expiry '" + accountInfo.get("accountExpiry") + "' is not matched unexpectedly", VTSStep.accountInfo.ACCOUNT_EXPIRY, accountInfo.get("accountExpiry"));

            JSONObject services = (JSONObject) accountInfo.get("servicesNeeded");
            Assert.assertNotNull(services);

            JSONArray servicesArray = (JSONArray) services.get("service");
            Assert.assertTrue("TSP service is not added to the account id unexpectedly", servicesArray.contains("TSP"));

            JSONObject idvMethods = (JSONObject) postResponseObject.get("idvMethods");
            Assert.assertNotNull(idvMethods);

            JSONArray idvMethodsArray = (JSONArray) idvMethods.get("idvMethod");
            for (Object o : idvMethodsArray) {
                JSONObject idvMethodObj = (JSONObject) o;
                if (idvMethodObj.get("idvChannel").toString().contains("SMS")) {
                    String query = "select * from idv_method where account_id = '" + VTSStep.accountInfo.ACCOUNT_ID + "' and channel = 'SMS'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    idvMethod = new IDVMethod(databaseSteps.result);

                    Assert.assertEquals("Contact info is not as expected for sms in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("EMAIL")) {
                    String query = "select * from idv_method where account_id = '" + VTSStep.accountInfo.ACCOUNT_ID + "' and channel = 'EMAIL'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    idvMethod = new IDVMethod(databaseSteps.result);

                    Assert.assertEquals("Contact info is not as expected for email in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("APP_TO_APP")) {
                    Assert.assertEquals("App2App Identifier is not as expected in cvm response payload", VTSStep.issuerIDVConfig.APP_APP_IDENTIFIER, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("CUSTOMERCARE") || idvMethodObj.get("idvChannel").toString().contains("CUSTOMERSERVICE")) {
                    if (VTSStep.issuerIDVConfig.CC_IDV_ENABLED.equalsIgnoreCase("y") && VTSStep.issuerIDVConfig.CC_IDV_DEFAULT_VALUE.equalsIgnoreCase("y"))
                        Assert.assertEquals("Customer care identifier is not as expected in cvm response payload", VTSStep.issuerIDVConfig.CC_IDV_DEFAULT_VALUE, idvMethodObj.get("idvContactInfo"));
                } else
                    Assert.fail("Unexpected IDV method present for the given account " + idvMethodObj.get("idvChannel").toString());
            }

            JSONObject virtualAccounts = (JSONObject) postResponseObject.get("virtualAccounts");
            Assert.assertNotNull(virtualAccounts);

            JSONArray vaIdArray = (JSONArray) virtualAccounts.get("virtualAccountID");
            Assert.assertTrue("Virtual account id created is not present in the virtual account list unexpectedly", vaIdArray.contains(VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID));
            Assert.assertEquals("Account state is not matched unexpectedly", VTSStep.accountInfo.ACCOUNT_STATE, postResponseObject.get("accountState"));
        }else if (MDESStep.issuer != null)
        {
            Assert.assertEquals("Issuer id '" + accountInfo.get("issuerId") + "' is not matched unexpectedly", MDESStep.issuer.ISSUER_ID, accountInfo.get("issuerId"));
            Assert.assertEquals("Issuer name '" + accountInfo.get("issuerName") + "' is not matched unexpectedly for the given account id'", MDESStep.issuer.ISSUER_NAME, accountInfo.get("issuerName"));
            Assert.assertEquals("Account type '" + accountInfo.get("accountType") + "' is not matched unexpectedly for the given account id '", MDESStep.accountInfo.ACCOUNT_TYPE, accountInfo.get("accountType"));
            Assert.assertEquals("Account expiry '" + accountInfo.get("accountExpiry") + "' is not matched unexpectedly", MDESStep.accountInfo.ACCOUNT_EXPIRY, accountInfo.get("accountExpiry"));

            JSONObject services = (JSONObject) accountInfo.get("servicesNeeded");
            Assert.assertNotNull(services);

            JSONArray servicesArray = (JSONArray) services.get("service");
            Assert.assertTrue("TSP service is not added to the account id unexpectedly", servicesArray.contains("TSP"));

            JSONObject idvMethods = (JSONObject) postResponseObject.get("idvMethods");
            Assert.assertNotNull(idvMethods);

            JSONArray idvMethodsArray = (JSONArray) idvMethods.get("idvMethod");
            for (Object o : idvMethodsArray) {
                JSONObject idvMethodObj = (JSONObject) o;
                if (idvMethodObj.get("idvChannel").toString().contains("SMS")) {
                    String query = "select * from idv_method where account_id = '" + MDESStep.accountInfo.ACCOUNT_ID + "' and channel = 'SMS'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    idvMethod = new IDVMethod(databaseSteps.result);

                    Assert.assertEquals("Contact info is not as expected for sms in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("EMAIL")) {
                    String query = "select * from idv_method where account_id = '" + MDESStep.accountInfo.ACCOUNT_ID + "' and channel = 'EMAIL'";
                    databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                    idvMethod = new IDVMethod(databaseSteps.result);

                    Assert.assertEquals("Contact info is not as expected for email in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("APP_TO_APP")) {
                    Assert.assertEquals("App2App Identifier is not as expected in cvm response payload", MDESStep.issuerIDVConfig.APP_APP_IDENTIFIER, idvMethodObj.get("idvContactInfo"));
                } else if (idvMethodObj.get("idvChannel").toString().contains("CUSTOMERCARE") || idvMethodObj.get("idvChannel").toString().contains("CUSTOMERSERVICE")) {
                    if (MDESStep.issuerIDVConfig.CC_IDV_ENABLED.equalsIgnoreCase("y") && MDESStep.issuerIDVConfig.CC_IDV_DEFAULT_VALUE.equalsIgnoreCase("y"))
                        Assert.assertEquals("Customer care identifier is not as expected in cvm response payload", MDESStep.issuerIDVConfig.CC_IDV_DEFAULT_VALUE, idvMethodObj.get("idvContactInfo"));
                } else
                    Assert.fail("Unexpected IDV method present for the given account " + idvMethodObj.get("idvChannel").toString());
            }

            JSONObject virtualAccounts = (JSONObject) postResponseObject.get("virtualAccounts");
            Assert.assertNotNull(virtualAccounts);

            JSONArray vaIdArray = (JSONArray) virtualAccounts.get("virtualAccountID");
            Assert.assertTrue("Virtual account id created is not present in the virtual account list unexpectedly", vaIdArray.contains(MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID));
            Assert.assertEquals("Account state is not matched unexpectedly", MDESStep.accountInfo.ACCOUNT_STATE, postResponseObject.get("accountState"));

        }
    }

    @Then("^I verify that get account info entries are created to event and external logs of common logging service as expected$")
    public void verifyGetAccountInfoEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            accountInfoEventLog();
            accountInfoExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that create account info entries are created to event and external logs of common logging service as expected$")
    public void verifyCreateAccountInfoEntriesInCL() throws Exception {
        issuerEventLog("eventlog_table.json");
        issuerExternalLog("externallog_table.json");

        createAccountInfoEventLog();
        createAccountInfoExternalLog();

        databaseSteps.verifyCommonLoggingService();
    }

    public void createAccountInfoEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("201")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        }
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "CREATE");

        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void accountInfoEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("20")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
        }
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "GET ACCOUNT");

        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    @When("^I retrieve the account Id from provided account information$")
    public void iRetrieveAccountId() {
        endpoint = apiProperties.getProperty("ISSUER_ACCOUNT_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
        endpoint = endpoint.replace("${accountID}", "accountid");
        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @Then("^I verify the account id is as expected in response$")
    public void verifyAccountIdInDatabase() throws Exception {
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertEquals("Account id '" + postResponseObject.get("accountID") + "' is not matched unexpectedly", accountInfo.ACCOUNT_ID, postResponseObject.get("accountID"));
    }

    @Given("I get account id by account info request body as defined {string} for {string} and {string}")
    public void getAccountIdByAccountInfoRequestBody(String validRequestBodies, String accountType, String accountState) throws Exception {
        String stateTestCard = null;

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + validRequestBodies + ".json");
        Assert.assertNotNull(postRequestObject);
        HashMap<String, String> accountJson = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(accountJson);

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" +  issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            if (accountState.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "issuerTestCards";
            } else if (accountState.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            accountJson.put("account", account);
            accountJson.put("accountType", accountType);
            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and ai.account in ('" + account + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        } else {
            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and Account_type in ('" + accountType + "') \n" +
                    "and Account_state in ('" + accountState + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);

            accountJson.put("account", accountInfo.ACCOUNT);
            accountJson.put("accountType", accountType);
            IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);

        }
    }

    @Given("I have update IDV request body as defined {string} for {string} for {string} and {string}")
    public void updateIDVRequestBody(String validRequestBodies, String accountType, String accountValue, String accountState) throws Exception {
        String stateTestCard = null;

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + validRequestBodies + ".json");
        Assert.assertNotNull(postRequestObject);

        HashMap<Object, Object> idvMethods = (HashMap<Object, Object>) postRequestObject.get("idvMethods");
        Assert.assertNotNull(idvMethods);

        JSONArray idvMethodArray = (JSONArray) idvMethods.get("idvMethod");

        String SMS = "+91" + CommonUtil.generateString(new Random(), NUMERIC, 10);
        String EMAIL = CommonUtil.generateString(new Random(), ALPHA, 12) + "@nets.eu";

        if (validRequestBodies.startsWith("valid")) {
            for (Object o : idvMethodArray) {
                JSONObject idvMethod = (JSONObject) o;
                if (idvMethod.get("idvChannel").equals("SMS")) {
                    idvMethod.put("idvContactInfo", SMS);
                } else if (idvMethod.get("idvChannel").equals("EMAIL")) {
                    idvMethod.put("idvContactInfo", EMAIL);
                }
            }
        }

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            if (accountState.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "issuerTestCards";
            } else if (accountState.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and ai.account in ('" + account + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        } else {
            String query = "select * from account_info \n" +
                    "where issuer_id='" + issuer.ISSUER_ID + "' " +
                    " and account_id in (SELECT t1.account_id FROM idv_method t1 INNER JOIN idv_method t2\n" +
                    "ON t1.account_id=t2.account_id WHERE \n" +
                    "t1.Channel='SMS' AND t2.Channel='EMAIL') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        }
        if (accountValue.equalsIgnoreCase("invalid")) {
            accountInfo.ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        }
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @When("^I put the request details to UpdateIDV endpoint$")
    public void IPutTheDetailsToUpdateIDVAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_IDV_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
        endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replaceFirst("pp", environment.toLowerCase());

        restAssuredAPI.put(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @Then("^I verify the update IDV response as expected$")
    public void verifyUpdateIDVResponse() throws Exception {

        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        final int actualStatusCode = RESTAssuredAPI.globalStaticResponse.getStatusCode();
        if (actualStatusCode == 200) {
            Assert.assertEquals("Account id '" + postResponseObject.get("accountID") + "' is not matched unexpectedly", accountInfo.ACCOUNT_ID, postResponseObject.get("accountID"));
            Assert.assertEquals("Account state '" + postResponseObject.get("accountState") + "' is not matched unexpectedly", "UPDATED", postResponseObject.get("accountState"));
            String query = "select * from idv_method where account_id = '" + accountInfo.ACCOUNT_ID + "'";
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            idvMethod = new IDVMethod(databaseSteps.result);

            Assert.assertTrue("No records created into idv method table after update idv", DBConnection.recordCount() > 1);

        } else if (actualStatusCode == 404) {
            Assert.assertEquals("Error Code '" + postResponseObject.get("errorCode") + "' is not matched unexpectedly", "LCM-1012", postResponseObject.get("errorCode"));
        } else {
            Assert.assertEquals("Error Code '" + postResponseObject.get("errorCode") + "' is not matched unexpectedly", "LCM-1014", postResponseObject.get("errorCode"));
        }


    }

    @Then("^I verify that update IDV entries are created for to event and external logs of common logging service as expected$")
    public void updateIDVEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            updateIDVEventLog();
            updateIDVExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void updateIDVEventLog() throws ParseException, IOException {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "UPDATE_IDV");
        eventLogObject.put("STATUS", "SUCCESS");

        String body = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("404")) {
            eventLogObject.put("ACCOUNT_ID", null);
            eventLogObject.put("STATUS", "FAILED");
        } else if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200") && body.contains("1012")) {
            eventLogObject.put("STATUS", "Failed");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);

    }

    public void updateIDVExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE_IDV");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE_IDV");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Given("I have Update Services Needed request body as defined {string} for {string} for {string} and {string} and action is {string} the {string} service")
    public void UpdateServicesNeededRequestBody(String validRequestBodies, String accountType, String accountValue, String accountState, String Action, String Service) throws Exception {
        String stateTestCard = null;

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + validRequestBodies + ".json");
        Assert.assertNotNull(postRequestObject);

        if (postRequestObject.get("accountAction") != null && postRequestObject.get("accountAction").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            postRequestObject.put("accountAction", Action);
        }
        HashMap<Object, Object> AccountInfo = (HashMap<Object, Object>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(AccountInfo);

        if (AccountInfo.get("reasonCode") != null && AccountInfo.get("reasonCode").toString().equalsIgnoreCase("DEFINE_AT_RUNTIME")) {
            AccountInfo.put("reasonCode", "0012");
        }
        HashMap<Object, Object> servicesNeeded = (HashMap<Object, Object>) AccountInfo.get("servicesNeeded");
        Assert.assertNotNull(servicesNeeded);

        JSONArray service = (JSONArray) servicesNeeded.get("service");

        if (service.contains("DEFINE_AT_RUNTIME")) {
            service.clear();
            service.add(Service);
        }

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            if (accountState.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "updateserviceTestCard";
            } else if (accountState.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (accountState.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and ai.account in ('" + account + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        } else {
            String query = "select * from issuer i, account_info ai\n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and Account_type in ('" + accountType + "') \n" +
                    "and Account_state in ('" + accountState + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

            Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName + " and account state " + accountState, DBConnection.recordCount() > 0);
            accountInfo = new AccountInfo(databaseSteps.result);
        }
        if (accountValue.equalsIgnoreCase("invalid")) {
            accountInfo.ACCOUNT_ID = CommonUtil.generateString(new Random(), NUMERIC, 26);
        }
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @When("^I put the request details to Update Services Needed endpoint$")
    public void IPutTheDetailsToUpdateAccountActionAPIEndpoint() {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_ACCOUNT_ACTION_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
        endpoint = endpoint.replace("${accountID}", accountInfo.ACCOUNT_ID);

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replaceFirst("pp", environment.toLowerCase());

        restAssuredAPI.put(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @Then("^I verify the LCM error code \"(.*?)\" in the response body$")
    public void iVerifyResponseForLCMErrorCode(String code) throws ParseException {
        String errorCode = String.valueOf(JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()).get("errorCode"));
        Assert.assertEquals("Invalid LCM error code is displayed for invalid encrypted PAN data", "LCM-" + code, errorCode);
    }

    @And("^I verify that table entries are as expected after update service for \"([^\"]*)\"$")
    public void iVerifyTableEntriesAfterUpdateservice(String action) throws Exception {

//        String query = "select * from account_lcm_service where account_id = '" + accountInfo.ACCOUNT_ID + "'";
//        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
//        AccountLCMService accountLcmService  = new AccountLCMService(databaseSteps.result);

//            if(action.equalsIgnoreCase("Add")){
//                Assert.assertTrue("No records created into idv method table after update idv", DBConnection.recordCount() > 0);
//            }else if(action.equalsIgnoreCase("Remove")){
//                Assert.assertEquals("No records created into idv method table after update idv", 0, DBConnection.recordCount());
//            }

    }


    @Then("^I verify that verify service needed entries are created for \"(.*?)\" to event and external logs of common logging service as expected$")
    public void verifyServiceNeededEntriesInCL(String action) throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            serviceneededEventLog(action);
            serviceneededExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void serviceneededEventLog(String action) throws ParseException, IOException {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "UPDATE SERVICES NEEDED");

        String body = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200") && body.contains("1011")) {
            eventLogObject.put("ACCOUNT_ID", null);
            eventLogObject.put("STATUS", "FAILED");
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200") && !body.isEmpty()) {
            if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
                eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
            eventLogObject.put("STATUS", "FAILED");
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);

        if (action.equalsIgnoreCase("remove")) {
            addlEventLogObject2.putAll(eventLogObject);
            addlEventLogObject2.put("SOURCE", "LCM_SERVICE");
            addlEventLogObject2.put("DESTINATION", "VISA_DATA_SERVICE");
            addlEventLogObject2.put("ACTION", "UPDATE VIRTUAL ACC STATE");

            databaseSteps.eventLogArrayObject.add(addlEventLogObject2);
        }
    }

    public void serviceneededExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE SERVICES NEEDED");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE SERVICES NEEDED");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", postRequestObject);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Then("^I verify the profile id as expected")
    public void verifyVirtualAccountProfileID() throws Exception {
        String query = null;
        if (VTSStep.virtualAccount != null) {
            query = "select * from token_info where virtual_account_id = '" + VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        }else if (MDESStep.virtualAccount != null){
            query = "select * from token_info where virtual_account_id = '" + MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        }else {
            query = "select * from token_info where virtual_account_id = '" + virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        }
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        tokenInfo = new TokenInfo(databaseSteps.result);
        Assert.assertEquals("Profile id is not matched unexpectedly", putRequestObject.get("profileID"), tokenInfo.CARD_METADATA_PROFILE_ID);
    }

    @Then("^I verify the virtual account state as expected")
    public void verifyVirtualAccountState() throws Exception {
        String query = null;

        if (VTSStep.virtualAccount != null) {
            query = "select * from virtual_account where virtual_account_id = '" + VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        } else if(MDESStep.virtualAccount != null) {
            query = "select * from virtual_account where virtual_account_id = '" + MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        }else {
            query = "select * from virtual_account where virtual_account_id = '" + virtualAccount.VIRTUAL_ACCOUNT_ID + "'";
        }

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        String expectedAccountState = putRequestObject.get("accountState").toString();
        if (expectedAccountState.contains("SUSPEND"))
            expectedAccountState = "SUSPENDED";
        else if (expectedAccountState.contains("RESUME"))
            expectedAccountState = "ACTIVE";
        else if (expectedAccountState.contains("INACTIVE"))
            expectedAccountState = "INACTIVE";
        else if (expectedAccountState.contains("ACTIVATE"))
            expectedAccountState = "ACTIVE";
        else if (expectedAccountState.contains("DELETE"))
            expectedAccountState = "DELETED";

    //    Assert.assertEquals("Virtual account status is not matched unexpectedly", expectedAccountState, virtualAccount.STATUS);
    }

    @Then("^I verify the account state as expected")
    public void verifyAccountState() throws Exception {
        String query = null;
        if (VTSStep.accountInfo != null) {
            query = "select * from account_info where account = '" + VTSStep.accountInfo.ACCOUNT + "' and issuer_id = '" + issuer.ISSUER_ID + "'";
        }else if (MDESStep.accountInfo != null) {
            query = "select * from account_info where account = '" + MDESStep.accountInfo.ACCOUNT + "' and issuer_id = '" + issuer.ISSUER_ID + "'";
        }else {
            query = "select * from account_info where account = '" + accountInfo.ACCOUNT + "'";
        }
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        Assert.assertTrue("Account is not found in the table unexpectedly", DBConnection.recordCount() > 0);
        accountInfo = new AccountInfo(databaseSteps.result);

        String expectedAccountState = putRequestObject.get("accountState").toString();
        if (expectedAccountState.contains("SUSPEND"))
            expectedAccountState = "SUSPENDED";
        else if (expectedAccountState.contains("RESUME"))
            expectedAccountState = "ACTIVE";
        else if (expectedAccountState.contains("DELETE"))
            expectedAccountState = "DELETED";

        Assert.assertEquals("Account state is not matched unexpectedly", expectedAccountState, accountInfo.ACCOUNT_STATE);
    }

    @Then("^I verify the virtual account information as expected$")
    public void verifyVirtualAccountInfo() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        if(VTSStep.issuer != null) {
            Assert.assertEquals("Virtual account id '" + postResponseObject.get("virtualAccountID") + "' is not matched unexpectedly", VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID"));
            String account;
            account = VTSStep.accountInfo.ACCOUNT.substring(0, 4) + StringUtils.repeat("X", VTSStep.accountInfo.ACCOUNT.length() - 8) + VTSStep.accountInfo.ACCOUNT.substring(VTSStep.accountInfo.ACCOUNT.length() - 4);
            Assert.assertEquals("Pan number '" + postResponseObject.get("pan") + "' is not matched unexpectedly for the given virtual account id '" + VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'", account, postResponseObject.get("pan"));
            //Assert.assertEquals("Pan source '" + postResponseObject.get("panSource") + "' is not matched unexpectedly for the given virtual account id '" + VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'", VTSStep.virtualAccount.PAN_SOURCE, postResponseObject.get("panSource"));
        }else if(MDESStep.issuer != null){
            Assert.assertEquals("Virtual account id '" + postResponseObject.get("virtualAccountID") + "' is not matched unexpectedly", MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID, postResponseObject.get("virtualAccountID"));
            String account;
            account = MDESStep.accountInfo.ACCOUNT.substring(0, 4) + StringUtils.repeat("X", MDESStep.accountInfo.ACCOUNT.length() - 8) + MDESStep.accountInfo.ACCOUNT.substring(MDESStep.accountInfo.ACCOUNT.length() - 4);
            Assert.assertEquals("Pan number '" + postResponseObject.get("pan") + "' is not matched unexpectedly for the given virtual account id '" + MDESStep.virtualAccount.VIRTUAL_ACCOUNT_ID + "'", account, postResponseObject.get("pan"));

        }
    }

    @Then("^I verify the device binding info list on external log table$")
    public void verifyDeviceBindingInfo() throws Exception {
        String query = "select * from external_log where X_REQUEST_ID = '" + DatabaseSteps.headersAsMap.get("X-Request-ID") + "' and API_CALL = 'TOKEN_INQUIRY_STATE'";

        databaseSteps.iEstablishConnectionToCLSDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        String deviceBindingInfoList = databaseSteps.result.next() ? databaseSteps.result.getString("EXTERNAL_RESPONSE_PAYLOAD") : null;

        databaseSteps.iEstablishConnectionToLCMDatabase();
        assert deviceBindingInfoList != null;
        deviceBindingInfoList = deviceBindingInfoList.replace("\\", "");
        JSONObject extResPayload = JSONHelper.parseJSONObject(deviceBindingInfoList);
        JSONArray deviceBindingInfoArray = JSONHelper.parseJSONArray(extResPayload.get("deviceBindingInfoList").toString());

        for (Object o : deviceBindingInfoArray) {
            JSONObject deviceInfoObj = (JSONObject) o;
            String vaid = virtualAccount != null ? virtualAccount.VIRTUAL_ACCOUNT_ID : VTSStep.virtualAccount.VIRTUAL_ACCOUNT_ID;
            query = "select * from device_info where virtual_account_id = '" + vaid + "' and card_scheme_device_id = '" + deviceInfoObj.get("deviceId") + "'";

            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            deviceInfo = new DeviceInfo(databaseSteps.result);

            Assert.assertEquals("Device name '" + deviceInfoObj.get("deviceName") + "' is not matched unexpectedly for the given virtual account id '" + vaid + "'", deviceInfoObj.get("deviceName"), deviceInfo.DEVICE_NAME);
            Assert.assertEquals("Device index '" + deviceInfoObj.get("deviceIndex") + "' is not matched unexpectedly for the given virtual account id '" + vaid + "'", deviceInfoObj.get("deviceIndex").toString(), deviceInfo.DEVICE_INDEX);
        }
    }

    @Then("^I verify that get merchant list entries are created to event and external logs of Common logging service$")
    public void verifyGetMerchantListEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            getMerchantEventLog();
            getMerchantExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that enrol to click2pay entries are created to event and external logs of Common logging service$")
    public void verifyEnrolClick2PayEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            enrolClick2PayEventLog();
            enrolClick2PayExternalLog();

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that retrieve virtual account info entries are created to event and external logs of common logging service as expected with realtime data as \"([^\"]*)\"$")
    public void verifyGetVirtualAccountInfoInEventExternalLogs(String realTime) throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            getVirtualAccountInfoEventLog(realTime);
            getVirtualAccountInfoExternalLog(realTime);

            databaseSteps.verifyCommonLoggingService();
        }
    }

    @Then("^I verify that retrieve virtual account info entries are created to event log of Common logging service with realtime data as \"([^\"]*)\"$")
    public void verifyGetVirtualAccountInfoEntriesInCLEventLog(String realTime) throws Exception {
        issuerEventLog("eventlog_table.json");
        getVirtualAccountInfoEventLog(realTime);

        databaseSteps.verifyCommonLoggingServiceEventLog();
    }

    @Then("^I verify that retrieve virtual account info entries are created to external log$")
    public void verifyGetVirtualAccountInfoEntriesInExternalLog(String realTime) throws Exception {
        issuerExternalLog("externallog_table.json");
        getVirtualAccountInfoExternalLog(realTime);

        databaseSteps.verifyExternalLog();
    }

    @Then("^I verify that retrieve virtual account info entries are created to external log of Common logging service with realtime data as \"([^\"]*)\"$")
    public void verifyGetVirtualAccountInfoEntriesInCLExternalLog(String realTime) throws Exception {
        issuerExternalLog("externallog_table.json");
        getVirtualAccountInfoExternalLog(realTime);

        databaseSteps.verifyCommonLoggingServiceExternalLog();
    }

    @And("^I have the Issuer event log request body as defined in \"([^\"]*)\"$")
    public void issuerEventLog(String requestBodyPath) throws ParseException, IOException {

        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", DatabaseSteps.headersAsMap.get("X-Issuer-ID"));
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty())
            eventLogObject.put("STATUS", "SUCCESS");
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "LCM_SERVICE");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "VISA_DATA_SERVICE");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("400")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        }
    }

    @And("^I have the Get merchant list event log request body as defined in \"([^\"]*)\"$")
    public void getMerchantEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "GET ELIGIBLE MERCHANTS");

        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    @And("^I have the enrol click2pay event log request body as defined in \"([^\"]*)\"$")
    public void enrolClick2PayEventLog() {
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "Enrol card to Click2Pay");

        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    @And("^I have the get virtual account info event log request body as defined in \"([^\"]*)\"$")
    public void getVirtualAccountInfoEventLog(String realTime) {
        eventLogObject.put("DESTINATION", "VISA TOKEN DATA SERVICE");
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200"))
                eventLogObject.put("ACTION", "GET_VIRTUAL_ACCOUNT_INFO");
            else
                eventLogObject.put("ACTION", "GET VIRTUAL ACCOUNT INFO");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("40")) {
            eventLogObject.put("SOURCE", "VISA TOKEN DATA SERVICE");
            eventLogObject.put("DESTINATION", "VISA_API");
            eventLogObject.put("STATUS", "FAILED");
        }

        databaseSteps.eventLogArrayObject.add(eventLogObject);

        addlEventLogObject.putAll(eventLogObject);
        addlEventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        addlEventLogObject.put("DESTINATION", "LCM_SERVICES");
        addlEventLogObject.put("ACTION", "GET VIRTUAL ACCOUNT INFO");

        databaseSteps.eventLogArrayObject.add(addlEventLogObject);

        if (realTime.equalsIgnoreCase("true")) {
            addlEventLogObject2.putAll(addlEventLogObject);
            addlEventLogObject2.put("SOURCE", "VISA TOKEN DATA SERVICE");
            addlEventLogObject2.put("DESTINATION", "VISA_API");
            addlEventLogObject2.put("ACTION", "TOKEN_INQUIRY_STATE");

            databaseSteps.eventLogArrayObject.add(addlEventLogObject2);
        }
    }

    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    public void iHaveTheIssuerExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }

    @And("^I have the Issuer external log request body as defined in \"([^\"]*)\"$")
    public void issuerExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheIssuerExternalLogRequestBodyAsDefinedIn(requestBodyPath);

        // Service level log
        String Response = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty())
            externalLogObject.put("STATUS", "SUCCESS");
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty() && Response.isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty() && !Response.isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));

        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", null);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);

        databaseSteps.externalLogArrayObject.add(externalLogObject);

        //Additional external object
        addlexternalLogObject.putAll(externalLogObject);

        databaseSteps.externalLogArrayObject.add(addlexternalLogObject);
    }

    @Then("^I verify that get account id entries are created to event and external logs of common logging service as expected$")
    public void verifyGetAccountIdEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            accountIdEventLog();
            accountIdExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }


    public void accountIdExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET ACCOUNT ID");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET ACCOUNT ID");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @Given("I have the invalid issuer request body {string} for defined {string}")
    public void i_have_the_invalid_issuer_request_body_for_and(String requestBodyPath, String accountValue) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + ".json");
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject.get("accountInfo");
        accountInfo = new AccountInfo();
        Assert.assertNotNull(account);
        account.put("account", accountValue);
    }

    @And("^I retrieve encrypted payload request body as defined in \"([^\"]*)\" for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheGetEncryptedPayloadRequestBodyAsDefinedInForIssuer(String requestBodyPath, String tokenRequesterName, String accountRefType) throws Exception {
        String tokenRequester = tokenRequesterName;
        if (tokenRequester.equalsIgnoreCase("Google Pay"))
            tokenRequester = "_GP";
        else if (tokenRequester.equalsIgnoreCase("Apple Pay"))
            tokenRequester = "_AP";

        String query = null;

        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);
            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("issuerTestCards").toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = JSONHelper.parseJSONObject(testCard.toString()).get("account").toString();
            query = "select * from issuer i, account_info ai\n" +
                    "where lower(i.issuer_name) like lower('%" + issuerName + "%')" +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and Account_type in ('" + accountRefType + "')  and account in ('" + account + "')\n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else {
            query = "select * from issuer i, account_info ai\n" +
                    "where lower(i.issuer_name) like lower('%" + issuerName + "%')" +
                    "and ai.issuer_id=i.issuer_id\n" +
                    "and Account_type in ('" + accountRefType + "') \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        }

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName, DBConnection.recordCount() > 0);
        accountInfo = new AccountInfo(databaseSteps.result);

        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + requestBodyPath + tokenRequester + ".json");
        Assert.assertNotNull(postRequestObject);

        HashMap<String, String> accountInfoObj = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(accountInfoObj);

        accountInfoObj.put("account", accountInfo.ACCOUNT);
        accountInfoObj.put("accountType", accountInfo.ACCOUNT_TYPE);
        accountInfoObj.put("accountExpiry", accountInfo.ACCOUNT_EXPIRY);
        if (tokenRequester.contains("AP"))
            accountInfoObj.put("reasonCode", "2001");
        else if (tokenRequester.contains("GP"))
            accountInfoObj.put("reasonCode", "2000");

        HashMap<String, String> providerInfoObj = (HashMap<String, String>) postRequestObject.get("providerInfo");
        Assert.assertNotNull(providerInfoObj);

        query = "select * from code_mapping where code = 'TOKEN_REQUESTER_GROUP' and Partner like 'VISA%' and internal_value = '" + tokenRequesterName + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        codeMapping = new CodeMapping(databaseSteps.result);

        providerInfoObj.put("clientWalletProvider", codeMapping.EXTERNAL_VALUE);

        if (tokenRequester.contains("GP")) {
            //deviceInfoObj.put("deviceID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 48));
            providerInfoObj.put("clientDeviceID", CommonUtil.generateString(new Random(), ALPHANUMERIC, 48));
        }
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    public void accountIdEventLog() throws ParseException, IOException {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "GET ACCOUNT ID");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
                eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void accountInfoExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET ACCOUNT");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET ACCOUNT");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);

    }

    public void createAccountInfoExternalLog() throws ParseException, IOException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "CREATE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "CREATE");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);

    }


    @Then("^I verify the account information as expected in response$")
    public void verifyAccountInformationForIssuer() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        String query1 = "select * from issuer where issuer_id = '" + accountInfo.ISSUER_ID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query1);
        issuer = new Issuer(databaseSteps.result);

        JSONObject accountInfoResponse = (JSONObject) postResponseObject.get("accountInfo");
        Assert.assertNotNull(accountInfo);

        Assert.assertEquals("Issuer id '" + accountInfoResponse.get("issuerId") + "' is not matched unexpectedly", issuer.ISSUER_ID, accountInfoResponse.get("issuerId"));
        Assert.assertEquals("Issuer name '" + accountInfoResponse.get("issuerName") + "' is not matched unexpectedly for the given account id'", issuer.ISSUER_NAME, accountInfoResponse.get("issuerName"));
        Assert.assertEquals("Account type '" + accountInfoResponse.get("accountType") + "' is not matched unexpectedly for the given account id '", accountInfo.ACCOUNT_TYPE, accountInfoResponse.get("accountType"));
        Assert.assertEquals("Account expiry '" + accountInfoResponse.get("accountExpiry") + "' is not matched unexpectedly", accountInfo.ACCOUNT_EXPIRY, accountInfoResponse.get("accountExpiry"));

        JSONObject services = (JSONObject) accountInfoResponse.get("servicesNeeded");
        Assert.assertNotNull(services);

        String query3 = "select * from account_LCM_service where account_id = '" + accountInfo.ACCOUNT_ID + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query3);
        AccountLCMService accountLcmService = new AccountLCMService(databaseSteps.result);

        JSONArray servicesArray = (JSONArray) services.get("service");
        Assert.assertTrue("Services is not as expected in response payload", servicesArray.contains(accountLcmService.LCM_SERVICE));

        JSONObject idvMethods = (JSONObject) postResponseObject.get("idvMethods");
        Assert.assertNotNull(idvMethods);

        JSONArray idvMethodsArray = (JSONArray) idvMethods.get("idvMethod");
        for (Object o : idvMethodsArray) {
            JSONObject idvMethodObj = (JSONObject) o;
            if (idvMethodObj.get("idvChannel").toString().contains("SMS")) {
                String query = "select * from idv_method where account_id = '" + accountInfo.ACCOUNT_ID + "' and channel = 'SMS'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertEquals("Contact info is not as expected for sms in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
            } else if (idvMethodObj.get("idvChannel").toString().contains("EMAIL")) {
                String query = "select * from idv_method where account_id = '" + accountInfo.ACCOUNT_ID + "' and channel = 'EMAIL'";
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                idvMethod = new IDVMethod(databaseSteps.result);

                Assert.assertEquals("Contact info is not as expected for email in cvm response payload", idvMethod.CONTACT_INFO, idvMethodObj.get("idvContactInfo"));
            } else if (idvMethodObj.get("idvChannel").toString().contains("APP_TO_APP")) {
                String query = "select * from issuer_idv_config where issuer_id = '" + issuer.ISSUER_ID;
                databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
                issuerIDVConfig = new IssuerIDVConfig(databaseSteps.result);
                Assert.assertEquals("App2App Identifier is not as expected in cvm response payload", issuerIDVConfig.APP_APP_IDENTIFIER, idvMethodObj.get("idvContactInfo"));
            } else if (idvMethodObj.get("idvChannel").toString().contains("CUSTOMERCARE") || idvMethodObj.get("idvChannel").toString().contains("CUSTOMERSERVICE")) {
                if (issuerIDVConfig.CC_IDV_ENABLED.equalsIgnoreCase("y") && issuerIDVConfig.CC_IDV_DEFAULT_VALUE.equalsIgnoreCase("y"))
                    Assert.assertEquals("Customer care identifier is not as expected in cvm response payload", issuerIDVConfig.CC_IDV_DEFAULT_VALUE, idvMethodObj.get("idvContactInfo"));
            } else
                Assert.fail("Unexpected IDV method present for the given account " + idvMethodObj.get("idvChannel").toString());
        }

        Assert.assertEquals("Account state is not matched unexpectedly", accountInfo.ACCOUNT_STATE, postResponseObject.get("accountState"));
        JSONObject virtualAccounts = (JSONObject) postResponseObject.get("virtualAccounts");
        String query2 = "select virtual_account_id from token_info\n" +
                "where virtual_account_id in (select virtual_account_id from virtual_account where account_id = '" + accountInfo.ACCOUNT_ID + "'" +
                ") and token is not null";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query2);
        VirtualAccount.fetchVirtualAccounts(databaseSteps.result);
        if (virtualAccounts != null) {
            JSONArray vaIdArray = (JSONArray) virtualAccounts.get("virtualAccountID");
            Assert.assertTrue("Virtual account id  is not present in the virtual account list unexpectedly", vaIdArray.containsAll(VirtualAccount.VIRTUALACCOUNTS));
        } else {
            Assert.assertTrue(VirtualAccount.VIRTUALACCOUNTS.isEmpty());
        }
    }


    @Then("^I verify the account information as expected in response for create or register for \"([^\"]*)\"$")
    public void verifyAccountInformationForIssuerRegisterorCreate(String accountType) throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        String query1 = "select * from account_info where account='" + account + "'";
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query1);
        accountInfo = new AccountInfo(databaseSteps.result);
        Assert.assertNotNull(accountInfo);
        Assert.assertEquals("Account '" + account + " is matched successfully", account, accountInfo.ACCOUNT);
        Assert.assertEquals("Account Expiry '" + accountExpiry + " is matched successfully", accountExpiry, accountInfo.ACCOUNT_EXPIRY.toString());
        Assert.assertEquals("Account type '" + accountType + " is matched successfully", accountType, accountInfo.ACCOUNT_TYPE);
        String accountIDResponse = String.valueOf(postResponseObject.get("accountID"));
        String accountStatus = String.valueOf(postResponseObject.get("accountState"));
        String accntState = accountInfo.ACCOUNT_STATE;
        Assert.assertEquals("Account ID '" + accountIDResponse + " is matched successfully", accountIDResponse, accountInfo.ACCOUNT_ID);
        Assert.assertEquals("Account status '" + accountStatus + " is matched successfully", accountStatus, accountInfo.ACCOUNT_STATE);
    }

    public void getMerchantExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET ELIGIBLE MERCHANTS");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET ELIGIBLE MERCHANTS");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", externalLogObject.get("ENDPOINT") + "getEligibleMerchants");

        String nhppEndpoint = apiProperties.getProperty("NHPP_DATA");
        nhppEndpoint = nhppEndpoint.replace("${VISA_DATA}", apiProperties.getProperty("VISA_DATA"));

        addlexternalLogObject.put("ENDPOINT", nhppEndpoint);
        addlexternalLogObject.put("API_CALL", "NHPP PROFILE");
        addlexternalLogObject.put("ACTION", "NHPP PROFILE");
        addlexternalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        addlexternalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
    }

    public void enrolClick2PayExternalLog() {
        String serviceEndpoint = apiProperties.getProperty("ISSUER_SERVICE_DATA");
        serviceEndpoint = serviceEndpoint.replace("${ISSUER_DATA}", apiProperties.getProperty("ISSUER_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());

        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "Enrol card to Click2Pay");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "Enrol card to Click2Pay");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", externalLogObject.get("ENDPOINT") + "accounts");

        String pushCardEndpoint = apiProperties.getProperty("PUSHCARD_DATA");
        pushCardEndpoint = pushCardEndpoint.replace("${VISA_DATA}", apiProperties.getProperty("VISA_DATA"));

        addlexternalLogObject.put("ENDPOINT", pushCardEndpoint);
        addlexternalLogObject.put("API_CALL", "PUSH CARD TO WALLET");
        addlexternalLogObject.put("ACTION", "PUSH CARD TO WALLET");
        addlexternalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        addlexternalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);
    }


    public void getVirtualAccountInfoExternalLog(String realTime) {
        String serviceEndpoint = apiProperties.getProperty("ISSUER_VIRTUAL_ACCOUNT_DATA_DUMMY");
        serviceEndpoint = serviceEndpoint.replace("${ISSUER_DATA}", apiProperties.getProperty("ISSUER_DATA"));
        serviceEndpoint = serviceEndpoint.replace("${ENVIRONMENT}", environment.toLowerCase());
        serviceEndpoint = serviceEndpoint.replace("${accountID}", virtualAccount.ACCOUNT_ID);
        serviceEndpoint = serviceEndpoint.replace("${virtualAccountID}", virtualAccount.VIRTUAL_ACCOUNT_ID);

        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", serviceEndpoint);
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET VIRTUAL ACCOUNT INFO");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET VIRTUAL ACCOUNT INFO");
        if (postResponseObject.get("pan") != null) {
            account = account.replace("XXXXXXXX", "XXxxxxxx");
            postResponseObject.put("pan", account);
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", postResponseObject);
        }

        if (postResponseObject.get("tokenPAN") != null) {
            String tokenPAN = postResponseObject.get("tokenPAN").toString();

            tokenPAN = tokenPAN.substring(0, 4) + StringUtils.repeat("X", 2) + StringUtils.repeat("x", tokenPAN.length() - 10) + tokenPAN.substring(tokenPAN.length() - 4);
            postResponseObject.put("tokenPAN", tokenPAN);
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", postResponseObject);
        }

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).startsWith("40"))
            externalLogObject.put("STATUS", "FAILED");

        String tokenInquiryEndpoint = apiProperties.getProperty("TOKEN_INQUIRY_DATA");
        tokenInquiryEndpoint = tokenInquiryEndpoint.replace("${VISA_T_DATA}", apiProperties.getProperty("VISA_T_DATA"));

        addlexternalLogObject.put("ENDPOINT", tokenInquiryEndpoint);

        if (realTime.equalsIgnoreCase("true")) {
            addlexternalLogObject.put("API_CALL", "TOKEN_INQUIRY_STATE");
            addlexternalLogObject.put("ACTION", "TOKEN_INQUIRY_STATE");
            addlexternalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
            addlexternalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", null);

            databaseSteps.externalLogArrayObject.add(addlexternalLogObject);
        }
    }

    @When("I get wallet id for provided issuer")
    public String iGetWalletIdForProvidedIssuer() throws Exception {
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String queryParamPath = "Issuer/request" + authHeader;
            iHaveTheIssuerQueryParamsAsDefinedIn(queryParamPath);
            for (var entry : queryParamsAsMap.entrySet()) {
                if (entry.getKey().equals("clientWalletAccountID")) {
                    walletId = entry.getValue();
                }
            }
        } else {
            iFetchAccountInfo();
            walletId = virtualAccount.CLIENT_WALLET_ID;
            Assert.assertNotNull(walletId);
        }
        return walletId;
    }

    @And("^I create the encrypted data for TPAN")
    public void createEncryptedDataForTPAN() throws Exception {
        endpoint = apiProperties.getProperty("ENCRYPTION_LI_DATA");
        endpoint = endpoint.replace("${ENCRYPTION_DECRYPTION_DATA}", apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA"));
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String bodyPath = "Encryption/valid/body/valid_request_body_IA.json";

        iHaveTheDefaultEncryptionHeadersAsDefinedIn(headerPath);
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + bodyPath);
        Assert.assertNotNull(postRequestObject);
        iFetchAccountInfo();
        String query = "select * from token_info where token is not null\n" +
                "and virtual_account_id in (select virtual_account_id from virtual_account\n" +
                "where account_id='" + accountInfo.ACCOUNT_ID + "') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        Assert.assertTrue("There is no records found in token info table", DBConnection.recordCount() > 0);
        tokenInfo = new TokenInfo(databaseSteps.result);
        postRequestObject.put("account", tokenInfo.TOKEN);
        postRequestObject.put("accountExpiry", accountInfo.ACCOUNT_EXPIRY);
        encryptionRequestBody.putAll(postRequestObject);
        restAssuredAPI.post(encryptionRequestBody, headersAsMap, endpoint);
        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = restAssuredAPI.globalResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("I fetch account info to be updated")
    public void iFetchAccountInfo() throws Exception {
        String query;
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" +issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("issuerTestCards").toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());
            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            query = "select * from issuer i, account_info ai,virtual_account va \n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id and ai.account_id=va.account_id " +
                    " and account in ('" + account + "') " +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else {
            query = "select * from issuer i, account_info ai , virtual_account va \n" +
                    "where lower(issuer_name) like lower('%" + issuerName + "%') " +
                    "and ai.issuer_id=i.issuer_id and ai.account_id=va.account_id  and va.client_wallet_id is not null " +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        }
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no records found in account info table matching the issuer " + issuerName, DBConnection.recordCount() > 0);
        accountInfo = new AccountInfo(databaseSteps.result);
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
        query = "select * from virtual_account where account_id ='" + accountInfo.ACCOUNT_ID + "'" +
                " and client_wallet_id is not null ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        Assert.assertTrue("There is no records found in virtual info table matching the issuer " + issuerName, DBConnection.recordCount() > 0);
        virtualAccount = new VirtualAccount(databaseSteps.result);
        IssuerStep.log.info(virtualAccount.VIRTUAL_ACCOUNT_ID + " - " + virtualAccount.ACCOUNT_ID);
    }

    @And("^I have the scheme token request body as defined in for issuer \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iHaveTheSchemeTokenRequestBodyAsDefinedInForIssuer(String accountType, String validRequestBodies) throws Exception {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + validRequestBodies + ".json");
        Assert.assertNotNull(postRequestObject);
        HashMap<String, String> accountJson = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(accountJson);
        String query;
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("issuerTestCards").toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());

            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            query = " select * from account_info a, virtual_account v ,token_info t\n" +
                    " where a.account_id = v.account_id\n" +
                    "  and v.virtual_account_id=t.virtual_account_id\n" +
                    " and t.token is not null\n" +
                    " and  a.Account in ('" + account + "') \n" +
                    " and a.account_state='ACTIVE' " +
                    " and a.issuer_id in (select issuer_id from issuer\n" +
                    " where lower(issuer_name) like lower('%" + issuerName + "%'))";

        } else {

            query = " select * from account_info a, virtual_account v ,token_info t\n" +
                    " where a.account_id = v.account_id\n" +
                    "  and v.virtual_account_id=t.virtual_account_id\n" +
                    " and t.token is not null\n" +
                    " and  a.Account_ref_type in ('" + accountType + "') \n" +
                    " and a.account_state='ACTIVE' " +
                    " and a.issuer_id in (select issuer_id from issuer\n" +
                    " where lower(issuer_name) like lower('%" + issuerName + "%'))" +
                    " ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        }
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no account present with tokens with provided testcard/in DB " + issuerName, DBConnection.recordCount() > 0);
        accountInfo = new AccountInfo(databaseSteps.result);

        if (accountType.equalsIgnoreCase("cardid"))
            accountJson.put("account", accountInfo.ACCOUNT_REF);
        else if (accountType.equalsIgnoreCase("panid"))
            accountJson.put("account", accountInfo.ACCOUNT_ID);
        else if (accountType.equalsIgnoreCase("lcmid"))
            accountJson.put("account", accountInfo.ACCOUNT_ID);
        accountJson.put("accountType", accountType);
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @When("I fetch tokens with invalid WalletId {string}")
    public void iFetchTokensWithInvalidWalletId(String invalidWalletId) {
        walletId = invalidWalletId;
    }

    @Then("I verify token details as expected for issuer")
    public void iVerifyTokenDetailsAsExpectedForIssuer() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        postResponseArrayObject = (JSONArray) postResponseObject.get("tokenInfoList");
        Assert.assertTrue("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject.size() > 0);
        Assert.assertEquals("No. of tokens retrieved is misleading in get token response", Integer.parseInt(postResponseObject.get("noOfTokens").toString()), postResponseArrayObject.size());

        String query = "select count(*) from account_info a join virtual_account v on a.account_id = v.account_id join token_info t on \n" +
                "v.virtual_account_id=t.virtual_account_id\n" +
                "where v.client_wallet_id='" + walletId + "'\n" +
                "and t.token_status in ('ACTIVE','INACTIVE','SUSPENDED') and token is not null " +
                "and a.issuer_id in (select issuer_id from issuer" +
                " where lower(issuer_name) like lower('%" + issuerName + "%')) ";
        int noOfTokensinDB = 0;
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        if (databaseSteps.result.next()) {
            noOfTokensinDB = databaseSteps.result.getInt(1);
        }
        Assert.assertEquals(noOfTokensinDB, Integer.parseInt(postResponseObject.get("noOfTokens").toString()));
    }

    @And("I create the encrypted data for {string} TPAN")
    public void iCreateTheEncryptedDataForTPAN(String invalidTPAN) throws Exception {
        endpoint = apiProperties.getProperty("ENCRYPTION_LI_DATA");
        endpoint = endpoint.replace("${ENCRYPTION_DECRYPTION_DATA}", apiProperties.getProperty("ENCRYPTION_DECRYPTION_DATA"));
        String defaultHeader = "/valid/headers/defaultHeader.csv";
        String headerPath = "Encryption" + defaultHeader;
        String bodyPath = "Encryption/valid/body/valid_request_body_IA.json";

        iHaveTheDefaultEncryptionHeadersAsDefinedIn(headerPath);
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + bodyPath);
        Assert.assertNotNull(postRequestObject);

        postRequestObject.put("account", invalidTPAN);
        postRequestObject.put("accountExpiry", 0);

        encryptionRequestBody.putAll(postRequestObject);
        restAssuredAPI.post(encryptionRequestBody, headersAsMap, endpoint);
        RESTAssSteps.iVerifyTheStatusCode(200);

        try {
            encrypted_data = restAssuredAPI.globalResponse.getBody().asString();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @And("I fetch virtual accountId {string} and accountId as {string} to update profileId")
    public void iFetchVirtualAccountIdAndAccountIdAsToUpdateProfileId(String vaState, String state) throws Exception {
        String query, stateTestCard = null;
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + cardScheme + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are present for issuer", testCardSets);

            if (state.equalsIgnoreCase("ACTIVE")) {
                stateTestCard = "issuerTestCards";
            } else if (state.equalsIgnoreCase("DELETED")) {
                stateTestCard = "deletedTestCard";
            } else if (state.equalsIgnoreCase("SUSPENDED")) {
                stateTestCard = "suspendedTestCard";
            }

            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get(stateTestCard).toString());
            Assert.assertTrue("No test cards are present for issuer", testCards != null && !testCards.isEmpty());
            Random random = new Random();
            JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
            account = testCard.get("account").toString();

            query = "select * from virtual_account where status='" + vaState + "' \n" +
                    "and account_id in (select account_id from account_info where account_state='" + state + "' \n" +
                    "and account='" + account + "' and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))) " +
                    " and virtual_account_id in (select virtual_account_id from token_info ) \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else {
            query = "select * from virtual_account where status='" + vaState + "' \n" +
                    "and account_id in (select account_id from account_info where account_state='" + state + "' \n" +
                    "and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "')))" +
                    " and virtual_account_id in (select virtual_account_id from token_info ) \n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        }
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no virtualAccount/Token matching the issuer " + issuerName + " and state " + vaState + " for provided Testcard/in DB", DBConnection.recordCount() > 0);
        virtualAccount = new VirtualAccount(databaseSteps.result);

        accountInfo = new AccountInfo();
        accountInfo.ACCOUNT_ID = virtualAccount.ACCOUNT_ID;
        IssuerStep.log.info(virtualAccount.VIRTUAL_ACCOUNT_ID + " - " + virtualAccount.ACCOUNT_ID);
    }

    @When("I update the profile id based on account id {string} and virtual account id {string}")
    public void iUpdateTheProfileIdBasedOnAccountIdAndVirtualAccountId(String invalidAccount, String invalidVAccount) {
        endpoint = apiProperties.getProperty("ISSUER_UPDATE_PROFILE_VIRTUAL_ACCOUNT_DATA");
        endpoint = endpoint.replace("${ISSUER_VIRTUAL_ACCOUNT_DATA}", apiProperties.getProperty("ISSUER_VIRTUAL_ACCOUNT_DATA"));
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));
        endpoint = endpoint.replace("${accountID}", invalidAccount);
        endpoint = endpoint.replace("${virtualAccountID}", invalidVAccount);
        accountInfo = new AccountInfo();
        accountInfo.ACCOUNT_ID = invalidAccount;
        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-" + environment.toLowerCase());
        else if (environment.equalsIgnoreCase("test"))
            endpoint = endpoint.replace("pp", environment.toLowerCase());

        DatabaseSteps.headersAsMap.put("X-Request-ID", CommonUtil.generateUUID());
        restAssuredAPI.put(putRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }

    @And("I fetch account info to update state as {string}")
    public void iFetchAccountInfoToUpdateStateAs(String state) throws Exception {
        String query = null;
        if (state.equalsIgnoreCase("RESUME")) {
            state = "SUSPENDED";
        } else if (state.equalsIgnoreCase("SUSPEND")) {
            state = "ACTIVE";
        } else {
            state = "invalid";
        }
        if (state.equalsIgnoreCase("invalid")) {
            query = "select * from account_info " +
                    "where issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))\n" +
                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else {
//            query = "select * from account_info where account_state='" + state + "' \n" +
//                    "and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))\n" +
//                    "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

            query = "SELECT a.account_id,COUNT(va.virtual_account_id) FROM virtual_account va,account_info a\n" +
                    "where a.account_id=va.account_id and a.account_state='" + state + "' \n" +
                    "and issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))\n" +
                    "GROUP BY (a.account_id)\n" +
                    "HAVING COUNT(va.virtual_account_id)<20 ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        }
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        Assert.assertTrue("There is no records found in Account table matching the issuer " + issuerName + "and state " + state, DBConnection.recordCount() > 0);
        if (databaseSteps.result.next()) {
            String accountId = databaseSteps.result.getString("ACCOUNT_ID");
            query = "select * from account_info where account_id='" + accountId + "'";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            accountInfo = new AccountInfo(databaseSteps.result);
        } else {
            Assert.assertTrue("There is no records found in Account table matching the issuer " + issuerName + "and state " + state, DBConnection.recordCount() > 0);
        }
        IssuerStep.log.info(accountInfo.ACCOUNT + " - " + accountInfo.ACCOUNT_ID);
    }

    @And("I fetch virtual account info to update state as {string}")
    public void iFetchVirtualAccountInfoToUpdateStateAs(String state) throws Exception {
        String query, stateTestCard = null;
        if (state.equalsIgnoreCase("RESUME")) {
            state = "SUSPENDED";
        } else if (state.equalsIgnoreCase("SUSPEND")) {
            state = "ACTIVE";
        }
        query = "select * from virtual_account where status='" + state + "' \n" +
                "and account_id in (select account_id from account_info " +
                "where issuer_id in (select issuer_id from issuer where lower(issuer_name) like lower('" + issuerName + "'))) \n" +
                "ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        Assert.assertTrue("There is no records found in Virtual Account table matching the issuer " + issuerName + "and state " + state, DBConnection.recordCount() > 0);
        virtualAccount = new VirtualAccount(databaseSteps.result);
        IssuerStep.log.info(virtualAccount.VIRTUAL_ACCOUNT_ID + " - " + virtualAccount.ACCOUNT_ID);
    }

    @And("I again RESUME the accountState to make it ACTIVE")
    public void iAgainRESUMETheAccountStateToMakeItACTIVE() throws Exception {
        iHaveTheUpdateAccountStateRequestBodyAsDefinedIn("valid/body/valid_request_body_AS", "RESUME");
        iUpdateAccountState();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyAccountState();
    }

    @And("I again RESUME the virtual accountState to make it ACTIVE")
    public void iAgainRESUMETheVirtualAccountStateToMakeItACTIVE() throws Exception {
        iHaveTheUpdateVirtualAccountStateRequestBodyAsDefinedIn("valid/body/valid_request_body_VAS", "RESUME");
        iUpdateVirtualAccountState();
        RESTAssSteps.iVerifyTheStatusCode(200);
        verifyVirtualAccountState();
    }

    @And("I verify scheme token details as expected for issuer")
    public void iVerifySchemeTokenDetailsAsExpectedForIssuer() throws Exception {
        RESTAssSteps.iVerifyResponseForValidJson();
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        postResponseArrayObject = (JSONArray) postResponseObject.get("tokenInfo");
        Assert.assertTrue("Unexpected response has been generated and the response is " + postResponseObject, postResponseArrayObject.size() > 0);
        Assert.assertEquals("No. of tokens retrieved is misleading in get token response", Integer.parseInt(postResponseObject.get("noOfTokens").toString()), postResponseArrayObject.size());

        String query = "select count(*) from token_info t JOIN \n" +
                "virtual_account v on t.virtual_account_id = v.virtual_account_id and t.token_status IN ( 'ACTIVE', 'SUSPENDED', 'INACTIVE' )\n" +
                "and t.token_type = 'SECURE_ELEMENT' and v.account_id = '" + accountInfo.ACCOUNT_ID + "'";

        int noOfTokensinDB = 0;
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        if (databaseSteps.result.next()) {
            noOfTokensinDB = databaseSteps.result.getInt(1);
        }
        Assert.assertEquals(noOfTokensinDB, Integer.parseInt(postResponseObject.get("noOfTokens").toString()));
    }

    @And("I have the scheme token invalid {string} request body as defined in for issuer {string} and {string}")
    public void iHaveTheSchemeTokenInvalidRequestBodyAsDefinedInForIssuerAnd(String invalidAccount, String accountType, String validRequestBodies) throws IOException, ParseException {
        postRequestObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + IssuerRequestFilePath + validRequestBodies + ".json");
        Assert.assertNotNull(postRequestObject);
        HashMap<String, String> accountJson = (HashMap<String, String>) postRequestObject.get("accountInfo");
        Assert.assertNotNull(accountJson);

        accountJson.put("account", invalidAccount);
        accountJson.put("accountType", accountType);
        accountInfo = new AccountInfo();
        accountInfo.ACCOUNT_ID = invalidAccount;

    }

    @And("I verify that get scheme token entries are created to event and external logs of common logging service as expected")
    public void iVerifyThatGetSchemeTokenEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            schemeTokenEventLog();
            schemeTokenExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }

    }

    public void schemeTokenEventLog() {
        eventLogObject.put("SOURCE", "ISSUER DATA SERVICE");
        eventLogObject.put("DESTINATION", "VISA TOKEN DATA SERVICE");
        eventLogObject.put("ACTION", "GET TOKEN DETAILS");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void schemeTokenExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET TOKEN DETAILS");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET TOKEN DETAILS");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("I verify that profile Id update entries are created to event and external logs of common logging service as expected")
    public void iVerifyThatProfileIdUpdateEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            updateProfileEventLog();
            updateProfileExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void updateProfileEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "VIRTUAL_ACCOUNT_PROFILE_UPDATE");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
            eventLogObject.put("ACTION", "UPDATE VIRTUAL ACCOUNT PROFILE ID");
        } else if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("ACCOUNT_ID", virtualAccount.ACCOUNT_ID);

        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void updateProfileExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE VIRTUAL ACCOUNT PROFILE ID");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE VIRTUAL ACC PROFILE ID");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("I verify that get tokens entries are created to event and external logs of common logging service as expected")
    public void iVerifyThatGetTokensEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            accountInfo = new AccountInfo();
            accountInfo.ACCOUNT_ID = null;

            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            getTokensEventLog();
            getTokensExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void getTokensEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "GET TOKENS");
        eventLogObject.put("ACCOUNT_ID", null);

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");

        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void getTokensExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "GET TOKENS");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "GET TOKENS");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("I verify that update account state entries are created to event and external logs of common logging service as expected")
    public void iVerifyThatUpdateAccountStateEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            updateAccountStateEventLog();
            updateAccountStateExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void updateAccountStateEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "UPDATE ACCOUNT STATE ACTION");

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void updateAccountStateExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE ACCOUNT STATE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE ACCOUNT STATE ACTION");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("I verify that virtual account state entries are created to event and external logs of common logging service as expected")
    public void iVerifyThatUpdateVirtualAccountStateEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            accountInfo = new AccountInfo();
            accountInfo.ACCOUNT_ID = null;
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            updateVirtualAccountStateEventLog();
            updateVirtualAccountStateExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }
    }

    public void updateVirtualAccountStateEventLog() {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ACTION", "VIRTUAL_ACCOUNT_STATUS_UPDATE");
        eventLogObject.put("ACCOUNT_ID", virtualAccount.ACCOUNT_ID);

        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");

        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);
    }

    public void updateVirtualAccountStateExternalLog() {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE VIRTUAL ACCOUNT STATE");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE VIRTUAL ACCOUNT STATE");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    @And("I verify that update profile id entries are created to event and external logs of common logging service as expected {string}")
    public void iVerifyThatUpdateProfileIdEntriesAreCreatedToEventAndExternalLogsOfCommonLoggingServiceAsExpected(String virtualAccountFlag) throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            issuerEventLog("eventlog_table.json");
            issuerExternalLog("externallog_table.json");

            updateProfileIDEventLog(virtualAccountFlag);
            updateProfileIDExternalLog();
            databaseSteps.verifyCommonLoggingService();
        }

    }

    private void updateProfileIDExternalLog() throws ParseException {
        if (externalLogObject.get("API_CALL") != null && externalLogObject.get("API_CALL").toString().isEmpty())
            externalLogObject.put("API_CALL", "UPDATE ACCOUNT PROFILE ID");
        if (externalLogObject.get("ACTION") != null && externalLogObject.get("ACTION").toString().isEmpty())
            externalLogObject.put("ACTION", "UPDATE ACCOUNT PROFILE ID");
        if (externalLogObject.get("ENDPOINT") != null && externalLogObject.get("ENDPOINT").toString().isEmpty())
            externalLogObject.put("ENDPOINT", endpoint);
        externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", putRequestObject);

        databaseSteps.externalLogArrayObject.add(externalLogObject);
    }

    private void updateProfileIDEventLog(String virtualAccountFlag) {
        eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
        eventLogObject.put("DESTINATION", "LCM_SERVICES");
        eventLogObject.put("ISSUER_ID", DatabaseSteps.headersAsMap.get("X-Issuer-ID").replace("]", " ]"));
        eventLogObject.put("ACCOUNT_ID", accountInfo.ACCOUNT_ID);
        if (virtualAccountFlag.equalsIgnoreCase("true")) {
            eventLogObject.put("ACTION", "VIRTUAL_ACCOUNT_PROFILE_UPDATE");
        } else {
            eventLogObject.put("ACTION", "ACCOUNT_PROFILE_UPDATE");
        }
        if (!String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("200")) {
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACTION", "UPDATE ACCOUNT PROFILE ID");
        }
        databaseSteps.eventLogArrayObject.add(eventLogObject);

    }
}