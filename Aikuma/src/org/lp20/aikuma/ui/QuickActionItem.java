package org.lp20.aikuma.ui;

import android.graphics.drawable.Drawable;

public class QuickActionItem {
	private String title;
	private Integer iconId;
	
	public QuickActionItem() {}
	public QuickActionItem(String title, int iconId) {
		this.title = title;
		this.iconId = iconId;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	
	public String getTitle() { return this.title; }
	public int getIconId() { return this.iconId; }
}
