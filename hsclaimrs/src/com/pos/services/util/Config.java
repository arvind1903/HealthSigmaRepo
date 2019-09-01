package com.pos.services.util;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class Config {
	private static Log log = LogFactory.getLog(Config.class);

	private static Config configInstance = null;
	
    private Properties props = new Properties();

    private Config() {
    	
    }
    
    public static Config getInstance() {
    	if(configInstance==null) {
    		configInstance = new Config();
    	}
    	return configInstance;
    }

    public String getProperty(String key) {
    	return getValue(key);
    }
    public String getValue(String key) {
      return props.getProperty(key);
    }
    
    public String getValue(String key, String defaultValue){
    	String val = getInstance().getValue(key);
    	return val != null? val : defaultValue;
    }
    
    public String getValueAsStringAndTrim(String key, String defaultValue){
    	String val = getValueAsStringAndTrim(key);
    	return val != null ? val : defaultValue;
    }
    
    public String getValueAsStringAndTrim(String key){
    	String value = getInstance().getValue(key);
    	if (value != null){
    		return value.trim();
    	} 
   		return value;
    }
    
    public int getValueAsInt(String key, int defaultValue){
    	String val = getValueAsStringAndTrim(key);
    	if (val != null){
    		return Integer.parseInt(val);
    	}
   		return defaultValue;
    }
    
    public boolean getValueAsBool(String key, boolean defaultValue){
    	String val = getValueAsStringAndTrim(key);
    	if (val == null){
    		return defaultValue;
    	}
    	return ("true".equalsIgnoreCase(val) || "T".equalsIgnoreCase(val));
    }

    public Properties getProperties() {
    	return props;
    }
    
    private void addProperties(Properties inProps)  {
    	this.props.putAll(inProps);
    }
    
    // load properties
    public static void init(String file) throws Exception{
    	log.info("Initializing config...");
    	
    	Config config = getInstance();
    	
    	Properties fileProps = config.getPropsFromFile(file);
    	
    	config.addProperties(fileProps); //TODO Refactor this

    	ConnectionUtil.initializeDataSource(fileProps);
    	
    	log.info("Config initialization completed!");
    }

 // load properties
    public static void init(Properties props) throws Exception{
    	log.info("Initializing config...");
    	Config config = getInstance();
    	config.addProperties(props); //TODO Refactor this
    	ConnectionUtil.initializeDataSource(props);
    	ConnectionUtil.initializeDataSource(props);
    	log.info("Config initialization completed!");
    }

    
    private Properties getPropsFromDB(String query){
    	if(query==null) {
    		return null; 
    	}
    	Connection conn = ConnectionUtil.getConnection();
    	
    	try {
        	log.info("Loading props from DB...");
        	Properties props = new Properties();
        	Statement stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(query);
    		while (rs.next()){
    			String id = rs.getString(1);
    			String val = rs.getString(2);
    			props.put(id, val);
    		}
    		rs.close();
    		stmt.close();
    		
        	log.info("Finished loading props from DB");
        	return props;
    	} catch (Exception e){
    		throw new RuntimeException(e);
    	} finally {
    		ConnectionUtil.closeConnection(conn);
    	}
    }
    
    private Properties getPropsFromFile(String configFile){
	    Properties props = new Properties();
   		try {
   	    	FileInputStream input = new FileInputStream(configFile);
			props.load(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	log.info("Done loading props from file.");
	    return props;
    }

    private Properties getPropsFromFile(){
    	ClassLoader cl = this.getClass().getClassLoader();
    	String configFile = "application.properties";
    	URL url = cl.getResource(configFile);

    	log.info("Loading props from file: " + url);
    	InputStream input = cl.getResourceAsStream(configFile);
    	
	    Properties props = new Properties();
   		try {
			props.load(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	log.info("Done loading props from file.");
	    return props;
    }
}
