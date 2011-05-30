package com.saturdaycoder.easydoubanfm;
import android.app.PendingIntent;
import android.graphics.*;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
//import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class EasyDoubanFmWidget extends AppWidgetProvider {
	synchronized
	public static void updateWidgetChannel(Context context, String name) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
							R.layout.appwidget);
		
		updateViews.setTextViewText(R.id.textChannel, name);
		
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void updateWidgetProgress(Context context, int progress) {
		if (progress >= 20
        		&& progress < 40) {
        	Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress1);
        	EasyDoubanFmWidget.updateWidgetInfo(context, bmp, null);
        }
		if (progress >= 40
        		&& progress < 60) {
        	Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress2);
        	EasyDoubanFmWidget.updateWidgetInfo(context, bmp, null);
        }
		if (progress >= 60
        		&& progress < 80) {
        	Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress3);
        	EasyDoubanFmWidget.updateWidgetInfo(context, bmp, null);
        }
		if (progress >= 80
        		&& progress < 100) {
        	Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress4);
        	EasyDoubanFmWidget.updateWidgetInfo(context, bmp, null);
        }
	}

	
	synchronized
	public static void updateWidgetOnOffButton(Context context, boolean isOn) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    
	    if (!isOn) {
	    	Debugger.debug("set ON/OFF button as OFF");
	    	//updateViews.setViewVisibility(R.id.buttonOn, View.GONE);
	    	updateViews.setViewVisibility(R.id.buttonOnoff, View.VISIBLE);
	    	
	    	Intent menuIntent = new Intent(DoubanFmService.NULL_EVENT);
			PendingIntent menuPendingIntent = PendingIntent.getBroadcast(context, 
					0, menuIntent, 0);
	    	updateViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);
	    } else {
	    	Debugger.debug("set ON/OFF button as ON");
	    	//updateViews.setViewVisibility(R.id.buttonOn, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOnoff, View.GONE);
	    	
			// Menu button
			Intent menuIntent = new Intent(context, ChannelSelectorActivity.class);
			PendingIntent menuPendingIntent = PendingIntent.getActivity(context, 
					0, menuIntent, 0);
			updateViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);
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
	
	synchronized
	public static void updateWidgetInfo(Context context, Bitmap bmp, MusicInfo dfm) {
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
	
	synchronized
	public static void clearWidgetInfo(Context context) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    //updateViews.setImageViewResource(R.id.imageCover, R.drawable.default_album);
	    
	    updateViews.setTextViewText(R.id.textArtist, "");
	    updateViews.setTextViewText(R.id.textTitle, "");
	   
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void clearWidgetImage(Context context) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    updateViews.setImageViewResource(R.id.imageCover, R.drawable.default_album);
	    
	    //updateViews.setTextViewText(R.id.textArtist, "");
	    //updateViews.setTextViewText(R.id.textTitle, "");
	   
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void clearWidgetChannel(Context context) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    updateViews.setTextViewText(R.id.textChannel, "未选定频道");
	   
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void updateWidgetBlurText(Context context, boolean blur) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
		if (!blur) {
		    updateViews.setTextColor(R.id.textArtist, 0xffffff);
		    updateViews.setTextColor(R.id.textTitle, 0xffffff);
		    updateViews.setTextColor(R.id.textChannel, 0xffffff);
		} else {
		    updateViews.setTextColor(R.id.textArtist, 0x808080);
		    updateViews.setTextColor(R.id.textTitle, 0x808080);
		    updateViews.setTextColor(R.id.textChannel, 0x808080);
		}
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		//manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void updateWidgetBlurInfo(Context context, boolean blur) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
		if (!blur) {
		    updateViews.setTextColor(R.id.textArtist, 0xffffff);
		    updateViews.setTextColor(R.id.textTitle, 0xffffff);
		} else {
		    updateViews.setTextColor(R.id.textArtist, 0x808080);
		    updateViews.setTextColor(R.id.textTitle, 0x808080);
		}
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		//manager.updateAppWidget(thisWidget, updateViews);
	}
	
	synchronized
	public static void updateWidgetBlurChannel(Context context, boolean blur) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
		if (!blur) {
		    updateViews.setTextColor(R.id.textChannel, 0xffffff);
		} else {
		    updateViews.setTextColor(R.id.textChannel, 0x808080);
		}
	    
		ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		//manager.updateAppWidget(thisWidget, updateViews);
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
			/*Intent trashIntent = new Intent(DoubanFmService.DOUBAN_FM_TRASH);
			PendingIntent trashPendingIntent = PendingIntent.getBroadcast(context, 
					0, trashIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonTrash, trashPendingIntent);
			remoteViews.setViewVisibility(R.id.buttonTrash, View.GONE);
			
			// Favorite button
			Intent favoriteIntent = new Intent(DoubanFmService.DOUBAN_FM_FAVORITE);
			PendingIntent favoritePendingIntent = PendingIntent.getBroadcast(context, 
					0, favoriteIntent, 0);
			remoteViews.setViewVisibility(R.id.buttonFavorite, View.GONE);
			remoteViews.setOnClickPendingIntent(R.id.buttonFavorite, favoritePendingIntent);*/
			
			// Next button
			Intent nextIntent = new Intent(DoubanFmService.CONTROL_NEXT);
			PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 
					0, nextIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonNext, nextPendingIntent);
		
			
			// Play/Pause button
			Intent stopIntent = new Intent(DoubanFmService.CONTROL_PLAYPAUSE);
			PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
					0, stopIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
			
			// Download button
			Intent downloadIntent = new Intent(DoubanFmService.CONTROL_DOWNLOAD);
			PendingIntent downloadPendingIntent = PendingIntent.getBroadcast(context, 
					0, downloadIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonDownload, downloadPendingIntent);
			
			// Off button
			Intent openIntent = new Intent(context, DoubanFmService.class);
			PendingIntent openPendingIntent = PendingIntent.getService(context, 
					0, openIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOnoff, openPendingIntent);
		
			// On button
			Intent closeIntent = new Intent(DoubanFmService.CONTROL_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOn, closePendingIntent);
			

			
			
			// APPWIDGET manager updating
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
		Intent intent = new Intent(DoubanFmService.CONTROL_CLOSE);  
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
