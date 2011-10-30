package com.saturdaycoder.easydoubanfm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WidgetContent {
	public String channel;
	public Bitmap picture;
	public String artist;
	public String title;
	public int onState;
	public boolean rated;
	public boolean paused;
	public WidgetContent(Resources res) {
		channel = res.getString(R.string.text_channel_unselected);
		picture = BitmapFactory.decodeResource(res, R.drawable.default_album);
		artist = title = "";
		onState = Global.STATE_IDLE;//EasyDoubanFmWidget.STATE_OFF;
		rated = false;
		paused = false;
	}
}
