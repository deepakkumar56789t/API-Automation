package com.lcm.test.ui.pages;

import com.lcm.test.ui.commonUtils.Utils;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;


public class SearchScreenPage extends PageObject {
    String fundingPanLoop;

    @FindBy(xpath = "(//div[@fxlayout='row'])[3]/select")
    WebElementFacade dropdown;

    @FindBy(xpath = "//*[contains(text(),'Funding Pan')]")
    WebElementFacade funding_Pan;

    @FindBy(xpath = "//*[contains(text(),'IMEI Number')]")
    WebElementFacade imei_Number;

    @FindBy(xpath = "//*[contains(text(),'Card Id')]")
    WebElementFacade card_Id;

    @FindBy(xpath = "//*[@placeholder='Search']")
    WebElementFacade search_Box;

    @FindBy(xpath = "(//*[contains(text(),'search')])[2]")
    WebElementFacade search_Button;

    @FindBy(xpath = "(//div[@class='mat-radio-inner-circle'])[2]")
    WebElementFacade merchant_Radio_button;

    public void fundingPanSearchToPortal(String fundingPan) {
        waitForAngularRequestsToFinish();
        if (dropdown.isDisplayed()) {
            dropdown.click();
        }
        if (funding_Pan.isDisplayed()) {
            funding_Pan.click();
        }
        if (search_Box.isDisplayed()) {
            search_Box.sendKeys(fundingPan);
        }
        if (search_Button.isDisplayed()) {
            search_Button.click();
        }
        waitForAngularRequestsToFinish();
    }
    public void fundingPanSearchToPortal(Utils utils) {
        fundingPanLoop = utils.fundingPan;
        waitForAngularRequestsToFinish();
        if (dropdown.isDisplayed()) {
            dropdown.click();
        }
        if (funding_Pan.isDisplayed()) {
            funding_Pan.click();
        }
        if (search_Box.isDisplayed()) {
            search_Box.sendKeys(fundingPanLoop);
        }
        if (search_Button.isDisplayed()) {
            search_Button.click();
        }
        waitForAngularRequestsToFinish();
    }
    public void fundingPanSearchToPortalMechant(Utils utils,String string){
        fundingPanLoop = utils.fundingPan;
        System.out.println(string);
        waitForAngularRequestsToFinish();
        if (dropdown.isDisplayed()) {
            dropdown.click();
        }
        if (funding_Pan.isDisplayed()) {
            funding_Pan.click();
        }
        if (merchant_Radio_button.isDisplayed()) {
            merchant_Radio_button.click();
        }
        if (search_Box.isDisplayed()) {
            search_Box.sendKeys(fundingPanLoop);
        }
        if (search_Button.isDisplayed()) {
            search_Button.click();
        }
        waitForAngularRequestsToFinish();
    }
    public void IMEISearchToPortal(String IMEI){
        waitForAngularRequestsToFinish();
        if (dropdown.isDisplayed()) {
            dropdown.click();
        }
        if (imei_Number.isDisplayed()) {
            imei_Number.click();
        }
        if (search_Box.isDisplayed()) {
            search_Box.sendKeys(IMEI);
        }
        if (search_Button.isDisplayed()) {
            search_Button.click();
        }
        waitForAngularRequestsToFinish();
    }
    public void cardIdSearchToPortal(String cardId) {
        waitForAngularRequestsToFinish();
        if (dropdown.isDisplayed()) {
            dropdown.click();
        }
        if (card_Id.isDisplayed()) {
            card_Id.click();
        }
        if (search_Box.isDisplayed()) {
            search_Box.sendKeys(cardId);
        }
        if (search_Button.isDisplayed()) {
            search_Button.click();
        }
    }
}
