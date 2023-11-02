package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VirtualRiskInfo {

    public String ACC_HOLDER_NAME = null;
    public String CS_RISK_ASSESSMENT_SCORE = null;
    public String DEVICE_COUNTRY = null;
    public String DEVICE_IMEI = null;
    public String DEVICE_SERIAL_NUMBER = null;
    public String DEVICE_WITH_ACTIVE_TOKENS = null;
    public String NO_OF_ACTIVE_TOKENS = null;
    public String RISK_ASSESMENT_SCORE = null;
    public String VIRTUAL_ACCOUNT_ID = null;
    public String VISA_TOKEN_DECISION = null;
    public String VISA_TOKEN_SCORE = null;
    public String WALLET_PRVDR_ACCT_SCORE = null;
    public String WALLET_PRVDR_DEVICE_SCORE = null;
    public String WALLET_PRVDR_REASON_CODES = null;

    public VirtualRiskInfo(ResultSet result) throws SQLException {
        if (result.next()) {
            ACC_HOLDER_NAME = result.getString("ACC_HOLDER_NAME");
            CS_RISK_ASSESSMENT_SCORE = result.getString("CS_RISK_ASSESSMENT_SCORE");
            DEVICE_COUNTRY = result.getString("DEVICE_COUNTRY");
            DEVICE_IMEI = result.getString("DEVICE_IMEI");
            DEVICE_SERIAL_NUMBER = result.getString("DEVICE_SERIAL_NUMBER");
            DEVICE_WITH_ACTIVE_TOKENS = result.getString("DEVICE_WITH_ACTIVE_TOKENS");
            NO_OF_ACTIVE_TOKENS = result.getString("NO_OF_ACTIVE_TOKENS");
            RISK_ASSESMENT_SCORE = result.getString("RISK_ASSESMENT_SCORE");
            VIRTUAL_ACCOUNT_ID = result.getString("VIRTUAL_ACCOUNT_ID");
            VISA_TOKEN_DECISION = result.getString("VISA_TOKEN_DECISION");
            VISA_TOKEN_SCORE = result.getString("VISA_TOKEN_SCORE");
            WALLET_PRVDR_ACCT_SCORE = result.getString("WALLET_PRVDR_ACCT_SCORE");
            WALLET_PRVDR_DEVICE_SCORE = result.getString("WALLET_PRVDR_DEVICE_SCORE");
            WALLET_PRVDR_REASON_CODES = result.getString("WALLET_PRVDR_REASON_CODES");
        }
        else
            Assert.fail("No rows fetched from Token Info table for given data");
    }
}
