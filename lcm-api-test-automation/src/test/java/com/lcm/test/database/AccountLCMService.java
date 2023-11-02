package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountLCMService {

    public String ACCOUNT_ID = null;
    public String LCM_SERVICE = null;
    public AccountLCMService(ResultSet result) throws SQLException {
        if (result.next()) {
            ACCOUNT_ID = result.getString("ACCOUNT_ID");
            LCM_SERVICE = result.getString("LCM_SERVICE");
        }
        else
            Assert.fail("No rows fetched from account lcm service table for given account id");
    }
}
