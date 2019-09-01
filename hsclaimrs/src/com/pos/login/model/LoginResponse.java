package com.pos.login.model;

import java.util.ArrayList;
import java.util.List;



public class LoginResponse {
	private List<ClientSolution> solutions=new ArrayList<ClientSolution>();
	private List<UserAuthorizedWorkQueue> claimsassginedlist=new ArrayList<UserAuthorizedWorkQueue>();
	public List<UserAuthorizedWorkQueue> getClaimsassginedlist() {
		return claimsassginedlist;
	}

	public void setClaimsassginedlist(List<UserAuthorizedWorkQueue> claimsassginedlist) {
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
	
	
}
