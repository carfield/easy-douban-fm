package com.saturdaycoder.easydoubanfm;
import android.app.Notification;
import android.os.Handler;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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


public class DoubanFmService extends Service implements IDoubanFmService {
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
	
	public static final String CONTROL_NEXT = "com.saturdaycoder.easydoubanfm.control.next";
	public static final String CONTROL_PLAYPAUSE = "com.saturdaycoder.easydoubanfm.control.playpause";
	public static final String CONTROL_CLOSE = "com.saturdaycoder.easydoubanfm.control.close";
	public static final String CONTROL_DOWNLOAD = "com.saturdaycoder.easydoubanfm.control.download";
	public static final String CONTROL_DOWNLOAD_NOTIFICATION_CLICKED = "com.saturdaycoder.easydoubanfm.control.download_clicked";
	public static final String CONTROL_FAVORITE = "com.saturdaycoder.easydoubanfm.control.favorite";
	public static final String CONTROL_TRASH = "com.saturdaycoder.easydoubanfm.control.trash";
	public static final String CONTROL_SELECT_CHANNEL = "com.saturdaycoder.easydoubanfm.control.select_channel";
	
	public static final String NULL_EVENT = "com.saturdaycoder.easydoubanfm.null";

	private static final int MAX_HISTORY_COUNT = 20;
	private static final int MAX_PENDING_COUNT = 20;
	private static final int MAX_PREBUFFER_PLAYER_COUNT = 2;
	private ArrayList<MusicInfo> musicHistory;
	private ArrayList<MusicInfo> pendingMusicList;
	private MusicInfo curMusic;
	private MusicInfo lastMusic;
	private PlayMusicThread playThread = null;	
	private final IBinder mBinder = new LocalBinder();
	private GetPictureTask picTask = null;
	private IDownloadService mDownload;
	private ServiceConnection mDownloadServiceConn;
	private boolean mDownloadBind;
	private Database db;
	private char lastStopReason;
	private Handler mainHandler;
	private ArrayList<DownloadInfo> pendingDownloads;
	
	private class DownloadInfo {
		String url;
		String filename;
	}
	
	public class LocalBinder extends Binder {
		
		DoubanFmService getService() {
			return DoubanFmService.this;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Debugger.verbose("Service onStart");
		super.onStart(intent, startId);
		
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, true);
		
		// check login
		if (Preference.getLogin(this)) {
			String email = Preference.getAccountEmail(this);
			String passwd = Preference.getAccountPasswd(this);
			try {
				session = DoubanFmApi.login(email, passwd, 583);
			} catch (Exception e) {
				Debugger.error("IO ERROR loging in: " + e.toString());
			}
		}
		else {
			session = null;
		}
		
        // get stored channel table
		db = new Database(this);		
		// get channel table if it's not existing
		FmChannel[] chans = db.getChannels();
		if (chans == null) {
			EasyDoubanFmWidget.updateWidgetChannel(this, "正在下载频道列表...");
			chans = DoubanFmApi.getChannelTable();
			for (FmChannel c: chans) {
				db.saveChannel(c);
			}
			Preference.selectChannel(this, 0);
			
		}
		
		// get the currently selected channel
		int ci = Preference.getSelectedChannel(this);
		FmChannel c = db.getChannelInfo(ci);
		EasyDoubanFmWidget.updateWidgetChannel(this, c.name);
		
