package com.lcm.core.utilities;

import java.sql.*;

public class DBConnection
{
    static ResultSet result = null;
    static Connection conn = null;
    static Statement statement = null;
    private static String dbFile = null;
    LCMProperties runner = new LCMProperties("runner.properties");
    private final String environment = System.getProperty("environment") != null ? System.getProperty("environment").toUpperCase() : runner.getProperty("ENVIRONMENT").toUpperCase();

    public DBConnection(Boolean isLCM, Boolean isCommonLogging, Boolean isSubscription) throws SQLException, ClassNotFoundException {
        //Close the existing connection if any
        close();

        if (isLCM != null && isLCM) {
            dbFile = "database/" + environment + "/LCM.properties";
        }
        else if (isCommonLogging != null && isCommonLogging) {
            dbFile = "database/" + environment + "/CommonLog.properties";
        }
        else if (isSubscription != null && isSubscription) {
            dbFile = "database/" + environment + "/Subscription.properties";
        }

        LCMProperties dbProperties = new LCMProperties(dbFile);
        String hostName = dbProperties.getProperty("HOSTNAME");
        String portNumber = dbProperties.getProperty("PORT-NUMBER");
        String serviceName = dbProperties.getProperty("SERVICE-NAME");
        String username = dbProperties.getProperty("USERNAME");
        String password = dbProperties.getProperty("PASSWORD");

        String url = "jdbc:oracle:thin:" + username + "/" + password + "@//" + hostName + ":" + portNumber + "/" + serviceName;
        Class.forName("oracle.jdbc.driver.OracleDriver");

        conn = DriverManager.getConnection(url);
        statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
    }

    public void close() throws SQLException {
        if (conn != null) {
            statement.close();
            conn.close();
            statement = null;
            conn = null;
        }
    }

    public ResultSet runQuery(String query) {
        if (conn != null) {
            try {
                result = statement.executeQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static int recordCount() {
        int size = 0;
        try {
            if (result != null) {
                result.last();
                size = result.getRow();
                result.beforeFirst();
            }
        }
        catch(Exception ex) {
            return 0;
        }
        return size;
    }
}