package com.pos.services.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.pos.hs.model.ClientSolution;
import com.pos.hs.model.Menus;
import com.pos.hs.model.PageAccess;
import com.pos.hs.model.UserModel;
import com.pos.hs.model.UserPermissions;
import com.pos.hs.model.UserProfile;
import com.pos.services.util.ConnectionUtil;
import com.pos.services.util.PosidexRandom;
import com.posidex.dao.impl.jdbc.AbstractJDBCDAO;
import com.posidex.queueutil.dao.DAOException;

public class JDBCUtilAdminMysql extends AbstractJDBCDAO {

	private static final Log logger = LogFactory.getLog(JDBCUtilAdminMysql.class.getName());

	public static UserModel validateUserNamePassword(String userName, String password) throws Exception {
		UserModel user = null;
		try
		{
			
			user = getUser(userName);
		
		if (user != null && user.getUserName() != null) {
			if (!user.matchesPassword(password)) {
				if (!user.getUserType().equalsIgnoreCase("S")) {
					JDBCUtilAdminMysql.updateFailedAttempts(user, false);
				}
				user.setStatus("failed");
				user.setLoginStatus("N");
				return user;
			} else if (user.matchesPassword(password)) {
				user.setStatus("success");
				user.setLoginStatus("Y");
				JDBCUtilAdminMysql.updateFailedAttempts(user, true);
				return user;
			}
		}
		
		}catch(Exception ex)
		{
			
			throw ex;
		}
		return user;
	}

