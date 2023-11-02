package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BinRange {

    public String ISSUER_ID = null;
    public String MEMBER_NO = null;
    public String CARD_ORG = null;
    public String BIN_RANGE_LOW = null;
    public String BIN_RANGE_HIGH = null;
    public String RISK_SCR_THRESHOLD_1 = null;
    public String RISK_SCR_THRESHOLD_2 = null;
    public String PAN_LENGTH = null;
    public String PROFILE_ID = null;
    public List<String> BIN_RANGES_LOW = new ArrayList<>();
    public List<String> BIN_RANGES_HIGH = new ArrayList<>();
    public List<String> PANS_LENGTH = new ArrayList<>();
    public List<String> PROFILES_ID = new ArrayList<>();
    public BinRange() { }
    public BinRange(ResultSet result) throws SQLException {
        if (result.next()) {
            ISSUER_ID = result.getString("ISSUER_ID");
            MEMBER_NO = result.getString("MEMBER_NO");
            CARD_ORG = result.getString("CARD_ORG");
            BIN_RANGE_LOW = result.getString("BIN_RANGE_LOW");
            BIN_RANGE_HIGH = result.getString("BIN_RANGE_HIGH");
            RISK_SCR_THRESHOLD_1 = result.getString("RISK_SCR_THRESHOLD_1");
            RISK_SCR_THRESHOLD_2 = result.getString("RISK_SCR_THRESHOLD_2");
            PAN_LENGTH = result.getString("PAN_LENGTH");
            PROFILE_ID = result.getString("PROFILE_ID");
        }
        else
            Assert.fail("No rows fetched from bin range table for given issuer id");
    }

    public void fetchBinRanges(ResultSet result) throws SQLException {
        BIN_RANGES_LOW = new ArrayList<>();
        BIN_RANGES_HIGH = new ArrayList<>();
        PANS_LENGTH = new ArrayList<>();
        PROFILES_ID = new ArrayList<>();

        while (result.next()) {
            BIN_RANGES_LOW.add(result.getString("BIN_RANGE_LOW"));
            BIN_RANGES_HIGH.add(result.getString("BIN_RANGE_HIGH"));
            PANS_LENGTH.add(result.getString("PAN_LENGTH"));
            PROFILES_ID.add(result.getString("PROFILE_ID"));
        }
    }
}
