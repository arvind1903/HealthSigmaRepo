package com.pos.a.model;

import java.io.Serializable;

public class PredictionResult implements Serializable{
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String batchid;
private String predictionStatus;
private String predictionAccuracy;
private String count;
public String getBatchid() {
	return batchid;
}
public void setBatchid(String batchid) {
	this.batchid = batchid;
}
public String getPredictionStatus() {
	return predictionStatus;
}
public void setPredictionStatus(String predictionStatus) {
	this.predictionStatus = predictionStatus;
}
public String getPredictionAccuracy() {
	return predictionAccuracy;
}
public void setPredictionAccuracy(String predictionAccuracy) {
	this.predictionAccuracy = predictionAccuracy;
}
public String getCount() {
	return count;
}
public void setCount(String count) {
	this.count = count;
}





}