	public static UserModel getUser(String username) throws Exception {

		Connection conn = null;
		PreparedStatement pStatement = null;

		ResultSet rs = null;
		UserModel user = new UserModel();

		try {
			conn = ConnectionUtil.getConnection();
			pStatement = conn.prepareStatement("select * from hs_users where user_name=? ");
			pStatement.setString(1, username);

			rs = pStatement.executeQuery();
			while (rs.next()) {
				user.setSaltPassword(rs.getString("salt_password"));
				user.setHashPassword(rs.getString("hash_password"));
				user.setUserId(rs.getInt("user_id"));
				user.setUserName(rs.getString("user_name"));
				user.setFirstName(rs.getString("first_name"));
				user.setLastName(rs.getString("last_name"));
				user.setEmail(rs.getString("email"));
				user.setUserType(rs.getString("status"));
				user.setClientId(rs.getInt("client_id"));
				user.setLastLoggedIn(rs.getString("last_logged_in"));
				user.setUserLandingPage(rs.getString("landing_page"));
				user.setClientCd(rs.getString("client_cd"));
				user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
				user.setDepartmentName(rs.getString("department_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		finally {
			ConnectionUtil.closeStatement(pStatement);
			ConnectionUtil.closeConnection(conn);
		}
		return user;
	}

	public static int updateFailedAttempts(UserModel model, boolean isPwdMatched) throws DAOException {
		logger.debug("Entered into updateFailedAttempts() of JDBCUtilAdminMysql class with userid::" + model.getUserId()
				+ ", isPwdMatched::" + isPwdMatched);
		int updatCt = 0;
		Connection connection = null;
		PreparedStatement pstmt = null;
		try {
			connection = ConnectionUtil.getConnection();
			if (isPwdMatched) {
				pstmt = connection.prepareStatement("UPDATE hs_user set failed_login_attempts=? where USER_ID=?");
				pstmt.setInt(1, 0);
				pstmt.setInt(2, model.getUserId());
				updatCt = pstmt.executeUpdate();

			} else {
				pstmt = connection.prepareStatement("UPDATE hs_user set failed_login_attempts=? where USER_ID=?");
				pstmt.setInt(1, model.getFailedLoginAttempts() + 1);
				pstmt.setInt(2, model.getUserId());
				updatCt = pstmt.executeUpdate();
				// logger.debug("Update Count after incrementing
				// failed_login_attempts by 1::"+updatCt);
			}
		} catch (Exception e) {
			logger.error("Error while updateFailedAttempts() mehtod::" + e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (connection != null)
					connection.close();
			} catch (Exception e) {
				logger.error("Error while closing connection::" + e.getMessage());
			}
		}
		logger.debug("Leaving from updateFailedAttempts() of JDBCUserDAO class with updateCt::" + updatCt);
		return updatCt;
	}

	public static boolean updateUserLoginDetails(long userId) throws Exception {
		logger.info("in the UpdateLoginDetails() JDBCUserDAO class");

		Connection connection = null;
		PreparedStatement pstmt = null;
		PreparedStatement pstmt1 = null;
		int count = 0;
		try {
			connection = ConnectionUtil.getConnection();
			String query = "UPDATE  hs_user_log_activity SET log_out_time=current_timestamp WHERE user_id=? AND id IN( SELECT inSelect.*  FROM ( SELECT MAX(id) FROM hs_user_log_activity WHERE user_id=? )AS inSelect)";
			pstmt = connection.prepareStatement(query);
			logger.debug("query:" + query);
			pstmt.setLong(1, userId);
			pstmt.setLong(2, userId);
			count = pstmt.executeUpdate();
			String query1 = "update hs_users set last_logged_in=current_timestamp where user_id=?";
			pstmt1 = connection.prepareStatement(query1);
			pstmt1.setLong(1, userId);
			pstmt1.executeUpdate();

		} catch (Exception ex) {
			logger.error("Error while processing UpdateUserLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (pstmt1 != null)
				pstmt1.close();
			if (connection != null)
				connection.close();
		}
		logger.info("in the UpdateLoginDetails() JDBCUserDAO class");

		return count > 0 ? true : false;
	}

	public static boolean insertLoginDetails(UserModel user, boolean isMultiLogin) throws Exception {
		logger.info("in the insertLoginDetails() JDBCUserDAO class");
		int count = 0;
		boolean result = false;
		Connection connection = null;
		PreparedStatement pstmt = null;
		// String sequenceName = PsxConstants.um_user_log_activity_sequence;
		try {
			Long randomNumber = PosidexRandom.nextPasswordRandomNumber();
			user.setLoginid(randomNumber);
			connection = ConnectionUtil.getConnection();

			pstmt = connection.prepareStatement(
					"INSERT INTO hs_user_log_activity (id,user_id,log_in_time,log_out_time,status, log_in_status,entry_made_by,SYSTEM_IP) values(appnextval('hs_user_log_activity_sequence'),?,?,?,?,?,?,?)");
			pstmt.setLong(1, user.getUserId());
			pstmt.setString(2, String.valueOf(new java.sql.Timestamp(System.currentTimeMillis())));
			pstmt.setString(3, null);

			pstmt.setString(4, user.getLoginStatus());
			pstmt.setString(5, user.getStatus());

			pstmt.setString(6, null);
			// logger.info("user.getSystemIp() value:"+user.getSystemIp());
			pstmt.setString(7, user.getSystemIp());
			// logger.info("user.getSystemIp():"+user.getSystemIp());

			count = pstmt.executeUpdate();
			if (count == 1) {
				result = true;
			}
		} catch (Exception ex) {
			logger.error("Error while processing insertLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (connection != null)
				connection.close();
		}
		logger.info("out of insertLoginDetails() JDBCUserDAO class");
		// logger.debug(" result in the class JDBCUserDAO's
		// insertLoginDetails()" + result); //$NON-NLS-1$
		return result;

	}

	public static List<PageAccess> getPageAccess(UserProfile profile) throws Exception {
		logger.info("out of getPageAccess() JDBCUserDAO class");
		List<PageAccess> pageAccessList = new ArrayList<PageAccess>();
		PageAccess pageAccess = null;
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			connection = ConnectionUtil.getConnection();
			String query = "select * from hs_role_permissions where role_name=? and active='Y' and solution_name=?";
			pstmt = connection.prepareStatement(query);
			pstmt.setString(1, profile.getAttributeValue());
			pstmt.setString(2, profile.getSolutionName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pageAccess = new PageAccess();
				pageAccess.setPermId(rs.getString("perm_id"));
				pageAccess.setRoleId(rs.getString("role_id"));
				pageAccess.setrOLENAME(rs.getString("role_name"));
				pageAccess.setPage_access(rs.getString("page_access"));
				pageAccess.setMenuId(rs.getString("menu_id"));
				pageAccess.setMenuName(rs.getString("menu_name"));
				pageAccess.setPageId(rs.getString("page_id"));
				pageAccess.setPageName(rs.getString("page_name"));
				pageAccess.setRoutingUrl(rs.getString("routing_url"));
				pageAccess.setIsChildPage(rs.getString("is_child_page"));
				pageAccessList.add(pageAccess);
			}

		} catch (Exception ex) {
			logger.error("Error while processing insertLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (connection != null)
				connection.close();
		}
		return pageAccessList;
	}

	public static List<ClientSolution> getSolutions(UserModel model) throws Exception {
		logger.info("out of getSolutions() JDBCUserDAO class");
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<ClientSolution> solutionlist = new ArrayList<ClientSolution>();
		ClientSolution solution = null;
		UserProfile profile = null;
		try {
			connection = ConnectionUtil.getConnection();

			String query1 = "select * from hs_user_profile where client_cd=? and user_name=? ";
			pstmt = connection.prepareStatement(query1);
			pstmt.setString(1, model.getClientCd());
			pstmt.setString(2, model.getUserName());
			System.out.println("client_cd :: " + model.getClientCd());
			System.out.println("user :: " + model.getUserName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				solution = new ClientSolution();
				profile = new UserProfile();
				profile.setUserName(rs.getString("user_name"));
				profile.setSolutionName(rs.getString("solution_name"));
				profile.setAttributeType(rs.getString("attribute_type"));
				profile.setAttributeName(rs.getString("attribute_name"));
				profile.setAttributeValue(rs.getString("attribute_value"));
				profile.setCreatedOn(rs.getString("created_on"));
				System.out.println("PROFILE::" + new Gson().toJson(profile));
				profile.setPageAccessList(getPageAccess(profile));
				System.out.println("PROFILEss::" + new Gson().toJson(profile));
				solution.setUserProfile(profile);
				solutionlist.add(solution);
			}

		} catch (Exception ex) {
			logger.error("Error while processing insertLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (connection != null)
				connection.close();
		}
		return solutionlist;
	}

	public static List<Menus> getMenusAndPages(UserModel model) throws DAOException, SQLException {
		logger.info("out of getMenusAndPages() JDBCUtilAdminMysql class");
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Menus> menus = new ArrayList<Menus>();
		UserProfile profile = null;
		try {
			connection = ConnectionUtil.getConnection();

			String query1 = "select * from hs_user_profile where client_cd=? and user_name=? ";
			pstmt = connection.prepareStatement(query1);
			pstmt.setString(1, model.getClientCd());
			pstmt.setString(2, model.getUserName());
			System.out.println("client_cd :: " + model.getClientCd());
			System.out.println("user :: " + model.getUserName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				profile = new UserProfile();
				profile.setUserName(rs.getString("user_name"));
				profile.setSolutionName(rs.getString("solution_name"));
				profile.setAttributeType(rs.getString("attribute_type"));
				profile.setAttributeName(rs.getString("attribute_name"));
				profile.setAttributeValue(rs.getString("attribute_value"));
				profile.setCreatedOn(rs.getString("created_on"));
				System.out.println("PROFILE::" + new Gson().toJson(profile));
				menus.addAll(getMenusPageAccess(profile));

			}

		} catch (Exception ex) {
			logger.error("Error while processing insertLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (connection != null)
				connection.close();
		}
		return menus;
	}

	private static List<Menus> getMenusPageAccess(UserProfile profile) throws DAOException, SQLException {
		logger.info("in of getMenusPageAccess() JDBCUserDAO class");

		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Menus> finalmenus = new ArrayList<Menus>();
		Map<Integer,Menus> menus = new HashMap<Integer,Menus>();
		try {
			List<PageAccess> pageAccessList = new ArrayList<PageAccess>();
			PageAccess pageAccess = null;
			Menus menu = new Menus();
			connection = ConnectionUtil.getConnection();
			String query = "select * from hs_role_permissions where role_name=? and active='Y' and solution_name=?";
			pstmt = connection.prepareStatement(query);
			pstmt.setString(1, profile.getAttributeValue().trim());
			pstmt.setString(2, profile.getSolutionName().trim());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pageAccess = new PageAccess();
				menu = new Menus();
				menu.setMenuId(Integer.parseInt(rs.getString("menu_id")));
				menu.setMenuName(rs.getString("menu_name"));
				menus.put(Integer.parseInt(rs.getString("menu_id")),menu);
				pageAccess.setPermId(rs.getString("perm_id"));
				pageAccess.setRoleId(rs.getString("role_id"));
				pageAccess.setrOLENAME(rs.getString("role_name"));
				pageAccess.setPage_access(rs.getString("page_access"));
				pageAccess.setMenuId(rs.getString("menu_id"));
				pageAccess.setMenuName(rs.getString("menu_name"));
				pageAccess.setPageId(rs.getString("page_id"));
				pageAccess.setPageName(rs.getString("page_name"));
				pageAccess.setRoutingUrl(rs.getString("routing_url"));
				pageAccess.setIsChildPage(rs.getString("is_child_page"));
				pageAccessList.add(pageAccess);
			}
			for (Entry<Integer,Menus> entry: menus.entrySet()) {
				List<PageAccess> pageList = new ArrayList<PageAccess>();
				for (PageAccess page : pageAccessList) {
					if (Integer.parseInt(page.getMenuId()) == entry.getKey()
							&& page.getMenuName().trim().equalsIgnoreCase(entry.getValue().getMenuName().trim())) {
						pageList.add(page);
					}
				}
				if (pageList != null && pageList.size() > 0) {
					entry.getValue().setPages(pageList);
					if (pageList != null && pageList.size() > 1)
						entry.getValue().setIsChildAvailable("Yes");
					else
						entry.getValue().setIsChildAvailable("No");
				}

				finalmenus.add(entry.getValue());
			}

		} catch (Exception ex) {
			logger.error("Error while processing insertLoginDetails: " + ex.getMessage());//$NON-NLS-1$
			ex.printStackTrace();
			throw new DAOException("errors.internalError");//$NON-NLS-1$
		} finally {
			if (pstmt != null)
				pstmt.close();
			if (connection != null)
				connection.close();
		}
		return finalmenus;
	}
	
	public static List<UserPermissions> getmyAssignedClaims(UserModel request) throws SQLException {
		
		List<UserPermissions>  permissionlist=new ArrayList<UserPermissions>();
		UserPermissions permission=new UserPermissions();
		Connection conn=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
	
		
		try{
			System.out.println("USER ID:::"+request.getUserId());
			conn=ConnectionUtil.getConnection();
			String query="select * from hs_user_workqueue where userid=? and active='Y' and client_cd=?";
			pstmt=conn.prepareStatement(query);
			pstmt.setInt(1,request.getUserId());
			pstmt.setString(2,request.getClientCd());
			rs=pstmt.executeQuery();
			while(rs.next())
			{
				permission=new UserPermissions();
				permission.setEmpgrp(rs.getString("Employer_Group"));
				permission.setHealthplan(rs.getString("Health_Plan"));
				permission.setProduct(rs.getString("Product"));
				permission.setStatus(rs.getString("status"));
				permission.setClientCd(rs.getString("client_cd"));
				permissionlist.add(permission);
				
			}
			
		
			
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally{
			if(conn!=null)
				conn.close();
			if(pstmt!=null)
				pstmt.close();
		}
		return permissionlist;
	}
}