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
	public static final int STATE_ON = 1;
	public static final int STATE_PREPARE = 0;
	public static final int STATE_OFF = -1;
	
	private static WidgetContent widgetContent = null;
	
	public static WidgetContent getContent(Context context) {
		if (widgetContent == null) {
			synchronized(EasyDoubanFmWidget.class){
				if (widgetContent == null) {
					widgetContent = new WidgetContent(context.getResources());
				}
			}
		}
		return widgetContent;
	}
	
	synchronized
	public static void updateContent(Context context, 
			WidgetContent content, int[] appWidgetIds) {
		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.appwidget);
		
		// channel text
		updateViews.setTextViewText(R.id.buttonChannel, content.channel);
		// picture
		updateViews.setImageViewBitmap(R.id.imageCover, content.picture);
		// music artist
		updateViews.setTextViewText(R.id.textArtist, content.artist);
		// music title
		updateViews.setTextViewText(R.id.textTitle, content.title);
		// on/off state
		switch(content.onState) {
		case STATE_ON:
			updateViews.setImageViewResource(R.id.buttonOnOff, R.drawable.on);
			break;
		case STATE_PREPARE:
			updateViews.setImageViewResource(R.id.buttonOnOff, R.drawable.onoff);
			break;
		case STATE_OFF:
			updateViews.setImageViewResource(R.id.buttonOnOff, R.drawable.off);
			break;
		default:
			break;
		}
		// rating
		if (content.rated) {
			updateViews.setViewVisibility(R.id.buttonLikeUnset, Button.GONE);
		} else {
			updateViews.setViewVisibility(R.id.buttonLikeUnset, Button.VISIBLE);
		}
		
		
		// link buttons
		linkButtons(context, updateViews, appWidgetIds);
		
		// appwidget manager do the update
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		if (appWidgetIds == null || appWidgetIds.length == 0) {
			ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
			
			manager.updateAppWidget(thisWidget, updateViews);
		} else {
			for (int i: appWidgetIds) {
				manager.updateAppWidget(i, updateViews);
			}
		}
	}
	
    private static EasyDoubanFmWidget sInstance = null;
    
    public static synchronized EasyDoubanFmWidget getInstance() {
    	if (sInstance == null) {
    		synchronized(EasyDoubanFmWidget.class){
		        if (sInstance == null) {
		            sInstance = new EasyDoubanFmWidget();
		        }
    		}
    	}
        return sInstance;
    }
    
    private static RemoteViews remoteViewsInstance = null;
	public static RemoteViews getRemoteViews(Context context) {
		if (remoteViewsInstance == null) {
			synchronized(EasyDoubanFmWidget.class) {
				if (remoteViewsInstance == null) {
					remoteViewsInstance = new RemoteViews(context.getPackageName(),
							R.layout.appwidget);
				}
			}
		}
		return remoteViewsInstance;
	}
	
	
	synchronized
	public static void setProgress(Context context, int progress) {
		Bitmap bmp = null;
		if (progress >= 20
        		&& progress < 40) {
        	bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress1);
        	
        }
		if (progress >= 40
        		&& progress < 60) {
        	bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress2);
        	
        }
		if (progress >= 60
        		&& progress < 80) {
        	bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress3);
        	
        }
		if (progress >= 80
        		&& progress < 100) {
        	bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.progress4);
        	
        }
		
		if (bmp != null) {
			RemoteViews updateViews = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
			updateViews.setImageViewBitmap(R.id.imageCover, bmp);
			ComponentName thisWidget = new ComponentName(context, EasyDoubanFmWidget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(thisWidget, updateViews);
		}
	}


	synchronized
	public static void linkButtons(Context context, 
					RemoteViews remoteViews, int widgetIds[]) {

	
		Debugger.info("Widget re-link button listeners");
		
		ComponentName cn = new ComponentName(context, DoubanFmService.class);
		// Next button
		Intent nextIntent = new Intent(DoubanFmService.CONTROL_NEXT);
		nextIntent.setComponent(cn);
		PendingIntent nextPendingIntent = PendingIntent.getService(context, 
				0, nextIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonNext, nextPendingIntent);
		
		// RATE button
		Intent rateIntent = new Intent(DoubanFmService.CONTROL_RATE);
		rateIntent.setComponent(cn);
		PendingIntent ratePendingIntent = PendingIntent.getService(context, 
				0, rateIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonLikeUnset, ratePendingIntent);
		
		// UNRATE button
		Intent unrateIntent = new Intent(DoubanFmService.CONTROL_UNRATE);
		unrateIntent.setComponent(cn);
		PendingIntent unratePendingIntent = PendingIntent.getService(context, 
				0, unrateIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonLikeSet, unratePendingIntent);
		
		// TRASH button
		Intent trashIntent = new Intent(DoubanFmService.CONTROL_TRASH);
		trashIntent.setComponent(cn);
		PendingIntent trashPendingIntent = PendingIntent.getService(context, 
				0, trashIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonHate, trashPendingIntent);
	
		
		// Play/Pause button
		Intent stopIntent = new Intent(DoubanFmService.CONTROL_PLAYPAUSE);
		stopIntent.setComponent(cn);
		PendingIntent stopPendingIntent = PendingIntent.getService(context, 
				0, stopIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonPlaypause, stopPendingIntent);	
		
		// Download button
		Intent downloadIntent = new Intent(DoubanFmService.CONTROL_DOWNLOAD);
		downloadIntent.setComponent(cn);
		PendingIntent downloadPendingIntent = PendingIntent.getService(context, 
				0, downloadIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonDownload, downloadPendingIntent);
		
		// On/Off button
		Intent openIntent = new Intent(DoubanFmService.CONTROL_ONOFF);
		openIntent.setComponent(cn);
		PendingIntent openPendingIntent = PendingIntent.getService(context, 
				0, openIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonOnOff, openPendingIntent);
	
		// Menu button
		Intent menuIntent = new Intent(context, PreferenceActivity.class);
		
		PendingIntent menuPendingIntent = PendingIntent.getActivity(context, 
				0, menuIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonMenu, menuPendingIntent);

		// channel button
		Intent channelIntent = new Intent(context, ChannelSelectorActivity.class);
		
		PendingIntent channelPendingIntent = PendingIntent.getActivity(context, 
				0, channelIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.buttonChannel, channelPendingIntent);

	}
	
	@Override
	public void onUpdate(Context context, 
			AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		Debugger.debug("widget onUpdate");
		//RemoteViews remoteViews = getRemoteViews(context); 
		
		WidgetContent content = getContent(context);
		
		updateContent(context, content, appWidgetIds);
		
		Intent i = new Intent(DoubanFmService.CONTROL_UPDATE_WIDGET);
		context.sendBroadcast(i);
	}

	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		Debugger.info("widget onDeleted");
		super.onDeleted(context, appWidgetIds);
		Intent intent = new Intent(DoubanFmService.CONTROL_CLOSE);  
		context.sendBroadcast(intent);
	}
	
	@Override
	public void onDisabled(Context context) {
		Debugger.info("widget onDisabled");
		super.onDisabled(context);
		Intent intent = new Intent(DoubanFmService.CONTROL_CLOSE);  
		context.sendBroadcast(intent);
	}
	
	@Override
	public void onEnabled(Context context) {
		Debugger.info("widget onEnabled");
		super.onEnabled(context);

	}

	
}
