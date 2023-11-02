package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IssuerConfig {

    public String ISSUER_ID = null;
    public String BIN_RANGE_LOW = null;
    public String CALL_ISSUER_ALWAYS = null;
    public String CALL_ISSUER_FOR_C2P = null;
    public String ENABLE_PROFILE_ID_REF = null;

    public IssuerConfig(ResultSet result) throws SQLException {
        if (result.next()) {
            BIN_RANGE_LOW = result.getString("BIN_RANGE_LOW");
            CALL_ISSUER_ALWAYS = result.getString("CALL_ISSUER_ALWAYS");
            CALL_ISSUER_FOR_C2P = result.getString("CALL_ISSUER_FOR_C2P");
            ISSUER_ID = result.getString("ISSUER_ID");
            ENABLE_PROFILE_ID_REF = result.getString("ENABLE_PROFILE_ID_REF");
        }
        else
            Assert.fail("No rows fetched from Issuer config table for given data");
    }
}

