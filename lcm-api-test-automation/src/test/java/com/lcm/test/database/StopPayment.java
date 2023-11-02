package com.lcm.test.database;

import com.lcm.core.utilities.CommonUtil;
import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class StopPayment {

    public String START_DATE = null;
    public String END_DATE = null;

    public static void buildStopPaymentResponse(ResultSet result, JSONObject expectedResult) throws SQLException {
        if (result.next()) {
            if (expectedResult.get("stopPaymentInfo") != null) {
                JSONObject stopPaymentInfo = (JSONObject) expectedResult.get("stopPaymentInfo");
                if (stopPaymentInfo.get("startDate") != null && stopPaymentInfo.get("startDate").equals("FETCH_FROM_DB"))
                    stopPaymentInfo.put("startDate", result.getString("START_DATE").replace(".", ""));
                if (stopPaymentInfo.get("endDate") != null && stopPaymentInfo.get("endDate").equals("FETCH_FROM_DB"))
                    stopPaymentInfo.put("endDate", result.getString("END_DATE").replace(".", ""));
                if (stopPaymentInfo.get("stopPaymentType") != null && stopPaymentInfo.get("stopPaymentType").equals("FETCH_FROM_DB"))
                    stopPaymentInfo.put("stopPaymentType", result.getString("TYPE"));
                if (stopPaymentInfo.get("status") != null && stopPaymentInfo.get("status").equals("FETCH_FROM_DB"))
                    stopPaymentInfo.put("status", result.getString("STATUS"));
            }
        }
    }

    public static void buildExtendStopPaymentRequest(ResultSet result, JSONObject actualRequest) throws SQLException, ParseException {
        if (result.next()) {
            if (actualRequest.get("endDate") != null && actualRequest.get("endDate").equals("FETCH_FROM_DB")) {
                String endDate = result.getString("END_DATE");
                endDate = CommonUtil.addDaysFromDate(endDate, CommonUtil.generateRandomNumber(), "dd.MM.yyyy");
                actualRequest.put("endDate", endDate.replace(".", ""));
            }
        }
    }

    public StopPayment(ResultSet result) throws SQLException {
        if (result.next()) {
            END_DATE = result.getString("END_DATE");
            START_DATE = result.getString("START_DATE");
        }
    }
}
