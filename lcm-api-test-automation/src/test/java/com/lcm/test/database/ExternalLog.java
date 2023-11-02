package com.lcm.test.database;

import com.lcm.core.utilities.DBConnection;
import com.lcm.core.utilities.JSONHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ExternalLog {

    private static final Logger log;

    static {
        log = LoggerFactory.getLogger(EventLog.class.getName());
    }

    public static void verifyExternalLogs(ResultSet result, JSONArray expectedResults) throws SQLException, ParseException {

        Assert.assertTrue("No results found in the database for External Log", DBConnection.recordCount() != 0);

        while (result.next()) {
            int idx;
            String logType = result.getString("ENDPOINT").toLowerCase().contains("sandbox") || result.getString("API_CALL").toLowerCase().contains("tuum")? "additional" : "service";
            logType += " external log";
            String endpoint = null;

            for (idx=0; idx<expectedResults.size(); idx++) {
                JSONObject expectedResult = (JSONObject) expectedResults.get(idx);
                endpoint = expectedResult.get("ENDPOINT").toString();

                if (result.getString("ENDPOINT").equals(endpoint)) {
                    Assert.assertEquals("Incorrect details are fetched from DB for given X_REQUEST_ID in " + logType, expectedResult.get("X_REQUEST_ID"), result.getString("X_REQUEST_ID"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given API_CALL in " + logType, expectedResult.get("API_CALL"), result.getString("API_CALL"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given ENDPOINT in " + logType, expectedResult.get("ENDPOINT"), result.getString("ENDPOINT"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given HTTP_RESPONSE in " + logType, expectedResult.get("HTTP_RESPONSE"), result.getString("HTTP_RESPONSE"));

                    Assert.assertEquals("Incorrect details are fetched from DB for given STATUS in " + logType, expectedResult.get("STATUS"), result.getString("STATUS"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given ACTION in " + logType, expectedResult.get("ACTION"), result.getString("ACTION"));

                    if (expectedResult.get("EXTERNAL_REQUEST_PAYLOAD") != null && result.getString("EXTERNAL_REQUEST_PAYLOAD") != null){
                       JSONHelper.compareJSONMessage((JSONObject) expectedResult.get("EXTERNAL_REQUEST_PAYLOAD"), JSONHelper.parseJSONObject(result.getString("EXTERNAL_REQUEST_PAYLOAD")));
                    }
                    else if (expectedResult.get("EXTERNAL_REQUEST_PAYLOAD") != null && result.getString("EXTERNAL_REQUEST_PAYLOAD") == null) {
                        Assert.assertNotNull("EXTERNAL_REQUEST_PAYLOAD is returned null in " + logType, result.getString("EXTERNAL_REQUEST_PAYLOAD"));
                    }

                    if (expectedResult.get("EXTERNAL_RESPONSE_PAYLOAD") != null && result.getString("EXTERNAL_RESPONSE_PAYLOAD") != null){
                        if (expectedResult.get("HTTP_RESPONSE").toString().startsWith("20")) {
                            JSONObject actualResult = JSONHelper.parseJSONObject(result.getString("EXTERNAL_RESPONSE_PAYLOAD"));
                           if(actualResult.containsKey("activationMethods")){
                           JSONArray activation=(JSONArray) actualResult.get("activationMethods");
                               for (Object o : activation) {
                                   JSONObject object = (JSONObject) o;
                                   object.remove("identifier");
                                   object.remove("sourceAddress");
                                   object.remove("otpMethodPlatform");
                               }
                           }
                            actualResult.remove("deviceInfo");
                            JSONObject expectedRes = (JSONObject) expectedResult.get("EXTERNAL_RESPONSE_PAYLOAD");
                            expectedRes.remove("deviceInfo");
                            JSONHelper.compareJSONMessage(expectedRes, actualResult);
//                            JSONObject actualDevice = (JSONObject) actualResult.get("deviceInfo");
//                            JSONObject expectedDevice = (JSONObject) expectedRes.get("deviceInfo");
//                            JSONHelper.compareJSONMessage(expectedDevice, actualDevice);

                        }
                        else {
                            if (JSONHelper.isJSONValid(result.getString("EXTERNAL_RESPONSE_PAYLOAD"))) {
                                JSONObject actualRes = JSONHelper.parseJSONObject(result.getString("EXTERNAL_RESPONSE_PAYLOAD"));
                                JSONObject expectedRes = (JSONObject) expectedResult.get("EXTERNAL_RESPONSE_PAYLOAD");
                                JSONHelper.compareJSONMessage(expectedRes, actualRes);
                            }
                            else
                                Assert.assertEquals("Incorrect details are fetched from DB for given STATUS in " + logType, expectedResult.get("EXTERNAL_RESPONSE_PAYLOAD"), result.getString("EXTERNAL_RESPONSE_PAYLOAD"));
                        }

                    }
                    else if (expectedResult.get("EXTERNAL_RESPONSE_PAYLOAD") != null && result.getString("EXTERNAL_RESPONSE_PAYLOAD") == null) {
                        Assert.assertNotNull("EXTERNAL_RESPONSE_PAYLOAD is returned null in " + logType, result.getString("EXTERNAL_RESPONSE_PAYLOAD"));
                    }

                    break;
                }
            }

            if (idx < expectedResults.size())
                Assert.assertEquals("Unexpected record has been found or generated in External log table for the X_REQUEST_ID " + result.getString("X_REQUEST_ID"), result.getString("ENDPOINT"), endpoint);
        }
    }
}
