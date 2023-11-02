package com.lcm.core.steps;

import com.lcm.core.utilities.*;
import com.lcm.test.database.EventLog;
import com.lcm.test.database.ExternalLog;
import com.lcm.test.steps.MDESStep;
import com.lcm.test.steps.VTSStep;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.json.simple.JSONArray;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DatabaseSteps {

    private static final Logger log;
    private final LCMProperties runner = new LCMProperties("runner.properties");
    private final String commonLogging = System.getProperty("commonLoggingEnabled") != null ? System.getProperty("commonLoggingEnabled") : runner.getProperty("COMMON_LOGGING_ENABLED");

    static {
        log = LoggerFactory.getLogger(DatabaseSteps.class.getName());
    }

    public DBConnection dbConnection;
    public static Map<String, String> headersAsMap;
    public ResultSet result = null;
    public JSONArray eventLogArrayObject = new JSONArray();
    public JSONArray externalLogArrayObject = new JSONArray();

    @After(order = 0)
    @Given("I close db connection")
    public void closeDBConnection() throws Exception {
        if (this.dbConnection != null) {
            this.dbConnection.close();
            this.dbConnection = null;
        }

        VTSStep.tokenReferenceID = null;
    }

    @Given("I establish connection to LCM database")
    public void iEstablishConnectionToLCMDatabase() throws Exception {
        this.dbConnection = new DBConnection(true, null, null);
    }

    @Given("I establish connection to subscription database")
    public void iEstablishConnectionToSubscriptionDatabase() throws SQLException, ClassNotFoundException {
        this.dbConnection = new DBConnection(null, null, true);
    }

    @Given("I establish connection to Common Logging service database")
    public void iEstablishConnectionToCLSDatabase() throws SQLException, ClassNotFoundException {
        this.dbConnection = new DBConnection(null, true, null);
    }

    @Then("^I verify that no entries are created to event and external logs of Common logging service$")
    public void verifyNoEntriesInCL() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            String eventLogQuery = "select * from event_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";
            String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

            iEstablishConnectionToCLSDatabase();

            result = dbConnection.runQuery(eventLogQuery);
            Assert.assertEquals("Entries found in the Common logging service database for Event Log", 0, DBConnection.recordCount());

            result = dbConnection.runQuery(externalLogQuery);
            Assert.assertEquals("Entries found in the Common logging service database for External Log", 0, DBConnection.recordCount());
        }
    }

    @Then("^I verify that entries are created to event and external log tables as expected$")
    public void verifyEventAndExternalLog() throws Exception {
        String eventLogQuery = "select * from event_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";
        String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

        iEstablishConnectionToLCMDatabase();

        result = dbConnection.runQuery(eventLogQuery);
        EventLog.verifyEventLogs(result, eventLogArrayObject);

        result = dbConnection.runQuery(externalLogQuery);
        ExternalLog.verifyExternalLogs(result, externalLogArrayObject);
    }

    @Then("^I verify that entries are created to event and external logs of Common logging service as expected$")
    public void verifyCommonLoggingService() throws Exception {
        String eventLogQuery = "select * from event_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";
        String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

        iEstablishConnectionToCLSDatabase();

        result = dbConnection.runQuery(eventLogQuery);
        EventLog.verifyEventLogs(result, eventLogArrayObject);

        result = dbConnection.runQuery(externalLogQuery);
        ExternalLog.verifyExternalLogs(result, externalLogArrayObject);
    }

    @Then("^I verify that MDES entries are created to event and external logs of Common logging service as expected$")
    public void verifyMDESCommonLoggingService() throws Exception {
        if (commonLogging.equalsIgnoreCase("yes")) {
            String eventLogQuery = "select * from event_log where X_REQUEST_ID = '" + MDESStep.requestId + "' and ACTION='" + MDESStep.apiCall + "' ";

            String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + MDESStep.requestId + "'and ACTION='" + MDESStep.apiCall + "' ";

            iEstablishConnectionToCLSDatabase();

            result = dbConnection.runQuery(eventLogQuery);
            EventLog.verifyEventLogs(result, eventLogArrayObject);

            result = dbConnection.runQuery(externalLogQuery);
            ExternalLog.verifyExternalLogs(result, externalLogArrayObject);
        }
    }

    @Then("^I verify that entries are created to event log of Common logging service as expected$")
    public void verifyCommonLoggingServiceEventLog() throws Exception {
        String eventLogQuery = "select * from event_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

        iEstablishConnectionToCLSDatabase();

        result = dbConnection.runQuery(eventLogQuery);
        EventLog.verifyEventLogs(result, eventLogArrayObject);
    }

    @Then("^I verify that entries are created to external log of Common logging service as expected$")
    public void verifyCommonLoggingServiceExternalLog() throws Exception {
        String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

        iEstablishConnectionToCLSDatabase();

        result = dbConnection.runQuery(externalLogQuery);
        ExternalLog.verifyExternalLogs(result, externalLogArrayObject);
    }

    @Then("^I verify that entries are created to external log table as expected$")
    public void verifyExternalLog() throws Exception {
        String externalLogQuery = "select * from external_log where X_REQUEST_ID = '" + headersAsMap.get("X-Request-ID") + "'";

        iEstablishConnectionToLCMDatabase();

        result = dbConnection.runQuery(externalLogQuery);
        ExternalLog.verifyExternalLogs(result, externalLogArrayObject);
    }

}