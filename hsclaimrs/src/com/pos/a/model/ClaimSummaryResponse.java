package com.pos.a.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimSummaryResponse {
List<ClaimSummary> claimsummaryList=new ArrayList<ClaimSummary>();
List<String> productList=new ArrayList<String>();
List<String> healthplanList=new ArrayList<String>();
List<String> empgrpList=new ArrayList<String>();
List<String> clientCodeList=new ArrayList<String>();
List<String> agelist=new ArrayList<String>();
List<String> amountlist=new ArrayList<String>();

public List<String> getAmountlist() {
	return amountlist;
}
public void setAmountlist(List<String> amountlist) {
	this.amountlist = amountlist;
}
public List<String> getAgelist() {
	return agelist;
}
public void setAgelist(List<String> agelist) {
	this.agelist = agelist;
}
public List<String> getClientCodeList() {
	return clientCodeList;
}
public void setClientCodeList(List<String> clientCodeList) {
	this.clientCodeList = clientCodeList;
}
public List<String> getProductList() {
	return productList;
}
public void setProductList(List<String> productList) {
	this.productList = productList;
}
public List<String> getHealthplanList() {
	return healthplanList;
}
public void setHealthplanList(List<String> healthplanList) {
	this.healthplanList = healthplanList;
}
public List<String> getEmpgrpList() {
	return empgrpList;
}
public void setEmpgrpList(List<String> empgrpList) {
	this.empgrpList = empgrpList;
}
String error;
String info;

public List<ClaimSummary> getClaimsummaryList() {
	return claimsummaryList;
}
public void setClaimsummaryList(List<ClaimSummary> claimsummaryList) {
	this.claimsummaryList = claimsummaryList;
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
 

}
