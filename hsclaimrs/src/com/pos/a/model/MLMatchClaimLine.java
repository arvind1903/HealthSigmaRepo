package com.pos.a.model;

import java.math.BigDecimal;

public class MLMatchClaimLine {
	
	private String id;
	private String batchid;
	private String clientcd;
	private String healthPlan;
	private String product;
	private String employerGroup;
	
	//source claim info
	private String sourceClmId;
	private String sourceClmline;
	private String sourceProvider;
	private String sourcepos;
	private String sourceproduct;
	private String sourceproccode;
	private String sourcemodifier1;
	private String sourcemodifier2;
	private String sourcerate;
	
	//matched group and
	//sample claim from the group
	private String matchedGroupNumber;
	private String matchedClmid;
	private String matchedClmline;
	private String predictedrate;
	private String matchedProvider;
	private String matchedpos;
	private String matchedproduct;
	private String matchedproccode;
	private String matchedmodifier1;
	private String matchedmodifier2;
	private String matchedrate;
	
	//match status
	private String isprovider;
	private String ispos;
	private String isproduct;
	private String isproccode;
	private String ismodifier1;
	private String ismodifier2;
	private String israte;
	
	// prediction status
	private String distance;
	private String neighbor;
	private String predictionstatus;
	private String predictionaccuracy;
	private BigDecimal predictionVariation;
	private BigDecimal predictionErrorPct;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBatchid() {
		return batchid;
	}
	public void setBatchid(String batchid) {
		this.batchid = batchid;
	}
	public String getClientcd() {
		return clientcd;
	}
	public void setClientcd(String clientcd) {
		this.clientcd = clientcd;
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
	public String getEmployerGroup() {
		return employerGroup;
	}
	public void setEmployerGroup(String employerGroup) {
		this.employerGroup = employerGroup;
	}
	public String getSourceClmId() {
		return sourceClmId;
	}
	public void setSourceClmId(String sourceClmId) {
		this.sourceClmId = sourceClmId;
	}
	public String getSourceClmline() {
		return sourceClmline;
	}
	public void setSourceClmline(String sourceClmline) {
		this.sourceClmline = sourceClmline;
	}
	public String getSourceProvider() {
		return sourceProvider;
	}
	public void setSourceProvider(String sourceProvider) {
		this.sourceProvider = sourceProvider;
	}
	public String getSourcepos() {
		return sourcepos;
	}
	public void setSourcepos(String sourcepos) {
		this.sourcepos = sourcepos;
	}
	public String getSourceproduct() {
		return sourceproduct;
	}
	public void setSourceproduct(String sourceproduct) {
		this.sourceproduct = sourceproduct;
	}
	public String getSourceproccode() {
		return sourceproccode;
	}
	public void setSourceproccode(String sourceproccode) {
		this.sourceproccode = sourceproccode;
	}
	public String getSourcemodifier1() {
		return sourcemodifier1;
	}
	public void setSourcemodifier1(String sourcemodifier1) {
		this.sourcemodifier1 = sourcemodifier1;
	}
	public String getSourcemodifier2() {
		return sourcemodifier2;
	}
	public void setSourcemodifier2(String sourcemodifier2) {
		this.sourcemodifier2 = sourcemodifier2;
	}
	public String getSourcerate() {
		return sourcerate;
	}
	public void setSourcerate(String sourcerate) {
		this.sourcerate = sourcerate;
	}
	public String getMatchedGroupNumber() {
		return matchedGroupNumber;
	}
	public void setMatchedGroupNumber(String matchedGroupNumber) {
		this.matchedGroupNumber = matchedGroupNumber;
	}
	public String getMatchedClmid() {
		return matchedClmid;
	}
	public void setMatchedClmid(String matchedClmid) {
		this.matchedClmid = matchedClmid;
	}
	public String getMatchedClmline() {
		return matchedClmline;
	}
	public void setMatchedClmline(String matchedClmline) {
		this.matchedClmline = matchedClmline;
	}
	public String getPredictedrate() {
		return predictedrate;
	}
	public void setPredictedrate(String predictedrate) {
		this.predictedrate = predictedrate;
	}
	public String getMatchedProvider() {
		return matchedProvider;
	}
	public void setMatchedProvider(String matchedProvider) {
		this.matchedProvider = matchedProvider;
	}
	public String getMatchedpos() {
		return matchedpos;
	}
	public void setMatchedpos(String matchedpos) {
		this.matchedpos = matchedpos;
	}
	public String getMatchedproduct() {
		return matchedproduct;
	}
	public void setMatchedproduct(String matchedproduct) {
		this.matchedproduct = matchedproduct;
	}
	public String getMatchedproccode() {
		return matchedproccode;
	}
	public void setMatchedproccode(String matchedproccode) {
		this.matchedproccode = matchedproccode;
	}
	public String getMatchedmodifier1() {
		return matchedmodifier1;
	}
	public void setMatchedmodifier1(String matchedmodifier1) {
		this.matchedmodifier1 = matchedmodifier1;
	}
	public String getMatchedmodifier2() {
		return matchedmodifier2;
	}
	public void setMatchedmodifier2(String matchedmodifier2) {
		this.matchedmodifier2 = matchedmodifier2;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getNeighbor() {
		return neighbor;
	}
	public void setNeighbor(String neighbor) {
		this.neighbor = neighbor;
	}
	public String getPredictionstatus() {
		return predictionstatus;
	}
	public void setPredictionstatus(String predictionstatus) {
		this.predictionstatus = predictionstatus;
	}
	public String getPredictionaccuracy() {
		return predictionaccuracy;
	}
	public void setPredictionaccuracy(String predictionaccuracy) {
		this.predictionaccuracy = predictionaccuracy;
	}
	public BigDecimal getPredictionVariation() {
		return predictionVariation;
	}
	public void setPredictionVariation(BigDecimal predictionVariation) {
		this.predictionVariation = predictionVariation;
	}
	public BigDecimal getPredictionErrorPct() {
		return predictionErrorPct;
	}
	public void setPredictionErrorPct(BigDecimal predictionErrorPct) {
		this.predictionErrorPct = predictionErrorPct;
	}
	public String getMatchedrate() {
		return matchedrate;
	}
	public void setMatchedrate(String matchedrate) {
		this.matchedrate = matchedrate;
	}
	public String getIsprovider() {
		return isprovider;
	}
	public void setIsprovider(String isprovider) {
		this.isprovider = isprovider;
	}
	public String getIspos() {
		return ispos;
	}
	public void setIspos(String ispos) {
		this.ispos = ispos;
	}
	public String getIsproduct() {
		return isproduct;
	}
	public void setIsproduct(String isproduct) {
		this.isproduct = isproduct;
	}
	public String getIsproccode() {
		return isproccode;
	}
	public void setIsproccode(String isproccode) {
		this.isproccode = isproccode;
	}
	public String getIsmodifier1() {
		return ismodifier1;
	}
	public void setIsmodifier1(String ismodifier1) {
		this.ismodifier1 = ismodifier1;
	}
	public String getIsmodifier2() {
		return ismodifier2;
	}
	public void setIsmodifier2(String ismodifier2) {
		this.ismodifier2 = ismodifier2;
	}
	public String getIsrate() {
		return israte;
	}
	public void setIsrate(String israte) {
		this.israte = israte;
	}
	
	
	
	
	
	
	}
