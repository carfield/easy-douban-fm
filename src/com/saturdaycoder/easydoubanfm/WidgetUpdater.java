package com.saturdaycoder.easydoubanfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

public class WidgetUpdater extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			Debugger.error("WidgetUpdater received NULL intent");
			return;
		}
		
		String action = intent.getAction();
		
		if (action.equals(Global.EVENT_PLAYER_POWER_STATE_CHANGED)) {
			int state = intent.getIntExtra(Global.EXTRA_STATE, 
													Global.INVALID_STATE);
			Debugger.debug("EVENT_PLAYER_POWER_STATE_CHANGED, state=" + state);
			switch(state) {
			case Global.STATE_IDLE: {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content = EasyDoubanFmWidget.getContent(context);
				content.channel = context.getResources().getString(R.string.text_channel_unselected);
				content.picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album);
				content.artist = "";
				content.title = "";
				content.onState = EasyDoubanFmWidget.STATE_OFF;
				EasyDoubanFmWidget.updateContent(context, content, null);
				return;
			}
			case Global.STATE_PREPARE: {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content.onState = EasyDoubanFmWidget.STATE_PREPARE;
				EasyDoubanFmWidget.updateContent(context, content, null);
				return;
			}
			case Global.STATE_STARTED: {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content.onState = EasyDoubanFmWidget.STATE_ON;
				EasyDoubanFmWidget.updateContent(context, content, null);
				return;
			}
			default:
				Debugger.error("invalid state value " + state + " for " + action);
				return;
			}
		}
		
		else if (action.equals(Global.EVENT_LOGIN_STATE_CHANGED)) {
			int state = intent.getIntExtra(Global.EXTRA_STATE, 
													Global.INVALID_STATE);
			switch (state) {
			case Global.STATE_STARTED: 
			case Global.STATE_IDLE:
			case Global.STATE_ERROR:
				return;
			case Global.STATE_PREPARE: {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content.channel = context.getResources().getString(R.string.text_login_inprocess);
				EasyDoubanFmWidget.updateContent(context, content, null);
				return;
			}
			default:
				Debugger.error("invalid state value " + state + " for " + action);
				return;
			}
		}
		
		else if (action.equals(Global.EVENT_CHANNEL_CHANGED)) {
			String chan = intent.getStringExtra(Global.EXTRA_CHANNEL);
			if (chan == null || chan.equals("")) {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content.channel = "Error";
				EasyDoubanFmWidget.updateContent(context, content, null);
			} else {
				WidgetContent content = EasyDoubanFmWidget.getContent(context);
				content.channel = chan;
				EasyDoubanFmWidget.updateContent(context, content, null);
			}
		}
	}
}
