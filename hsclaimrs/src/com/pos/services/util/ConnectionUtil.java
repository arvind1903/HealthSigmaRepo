package com.pos.services.util;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.posidex.customanttasks.StringEncrypter;

public class ConnectionUtil {
	private static Log log = LogFactory.getLog(ConnectionUtil.class);

	private static ComboPooledDataSource dataSource = null; 
	
	public static void initializeDataSource(Properties properties) throws Exception {
		log.info("Initializing data source...");
		dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass(properties.getProperty("db.connection.driver"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		dataSource.setJdbcUrl( properties.getProperty("db.connection.url") ); 
		dataSource.setUser(properties.getProperty("db.connection.userName")); 
		String deryptedPassword = StringEncrypter.decrypt(properties.getProperty("db.connection.password"));
		//String password = properties.getProperty("mrws.db.connection.passwordInText");
		//TODO unencrypted password is being used
			dataSource.setPassword(deryptedPassword);
			/*
			 * added the following properties to handle connection timeout issue
			 */
			dataSource.setTestConnectionOnCheckout(true);
			dataSource.setPreferredTestQuery("SELECT 1");
			dataSource.setMinPoolSize(Integer.parseInt(properties.getProperty("db.connection.minconnectionpool")));
			dataSource.setAcquireIncrement(Integer.parseInt(properties.getProperty("db.connection.acquireincrement")));
			dataSource.setMaxPoolSize(Integer.parseInt(properties.getProperty("db.connection.maxconnectionpool")));
			dataSource.setMaxIdleTimeExcessConnections(Integer.parseInt(properties.getProperty("db.connection.maxIdleTime")));
			dataSource.setIdleConnectionTestPeriod(300);
			log.info("Data source initialization completed!");
	}
		
  	public static Connection getConnection(){
  		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
  	
  	public static void closeConnection(Connection conn){
  		if (conn != null){
	  		try {
	  			conn.close();
	  		} catch (Exception e){
				throw new RuntimeException(e);
	  		}
  		}
  	}
  	
  	public static void closeStatement(Statement stmt)
  	{
  		if (stmt != null){
	  		try {
	  			stmt.close();
	  		} catch (Exception e){
				throw new RuntimeException(e);
	  		}
  		}
  	}

	public static void closeStatement(PreparedStatement stmt)
  	{
  		if (stmt != null){
	  		try {
	  			stmt.close();
	  		} catch (Exception e){
				throw new RuntimeException(e);
	  		}
  		}
  	}

  	
	public static void closeResultSet(ResultSet rs)
  	{
  		if (rs != null){
	  		try {
	  			rs.close();
	  		} catch (Exception e){
				throw new RuntimeException(e);
	  		}
  		}
  	}


 	public static void closeDBHandlers(ResultSet rs,Statement stmt,Connection conn)
  	{
  		try {
  		if (rs != null){
	  		rs.close();
  		}
  		if (stmt != null){
	  		stmt.close();
  		}
  		if (conn != null){
	  		conn.close();
  		}
  		} catch (Exception e){
		throw new RuntimeException(e);
		}
  	}

	public static void closeDBHandlers(ResultSet rs,PreparedStatement stmt,Connection conn)
  	{
  		try {
  		if (rs != null){
	  		rs.close();
  		}
  		if (stmt != null){
	  		stmt.close();
  		}
  		if (conn != null){
	  		conn.close();
  		}
  		} catch (Exception e){
		throw new RuntimeException(e);
		}
  	}

 	
  	public static void closeDBHandlers(ResultSet rs,Statement stmt)
  	{
  		try {
  		if (rs != null){
	  		rs.close();
  		}
  		if (stmt != null){
	  		stmt.close();
  		}
  	    } catch (Exception e){
		throw new RuntimeException(e);
		}
  	}

  	public static void closeDBHandlers(ResultSet rs,PreparedStatement stmt)
  	{
  		try {
  		if (rs != null){
	  		rs.close();
  		}
  		if (stmt != null){
	  		stmt.close();
  		}
  	    } catch (Exception e){
		throw new RuntimeException(e);
		}
  	}

  	
  	public static int getDBType(){
  		return 2;
  	}
}
