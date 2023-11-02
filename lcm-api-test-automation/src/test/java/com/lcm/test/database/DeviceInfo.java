package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeviceInfo {

    public String DEVICE_ID = null;
    public String VIRTUAL_ACCOUNT_ID = null;
    public String CARD_SCHEME_DEVICE_ID = null;
    public String DEVICE_BRAND = null;
    public String DEVICE_ID_TYPE = null;
    public String DEVICE_INDEX = null;
    public String DEVICE_IP_ADDRESS = null;
    public String DEVICE_LANGUAGE_CODE = null;
    public String DEVICE_LOCATION = null;
    public String DEVICE_MANUFACTURER = null;
    public String DEVICE_MODEL = null;
    public String DEVICE_NAME = null;
    public String DEVICE_NUMBER = null;
    public String DEVICE_TYPE = null;
    public String ORIGINAL_DEVICE = null;
    public String CREATED_DEVICE = null;
    public String OS_TYPE = null;
    public String OS_VERSION = null;

    public DeviceInfo(ResultSet result) throws SQLException {
        if (result.next()) {
            DEVICE_ID = result.getString("DEVICE_ID");
            VIRTUAL_ACCOUNT_ID = result.getString("VIRTUAL_ACCOUNT_ID");
            CARD_SCHEME_DEVICE_ID = result.getString("CARD_SCHEME_DEVICE_ID");
            DEVICE_BRAND = result.getString("DEVICE_BRAND");
            DEVICE_ID_TYPE = result.getString("DEVICE_ID_TYPE");
            DEVICE_INDEX = result.getString("DEVICE_INDEX");
            DEVICE_IP_ADDRESS = result.getString("DEVICE_IP_ADDRESS");
            DEVICE_LANGUAGE_CODE = result.getString("DEVICE_LANGUAGE_CODE");
            DEVICE_LOCATION = result.getString("DEVICE_LOCATION");
            DEVICE_MANUFACTURER = result.getString("DEVICE_MANUFACTURER");
            DEVICE_MODEL = result.getString("DEVICE_MODEL");
            DEVICE_NAME = result.getString("DEVICE_NAME");
            DEVICE_NUMBER = result.getString("DEVICE_NUMBER");
            DEVICE_TYPE = result.getString("DEVICE_TYPE");
            ORIGINAL_DEVICE = result.getString("ORIGINAL_DEVICE");
            OS_TYPE = result.getString("OS_TYPE");
            OS_VERSION = result.getString("OS_VERSION");
            CREATED_DEVICE=result.getString("CREATED_BY");
        }
        else
            Assert.fail("No rows fetched from Token Info table for given data");
    }
}
