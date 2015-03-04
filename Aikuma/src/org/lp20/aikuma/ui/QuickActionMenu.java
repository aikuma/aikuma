package org.lp20.aikuma.ui;

import org.lp20.aikuma2.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * QuickAction Menu class(used for star,flag,share,archive)
 * 
 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
 *
 * @param <T>	The Item type which QuickActionMenu will handle
 */
public class QuickActionMenu<T> {
	private Context context;
	private LayoutInflater inflater;
	private WindowManager windowManager;
	
	private PopupWindow mWindow;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private ViewGroup mRootView;
	private ViewGroup mItemGroupView;
	private OnActionItemClickListener<T> mListener;
	
	private int notificationHeight;
	
	private T mItem;
	
	private int mActionItemPos;
	
	/**
	 * Constructor for the quick-menu
	 * @param context	Activity-context where quick-menu is called
	 */
	public QuickActionMenu(Context context) {
		this.context = context;
		this.mWindow = new PopupWindow(context);
		
		//Background Click -> dismiss QuickActionMenu
		mWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mWindow.dismiss();	
					return true;
				} else {
					return false;
				}
			}
		});
		
		windowManager = (WindowManager) 
				context.getSystemService(Context.WINDOW_SERVICE);
		inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		

	    setContentView(R.layout.quickaction_menu);    
		
		//setRootViewId(R.layout.quickaction);
	    mActionItemPos = 0;
	}
	
	private void setContentView(int id) {
		mRootView = (ViewGroup) inflater.inflate(id, null);
		
		mItemGroupView = (ViewGroup) mRootView.findViewById(R.id.itemGroup);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrowUp);
		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrowDown);
		
		mWindow.setContentView(mRootView);	
	}
	
	/**
	 * add an action-item to the menu
	 * @param action	action-item
	 */
	public void addActionItem(QuickActionItem action) {
		String title = action.getTitle();
		Integer iconId = action.getIconId();
		
		View itemView = (View) inflater.inflate(R.layout.quickaction_item, null);
		
		ImageView iconImg = (ImageView) itemView.findViewById(R.id.quickActionIcon);
		TextView text = (TextView) itemView.findViewById(R.id.quickActionTitle);

		if (iconId != null) {
			iconImg.setImageResource(iconId);
		} else {
			iconImg.setVisibility(View.GONE);
		}
		
		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		final int pos =  mActionItemPos;
		itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null && mItem != null) mListener.onItemClick(pos, mItem);
				mWindow.dismiss();
			}
		});
		
		itemView.setFocusable(true);
		itemView.setClickable(true);
			 
		mItemGroupView.addView(itemView, mActionItemPos+1);
		
		mActionItemPos++;
	}
	
	/**
	 * Enable the action-item at pos
	 * @param pos		the position number of the item in the menu
	 * @param isEnable	treu:enable/false:disable
	 */
	public void setItemEnabledAt(int pos, boolean isEnable) {
		if(pos < 0 || pos >= mActionItemPos)
			throw new IllegalArgumentException(
					"There is no item at position" + "pos");
		
		View itemView = mItemGroupView.getChildAt(pos+1);
		itemView.setClickable(isEnable);		
	}
	
	/**
	 * Set the icon of the action-item at pos
	 * @param pos			the position number of the item in the menu
	 * @param resourceId	icon resource ID
	 */
	public void setItemImageResourceAt(int pos, int resourceId) {
		if(pos < 0 || pos >= mActionItemPos)
			throw new IllegalArgumentException(
					"There is no item at position" + "pos");
		
		View itemView = mItemGroupView.getChildAt(pos+1);
		ImageView iconImg = (ImageView) itemView.findViewById(R.id.quickActionIcon);
		iconImg.setImageResource(resourceId);
	}
	
	/**
	 * Set the listener for the item click eventss
	 * @param listener	listener-object
	 */
	public void setOnActionItemClickListener(
			OnActionItemClickListener<T> listener) {
		mListener = listener;
	}
	
	/**
	 * Show the QuickMenu in the device
	 * @param anchor	the view from which Quick-menu is called
	 * @param position	The click-position in the anchor
	 * @param item		The item shown in the anchor
	 */
	public void show(View anchor, int[] position, T item) {
		if(notificationHeight == 0) {
			int[] location = new int[2];
			anchor.getLocationOnScreen(location);
			notificationHeight = location[1]-anchor.getTop();
		}
		
		int xPos = position[0];
		int yPos = position[1];
		mItem = item;
		
		Log.i("hi", "show: " + xPos + " " + yPos + " " + anchor.getTop());
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		
		if(position.length != 2) 
			throw new IllegalArgumentException(
					"two values are required for a position");
		if(xPos < 0 || xPos > screenWidth)
			throw new IllegalArgumentException(
					"xPosition is outside the screen");
		if(yPos < 0 || yPos > screenHeight)
			throw new IllegalArgumentException(
					"yPosition is outside the screen");
		
		preShow();
		int menuWidth = mRootView.getMeasuredWidth();
		int menuHeight = mRootView.getMeasuredHeight();
		int arrowWidth = mArrowUp.getMeasuredWidth();
		
		int menuXPos = (screenWidth - menuWidth)/2;
		int menuYPos = (yPos - menuHeight);
		
		if(menuXPos > (xPos-arrowWidth/2)) {
			menuXPos = xPos - arrowWidth/2;
			menuXPos = (menuXPos < 0)? 0 : menuXPos;
		}
		if(menuYPos - notificationHeight < 0) {
			menuYPos = yPos;
			showArrow(R.id.arrowUp, xPos - arrowWidth/2);
		} else {
			showArrow(R.id.arrowDown, xPos - arrowWidth/2);
			
		}
		
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, menuXPos, menuYPos);
	}
	
	/**
	 * Show the QuickMenu in the device
	 * (the arrow of quick-menu is shown at the center)
	 * @param anchor	the view from which Quick-menu is called
	 * @param item		The item shown in the anchor
	 */
	public void show (View anchor, T item) {
		mItem = item;
		preShow();
		
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int menuWidth = mRootView.getMeasuredWidth();
		int menuHeight = mRootView.getMeasuredHeight();
		
		int[] anchorLocationOnScreen = new int[2];
		anchor.getLocationOnScreen(anchorLocationOnScreen);
		int anchorLeft = anchor.getLeft();
		int anchorRight = anchor.getRight();
				
		int xPos = (screenWidth - menuWidth) / 2;
		int yPos = (anchorLocationOnScreen[1] - menuHeight);

		if(anchor.getTop() - menuHeight < 0) {
			yPos = anchorLocationOnScreen[1]+anchor.getHeight();
			Log.i("QuickActionMenu", "yPos: " + yPos);
			showArrow(R.id.arrowUp, (anchorLeft+anchorRight)/2);
		} else {
			showArrow(R.id.arrowDown, (anchorLeft+anchorRight/2));
		}

		//setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
	
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		
		//if (animateTrack) mTrack.startAnimation(mTrackAnim);
	}
	
	private void preShow() {
		mRootView.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mRootView.measure(LayoutParams.WRAP_CONTENT, 
				LayoutParams.WRAP_CONTENT);
		
		mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
	
		mWindow.setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mWindow.setTouchable(true);
		mWindow.setFocusable(true);
		mWindow.setOutsideTouchable(true);
	}

	private void showArrow(int arrowId, int arrowXPos) {
        View showArrow = (arrowId == R.id.arrowUp) ? mArrowUp : mArrowDown;
        View hideArrow = (arrowId == R.id.arrowUp) ? mArrowDown : mArrowUp;

        showArrow.setVisibility(View.VISIBLE);
        hideArrow.setVisibility(View.INVISIBLE);
        
        int arrowWidth = mArrowUp.getMeasuredWidth();
        
        ViewGroup.MarginLayoutParams param = 
        		(ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
        
        param.leftMargin = arrowXPos;
    }
	
	/**
	 * Listener interface for Quick-menu
	 * @author Sangyeop Lee	<sangl1@student.unimelb.edu.au>
	 *
	 * @param <T>	The item type which Click action will handle
	 */
	public interface OnActionItemClickListener<T> {
		/**
		 * Click listener
		 * @param pos	Action-item position in the menu
		 * @param item	The item which Click action will handle
		 */
		public abstract void onItemClick(int pos, T item);
	}
}
