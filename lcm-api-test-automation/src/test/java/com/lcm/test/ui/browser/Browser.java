package com.lcm.test.ui.browser;

import com.lcm.core.utilities.LCMProperties;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.junit.runners.SerenityRunner;
import org.junit.runner.RunWith;

@RunWith(SerenityRunner.class)
public class Browser extends PageObject {
    private final static LCMProperties properties = new LCMProperties("ui.properties");
    private final static LCMProperties credentials = new LCMProperties("credentails.properties");
    public static String user = credentials.getProperty("externalUsername");
    public static String pass = credentials.getProperty("externalPassword");
    private final static LCMProperties runner = new LCMProperties("runner.properties");
    public static final String environment =  runner.getProperty("ENVIRONMENT").toUpperCase();
    private static final String iskundeportal = System.getProperty("IS_KUNDEPORTAL") != null ? System.getProperty("IS_KUNDEPORTAL").toUpperCase() : runner.getProperty("IS_KUNDEPORTAL").toUpperCase();
    private static final String isInternalUrl = System.getProperty("IS_INTERNAL") != null ? System.getProperty("IS_INTERNAL").toUpperCase() : runner.getProperty("IS_INTERNAL").toUpperCase();
    private static String portalUrl;
    private static String urlType;
    private static String portalEnvironment;
    public static String getPortalUrlEnvironmentType() throws Exception {
        portalEnvironment=environment.toLowerCase();
        if(!portalEnvironment.isEmpty()){
                    if(iskundeportal.equalsIgnoreCase("Yes")){
                        portalUrl=properties.getProperty("PORTAL_DEMO_KUNDE_URL");
                    }
                    else{
                        portalUrl=properties.getProperty("PORTAL_" + environment + "_URL");
                        if(isInternalUrl.equalsIgnoreCase("Yes")){
                            urlType="internal";
                        }
                        else if(isInternalUrl.equalsIgnoreCase("No")){
                            urlType="external";
                        }
                        else{
                            throw new IllegalArgumentException("URL is invalid or blanck check url then proceed");
                        }

                        portalUrl=portalUrl.replace("${PURLTYPE}",urlType);
                    }
        }
        return portalUrl;
    }
    public  void invokeBrowserUtility() throws Exception {
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        openUrl(getPortalUrlEnvironmentType());
        waitForAngularRequestsToFinish();
        getDriver().manage().window().maximize();
    }
}
