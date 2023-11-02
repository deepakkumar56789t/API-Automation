package com.lcm.test.steps;


import com.lcm.test.database.AccountInfo;
import com.lcm.test.database.DeviceInfo;
import com.lcm.test.database.Issuer;
import com.lcm.test.database.TokenInfo;
import com.lcm.test.ui.browser.Browser;
import com.lcm.test.ui.commonUtils.Utils;
import com.lcm.test.ui.pages.DeviceMerchantViewPage;
import com.lcm.test.ui.pages.LoginPage;
import com.lcm.test.ui.pages.SearchScreenPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Steps;
import com.lcm.core.steps.DatabaseSteps;

public class PortalStep {
    @Steps
    LoginPage loginPage;

    @Steps
    SearchScreenPage searchScreen;
    @Steps
    Utils utildbQuery;

    @Steps
    DeviceMerchantViewPage device;

    @Steps
    Browser browser;

    @Steps
    DatabaseSteps databaseSteps;

    static AccountInfo accountInfo;
    static Issuer issuer;

    static TokenInfo tokenInfo;
    static DeviceInfo deviceInfo;

    @When("I verify the Portal data funding pan for device token")
    public void verifyTheportalDataFundingPanDevice() {
        device.verifyTheFundingDataOutput(utildbQuery);
    }
    @Then("I verify the Portal data funding pan for merchant token")
    public void verifyTheportalDataFundingPanMerchant() {
        device.verifyTheFundingDataOutputMerchant();
    }
    @When("I verify the token info and device info")
    public void verifyPortalDeviceTokenService() throws Exception {
        device.deviceToken();
        utildbQuery.verifyTheDeviceInfo(databaseSteps, deviceInfo);
        utildbQuery.verifyTheTokenInfo(databaseSteps, tokenInfo);
    }
    @Then("I verify the token info and merchant info")
    public void verifyPortalMerchantTokenService() {
        device.merchantToken();
    }
    @Given("I Login to Portal with valid credentials")
    public void loginToPortal() throws Exception {
        browser.invokeBrowserUtility();
        loginPage.inputFormAndSubmitPortalURL(Browser.user, Browser.pass);
    }
    /*@Given("I Login to Portal with valid credentials for merchant")
    public void loginToPortalMerchant() throws Exception {
        browser.invokeBrowserUtility();
        loginPage.inputFormAndSubmitPortalURL(Browser.user, Browser.pass);
    }*/
    @When("I Search the funding PAN {string} in Portal.")
    public void searchFundingPanInPortal(String string) {
        searchScreen.fundingPanSearchToPortal(string);
    }
    @Then("I verify Device id {string} in Portal")
    public void verifyDeviceIdInPortal(String string) {
        device.numberOfDeviceTokens(string);
        device.deviceToken();
    }
    @Given("I checked the Funding Pan added for the IssuerId for device Token {string},{string}")
    public void checkedFundingPanAddedIssuerIdDeviceToken(String string, String string2) throws Exception {
        utildbQuery.checkEnableTestCardsDatabase(databaseSteps,string,string2,accountInfo,issuer);
    }
    @Given("I checked the Funding Pan added for the IssuerId for merchant Token {string},{string}")
    public void checkedFundingPanAddedIssuerId(String string, String string2) throws Exception {
        utildbQuery.fundingPanAddedToIssuerIdFromDatabase(string, string2, databaseSteps, accountInfo, issuer);
    }
    @When("I Search the funding PAN in Portal and check the device view for {string}")
    public void verifyDeviceIdInPortalFundingPan(String string){
        searchScreen.fundingPanSearchToPortal(utildbQuery);
        device.numberOfDeviceTokens(string);
    }
    @When("I search the funding Pan and checked the device view")
    public void verifyDeviceIdInPortalFundingPan(){
        searchScreen.fundingPanSearchToPortal(utildbQuery);
    }
    @When("I perform the device level actions in individual token")
    public void verifyDeviceLevelActionFundingPan() {
        device.deviceOperationToken();
    }
    @When("I Verfiy The Device view data after performed the each token level actions")
    public void verifyDeviceLevelAction() {
        device.deviceOperationToken();
    }
    @When("I Search the funding Pan in portal and check the merchant view for {string}")
    public void verifyMerchantPortalFundingPan(String string) {
        searchScreen.fundingPanSearchToPortalMechant(utildbQuery,string);
    }
}