		nextMusic();
	}
	

	

	
	@Override
	public void onCreate() {
		super.onCreate();
		Debugger.debug("service onCreate");
		
		pendingDownloads = new ArrayList<DownloadInfo>();
		
		curMusic = lastMusic = null;
		
		mDownload = null;
		mDownloadServiceConn = null;
		mDownloadBind = false;
		
		lastStopReason = DoubanFmApi.TYPE_NEW;
		
        if (receiver == null)  {
            receiver = new DoubanFmControlReceiver();  
        }
        
        mainHandler = new DoubanFmHandler();
        playThread = new PlayMusicThread(mainHandler);
        playThread.start();
        
        musicHistory = new ArrayList<MusicInfo>();
        pendingMusicList = new ArrayList<MusicInfo>();
        
		IntentFilter filter = new IntentFilter();
		filter.addAction(DoubanFmService.CONTROL_NEXT);
		filter.addAction(DoubanFmService.CONTROL_PLAYPAUSE);
		filter.addAction(DoubanFmService.CONTROL_CLOSE);
		filter.addAction(DoubanFmService.CONTROL_DOWNLOAD);
		filter.addAction(DoubanFmService.CONTROL_FAVORITE);
		filter.addAction(DoubanFmService.CONTROL_TRASH);
		filter.addAction(DoubanFmService.CONTROL_SELECT_CHANNEL);
		//filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON );
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter); 
		Debugger.verbose("DoubanFm Control Service registered");
		
		shakeDetector = new ShakeDetector(this);
		
		shakeListener = new ShakeDetector.OnShakeListener() {
			
			@Override
			public void onShake() {
				// TODO Auto-generated method stub
				Debugger.info("Detected SHAKING!!!");
				//int sessionid = getSessionId() + 1;
            	nextMusic(Preference.getSelectedChannel(DoubanFmService.this));
			}
		};
		
