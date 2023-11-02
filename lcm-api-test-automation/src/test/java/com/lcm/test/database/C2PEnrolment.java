package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class C2PEnrolment {

    public static String C2P_ENROLMENT_ID = null;
    public static String ISSUER_ID = null;
    public static String ISSUER_TRACE_ID = null;
    public static String X_REQUEST_ID = null;
    public static String ACCOUNT_ID = null;
    public static String TOKEN_REQUESTOR = null;
    public static String STATUS = null;

    public C2PEnrolment(ResultSet result) throws SQLException {
        if (result.next()) {
            C2P_ENROLMENT_ID = result.getString("C2P_ENROLMENT_ID");
            ISSUER_ID = result.getString("ISSUER_ID");
            ISSUER_TRACE_ID = result.getString("ISSUER_TRACE_ID");
            X_REQUEST_ID = result.getString("X_REQUEST_ID");
            ACCOUNT_ID = result.getString("ACCOUNT_ID");
            TOKEN_REQUESTOR = result.getString("TOKEN_REQUESTOR");
            STATUS = result.getString("STATUS");
        }
        else
            Assert.fail("No rows fetched from C2P Enrolment table for given issuer id");
    }
}
