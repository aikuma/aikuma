package au.edu.melbuni.boldapp.extensions;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class ColorfulImageButton extends ImageButton {

	boolean activated = false;
	int color = Color.GREEN;

	OnLongClickListener originalLongClickListener;
	OnClickListener originalClickListener;

	OnLongClickListener longClickWrapper = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			if (originalLongClickListener != null) {
				toggle();
				
				return originalLongClickListener.onLongClick(v);
			}

			return false;
		}
	};

	OnClickListener clickWrapper = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (originalClickListener != null) {
				toggle();

				originalClickListener.onClick(v);
			}
		}
	};
	
	public ColorfulImageButton(Context context) {
		super(context);
	}

	public ColorfulImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ColorfulImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setActivatedColor(int color) {
		this.color = color;
	}
	
	public boolean isActivated() {
		return activated;
	}

	public void activate() {
		ColorfulImageButton.this.getBackground().setColorFilter(color,
				Mode.MULTIPLY);
		activated = true;
	}
	
	public void deactivate() {
		ColorfulImageButton.this.getBackground().clearColorFilter();
		activated = false;
	}
	
	public void toggle() {
		if (activated) {
			deactivate();
		} else {
			activate();
		}
	}
	
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		originalLongClickListener = l;

		super.setOnLongClickListener(l);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		originalClickListener = l;

		super.setOnClickListener(l);
	}

}