		shakeDetector.registerOnShakeListener(shakeListener);
		shakeDetector.start();
	}
	private ShakeDetector.OnShakeListener shakeListener;
	private ShakeDetector shakeDetector;// = new ShakeDetector(this);
	private DoubanFmControlReceiver receiver;
	
	/*@Override
	public void resumeService() {
		
	}
	
	@Override
	public void pauseService() {
		
	}*/
	
	@Override
	public void closeService() {
		Debugger.info("closeService");
		
		//stopAllMusic();
		stopSelf();
	}
	
	/*@Override
	public void stopMusic() {
		Debugger.info("stop session ");// + sessionid + " current is " + this.sessionId);
		//if (sessionid == this.sessionId) {
			//try {
				//if (mPlayer.isPlaying())
				//	mPlayer.stop();
		if (playThread != null) {
			playThread.stopPlay();
		}
		Intent intent = new Intent(STATE_PAUSED);  
	    //intent.putExtra("session", sessionid);  
	    sendBroadcast(intent);
	    //Debugger.info("session " + sessionid + " STOPPED");
			//} catch (Exception e) {
			//	Debugger.error("Error stopping music [" + sessionid + "]: " + e.toString());
			//}
			//EasyDoubanFmWidget.updateWidgetBlurInfo(this, true);
		EasyDoubanFmWidget.clearWidgetInfo(this);
		//}
	}*/
	
	
	/*public void stopAllMusic() {
		Debugger.info("stop all music");
		if (musicThread != null) {
			try  {
				musicThread.join();
			} catch (Exception e) {
				
			}
		}
		try {
			if (mPlayer.isPlaying())
				mPlayer.stop();
			Intent intent = new Intent(STATE_FINISHED);  
		    sendBroadcast(intent);
		    
		} catch (Exception e) {
			Debugger.error("Error stopping all music: " + e.toString());
		}
		EasyDoubanFmWidget.updateWidgetBlurInfo(this, true);
		EasyDoubanFmWidget.clearWidgetInfo(this);
	}*/
	
	private void playPauseMusic() {
		//try {
		if (playThread == null)
			return;
		
		if (playThread.isPlaying()) {
			playThread.pausePlay();
			Intent intent = new Intent(STATE_PAUSED);  
			//intent.putExtra("session", this.sessionId);  
			sendBroadcast(intent);  
		}
		else {
			playThread.resumePlay();
			Intent intent = new Intent(STATE_RESUMED);  
			//intent.putExtra("session", this.sessionId);  
			sendBroadcast(intent);
		}
		//} catch (Exception e) {
		//	Debugger.error("Error play/pause music: " + e.toString());

		//}
	}
	
	
	private void fillPendingList(int channel) {
		String[] historySids = null;
		if (musicHistory.size() > 0) {
			historySids = new String[musicHistory.size()];
			for (int i = 0; i < historySids.length; ++i)
				historySids[i] = musicHistory.get(i).sid;
		}
		try {
			MusicInfo[] musics = DoubanFmApi.report(session, channel, 
					((curMusic != null)? curMusic.sid: ""), lastStopReason, historySids);
			if (musics == null || musics.length == 0) {
				Debugger.error("musics == 0");
			}
			for (MusicInfo i: musics) {
				if (pendingMusicList.size() >= MAX_PENDING_COUNT) {
					break;
				} else {
					pendingMusicList.add(i);
					Debugger.debug("pending list size is " + pendingMusicList.size());
				}
			}
			
		} catch (Exception e) {
			Debugger.error("Error prefetching music list: " + e.toString());
			//return -1;
		}
	}
	
	private MusicInfo getNextMusic(int channel) {
		if (pendingMusicList.size() == 0) {
			// pre-fetch
			fillPendingList(channel);
		} 
		Debugger.verbose("pending list size is " + pendingMusicList.size() + " return ");
		return pendingMusicList.remove(0);	
	}
	
	@Override
	public void selectChannel(int id) {
		Debugger.info("SELECT CHANNEL to " + id);
		FmChannel chan = db.getChannelInfo(id);
		if (chan == null) {
			Debugger.error("#### channel user selected is invalid");
			id = 0;
			Preference.selectChannel(this, id);
			EasyDoubanFmWidget.updateWidgetChannel(this, "公共频道");
		}
		else {
			Preference.selectChannel(this, id);
			EasyDoubanFmWidget.updateWidgetChannel(this, chan.name);
		}
		pendingMusicList.clear();
		//stopMusic();
		lastStopReason = DoubanFmApi.TYPE_NEW;
		//stopMusic();
		nextMusic(id);
	}
	
	private Session session;
	
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
	    
	    playThread.startPlay(curMusic.musicUrl);
	    
	    EasyDoubanFmWidget.updateWidgetProgress(this, 60);
		
	    // update appwidget view image
	    if (picTask != null)
	    	picTask.cancel(true);
	    picTask = new GetPictureTask();
	    picTask.execute(curMusic.pictureUrl);
	    
	    // secretly pre-fetch play list
	    if (pendingMusicList.size() < 1) {
	    	fillPendingList(channel);
	    }
	}
	
	
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Debugger.verbose("Service onStartCommand(intent, " + flags + ", " + startId + ")");
		startMusic(++this.sessionId, 0);
		return START_STICKY;
	}*/
	
	@Override
	public void onDestroy() {
		Debugger.info("DoubanFmService onDestroy");
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, false);
		EasyDoubanFmWidget.updateWidgetInfo(this, null, null);

		unregisterReceiver(receiver); 
		
		if (shakeDetector != null ) {
			shakeDetector.stop();
			if (shakeListener != null)
				shakeDetector.unregisterOnShakeListener(shakeListener);
		}
		
		Debugger.verbose("DoubanFm Control Service unregistered");
		receiver = null;
		
		if (picTask != null)
			picTask.cancel(true);
		
		if (playThread != null) {
			//playThread.stopPlay();
			playThread.releasePlay();
			try  {
				playThread.join();
				Debugger.info("PlayMusic thread has quited");
				playThread = null;
			} catch (Exception e) {
				
			}
		}
		
		EasyDoubanFmWidget.clearWidgetChannel(this);
		EasyDoubanFmWidget.clearWidgetImage(this);
		EasyDoubanFmWidget.clearWidgetInfo(this);
		
		
		super.onDestroy();
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	/*private void doGetPicture(String picurl) {
    	GetPictureTask task = new GetPictureTask();
        task.execute(picurl);
    }*/
	
    private class GetPictureTask extends AsyncTask<String, Integer, Bitmap> {
    	public GetPictureTask(){//MusicInfo info) {
    		//this.session = session;
    	}
    	//private int session;
    	@Override
    	protected void onCancelled () {
    		Debugger.info("GetPictureTask is cancelled");
    		picTask = null;
    	}
    	@Override
    	protected Bitmap doInBackground(String... params) {
    		/*try {
    	    	URL url = new URL((String)params[0]);
    	    	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    	    	conn.connect();
    	    	InputStream is = conn.getInputStream();
    	    	
    	    	Debugger.info("Picture size is " + is.available() + " bytes");
    	    	
    	    	Bitmap bmp = BitmapFactory.decodeStream(is);
    	    	is.close();
    	    	return bmp;
        	} catch (Exception e) {
        		Debugger.error("Error get music picture: " + e.toString());
        		return null;
        	}*/
        	
    		HttpGet httpGet = new HttpGet(params[0]);
    		httpGet.setHeader("User-Agent", 
    				String.valueOf(Preference.getClientVersion(DoubanFmService.this)));
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
					//double prog = 60 + ((double)l / length * 40);
					//if (l >= length / 3)
						//publishProgress(80);
				}
				//publishProgress(90);
				
				Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, length);
				//publishProgress(100);
				return bmp;
			} catch (Exception e) {
				Debugger.error("Error getting picture: " + e.toString());
				return null;
			}
        	
    	}
    	//private int lastProg = 0;
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, progress[0].intValue());
        }
    	@Override
        protected void onPostExecute(Bitmap bmp) {
    		EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, bmp, null);
    		EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, 100);
    		picTask = null;
    		
        }
    }
    
    @Override
    public void likeMusic(boolean like) {
    	if (curMusic == null)
    		return;
    	
    	lastStopReason = (like)? DoubanFmApi.TYPE_RATE: DoubanFmApi.TYPE_UNRATE;
    	String[] historySids = null;
		if (musicHistory.size() > 0) {
			historySids = new String[musicHistory.size()];
			for (int i = 0; i < historySids.length; ++i)
				historySids[i] = musicHistory.get(i).sid;
		}
    	try {
			MusicInfo[] musics = DoubanFmApi.report(session, 
					Preference.getSelectedChannel(DoubanFmService.this), 
					curMusic.sid,
					lastStopReason, historySids);
			pendingMusicList.clear();
			for (MusicInfo i: musics) {
				if (pendingMusicList.size() >= MAX_PENDING_COUNT) {
					break;
				} else {
					pendingMusicList.add(i);
				}
			}
		} catch (Exception e) {
			Debugger.error("Error getting music list after rating music: " + e.toString());
		}
    }
    
    @Override
    public void banMusic() {
    	if (curMusic == null)
    		return;
    	
    	lastStopReason = DoubanFmApi.TYPE_BYE;
    	String[] historySids = null;
		if (musicHistory.size() > 0) {
			historySids = new String[musicHistory.size()];
			for (int i = 0; i < historySids.length; ++i)
				historySids[i] = musicHistory.get(i).sid;
		}
    	try {
			MusicInfo[] musics = DoubanFmApi.report(session, 
					Preference.getSelectedChannel(DoubanFmService.this), 
					curMusic.sid,
					lastStopReason, historySids);
			pendingMusicList.clear();
			for (MusicInfo i: musics) {
				if (pendingMusicList.size() >= MAX_PENDING_COUNT) {
					break;
				} else {
					pendingMusicList.add(i);
				}
			}
		} catch (Exception e) {
			Debugger.error("Error getting music list after BAN music: " + e.toString());
		}
    }
    
    private void downloadMusic() {
    	String url = curMusic.musicUrl;
    	String filename = getUnixFilename(curMusic.artist, curMusic.title,
    			url);
    	Debugger.verbose("download filename is \"" + filename + "\"");
    	if (!mDownloadBind) {
    		// add this download to pending list
	        DownloadInfo d = new DownloadInfo();
	        d.url = url;
	        d.filename = filename;
	        pendingDownloads.add(d);
    		
    		//Intent i = new Intent(this, DownloadService.class);
    		//this.startService(i);
	    	mDownloadServiceConn = new ServiceConnection(){
	        	public void onServiceConnected(ComponentName className, IBinder service) {
	        		DownloadService.LocalBinder b = (DownloadService.LocalBinder)service;
	        		mDownload = (IDownloadService)b.getService();
	        		Debugger.info("DownloadService connected");
	        		mDownloadBind = true;
	        		while (pendingDownloads.size() > 0) {
	        			DownloadInfo d = pendingDownloads.remove(0);
	        			Debugger.info("ADD one pending download");
	        			mDownload.download(d.url, d.filename);
	        		}
	        	}
	        	public void onServiceDisconnected(ComponentName className) {
	        		Debugger.info("DownloadService disconnected");
	        		mDownload = null;
	        		mDownloadBind = false;
	        	}
	        };
	        bindService(new Intent(this, DownloadService.class), 
	        		mDownloadServiceConn, Context.BIND_AUTO_CREATE);
	        
	        
    	}
    	//Debugger.verbose("Download service started");
    	else {
    		mDownload.download(url, filename);
    	}
    }
    
    private String getUnixFilename(String artist, String title, String url) {
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
    	
    	return artist + "_-_" + title + fileextension;
    }
    
    private void popNotify(String msg)
    {
        Toast.makeText(DoubanFmService.this, msg,
                Toast.LENGTH_LONG).show();
    }
    
    public class DoubanFmControlReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context arg0, Intent arg1) {  
            String action = arg1.getAction(); 
            Bundle b = arg1.getExtras();

            Debugger.debug("received broadcast: " + action);
            if (action.equals(DoubanFmService.CONTROL_NEXT)) {
            	Debugger.info("Douban service received START command");
            	//int sessionid = getSessionId() + 1;
            	//stopMusic();
            	nextMusic();
            	return;
            }
            if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            	String keyevent = arg1.getStringExtra(Intent.EXTRA_KEY_EVENT);
            	Debugger.info("Douban service received MEDIA BUTTON: " + keyevent);
            	//int sessionid = getSessionId() + 1;
            	//stopMusic();
            	nextMusic();
            	abortBroadcast();
		        setResultData(null);		        
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_PLAYPAUSE)) {
            	Debugger.info("Douban service received PLAY/PAUSE command");
            	//int sessionid = getSessionId();
            	playPauseMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_CLOSE)) {
            	Debugger.info("Douban service received CLOSE command");
            	closeService();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_DOWNLOAD)) {
            	Debugger.info("Douban service received DOWNLOAD command");
            	downloadMusic();
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_FAVORITE)) {
            	Debugger.info("Douban service received FAVORITE command");
            	boolean like = arg1.getBooleanExtra("like", true);
            	likeMusic(like);
            	return;
            }
            if (action.equals(DoubanFmService.CONTROL_TRASH)) {
            	Debugger.info("Douban service received FAVORITE command");
            	banMusic();
            	//stopMusic();
            	nextMusic();
            	return;
            }
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            	boolean conn = arg1.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
            	Debugger.info("Douban service received WIFI STATE CHANGED: " + conn);
            	return;
            }
        }
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
    		    Debugger.info("Session failed: " + curMusic.toString());
    		    sendBroadcast(intent);
    		    lastStopReason = DoubanFmApi.TYPE_SKIP;
    		    //stopMusic();
    		    nextMusic();
    		}
    		case PAUSED: {
    			Intent intent = new Intent(STATE_PAUSED);  
    		    Debugger.info("Session failed: " + curMusic.toString());
    		    sendBroadcast(intent);
    		}
    		case IOERROR: {
    			Intent intent = new Intent(STATE_FAILED);  
    		    Debugger.info("Session failed IOERROR: " + curMusic.toString());
    		    sendBroadcast(intent);
    		}
    		case COMPLETED: {
    			Intent intent = new Intent(STATE_FINISHED);  
    		    Debugger.info("Session failed: " + curMusic.toString());
    		    sendBroadcast(intent);
    		    lastStopReason = DoubanFmApi.TYPE_NEW;
    		    //stopMusic();
    		    nextMusic();
    		}
    		default:
    			break;
    		}
    	}
    }
}
