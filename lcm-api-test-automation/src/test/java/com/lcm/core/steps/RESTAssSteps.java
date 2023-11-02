package com.lcm.core.steps;

import com.lcm.core.utilities.JSONHelper;
import com.lcm.core.utilities.RESTAssuredAPI;
import io.cucumber.java.en.Then;
import org.assertj.core.api.SoftAssertions;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RESTAssSteps
{
    private static final Logger log;
    public SoftAssertions softAssert = new SoftAssertions();

    static {
        log = LoggerFactory.getLogger(RESTAssSteps.class.getName());
    }

    @Then("^I verify the status code as \"(.*?)\"$")
    public static void iVerifyTheStatusCode(final int expectedStatusCode) {
        final int actualStatusCode = RESTAssuredAPI.globalStaticResponse.getStatusCode();
        final String body = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (expectedStatusCode != actualStatusCode)
            RESTAssSteps.log.error("Status code not as expected.\n \"" + body + "\"");

        String xRequestId = "";
        if (DatabaseSteps.headersAsMap != null)
            xRequestId = " and the X-Request-ID is " + DatabaseSteps.headersAsMap.get("X-Request-ID");

        Assert.assertEquals("Status code is not as expected. The response body is " + body + xRequestId, expectedStatusCode, actualStatusCode);
    }

    @Then("^I verify that the response body is void$")
    public static void iVerifyTheVoidResponseBody() {
        final String body = RESTAssuredAPI.globalStaticResponse.getBody().asString();

        if (!body.isEmpty())
            RESTAssSteps.log.error("Response body is not empty and the response body is " + body + "\"");

        Assert.assertTrue("Response body is not empty and the response body is " + body, body.isEmpty());
    }

    @Then("^I verify that response time is under the SLA \"(.*?)\" milliseconds for \"(.*?)\" request$")
    public void iVerifyTheSLA(final long expectedMilliSec, String request) {
        final long actualMilliSec = RESTAssuredAPI.globalStaticResponse.getTime();
        if (actualMilliSec > expectedMilliSec) {
            RESTAssSteps.log.error("SLA is not as expected for " + request + " and it is " + actualMilliSec + " milliseconds");
        }

        softAssert.assertThat(actualMilliSec).as("SLA is not as expected for " + request + "\nExpected SLA: " + expectedMilliSec + "   Actual SLA: " + actualMilliSec).isLessThan(expectedMilliSec);
    }


    @Then("^I fetch the response time of the request$")
    public static void iFetchResponseTime() {
        final long actualMilliSec = RESTAssuredAPI.globalStaticResponse.getTime();

        RESTAssSteps.log.info("The response time to process the request is  " + actualMilliSec);
    }

    @Then("^I verify response is a valid json$")
    public static void iVerifyResponseForValidJson() {
        final String responseBody = RESTAssuredAPI.globalStaticResponse.getBody().asString();
        Assert.assertTrue(JSONHelper.isJSONValid(responseBody));
    }

    @Then("^I verify the LCM error code \"(.*?)\" in the response$")
    public static void iVerifyResponseForLCMErrorCode(String code) throws ParseException {
        String errorCode = String.valueOf(JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()).get("errorCode"));
        Assert.assertEquals("Invalid LCM error code is displayed for invalid encrypted PAN data","LCM-" + code, errorCode);
    }

    @Then("^I verify the error code \"(.*?)\" in the response$")
    public static void iVerifyResponseForErrorCode(String code) throws ParseException {
        String errorCode = String.valueOf(JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString()).get("errorCode"));
        Assert.assertEquals("Invalid LCM error code is displayed for invalid encrypted PAN data",code, errorCode);
    }

}
