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
	
	// service status
	public static final String STATE_STARTED = "com.saturdaycoder.easydoubanfm.state.started";
	public static final String STATE_FINISHED = "com.saturdaycoder.easydoubanfm.state.finished";
	public static final String STATE_PAUSED = "com.saturdaycoder.easydoubanfm.state.paused";
	public static final String STATE_RESUMED = "com.saturdaycoder.easydoubanfm.state.resumed";
	public static final String STATE_DOWNLOADED = "com.saturdaycoder.easydoubanfm.state.downloaded";
	public static final String STATE_DOWNLOAD_FAILED = "com.saturdaycoder.easydoubanfm.state.download_failed";
	public static final String STATE_FAVOR_ADDED = "com.saturdaycoder.easydoubanfm.state.favor_added";
	public static final String STATE_FAVOR_REMOVED = "com.saturdaycoder.easydoubanfm.state.favor_removed";
	public static final String STATE_FAVOR_TRASHED = "com.saturdaycoder.easydoubanfm.state.trashed";
	public static final String STATE_FAILED = "com.saturdaycoder.easydoubanfm.state.failed";
	
	// for binding service
	public static final String BINDTYPE_FM = "FM";
	public static final String BINDTYPE_DOWNLOAD = "DOWNLOAD";
	public static final String EXTRA_BINDSERVICE_TYPE = "bindtype";
	
	// actions for FM
	public static final String CONTROL_NEXT = "com.saturdaycoder.easydoubanfm.control.next";
	public static final String CONTROL_PLAYPAUSE = "com.saturdaycoder.easydoubanfm.control.playpause";
	public static final String CONTROL_PAUSE = "com.saturdaycoder.easydoubanfm.control.pause";
	public static final String CONTROL_RESUME = "com.saturdaycoder.easydoubanfm.control.resume";
	public static final String CONTROL_CLOSE = "com.saturdaycoder.easydoubanfm.control.close";
	public static final String CONTROL_DOWNLOAD = "com.saturdaycoder.easydoubanfm.control.download";
	public static final String CONTROL_DOWNLOAD_NOTIFICATION_CLICKED = "com.saturdaycoder.easydoubanfm.control.download_clicked";
	public static final String CONTROL_UNRATE = "com.saturdaycoder.easydoubanfm.control.unrate";
	public static final String CONTROL_RATE = "com.saturdaycoder.easydoubanfm.control.rate";
	public static final String CONTROL_TRASH = "com.saturdaycoder.easydoubanfm.control.trash";
	public static final String CONTROL_SELECT_CHANNEL = "com.saturdaycoder.easydoubanfm.control.select_channel";
	public static final String CONTROL_UPDATE_WIDGET = "com.saturdaycoder.easydoubanfm.control.update_widget";
	
	// actions for downloader
	public static final String ACTION_DOWNLOAD = "com.saturdaycoder.easydoubanfm.download";
	public static final String ACTION_CANCEL_DOWNLOAD = "com.saturdaycoder.easydoubanfm.cancel_download";
	public static final String ACTION_CLEAR_NOTIFICATION = "com.saturdaycoder.easydoubanfm.clear_notification";
	public static final String ACTION_NULL = "com.saturdaycoder.easydoubanfm.null";
	public static final String EXTRA_DOWNLOAD_SESSION = "session";
	public static final String EXTRA_DOWNLOAD_URL = "url";
	public static final String EXTRA_DOWNLOAD_FILENAME = "filename";
	
	public static final String NULL_EVENT = "com.saturdaycoder.easydoubanfm.null";

	private static final int MAX_HISTORY_COUNT = 20;
	private static final int MAX_PENDING_COUNT = 20;
	private static final int MAX_PREBUFFER_PLAYER_COUNT = 2;
	private ArrayList<MusicInfo> musicHistory;
	private ArrayList<MusicInfo> pendingMusicList;
	private MusicInfo curMusic;
	private Bitmap curPic;
	private MusicInfo lastMusic;
	private PlayMusicThread playThread = null;	
	private final IBinder mBinder = new LocalBinder();
	private GetPictureTask picTask = null;
	
	
	// SYSTEM SERVICES
	private NotificationManager notificationManager;
	private AudioManager audioManager;
	private MediaScannerConnection.MediaScannerConnectionClient scannerClient;

	private Database db;
	HttpParams httpParameters;
	private char lastStopReason;
	private Handler mainHandler;
	private Downloader downloader;
	
	// variables that need synchronize
	private boolean isFmOn;
	private boolean isDownloaderOn;
	
	
	// Event Listeners
	private DownloadReceiver downloadListener;
	private MediaButtonListener mediaButtonListener;
	private ShakeDetector.OnShakeListener shakeListener;
	private ShakeDetector shakeDetector;
	private DoubanFmControlReceiver controlListener;
	
	
	// for service foreground notification
	private static final int serviceNotId = 1;
	private static final int downloadDefaultNotId = 2;
	private Notification fgNotification;
	
	
	public class LocalBinder extends Binder {
		
		DoubanFmService getService() {
			return DoubanFmService.this;
		}
	}
	
	/*@Override
	public void onStart(Intent intent, int startId) {
		Debugger.verbose("Service onStart");
		Debugger.error("ON START");
		super.onStart(intent, startId);

	}*/
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Debugger.warn("SERVICE ONSTARTCOMMAND");
		
		if (intent == null) { // it tells us the service was killed by system.
			popNotify(getResources().getString(R.string.text_killed_for_memory));
			//closeDownloader();
			//closeFM();
			updateWidgets();
			return START_NOT_STICKY;
		}
		
		
		Debugger.warn("Intent action=\"" + intent.getAction() + "\" flags=" + flags + " startId=" + startId);
		
		String act = intent.getExtras().getString(EXTRA_BINDSERVICE_TYPE);
		Debugger.verbose("action is \"" + act + "\"");
		
		// open FM alone
		if (act != null && act.equals(BINDTYPE_FM)) {
			openFM();
		}
		// open downloader alone
		else if (act != null && act.equals(BINDTYPE_DOWNLOAD)) {
			openDownloader();
			
			if (isDownloaderOn) {
				String url = intent.getExtras().getString(EXTRA_DOWNLOAD_URL);
				String filename = intent.getExtras().getString(EXTRA_DOWNLOAD_FILENAME);
				int sessionId = intent.getExtras().getInt(EXTRA_DOWNLOAD_SESSION);
				notificationManager.cancel(sessionId);
				if (url != null && filename != null) {
					Debugger.info("got extra url=\"" + url + "\" filename=\"" + filename + "\"");
					downloader.download(sessionId, url, filename);
					
				} else {
					Debugger.info("got no extra url and filename, download current playing music");
					if (curMusic != null) {
						int sid = 0;
						try {
							sid = Integer.parseInt(curMusic.sid);
						} catch (Exception e) {
							Debugger.error("SID is not a number: " + e.toString());
							sid = downloadDefaultNotId;
						}
						downloader.download(sid, curMusic.musicUrl, 
								getUnixFilename(curMusic.artist, curMusic.title, curMusic.musicUrl));
					}
				}
			}
		}
		// open neither FM nor downloader
		else  {
			Debugger.warn("open neither FM nor downloader");
			return START_NOT_STICKY;
		}
		
		
		/*fgNotification = new Notification(R.drawable.icon,
				getResources().getString(R.string.app_name),
                System.currentTimeMillis());
		
		fgNotification.flags |= Notification.FLAG_NO_CLEAR;
		Intent it = new Intent(NULL_EVENT);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
	
		fgNotification.setLatestEventInfo(this, "",
                   "", pi);
		
		startForeground(serviceNotId, fgNotification);*/
		
		return START_STICKY;
		
	}
	

	@Override
	public boolean login(String email, String passwd) {
		if (email == null || email.equals("") 
				|| passwd == null || passwd.equals("")) {
			return false;
		}
			
		try {
			session = DoubanFmApi.login(email, passwd, Preference.getClientVersion());
			Preference.setLogin(this, (session != null));
		} catch (IOException e) {
			popNotify(getResources().getString(R.string.text_network_error));
			Debugger.error("network error: " + e.toString());
			session = null;
		}
		return (session != null);
	}

	@Override
	public void logout() {
		Preference.setLogin(this, false);
		session = null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Debugger.debug("service onCreate");
		
		curMusic = lastMusic = null;
		curPic = null;
		
		lastStopReason = DoubanFmApi.TYPE_NEW;
        // get system audio manager
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
        if (audioManager  == null) {
        	Debugger.error("Error getting system AUDIO_SERVICE");
        	popNotify("Audio Manager not exist!!");
        	return;
        }
 
		// work-around on gingerbread restart bug: onStartCommand will not execute,
		// so update widgets here
		if (android.os.Build.VERSION.SDK_INT == 9 
				|| android.os.Build.VERSION.SDK_INT == 10) {
			updateWidgets();
		}
		
		
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		httpParameters = new BasicHttpParams();
		int timeoutConnection = Preference.getConnectTimeout(this);
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = Preference.getSocketTimeout(this);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		DoubanFmApi.setHttpParameters(httpParameters);
	}

	
	@Override
	public void openDownloader() {
		if (!isDownloaderOn) {
			synchronized(DoubanFmService.this) {
				if (!isDownloaderOn) {
					downloader = new Downloader(this);
					
					IntentFilter dfilter = new IntentFilter();
					dfilter.addAction(DoubanFmService.ACTION_DOWNLOAD);
					dfilter.addAction(DoubanFmService.ACTION_CANCEL_DOWNLOAD);
					dfilter.addAction(DoubanFmService.ACTION_CLEAR_NOTIFICATION);
					downloadListener = new DownloadReceiver();
					registerReceiver(downloadListener, dfilter);
					
					isDownloaderOn = true;
				}

			}
		}
	}
	
	@Override
	public void closeDownloader() {
		if (isDownloaderOn) {
			synchronized(this) {
				if (isDownloaderOn) {
					Debugger.verbose("closeDownloader: cancel all tasks");
					downloader.cancelAll();
					Debugger.verbose("closeDownloader: unregister download receiver");
					try {
						unregisterReceiver(downloadListener);
					} catch (IllegalArgumentException e) {
						Debugger.error("download receiver not registered, skip unregistering");
					}
					isDownloaderOn = false;
					downloader = null;
				}
			}
		}
		
		if (!isFmOn) {
			synchronized(this) {
				if (!isFmOn) {
					Debugger.verbose("closeDownloader: FM is not on. close service");
					closeService();
				}
			}
		}
	}
	@Override
	public void openFM() {
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, 0);

		if (!isFmOn) {
			synchronized(this) {
				if (!isFmOn) {
			        if (controlListener == null)  {
			            controlListener = new DoubanFmControlReceiver();  
			        }
			        
			       
			        mainHandler = new DoubanFmHandler();
			        playThread = new PlayMusicThread(mainHandler, audioManager);
			        playThread.start();
			        
			        musicHistory = new ArrayList<MusicInfo>();
			        pendingMusicList = new ArrayList<MusicInfo>();
			        
					IntentFilter filter = new IntentFilter();
					filter.addAction(DoubanFmService.CONTROL_NEXT);
					filter.addAction(DoubanFmService.CONTROL_PLAYPAUSE);
					filter.addAction(DoubanFmService.CONTROL_PAUSE);
					filter.addAction(DoubanFmService.CONTROL_RESUME);
					filter.addAction(DoubanFmService.CONTROL_CLOSE);
					filter.addAction(DoubanFmService.CONTROL_DOWNLOAD);
					filter.addAction(DoubanFmService.CONTROL_RATE);
					filter.addAction(DoubanFmService.CONTROL_UNRATE);
					filter.addAction(DoubanFmService.CONTROL_TRASH);
					filter.addAction(DoubanFmService.CONTROL_SELECT_CHANNEL);
					filter.addAction(DoubanFmService.CONTROL_UPDATE_WIDGET);
					registerReceiver(controlListener, filter); 
							
					mediaButtonListener = new MediaButtonListener();
					IntentFilter mfilter = new IntentFilter();
					mfilter.addAction(Intent.ACTION_MEDIA_BUTTON);
					registerReceiver(mediaButtonListener, mfilter);
					
					Debugger.verbose("DoubanFm Control Service registered");
					
					shakeDetector = new ShakeDetector(this);
					
					shakeListener = new ShakeDetector.OnShakeListener() {
						
						@Override
						public void onShake() {
							Debugger.info("Detected SHAKING!!!");
							Intent i = new Intent(DoubanFmService.CONTROL_NEXT);
							sendBroadcast(i);
						}
					};
					
					shakeDetector.registerOnShakeListener(shakeListener);
					
					try {
						shakeDetector.start();
					} catch (Exception e) {
						Debugger.error("no shake sensor: " + e.toString());
					}
					
					// get stored channel table
					db = new Database(this);		
					// get channel table if it's not existing
					FmChannel[] chans = FmChannel.Channels;//db.getChannels();
					
					
					// the API isn't updated by douban.com. So write it by 
					// myself instead of fetching from douban.com
					db.clearChannels();
					for(FmChannel fc: FmChannel.Channels) {
						db.saveChannel(fc);
					}
					/*if (chans == null || chans.length <= 1) {
						EasyDoubanFmWidget.updateWidgetChannel(this, 
								getResources().getString(R.string.text_channel_updating));
						try {
							chans = DoubanFmApi.getChannelTable();
						} catch (IOException e) {
							popNotify("由于网络原因无法获取频道列表，请稍后重试");
							Intent i = new Intent(CONTROL_CLOSE);
							sendBroadcast(i);
							return;
						}
			
						for (FmChannel c: chans) {
							db.saveChannel(c);
						}
						Preference.selectChannel(this, 0);
						
					}*/
					
					// get the currently selected channel
					int ci = Preference.getSelectedChannel(this);
					int idx = 0;
					for (; idx < chans.length; ++idx) {
						if (ci == chans[idx].channelId) {
							break;
						}
					}
					if (idx == chans.length) {
						// selected channel not exist
						Preference.selectChannel(this, 0);
						ci = 0;
					}
					
					FmChannel c = db.getChannelInfo(ci);
					
					
					// check login
					boolean login = Preference.getLogin(this);
					if (login) {
						EasyDoubanFmWidget.updateWidgetChannel(this, 
								getResources().getString(R.string.text_login_inprocess));
						String email = Preference.getAccountEmail(this);
						String passwd = Preference.getAccountPasswd(this);
						try {
							session = DoubanFmApi.login(email, passwd, Preference.getClientVersion());
							if (session != null) {
								Preference.setAccountEmail(this, email);
								Preference.setAccountPasswd(this, passwd);
								Preference.setLogin(this, true);
							} else {
								Preference.setAccountEmail(this, email);
								Preference.setAccountPasswd(this, null);
								Preference.setLogin(this, false);
							}
						} catch (Exception e) {
							Debugger.error("IO ERROR loging in: " + e.toString());
							popNotify("由于网络原因登录失败");
							session = null;
						}
					}
					else {
						session = null;
					}
					
					EasyDoubanFmWidget.updateWidgetOnOffButton(this, 1);
					EasyDoubanFmWidget.updateWidgetChannel(this, c.getDisplayName(login));
					
					isFmOn = true;
					
					nextMusic();
				}
			}
		}
	}
	
	
	@Override
	public void closeFM() {
		if (isFmOn) {
			synchronized(this) {
				if (isFmOn) {
					Debugger.info("DoubanFmService closeFM");
					EasyDoubanFmWidget.updateWidgetOnOffButton(this, 0);
					EasyDoubanFmWidget.updateWidgetChannel(this, "正在关闭电台...");
					EasyDoubanFmWidget.updateWidgetInfo(this, null, null);

					curMusic = lastMusic = null;
					curPic = null;
					
					try {
						unregisterReceiver(controlListener);
					} catch (IllegalArgumentException e) {
						Debugger.error("download receiver not registered, skip unregistering");
					}
					
					try {
						unregisterReceiver(mediaButtonListener);
					} catch (IllegalArgumentException e) {
						Debugger.error("media button listener not registered, skip unregistering");
					}
					
					if (shakeDetector != null ) {
						shakeDetector.stop();
						if (shakeListener != null)
							shakeDetector.unregisterOnShakeListener(shakeListener);
					}
					
					Debugger.debug("DoubanFm Control Service unregistered");
					controlListener = null;
					
					if (picTask != null) {
						picTask.cancel(true);
						picTask = null;
					}
					
					if (playThread != null) {
						playThread.releasePlay();
						playThread.quit();
						try  {
							//playThread.join();
							Debugger.info("PlayMusic thread has quited");
							playThread = null;
						} catch (Exception e) {
							
						}
					}
					
					EasyDoubanFmWidget.clearWidgetChannel(this);
					EasyDoubanFmWidget.clearWidgetImage(this);
					EasyDoubanFmWidget.clearWidgetInfo(this);
					
					EasyDoubanFmWidget.updateWidgetOnOffButton(this, -1);		
					isFmOn = false;
				}
			}
		}
		if (!isDownloaderOn) {
			synchronized(this) {
				if (!isDownloaderOn) {
					Debugger.debug("no download in process. close service");
					closeService();
				}
			}
		}
	}
	
	private void closeService() {
		Debugger.info("closeService");
		
		stopSelf();
	}
	
	
	
	@Override
	public void resumeMusic() {
		Debugger.verbose("SERVICE resumeMusic");
		if (playThread == null)
			return;
		
		if (!playThread.isPlaying()) {
			playThread.resumePlay();
			Intent intent = new Intent(STATE_RESUMED);  
			//intent.putExtra("session", this.sessionId);  
			sendBroadcast(intent);
		}
	}
	
	@Override
	public void pauseMusic() {
		Debugger.verbose("SERVICE pauseMusic");
		if (playThread == null)
			return;
		
		if (playThread.isPlaying()) {
			playThread.pausePlay();
			Intent intent = new Intent(STATE_PAUSED);  
			//intent.putExtra("session", this.sessionId);  
			sendBroadcast(intent);  
		}
	}
	
	private void playPauseMusic() {
		//try {
		if (playThread == null)
			return;
		
		if (playThread.isPlaying()) {
			pauseMusic();
		}
		else {
			resumeMusic();
		}

	}
	
	
	private void fillPendingList(int channel) throws IOException {
		String[] historySids = null;
		if (musicHistory.size() > 0) {
			historySids = new String[musicHistory.size()];
			for (int i = 0; i < historySids.length; ++i)
				historySids[i] = musicHistory.get(i).sid;
		}
		MusicInfo[] musics = null;
		musics = DoubanFmApi.report(session, channel, 
					((curMusic != null)? curMusic.sid: ""), lastStopReason, historySids);

		if (musics == null || musics.length == 0) {
			Debugger.error("musics == 0");
			popNotify(getResources().getString(R.string.err_get_songs));
			return;
		}
		for (MusicInfo i: musics) {
			if (pendingMusicList.size() >= MAX_PENDING_COUNT) {
				break;
			} else {
				pendingMusicList.add(i);
				Debugger.debug("pending list size is " + pendingMusicList.size());
			}
		}
		
		
	}
	
	private MusicInfo getNextMusic(int channel) {
		if (pendingMusicList.size() == 0) {
			// pre-fetch
			try {
				fillPendingList(channel);
			} catch (IOException e) {
				popNotify(getResources().getString(R.string.text_network_error));
				return null;
			}
		} 
		Debugger.verbose("pending list size is " + pendingMusicList.size() + " return ");
		if (pendingMusicList.size() > 0)
			return pendingMusicList.remove(0);
		else return null;
	}
	
	@Override
	public void selectChannel(int id) {
		Debugger.info("SELECT CHANNEL to " + id);
		FmChannel chan = db.getChannelInfo(id);
		if (chan == null) {
			Debugger.error("#### channel user selected is invalid");
			id = 0;
			Preference.selectChannel(this, id);
		}
		else {
			Preference.selectChannel(this, id);
		}
		
		boolean login = Preference.getLogin(this);
		EasyDoubanFmWidget.updateWidgetChannel(this, chan.getDisplayName(login));
		
		pendingMusicList.clear();
		//stopMusic();
		lastStopReason = DoubanFmApi.TYPE_NEW;
		//stopMusic();
		nextMusic(id);
	}
	
	private Session session;
	
	public void rateMusic() {
		// send report and update list
		lastStopReason = DoubanFmApi.TYPE_RATE;
		pendingMusicList.clear();
		try {
			EasyDoubanFmWidget.updateWidgetRated(this, true);
			fillPendingList(Preference.getSelectedChannel(this));
			
		} catch (Exception e) {
			//EasyDoubanFmWidget.updateWidgetRated(this, false);
			//Debugger.error("error rating music: " + e.toString());
			//popNotify(getResources().getString(R.string.notify_rate_fail));
		}
		// change button state
		
	}
	
	public void unrateMusic() {
		// send report and update list
		lastStopReason = DoubanFmApi.TYPE_UNRATE;
		pendingMusicList.clear();
		try {
			EasyDoubanFmWidget.updateWidgetRated(this, false);
			fillPendingList(Preference.getSelectedChannel(this));
		} catch (Exception e) {
			
		}
		// change button state
		//EasyDoubanFmWidget.updateWidgetRated(this, false);
	}
	

	
	@Override
	public void nextMusic() {
		nextMusic(Preference.getSelectedChannel(this));
	}
	
	@Override
	public void nextMusic(int channel) { 
		playThread.stopPlay();
		
		EasyDoubanFmWidget.clearWidgetInfo(this);
		
		EasyDoubanFmWidget.updateWidgetProgress(this, 20);
		
		lastMusic = curMusic;
		curMusic = getNextMusic(channel);
		if (lastMusic != null) {
			musicHistory.add(0, lastMusic);
			Debugger.verbose("adding music to history");
		}
		while (musicHistory.size() > MAX_HISTORY_COUNT) {
			musicHistory.remove(musicHistory.size() - 1);
			Debugger.verbose("remove old history");
		}
		
		EasyDoubanFmWidget.updateWidgetProgress(this, 40);
		if (curMusic == null) {
			Intent intent = new Intent(STATE_FAILED);  
		    //intent.putExtra("session", sessionid);  
		    sendBroadcast(intent);  
		    Debugger.verbose("curMusic == null!!");
		    popNotify(getResources().getString(R.string.err_get_songs));
		    return;
		} 
		
		
		
		// report picture ready
		Intent intent = new Intent(STATE_STARTED);  
	    intent.putExtra("album", curMusic.album);
		intent.putExtra("albumtitle", curMusic.albumtitle);
		intent.putExtra("artist", curMusic.artist);
		intent.putExtra("company", curMusic.company);
		intent.putExtra("rating_avg", curMusic.rating_avg);
		intent.putExtra("title", curMusic.title);
		intent.putExtra("pictureUrl", curMusic.pictureUrl);
		intent.putExtra("musicUrl", curMusic.musicUrl);
		Debugger.info("Session started: " + curMusic.toString());
	    sendBroadcast(intent);
	    
    	Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.progress2);
	    EasyDoubanFmWidget.updateWidgetInfo(this, bmp, curMusic);
	    
	    EasyDoubanFmWidget.updateWidgetRated(this, !curMusic.like.equals("0"));
	    
	    playThread.startPlay(curMusic.musicUrl);
	    
	    // set notification;
		fgNotification = new Notification(R.drawable.icon,
				//getResources().getString(R.string.app_name),
				curMusic.artist + " -- " + curMusic.title,
                System.currentTimeMillis());
		
		fgNotification.flags |= Notification.FLAG_NO_CLEAR;
	    Intent it = new Intent(NULL_EVENT);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
		fgNotification.setLatestEventInfo(this, curMusic.artist,
                curMusic.title, pi);
		notificationManager.notify(serviceNotId, fgNotification);
		startForeground(serviceNotId, fgNotification);
		
		
		
	    
	    EasyDoubanFmWidget.updateWidgetProgress(this, 60);
		
	    // update appwidget view image
	    if (picTask != null) {
	    	picTask.cancel(true);
	    }
	    picTask = new GetPictureTask();
	    picTask.execute(curMusic.pictureUrl);
	    
	    // secretly pre-fetch play list
	    if (pendingMusicList.size() < 1) {
	    	try {
	    		fillPendingList(channel);
	    	} catch (IOException e) {
	    		Debugger.error("network error filling pending list: " + e.toString());
	    	}
	    }
	}
	
	
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Debugger.verbose("Service onStartCommand(intent, " + flags + ", " + startId + ")");
		startMusic(++this.sessionId, 0);
		return START_STICKY;
	}*/
	
	@Override
	public void onLowMemory() {
		Debugger.warn("SERVICE ONLOWMEMORY");
		closeFM();
		closeDownloader();
	}
	
	@Override
	public void onDestroy() {
		Debugger.warn("SERVICE ONDESTROY");
		super.onDestroy();
	
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	
    private class GetPictureTask extends AsyncTask<String, Integer, Bitmap> {
    	public GetPictureTask(){
    	}
    	
    	@Override
    	protected void onCancelled () {
    		Debugger.info("GetPictureTask is cancelled");
    		picTask = null;
    	}
    	@Override
    	protected Bitmap doInBackground(String... params) {
        	
    		HttpGet httpGet = new HttpGet(params[0]);
    		httpGet.setHeader("User-Agent", 
    				String.valueOf(Preference.getClientVersion()));
    		httpGet.setHeader("Connection", "Keep-Alive");
    		HttpResponse httpResponse = null;
    		try {
    			httpResponse = new DefaultHttpClient().execute(httpGet);
    			publishProgress(80);
    		} catch (Exception e) {
    			Debugger.error("Error getting response of downloading music: " + e.toString());
    			return null;
    		}
    		
    		int statuscode = httpResponse.getStatusLine().getStatusCode();
			if (statuscode != 200) {
				Debugger.error("Error getting response of downloading music: status " + statuscode);
    			return null;
			}
				
			byte b[] = null;
			try {
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				b = new byte[length];
				int l = 0;
				while (l < length) {
					if (isCancelled())
						return null;
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, length);
				return bmp;
			} catch (Exception e) {
				Debugger.error("Error getting picture: " + e.toString());
				return null;
			}
        	
    	}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, progress[0].intValue());
        }
    	@Override
        protected void onPostExecute(Bitmap bmp) {
    		curPic = bmp;
    		EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, bmp, null);
    		EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, 100);
    		picTask = null;
    		
        }
    }

    
    @Override
    public void banMusic() {
    	if (curMusic == null) {
    		return;
    	}
    	
		lastStopReason = DoubanFmApi.TYPE_BYE;
		pendingMusicList.clear();
		try {
			fillPendingList(Preference.getSelectedChannel(this));
		} catch (Exception e) {
			Debugger.error("error in banMusic: " + e.toString());
		}
		popNotify(getResources().getString(R.string.notify_music_banned));
		nextMusic();
    }
    
    @Override
    public void downloadMusic() {
    	if (curMusic == null) {
    		return;
    	}
    	
    	String url = curMusic.musicUrl;
    	String filename = getUnixFilename(curMusic.artist, curMusic.title,
    			url);
    	
    	if (filename == null) {
    		return;
    	}
    	
    	Debugger.verbose("download filename is \"" + filename + "\"");

    	int sid = 0;
    	try {
    		sid = Integer.parseInt(curMusic.sid);
    	} catch (Exception e) {
    		sid = downloadDefaultNotId;
    	}
    	downloader.download(sid, url, filename);
    }
    
    
    
    private String getUnixFilename(String artist, String title, String url) {
    	if (artist == null || title == null || url == null) {
    		return null;
    	}
    	String fileextension = "";
    	String tmp = url;
    	int indDot = -1;
    	while (true) {
    		int i = tmp.indexOf('.');
    		if (i == -1) {
    			break;
    		} else {
    			tmp = tmp.substring(i + 1);
    			indDot = i;
    		}
    	} 
    	if (indDot == -1)
    		fileextension = ".mp3";
    	else fileextension = "." + tmp;//.substring(indDot);
    	
    	String name = artist + "_-_" + title;
    	Debugger.debug("before transform: " + name);
    	name = name.replace(" ", "_");
    	name = name.replace("/", "-");
    	name = name.replace("\\", "-");
    	name = name.replace(":", "-");
    	name = name.replace("?", "-");
    	name = name.replace("*", "-");
    	name = name.replace("\"", "-");
    	name = name.replace("<", "-");
    	name = name.replace(">", "-");
    	name = name.replace("|", "-");
    	
    	int namelen = (name.length() + fileextension.length() > 255)? 
    			(255 - fileextension.length()): name.length();
    	name = name.substring(0, namelen);
    	Debugger.debug("after transform: " + name + fileextension);
    	return name + fileextension;
    }
    
    private void popNotify(String msg)
    {
        Toast.makeText(DoubanFmService.this, msg,
                Toast.LENGTH_LONG).show();
    }
    
    public class DownloadReceiver extends BroadcastReceiver {
    	@Override  
        public void onReceive(Context arg0, Intent arg1) { 
    		String action = arg1.getAction(); 
            Bundle b = arg1.getExtras();

            Debugger.debug("received broadcast: " + action);
            if (action.equals(ACTION_CLEAR_NOTIFICATION)) {
				int sessionId = arg1.getIntExtra(EXTRA_DOWNLOAD_SESSION, -1);
				if (sessionId != -1) {
					notificationManager.cancel(sessionId);
					notifications.remove(sessionId);
				}
			}
            if (action.equals(ACTION_CANCEL_DOWNLOAD)) {
				int sessionId = arg1.getIntExtra(EXTRA_DOWNLOAD_SESSION, -1);
				Debugger.info("download session " + sessionId + " cancelling");
				downloader.cancel(sessionId);
			}
			if (action.equals(ACTION_DOWNLOAD)) {
				String url = arg1.getStringExtra(EXTRA_DOWNLOAD_URL);
				String filename = arg1.getStringExtra(EXTRA_DOWNLOAD_FILENAME);
				int sessionId = arg1.getIntExtra(EXTRA_DOWNLOAD_SESSION, 0);
				downloader.download(sessionId, url, filename);
			}
    	}
    }
    

    
    public class DoubanFmControlReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context arg0, Intent arg1) {  
            String action = arg1.getAction(); 
            Bundle b = arg1.getExtras();

            Debugger.debug("received broadcast: " + action);
            
            if (action.equals(DoubanFmService.CONTROL_NEXT)) {
            	Debugger.info("Douban service received START command");
            	nextMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_TRASH)) {
            	Debugger.info("Douban service received TRASH command");
            	banMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_RATE)) {
            	Debugger.info("Douban service received RATE command");
            	rateMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_UNRATE)) {
            	Debugger.info("Douban service received UNRATE command");
            	unrateMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_PLAYPAUSE)) {
            	Debugger.info("Douban service received PLAY/PAUSE command");
            	playPauseMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_PAUSE)) {
            	Debugger.info("Douban service received PAUSE command");
            	pauseMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_RESUME)) {
            	Debugger.info("Douban service received RESUME command");
            	resumeMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_CLOSE)) {
            	Debugger.info("Douban service received CLOSE command");
            	closeFM();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_DOWNLOAD)) {
            	Debugger.info("Douban service received DOWNLOAD command");
            	downloadMusic();
            	return;
            }

            if (action.equals(DoubanFmService.CONTROL_TRASH)) {
            	Debugger.info("Douban service received FAVORITE command");
            	banMusic();
            	nextMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_SELECT_CHANNEL)) {
            	int chann = arg1.getIntExtra("channel", 0);
            	Debugger.info("Douban service received SELECT_CHANNEL: " + chann);
            	selectChannel(chann);
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_UPDATE_WIDGET)) {
            	Debugger.info("Douban service received UPDATE_WIDGET");
            	updateWidgets();
            	return;
            }
        }
    }  
    
    private void updateWidgets() {
    	EasyDoubanFmWidget.setWidgetButtonListeners(DoubanFmService.this, null);
    	int selChan = Preference.getSelectedChannel(DoubanFmService.this);

    	EasyDoubanFmWidget.updateWidgetOnOffButton(DoubanFmService.this, 
    			(isFmOn? 1: -1));
    	
    	if (isFmOn) {
	    	FmChannel[] chans = db.getChannels();
	    	if (chans == null || chans.length == 0) {
	    		Debugger.error("channel number in database is 0!");
	    		return;
	    	}
	    	if (selChan >= chans.length) {
	    		Debugger.error("selected channel out of bound!");
	    		return;
	    	}
	    	String chanName = chans[selChan].name;
	    	EasyDoubanFmWidget.updateWidgetChannel(DoubanFmService.this, 
	    			chanName);
    	} else {
    		EasyDoubanFmWidget.updateWidgetChannel(DoubanFmService.this, 
	    			getResources().getString(R.string.text_channel_unselected));
    	}
    	
    	if (isFmOn && curMusic != null) {
	    	EasyDoubanFmWidget.updateWidgetRated(DoubanFmService.this, 
	    			(curMusic.like.equals("0")? false: true));
	    	EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, 
	    			curPic, curMusic);
    	} else {
	    	EasyDoubanFmWidget.updateWidgetRated(DoubanFmService.this, false);
	    	MusicInfo mi = new MusicInfo();
	    	mi.artist = mi.title = "";
	    	EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, null, mi);
    	}
    	
    }
    
    @Override 
	public void onConfigurationChanged(Configuration newConfig) {
		Debugger.info( "ONCONFIGURATIONCHANGED: " + newConfig.toString());
		super.onConfigurationChanged(newConfig); 
    	updateWidgets();
	}
    
    private class DoubanFmHandler extends Handler {
    	@Override
    	public void handleMessage(Message msg) {
    		PlayMusicThread.PlayState state = (PlayMusicThread.PlayState)msg.obj;
    		switch(state) {
    		case STARTED: {
    			break;
    		}
    		case STOPPED: {
    			break;
    		}
    		case ERROR: {
    			Intent intent = new Intent(STATE_FAILED);  
    		    Debugger.info("Session failed");//: " + curMusic.toString());
    		    sendBroadcast(intent);
    		    lastStopReason = DoubanFmApi.TYPE_SKIP;
    		    nextMusic();
    		}
    		case PAUSED: {
    			Intent intent = new Intent(STATE_PAUSED);  
    		    Debugger.info("Session failed");//: " + curMusic.toString());
    		    sendBroadcast(intent);
    		}
    		case IOERROR: {
    			Intent intent = new Intent(STATE_FAILED);  
    		    Debugger.info("Session failed IOERROR");//: " + curMusic.toString());
    		    sendBroadcast(intent);
    		}
    		case COMPLETED: {
    			Intent intent = new Intent(STATE_FINISHED);  
    		    Debugger.info("Session failed");//: " + curMusic.toString());
    		    sendBroadcast(intent);
    		    lastStopReason = DoubanFmApi.TYPE_NEW;
    		    nextMusic();
    		}
    		default:
    			break;
    		}
    	}
    }
    
	private static final int DOWNLOAD_ERROR_OK = 0;
	private static final int DOWNLOAD_ERROR_IOERROR = -1;
	private static final int DOWNLOAD_ERROR_CANCELLED = -2;
	
	
	private Map<Integer, Notification> notifications = new HashMap<Integer, Notification>(); 
	
	private void notifyDownloading(int sessionId, String url, String filename) {
		
		
		Intent i = new Intent(ACTION_CANCEL_DOWNLOAD);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		//i.putExtra(EXTRA_DOWNLOAD_URL, url);
		//i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		//i.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, PendingIntent.FLAG_CANCEL_CURRENT );

		Notification notification = new Notification(android.R.drawable.stat_sys_download, 
				getResources().getString(R.string.text_downloading),
                System.currentTimeMillis());
		
		notification.contentView = new android.widget.RemoteViews(getPackageName(),
				R.layout.download_notification_1); 
		notification.contentView.setTextViewText(R.id.textDownloadFilename, filename);
		notification.contentView.setTextViewText(R.id.textDownloadSize, 
				getResources().getString(R.string.text_download_cancel));
		notification.contentView.setProgressBar(R.id.progressDownloadNotification, 100,0, false);

		notifications.put(sessionId, notification);
		
        //notification.setLatestEventInfo(this, getResources().getString(R.string.text_downloading), 
        //		filename, pi);
		//Intent notificationIntent = new Intent(DoubanFmService.ACTION_NULL); 
    	//PendingIntent contentIntent = PendingIntent.getBroadcast(this,0,notificationIntent,0); 
    	notification.contentIntent = pi;      

		notificationManager.notify(sessionId, notification);
	}
	
	private void notifyDownloadOk(int sessionId, String url, String filename) {
				
		Intent i = new Intent(ACTION_CLEAR_NOTIFICATION);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		//i.putExtra(EXTRA_DOWNLOAD_URL, url);
		//i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		//i.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification = new Notification(R.drawable.stat_sys_install_complete, 
				getResources().getString(R.string.text_download_ok),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getResources().getString(R.string.text_download_ok_long), 
        		filename, pi);
        notificationManager.notify(sessionId, notification);
	}
	
	private void notifyDownloadFail(int sessionId, String url, String filename) {
		
		Intent i = new Intent(this, DoubanFmService.class);
		i.putExtra(EXTRA_BINDSERVICE_TYPE, BINDTYPE_DOWNLOAD);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		//i.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
		PendingIntent pi = PendingIntent.getService(this, 
				0, i, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification = new Notification(android.R.drawable.stat_notify_error, 
				getResources().getString(R.string.text_download_fail),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getResources().getString(R.string.text_download_fail_long), 
        		filename, pi);
        notificationManager.notify(sessionId, notification);
		
	}
	

	
	private class Downloader { 
		private static final int DOWNLOAD_BUFFER = 102400;
		private class DownloadTask extends AsyncTask<String, Integer, Integer> {
			private int sessionId;
			private int lastProgress;
			private int progress;
			private String url;
			private String filename;
			public DownloadTask(int sessionId) {
				this.sessionId = sessionId;
				progress = -1;
				lastProgress = 0;
			}
			public int getProgress() {
				return progress;
			}
			@Override
	    	protected void onProgressUpdate(Integer... progress) {
				this.progress = progress[0];
				Debugger.verbose("Download progress: " + this.progress);
				
				if (isCancelled()) {
					return;
				}
				
				Notification n = notifications.get(this.sessionId);
				if (n == null) {
					return;
				}
				
				//if (this.progress == 0 || this.progress == 100
				//		|| this.progress - this.lastProgress > 10) {
				n.contentView.setProgressBar(R.id.progressDownloadNotification, 
						100, this.progress, false);
				String text = String.valueOf(this.progress) + "%";
				if (totalBytes != -1 && downloadedBytes != -1)
					text += " (" + downloadedBytes/1024 + "K/" + totalBytes/1024 + "K)";
				n.contentView.setTextViewText(R.id.textDownloadSize, 
						getResources().getString(R.string.text_download_cancel)+ text);
				
				Intent i = new Intent(ACTION_CANCEL_DOWNLOAD);
				i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
				//i.putExtra(EXTRA_DOWNLOAD_URL, url);
				//i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
				//i.setData((android.net.Uri.parse("foobar://"+android.os.SystemClock.elapsedRealtime())));
				PendingIntent pi = PendingIntent.getBroadcast(DoubanFmService.this, 
						0, i, PendingIntent.FLAG_CANCEL_CURRENT);
				n.contentIntent = pi;
				
				notificationManager.notify(this.sessionId, n);
				
				//	this.lastProgress = this.progress;
				//}
				
			}
			
			@Override
	        protected void onPostExecute(Integer result) {
				switch (result) {
				case DOWNLOAD_ERROR_OK:
					Debugger.info("Download finish");
					notifyDownloadOk(this.sessionId, this.url, this.filename);
					
					File musicfile = null;
					musicfile = getDownloadFile(filename);
					
					if (musicfile != null) {
						SingleMediaScanner scanner = new SingleMediaScanner(DoubanFmService.this, musicfile);
					}
					
					break;
				case DOWNLOAD_ERROR_IOERROR:
				case DOWNLOAD_ERROR_CANCELLED:
					File f = getDownloadFile(filename);
					if (f.exists()) {
						f.delete();
					}
					Debugger.info("Download failed");
					notifyDownloadFail(this.sessionId, this.url, this.filename);
					break;
				default:
					break;
				}
				tasks.remove(this.sessionId);
				Debugger.info("remaining task " + tasks.size());
				// exit service if no pending tasks
				if (tasks.size() == 0) {
					closeDownloader();
				}
			}
			
			@Override
	    	protected void onCancelled() {
				File f = getDownloadFile(filename);
				if (f.exists()) {
					f.delete();
				}
				
				tasks.remove(this.sessionId);
				notifyDownloadFail(this.sessionId, this.url, this.filename);
				// exit service if no pending tasks
				if (tasks.size() == 0) {
					closeDownloader();
				}
			}
			private long totalBytes = -1;
			private long downloadedBytes = -1;
			@Override
	    	protected Integer doInBackground(String... params) {
				// param 0: url of music
				// param 1: download filename
				
				if (params.length < 2) {
					Debugger.error("Download task requires more arguments than " + params.length);
					return DOWNLOAD_ERROR_CANCELLED;
				}
				this.url = params[0];
				this.filename = params[1];
				Debugger.info("url = " + this.url + ", filename = " + this.filename); 
				
				// Step 1. get bytes
				HttpGet httpGet = new HttpGet(url);
	    		httpGet.setHeader("User-Agent", 
	    				String.valueOf(Preference.getClientVersion()));

	    		HttpResponse httpResponse = null;
	    		
	    		HttpParams hp = new BasicHttpParams();
	    		int timeoutConnection = 10000;
	    		HttpConnectionParams.setConnectionTimeout(hp, timeoutConnection);
	    		int timeoutSocket = 30000;
	    		HttpConnectionParams.setSoTimeout(hp, timeoutSocket);
	    		
	    		try {
	    			httpResponse = new DefaultHttpClient(hp).execute(httpGet);
	    			//publishProgress(5);
	    		} catch (Exception e) {
	    			Debugger.error("Error getting response of downloading music: " + e.toString());
	    			return DOWNLOAD_ERROR_IOERROR;
	    		}
	    		
	    		Debugger.info("received response");
	    		int statuscode = httpResponse.getStatusLine().getStatusCode();
				if (statuscode != 200) {
					Debugger.error("Error getting response of downloading music: status " + statuscode);
	    			return statuscode;
				}
				
				
				
				if (isCancelled()) {
					return DOWNLOAD_ERROR_CANCELLED;
				}

				// step 2. create file
				OutputStream os = null;
				try {
					File musicfile = null;
					musicfile = getDownloadFile(filename);
					
					if (musicfile == null) {
						Debugger.error("can not get download file");
						return DOWNLOAD_ERROR_IOERROR;
					}
					Debugger.info("got download file, start writing");
					os = new FileOutputStream(musicfile);
				} catch (Exception e) {
					Debugger.error("Error writing file to external storage: " + e.toString());
					return DOWNLOAD_ERROR_IOERROR;
				}
				//publishProgress(10);
				
				// step 3. write into file after each read
				byte b[] = new byte[DOWNLOAD_BUFFER];
				try {
					InputStream is = httpResponse.getEntity().getContent();
					//long len = httpResponse.getEntity().getContentLength();
					totalBytes = httpResponse.getEntity().getContentLength();
					downloadedBytes = 0;
					while (downloadedBytes < totalBytes) {
						if (isCancelled())
							return DOWNLOAD_ERROR_CANCELLED;
						int tmpl = is.read(b, 0, DOWNLOAD_BUFFER);
						if (tmpl == -1)
							break;
						
						Debugger.debug("writing file " + tmpl + ", " + downloadedBytes + "/" + totalBytes);
						os.write(b, 0, tmpl);
						downloadedBytes += tmpl;
						
						double prog = ((double)downloadedBytes / totalBytes * 100);
						publishProgress((int)prog);
						
					}
					os.flush();
					os.close();
					publishProgress(100);
					return DOWNLOAD_ERROR_OK;
				} catch (Exception e) {
					Debugger.error("Error getting content of music: " + e.toString());
					return DOWNLOAD_ERROR_IOERROR;
				}
				
			}
		}
		private Context context;
		private Map<Integer, DownloadTask> tasks;
		private boolean writable;
		//private int sessionId;ven
		
		
		public boolean isRunning() {
			Debugger.info("downloader task list size: " + tasks.size());
			return tasks.size() > 0;
		}
		
		public Downloader(Context context) {
			this.context = context;
			Debugger.info("DOWNLOADER CREATE");
			tasks = new HashMap<Integer, DownloadTask>();
			//sessionId = 1;
		}

		
		private File getDownloadFile(String filename) {
			
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				return null;
			}
			
			File dir = new File(android.os.Environment.getExternalStorageDirectory(), 
					Preference.getDownloadDirectory(context));
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(Environment.getExternalStorageDirectory(), 
					Preference.getDownloadDirectory(context) + "/" + filename);
			return file;
		}
		

		private boolean getExternalStorageState() {
			return writable;
		}

		public int download(int sid, String url, String filename) {
			Debugger.debug("DownloadService.download(" + url + ", " + filename + ")");
			DownloadTask task = null;
			//int ret = -1;
			//synchronized(this) {
			task = new DownloadTask(sid);
			tasks.put(sid, task);
			//ret = sessionId;
			//++sessionId;
			//}
			Debugger.debug("execute task " + sid);
			notifyDownloading(sid, url, filename);
			task.execute(url, filename);
			return sid;
		}
		

		public int getProgress(int sessionId) {
			DownloadTask task = tasks.get(sessionId);
			if (task != null) {
				return task.getProgress();
			}
			else {
				return -1;
			}
		}
		
		//@Override
		public void cancel(int sessionId) {
			DownloadTask task = tasks.get(sessionId);
			if (task != null) {
				task.cancel(true);
				tasks.remove(sessionId);
			}
		}
		
		public void cancelAll() {
			Set<Integer> keyset = tasks.keySet();
			Iterator<Integer> it = keyset.iterator();
			while (it.hasNext()) {
				Integer sid = it.next();
				DownloadTask t = tasks.get(sid);
				t.cancel(true);
			}
			tasks.clear();
		}
	}
    
}
