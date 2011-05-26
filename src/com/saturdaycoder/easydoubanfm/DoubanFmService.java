package com.saturdaycoder.easydoubanfm;
import android.app.Notification;
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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class DoubanFmService extends Service  implements IDoubanFmService {

	private MediaPlayer mPlayer = null;
	private int sessionId = -1;
	private DoubanFmMusic fmMusic = null;
	public static final String SESSION_STARTED = "easydoubanfm_music_started";
	public static final String SESSION_FINISHED = "easydoubanfm_music_finished";
	public static final String SESSION_PAUSED = "easydoubanfm_music_paused";
	public static final String SESSION_RESUMED = "easydoubanfm_music_resumed";
	public static final String SESSION_DOWNLOADED = "easydoubanfm_music_downloaded";
	public static final String SESSION_DOWNLOAD_FAILED = "easydoubanfm_music_download_failed";
	public static final String SESSION_FAVOR_ADDED = "easydoubanfm_music_favor_added";
	public static final String SESSION_FAVOR_REMOVED = "easydoubanfm_music_favor_removed";
	public static final String SESSION_FAVOR_TRASHED = "easydoubanfm_music_trashed";
	public static final String SESSION_FAILED = "easydoubanfm_music_failed";
	
	public static final String DOUBAN_FM_NEXT = "easydoubanfm_next";
	public static final String DOUBAN_FM_PLAYPAUSE = "easydoubanfm_playpause";
	public static final String DOUBAN_FM_CLOSE = "easydoubanfm_close";
	public static final String DOUBAN_FM_DOWNLOAD = "easydoubanfm_download";
	public static final String DOUBAN_FM_DOWNLOAD_NOTIFICATION_CLICKED = "easydoubanfm_download_clicked";
	public static final String DOUBAN_FM_FAVORITE = "easydoubanfm_favorite";
	public static final String DOUBAN_FM_TRASH = "easydoubanfm_trash";
	public static final String DOUBAN_FM_SELECT_CHANNEL = "easydoubanfm_select_channel";
	
	public static final String DOUBAN_FM_NULL = "easydoubanfm_null";
	
	
	private final IBinder mBinder = new LocalBinder();	

	private PlayMusicThread musicThread = null;
	//private DownloadMusicTask downloadTask = null;
	private GetPictureTask picTask = null;
	
	private ArrayList<DownloadMusicTask> downloadingList = new ArrayList<DownloadMusicTask>();
	
	public class LocalBinder extends Binder {
		
		DoubanFmService getService() {
			return DoubanFmService.this;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Debugger.verbose("Service onStart");
		super.onStart(intent, startId);
		
		startMusic(++this.sessionId, db.getSelectedChannel());
	}
	
	@Override
	public int getSessionId() {
		return this.sessionId;
	}
	
	private void getChannelTable() {
		Debugger.verbose("Start SCANNING channel table");
		String uri = "http://www.douban.com:80/j/app/radio/channels?";
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeader("User-Agent", "Android-2.2.1");
		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			Debugger.verbose("response is:");
			Debugger.verbose(httpResponse.getStatusLine().toString());
			for (Header h: httpResponse.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Debugger.error("getchannel response is " + httpResponse.getStatusLine().getStatusCode());
			} else {
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				byte b[] = new byte[length];
				int l = 0;
				while (l < length) {
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				String c = new String(b, 0, length, "UTF-8");
				JSONObject json = new JSONObject(c);
				JSONArray jsa = json.getJSONArray("channels");
				for (int i = 0; i < jsa.length(); ++i) {
					DoubanFmChannel chan = new DoubanFmChannel();
					JSONObject co = (JSONObject)jsa.get(i);
					chan.abbrEn = co.getString("abbr_en");
					chan.channelId = co.getInt("channel_id");
					chan.nameEn = co.getString("name_en");
					chan.name = co.getString("name");
					chan.seqId = co.getInt("seq_id");
					Debugger.info("scanned new channel: name=" + chan.name 
							+ " id=" + chan.channelId + " seq=" + chan.seqId);
					db.saveChannel(chan);
				}
			}
		} catch (Exception e) {
			Debugger.error("error scanning channel table: " + e.toString());
		}
	}
	
	private DoubanFmDatabase db;
	
	@Override
	public void onCreate() {
		super.onCreate();
		Debugger.debug("service onCreate");
		
		
		
		
		
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, true);
        // get stored channel table
		db = new DoubanFmDatabase(this);
		
		// get channel table if it's not existing
		DoubanFmChannel[] chans = db.getChannels();
		if (chans == null) {
			EasyDoubanFmWidget.updateWidgetChannel(this, "正在下载频道列表...");
			getChannelTable();
			db.selectChannel(0);
			//EasyDoubanFmWidget.updateWidgetChannel(this, "公共频道");
		}
		
		
		int ci = db.getSelectedChannel();
		if (ci == 0)
			EasyDoubanFmWidget.updateWidgetChannel(this, "公共频道");
		else {
			DoubanFmChannel c = db.getChannelInfo(ci);
			EasyDoubanFmWidget.updateWidgetChannel(this, c.name);
		}
		EasyDoubanFmWidget.updateWidgetBlurChannel(this, false);
		
		// initiate media player
		sessionId = 1;
		if (mPlayer == null)
			mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Debugger.verbose("media player progress " + percent);
				
			}
		});
		mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				int sid = getSessionId();
				Intent intent = new Intent(SESSION_FINISHED);  
			    intent.putExtra("session", sid);  
			    sendBroadcast(intent);
				
			    startMusic(getSessionId() + 1, 0);
			    return true;
			}
		});
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				int sid = getSessionId();
				Intent intent = new Intent(SESSION_FINISHED);  
			    intent.putExtra("session", sid);  
			    sendBroadcast(intent);
				
			    startMusic(getSessionId() + 1, 0);
			}
		});

        if (receiver == null)  {
            receiver = new DoubanFmControlReceiver();  
        }
		IntentFilter filter = new IntentFilter();
		filter.addAction(DoubanFmService.DOUBAN_FM_NEXT);
		filter.addAction(DoubanFmService.DOUBAN_FM_PLAYPAUSE);
		filter.addAction(DoubanFmService.DOUBAN_FM_CLOSE);
		filter.addAction(DoubanFmService.DOUBAN_FM_DOWNLOAD);
		filter.addAction(DoubanFmService.DOUBAN_FM_FAVORITE);
		filter.addAction(DoubanFmService.DOUBAN_FM_TRASH);
		filter.addAction(DoubanFmService.DOUBAN_FM_SELECT_CHANNEL);
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
				int sessionid = getSessionId() + 1;
            	startMusic(sessionid, db.getSelectedChannel());
			}
		};
		
		shakeDetector.registerOnShakeListener(shakeListener);
		shakeDetector.start();
	}
	private ShakeDetector.OnShakeListener shakeListener;
	private ShakeDetector shakeDetector;// = new ShakeDetector(this);
	private DoubanFmControlReceiver receiver;
	
	@Override
	public void resumeService() {
		
	}
	
	@Override
	public void pauseService() {
		
	}
	
	@Override
	public void closeService() {
		Debugger.info("closeService");
		
		stopAllMusic();
		stopSelf();
	}
	
	@Override
	public void stopMusic(int sessionid) {
		Debugger.info("stop session " + sessionid + " current is " + this.sessionId);
		if (sessionid == this.sessionId) {
			try {
				if (mPlayer.isPlaying())
					mPlayer.stop();
				Intent intent = new Intent(SESSION_PAUSED);  
			    intent.putExtra("session", sessionid);  
			    sendBroadcast(intent);
			    Debugger.info("session " + sessionid + " STOPPED");
			} catch (Exception e) {
				Debugger.error("Error stopping music [" + sessionid + "]: " + e.toString());
			}
			EasyDoubanFmWidget.updateWidgetBlurInfo(this, true);
			EasyDoubanFmWidget.clearWidgetInfo(this);
		}
	}
	
	
	public void stopAllMusic() {
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
			Intent intent = new Intent(SESSION_FINISHED);  
		    sendBroadcast(intent);
		    
		} catch (Exception e) {
			Debugger.error("Error stopping all music: " + e.toString());
		}
		EasyDoubanFmWidget.updateWidgetBlurInfo(this, true);
		EasyDoubanFmWidget.clearWidgetInfo(this);
	}
	
	private void playPauseMusic() {
		try {
			if (mPlayer.isPlaying()) {
				mPlayer.pause();
				Intent intent = new Intent(SESSION_PAUSED);  
				intent.putExtra("session", this.sessionId);  
				sendBroadcast(intent);  
			}
			else {
				mPlayer.start();
				Intent intent = new Intent(SESSION_RESUMED);  
				intent.putExtra("session", this.sessionId);  
				sendBroadcast(intent);
			}
		} catch (Exception e) {
			Debugger.error("Error play/pause music: " + e.toString());

		}
	}
	
	private DoubanFmMusic getMusicInfo(int channel) {
		String uri = "http://www.douban.com:80/j/app/radio/people?"
			+ "app_name=radio_android"
			+ "&user_id="//43087578" 
			+ "&token="//91726b356f"
			+ "&expire="//1308458469"
			+ "&version=582"
			+ "&type=n"
			+ "&sid="
			+ "&channel=" + channel
			+ "&h="; //670248:p%7C626377:p%7C704713:p%7C961942:p";
		String date = DateFormat.format("yyyy-MM-dd kk:mm:ss", Integer.parseInt("1308458469")).toString();
		
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeader("User-Agent", "Android-2.2.1");

		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			Debugger.verbose("response is:");
			Debugger.verbose(httpResponse.getStatusLine().toString());
			for (Header h: httpResponse.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				byte b[] = new byte[length];
				int l = 0;
				while (l < length) {
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				String c = new String(b, 0, length, "UTF-8");
				DoubanFmMusic dfm = new DoubanFmMusic();
				JSONObject json = new JSONObject(c);
				JSONArray jsonsongarray = json.getJSONArray("song");
				json = jsonsongarray.getJSONObject(0);
				dfm.album = json.getString("album");
				dfm.albumtitle = json.getString("albumtitle");
				dfm.artist = json.getString("artist");
				dfm.company = json.getString("company");
				dfm.rating_avg = json.getDouble("rating_avg");
				dfm.title = json.getString("title");
				dfm.pictureUrl = json.getString("picture");
				dfm.musicUrl = json.getString("url");
				return dfm;
			} else {
				Debugger.error("response is " + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
		} catch (ClientProtocolException e) {
			Debugger.error("Error get new music info: " + e.toString());
			return null;
		} catch (IOException e) {
			Debugger.error("Error get new music info: " + e.toString());	
			popNotify("网络连接发生故障。请检查您的网络连接，或者稍等片刻，然后按前进按钮或耳机线控按钮，或者甩动手机继续。");
			return null;
		} catch (Exception e) {
			Debugger.error("Error get new music info: " + e.toString());			
			return null;
		}
	}
	
	@Override
	public void selectChannel(int id) {
		Debugger.info("SELECT CHANNEL to " + id);
		DoubanFmChannel chan = db.getChannelInfo(id);
		if (chan == null) {
			Debugger.error("#### channel user selected is invalid");
			id = 0;
			db.selectChannel(id);
			EasyDoubanFmWidget.updateWidgetChannel(this, "公共频道");
		}
		else {
			db.selectChannel(id);
			EasyDoubanFmWidget.updateWidgetChannel(this, (id == 0)? "公共频道": chan.name);
		}
		stopAllMusic();
		startMusic(getSessionId() + 1, id);
	}
	
	@Override
	public void startMusic(int sessionid, int channel) {
		//stopAllMusic();
		
		EasyDoubanFmWidget.updateWidgetBlurInfo(this, true);
		
		if (mPlayer.isPlaying()) 
			stopAllMusic();
		
		
		//EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, null, null);
	    EasyDoubanFmWidget.updateWidgetProgress(this, 0);
		
		Debugger.info("session " + sessionid + " STARTED");
		
		EasyDoubanFmWidget.updateWidgetProgress(this, 20);
		EasyDoubanFmWidget.clearWidgetInfo(this);
		DoubanFmMusic dfm = getMusicInfo(channel);
		EasyDoubanFmWidget.updateWidgetProgress(this, 40);
		if (dfm == null) {
			Intent intent = new Intent(SESSION_FAILED);  
		    intent.putExtra("session", sessionid);  
		    sendBroadcast(intent);  
		    return;
		} 
		
		
		this.sessionId = sessionid;
		this.fmMusic = dfm;
		
		// report picture ready
		Intent intent = new Intent(SESSION_STARTED);  
		intent.putExtra("session", sessionid);
	    intent.putExtra("album", dfm.album);
		intent.putExtra("albumtitle", dfm.albumtitle);
		intent.putExtra("artist", dfm.artist);
		intent.putExtra("company", dfm.company);
		intent.putExtra("rating_avg", dfm.rating_avg);
		intent.putExtra("title", dfm.title);
		intent.putExtra("pictureUrl", dfm.pictureUrl);
		intent.putExtra("musicUrl", dfm.musicUrl);
		Debugger.info("Session started: " + dfm.toString());
	    sendBroadcast(intent);  
    	Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.progress2);
	    EasyDoubanFmWidget.updateWidgetInfo(this, bmp, dfm);
	    
	    EasyDoubanFmWidget.updateWidgetBlurInfo(this, false);
	    
		// get music
	    //PlayMusicTask musicTask = new PlayMusicTask();
	    //musicTask.execute(dfm.musicUrl);
	    musicThread = new PlayMusicThread(mPlayer, dfm.musicUrl);
	    musicThread.start();
	    
	    EasyDoubanFmWidget.updateWidgetProgress(this, 60);
		
	    // update appwidget view image
	    if (picTask != null)
	    	picTask.cancel(true);
	    picTask = new GetPictureTask(sessionid);
	    picTask.execute(dfm.pictureUrl);
	    
	    
	}
	
	
	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Debugger.verbose("Service onStartCommand(intent, " + flags + ", " + startId + ")");
		startMusic(++this.sessionId, 0);
		return START_STICKY;
	}*/
	
	@Override
	public void onDestroy() {
		Debugger.info("onDestroy");
		EasyDoubanFmWidget.updateWidgetOnOffButton(this, false);
		EasyDoubanFmWidget.updateWidgetInfo(this, null, null);
		//EasyDoubanFmWidget.updateWidgetProgress(this, 0);
		
		this.sessionId = -1;
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
		
		//for (DownloadMusicTask t: downloadingList) {
		//	t.cancel(true);
		//}
		
		if (musicThread != null) {
			try  {
				musicThread.join();
			} catch (Exception e) {
				
			}
		}
		
		stopAllMusic();
		
		EasyDoubanFmWidget.updateWidgetBlurText(this, true);
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
    	public GetPictureTask(int session) {
    		this.session = session;
    	}
    	private int session;
    	@Override
    	protected void onCancelled () {
    		Debugger.info("GetPictureTask[" + session + "] is cancelled");
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
    		httpGet.setHeader("User-Agent", "Android-2.2.1");
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
    		if (session == getSessionId()) {
    			EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, bmp, null);
    			EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, 100);
    		}
    		
        }
    }
    
    private class PlayMusicTask extends AsyncTask<String, Integer, Integer> {
    	@Override
    	protected Integer doInBackground(String... params) {
    		String musicurl = "";
    		try {
    			if (mPlayer.isPlaying())
    				mPlayer.stop();
    			mPlayer.reset();
    		} catch (Exception e) {
    			Debugger.error("stop mediaplayer error: " + e.toString());
    			return -1;
    		}
    		try {
    			musicurl = (String)params[0];
    		} catch (Exception e) {
    			Debugger.error("get param for music url error: " + e.toString());
    			return -1;
    		}
    		try {	
    			mPlayer.setDataSource(musicurl);//DoubanFmService.this, Uri.parse(musicurl));
    		} catch (Exception e) {
    			Debugger.error("mediaplayer setDataSource error: " + e.toString());
    			return -1;
    		}
    		try {
    			mPlayer.prepare();
    		} catch (Exception e) {
    			Debugger.error("mediaplayer prepare error: " + e.toString());
    			return -1;
    		}
    		try {
    			mPlayer.seekTo(0);
    		} catch (Exception e) {
    			Debugger.error("mediaplayer seek error: " + e.toString());
    			return -1;
    		}
    		try {
    			mPlayer.start();
    			return 0;
    		} catch (Exception e) {
    			Debugger.error("mediaplayer play error: " + e.toString());
    			return -1;
    		}
    		
        	
    	}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
            
        }
    	@Override
        protected void onPostExecute(Integer result) {
    		int s = getSessionId();
    		switch (result) {
    		case 0: {
	    		Intent i = new Intent(SESSION_PAUSED);  
			    i.putExtra("session", s);  
			    sendBroadcast(i);
			    break;
    		}
    		case -1: {
	    		Intent i = new Intent(SESSION_FAILED);  
			    i.putExtra("session", s);  
			    sendBroadcast(i);
			    break;
    		}
    		default:
    			break;
    		}
        }
    }
    
    private class DownloadMusicTask extends AsyncTask<DoubanFmMusic, Integer, Integer> {
    	public DownloadMusicTask(int session) {
    		this.sessionId = session;
    	}
    	private int sessionId;
    	private DoubanFmMusic dfm;
    	@Override
    	protected void onCancelled() {
    		downloadingList.remove(this);
    		showStatusBarNotification(sessionId, dfm, DownloadStatus.DOWNLOAD_FAIL);
			Debugger.info("Downloading cancelled");
			Intent i = new Intent(SESSION_DOWNLOAD_FAILED);  
		    i.putExtra("session", sessionId);  
		    sendBroadcast(i);
    	}
    	@Override
    	protected Integer doInBackground(DoubanFmMusic... params) {
    		
    		showStatusBarNotification(sessionId, fmMusic, DownloadStatus.DOWNLOADING);
    		dfm = params[0];
    		
    		String musicurl = dfm.musicUrl;
    		String filename = dfm.title + ".mp3"; 
    		HttpGet httpGet = new HttpGet(musicurl);
    		httpGet.setHeader("User-Agent", "Android-2.2.1");

    		HttpResponse httpResponse = null;
    		try {
    			httpResponse = new DefaultHttpClient().execute(httpGet);
    			publishProgress(10);
    		} catch (Exception e) {
    			Debugger.error("Error getting response of downloading music: " + e.toString());
    			return -1;
    		}
    		
    		int statuscode = httpResponse.getStatusLine().getStatusCode();
			if (statuscode != 200) {
				Debugger.error("Error getting response of downloading music: status " + statuscode);
    			return -1;
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
					double prog = 10 + ((double)l / length * 80);
					publishProgress((int)prog);
				}
				publishProgress(90);
			} catch (Exception e) {
				Debugger.error("Error getting content of music: " + e.toString());
				return -1;
			}
			try {
				File dir = new File(android.os.Environment.getExternalStorageDirectory(), 
						"/easydoubanfm");
				if (!dir.exists()) {
				   dir.mkdirs();
				}
				File musicfile = new File(android.os.Environment.getExternalStorageDirectory(), 
						"/easydoubanfm/" + filename);
				OutputStream os = new FileOutputStream(musicfile);
				os.write(b);
				os.flush();
				os.close();
				publishProgress(100);
				return 0;
			} catch (Exception e) {
				Debugger.error("Error writing file to SD card: " + e.toString());
				return -1;
			}
		}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
            Debugger.info("File download progress " + progress[0].intValue() + "%");
        }
    	@Override
        protected void onPostExecute(Integer result) {
    		int s = sessionId;
    		downloadingList.remove(this);
    		switch (result) {
    		case 0: {
    			showStatusBarNotification(sessionId, dfm, DownloadStatus.DOWNLOAD_SUCC);
    			Debugger.info("Downloading success");
	    		Intent i = new Intent(SESSION_DOWNLOADED);  
			    i.putExtra("session", s);  
			    sendBroadcast(i);
			    break;
    		}
    		case -1: {
    			showStatusBarNotification(sessionId, dfm, DownloadStatus.DOWNLOAD_FAIL);
    			Debugger.info("Downloading failed");
	    		Intent i = new Intent(SESSION_DOWNLOAD_FAILED);  
			    i.putExtra("session", s);  
			    sendBroadcast(i);
			    break;
    		}
    		default:
    			break;
    		}
        }
    }
    
    private enum DownloadStatus {
    	DOWNLOADING,
    	DOWNLOAD_SUCC,
    	DOWNLOAD_FAIL,
    	DOWNLOAD_CANCEL,
    }
    
    protected void showStatusBarNotification(int sessionId, DoubanFmMusic dfm,
    		DownloadStatus status) {
    	int sid = sessionId;
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		

		
		//PendingIntent contentIntent = null;
		if (dfm != null) {
			Intent i = new Intent(DoubanFmService.DOUBAN_FM_DOWNLOAD_NOTIFICATION_CLICKED);
			i.putExtra("session", sid);
			PendingIntent pi = PendingIntent.getService(this, 
					0, i, 0);

			switch (status) {
			case DOWNLOADING: {
				Notification notification = new Notification(android.R.drawable.stat_sys_download, 
						"下载中",
		                System.currentTimeMillis());
		        notification.setLatestEventInfo(this, "正在下载", dfm.title, pi);
		        nm.notify(sid, notification);
		        break;
			}
			case DOWNLOAD_SUCC:{
				Notification notification = new Notification(android.R.drawable.stat_sys_download_done, 
						"下载完成",
		                System.currentTimeMillis());
		        notification.setLatestEventInfo(this, "下载完成", "文件存放于/sdcard/easydoubanfm/" + dfm.title + ".mp3", pi);
		        nm.notify(sid, notification);
		        break;
			}
			case DOWNLOAD_FAIL:{
				Notification notification = new Notification(android.R.drawable.stat_notify_error, 
						"下载失败",
		                System.currentTimeMillis());
		        notification.setLatestEventInfo(this, "下载失败", dfm.title, pi);
		        nm.notify(sid, notification);
		        break;
			}
			default:
				break;
			}
	        
			
		}
		else {
			
		}
	}
    
    protected void cancelStatusBarNotification(int sid) {
    	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	nm.cancel(sid);
    }
    
    private void favorMusic() {
    	Debugger.error("This is not supported yet");
    }
    
    private void hateMusic() {
    	Debugger.error("This is not supported yet");
    }
    
    private void downloadMusic() {
    	DownloadMusicTask downloadTask = new DownloadMusicTask(getSessionId());
    	downloadingList.add(downloadTask);
    	Debugger.verbose("start an async task of downloading");
    	downloadTask.execute(fmMusic);
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
            if (action.equals(DoubanFmService.DOUBAN_FM_NEXT)) {
            	Debugger.info("Douban service received START command");
            	int sessionid = getSessionId() + 1;
            	startMusic(sessionid, db.getSelectedChannel());
            	return;
            }
            if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            	String keyevent = arg1.getStringExtra(Intent.EXTRA_KEY_EVENT);
            	Debugger.info("Douban service received MEDIA BUTTON: " + keyevent);
            	int sessionid = getSessionId() + 1;
            	startMusic(sessionid, db.getSelectedChannel());
            	abortBroadcast();
		        setResultData(null);		        
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_PLAYPAUSE)) {
            	Debugger.info("Douban service received PLAY/PAUSE command");
            	//int sessionid = getSessionId();
            	playPauseMusic();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_CLOSE)) {
            	Debugger.info("Douban service received CLOSE command");
            	closeService();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_DOWNLOAD)) {
            	Debugger.info("Douban service received DOWNLOAD command");
            	downloadMusic();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_FAVORITE)) {
            	Debugger.info("Douban service received FAVORITE command");
            	favorMusic();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_TRASH)) {
            	Debugger.info("Douban service received FAVORITE command");
            	hateMusic();
            	return;
            }
            if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            	boolean conn = arg1.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
            	Debugger.info("Douban service received WIFI STATE CHANGED: " + conn);
            	return;
            }
        }
    }  
}
