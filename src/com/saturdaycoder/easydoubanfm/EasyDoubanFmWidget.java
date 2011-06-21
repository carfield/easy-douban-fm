package com.saturdaycoder.easydoubanfm;
import android.app.PendingIntent;
import android.graphics.*;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
//import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
	public static void updateWidgetOnOffButton(Context context, int onState) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    
	    switch (onState) {
	    case -1: {
	    	Debugger.debug("set power state as OFF");
	    	updateViews.setViewVisibility(R.id.buttonOn, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOnOff, View.GONE);
	    	updateViews.setViewVisibility(R.id.buttonOff, View.VISIBLE);
	    	
	    	Intent menuIntent = new Intent(DoubanFmService.NULL_EVENT);
			PendingIntent menuPendingIntent = PendingIntent.getBroadcast(context, 
					0, menuIntent, 0);
	    	updateViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);
	    	
			Intent openIntent = new Intent(context, DoubanFmService.class);
			openIntent.putExtra(DoubanFmService.EXTRA_BINDSERVICE_TYPE, DoubanFmService.BINDTYPE_FM);
			openIntent.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
			//Intent openIntent = new Intent(DoubanFmService.ACTION_NULL);
			PendingIntent openPendingIntent = PendingIntent.getService(context, 
					0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			updateViews.setOnClickPendingIntent(R.id.buttonOff, openPendingIntent);
	    	break;
	    }
	    case 0: {
	    	Debugger.debug("set power state as ON<->OFF");
	    	updateViews.setViewVisibility(R.id.buttonOn, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOnOff, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOff, View.GONE);
	    	
			// Menu button
			Intent menuIntent = new Intent(context, ChannelSelectorActivity.class);
			PendingIntent menuPendingIntent = PendingIntent.getActivity(context, 
					0, menuIntent, 0);
			updateViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);
			break;
	    }
	    case 1: {
	    	Debugger.debug("set power state as ON");
	    	updateViews.setViewVisibility(R.id.buttonOn, View.VISIBLE);
	    	updateViews.setViewVisibility(R.id.buttonOnOff, View.GONE);
	    	updateViews.setViewVisibility(R.id.buttonOff, View.GONE);
	    	
			// Menu button
			Intent menuIntent = new Intent(context, ChannelSelectorActivity.class);
			PendingIntent menuPendingIntent = PendingIntent.getActivity(context, 
					0, menuIntent, 0);
			updateViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);
			
			Intent closeIntent = new Intent(DoubanFmService.CONTROL_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			updateViews.setOnClickPendingIntent(R.id.buttonOn, closePendingIntent);
			
			break;
	    }
	    default:
	    	Debugger.error("invalid on/off state");
	    	break;
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
	public static void updateWidgetRated(Context context, boolean isRated) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
	    
		if (isRated) {
			updateViews.setViewVisibility(R.id.buttonLikeUnset, Button.GONE);
		} else {
			updateViews.setViewVisibility(R.id.buttonLikeUnset, Button.VISIBLE);
		}
		
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
	
	public static void setWidgetButtonListeners(Context context, int widgetIds[]) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.appwidget);
	
		// Next button
		Intent nextIntent = new Intent(DoubanFmService.CONTROL_NEXT);
		PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 
				0, nextIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonNext, nextPendingIntent);
		
		// RATE button
		Intent rateIntent = new Intent(DoubanFmService.CONTROL_RATE);
		PendingIntent ratePendingIntent = PendingIntent.getBroadcast(context, 
				0, rateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonLikeUnset, ratePendingIntent);
		
		// UNRATE button
		Intent unrateIntent = new Intent(DoubanFmService.CONTROL_UNRATE);
		PendingIntent unratePendingIntent = PendingIntent.getBroadcast(context, 
				0, unrateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonLikeSet, unratePendingIntent);
		
		// TRASH button
		Intent trashIntent = new Intent(DoubanFmService.CONTROL_TRASH);
		PendingIntent trashPendingIntent = PendingIntent.getBroadcast(context, 
				0, trashIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonHate, trashPendingIntent);
	
		
		// Play/Pause button
		Intent stopIntent = new Intent(DoubanFmService.CONTROL_PLAYPAUSE);
		PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
				0, stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
		
		// Download button
		Intent downloadIntent = new Intent(context, DoubanFmService.class);
		downloadIntent.putExtra(DoubanFmService.EXTRA_BINDSERVICE_TYPE, DoubanFmService.BINDTYPE_DOWNLOAD);
		downloadIntent.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
		PendingIntent downloadPendingIntent = PendingIntent.getService(context, 
				0, downloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonDownload, downloadPendingIntent);
		
		// Off button
		Intent openIntent = new Intent(context, DoubanFmService.class);
		openIntent.putExtra(DoubanFmService.EXTRA_BINDSERVICE_TYPE, DoubanFmService.BINDTYPE_FM);
		openIntent.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
		//Intent openIntent = new Intent(DoubanFmService.ACTION_NULL);
		PendingIntent openPendingIntent = PendingIntent.getService(context, 
				0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonOff, openPendingIntent);
	
		// On button
		Intent closeIntent = new Intent(DoubanFmService.CONTROL_CLOSE);
		PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
				0, closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.buttonOn, closePendingIntent);
		
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		if (widgetIds == null || widgetIds.length == 0) {
			ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
			
			manager.updateAppWidget(thisWidget, remoteViews);
		} else {
			for (int i: widgetIds) {
				manager.updateAppWidget(i, remoteViews);
			}
		}
	}
	
	@Override
	public void onUpdate(Context context, 
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		Debugger.debug("widget onUpdate");
		
		setWidgetButtonListeners(context, appWidgetIds);
		
		
		
		/*for (int i: appWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
						R.layout.appwidget);
			
		
			// Next button
			Intent nextIntent = new Intent(DoubanFmService.CONTROL_NEXT);
			PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 
					0, nextIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonNext, nextPendingIntent);
			
			// RATE button
			Intent rateIntent = new Intent(DoubanFmService.CONTROL_RATE);
			PendingIntent ratePendingIntent = PendingIntent.getBroadcast(context, 
					0, rateIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonLikeUnset, ratePendingIntent);
			
			// UNRATE button
			Intent unrateIntent = new Intent(DoubanFmService.CONTROL_UNRATE);
			PendingIntent unratePendingIntent = PendingIntent.getBroadcast(context, 
					0, unrateIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonLikeSet, unratePendingIntent);
			
			// TRASH button
			Intent trashIntent = new Intent(DoubanFmService.CONTROL_TRASH);
			PendingIntent trashPendingIntent = PendingIntent.getBroadcast(context, 
					0, trashIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonHate, trashPendingIntent);
		
			
			// Play/Pause button
			Intent stopIntent = new Intent(DoubanFmService.CONTROL_PLAYPAUSE);
			PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 
					0, stopIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
			
			// Download button
			Intent downloadIntent = new Intent(context, DoubanFmService.class);
			downloadIntent.putExtra(DoubanFmService.EXTRA_BINDSERVICE_TYPE, DoubanFmService.BINDTYPE_DOWNLOAD);
			downloadIntent.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
			PendingIntent downloadPendingIntent = PendingIntent.getService(context, 
					0, downloadIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonDownload, downloadPendingIntent);
			
			// Off button
			Intent openIntent = new Intent(context, DoubanFmService.class);
			openIntent.putExtra(DoubanFmService.EXTRA_BINDSERVICE_TYPE, DoubanFmService.BINDTYPE_FM);
			openIntent.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
			//Intent openIntent = new Intent(DoubanFmService.ACTION_NULL);
			PendingIntent openPendingIntent = PendingIntent.getService(context, 
					0, openIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOff, openPendingIntent);
		
			// On button
			Intent closeIntent = new Intent(DoubanFmService.CONTROL_CLOSE);
			PendingIntent closePendingIntent = PendingIntent.getBroadcast(context, 
					0, closeIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOn, closePendingIntent);
			

			
			
			// APPWIDGET manager updating
			appWidgetManager.updateAppWidget(i, remoteViews);
		}*/
		
		Intent i = new Intent(DoubanFmService.CONTROL_UPDATE_WIDGET);
		context.sendBroadcast(i);
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
			remoteViews.setOnClickPendingIntent(R.id.buttonOff, closePendingIntent);
		} else {
			Debugger.debug("set button ON/OFF as open button");
			Intent onIntent = new Intent(context, DoubanFmService.class);
			PendingIntent onPendingIntent = PendingIntent.getService(context, 
					0, onIntent, 0);
			remoteViews.setOnClickPendingIntent(R.id.buttonOff, onPendingIntent);
		}
	    ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, remoteViews);
	}*/
	
	//private static boolean serviceStarted = false;
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Intent intent = new Intent(DoubanFmService.CONTROL_CLOSE);  
		context.sendBroadcast(intent);
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
