package com.lcm.test.ui.pages;

import com.lcm.core.utilities.LCMProperties;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;

public class LoginPage extends PageObject {

    private final static LCMProperties runner = new LCMProperties("runner.properties");
    private static final String isKundePortal = System.getProperty("IS_KUNDEPORTAL") != null ? System.getProperty("IS_KUNDEPORTAL").toUpperCase() : runner.getProperty("IS_KUNDEPORTAL").toUpperCase();

    private final static LCMProperties credentials = new LCMProperties("credentails.properties");

    @FindBy(xpath = "//input[@name='logonID']")
     WebElementFacade userNameKunde;

    @FindBy(xpath ="//input[@id='password']")
     WebElementFacade passwordKunde;

    @FindBy(xpath="//input[@id='input_submit']")
     WebElementFacade SignInKunde;


    @FindBy(xpath="//a[text()='Accept']")
    WebElementFacade acceptCookies;

    @FindBy(xpath="//strong[text()='User ID and password ']")
    WebElementFacade userIdPasswordLink;

    @FindBy(xpath="//a[text()='1Shop4Everything']")
    WebElementFacade oneShop4Everything;

    @FindBy(xpath = "//input[@id='username']")
    WebElementFacade usernameExternalURl;

    @FindBy(xpath = "//input[@id='password']")
    WebElementFacade passwordExternalURL;

    @FindBy(xpath = "//span[@class=\"regularBtn-content\"]")
    WebElementFacade signInExternalUrl;



    public void inputFormAndSubmitPortalURL(String user, String Pass){
        if(isKundePortal.equalsIgnoreCase("Yes")){
            user=credentials.getProperty("kundePortalusername");
            Pass=credentials.getProperty("kundePortalpassword");
            if(acceptCookies.isDisplayed()){
                acceptCookies.click();
            }
            waitForAngularRequestsToFinish();
            if(userIdPasswordLink.isDisplayed()){
                userIdPasswordLink.click();
            }
            waitForAngularRequestsToFinish();
            if(userNameKunde.isDisplayed()){
                userNameKunde.sendKeys(user);
            }
            if(passwordKunde.isDisplayed()){
                passwordKunde.sendKeys(Pass);
            }
            if(SignInKunde.isDisplayed()){
                SignInKunde.click();
            }
            waitForAngularRequestsToFinish();
            if(oneShop4Everything.isDisplayed()){
                oneShop4Everything.click();
            }
            waitForAngularRequestsToFinish();
        }
        else{
            if(usernameExternalURl.isDisplayed()){
                usernameExternalURl.sendKeys(user);
            }
            if(passwordExternalURL.isDisplayed()){
                passwordExternalURL.sendKeys(Pass);
            }
            if(signInExternalUrl.isDisplayed()){
                signInExternalUrl.click();
            }
        }
    }
}

