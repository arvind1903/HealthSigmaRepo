package com.pos.a.model;


public class ClaimHeader {

	private String akid;
	private String clientCd;
	private String claimId;
	private String claimFromDate;
	private String claimToDate;
	private String claimCategory;
	private String claimCurrentStatus;
	private String healthPlan;
	private String product;
	private String employeeGroup;
	private String npi;
	private String patientName;
	private String patientBirthDate;
	private String patientGender;
	private String insuradId;
	private String insuredAddress;
	private String insuredBirthDate;
	private String drg;
	private String billedAmt;
	private double allowedAmt;
	private String dispositionStatus;
	private String filler1;
	private String filler2;
	private String filler3;
	private String filler4;
	private String filler5;
	private String createdOn;
	private String createdBy;
	private String isNewClaim;
	private String predictionStatus;
	private String auditStatus;
	private String  lockedBy;
	private String  departmentName;
	private double  finalAmt;
	private String  reason;
	//only for display purpose
	private String  lockedByUserName;
	
	public String getLockedByUserName() {
		return lockedByUserName;
	}
	public void setLockedByUserName(String lockedByUserName) {
		this.lockedByUserName = lockedByUserName;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public String getLockedBy() {
		return lockedBy;
	}
	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}
	public String getPredictionStatus() {
		return predictionStatus;
	}
	public void setPredictionStatus(String predictionStatus) {
		this.predictionStatus = predictionStatus;
	}
	public String getAuditStatus() {
		return auditStatus;
	}
	public void setAuditStatus(String auditStatus) {
		this.auditStatus = auditStatus;
	}
	public String getAkid() {
		return akid;
	}
	public void setAkid(String akid) {
		this.akid = akid;
	}
	public String getBilledAmt() {
		return billedAmt;
	}
	public void setBilledAmt(String billedAmt) {
		this.billedAmt = billedAmt;
	}
	
	public double getAllowedAmt() {
		return allowedAmt;
	}
	public void setAllowedAmt(double allowedAmt) {
		this.allowedAmt = allowedAmt;
	}
	public double getFinalAmt() {
		return finalAmt;
	}
	public void setFinalAmt(double finalAmt) {
		this.finalAmt = finalAmt;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public String getClientCd() {
		return clientCd;
	}
	public void setClientCd(String clientCd) {
		this.clientCd = clientCd;
	}
	public String getClaimId() {
		return claimId;
	}
	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}
	public String getClaimFromDate() {
		return claimFromDate;
	}
	public void setClaimFromDate(String claimFromDate) {
		this.claimFromDate = claimFromDate;
	}
	public String getClaimToDate() {
		return claimToDate;
	}
	public void setClaimToDate(String claimToDate) {
		this.claimToDate = claimToDate;
	}
	public String getClaimCategory() {
		return claimCategory;
	}
	public void setClaimCategory(String claimCategory) {
		this.claimCategory = claimCategory;
	}
	public String getClaimCurrentStatus() {
		return claimCurrentStatus;
	}
	public void setClaimCurrentStatus(String claimCurrentStatus) {
		this.claimCurrentStatus = claimCurrentStatus;
	}
	public String getHealthPlan() {
		return healthPlan;
	}
	public void setHealthPlan(String healthPlan) {
		this.healthPlan = healthPlan;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public String getEmployeeGroup() {
		return employeeGroup;
	}
	public void setEmployeeGroup(String employeeGroup) {
		this.employeeGroup = employeeGroup;
	}
	public String getNpi() {
		return npi;
	}
	public void setNpi(String npi) {
		this.npi = npi;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getPatientBirthDate() {
		return patientBirthDate;
	}
	public void setPatientBirthDate(String patientBirthDate) {
		this.patientBirthDate = patientBirthDate;
	}
	public String getPatientGender() {
		return patientGender;
	}
	public void setPatientGender(String patientGender) {
		this.patientGender = patientGender;
	}
	public String getInsuradId() {
		return insuradId;
	}
	public void setInsuradId(String insuradId) {
		this.insuradId = insuradId;
	}
	public String getInsuredAddress() {
		return insuredAddress;
	}
	public void setInsuredAddress(String insuredAddress) {
		this.insuredAddress = insuredAddress;
	}
	public String getInsuredBirthDate() {
		return insuredBirthDate;
	}
	public void setInsuredBirthDate(String insuredBirthDate) {
		this.insuredBirthDate = insuredBirthDate;
	}
	public String getDrg() {
		return drg;
	}
	public void setDrg(String drg) {
		this.drg = drg;
	}
	
	public String getDispositionStatus() {
		return dispositionStatus;
	}
	public void setDispositionStatus(String dispositionStatus) {
		this.dispositionStatus = dispositionStatus;
	}
	public String getFiller1() {
		return filler1;
	}
	public void setFiller1(String filler1) {
		this.filler1 = filler1;
	}
	public String getFiller2() {
		return filler2;
	}
	public void setFiller2(String filler2) {
		this.filler2 = filler2;
	}
	public String getFiller3() {
		return filler3;
	}
	public void setFiller3(String filler3) {
		this.filler3 = filler3;
	}
	public String getFiller4() {
		return filler4;
	}
	public void setFiller4(String filler4) {
		this.filler4 = filler4;
	}
	public String getFiller5() {
		return filler5;
	}
	public void setFiller5(String filler5) {
		this.filler5 = filler5;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getIsNewClaim() {
		return isNewClaim;
	}
	public void setIsNewClaim(String isNewClaim) {
		this.isNewClaim = isNewClaim;
	}
	
	@Override
	public String toString() {
		return "ClaimHeader [akid=" + akid + ", clientCd=" + clientCd + ", claimId=" + claimId + ", claimFromDate="
				+ claimFromDate + ", claimToDate=" + claimToDate + ", claimCategory=" + claimCategory
				+ ", claimCurrentStatus=" + claimCurrentStatus + ", healthPlan=" + healthPlan + ", product=" + product
				+ ", employeeGroup=" + employeeGroup + ", npi=" + npi + ", patientName=" + patientName
				+ ", patientBirthDate=" + patientBirthDate + ", patientGender=" + patientGender + ", insuradId="
				+ insuradId + ", insuredAddress=" + insuredAddress + ", insuredBirthDate=" + insuredBirthDate + ", drg="
				+ drg + ", billedAmt=" + billedAmt + ", allowedAmt=" + allowedAmt + ", dispositionStatus="
				+ dispositionStatus + ", filler1=" + filler1 + ", filler2=" + filler2 + ", filler3=" + filler3
				+ ", filler4=" + filler4 + ", filler5=" + filler5 + ", createdOn=" + createdOn + ", createdBy="
				+ createdBy + ", isNewClaim=" + isNewClaim + "]";
	}	
}