package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CodeMapping {

    public String CODE_MAPPING_ID = null;
    public String CODE = null;
    public String PARTNER = null;
    public String DIRECTION = null;
    public String VALID_FROM = null;
    public String VALID_TO = null;
    public String INTERNAL_VALUE = null;
    public String EXTERNAL_VALUE = null;
    public List<String> EXTERNAL_VALUES = new ArrayList<>();
    public CodeMapping() { }

    public CodeMapping(ResultSet result) throws SQLException {
        if (result.next()) {
            CODE_MAPPING_ID = result.getString("CODE_MAPPING_ID");
            CODE = result.getString("CODE");
            PARTNER = result.getString("PARTNER");
            DIRECTION = result.getString("DIRECTION");
            VALID_FROM = result.getString("VALID_FROM");
            VALID_TO = result.getString("VALID_TO");
            INTERNAL_VALUE = result.getString("INTERNAL_VALUE");
            EXTERNAL_VALUE = result.getString("EXTERNAL_VALUE");
        }
        else
            Assert.fail("No rows fetched from Code Mapping table for given data");
    }

    public void fetchTokenRequesters(ResultSet result) throws SQLException {
        EXTERNAL_VALUES = new ArrayList<>();

        while (result.next()) {
            EXTERNAL_VALUES.add(result.getString("EXTERNAL_VALUE"));
        }
    }
}
