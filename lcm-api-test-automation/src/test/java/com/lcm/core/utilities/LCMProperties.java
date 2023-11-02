package com.lcm.core.utilities;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class LCMProperties
{
    private static final Logger log;
    private final String lcmPropertyFile;
    private Properties properties;
    private String waitTime;

    public LCMProperties(final String serviceName) {
        if (serviceName.endsWith(".properties")) {
            this.lcmPropertyFile = serviceName.toLowerCase();
        }
        else {
            this.lcmPropertyFile = "api.properties";
        }
        this.fileProcessor();
    }
    
    private void fileProcessor() {
        this.properties = new Properties();
        try {
            final InputStream inputStream = LCMProperties.class.getClassLoader().getResourceAsStream(this.lcmPropertyFile);
            this.properties.load(inputStream);
//            this.loadSystemProperties();
//            System.setProperties(this.properties);
        }
        catch (FileNotFoundException e) {
            LCMProperties.log.severe("Property File could not be found: " + this.lcmPropertyFile);
        }
        catch (IOException ex) {
            LCMProperties.log.severe(String.format("Issues reading properties of :%s. %s", this.lcmPropertyFile, ex.getMessage()));
        }
    }
    
    public String getProperty(final String key) {
        return this.properties.getProperty(key);
    }
    
    public String setProperty(final String key, final String value) {
        return (String)this.properties.setProperty(key, value);
    }
    
    public void loadSystemProperties() {
        this.properties.putAll(System.getProperties());
    }
    
    public String getMavenProperty(final String key) {
        try {
            final MavenXpp3Reader reader = new MavenXpp3Reader();
            final Model model = reader.read(new FileReader("pom.xml"));
            this.waitTime = model.getProperties().getProperty(key);
        }
        catch (IOException | org.codehaus.plexus.util.xml.pull.XmlPullParserException ex2) {
            LCMProperties.log.severe("Unable to find the key : " + key);
        }
        return this.waitTime;
    }
    
    static {
        log = Logger.getLogger(LCMProperties.class.getName());
    }
}
