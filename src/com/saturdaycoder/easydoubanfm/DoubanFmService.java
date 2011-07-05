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

import android.media.MediaScannerConnection.*;

public class DoubanFmService extends Service implements IDoubanFmService {
	private static final String BROADCAST_PREFIX = "com.saturdaycoder.easydoubanfm";

	// actions for player
	public static final String ACTION_PLAYER_SKIP = BROADCAST_PREFIX + ".action.PLAYER_SKIP";
	public static final String ACTION_PLAYER_NEXT_CHANNEL = BROADCAST_PREFIX + ".action.PLAYER_NEXT_CHANNEL";
	public static final String ACTION_PLAYER_PLAYPAUSE = BROADCAST_PREFIX + ".action.PLAYER_PLAYPAUSE";
	public static final String ACTION_PLAYER_PAUSE = BROADCAST_PREFIX + ".action.PLAYER_PAUSE";
	public static final String ACTION_PLAYER_RESUME = BROADCAST_PREFIX + ".action.PLAYER_RESUME";
	public static final String ACTION_PLAYER_ON = BROADCAST_PREFIX + ".action.PLAYER_ON";
	public static final String ACTION_PLAYER_OFF = BROADCAST_PREFIX + ".action.PLAYER_OFF";
	public static final String ACTION_PLAYER_ONOFF = BROADCAST_PREFIX + ".action.PLAYER_ONOFF";
	public static final String ACTION_PLAYER_RATE = BROADCAST_PREFIX + ".action.PLAYER_RATE";
	public static final String ACTION_PLAYER_UNRATE = BROADCAST_PREFIX + ".action.PLAYER_UNRATE";
	public static final String ACTION_PLAYER_RATEUNRATE = BROADCAST_PREFIX + ".action.PLAYER_RATEUNRATE";
	public static final String ACTION_PLAYER_TRASH = BROADCAST_PREFIX + ".action.PLAYER_TRASH";
	public static final String ACTION_PLAYER_SELECT_CHANNEL = BROADCAST_PREFIX + ".action.PLAYER_SELECT_CHANNEL";
	public static final String ACTION_PLAYER_LOGIN = BROADCAST_PREFIX + ".action.PLAYER_LOGIN";
	public static final String ACTION_PLAYER_LOGOUT = BROADCAST_PREFIX + ".action.PLAYER_LOGOUT";
	// actions for downloader
	public static final String ACTION_DOWNLOADER_DOWNLOAD = BROADCAST_PREFIX + ".action.DOWNLOADER_DOWNLOAD";
	public static final String ACTION_DOWNLOADER_CANCEL = BROADCAST_PREFIX + ".action.DOWNLOADER_CANCEL";
	public static final String ACTION_DOWNLOADER_CLEAR_NOTIFICATION = BROADCAST_PREFIX + ".action.DOWNLOADER_CLEAR_NOTIFICATION";
	// extra for other, i.e. ui, etc.
	public static final String ACTION_WIDGET_UPDATE = BROADCAST_PREFIX + ".action.UPDATE_WIDGET";
	public static final String ACTION_ACTIVITY_UPDATE = BROADCAST_PREFIX + ".action.UPDATE_ACTIVITY";
	public static final String ACTION_NULL = BROADCAST_PREFIX + ".action.NULL";
	// extra for player
	public static final String EXTRA_MUSIC_URL = "extra.MUSIC_URL";
	public static final String EXTRA_PICTURE_URL = "extra.MUSIC_URL";
	
	public static final String EXTRA_LOGIN_USERNAME = "extra.LOGIN_USERNAME";
	public static final String EXTRA_LOGIN_PASSWD = "extra.LOGIN_PASSWD";
	
	//extra for downloader
	public static final String EXTRA_DOWNLOADER_DOWNLOAD_FILENAME = "extra.DOWNLOAD_FILENAME";
	
