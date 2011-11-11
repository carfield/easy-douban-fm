package com.saturdaycoder.easydoubanfm;
import android.app.Notification;
import android.media.*;
import android.os.Handler;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.impl.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import android.media.MediaPlayer;
import org.json.*;
import android.media.AudioManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import org.apache.http.params.*;

import com.saturdaycoder.easydoubanfm.apis.DoubanFmApi;
import com.saturdaycoder.easydoubanfm.channels.FmChannel;
import com.saturdaycoder.easydoubanfm.downloader.*;
import com.saturdaycoder.easydoubanfm.player.*;
import com.saturdaycoder.easydoubanfm.scheduling.SchedulerManager;

import android.media.MediaScannerConnection.*;

public class DoubanFmService extends Service implements IDoubanFmService {
	private final IBinder mBinder = new LocalBinder();


	HttpParams httpParameters;	
	Database db;
	
	// Event Listeners
	private CallListener callListener;
	private ShakingListener shakeControlListener;
	private ShakeDetector shakeDetector;
	private CameraButtonListener cameraButtonListener;
	private MediaButtonListener mediaButtonListener;	
	
	// for service foreground notification
	private Notification fgNotification;	
	
	// 
	DoubanFmPlayer dPlayer;
	DoubanFmDownloader dDownloader;
	
	private Handler mHandler = new Handler();
	
	private static final int IDLE_DELAY = 60000;
    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
        	Debugger.info("SERVICE is handle DelayStop");
            if (dDownloader.isOpen()) {
                return;
            }
            
            if (dPlayer.isOpen()) {
            	return;
            }
            
            if (SchedulerManager.getInstance(DoubanFmService.this).isStopScheduled() || 
            		SchedulerManager.getInstance(DoubanFmService.this).isStartScheduled()) {
            	return;
            }

