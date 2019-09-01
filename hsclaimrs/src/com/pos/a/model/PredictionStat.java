package com.pos.a.model;



public class PredictionStat {


private String healthPlan;
private String employerGroup;
private String product;
private PredictionAccuracy reviewjsonconfig ;
private PredictionAccuracy auditjsonconfig ;
private String clientcd;
public String getClientcd() {
	return clientcd;
}
public void setClientcd(String clientcd) {
	this.clientcd = clientcd;
}
public PredictionAccuracy getReviewjsonconfig() {
	return reviewjsonconfig;
}
public void setReviewjsonconfig(PredictionAccuracy reviewjsonconfig) {
	this.reviewjsonconfig = reviewjsonconfig;
}
public PredictionAccuracy getAuditjsonconfig() {
	return auditjsonconfig;
}
public void setAuditjsonconfig(PredictionAccuracy auditjsonconfig) {
	this.auditjsonconfig = auditjsonconfig;
}
public String getHealthPlan() {
	return healthPlan;
}
public void setHealthPlan(String healthPlan) {
	this.healthPlan = healthPlan;
}
public String getEmployerGroup() {
	return employerGroup;
}
public void setEmployerGroup(String employerGroup) {
	this.employerGroup = employerGroup;
}
public String getProduct() {
	return product;
}
public void setProduct(String product) {
	this.product = product;
}

}
