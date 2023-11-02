package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TokenInfo {

    public String API_CALL = null;
    public String CARD_METADATA_PROFILE_ID = null;
    public String CARD_ORG = null;
    public String MESSAGE_REASON_CODE = null;
    public String NO_OF_ACTIVE_TOKENS = null;
    public String NO_OF_INACTIVE_TOKENS = null;
    public String NO_OF_SUSPENDED_TOKENS = null;
    public String TOKEN = null;
    public String TOKEN_ACTIVATION_DATE = null;
    public String TOKEN_DEACTIVATION_DATE = null;
    public String TOKEN_EXPIRY_DATE = null;
    public String TOKEN_REFERENCE_ID = null;
    public String TOKEN_REQUESTOR_ID = null;
    public String TOKEN_REQUESTOR_NAME = null;
    public String TOKEN_STATUS = null;
    public String TOKEN_TYPE = null;
    public String TOKEN_UNIQUE_REFERENCE = null;
    public String VIRTUAL_ACCOUNT_ID = null;

    public TokenInfo(ResultSet result) throws SQLException {
        if (result.next()) {
            API_CALL = result.getString("API_CALL");
            CARD_METADATA_PROFILE_ID = result.getString("CARD_METADATA_PROFILE_ID");
            CARD_ORG = result.getString("CARD_ORG");
            MESSAGE_REASON_CODE = result.getString("MESSAGE_REASON_CODE");
            NO_OF_ACTIVE_TOKENS = result.getString("NO_OF_ACTIVE_TOKENS");
            NO_OF_INACTIVE_TOKENS = result.getString("NO_OF_INACTIVE_TOKENS");
            NO_OF_SUSPENDED_TOKENS = result.getString("NO_OF_SUSPENDED_TOKENS");
            TOKEN = result.getString("TOKEN");
            TOKEN_ACTIVATION_DATE = result.getString("TOKEN_ACTIVATION_DATE");
            TOKEN_DEACTIVATION_DATE = result.getString("TOKEN_DEACTIVATION_DATE");
            TOKEN_EXPIRY_DATE = result.getString("TOKEN_EXPIRY_DATE");
            TOKEN_REFERENCE_ID = result.getString("TOKEN_REFERENCE_ID");
            TOKEN_REQUESTOR_ID = result.getString("TOKEN_REQUESTOR_ID");
            TOKEN_REQUESTOR_NAME = result.getString("TOKEN_REQUESTOR_NAME");
            TOKEN_STATUS = result.getString("TOKEN_STATUS");
            TOKEN_TYPE = result.getString("TOKEN_TYPE");
            TOKEN_UNIQUE_REFERENCE = result.getString("TOKEN_UNIQUE_REFERENCE");
            VIRTUAL_ACCOUNT_ID = result.getString("VIRTUAL_ACCOUNT_ID");
        }
        else
            Assert.fail("No rows fetched from Token Info table for given token reference id");
    }
}
