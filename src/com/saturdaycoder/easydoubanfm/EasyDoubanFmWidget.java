package com.saturdaycoder.easydoubanfm;
import android.app.PendingIntent;
import android.graphics.*;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
//import android.os.IBinder;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.ServiceConnection;
//import android.os.Bundle;
//import android.content.BroadcastReceiver;

public class EasyDoubanFmWidget extends AppWidgetProvider {
	
	//private IDoubanFmService mDoubanFm;
	//private ServiceConnection mServiceConn;
	//private boolean mServiceIsBound;
	
	public static void updateWidgetInfo(Context context, Bitmap bmp, DoubanFmMusic dfm) {
	    RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.appwidget);
	    if (bmp == null) {
	    	updateViews.setImageViewResource(R.id.imageCover, R.drawable.default_album);
	    } else {
	    	updateViews.setImageViewBitmap(R.id.imageCover, bmp);
	    }
	    if (dfm != null) {
	    	updateViews.setTextViewText(R.id.textArtist, dfm.artist);
	    	updateViews.setTextViewText(R.id.textTitle, dfm.title);
	    }
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	
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
			remoteViews.setOnClickPendingIntent(R.id.buttonNext, startPendingIntent);	

			Intent closeIntent = new Intent(DoubanFmService.DOUBAN_FM_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOnoff, closePendingIntent);	
			
			Intent stopIntent = new Intent(DoubanFmService.DOUBAN_FM_STOP);
			PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
					0, stopIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
			

			
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
	
	//private int curSession;
	//private DoubanFmMusic curMusic;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}
	
}
