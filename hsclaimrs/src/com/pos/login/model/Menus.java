package com.pos.login.model;

import java.util.List;

public class Menus {
private int menuId;

private String MenuName;

private List<Pages> pages;

private String displayOrder;

public int getMenuId() {
	return menuId;
}

public void setMenuId(int menuId) {
	this.menuId = menuId;
}

public String getMenuName() {
	return MenuName;
}

public void setMenuName(String menuName) {
	MenuName = menuName;
}

public List<Pages> getPages() {
	return pages;
}

public void setPages(List<Pages> pages) {
	this.pages = pages;
}

public String getDisplayOrder() {
	return displayOrder;
}

public void setDisplayOrder(String displayOrder) {
	this.displayOrder = displayOrder;
}

@Override
public String toString() {
	return "Menus [menuId=" + menuId + ", MenuName=" + MenuName + ", pages=" + pages + ", displayOrder=" + displayOrder
			+ "]";
}


}