	// service status
	public static final String EVENT_PLAYER_MUSIC_PREPARE_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PREPARE_PROGRESS";
	public static final String EVENT_PLAYER_POWER_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_POWER_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PROGRESS";
	public static final String EVENT_PLAYER_PICTURE_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_PICTURE_STATE_CHANGED";
	public static final String EVENT_DOWNLOADER_STATE_CHANGED = BROADCAST_PREFIX + ".event.DOWNLOADER_STATE_CHANGED";
	public static final String EVENT_DOWNLOADER_PROGRESS = BROADCAST_PREFIX + ".event.DOWNLOADER_PROGRESS";
	public static final String EVENT_CHANNEL_CHANGED = BROADCAST_PREFIX + ".event.CHANNEL_CHANGED";
	public static final String EVENT_LOGIN_STATE_CHANGED = BROADCAST_PREFIX + ".event.LOGIN_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_RATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_RATED";
	public static final String EVENT_PLAYER_MUSIC_UNRATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_UNRATED";
	public static final String EVENT_PLAYER_MUSIC_BANNED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_BANNED";
	
	
	public static final String EXTRA_STATE = "extra.STATE";
	public static final String EXTRA_PROGRESS = "extra.PROGRESS";
	public static final String EXTRA_DOWNLOAD_SESSION = "extra.DOWNLOAD_SESSION";
	public static final String EXTRA_MUSIC_TITLE = "extra.MUSIC_TITLE";
	public static final String EXTRA_MUSIC_ARTIST = "extra.MUSIC_ARTIST";
	public static final String EXTRA_MUSIC_ISRATED = "extra.MUSIC_ISRATED";
	public static final String EXTRA_CHANNEL = "extra.CHANNEL";
	public static final String EXTRA_REASON = "extra.REASON";
	
	
	public static final int STATE_PREPARE = 0;
	public static final int STATE_STARTED = 1;	
	public static final int STATE_FINISHED = 2;
	public static final int STATE_FAILED = 3;
	public static final int STATE_CANCELLED = 4;
	public static final int STATE_IDLE = 5;
	public static final int STATE_ERROR = 6;
	
	public static final int STATE_MUSIC_SKIPPED = 7;
	public static final int STATE_MUSIC_PAUSED = 8;
	public static final int STATE_MUSIC_RESUMED = 9;
	
	public static final int INVALID_STATE = -1;
	

	public static final int REASON_NETWORK_IO_ERROR = 3;
	public static final int REASON_NETWORK_INVALID_URL = 4;
	
	public static final int REASON_MUSIC_FINISHED = 5;
	public static final int REASON_MUSIC_BANNED = 6;
	public static final int REASON_MUSIC_RATED = 7;
	public static final int REASON_MUSIC_UNRATED = 8;
	public static final int REASON_MUSIC_SKIPPED = 9;
	public static final int REASON_MUSIC_NEW_LIST = 10;
	public static final int REASON_MUSIC_LIST_EMPTY = 11;
	
	
	
	public static final int REASON_DOWNLOAD_STORAGE_INVALID = 12;
	public static final int REASON_DOWNLOAD_STORAGE_FULL = 13;
	public static final int REASON_DOWNLOAD_STORAGE_IO_ERROR = 14;
	public static final int REASON_DOWNLOAD_INVALID_FILENAME = 15;
	
	public static final int REASON_API_REQUEST_ERROR = 16;
	
	public static final int SERVICE_NOTIFICATION_ID = 1;
	public static final int DOWNLOAD_DEFAULT_SESSIONID = 2;
	
	
	public static final String QUICKCONTROL_SHAKE = "act_shake";
	public static final String QUICKCONTROL_MEDIA_BUTTON = "act_media_button";
	public static final String QUICKCONTROL_CAMERA_BUTTON = "act_camera_button";
	
	public static final int QUICKACT_NEXT_MUSIC = 0;
	public static final int QUICKACT_NEXT_CHANNEL = 1;
	public static final int QUICKACT_DOWNLOAD_MUSIC = 2;
	public static final int QUICKACT_PLAY_PAUSE = 3;
	public static final int QUICKACT_NONE = 4;
	
	private final IBinder mBinder = new LocalBinder();


	HttpParams httpParameters;	
	
	// Event Listeners
	private PhoneCallListener phoneCallListener;
	private PhoneControlListener phoneControlListener;
	private ShakeDetector shakeDetector;
	
	
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
		//lastStopReason = DoubanFmApi.TYPE_NEW;

		dPlayer = new DoubanFmPlayer(this);
		dDownloader = new DoubanFmDownloader(this);
 
		// work-around on gingerbread restart bug: onStartCommand will not execute,
		// so update widgets here
		if (android.os.Build.VERSION.SDK_INT == 9 
				|| android.os.Build.VERSION.SDK_INT == 10) {
			updateWidgets();
		}
		
		
		// listens to the call state event
		phoneCallListener = new PhoneCallListener();
		IntentFilter phoneFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(phoneCallListener, phoneFilter);

