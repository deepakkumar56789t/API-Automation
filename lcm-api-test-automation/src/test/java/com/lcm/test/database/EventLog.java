package com.lcm.test.database;

import com.lcm.core.utilities.DBConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventLog {

    private static final Logger log;

    static {
        log = LoggerFactory.getLogger(EventLog.class.getName());
    }

    public static void verifyEventLogs(ResultSet result, JSONArray expectedResults) throws SQLException {
        Assert.assertTrue("No results found in the database for Event Log", DBConnection.recordCount() != 0);

        while (result.next()) {
            int idx;
            String logType = result.getString("SOURCE").toLowerCase();
            logType += " log";
            boolean flag = false;

            for (idx = 0; idx < expectedResults.size(); idx++) {
                JSONObject expectedResult = (JSONObject) expectedResults.get(idx);
                if (result.getString("SOURCE").equals(expectedResult.get("SOURCE"))) {
                    Assert.assertEquals("Incorrect details are fetched from DB for given X_REQUEST_ID in " + logType + " of Event Log", expectedResult.get("X_REQUEST_ID"), result.getString("X_REQUEST_ID"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given ISSUER_ID in " + logType + " of Event Log", expectedResult.get("ISSUER_ID"), result.getString("ISSUER_ID"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given DESTINATION in " + logType + " of Event Log", expectedResult.get("DESTINATION"), result.getString("DESTINATION"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given STATUS in " + logType + " of Event Log", expectedResult.get("STATUS"), result.getString("STATUS"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given ACTION in " + logType + " of Event Log", expectedResult.get("ACTION").toString(), result.getString("ACTION"));
                    Assert.assertEquals("Incorrect details are fetched from DB for given ACCOUNT_ID in " + logType + " of Event Log", expectedResult.get("ACCOUNT_ID"), result.getString("ACCOUNT_ID") == null ? null : result.getString("ACCOUNT_ID").trim());
                    flag = true;
                    break;
                }
            }

            Assert.assertTrue("Unexpected record has been found or generated in Event log table for the X_REQUEST_ID " + result.getString("X_REQUEST_ID") + " in " + logType, flag);
        }
    }
}
