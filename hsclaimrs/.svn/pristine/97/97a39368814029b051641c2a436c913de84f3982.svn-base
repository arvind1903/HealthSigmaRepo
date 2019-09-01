package com.pos.a.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MLResults {
	private String index;
	private String AllowedAmt;
	private String sourceclmid;
	private String sourceclmline;
	private String predictions;
	private String neighbor;
	private String distance;
	private String matchclmid;
	private String matchclmline;
	private String predictionStatus;
	private String predictionType;
	private String matchGroupId;
	private String sourceRate;
	private String predictionRate;
	private BigDecimal predictionErrorPct;
	
	public String getPredictionStatus() {
		return predictionStatus;
	}
	public void setPredictionStatus(String predictionStatus) {
		this.predictionStatus = predictionStatus;
	}
	public String getPredictionType() {
		return predictionType;
	}
	public void setPredictionType(String predictionType) {
		this.predictionType = predictionType;
	}
	public String getMatchGroupId() {
		return matchGroupId;
	}
	public void setMatchGroupId(String matchGroupId) {
		this.matchGroupId = matchGroupId;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getAllowedAmt() {
		return AllowedAmt;
	}
	public void setAllowedAmt(String allowedAmt) {
		AllowedAmt = allowedAmt;
	}
	public String getSourceclmid() {
		return sourceclmid;
	}
	public void setSourceclmid(String sourceclmid) {
		this.sourceclmid = sourceclmid;
	}
	public String getSourceclmline() {
		return sourceclmline;
	}
	public void setSourceclmline(String sourceclmline) {
		this.sourceclmline = sourceclmline;
	}
	public String getPredictions() {
		return predictions;
	}
	public void setPredictions(String predictions) {
		this.predictions = predictions;
	}
	public String getNeighbor() {
		return neighbor;
	}
	public void setNeighbor(String neighbor) {
		this.neighbor = neighbor;
	}
	public String getDistance() {
		return distance;
	}
	public void setDistance(String distance) {
		this.distance = distance;
	}
	public String getMatchclmid() {
		return matchclmid;
	}
	public void setMatchclmid(String matchclmid) {
		this.matchclmid = matchclmid;
	}
	public String getMatchclmline() {
		return matchclmline;
	}
	public void setMatchclmline(String matchclmline) {
		this.matchclmline = matchclmline;
	}
	public String getSourceRate() {
		return sourceRate;
	}
	public void setSourceRate(String sourceRate) {
		this.sourceRate = sourceRate;
	}
	public String getPredictionRate() {
		return predictionRate;
	}
	public void setPredictionRate(String predictionRate) {
		this.predictionRate = predictionRate;
	}
	
public void setPredictionErrorPct(String sourceRate, String predictedRate){
	
		final BigDecimal ONE_HUNDRED = new BigDecimal(100);  
		BigDecimal sRate = new BigDecimal(sourceRate);
		BigDecimal pRate = new BigDecimal(predictedRate);
		
		// Find the difference between rates
		MathContext mc = new MathContext(3, RoundingMode.FLOOR);
		BigDecimal difference = sRate.subtract(pRate, mc);
		

		// Divide by the srate 
		MathContext mc1 = new MathContext(5, RoundingMode.FLOOR);	
		BigDecimal error = difference.abs().divide(sRate, mc1);
		

		// Convert to Pct
		MathContext mc2 = new MathContext(4, RoundingMode.DOWN);
		this.predictionErrorPct = error.multiply(ONE_HUNDRED,mc2);
		
	}
public BigDecimal getPredictionErrorPct() {
	return predictionErrorPct;
}

}
