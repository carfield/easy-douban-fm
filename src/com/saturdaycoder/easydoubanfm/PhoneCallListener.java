package com.saturdaycoder.easydoubanfm;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class PhoneCallListener extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm == null) {
			Debugger.error("null TelephonyManager");
			return;
		}
		if (intent == null) {
			Debugger.error("null Intent");
			return;
		}
		
		String action = intent.getAction();
		if (action == null) {
			Debugger.error("null Intent Action");
			return;
		}
		
		if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			int state = tm.getCallState();
			
			switch(state) {
			case TelephonyManager.CALL_STATE_IDLE: {
				Debugger.info("Call state idle");
				Intent i = new Intent(DoubanFmService.CONTROL_RESUME);
				context.sendBroadcast(i);
				break;
			}
			case TelephonyManager.CALL_STATE_OFFHOOK: {
				Debugger.info("Offhook call!");
				Intent i = new Intent(DoubanFmService.CONTROL_PAUSE);
				context.sendBroadcast(i);
				break;
			}
			case TelephonyManager.CALL_STATE_RINGING: {
				Debugger.info("Incoming call!");
				Intent i = new Intent(DoubanFmService.CONTROL_PAUSE);
				context.sendBroadcast(i);
				break;
			}
			default:
				Debugger.error("Invalid call state: " + state);
				break;
			}
			

		}
	}
}
