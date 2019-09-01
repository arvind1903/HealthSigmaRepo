package com.pos.login.model;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
	private String userName;
	private String solutionName;
	private String attributeType;
	private String attributeName;
	private String attributeValue;
	private String createdOn;
	private List<PageAccess> pageAccessList=new ArrayList<PageAccess>();
	public List<PageAccess> getPageAccessList() {
		return pageAccessList;
	}
	public void setPageAccessList(List<PageAccess> pageAccessList) {
		this.pageAccessList = pageAccessList;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getSolutionName() {
		return solutionName;
	}
	public void setSolutionName(String solutionName) {
		this.solutionName = solutionName;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeValue() {
		return attributeValue;
	}
	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
}
