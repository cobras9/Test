package com.mobilis.android.nfc.slidemenu.utils;

import java.util.ArrayList;

public class MenuLevel {

	private int level;
	private String name;
	private ArrayList<String> items;
	private ArrayList<MenuLevel> subMenu;
	
	public MenuLevel(){
		items = new ArrayList<String>();
		subMenu = new ArrayList<MenuLevel>();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ArrayList<String> getItems() {
		return items;
	}

	public void setItems(ArrayList<String> items) {
		this.items = items;
	}

	public ArrayList<MenuLevel> getSubMenu() {
		return subMenu;
	}

	public void setSubMenu(ArrayList<MenuLevel> subMenu) {
		this.subMenu = subMenu;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

