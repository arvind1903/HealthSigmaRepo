package com.pos.a.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimInfoResponse {
String error;
String info;
private ClaimHeader claimheader;
private  List<MatchingClaimLine> claimlinesmatchedlist=new ArrayList<MatchingClaimLine>();
private  List<ClaimLineInfo> claimlinesinfolist=new ArrayList<ClaimLineInfo>();
public List<ClaimLineInfo> getClaimlinesinfolist() {
	return claimlinesinfolist;
}
public void setClaimlinesinfolist(List<ClaimLineInfo> claimlinesinfolist) {
	this.claimlinesinfolist = claimlinesinfolist;
}
public List<MatchingClaimLine> getClaimlinesmatchedlist() {
	return claimlinesmatchedlist;
}
public void setClaimlinesmatchedlist(List<MatchingClaimLine> claimlinesmatchedlist) {
	this.claimlinesmatchedlist = claimlinesmatchedlist;
}
private  List<ClaimHistory> historylist=new ArrayList<ClaimHistory>();

public List<ClaimHistory> getHistorylist() {
	return historylist;
}
public void setHistorylist(List<ClaimHistory> historylist) {
	this.historylist = historylist;
}
private UserModel model;
public UserModel getModel() {
	return model;
}
public void setModel(UserModel model) {
	this.model = model;
}
private  List<ClaimLine> claimlinelist=new ArrayList<ClaimLine>();

public ClaimHeader getClaimheader() {
	return claimheader;
}
public void setClaimheader(ClaimHeader claimheader) {
	this.claimheader = claimheader;
}
public String getError() {
	return error;
}
public void setError(String error) {
	this.error = error;
}
public String getInfo() {
	return info;
}
public void setInfo(String info) {
	this.info = info;
}

public List<ClaimLine> getClaimlinelist() {
	return claimlinelist;
}
public void setClaimlinelist(List<ClaimLine> claimlinelist) {
	this.claimlinelist = claimlinelist;
}

}
