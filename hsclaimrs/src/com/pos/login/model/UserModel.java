package com.pos.login.model;

import java.util.List;



public class UserModel {
	private int mobile;

	private String userLandingPage;
	private String clientCd;
	private String roleName;
	private String departmentName;

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getClientCd() {
		return clientCd;
	}

	public void setClientCd(String clientCd) {
		this.clientCd = clientCd;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	private String updatedOn;

	private int updatedBy;

	private String jobTitle;

	private String userRole;

	private String dataSource;

	private int deletedBy;

	private String deletedOn;

	private String activationDate;

	private String CREATEDUSERNAME;

	private int userId ;

	private String userName;

	private String	password;
	
	private String hashPassword;

	private String saltPassword;

	private String email;

	private String firstName;

	private String lastName;

	private int clientId ;

	private int roleId ;

	private int createdBy ;

	private String createdOn;

	private String Status;

	private String lastLoggedIn;

	private int failedLoginAttempts;
	
	private Long loginid;
	
	private String	loginStatus;
	
	private String systemIp;
	private String userType;
	
	

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	
	
	private List<Menus> menus;
	
	private List<Pages> pages;



	public int getMobile() {
		return mobile;
	}

	public void setMobile(int mobile) {
		this.mobile = mobile;
	}

	public String getUserLandingPage() {
		return userLandingPage;
	}

	public void setUserLandingPage(String userLandingPage) {
		this.userLandingPage = userLandingPage;
	}

	public String getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public int getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(int deletedBy) {
		this.deletedBy = deletedBy;
	}

	public String getDeletedOn() {
		return deletedOn;
	}

	public void setDeletedOn(String deletedOn) {
		this.deletedOn = deletedOn;
	}

	public String getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	public String getCREATEDUSERNAME() {
		return CREATEDUSERNAME;
	}

	public void setCREATEDUSERNAME(String cREATEDUSERNAME) {
		CREATEDUSERNAME = cREATEDUSERNAME;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(String hashPassword) {
		this.hashPassword = hashPassword;
	}

	public String getSaltPassword() {
		return saltPassword;
	}

	public void setSaltPassword(String saltPassword) {
		this.saltPassword = saltPassword;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}

	public String getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(String lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public Long getLoginid() {
		return loginid;
	}

	public void setLoginid(Long loginid) {
		this.loginid = loginid;
	}

	public String getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	public String getSystemIp() {
		return systemIp;
	}

	public void setSystemIp(String systemIp) {
		this.systemIp = systemIp;
	}

	public List<Menus> getMenus() {
		return menus;
	}

	public void setMenus(List<Menus> menus) {
		this.menus = menus;
	}

	public List<Pages> getPages() {
		return pages;
	}

	public void setPages(List<Pages> pages) {
		this.pages = pages;
	}

	@Override
	public String toString() {
		return "UserModel [mobile=" + mobile + ", userLandingPage=" + userLandingPage + ", updatedOn=" + updatedOn
				+ ", updatedBy=" + updatedBy + ", jobTitle=" + jobTitle + ", userRole=" + userRole + ", dataSource="
				+ dataSource + ", deletedBy=" + deletedBy + ", deletedOn=" + deletedOn + ", activationDate="
				+ activationDate + ", CREATEDUSERNAME=" + CREATEDUSERNAME + ", userId=" + userId + ", userName="
				+ userName + ", password=" + password + ", hashPassword=" + hashPassword + ", saltPassword="
				+ saltPassword + ", email=" + email + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", clientId=" + clientId + ", roleId=" + roleId + ", createdBy=" + createdBy + ", createdOn="
				+ createdOn + ", Status=" + Status + ", lastLoggedIn=" + lastLoggedIn + ", failedLoginAttempts="
				+ failedLoginAttempts + ", loginid=" + loginid + ", loginStatus=" + loginStatus + ", systemIp="
				+ systemIp + ", userType=" + userType + ", menus=" + menus + ", pages=" + pages + "]";
	}

	

	
	
	
}