		// listens to hard button event
		phoneControlListener = new PhoneControlListener();
		IntentFilter mfilter = new IntentFilter();
		mfilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		mfilter.addAction(Intent.ACTION_CAMERA_BUTTON);
		registerReceiver(phoneControlListener, mfilter);
		
		// listens to shake event
		shakeDetector = new ShakeDetector(this);		
		shakeDetector.registerOnShakeListener(phoneControlListener);		
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
	        //mDelayedStopHandler.removeCallbacksAndMessages(null);
	        Message msg = mDelayedStopHandler.obtainMessage();
	        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
			return START_NOT_STICKY;
		}
		
		mDelayedStopHandler.removeCallbacksAndMessages(null);
		mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
		
		Debugger.warn("Intent action=\"" + intent.getAction() + "\" flags=" + flags + " startId=" + startId);
		String action = intent.getAction();
        if (action.equals(ACTION_PLAYER_ON)) {
        	Debugger.info("Douban service starts with ON command");
       		openPlayer();
        }        
        if (action.equals(ACTION_PLAYER_OFF)) {        	
        	Debugger.info("Douban service starts with OFF command");
      		closePlayer();
        }	
        if (action.equals(ACTION_PLAYER_ONOFF)) {
        	Debugger.info("Douban service starts with ON/OFF command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		closePlayer();
        	}	
        }
        if (action.equals(ACTION_PLAYER_SKIP)) {
        	Debugger.info("Douban service starts with SKIP command");
        	nextMusic();
        }
        if (action.equals(ACTION_PLAYER_NEXT_CHANNEL)) {
        	Debugger.info("Douban service starts with SKIP command");
        	nextChannel();
        }
        if (action.equals(ACTION_PLAYER_TRASH)) {
        	Debugger.info("Douban service starts with TRASH command");
        	banMusic();
        }
        if (action.equals(ACTION_PLAYER_RATE)) {
        	Debugger.info("Douban service starts with RATE command");
       		rateMusic();
        }
        if (action.equals(ACTION_PLAYER_UNRATE)) {
        	Debugger.info("Douban service starts with UNRATE command");
       		unrateMusic();
        }
        if (action.equals(ACTION_PLAYER_RATEUNRATE)) {
        	Debugger.info("Douban service starts with RATE/UNRATE command");
        	if (!dPlayer.isMusicRated())
        		rateMusic();
        	else 
        		unrateMusic();
        }
        if (action.equals(ACTION_PLAYER_PLAYPAUSE)) {
        	
        	Debugger.info("Douban service starts with PLAY/PAUSE command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		playPauseMusic();
        	}
        }
        if (action.equals(ACTION_PLAYER_PAUSE)) {
        	
        	Debugger.info("Douban service starts with PAUSE command");
        	pauseMusic();
        }
        if (action.equals(ACTION_PLAYER_RESUME)) {
        	
        	Debugger.info("Douban service starts with RESUME command");
        	if (!dPlayer.isOpen()) {
        		openPlayer();
        	}
        	else {
        		resumeMusic();
        	}
        }
        if (action.equals(ACTION_PLAYER_LOGIN)) {
        	String username = intent.getStringExtra(EXTRA_LOGIN_USERNAME); 
        	String passwd = intent.getStringExtra(EXTRA_LOGIN_PASSWD); 
        	Debugger.info("Douban service starts with LOGIN command");
        	
        	login(username, passwd);
        }
        if (action.equals(ACTION_PLAYER_LOGOUT)) {
        	Debugger.info("Douban service starts with LOGOUT command");        	
        	logout();
        }
        if (action.equals(ACTION_PLAYER_SELECT_CHANNEL)) {
        	int chann = intent.getIntExtra(EXTRA_CHANNEL, 0);
        	Debugger.info("Douban service starts with SELECT_CHANNEL: " + chann);
        	selectChannel(chann);
        }
        
        
        if (action.equals(ACTION_DOWNLOADER_DOWNLOAD)) {
        	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
        	String filename = intent.getStringExtra(EXTRA_DOWNLOADER_DOWNLOAD_FILENAME);
        	Debugger.info("Douban service starts with DOWNLOAD command");
        	openDownloader();
        	downloadMusic(url, filename);
        }
        if (action.equals(ACTION_DOWNLOADER_CANCEL)) {
        	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
        	Debugger.info("Douban service starts with CANCEL DOWNLOAD command");
        	openDownloader();
        	cancelDownload(url);
        }
        if (action.equals(ACTION_DOWNLOADER_CLEAR_NOTIFICATION)) {
        	String url = intent.getStringExtra(EXTRA_MUSIC_URL);
        	Debugger.info("Douban service starts with CLEAR NOTIFICATION command");
        	openDownloader();
        	clearDownloadNotification(url);
        }


        if (action.equals(DoubanFmService.ACTION_WIDGET_UPDATE)) {
        	Debugger.info("Douban service starts with UPDATE_WIDGET");
        	updateWidgets();
        }
        
        if (action.equals(DoubanFmService.ACTION_ACTIVITY_UPDATE)) {
        	Debugger.info("Douban service starts with UPDATE_ACTIVITY");
        	updateActivity();
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
        
        try {
			if (shakeDetector != null ) {
				shakeDetector.unregisterOnShakeListener(phoneControlListener);
				shakeDetector.stop();
				shakeDetector = null;
			}
        
        	if (phoneControlListener != null) {
        		unregisterReceiver(phoneControlListener);
        		phoneControlListener = null;
        	}
        	
        	if (phoneCallListener != null) {
        		unregisterReceiver(phoneCallListener);
        		phoneCallListener = null;
        	}
        } catch (Exception e) {
        	
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
		//closePlayer();
		//closeDownloader();
	}	
	
	private Runnable mBanMusicTask = new Runnable() {
		   public void run() {
			   dPlayer.banMusic();
		   }
		};	
	private void banMusic() {
		if (dPlayer.isOpen()) {
			mHandler.removeCallbacks(mBanMusicTask);
	        mHandler.postDelayed(mBanMusicTask, 50);			
		}
    }

	@Override
	public boolean login(String email, String passwd) {
		if (dPlayer.isOpen()) {
			return dPlayer.login(email, passwd);
		}
		return false;
	}

	//@Override
	private void logout() {
		if (dPlayer.isOpen()) {
			dPlayer.logout();
		}
	}
	


	private void openDownloader() {
		dDownloader.open();
	}
	
	private void closeDownloader() {
		dDownloader.close();
	}

	private Runnable mOpenPlayerTask = new Runnable() {
	   public void run() {
		   dPlayer.open();
	   }
	};
	
	private void openPlayer() {
		// start foreground with notification
		Notification fgNotification = new Notification(R.drawable.icon,
				getResources().getString(R.string.app_name),
		        System.currentTimeMillis());
		fgNotification.flags |= Notification.FLAG_NO_CLEAR;
		Intent it = new Intent(this, EasyDoubanFm.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, it, 0);
		fgNotification.setLatestEventInfo(this, "", "", pi);		
		startForeground(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
		
		mHandler.removeCallbacks(mOpenPlayerTask);
        mHandler.postDelayed(mOpenPlayerTask, 50);
	}
	
	private Runnable mClosePlayerTask = new Runnable() {
	   public void run() {
		   dPlayer.close();
	   }
	};
	private void closePlayer() {
		stopForeground(true);
		
		Debugger.debug("DoubanFm Control Service unregistered");
		mHandler.removeCallbacks(mClosePlayerTask);
        mHandler.postDelayed(mClosePlayerTask, 50);
	}
	
	private void closeService() {
		Debugger.info("closeService");
		
		closeDownloader();
		closePlayer();
		stopSelf();
	}
	private Runnable mResumeMusicTask = new Runnable() {
		   public void run() {
			   dPlayer.resumeMusic();
		   }
		};	
	private void resumeMusic() {
		if (dPlayer.isOpen()) {
			mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
			// start foreground with notification
			Notification fgNotification = new Notification(R.drawable.icon,
					getResources().getString(R.string.app_name),
			        System.currentTimeMillis());
			fgNotification.flags |= Notification.FLAG_NO_CLEAR;
			Intent it = new Intent(this, EasyDoubanFm.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, it, 0);
			fgNotification.setLatestEventInfo(this, "", "", pi);		
			startForeground(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
			
			mHandler.removeCallbacks(mResumeMusicTask);
	        mHandler.postDelayed(mResumeMusicTask, 50);
		}
	}
	private Runnable mPauseMusicTask = new Runnable() {
		   public void run() {
			   dPlayer.pauseMusic();
		   }
		};	
	private void pauseMusic() {
		if (dPlayer.isOpen()) {
			stopForeground(true);
			mHandler.removeCallbacks(mPauseMusicTask);
	        mHandler.postDelayed(mPauseMusicTask, 50);
	        // make sure the service will shut down on its own if it was
	        // just started but not bound to and nothing is playing
	        mDelayedPausedStopHandler.removeCallbacksAndMessages(null);
	        
	        if (Preference.getShutdownOnIdleEnable(this)) {
	        	int delay = Preference.getMaxIdleTime(this);
	        	Debugger.debug("shutdown on idle is activated: delay is " + delay);
		        Message msg = mDelayedPausedStopHandler.obtainMessage();
		        mDelayedPausedStopHandler.sendMessageDelayed(msg, 60000 * delay + 500);
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
	
	private class SelectChannelRunnable implements Runnable {
		private int chanId;
		public SelectChannelRunnable(int chanId) {
			this.chanId = chanId; 
		}
		@Override
		public void run() {
			dPlayer.selectChannel(chanId);
		}
	}
	@Override
	public void selectChannel(int id) {
		if (dPlayer.isOpen()) {
	        mHandler.postDelayed(new SelectChannelRunnable(id), 50);
		}
	}
	
	private Runnable mRateMusicTask = new Runnable() {
		   public void run() {
			   dPlayer.rateMusic();
		   }
		};		
	private void rateMusic() {
		if (dPlayer.isOpen()) {
			mHandler.removeCallbacks(mRateMusicTask);
	        mHandler.postDelayed(mRateMusicTask, 50);
		}
	}
	private Runnable mUnrateMusicTask = new Runnable() {
		   public void run() {
			   dPlayer.unrateMusic();
		   }
		};		
	private void unrateMusic() {
		if (dPlayer.isOpen()) {
			mHandler.removeCallbacks(mUnrateMusicTask);
	        mHandler.postDelayed(mUnrateMusicTask, 50);
		}

	}
	
	
	private Runnable mNextMusicTask = new Runnable() {
		   public void run() {
				dPlayer.skipMusic();
		   }
		};		
	private void nextMusic() {
		if (dPlayer.isOpen()) {
			mHandler.removeCallbacks(mNextMusicTask);
	        mHandler.postDelayed(mNextMusicTask, 50);
		}
	}
	
	
	private Runnable mNextChannelTask = new Runnable() {
		   public void run() {
			   dPlayer.forwardChannel();
		   }
		};	
	private void nextChannel() {
		if (dPlayer.isOpen()) {
			mHandler.removeCallbacks(mNextChannelTask);
	        mHandler.postDelayed(mNextChannelTask, 50);
		}
	}
	

	private class DownloadMusicTask implements Runnable {
		private String url;
		private String filename;
		public DownloadMusicTask(String url, String filename) {
			this.url = url;
			this.filename = filename;
		}
		@Override
		public void run() {
			dDownloader.download(url, filename);
		}
	}

    private void downloadMusic(String url, String filename) {
    	MusicInfo curMusic = null;
    	if (dPlayer.isOpen()) {
    		curMusic = dPlayer.getCurMusic();
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
	    		return;
    		}
    	}
    	Debugger.verbose("download filename should be \"" + filename + "\"");
    	
    	
    	Debugger.verbose("download filename is \"" + filename + "\"");

    	if (dDownloader.isOpen()) {
    		mHandler.postDelayed(new DownloadMusicTask(url, filename), 50);
    	}
    }
    
    
    private class CancelDownloadTask implements Runnable {
    	private String url;
    	public CancelDownloadTask(String url) {
    		this.url = url;
    	}
    	@Override 
    	public void run() {
    		dDownloader.cancel(url);
    	}
    }
    private void cancelDownload(String url) {
    	if (dDownloader.isOpen()) {
    		mHandler.postDelayed(new CancelDownloadTask(url), 50);
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
    		content.onState = EasyDoubanFmWidget.STATE_OFF;
    		content.paused = false;
    		content.rated = false;
    		content.picture = null;//BitmapFactory.decodeResource(getResources(), R.drawable.default_album);
    	}
    	
    	return content;
    }
    
    private void updateActivity() {
    	WidgetContent content = getCurrentWidgetContent();    	

    	EasyDoubanFm.updateContents(content);
    	
    	int curPos = dPlayer.getCurPosition();
    	int curDur = dPlayer.getCurDuration();
    	
    	EasyDoubanFm.updatePosition(curPos, curDur);
    }
    
    
    private void updateWidgets() {
    	WidgetContent content = getCurrentWidgetContent();    	
    	EasyDoubanFmWidget.updateContent(this, content, null);
    }
    

	
	
	//private Map<Integer, Notification> notifications = new HashMap<Integer, Notification>(); 
	
	private class PhoneCallListener extends BroadcastReceiver {
		boolean pausedByCall = false;
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
					//if (pausedByCall) {
						pausedByCall = false;
						resumeMusic();
						
					//}
					
					break;
				}
				case TelephonyManager.CALL_STATE_OFFHOOK: {
					Debugger.info("Offhook call!");
					if (dPlayer.isPlaying()) {
						pausedByCall = true;
						pauseMusic();
					}
					break;
				}
				case TelephonyManager.CALL_STATE_RINGING: {
					Debugger.info("Incoming call!");
					if (dPlayer.isPlaying()) {
						pausedByCall = true;
						pauseMusic();
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
	   
	
	private boolean doQuickAction(int quickact) {
		switch (quickact) {
		case QUICKACT_NEXT_MUSIC:
			nextMusic();
			return true;
		case QUICKACT_NEXT_CHANNEL:
			nextChannel();
			return true;
		case QUICKACT_PLAY_PAUSE:
			playPauseMusic();
			return true;
		case QUICKACT_DOWNLOAD_MUSIC:
			openDownloader();
			downloadMusic(null, null);
			return true;
		default:
			return false;
		}
				
	}
	
	private boolean handleShakeControl() {
		Debugger.info("Detected SHAKING!!!");
		if (!dPlayer.isOpen()) {
			return false;
		}
		if (Preference.getShakeEnable(this)) {
			int qa = Preference.getQuickAction(this, QUICKCONTROL_SHAKE);
			return doQuickAction(qa);
		}
		else {
			return false;
		}
	}
	
	private boolean handleMediaButtonControl(int keycode, int keyaction, long keytime) {

		Debugger.debug("keycode = " + keycode + " action = " + keyaction 
				+ "eventtime = " + keytime);
		if (!dPlayer.isOpen()) {
			return false;
		}
		if (!Preference.getMediaButtonEnable(this)) {
			return false;
		}	
		switch (keyaction) {
		case KeyEvent.ACTION_DOWN: {
			int qa = Preference.getQuickAction(this, QUICKCONTROL_MEDIA_BUTTON);
			return doQuickAction(qa);
		}
		default:
			return false;
		}	
		
	}
	
	private boolean handleCameraButtonControl() {

	
		Debugger.info("ACTION_CAMERA_BUTTON heard");
		if (!dPlayer.isOpen()) {
			return false;
		}
		if (!Preference.getCameraButtonEnable(this)) {
			return false;
		}		

		return doQuickAction(Preference.getQuickAction(this, QUICKCONTROL_CAMERA_BUTTON));
	}
	
	
	private class AsyncPlayerTask extends AsyncTask<Object, Integer, Integer> {
		@Override
		public Integer doInBackground(Object...params) {
			try {
				DoubanFmPlayer object = (DoubanFmPlayer)params[0];
				Method method = (Method)params[1];
				method.invoke(object);
			} catch (Exception e) {
				Debugger.error("error doInBackground: " + e.toString());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private class TimerPlayerTask extends TimerTask {
		private Method method;
		public TimerPlayerTask(Method method) {
			this.method = method;
		}
		@Override
		public void run() {
			
			try {
				method.invoke(dPlayer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
	private class PhoneControlListener extends BroadcastReceiver 
			 implements ShakeDetector.OnShakeListener {
		
		@Override
		public void onShake(Context context) {
			handleShakeControl();
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				Debugger.error("null Intent");
				return;
			}
		
			String action = intent.getAction();
			if (action == null) {
				Debugger.error("null Intent action");
				return;
			}
			
			if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {

			
				KeyEvent ke = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (ke == null) {
					Debugger.error("ACTION_MEDIA_BUTTON heard but null KeyEvent");
					return;
				}
				
				int keycode = ke.getKeyCode();
				int keyaction = ke.getAction();
				long eventtime = ke.getEventTime();
				
				if (handleMediaButtonControl(keycode, keyaction, eventtime)) {
					abortBroadcast();
					setResultData(null);
				}
			}
			
			if (action.equals(Intent.ACTION_CAMERA_BUTTON)) {
				if (handleCameraButtonControl()) {
					abortBroadcast();
					setResultData(null);
				}
			}
		}
	}
}