            Debugger.warn("SERVICE is stopped by DelayStopHandler");
            closeService();
        }
    };
    
    private Handler mDelayedPausedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
        	Debugger.info("SERVICE is handle DelayPauseStop");
            if (dDownloader.isOpen()) {
                return;
            }
            
            if (dPlayer.isOpen() && dPlayer.isPlaying()) {
            	return;
            }

           
            Debugger.warn("SERVICE is stopped by DelayPausedStopHandler");
            closeService();
        }
    };
	
	public class LocalBinder extends Binder {
		
		DoubanFmService getService() {
			return DoubanFmService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Debugger.debug("service onCreate");
		
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mDelayedPausedStopHandler.removeCallbacksAndMessages(null);	

		db = new Database(this);
		dPlayer = new DoubanFmPlayer(this, db);
		dDownloader = new DoubanFmDownloader(this, db);
		
		// work-around on gingerbread restart bug: onStartCommand will not execute,
		// so update widgets here
		if (android.os.Build.VERSION.SDK_INT == 9 
				|| android.os.Build.VERSION.SDK_INT == 10) {
			updateWidgets();
		}
		
		
		// listens to the call state event
		callListener = new CallListener();
		IntentFilter phoneFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(callListener, phoneFilter);

		// listens to hard button event
		shakeControlListener = new ShakingListener();
		
		// media button
		if (mediaButtonListener == null) {
			mediaButtonListener = new MediaButtonListener();
		}		
		IntentFilter mfilter = new IntentFilter();
		mfilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		mfilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(mediaButtonListener, mfilter);
		
		// media button
		if (cameraButtonListener == null) {
			cameraButtonListener = new CameraButtonListener();
		}		
		IntentFilter cfilter = new IntentFilter();
		cfilter.addAction(Intent.ACTION_CAMERA_BUTTON);
		cfilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(cameraButtonListener, cfilter);
		
		// listens to shake event
		shakeDetector = new ShakeDetector(this);		
		shakeDetector.registerOnShakeListener(shakeControlListener);		
		try {
			shakeDetector.start();
		} catch (Exception e) {
			Debugger.error("no shake sensor: " + e.toString());
		}
		
		httpParameters = new BasicHttpParams();
		int timeoutConnection = Preference.getConnectTimeout(this);
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = Preference.getSocketTimeout(this);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		DoubanFmApi.setHttpParameters(httpParameters);
		
		
		if (android.os.Build.VERSION.SDK_INT == 9 
				|| android.os.Build.VERSION.SDK_INT == 10) {
			long schedStopMillis = Preference.getScheduledStopTime(this);
			long schedStartMillis = Preference.getScheduledStartTime(this);
			
			Debugger.debug("stored scheduled stop timer: " + new Date(schedStopMillis).toLocaleString());
			Debugger.debug("stored scheduled start timer: " + new Date(schedStartMillis).toLocaleString());
			// delete stop timer
			if (schedStopMillis > 0) {
				Debugger.info("Clear scheduled stop timer: " + new Date(schedStopMillis).toLocaleString());
				Preference.setScheduledStopTime(this, 0);	
				
			}
			SchedulerManager.getInstance(this).cancelScheduleStop();
			
			// to keep the start timer valid
			
			// 5 minutes is safe margin (in case the service is killed near the scheduled time)
			if (schedStartMillis > System.currentTimeMillis()) {
				Debugger.info("Detected scheduled start timer: " + new Date(schedStartMillis).toLocaleString());
				//SchedulerManager.getInstance(this).scheduleStartAt(new Date(schedStartMillis));
				Intent i = new Intent(Global.ACTION_SCHEDULER_COMMAND);
		        i.setComponent(new ComponentName(this, DoubanFmService.class));
		        i.putExtra(Global.EXTRA_SCHEDULE_TYPE, Global.SCHEDULE_TYPE_START_PLAYER);
		        i.putExtra(Global.EXTRA_SCHEDULE_TIME, schedStartMillis);
		        startService(i);
				return;
			} else {
				SchedulerManager.getInstance(this).cancelScheduleStart();
			}
		}
		// If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Debugger.warn("SERVICE ONSTARTCOMMAND");
		
		if (intent == null) { // it tells us the service was killed by system.
			Debugger.warn("null intent");
			updateWidgets();
			
			long schedStopMillis = Preference.getScheduledStopTime(this);
			long schedStartMillis = Preference.getScheduledStartTime(this);
			
			Debugger.debug("stored scheduled stop timer: " + new Date(schedStopMillis).toLocaleString());
			Debugger.debug("stored scheduled start timer: " + new Date(schedStartMillis).toLocaleString());
			// delete stop timer
			if (schedStopMillis > 0) {
				Debugger.info("Clear scheduled stop timer: " + new Date(schedStopMillis).toLocaleString());
				Preference.setScheduledStopTime(this, 0);	
				
			}
			
			// to keep the start timer valid
			
			if (schedStartMillis > System.currentTimeMillis()) {
				Debugger.info("Detected scheduled start timer: " + new Date(schedStartMillis).toLocaleString());
				SchedulerManager.getInstance(this).scheduleStartAt(new Date(schedStartMillis));
				return START_STICKY;
			}
			
	        //mDelayedStopHandler.removeCallbacksAndMessages(null);
	        Message msg = mDelayedStopHandler.obtainMessage();
	        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
			return START_NOT_STICKY;
		}
		
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
		
		Debugger.warn("Intent action=\"" + intent.getAction() + "\" flags=" + flags + " startId=" + startId);
		String action = intent.getAction();
        if (action.equals(Global.ACTION_PLAYER_ON)) {
        	Debugger.info("Douban service starts with ON command");
       		openPlayer();
        }        
        if (action.equals(Global.ACTION_PLAYER_OFF)) {        	
        	Debugger.info("Douban service starts with OFF command");
      		closePlayer();
        }	
        if (action.equals(Global.ACTION_PLAYER_ONOFF)) {
        	Debugger.info("Douban service starts with ON/OFF command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		closePlayer();
        	}	
        }
        if (action.equals(Global.ACTION_PLAYER_SKIP)) {
        	Debugger.info("Douban service starts with SKIP command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		nextMusic();
        	}
        }
        if (action.equals(Global.ACTION_PLAYER_NEXT_CHANNEL)) {
        	Debugger.info("Douban service starts with SKIP command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		nextChannel();
        	}
        }
        if (action.equals(Global.ACTION_PLAYER_TRASH)) {
        	Debugger.info("Douban service starts with TRASH command");
        	banMusic();
        }
        if (action.equals(Global.ACTION_PLAYER_RATE)) {
        	Debugger.info("Douban service starts with RATE command");
       		rateMusic();
        }
        if (action.equals(Global.ACTION_PLAYER_UNRATE)) {
        	Debugger.info("Douban service starts with UNRATE command");
       		unrateMusic();
        }
        if (action.equals(Global.ACTION_PLAYER_RATEUNRATE)) {
        	Debugger.info("Douban service starts with RATE/UNRATE command");
        	if (!dPlayer.isMusicRated())
        		rateMusic();
        	else 
        		unrateMusic();
        }
        if (action.equals(Global.ACTION_PLAYER_PLAYPAUSE)) {
        	
        	Debugger.info("Douban service starts with PLAY/PAUSE command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		playPauseMusic();
        	}
        }
        if (action.equals(Global.ACTION_PLAYER_PAUSE)) {
        	
        	Debugger.info("Douban service starts with PAUSE command");
        	pauseMusic();
        }
        if (action.equals(Global.ACTION_PLAYER_RESUME)) {
        	
        	Debugger.info("Douban service starts with RESUME command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		resumeMusic();
        	}
        }
        if (action.equals(Global.ACTION_PLAYER_LOGIN)) {
        	String username = intent.getStringExtra(Global.EXTRA_LOGIN_USERNAME); 
        	String passwd = intent.getStringExtra(Global.EXTRA_LOGIN_PASSWD); 
        	Debugger.info("Douban service starts with LOGIN command");
        	
        	login(username, passwd);
        }
        if (action.equals(Global.ACTION_PLAYER_LOGOUT)) {
        	Debugger.info("Douban service starts with LOGOUT command");        	
        	logout();
        }
        if (action.equals(Global.ACTION_PLAYER_SELECT_CHANNEL)) {
        	int chann = intent.getIntExtra(Global.EXTRA_CHANNEL, 0);
        	Debugger.info("Douban service starts with SELECT_CHANNEL: " + chann);
        	selectChannel(chann);
        }
        
        if (action.equals(Global.ACTION_DOWNLOADER_DOWNLOAD)) {
        	String url = intent.getStringExtra(Global.EXTRA_MUSIC_URL);
        	String filename = intent.getStringExtra(Global.EXTRA_DOWNLOADER_DOWNLOAD_FILENAME);
        	Debugger.info("Douban service starts with DOWNLOAD command");
        	openDownloader();
        	downloadMusic(url, filename);
        }
        if (action.equals(Global.ACTION_DOWNLOADER_CANCEL)) {
        	String url = intent.getStringExtra(Global.EXTRA_MUSIC_URL);
        	Debugger.info("Douban service starts with CANCEL DOWNLOAD command");
        	openDownloader();
        	cancelDownload(url);
        }
        if (action.equals(Global.ACTION_DOWNLOADER_CLEAR_NOTIFICATION)) {
        	String url = intent.getStringExtra(Global.EXTRA_MUSIC_URL);
        	Debugger.info("Douban service starts with CLEAR NOTIFICATION command");
        	openDownloader();
        	clearDownloadNotification(url);
        }


        if (action.equals(Global.ACTION_WIDGET_UPDATE)) {
        	Debugger.info("Douban service starts with UPDATE_WIDGET");
        	updateWidgets();
        }
        
        if (action.equals(Global.ACTION_ACTIVITY_UPDATE)) {
        	Debugger.info("Douban service starts with UPDATE_ACTIVITY");
        	updateActivity();
        }
        
        if (action.equals(Global.ACTION_SCHEDULER_COMMAND)) {
        	int cmd = intent.getIntExtra(Global.EXTRA_SCHEDULE_TYPE, -1);
        	long millis = intent.getLongExtra(Global.EXTRA_SCHEDULE_TIME, -1);
        	if (millis > System.currentTimeMillis()) {
        		if (cmd == Global.SCHEDULE_TYPE_START_PLAYER) {
        			SchedulerManager.getInstance(this).scheduleStartAt(new Date(millis));
        			return START_STICKY;
        		}
        		if (cmd == Global.SCHEDULE_TYPE_STOP_PLAYER) {
        			SchedulerManager.getInstance(this).scheduleStopAt(new Date(millis));
        			return START_STICKY;
        		}
        	}
        	
        }
        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
		Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return START_STICKY;
		
		
	}
	
	@Override
	public void onDestroy() {
		Debugger.warn("SERVICE ONDESTROY");
       // make sure there aren't any other messages coming
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
        
        pausedByPhoneCall = false;
        
        try {
			if (shakeDetector != null ) {
				if (shakeControlListener != null)
					shakeDetector.unregisterOnShakeListener(shakeControlListener);
				shakeDetector.stop();
				shakeDetector = null;
			}
        
			// media button
			if (mediaButtonListener != null) {
				unregisterReceiver(mediaButtonListener);
				mediaButtonListener = null;
			}		
			
			// media button
			if (cameraButtonListener == null) {
				unregisterReceiver(cameraButtonListener);
				cameraButtonListener = null;
			}		
			
        	if (callListener != null) {
        		unregisterReceiver(callListener);
        		callListener = null;
        	}
        } catch (Exception e) {
        	
        }
        
        try {
        	db.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        //mMediaplayerHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		return mBinder;
	}
	@Override
	public void onLowMemory() {
		Debugger.warn("SERVICE ONLOWMEMORY");
	}	
	
	private void banMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.banMusic();
		}
		else {
			popNotify(getResources().getString(R.string.warning_not_open));
		}
    }

	@Override
	public boolean login(String email, String passwd) {
		return dPlayer.login(email, passwd);
	}

	//@Override
	private void logout() {
		dPlayer.logout();
	}
	


	private void openDownloader() {
		dDownloader.open();
	}
	
	private void closeDownloader() {
		dDownloader.close();
	}

	private void openPlayer() {
	   	Notification fgNotification = new Notification(R.drawable.icon,
				getResources().getString(R.string.app_name),
		        System.currentTimeMillis());
		fgNotification.flags |= Notification.FLAG_NO_CLEAR;
		Intent it = new Intent(DoubanFmService.this, EasyDoubanFm.class);
		PendingIntent pi = PendingIntent.getActivity(DoubanFmService.this, 0, it, 0);
		fgNotification.setLatestEventInfo(DoubanFmService.this, "", "", pi);		
		startForeground(Global.NOTIFICATION_ID_PLAYER, fgNotification);
		
		dPlayer.open();
	}
	
	private void closePlayer() {

		
		Debugger.debug("DoubanFm Control Service unregistered");

		stopForeground(true);
		dPlayer.close();
        pausedByPhoneCall = false;
        
        SchedulerManager.getInstance(this).cancelScheduleStop();
		
	}
	
	private void closeService() {
		Debugger.info("closeService");
		
		closeDownloader();
		closePlayer();
		stopSelf();
	}

	private void resumeMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.resumeMusic();
		}
	}

	private void pauseMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.pauseMusic();
	        // make sure the service will shut down on its own if it was
	        // just started but not bound to and nothing is playing
	        mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
	        
	        if (Preference.getShutdownOnIdleEnable(this)) {
	        	int delaylevel = Preference.getIdleThresholdLevel(this);
	        	if (delaylevel <0 || delaylevel >= Global.idleLevels.length)
	        		delaylevel = 3;
	        	
	        	Debugger.debug("shutdown on idle is activated: delay is " + Global.idleLevels[delaylevel]);
		        Message msg = mDelayedPausedStopHandler.obtainMessage();
		        mDelayedPausedStopHandler.sendMessageDelayed(msg, 60000 * Global.idleLevels[delaylevel] + 500);
		        //return START_STICKY;
	        } else {
	        	Debugger.debug("shutdown on idle is NOT activated");
	        }
		}

	}
	
	private void playPauseMusic() {
		if (dPlayer.isOpen()) {
			Debugger.debug("Player is " + (dPlayer.isPlaying()?"":"not") + " playing");
			if (dPlayer.isPlaying()) {
				pauseMusic();
			}
			else {
				if (dPlayer.isPreparing()) {
					Debugger.warn("Player is still preparing");
					return;
				}
				else {
					resumeMusic();
				}
			}
		}
	}
	
	@Override
	public void selectChannel(int id) {
		dPlayer.selectChannel(id);
	}
	
	private void rateMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.rateMusic();
		}
		else {
			popNotify(getResources().getString(R.string.warning_not_open));
		}
	}

	private void unrateMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.unrateMusic();
		}
		else {
			popNotify(getResources().getString(R.string.warning_not_open));
		}
	}
	
	private void nextMusic() {
		if (dPlayer.isOpen()) {
			dPlayer.skipMusic();
	        pausedByPhoneCall = false;
		}
		else {
			popNotify(getResources().getString(R.string.warning_not_open));
		}
	}
	
	private void nextChannel() {
		if (dPlayer.isOpen()) {
			dPlayer.forwardChannel();
	        pausedByPhoneCall = false;
		}
	}
	
    private void downloadMusic(String url, String filename) {
    	MusicInfo curMusic = null;
    	if (dPlayer.isOpen()) {
    		curMusic = dPlayer.getCurMusic();
    	} 
    	else if (url == null || url.equals("")) {
    		popNotify(getResources().getString(R.string.warning_not_open));
    	}
    	
    	if (url == null || url.equals("")) {
    		if (curMusic == null || curMusic.musicUrl == null) {
    			Debugger.warn("Player not open and URL invalid, halt downloading");
    			return;
    		}
    		url = curMusic.musicUrl;
    		
    	}
    	
    	if (filename == null || filename.equals(""))  {
    		filename = Utility.getUnixFilename(curMusic.artist, curMusic.title, url);
    		if (filename == null || filename.equals("")) {
	    		Debugger.error("can't generate valid file name, abort");
	    		popNotify(getResources().getString(R.string.warning_filename));
	    		return;
    		}
    	}
    	Debugger.verbose("download filename should be \"" + filename + "\"");
    	
    	
    	Debugger.verbose("download filename is \"" + filename + "\"");

    	if (dDownloader.isOpen()) {
    		//mHandler.postDelayed(new DownloadMusicTask(url, filename), Global.SERVICE_COMMAND_DELAY);
    		dDownloader.download(url, filename);
    	}
    }
    
    private void cancelDownload(String url) {
    	if (dDownloader.isOpen()) {
    		dDownloader.cancel(url);
    	}
    }
    
    private void clearDownloadNotification(String url) {
    	
    }
    

    
    private void popNotify(String msg)
    {
        Toast.makeText(DoubanFmService.this, msg,
                Toast.LENGTH_LONG).show();
    }
    
    private WidgetContent getCurrentWidgetContent() {
    	int selChan = Preference.getSelectedChannel(DoubanFmService.this);
    	WidgetContent content = EasyDoubanFmWidget.getContent(this);
    	MusicInfo curMusic = dPlayer.getCurMusic();
    	Bitmap curPic = dPlayer.getCurPic();
    	Database db = new Database(this);
    	if (dPlayer.isOpen()) {
    		
    		FmChannel chan = db.getChannelInfo(selChan);
    		if (chan != null) {
    			
		    	String chanName = chan.name;
	    		
		    	Debugger.verbose("update widget channel");

		    	content.channel = chanName;
    		}
        	if (dPlayer.getCurMusic() != null) {
        		Debugger.debug("updating music info");

        		content.rated = (curMusic.like.equals("0")? false: true);
        		content.picture = (curPic == null)? 
        				null//BitmapFactory.decodeResource(getResources(), R.drawable.default_album)
        				: curPic;
        		content.artist = curMusic.artist;
        		content.title = curMusic.title;
        	} 
    	} else {
    		content.artist = content.title = "";
    		content.channel = getResources().getString(R.string.text_channel_unselected);
    		content.onState = Global.STATE_IDLE;
    		content.paused = false;
    		content.rated = false;
    		content.picture = null;//BitmapFactory.decodeResource(getResources(), R.drawable.default_album);
    	}
    	
    	return content;
    }
    
    private void updateActivity() {
    	WidgetContent content = getCurrentWidgetContent();    	
    	dPlayer.notifyFullStatus();
    }
    
    
    private void updateWidgets() {
    	WidgetContent content = getCurrentWidgetContent();    	
    	EasyDoubanFmWidget.updateContent(this, content, null);
    }
    
	private boolean pausedByPhoneCall = false;

	private class CallListener extends BroadcastReceiver {
		//boolean pausedByCall = false;
		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (tm == null) {
				Debugger.error("null TelephonyManager");
				return;
			}
			if (intent == null) {
				Debugger.error("null Intent");
				return;
			}
			
			String action = intent.getAction();
			if (action == null) {
				Debugger.error("null Intent Action");
				return;
			}
			
			if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				int state = tm.getCallState();
				
				switch(state) {
				case TelephonyManager.CALL_STATE_IDLE: {
					Debugger.info("Call state idle");
					synchronized(DoubanFmService.class) {
						if (pausedByPhoneCall) {
							resumeMusic();
							pausedByPhoneCall = false;
						}
					}
					break;
				}
				case TelephonyManager.CALL_STATE_OFFHOOK: {
					Debugger.info("Offhook call!");
					synchronized(DoubanFmService.class) {
						if (dPlayer.isPlaying()) {
							pausedByPhoneCall = true;
							pauseMusic();
						}
					}
					break;
				}
				case TelephonyManager.CALL_STATE_RINGING: {
					Debugger.info("Incoming call!");
					synchronized(DoubanFmService.class) {
						if (dPlayer.isPlaying()) {
							pausedByPhoneCall = true;
							pauseMusic();
						}						
					}
					break;
				}
				default:
					Debugger.error("Invalid call state: " + state);
					break;
				}
				

			}
		}
	}
	
	private class ShakingListener implements ShakeDetector.OnShakeListener {
		@Override
		public void onShake(Context context) {
			Debugger.info("Detected SHAKING!!!");
			if (!dPlayer.isOpen()) {
				return;
			}
			if (Preference.getShakeEnable(DoubanFmService.this)) {
				int qa = Preference.getQuickAction(DoubanFmService.this, Global.QUICKCONTROL_SHAKE);
				QuickAction.doQuickAction(DoubanFmService.this, qa);
			}
		}
	}
	
	
	private class AsyncNewVersionChecker extends AsyncTask<Context, Integer, String> {

		@Override
		protected String doInBackground(Context... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
