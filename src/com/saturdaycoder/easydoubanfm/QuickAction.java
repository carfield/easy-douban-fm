package com.saturdaycoder.easydoubanfm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class QuickAction {
	public static boolean doQuickAction(Context context, int quickact) {
		switch (quickact) {
		case Global.QUICKACT_NEXT_MUSIC: {
			//nextMusic();
			Intent i = new Intent(Global.ACTION_PLAYER_SKIP);
	        i.setComponent(new ComponentName(context, DoubanFmService.class));
	        context.startService(i);
			return true;
		}
		case Global.QUICKACT_NEXT_CHANNEL: {
			//nextChannel();
			Intent i = new Intent(Global.ACTION_PLAYER_NEXT_CHANNEL);
	        i.setComponent(new ComponentName(context, DoubanFmService.class));
	        context.startService(i);
			return true;
		}
		case Global.QUICKACT_PLAY_PAUSE: {
			//playPauseMusic();
			Intent i = new Intent(Global.ACTION_PLAYER_PLAYPAUSE);
	        i.setComponent(new ComponentName(context, DoubanFmService.class));
	        context.startService(i);
			return true;
		}
		case Global.QUICKACT_DOWNLOAD_MUSIC: {
			//openDownloader();
			//downloadMusic(null, null);
			Intent i = new Intent(Global.ACTION_DOWNLOADER_DOWNLOAD);
	        i.setComponent(new ComponentName(context, DoubanFmService.class));
	        context.startService(i);
			return true;
		}
		default:
			return false;
		}
				
	}
}
