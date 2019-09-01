package com.pos.a.model;

public class ClaimDecision {
	private String decision;
private String remarks;
private String claimDecisionType;
private UserModel user;
private ClaimInfoResponse claiminforeponse;
private String reason;
private String claimnavtype;
public String getClaimnavtype() {
	return claimnavtype;
}
public void setClaimnavtype(String claimnavtype) {
	this.claimnavtype = claimnavtype;
}
public String getReason() {
	return reason;
}
public void setReason(String reason) {
	this.reason = reason;
}
public ClaimInfoResponse getClaiminforeponse() {
	return claiminforeponse;
}
public void setClaiminforeponse(ClaimInfoResponse claiminforeponse) {
	this.claiminforeponse = claiminforeponse;
}
public String getDecision() {
	return decision;
}
public void setDecision(String decision) {
	this.decision = decision;
}
public String getRemarks() {
	return remarks;
}
public void setRemarks(String remarks) {
	this.remarks = remarks;
}
public String getClaimDecisionType() {
	return claimDecisionType;
}
public void setClaimDecisionType(String claimDecisionType) {
	this.claimDecisionType = claimDecisionType;
}
public UserModel getUser() {
	return user;
}
public void setUser(UserModel user) {
	this.user = user;
}

}
