package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountInfo {

    public String ACCOUNT_ID = null;
    public String ACCOUNT_REF = null;
    public String ACCOUNT_REF_TYPE = null;
    public String ACCOUNT_TYPE = null;
    public String ACCOUNT_EXPIRY = null;
    public String ACCOUNT_STATE = null;
    public String ACCOUNT = null;
    public String ISSUER_ID = null;
    public String PROFILE_ID = null;
    public List<String> ACCOUNTS = new ArrayList<>();

    public AccountInfo() { }
    public AccountInfo(ResultSet result) throws SQLException {
        if (result.next()) {
            ACCOUNT_REF = result.getString("ACCOUNT_REF");
            ACCOUNT_ID = result.getString("ACCOUNT_ID");
            ACCOUNT_REF_TYPE = result.getString("ACCOUNT_REF_TYPE");
            ACCOUNT_TYPE = result.getString("ACCOUNT_TYPE");
            ACCOUNT_STATE = result.getString("ACCOUNT_STATE");
            ACCOUNT_EXPIRY = result.getString("ACCOUNT_EXPIRY");
            ACCOUNT = result.getString("ACCOUNT");
            ISSUER_ID = result.getString("ISSUER_ID");
            PROFILE_ID = result.getString("PROFILE_ID");
        }
        else
            Assert.fail("No rows fetched from account info table for given data");
    }

    public void fetchAccounts(ResultSet result) throws SQLException {
        ACCOUNTS = new ArrayList<>();
        while (result.next()) {
            ACCOUNTS.add(result.getString("ACCOUNT"));
        }
    }
}
