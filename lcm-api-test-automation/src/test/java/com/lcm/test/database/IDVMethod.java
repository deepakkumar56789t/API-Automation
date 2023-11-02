package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IDVMethod {

    public String ACCOUNT_ID = null;
    public String CHANNEL = null;
    public String CONTACT_INFO = null;
    public String OTP_IDENTIFIER = null;


    public IDVMethod(ResultSet result) throws SQLException {
       if (result.next()) {
           ACCOUNT_ID = result.getString("ACCOUNT_ID");
           CHANNEL = result.getString("CHANNEL");
           CONTACT_INFO = result.getString("CONTACT_INFO");
           OTP_IDENTIFIER = result.getString("OTP_IDENTIFIER");
        }
        else
            Assert.fail("No rows fetched from IDV method table for given data");
    }
}
