package com.saturdaycoder.easydoubanfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CameraButtonListener extends BroadcastReceiver {
	private boolean handleCameraButtonControl(Context context) {		
		Debugger.info("ACTION_CAMERA_BUTTON heard");

		if (!Preference.getCameraButtonEnable(context)) {
			return false;
		}		

		try {
			return QuickAction.doQuickAction(context, Preference.getQuickAction(context, Global.QUICKCONTROL_CAMERA_BUTTON));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			Debugger.error("null Intent");
			return;
		}
	
		String action = intent.getAction();
		if (action == null) {
			Debugger.error("null Intent action");
			return;
		}
		if (action.equals(Intent.ACTION_CAMERA_BUTTON)) {
			if (handleCameraButtonControl(context)) {
				abortBroadcast();
				setResultData(null);
			}
		}
	}
}
