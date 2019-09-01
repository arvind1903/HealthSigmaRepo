package com.pos.hs.Responses;

import java.util.ArrayList;
import java.util.List;

import com.pos.hs.model.ClientSolution;
import com.pos.hs.model.Menus;
import com.pos.hs.model.PageAccess;
import com.pos.hs.model.UserModel;
import com.pos.hs.model.UserPermissions;

public class LoginResponse {
	private List<ClientSolution> solutions=new ArrayList<ClientSolution>();
	private List<Menus> menus=new ArrayList<Menus>();

	private List<UserPermissions> claimsassginedlist=new ArrayList<UserPermissions>();
	public List<UserPermissions> getClaimsassginedlist() {
		return claimsassginedlist;
	}

	public void setClaimsassginedlist(List<UserPermissions> claimsassginedlist) {
		this.claimsassginedlist = claimsassginedlist;
	}

	public List<ClientSolution> getSolutions() {
		return solutions;
	}

	public void setSolutions(List<ClientSolution> solutions) {
		this.solutions = solutions;
	}

	private UserModel model;
	
	private String status;
	
	private String info;
	
	private String error;

	public UserModel getModel() {
		return model;
	}

	public void setModel(UserModel model) {
		this.model = model;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "LoginResponse [model=" + model + ", status=" + status + ", info=" + info + "]";
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public List<Menus> getMenus() {
		return menus;
	}

	public void setMenus(List<Menus> menus) {
		this.menus = menus;
	}
	
	
}
