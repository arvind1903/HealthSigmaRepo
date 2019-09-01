package com.pos.services.intf;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Service;

import com.pos.hs.model.UserModel;



@Path("/hsadminservice/")
@Service
@Consumes("application/json")
@Produces("application/json")
public interface HSAdminService {

		
	@POST
	@Path("/login")
	@Produces("application/json")
	public Response getLoginDetails(UserModel usermodel)throws Exception;

	@GET
	@Path("/logout")
	@Produces("application/json")
	public Response logoutUser(@QueryParam("userId")String userId) throws Exception;
	
	@GET
	@Path("/getUserMenuPages")
	@Produces("application/json")
	public Response getUserMenuPages(@QueryParam("userId")String userId) throws Exception;
	
	@Path("/checkUser")
	@Produces("application/json")
	public Response checkUser(@QueryParam("userId")String userId) throws Exception;
}