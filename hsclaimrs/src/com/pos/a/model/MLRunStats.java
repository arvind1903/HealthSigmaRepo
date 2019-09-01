package com.pos.a.model;

public class MLRunStats {
private String recentActivity;
private String batchid;
private String clientCd;
private String empGrp;
private String healthPlan;
private String prodcut;
private String predictionAccuracy;
private String predictionStatus;


public String getPredictionAccuracy() {
	return predictionAccuracy;
}
public void setPredictionAccuracy(String predictionAccuracy) {
	this.predictionAccuracy = predictionAccuracy;
}
public String getPredictionStatus() {
	return predictionStatus;
}
public void setPredictionStatus(String predictionStatus) {
	this.predictionStatus = predictionStatus;
}
public String getRecentActivity() {
	return recentActivity;
}
public void setRecentActivity(String recentActivity) {
	this.recentActivity = recentActivity;
}

public String getClientCd() {
	return clientCd;
}
public void setClientCd(String clientCd) {
	this.clientCd = clientCd;
}
public String getEmpGrp() {
	return empGrp;
}
public void setEmpGrp(String empGrp) {
	this.empGrp = empGrp;
}
public String getHealthPlan() {
	return healthPlan;
}
public void setHealthPlan(String healthPlan) {
	this.healthPlan = healthPlan;
}
public String getProdcut() {
	return prodcut;
}
public void setProdcut(String prodcut) {
	this.prodcut = prodcut;
}
public String getBatchid() {
	return batchid;
}
public void setBatchid(String batchid) {
	this.batchid = batchid;
}
}
