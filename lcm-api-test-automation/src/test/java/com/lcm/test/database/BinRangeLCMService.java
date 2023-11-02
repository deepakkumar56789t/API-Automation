package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BinRangeLCMService {


    public String BIN_RANGE_LOW = null;
    public String LCM_SERVICE = null;
    public BinRangeLCMService(ResultSet result) throws SQLException {
        if (result.next()) {
            BIN_RANGE_LOW = result.getString("BIN_RANGE_LOW");
            LCM_SERVICE = result.getString("LCM_SERVICE");
        }
        else
            Assert.fail("No rows fetched from bin range lcm service table for given issuer id");
    }
}
