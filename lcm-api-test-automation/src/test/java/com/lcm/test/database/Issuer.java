package com.lcm.test.database;

import org.junit.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Issuer {

    public String ISSUER_ID = null;
    public String ISSUER_NAME = null;
    public String PROVIDER_ID = null;

    public Issuer(ResultSet result) throws SQLException {
       if (result.next()) {
            ISSUER_NAME = result.getString("ISSUER_NAME");
            ISSUER_ID = result.getString("ISSUER_ID");
            PROVIDER_ID = result.getString("PROVIDER_ID");
        }
        else
            Assert.fail("No rows fetched from Issuer table for given data");
    }
}
