package com.lcm.test.steps;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.utilities.JSONHelper;
import com.lcm.core.utilities.LCMProperties;
import com.lcm.core.utilities.RESTAssuredAPI;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.thucydides.core.annotations.Steps;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.AssumptionViolatedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class VTCStep {
    @Steps
    DatabaseSteps databaseSteps;
    private String endpoint;
    private final String commonLogFilePath = "Database/CommonLog/";
    private JSONObject postRequestObject = new JSONObject();
    private JSONObject postResponseObject= new JSONObject();
    private JSONObject externalLogObject = new JSONObject();
    private JSONObject eventLogObject = new JSONObject();
    @Steps
    RESTAssuredAPI restAssuredAPI;
    private final String dataDriveFilePath = "src/main/resources/data/";
    private final LCMProperties apiProperties = new LCMProperties("api.properties");
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();
    private final String issuerName = System.getProperty("issuer") != null ? System.getProperty("issuer") : runner.getProperty("ISSUER");
    private final String testCardsEnabledAtSuiteLevel = System.getProperty("testCardsEnabled") != null ? System.getProperty("testCardsEnabled") : runner.getProperty("TESTCARDSENABLED");
    private boolean testCardsEnabledAtScenarioLevel = true;
    private String issuer_id = null;

    @Before(value = "@VTC_Comdirect", order = 1)
    public void vtcComdirect(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.toString().contains(issuerName.replace(" ", ""))) {
            throw new AssumptionViolatedException("Issuer " + issuerName + " is not supported for CardID. Hence, the scenario is skipped.");
        }
    }

    @Before(value = "@VTC_Sparekassen", order = 1)
    public void vtcSparekassen(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.toString().contains(issuerName.split(" ")[0])) {
            throw new AssumptionViolatedException("Issuer " + issuerName + " is not supported for PanID. Hence, the scenario is skipped.");
        }
    }

    @Before(value = "@VTC_PayAlly", order = 1)
    public void vtcPayAlly(final Scenario scenario) {
        final ArrayList<String> scenarioTags = new ArrayList<>();
        scenarioTags.addAll(scenario.getSourceTagNames());

        if (!scenarioTags.toString().toLowerCase().contains(issuerName.toLowerCase().replace(" ", ""))) {
            throw new AssumptionViolatedException("Issuer " + issuerName + " is not supported for PanRef. Hence, the scenario is skipped.");
        }
    }

    @Given("I have the VTC request body as defined {string}")
    public void i_have_the_vtc_request_body_for_and(String accountType) throws IOException, ParseException {
        if (testCardsEnabledAtSuiteLevel.equalsIgnoreCase("yes") && testCardsEnabledAtScenarioLevel) {
            String testCardsPath = dataDriveFilePath + "TestCards/" + environment + "/" + issuerName.toLowerCase() + ".json";
            JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
            Assert.assertNotNull("No test cards are added for VTC", testCardSets);
            JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("vtcTestCards").toString());
            Assert.assertTrue("No test cards are added for VTC", testCards != null && !testCards.isEmpty());
            for (Object testCard : testCards) {
                String account = JSONHelper.parseJSONObject(testCard.toString()).get("accountType").toString();
                if (account.equalsIgnoreCase(accountType)) {
                    postRequestObject = (JSONObject) testCard;
                }
            }
        }
    }

    @Then("I post the details to VTC API endpoint")
    public void i_post_the_details_to_vtc_service_endpoint() {
        endpoint = apiProperties.getProperty("VTC_ISSUER_DATA");
        endpoint = endpoint.replace("${LCM_ISSUER_DATA}", apiProperties.getProperty("LCM_ISSUER_DATA"));

        if (environment.equalsIgnoreCase("pp"))
            endpoint = endpoint.replace("lcm", "lcm-pp");

        restAssuredAPI.post(postRequestObject, DatabaseSteps.headersAsMap, endpoint);
    }


    @Then("^I verify that vtc entries are created to event and external logs of Common logging service$")
    public void verifyGetMerchantListEntriesInCL() throws Exception {
        issuerEventLog("eventlog_table.json");
        issuerExternalLog("externallog_table.json");
        databaseSteps.eventLogArrayObject.add(eventLogObject);
        databaseSteps.externalLogArrayObject.add(externalLogObject);
        databaseSteps.verifyCommonLoggingService();
    }

    @And("^I have the VTC event log request body as defined in \"([^\"]*)\"$")
    public void issuerEventLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheEventLogRequestBodyAsDefinedIn(requestBodyPath);

        if (eventLogObject.get("X_REQUEST_ID") != null && eventLogObject.get("X_REQUEST_ID").toString().isEmpty())
            eventLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (eventLogObject.get("ISSUER_ID") != null && eventLogObject.get("ISSUER_ID").toString().isEmpty())
            eventLogObject.put("ISSUER_ID", DatabaseSteps.headersAsMap.get("X-Issuer-ID"));
        if (eventLogObject.get("STATUS") != null && eventLogObject.get("STATUS").toString().isEmpty())
            eventLogObject.put("STATUS", "SUCCESS");
        if (eventLogObject.get("ACTION") != null && eventLogObject.get("ACTION").toString().isEmpty())
            eventLogObject.put("ACTION", "Get accountref using account");
        if (eventLogObject.get("ACCOUNT_ID") != null && eventLogObject.get("ACCOUNT_ID").toString().isEmpty())
            eventLogObject.put("ACCOUNT_ID", null);
        if (eventLogObject.get("SOURCE") != null && eventLogObject.get("SOURCE").toString().isEmpty())
            eventLogObject.put("SOURCE", "ISSUER");
        if (eventLogObject.get("DESTINATION") != null && eventLogObject.get("DESTINATION").toString().isEmpty())
            eventLogObject.put("DESTINATION", "LCM_SERVICES");

        if (String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()).equals("400")) {
            eventLogObject.put("SOURCE", "EXTERNAL_ISSUER");
            eventLogObject.put("DESTINATION", "LCM_SERVICES");
            eventLogObject.put("STATUS", "FAILED");
            eventLogObject.put("ACCOUNT_ID", null);
        }
    }

    @And("^I have the VTC external log request body as defined in \"([^\"]*)\"$")
    public void issuerExternalLog(String requestBodyPath) throws ParseException, IOException {
        iHaveTheIssuerExternalLogRequestBodyAsDefinedIn(requestBodyPath);
        if (externalLogObject.get("X_REQUEST_ID") != null && externalLogObject.get("X_REQUEST_ID").toString().isEmpty())
            externalLogObject.put("X_REQUEST_ID", DatabaseSteps.headersAsMap.get("X-Request-ID"));
        if (externalLogObject.get("HTTP_RESPONSE") != null && externalLogObject.get("HTTP_RESPONSE").toString().isEmpty())
            externalLogObject.put("HTTP_RESPONSE", String.valueOf(RESTAssuredAPI.globalStaticResponse.getStatusCode()));
        if (externalLogObject.get("STATUS") != null && externalLogObject.get("STATUS").toString().isEmpty())
            externalLogObject.put("STATUS", "SUCCESS");
        if (externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD") != null && externalLogObject.get("EXTERNAL_REQUEST_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_REQUEST_PAYLOAD", null);
        if (externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD") != null && externalLogObject.get("EXTERNAL_RESPONSE_PAYLOAD").toString().isEmpty())
            externalLogObject.put("EXTERNAL_RESPONSE_PAYLOAD", JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()));
        if (externalLogObject.get("REQUEST_ADDITIONAL_DATA") != null && externalLogObject.get("REQUEST_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("REQUEST_ADDITIONAL_DATA", null);
        if (externalLogObject.get("RESPONSE_ADDITIONAL_DATA") != null && externalLogObject.get("RESPONSE_ADDITIONAL_DATA").toString().isEmpty())
            externalLogObject.put("RESPONSE_ADDITIONAL_DATA", null);

    }

    public void iHaveTheEventLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        eventLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(eventLogObject);
    }

    public void iHaveTheIssuerExternalLogRequestBodyAsDefinedIn(String requestBodyPath) throws ParseException, IOException {
        externalLogObject = JSONHelper.messageAsSimpleJson(dataDriveFilePath + commonLogFilePath + requestBodyPath);
        Assert.assertNotNull(externalLogObject);
    }
    @Given("I have the request body as for which issuer has not opted VTC {string}")
    public void i_have_the_request_body_as_for_which_issuer_not_opted_VTC(String accountType) throws Exception {
        HashMap<String, String> account = (HashMap<String, String>) postRequestObject;
        issuer_id=DatabaseSteps.headersAsMap.get("X-Issuer-ID");
        Assert.assertNotNull(account);
        String query="Select ai.account, ai.account_ref,ai.account_ref_type from Issuer i, bin_range br ,bin_range_lcm_service brl,account_info ai\n" +
                "where br.issuer_id=i.issuer_id\n" +
                "and ai.issuer_id=i.issuer_id\n" +
                "and ai.issuer_id=br.issuer_id\n" +
                "and i.issuer_id='"+issuer_id+"'\n"+
                "and br.bin_range_low=brl.bin_range_low\n" +
                "and brl.lcm_service not in ('STC')\n" +
                "and CAST((ai.account) AS INT)\n" +
                "BETWEEN SUBSTR(br.bin_range_low,1,br.pan_length) AND SUBSTR(br.bin_range_high,1,br.pan_length)ORDER BY DBMS_RANDOM.RANDOM fetch next 1 rows only";

        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);

        if (databaseSteps.result.next()) {
                if(accountType.equalsIgnoreCase("pan"))
                {
                    if (databaseSteps.result.getString("ACCOUNT") != null)
                        account.put("account", databaseSteps.result.getString("ACCOUNT_REF"));
                    if (databaseSteps.result.getString("ACCOUNT_TYPE") != null && (databaseSteps.result.getString("ACCOUNT_TYPE").equalsIgnoreCase(accountType)))
                        account.put("accountType", accountType);
                }
                else{
                if (databaseSteps.result.getString("ACCOUNT_REF") != null)
                    account.put("account", databaseSteps.result.getString("ACCOUNT_REF"));
                if (databaseSteps.result.getString("ACCOUNT_REF_TYPE") != null && (databaseSteps.result.getString("ACCOUNT_REF_TYPE").equalsIgnoreCase(accountType)))
                    account.put("accountType", databaseSteps.result.getString("ACCOUNT_REF_TYPE"));}
            }
    }
    @And("^I verify that the response has a valid vtc parameters")
    public void verifyThatResponseHasValidMerchantList() throws ParseException {
        postResponseObject = JSONHelper.parseJSONObject(restAssuredAPI.globalResponse.getBody().asString());
        Assert.assertTrue("Response contains document id",postResponseObject.get("accountRef").toString().contains("ctc"));
        Assert.assertTrue("Response contains document id",postResponseObject.get("accountRefType").toString().contains("VTCID"));

    }
    @Given("I have invalid {string},{string} for header")
    public void i_have_for_header(String headers, String invalidvalue) {
        DatabaseSteps.headersAsMap.put("X-"+headers+"-ID", invalidvalue);
    }

}
