package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VirtualAccount {

    public String ACCOUNT_ID = null;
    public String VIRTUAL_ACCOUNT_ID = null;
    public String PAN_REFERENCE = null;
    public String STATUS = null;
    public String CARD_HOLDER_NAME = null;
    public String BILLING_ADDRESS = null;
    public String CARD_ORG = null;
    public String WALLET_ACCOUNT_EMAIL_HASH = null;
    public String CLIENT_WALLET_ID = null;
    public String PAN_SOURCE = null;
    public String ACTION_CODE = null;
    public String ERROR_CODE = null;
    public String OTP_VALUE = null;
    public String OTP_EXPIRATION_DATE = null;
    public String API_CALL = null;
    public String MC_REQUEST_ID = null;
    public String ENROLMENT_FLOW = null;
    public String MESSAGE_REASON = null;
    public static List<String> VIRTUALACCOUNTS = new ArrayList<>();

    public VirtualAccount(ResultSet result) throws SQLException {
        if (result.next()) {
            VIRTUAL_ACCOUNT_ID = result.getString("VIRTUAL_ACCOUNT_ID");
            ACCOUNT_ID = result.getString("ACCOUNT_ID");
            PAN_REFERENCE = result.getString("PAN_REFERENCE");
            STATUS = result.getString("STATUS");
            CARD_HOLDER_NAME = result.getString("CARD_HOLDER_NAME");
            BILLING_ADDRESS = result.getString("BILLING_ADDRESS");
            CARD_ORG = result.getString("CARD_ORG");
            WALLET_ACCOUNT_EMAIL_HASH = result.getString("WALLET_ACCOUNT_EMAIL_HASH");
            CLIENT_WALLET_ID = result.getString("CLIENT_WALLET_ID");
            PAN_SOURCE = result.getString("PAN_SOURCE");
            ACTION_CODE = result.getString("ACTION_CODE");
            ERROR_CODE = result.getString("ERROR_CODE");
            OTP_VALUE = result.getString("OTP_VALUE");
            OTP_EXPIRATION_DATE = result.getString("OTP_EXPIRATION_DATE");
            API_CALL = result.getString("API_CALL");
            ENROLMENT_FLOW = result.getString("ENROLMENT_FLOW");
            MESSAGE_REASON = result.getString("MESSAGE_REASON");
            MC_REQUEST_ID =result.getString("MC_REQUEST_ID");
        }
        else
            Assert.fail("No rows fetched from virtual account table for given data");
    }

    public static void fetchVirtualAccounts(ResultSet result) throws SQLException {
        VIRTUALACCOUNTS = new ArrayList<>();
        while (result.next()) {
            VIRTUALACCOUNTS.add(result.getString("VIRTUAL_ACCOUNT_ID"));
        }
    }
}
