package com.pos.a.model;

import java.util.ArrayList;
import java.util.List;

public class TrainingGroupResponse {
private String error;
private String info;
private List<TrainingGroup> trainingGroupsList=new ArrayList<TrainingGroup>();
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
public List<TrainingGroup> getTrainingGroupsList() {
	return trainingGroupsList;
}
public void setTrainingGroupsList(List<TrainingGroup> trainingGroupsList) {
	this.trainingGroupsList = trainingGroupsList;
}
}
