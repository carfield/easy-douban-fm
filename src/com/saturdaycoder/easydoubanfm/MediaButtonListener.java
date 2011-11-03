package com.saturdaycoder.easydoubanfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.KeyEvent;

public class MediaButtonListener extends BroadcastReceiver {
	private static boolean isMediaButtonDown = false;
	private static long mediaButtonDownStartTime;
	private static final Object mediaButtonDownLock = new Object();

	private boolean handleMediaButtonControl(Context context, 
			int keycode, int keyaction, long keytime) {
		synchronized(mediaButtonDownLock) {
			int longpresslevel = Preference.getLongPressThresholdLevel(context);
			if (longpresslevel < 0 || longpresslevel >= Global.longPressLevels.length)
				longpresslevel = 2;
		
			Debugger.debug("keycode = " + keycode + " action = " + keyaction 
					+ " eventtime = " + keytime);

			switch (keyaction) {
			case KeyEvent.ACTION_DOWN: {								
				if (!isMediaButtonDown) {
					mediaButtonDownStartTime = keytime;
				}
				else if (mediaButtonDownStartTime != -1 &&
						keytime - mediaButtonDownStartTime > 1000 * Global.longPressLevels[longpresslevel]) {
					mediaButtonDownStartTime = -1;
					Debugger.info("MEDIA BUTTON LONG PRESS");
					if (Preference.getMediaButtonLongEnable(context)) {
						QuickAction.doQuickAction(context,
								Preference.getQuickAction(context, Global.QUICKCONTROL_MEDIA_BUTTON_LONG));
					}
				}
				isMediaButtonDown = true;
				break;
			}
			case KeyEvent.ACTION_UP: {
				
				if (isMediaButtonDown) {
					if (mediaButtonDownStartTime == -1 || keytime < mediaButtonDownStartTime) {
						Debugger.info("MEDIA BUTTON PRESSED but wrong start time");						
					}
					else if (keytime - mediaButtonDownStartTime > 1000 * Global.longPressLevels[longpresslevel]) {
						Debugger.info("MEDIA BUTTON LONG PRESS");
						if (Preference.getMediaButtonLongEnable(context)) {							
							QuickAction.doQuickAction(context,
									Preference.getQuickAction(context, Global.QUICKCONTROL_MEDIA_BUTTON_LONG));
						}
						
					}
					else {
						Debugger.info("MEDIA BUTTON SHORT PRESS");
						if (Preference.getMediaButtonEnable(context)) {							
							QuickAction.doQuickAction(context,
									Preference.getQuickAction(context, Global.QUICKCONTROL_MEDIA_BUTTON));
						}
					}
				}
				isMediaButtonDown = false;
				mediaButtonDownStartTime = -1;
				break;
			}
			case KeyEvent.ACTION_MULTIPLE: 
			default:
				isMediaButtonDown = false;
				break;
			}
			return true;
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
		
		if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
			Debugger.info("ACTION_MEDIA_BUTTON heard");
		
			//String kes = intent.getStringExtra(Intent.EXTRA_KEY_EVENT);
			//Parcelable pke = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			KeyEvent ke = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			
			
			if (ke == null) {
				Debugger.error("ACTION_MEDIA_BUTTON heard but null KeyEvent");
				return;
			}
			//Debugger.verbose("parcelable string=\"" + pke.toString() + "\"");
			int keycode = ke.getKeyCode();
			int keyaction = ke.getAction();
			long eventtime = ke.getEventTime();
			
			if (handleMediaButtonControl(context, keycode, keyaction, eventtime)) {
				abortBroadcast();
				setResultData(null);
			}
		}
		

	}
}
