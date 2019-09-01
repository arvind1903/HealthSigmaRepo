package com.pos.listener;

import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pos.services.util.Config;



public class ApplicationContextListener implements ServletContextListener
{
	/** The Constant logger. */
	private static final Log	logger							= LogFactory.getLog(ApplicationContextListener.class.getName());
	private static final String	CONFIG_DIR_PARAM				= "config_dir";
	private static final String	LOG4J_CONFIGURATION_FILENAME	= "log4j.xml";
	private static final String	APPLICATION_PROPERTY_FILENAME	= "application.properties";
	private static Properties properties=new Properties();
	
	
	/**
	 * @return the properties
	 */
	public static Properties getProperties() {
		return properties;
	}

	public void contextInitialized( ServletContextEvent contextEvent )
	{
		logger.debug("In contextInitialized() method.");
		try
		{
			ServletContext servletContext = contextEvent.getServletContext();
			String configDir = servletContext.getInitParameter(CONFIG_DIR_PARAM);
			logger.debug(configDir);
			String log4jFile = configDir + "/" + LOG4J_CONFIGURATION_FILENAME;
			String applicationPropertyFile = configDir + "/" + APPLICATION_PROPERTY_FILENAME;
			/*Application configuration*/
			initApplication(applicationPropertyFile,servletContext);
			
			//to-do cache approach
			
		}
		catch (Exception e)
		{
			logger.error("error in contextInitialized"+e);
			throw new RuntimeException(e);
		}
	}

	
	private void initApplication( String applicationPropertyFile, ServletContext servletContext ) throws Exception
	{
		try
		{
			Properties applicationProperties = new Properties();
			applicationProperties.load(new FileInputStream(applicationPropertyFile));
			Config.init(applicationProperties);
			/* Loading the entire application properties and placing it into application scope,which is useful
			 * to access in entire application 
			 */
			servletContext.setAttribute("webApplicationProperties", applicationProperties);
			servletContext.setAttribute("applicationProperties", applicationPropertyFile);
		}
		catch (Exception e)
		{
			logger.error("error in initApplication"+e);
			throw e;
		}
	}
	

	public void contextDestroyed( ServletContextEvent contextEvent )
	{
		logger.debug("In contextDestroyed() method.");
		ServletContext servletContext = contextEvent.getServletContext();
		logger.debug("Destroying the application.properties object from application scope.");
		servletContext.removeAttribute("webApplicationProperties");
	}
}
