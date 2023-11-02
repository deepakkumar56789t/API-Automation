package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IssuerIDVConfig {

    public String ISSUER_ID = null;
    public String BIN_RANGE_LOW = null;
    public String TOKEN_REQUESTER = null;
    public String SMS_IDV_ENABLED = null;
    public String EMAIL_IDV_ENABLED = null;
    public String APP_2_APP_IDV_ENABLED = null;
    public String APP_APP_IDENTIFIER = null;
    public String CC_IDV_ENABLED = null;
    public String CC_IDV_DEFAULT = null;
    public String CC_IDV_DEFAULT_VALUE = null;


    public IssuerIDVConfig(ResultSet result) throws SQLException {
        if (result.next()) {
            BIN_RANGE_LOW = result.getString("BIN_RANGE_LOW");
            TOKEN_REQUESTER = result.getString("TOKEN_REQUESTER");
            ISSUER_ID = result.getString("ISSUER_ID");
            SMS_IDV_ENABLED = result.getString("SMS_IDV_ENABLED");
            EMAIL_IDV_ENABLED = result.getString("EMAIL_IDV_ENABLED");
            APP_2_APP_IDV_ENABLED = result.getString("APP_2_APP_IDV_ENABLED");
            APP_APP_IDENTIFIER = result.getString("APP_APP_IDENTIFIER");
            CC_IDV_ENABLED = result.getString("CC_IDV_ENABLED");
            CC_IDV_DEFAULT = result.getString("CC_IDV_DEFAULT");
            CC_IDV_DEFAULT_VALUE = result.getString("CC_IDV_DEFAULT_VALUE");
        }
        else
            Assert.fail("No rows fetched from Issuer idv config table for given data");
    }
}
