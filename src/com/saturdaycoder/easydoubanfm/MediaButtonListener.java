package com.saturdaycoder.easydoubanfm;
import android.content.*;
import android.view.*;
public class MediaButtonListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!Preference.getMediaButtonEnable(context)) {
			return;
		}
		
		
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
			KeyEvent ke = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (ke == null) {
				Debugger.error("ACTION_MEDIA_BUTTON heard but null KeyEvent");
	            return;
	        }

	        int keycode = ke.getKeyCode();
	        int keyaction = ke.getAction();
	        long eventtime = ke.getEventTime();
	        
	        Debugger.debug("keycode = " + keycode + " action = " + keyaction 
	        		+ "eventtime = " + eventtime);
	        
	        if(context != null) {
	        	switch (keyaction) {
	        	case KeyEvent.ACTION_DOWN: {
	        		Intent i = new Intent(DoubanFmService.CONTROL_NEXT);
	        		context.sendBroadcast(i);
	        		break;
	        	}
	        	default:
	        		break;
	        	}
	        	abortBroadcast();
		        setResultData(null);
	        }
		}
	}
}
