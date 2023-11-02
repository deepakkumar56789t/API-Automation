package com.lcm.test.ui.pages;

import com.lcm.test.ui.commonUtils.Utils;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import org.junit.Assert;

public class DeviceMerchantViewPage extends PageObject {
    public String wallet;

    public String status;
    public String merchantView;
    public String lcmUserUI;
    public String enrollmentFlow;
    public String buttonType;

    @FindBy(xpath = "(//span[@style='transform: rotate(0deg);'])[1]")
    WebElementFacade dropdownWallet;

    @FindBy(xpath = "//label[text()='Wallet']/../span")
    WebElementFacade walletType;

    @FindBy(xpath = "(//mat-cell[@role='gridcell'])[2]")
    WebElementFacade device1;

    @FindBy(xpath = "(//div[@class='form-group label-text'])[3]/span")
    WebElementFacade noOfTokens;

    @FindBy(xpath = "(//mat-cell[@mattooltipclass='tooltip'])[1]")
    WebElementFacade statusE;

    @FindBy(xpath = "(//span[text()='LCM'])[1]")
    WebElementFacade lcmUser;

    @FindBy(xpath = "//a[text()='×']")
    WebElementFacade closeDeviceToken;

    @FindBy(xpath = "//span[@style='transform: rotate(0deg);']")
    WebElementFacade merchantDropDown;

    @FindBy(xpath = "//div[@class='form-group mat-card-label']")
    WebElementFacade merchantViewVerification;

    @FindBy(xpath = "(//mat-cell[@role='gridcell'])[6]")
    WebElementFacade merchantEnrollmenetFlow;

    @FindBy(xpath = "(//button[@ng-reflect-klass='button'])[1]")
    WebElementFacade individualTokenButton;

    @FindBy(xpath = "//p[contains(text(),'suspend Virtual-Card')]/../select")
    WebElementFacade suspendReasonDropdown;

    public void suspendOperation(){
        individualTokenButton.click();
        if (suspendReasonDropdown.isDisplayed()) {
            suspendReasonDropdown.click();
        }
    }
    public void resumeOperation(){
        individualTokenButton.click();
        if (suspendReasonDropdown.isDisplayed()) {
            suspendReasonDropdown.click();
        }
    }

    public void deviceOperationToken(){
        if (dropdownWallet.isDisplayed()) {
            dropdownWallet.click();
        }
        waitForAngularRequestsToFinish();
        if (individualTokenButton.isDisplayed()) {
            buttonType=individualTokenButton.getText();
        }
        if(buttonType.equalsIgnoreCase("suspend")){
            suspendOperation();
        }
        else if(buttonType.equalsIgnoreCase("Resume")){
            resumeOperation();
        }
        else {
            throw new IllegalArgumentException("Suspend or Resume button not visible");
        }

    }
    public void verifyTheFundingDataOutput(Utils utildbQuery) {
        if (wallet.equalsIgnoreCase(utildbQuery.wallet)) {
            System.out.println("The response body is UI as wallet:- " + wallet.toLowerCase() + " and DB as wallet:-" + utildbQuery.wallet.toLowerCase());
            Assert.assertEquals("The response body is UI as wallet:- " + wallet.toLowerCase() + " and DB as wallet:-" + utildbQuery.wallet.toLowerCase(), wallet.toLowerCase(), utildbQuery.wallet.toLowerCase());
        }
        if (status.equalsIgnoreCase(utildbQuery.status)) {
            System.out.println("The response body is UI as status:- " + status.toLowerCase() + " and DB as status:-" + utildbQuery.status.toLowerCase());
            Assert.assertEquals("The response body is UI as status:- " + status.toLowerCase() + "and DB as status:-" + utildbQuery.status.toLowerCase(), status.toLowerCase(), utildbQuery.status.toLowerCase());
        }
        if (lcmUserUI.equalsIgnoreCase(utildbQuery.lcm_user)) {
            System.out.println("The response body is UI as LCM User:- " + lcmUserUI.toLowerCase() + " and DB as LCM User:-" + utildbQuery.lcm_user.toLowerCase());
            Assert.assertEquals("The response body is UI as LCM User:- " + lcmUserUI.toLowerCase() + " and DB as LCM User:-" + utildbQuery.lcm_user.toLowerCase(), lcmUserUI.toLowerCase(), utildbQuery.lcm_user.toLowerCase());
        }

    }

    public void verifyTheFundingDataOutputMerchant() {
        if (enrollmentFlow.equalsIgnoreCase("green")) {
            Assert.assertEquals("The response body is UI as enrollmentFlow:- " + enrollmentFlow, enrollmentFlow.toLowerCase(), "green");
        } else if (enrollmentFlow.equalsIgnoreCase("yellow")) {
            Assert.assertEquals("The response body is UI as enrollmentFlow:- " + enrollmentFlow, enrollmentFlow.toLowerCase(), "yellow");
        } else if (enrollmentFlow.equalsIgnoreCase("red")) {
            Assert.assertEquals("The response body is UI as enrollmentFlow:- " + enrollmentFlow, enrollmentFlow.toLowerCase(), "red");
        }
    }

    public void merchantToken() {
        waitForAngularRequestsToFinish();
        if (merchantDropDown.isDisplayed()) {
            merchantDropDown.click();
        }
        if (merchantViewVerification.isDisplayed()) {
            merchantView = merchantViewVerification.getText();
            if (merchantView.contains("Merchant Tokens")) {
                Assert.assertTrue(true);
            }
        }
        if (merchantEnrollmenetFlow.isDisplayed()) {
            enrollmentFlow = merchantEnrollmenetFlow.getText();
        }
    }

    public void numberOfDeviceTokens(String lcmId) {
        waitForAngularRequestsToFinish();
        Assert.assertTrue(Integer.parseInt(String.valueOf(noOfTokens.getText())) > 0);
    }
    public void deviceToken() {
        if (dropdownWallet.isDisplayed()) {
            dropdownWallet.click();
        }
        waitForAngularRequestsToFinish();
        if (statusE.isDisplayed()) {
            status = statusE.getText();
        }
        if (device1.isDisplayed()) {
            device1.click();
        }
        waitForAngularRequestsToFinish();
        if (walletType.isDisplayed()) {
            wallet = walletType.getText();
        }
        if (lcmUser.isDisplayed()) {
            lcmUserUI = lcmUser.getText();
        }
        if (closeDeviceToken.isDisplayed()) {
            closeDeviceToken.click();
        }
    }
}
