package com.pos.a.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimLineInfo {
	private List<ClaimLine> claimLineList=new ArrayList<ClaimLine>();
	private String claimId;
	private String hccpt;
	private String pos;
	private String mod;
	public String getMod() {
		return mod;
	}
	public void setMod(String mod) {
		this.mod = mod;
	}
	private String renderingnpi;
	public String getHccpt() {
		return hccpt;
	}
	public void setHccpt(String hccpt) {
		this.hccpt = hccpt;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getRenderingnpi() {
		return renderingnpi;
	}
	public void setRenderingnpi(String renderingnpi) {
		this.renderingnpi = renderingnpi;
	}
	private int count;
	private  int groupId;
	
	public List<ClaimLine> getClaimLineList() {
		return claimLineList;
	}
	public void setClaimLineList(List<ClaimLine> claimLineList) {
		this.claimLineList = claimLineList;
	}
	public String getClaimId() {
		return claimId;
	}
	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
}
