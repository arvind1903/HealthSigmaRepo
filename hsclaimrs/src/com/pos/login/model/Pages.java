package com.pos.login.model;

public class Pages {

	private int pageId;
	
	private String pageName;
	
	private String displayOrder;
	
	private String securityAccessTo;

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(String displayOrder) {
		this.displayOrder = displayOrder;
	}

	public String getSecurityAccessTo() {
		return securityAccessTo;
	}

	public void setSecurityAccessTo(String securityAccessTo) {
		this.securityAccessTo = securityAccessTo;
	}

	@Override
	public String toString() {
		return "Pages [pageId=" + pageId + ", pageName=" + pageName + ", displayOrder=" + displayOrder
				+ ", securityAccessTo=" + securityAccessTo + "]";
	}
	
}
