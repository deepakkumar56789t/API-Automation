package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WalletConfig {

    public String ISSUER_ID = null;
    public String BIN_RANGE_LOW = null;
    public String TOKEN_REQUESTER = null;
    public String NO_OF_VIRTUAL_CARD = null;
    public List<String> TOKEN_REQUESTERS = new ArrayList<>();

    public WalletConfig() { }
    public WalletConfig(ResultSet result) throws SQLException {
        if (result.next()) {
            BIN_RANGE_LOW = result.getString("BIN_RANGE_LOW");
            TOKEN_REQUESTER = result.getString("TOKEN_REQUESTER");
            NO_OF_VIRTUAL_CARD = result.getString("NO_OF_VIRTUAL_CARD");
            ISSUER_ID = result.getString("ISSUER_ID");
        }
        else
            Assert.fail("No rows fetched from wallet config table for given data");
    }

    public void fetchTokenRequesters(ResultSet result) throws SQLException {
        TOKEN_REQUESTERS = new ArrayList<>();

        while (result.next()) {
            TOKEN_REQUESTERS.add(result.getString("TOKEN_REQUESTER"));
        }
    }
}