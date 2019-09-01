package com.pos.a.model;

import com.pos.login.model.LoginResponse;
public class TrainingGroupRequest {
	private String providerId;
	private String  proccode;
	private String  healthplan;
	private String  claimType;
	private String  clientCode;
	public String getHealthplan() {
		return healthplan;
	}
	public void setHealthplan(String healthplan) {
		this.healthplan = healthplan;
	}
	public String getClaimType() {
		return claimType;
	}
	public void setClaimType(String claimType) {
		this.claimType = claimType;
	}
	public String getClientCode() {
		return clientCode;
	}
	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}
	private LoginResponse response;
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getProccode() {
		return proccode;
	}
	public void setProccode(String proccode) {
		this.proccode = proccode;
	}
	public LoginResponse getResponse() {
		return response;
	}
	public void setResponse(LoginResponse response) {
		this.response = response;
	}
}
