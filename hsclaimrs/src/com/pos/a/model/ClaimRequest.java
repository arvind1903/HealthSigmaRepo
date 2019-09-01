package com.pos.a.model;

import com.pos.login.model.LoginResponse;

public class ClaimRequest {
private String empgrpType;
private String productType;
private String healthplanType;
private String predictionMatch;
private String allowedAmount;
private String auditStatus;
private String age;
public String getAge() {
	return age;
}
public void setAge(String age) {
	this.age = age;
}
private LoginResponse loginresponse;

public LoginResponse getLoginresponse() {
	return loginresponse;
}
public void setLoginresponse(LoginResponse loginresponse) {
	this.loginresponse = loginresponse;
}
public String getAuditStatus() {
	return auditStatus;
}
public void setAuditStatus(String auditStatus) {
	this.auditStatus = auditStatus;
}
public String getClaimStatus() {
	return claimStatus;
}
public void setClaimStatus(String claimStatus) {
	this.claimStatus = claimStatus;
}
private String claimStatus;

public String getPredictionMatch() {
	return predictionMatch;
}
public void setPredictionMatch(String predictionMatch) {
	this.predictionMatch = predictionMatch;
}
public String getAllowedAmount() {
	return allowedAmount;
}
public void setAllowedAmount(String allowedAmount) {
	this.allowedAmount = allowedAmount;
}
public String getEmpgrpType() {
	return empgrpType;
}
public void setEmpgrpType(String empgrpType) {
	this.empgrpType = empgrpType;
}
public String getProductType() {
	return productType;
}
public void setProductType(String productType) {
	this.productType = productType;
}
public String getHealthplanType() {
	return healthplanType;
}
public void setHealthplanType(String healthplanType) {
	this.healthplanType = healthplanType;
}
}
