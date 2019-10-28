package com.android.softapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.android.softapp.R;

// Create overlay dialog for lockedscreen to disable hardware buttons
	public class OverlayDialog extends AlertDialog {

		public OverlayDialog(Activity activity) {
			super(activity, R.style.OverlayDialog);
			WindowManager.LayoutParams params = getWindow().getAttributes();
			params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
			params.dimAmount = 0.0F;
			params.width = 0;
			params.height = 0;
			params.gravity = Gravity.BOTTOM;
			getWindow().setAttributes(params);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
					0xffffff);
			setOwnerActivity(activity);
			setCancelable(false);
		}

		// consume touch events
		public final boolean dispatchTouchEvent(MotionEvent motionevent) {
			return true;
		}

	}
