package com.saturdaycoder.easydoubanfm;
import android.app.PendingIntent;
import android.graphics.*;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
//import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class EasyDoubanFmWidget extends AppWidgetProvider {
	
	
	synchronized
	public static void updateWidgetProgress(Context context, boolean isOn) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
							R.layout.appwidget);
			
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}

	
	synchronized
	public static void updateWidgetOnOffButton(Context context, boolean isOn) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    
	    if (!isOn) {
	    	Debugger.debug("set ON/OFF button as OFF");
	    	updateViews.setViewVisibility(R.id.buttonOn, View.GONE);
	    	updateViews.setViewVisibility(R.id.buttonOnoff, View.VISIBLE);
	    } else {
	    	Debugger.debug("set ON/OFF button as ON");
	    	updateViews.setViewVisibility(R.id.buttonOn, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOnoff, View.GONE);
	    }
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void updateWidgetPlayPauseButton(Context context, boolean isPlaying) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
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
		
		Debugger.debug("widget onUpdate");
		
		for (int i: appWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
						R.layout.appwidget);
			
			// Trash button
			Intent trashIntent = new Intent(DoubanFmService.DOUBAN_FM_TRASH);
			PendingIntent trashPendingIntent = PendingIntent.getBroadcast(context, 
					0, trashIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonTrash, trashPendingIntent);
			remoteViews.setViewVisibility(R.id.buttonTrash, View.GONE);
			
			// Favorite button
			Intent favoriteIntent = new Intent(DoubanFmService.DOUBAN_FM_FAVORITE);
			PendingIntent favoritePendingIntent = PendingIntent.getBroadcast(context, 
					0, favoriteIntent, 0);
			remoteViews.setViewVisibility(R.id.buttonFavorite, View.GONE);
			remoteViews.setOnClickPendingIntent(R.id.buttonFavorite, favoritePendingIntent);
			
			// Next button
			Intent nextIntent = new Intent(DoubanFmService.DOUBAN_FM_NEXT);
			PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 
					0, nextIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonNext, nextPendingIntent);
		
			
			// Play/Pause button
			Intent stopIntent = new Intent(DoubanFmService.DOUBAN_FM_PLAYPAUSE);
			PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
					0, stopIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
			
			// Download button
			Intent downloadIntent = new Intent(DoubanFmService.DOUBAN_FM_DOWNLOAD);
			PendingIntent downloadPendingIntent = PendingIntent.getBroadcast(context, 
					0, downloadIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonDownload, downloadPendingIntent);
			
			// On/Off button
			Intent openIntent = new Intent(context, DoubanFmService.class);
			PendingIntent openPendingIntent = PendingIntent.getService(context, 
					0, openIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOnoff, openPendingIntent);
		
			// On button
			Intent closeIntent = new Intent(DoubanFmService.DOUBAN_FM_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOn, closePendingIntent);
			
			appWidgetManager.updateAppWidget(i, remoteViews);
		}
	}
	
	/*public static void setOnOffButton(Context context, boolean isServiceOn) {
		serviceStarted = isServiceOn;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.appwidget);
		if (serviceStarted) {
			Debugger.debug("set button ON/OFF as close button");
			Intent closeIntent = new Intent(DoubanFmService.DOUBAN_FM_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOnoff, closePendingIntent);
		} else {
			Debugger.debug("set button ON/OFF as open button");
			Intent onIntent = new Intent(context, DoubanFmService.class);
			PendingIntent onPendingIntent = PendingIntent.getService(context, 
					0, onIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOnoff, onPendingIntent);
		}
	    ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, remoteViews);
	}*/
	
	//private static boolean serviceStarted = false;
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Intent intent = new Intent(DoubanFmService.DOUBAN_FM_CLOSE);  
		context.sendBroadcast(intent);
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
