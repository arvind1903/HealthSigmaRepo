package com.pos.a.model;



public class ClaimHistory {
private ClaimHeader claimheader;
private ClaimLine claimLine;

public ClaimLine getClaimLine() {
	return claimLine;
}
public void setClaimLine(ClaimLine claimLine) {
	this.claimLine = claimLine;
}
private String createdOn;
private String claimId;
private String editedBy;
public ClaimHeader getClaimheader() {
	return claimheader;
}
public void setClaimheader(ClaimHeader claimheader) {
	this.claimheader = claimheader;
}
public String getCreatedOn() {
	return createdOn;
}
public void setCreatedOn(String createdOn) {
	this.createdOn = createdOn;
}
public String getClaimId() {
	return claimId;
}
public void setClaimId(String claimId) {
	this.claimId = claimId;
}
public String getEditedBy() {
	return editedBy;
}
public void setEditedBy(String editedBy) {
	this.editedBy = editedBy;
}
}
