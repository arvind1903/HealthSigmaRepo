package com.pos.a.model;

public class ClaimBOResponse {
private ClaimMoreDetails headerinfo;
private String info;
public ClaimMoreDetails getHeaderinfo() {
	return headerinfo;
}
public void setHeaderinfo(ClaimMoreDetails headerinfo) {
	this.headerinfo = headerinfo;
}
public String getInfo() {
	return info;
}
public void setInfo(String info) {
	this.info = info;
}
public String getError() {
	return error;
}
public void setError(String error) {
	this.error = error;
}
private String error;
}
