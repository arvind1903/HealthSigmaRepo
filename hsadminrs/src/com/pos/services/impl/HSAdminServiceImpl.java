package com.pos.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.crypto.SecretKey;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.WebServiceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pos.hs.Responses.LoginResponse;
import com.pos.hs.model.ClientSolution;
import com.pos.hs.model.Menus;
import com.pos.hs.model.PageAccess;
import com.pos.hs.model.Pages;
import com.pos.hs.model.UserModel;
import com.pos.hs.model.UserPermissions;
import com.pos.services.intf.HSAdminService;
import com.pos.services.jdbc.JDBCUtilAdminMysql;
import com.pos.services.util.PasswordDecryptor;

public class HSAdminServiceImpl implements HSAdminService {

	private static final Logger logger = LogManager.getLogger(HSAdminServiceImpl.class);
	public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
	byte[] arrayBytes;
	SecretKey key;

	@Resource
	public WebServiceContext webServiceContext;

	@Context
	private org.apache.cxf.jaxrs.ext.MessageContext rsMessageContext;

	public void setWebServiceContext(WebServiceContext webServiceContext) {
		this.webServiceContext = webServiceContext;
	}

	public HSAdminServiceImpl() {

	}

	/*
	 * @Override public Response getLoginDetails(UserModel usermodel) throws
	 * Exception { logger.info(
	 * "Inside HSAdminServiceImpl class of getLoginDetails Method");
	 * LoginResponse response = new LoginResponse(); String
	 * password=com.pos.services.util.PasswordDecryptor.Decrypt(
	 * "PosidexSecretKey", usermodel.getPassword());
	 * usermodel.setPassword(password); List<Menus> menus = new
	 * ArrayList<Menus>(); List<Pages> pages = new ArrayList<Pages>(); UserModel
	 * model=new UserModel(); try{ model =
	 * JDBCUtilAdminMysql.validateUserNamePassword(usermodel);
	 * 
	 * //to do menus = JDBCUtilAdminMysql.getMenus(); pages =
	 * JDBCUtilAdminMysql.getPages();
	 * 
	 * if(model==null) { response.setError(
	 * "Please Enter a Correct UserName Or Password"); } else{
	 * if(!model.getUser_role().equalsIgnoreCase("Re_User")) {
	 * if(model.getStatus().equalsIgnoreCase("A")) {
	 * JDBCUtilAdminMysql.insertLoginDetails(model,true);
	 * response.setModel(model); response.setInfo("User is Logged Successfully"
	 * ); } else if(model.getStatus().equalsIgnoreCase("S")) {
	 * 
	 * response.setError("Your Account is InActive."); response.setModel(null);
	 * } else if(model.getStatus().equalsIgnoreCase("D")) {
	 * 
	 * response.setError("Your Account is Deleted."); response.setModel(null); }
	 * 
	 * else if(model.getStatus().equalsIgnoreCase("failed")) {
	 * response.setError("Login Failed Invalid Username or Password");
	 * response.setModel(null); } } else { response.setModel(null);
	 * response.setError("You Are Not Authorized"); } }
	 * response.setModel(model); response.getModel().setMenus(menus);
	 * response.getModel().setPages(pages); } catch(Exception e) {
	 * response.setError(e.getMessage()); } logger.info(
	 * "Outside of HSAdminServiceImpl class of getLoginDetails Method"); return
	 * Response.ok().entity(response) .status(200)
	 * .header("Access-Control-Allow-Origin", "http://localhost:4200")
	 * .header("Access-Control-Allow-Headers",
	 * "origin, content-type, accept, authorization")
	 * .header("Access-Control-Allow-Credentials", "true")
	 * .header("Access-Control-Allow-Methods",
	 * "GET, POST, PUT, DELETE, OPTIONS, HEAD")
	 * .header("Access-Control-Max-Age",
	 * "1209600").type(MediaType.APPLICATION_JSON).build(); }
	 */

	@Override
	public Response getLoginDetails(UserModel user) throws Exception {

		LoginResponse response = new LoginResponse();
		 List<UserPermissions> claimsassginedlist=new ArrayList<UserPermissions>();
		// List<PageAccess> pageAccessList=new ArrayList<PageAccess>();
		String pass = user.getPassword();
		String userName = user.getUserName();
		String password = PasswordDecryptor.Decrypt("PosidexSecretKey", pass);
		UserModel model = new UserModel();
		List<ClientSolution> solutionList = new ArrayList<ClientSolution>();
		List<Menus> menus = new ArrayList<Menus>();
		try {
			model = JDBCUtilAdminMysql.validateUserNamePassword(userName, password);
			
			if(model.getUserName()!=null)
			{
				if (model.getStatus().equalsIgnoreCase("success") && model.getUserType().equalsIgnoreCase("A")) 
				{
					JDBCUtilAdminMysql.insertLoginDetails(model, true);
					response.setModel(model);
					solutionList = JDBCUtilAdminMysql.getSolutions(model);
					menus = JDBCUtilAdminMysql.getMenusAndPages(model);
					
					response.setSolutions(solutionList);
					response.setMenus(menus);
					System.out.println("RESPONSE::" + new Gson().toJson(response));

				} 
				else if (model.getUserType().equalsIgnoreCase("S"))
				{

					response.setError("Your Account is InActive.");
					response.setModel(null);
				} 
				else if (model.getUserType().equalsIgnoreCase("D"))
				{

					response.setError("Your Account is Deleted.");
					response.setModel(null);
				}

				else if (model.getStatus().equalsIgnoreCase("failed"))
				{
					response.setError("Login Failed Invalid Username or Password");
					response.setModel(null);
				}
					if(model!=null)
					claimsassginedlist=JDBCUtilAdminMysql.getmyAssignedClaims(model);
				
					response.setClaimsassginedlist(claimsassginedlist);
				}
			else {
				
				response.setError("Please Enter a Correct UserName Or Password");

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Inside Catch::"+e.getMessage());
			response.setError(e.getMessage());
			return Response.ok().entity(response).status(200)
					.header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		return Response.ok().entity(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
	}

	@Override
	public Response logoutUser(String userid) throws Exception {
		LoginResponse response = new LoginResponse();
		String status = "";
		try {

			if (JDBCUtilAdminMysql.updateUserLoginDetails(Long.parseLong(userid))) {
				status = "success";
				response.setStatus("success");
				response.setInfo("User Logged Out Successfully");
			} else {
				status = "failed";
				response.setStatus("failed");
				response.setInfo("User failed to logout");

			}
		} catch (Exception e) {
			response.setError(e.getMessage());
			return Response.ok(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
					.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
					.header("Access-Control-Allow-Credentials", "true")
					.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
					.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();
		}
		response.setStatus(status);
		return Response.ok(response).status(200).header("Access-Control-Allow-Origin", "http://localhost:4200")
				.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
				.header("Access-Control-Allow-Credentials", "true")
				.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
				.header("Access-Control-Max-Age", "1209600").type(MediaType.APPLICATION_JSON).build();

	}
	
	@Override
	public Response getUserMenuPages(String userId) throws Exception {
		System.out.println(" get User menu pages");
		return null;
	}
}
