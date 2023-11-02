package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountAdditionalInfo {

    public String ACCOUNT_ID = null;
    public String ADDITIONAL_PARAMETERS_RESPONSE = null;
    public AccountAdditionalInfo(ResultSet result) throws SQLException {
        if (result.next()) {
            ACCOUNT_ID = result.getString("ACCOUNT_ID");
            ADDITIONAL_PARAMETERS_RESPONSE = result.getString("ADDITIONAL_PARAMETERS_RESPONSE");
        }
        else
            Assert.fail("No rows fetched from account additional info table for given account id");
    }
}
