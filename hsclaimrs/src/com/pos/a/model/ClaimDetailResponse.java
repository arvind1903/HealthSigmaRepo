package com.pos.a.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimDetailResponse {
	List<ClaimHeader> claimdetaillist=new ArrayList<ClaimHeader>();
	String info;
	String error;
	String processedCount;
	String myQueueCount;
	
	public String getMyQueueCount() {
		return myQueueCount;
	}
	public void setMyQueueCount(String myQueueCount) {
		this.myQueueCount = myQueueCount;
	}
	public String getProcessedCount() {
		return processedCount;
	}
	public void setProcessedCount(String processedCount) {
		this.processedCount = processedCount;
	}
	public List<ClaimHeader> getClaimdetaillist() {
		return claimdetaillist;
	}
	public void setClaimdetaillist(List<ClaimHeader> claimdetaillist) {
		this.claimdetaillist = claimdetaillist;
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
}
