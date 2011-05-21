package com.saturdaycoder.easydoubanfm;
import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

public class EasyDoubanFmWidget extends AppWidgetProvider {
	
	private IDoubanFmService mDoubanFm;
	private ServiceConnection mServiceConn;
	private boolean mServiceIsBound;
	
	@Override
	public void onUpdate(Context context, 
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		for (int i: appWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), 
					R.layout.appwidget);
			
			Intent startIntent = new Intent(context, DoubanFmService.class);
			PendingIntent startPendingIntent = PendingIntent.getService(context, 
					0, startIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonSwitch, startPendingIntent);	

			Intent closeIntent = new Intent(DoubanFmService.DOUBAN_FM_CLOSE);
			//closeIntent.putExtra(DoubanFmService.DOUBAN_FM_ARG_COMMAND, 
			//		DoubanFmService.DOUBAN_FM_CONTROL_CLOSE);
			//closeIntent.putExtra(DoubanFmService.DOUBAN_FM_ARG_SESSION, 
			//		5);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonClose, closePendingIntent);	
			
			Intent stopIntent = new Intent(DoubanFmService.DOUBAN_FM_STOP);
			//stopIntent.putExtra(DoubanFmService.DOUBAN_FM_ARG_COMMAND, 
			//		DoubanFmService.DOUBAN_FM_CONTROL_STOP);
			//stopIntent.putExtra(DoubanFmService.DOUBAN_FM_ARG_SESSION, 
			//		5);
			PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
					0, stopIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonStop, stopPendingIntent);	
			

			
			appWidgetManager.updateAppWidget(i, remoteViews);
		}
	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}
}
