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
import android.text.format.DateFormat;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
	private MediaScannerConnection.MediaScannerConnectionClient scannerClient;
	//private IDownloadService mDownload;
	//private ServiceConnection mDownloadServiceConn;
	//private boolean mDownloadBind;
	private Database db;
	HttpParams httpParameters;
	private char lastStopReason;
	private Handler mainHandler;
	//private ArrayList<DownloadInfo> pendingDownloads;
	private Downloader downloader;
	private boolean isFmOn;
	private DownloadReceiver downloadReceiver;
	
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
		if (!isFmOn) {
			openFM();
			isFmOn = true;
		}
		
		

		
        // get stored channel table
		db = new Database(this);		
		// get channel table if it's not existing
		FmChannel[] chans = db.getChannels();
		if (chans == null) {
			EasyDoubanFmWidget.updateWidgetChannel(this, 
					getResources().getString(R.string.text_channel_updating));
			chans = DoubanFmApi.getChannelTable();
			for (FmChannel c: chans) {
				db.saveChannel(c);
			}
			Preference.selectChannel(this, 0);
			
		}
		
		// get the currently selected channel
		int ci = Preference.getSelectedChannel(this);
		FmChannel c = db.getChannelInfo(ci);
		
		
		// check login
		boolean login = Preference.getLogin(this);
		if (login) {
			EasyDoubanFmWidget.updateWidgetChannel(this, 
					getResources().getString(R.string.text_login_inprocess));
			String email = Preference.getAccountEmail(this);
			String passwd = Preference.getAccountPasswd(this);
			try {
				session = DoubanFmApi.login(email, passwd, 583);
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
			}
		}
		else {
			session = null;
		}
		
		EasyDoubanFmWidget.updateWidgetChannel(this, c.getDisplayName(login));
		
		nextMusic();
	}
	

	@Override
	public boolean login(String email, String passwd) {
		try {
			session = DoubanFmApi.login(email, passwd, 583);
			Preference.setLogin(this, (session != null));
		} catch (IOException e) {
			popNotify(getResources().getString(R.string.text_network_error));
			Debugger.error("network error: " + e.toString());
			session = null;
		}
		return (session != null);
	}

	
	@Override
	public void onCreate() {
		super.onCreate();
		Debugger.debug("service onCreate");
		
		if (downloader == null)
			downloader = new Downloader(this);
		
		httpParameters = new BasicHttpParams();
		int timeoutConnection = Preference.getConnectTimeout(this);
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		int timeoutSocket = Preference.getSocketTimeout(this);
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
		DoubanFmApi.setHttpParameters(httpParameters);
	}
	private ShakeDetector.OnShakeListener shakeListener;
	private ShakeDetector shakeDetector;// = new ShakeDetector(this);
	private DoubanFmControlReceiver receiver;
	
	private void openFM() {
		curMusic = lastMusic = null;
		
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
		
		filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		filter.addAction(Intent.ACTION_MEDIA_BUTTON );
		
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		registerReceiver(receiver, filter); 
		
		IntentFilter dfilter = new IntentFilter();
		dfilter.addAction(DoubanFmService.ACTION_DOWNLOAD);
		dfilter.addAction(DoubanFmService.ACTION_CLEAR_NOTIFICATION);
		downloadReceiver = new DownloadReceiver();
		registerReceiver(downloadReceiver, dfilter);
		
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
	
	
	@Override
	public void closeFM() {
		
		Debugger.info("DoubanFmService closeFM");
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, false);
		EasyDoubanFmWidget.updateWidgetInfo(this, null, null);

		unregisterReceiver(receiver); 
		
		if (shakeDetector != null ) {
			shakeDetector.stop();
			if (shakeListener != null)
				shakeDetector.unregisterOnShakeListener(shakeListener);
		}
		
		Debugger.debug("DoubanFm Control Service unregistered");
		receiver = null;
		
		if (picTask != null)
			picTask.cancel(true);
		
		if (playThread != null) {
			//playThread.stopPlay();
			playThread.releasePlay();
			playThread.quit();
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
		
		isFmOn = false;
		
		if (!downloader.isRunning()) {
			Debugger.debug("no download in process. close service");
			closeService();
		}
	}
	
	private void closeService() {
		Debugger.info("closeService");
		
		unregisterReceiver(downloadReceiver);
		
		stopSelf();
	}
	

	
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
			//EasyDoubanFmWidget.updateWidgetChannel(this, "公共频道");
		}
		else {
			Preference.selectChannel(this, id);
			//EasyDoubanFmWidget.updateWidgetChannel(this, chan.name);
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
	public void onDestroy() {
		
		
		
		super.onDestroy();
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	
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
    	/*if (!mDownloadBind) {
    		// add this download to pending list
	        DownloadInfo d = new DownloadInfo();
	        d.url = url;
	        d.filename = filename;
	        pendingDownloads.add(d);
    		
    		Intent i = new Intent(this, Downloader.class);
    		this.startService(i);
    		
	    	mDownloadServiceConn = new ServiceConnection(){
	        	public void onServiceConnected(ComponentName className, IBinder service) {
	        		Downloader.LocalBinder b = (Downloader.LocalBinder)service;
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
	        bindService(new Intent(this, Downloader.class), 
	        		mDownloadServiceConn, 0);//Context.BIND_NOT_FOREGROUND);
	        
	        
    	}
    	//Debugger.verbose("Download service started");
    	else {
    		mDownload.download(url, filename);
    	}*/
    	downloader.download(url, filename);
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
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				if (sessionId != -1) {
					nm.cancel(sessionId);
				}
			}
			if (action.equals(ACTION_DOWNLOAD)) {
				String url = arg1.getStringExtra(EXTRA_DOWNLOAD_URL);
				String filename = arg1.getStringExtra(EXTRA_DOWNLOAD_FILENAME);
				downloader.download(url, filename);
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
            	closeFM();
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
    
	private static final int DOWNLOAD_ERROR_OK = 0;
	private static final int DOWNLOAD_ERROR_IOERROR = -1;
	private static final int DOWNLOAD_ERROR_CANCELLED = -2;
	//private static final int 
	
	

	
	private void notifyDownloading(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Intent i = new Intent(ACTION_NULL);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_sys_download, 
				getResources().getString(R.string.text_downloading),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getResources().getString(R.string.text_downloading), 
        		filename, pi);
        nm.notify(sessionId, notification);
	}
	
	private void notifyDownloadOk(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				
		Intent i = new Intent(ACTION_CLEAR_NOTIFICATION);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_sys_download_done, 
				getResources().getString(R.string.text_download_ok),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getResources().getString(R.string.text_download_ok), 
        		filename, pi);
        nm.notify(sessionId, notification);
	}
	
	private void notifyDownloadFail(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Intent i = new Intent(ACTION_CLEAR_NOTIFICATION);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_notify_error, 
				getResources().getString(R.string.text_download_fail),
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, getResources().getString(R.string.text_download_fail), 
        		filename, pi);
        nm.notify(sessionId, notification);
		
	}
	
	public static final String ACTION_DOWNLOAD = "com.saturdaycoder.easydoubanfm.download";
	public static final String ACTION_CLEAR_NOTIFICATION = "com.saturdaycoder.easydoubanfm.clear_notification";
	public static final String ACTION_NULL = "com.saturdaycoder.easydoubanfm.null";
	public static final String EXTRA_DOWNLOAD_SESSION = "session";
	public static final String EXTRA_DOWNLOAD_URL = "url";
	public static final String EXTRA_DOWNLOAD_FILENAME = "filename";
	
	private class Downloader { 
		private static final int DOWNLOAD_BUFFER = 81920;
		private class DownloadTask extends AsyncTask<String, Integer, Integer> {
			private int sessionId;
			private int progress;
			private String url;
			private String filename;
			public DownloadTask(int sessionId) {
				this.sessionId = sessionId;
				progress = -1;
			}
			public int getProgress() {
				return progress;
			}
			@Override
	    	protected void onProgressUpdate(Integer... progress) {
				this.progress = progress[0];
				Debugger.verbose("Download progress: " + this.progress);
			}
			
			@Override
	        protected void onPostExecute(Integer result) {
				switch (result) {
				case DOWNLOAD_ERROR_OK:
					Debugger.info("Download finish");
					notifyDownloadOk(this.sessionId, this.url, this.filename);
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
				if (tasks.size() == 0 && !isFmOn) {
					closeService();
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
				if (tasks.size() == 0 && !isFmOn) {
					closeService();
				}
			}
			
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
	    				String.valueOf(Preference.getClientVersion(DoubanFmService.this)));

	    		HttpResponse httpResponse = null;
	    		try {
	    			httpResponse = new DefaultHttpClient(httpParameters).execute(httpGet);
	    			publishProgress(5);
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
				publishProgress(10);
				
				// step 3. write into file after each read
				byte b[] = new byte[DOWNLOAD_BUFFER];
				try {
					InputStream is = httpResponse.getEntity().getContent();
					long len = httpResponse.getEntity().getContentLength();
					int l = 0;
					while (l < len) {
						if (isCancelled())
							return DOWNLOAD_ERROR_CANCELLED;
						int tmpl = is.read(b, l, DOWNLOAD_BUFFER);
						if (tmpl == -1)
							break;
						
						Debugger.debug("writing file " + tmpl + ", " + l + "/" + len);
						os.write(b, 0, tmpl);
						l += tmpl;
						
						double prog = 10 + ((double)l / len * 90);
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
		private int sessionId;
		
		
		public boolean isRunning() {
			Debugger.info("downloader task list size: " + tasks.size());
			return tasks.size() > 0;
		}
		
		public Downloader(Context context) {
			this.context = context;
			Debugger.info("DOWNLOADER CREATE");
			tasks = new HashMap<Integer, DownloadTask>();
			sessionId = 1;
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

		public int download(String url, String filename) {
			Debugger.debug("DownloadService.download(" + url + ", " + filename + ")");
			DownloadTask task = null;
			int ret = -1;
			synchronized(this) {
				task = new DownloadTask(sessionId);
				tasks.put(sessionId, task);
				ret = sessionId;
				++sessionId;
			}
			Debugger.debug("execute task " + ret);
			notifyDownloading(ret, url, filename);
			task.execute(url, filename);
			return ret;
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
			}
		}
	}
    
}
