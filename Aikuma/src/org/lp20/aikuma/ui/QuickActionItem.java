package org.lp20.aikuma.ui;

import android.graphics.drawable.Drawable;

/**
 * Action Item class for QuickActionMenu
 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 *
 */
public class QuickActionItem {
	private String title;
	private Integer iconId;
	
	/**
	 * Constructor for the class
	 */
	public QuickActionItem() {}
	
	/**
	 * Constructor for the class
	 * @param title		Action-name
	 * @param iconId	Action-icon resource ID
	 */
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
