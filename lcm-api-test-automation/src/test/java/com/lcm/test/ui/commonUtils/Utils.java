package com.lcm.test.ui.commonUtils;

import com.lcm.core.steps.DatabaseSteps;
import com.lcm.core.utilities.JSONHelper;
import com.lcm.core.utilities.LCMProperties;
import com.lcm.test.database.AccountInfo;
import com.lcm.test.database.DeviceInfo;
import com.lcm.test.database.TokenInfo;
import com.lcm.test.database.Issuer;
import com.lcm.test.ui.browser.Browser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import java.util.Random;

public class Utils {
    public String fundingPan;
    public String cardId;
    public String accountRefType;
    public String issuerId;
    String query;
    public String wallet;

    public String status;
    public String[] deviceMerchantToken = new String[2];
    public String lcm_user;
    private final static LCMProperties runner = new LCMProperties("runner.properties");
    private String issuerName = System.getProperty("issuer") != null ? System.getProperty("issuer") : runner.getProperty("ISSUER");
    private static final String testCardsEnabled = runner.getProperty("TEST_CARDS_ENABLED").toUpperCase();
    private final String dataDriveFilePath = "src/main/resources/data/";

    public void checkEnableTestCardsDatabase(DatabaseSteps databaseSteps, String string, String string2, AccountInfo accountInfo, Issuer issuer) throws Exception {

        if (testCardsEnabled.equalsIgnoreCase("No")) {
            fundingPanAddedToIssuerIdFromDatabase(string, string2, databaseSteps, accountInfo, issuer);
        } else if (testCardsEnabled.equalsIgnoreCase("Yes")) {
            fetchValidTSPAccountFromDatabaseUsingTestCards(databaseSteps,accountInfo);
        } else {
            throw new IllegalArgumentException("Test card status is invalid. Please check format once before using it.");
        }
    }
    public void fetchValidTSPAccountFromDatabaseUsingTestCards(DatabaseSteps databaseSteps, AccountInfo  accountInfo) throws Exception {
        String testCardsPath = dataDriveFilePath + "TestCards/" + Browser.environment + "/" + issuerName.toLowerCase() + ".json";
        JSONObject testCardSets = JSONHelper.messageAsSimpleJson(testCardsPath);
        Assert.assertNotNull("No test cards are added for Portal", testCardSets);
        JSONArray testCards = JSONHelper.parseJSONArray(testCardSets.get("portalTestCards").toString());
        Assert.assertTrue("No test cards are added for Portal", testCards != null && !testCards.isEmpty());
        Random random = new Random();
        JSONObject testCard = (JSONObject) testCards.get(random.nextInt(testCards.size()));
        fundingPan = String.valueOf(testCard.get("account"));
        System.out.println("Funding Pan:-" + fundingPan);
        query="select * from account_info where account='"+fundingPan+"'";
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);
        accountInfo.fetchAccounts(databaseSteps.result);
        accountRefType = accountInfo.ACCOUNT_REF_TYPE;
        if(accountRefType.equalsIgnoreCase("CARDID")){
        cardId=accountInfo.ACCOUNT_REF;
        }
    }
    public void fundingPanAddedToIssuerIdFromDatabase(String string, String string2, DatabaseSteps databaseSteps, AccountInfo accountInfo, Issuer issuer) throws Exception {
        string = string.toUpperCase();
        query = "select * from issuer where issuer_name='" + issuerName + "'";
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        issuer = new Issuer(databaseSteps.result);
        issuerId = issuer.ISSUER_ID;
        if (string2.equals("Device")) {
            deviceMerchantToken[0] = "HCE";
            deviceMerchantToken[1] = "SECURE_ELEMENT";
        } else if (string2.equals("Merchant")) {
            deviceMerchantToken[0] = "CARD_ON_FILE";
            deviceMerchantToken[1] = "ECOMMERCE";
        } else {
            throw new IllegalArgumentException("Invalid Device or Merchant Token.");
        }
        if (string.equals("ABU")) {
            query = "select * from account_info where  issuer_id in (select issuer_id from bin_range b join bin_range_lcm_service l on (b.bin_range_low=l.bin_range_low) where l.lcm_service='" + string + "' and b.issuer_id='" + issuerId + "') and account_state='ACTIVE'  " +
                    "and account_id in (select distinct account_id from virtual_account v, token_info t " +
                    "where v.virtual_account_id=t.virtual_account_id and t.token_type " +
                    "is not null and t.token_type in ('" + deviceMerchantToken[0] + "','" + deviceMerchantToken[1] + "') and t.token_status='ACTIVE') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else if (string.equals("TSP")) {
            query = "select * from account_info where  issuer_id in (select issuer_id from bin_range b join bin_range_lcm_service l on (b.bin_range_low=l.bin_range_low) where l.lcm_service='" + string + "' and b.issuer_id='" + issuerId + "') and account_state='ACTIVE'  " +
                    "and account_id in (select distinct account_id from virtual_account v, token_info t " +
                    "where v.virtual_account_id=t.virtual_account_id and t.token_type " +
                    "is not null and t.token_type in ('" + deviceMerchantToken[0] + "','" + deviceMerchantToken[1] + "') and t.token_status='ACTIVE') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else if (string.equals("TSP,ABU")) {
            String[] tspAbuArraysString = string.split(",");
            query = "select * from account_info where  issuer_id in (select issuer_id from bin_range b join bin_range_lcm_service l on (b.bin_range_low=l.bin_range_low) where l.lcm_service='" + tspAbuArraysString[0] + "' or l.lcm_service='" + tspAbuArraysString[1] + "' and b.issuer_id='" + issuerId + "') and account_state='ACTIVE'  " +
                    "and account_id in (select distinct account_id from virtual_account v, token_info t " +
                    "where v.virtual_account_id=t.virtual_account_id and t.token_type " +
                    "is not null and t.token_type in ('" + deviceMerchantToken[0] + "','" + deviceMerchantToken[1] + "') and t.token_status='ACTIVE') ORDER BY DBMS_RANDOM.RANDOM fetch first 1 rows only";
        } else {
            String data = string.toLowerCase();
            throw new IllegalArgumentException("Data is invalid. Please check this data " + data + " before proceeded");
        }
        databaseSteps.iEstablishConnectionToLCMDatabase();
        databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
        accountInfo = new AccountInfo(databaseSteps.result);
        accountInfo.fetchAccounts(databaseSteps.result);
        fundingPan = accountInfo.ACCOUNT;
        accountRefType = accountInfo.ACCOUNT_REF_TYPE;
        if(accountRefType.equalsIgnoreCase("CARDID")){
            cardId=accountInfo.ACCOUNT_REF;
        }
    }


    public void verifyTheTokenInfo(DatabaseSteps databaseSteps, TokenInfo tokenInfo) throws Exception {
        if (!fundingPan.equalsIgnoreCase("null")) {
            query = "select * from token_info where token_type in ('HCE','SECURE_ELEMENT') and token_status='ACTIVE' and " +
                    "virtual_account_id in (select virtual_account_id from account_info a join virtual_account v on (a.account_id=v.account_id) where a.account='" + fundingPan + "')";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            tokenInfo = new TokenInfo(databaseSteps.result);
            wallet = tokenInfo.TOKEN_REQUESTOR_NAME;
            status = tokenInfo.TOKEN_STATUS;
        }
    }

    public void verifyTheDeviceInfo(DatabaseSteps databaseSteps, DeviceInfo deviceInfo) throws Exception {
        if (!fundingPan.equalsIgnoreCase("null")) {
            query = "select d.* from device_info d join token_info t on (d.virtual_account_id=t.virtual_account_id) " +
                    "where t.token_type in ('HCE','SECURE_ELEMENT') and t.token_status='ACTIVE' and t.virtual_account_id in " +
                    "(select v.virtual_account_id from account_info a join virtual_account v on (a.account_id=v.account_id) where a.account='" + fundingPan + "')";
            databaseSteps.iEstablishConnectionToLCMDatabase();
            databaseSteps.result = databaseSteps.dbConnection.runQuery(query);
            deviceInfo = new DeviceInfo(databaseSteps.result);
            lcm_user = deviceInfo.CREATED_DEVICE;
        }
    }


}
