package com.saturdaycoder.easydoubanfm;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateFormat;
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
	public static final String DOUBAN_FM_FAVORITE = "easydoubanfm_favorite";
	public static final String DOUBAN_FM_TRASH = "easydoubanfm_trash";
	public static final String DOUBAN_FM_SELECT_CHANNEL = "easydoubanfm_select_channel";
	
	
	private final IBinder mBinder = new LocalBinder();	

	private PlayMusicThread musicThread = null;
	
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
		
		db = new DoubanFmDatabase(this);
		
		// get channel table if it's not existing
		DoubanFmChannel[] chans = db.getChannels();
		if (chans == null) {
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
		registerReceiver(receiver, filter); 
		Debugger.verbose("DoubanFm Control Service registered");
	}
	private DoubanFmControlReceiver receiver;
	
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
				Debugger.dumpException(e);
			}
		}
	}
	
	
	public void stopAllMusic() {
		Debugger.info("stop all music");
		try {
			if (mPlayer.isPlaying())
				mPlayer.stop();
			Intent intent = new Intent(SESSION_FINISHED);  
		    sendBroadcast(intent);
		    
		} catch (Exception e) {
			Debugger.dumpException(e);
		}
		
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
			Debugger.dumpException(e);
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
			Debugger.dumpException(e);
			return null;
		} catch (IOException e) {
			Debugger.dumpException(e);
			return null;
		} catch (Exception e) {
			Debugger.dumpException(e);
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
			EasyDoubanFmWidget.updateWidgetChannel(this, chan.name);
		}
		stopAllMusic();
		startMusic(getSessionId() + 1, id);
	}
	
	@Override
	public void startMusic(int sessionid, int channel) {
		//stopAllMusic();
		
		
		if (mPlayer.isPlaying()) 
			stopAllMusic();
		
		EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, null, null);
	    EasyDoubanFmWidget.updateWidgetProgress(this, true);
		
		Debugger.info("session " + sessionid + " STARTED");
		
		DoubanFmMusic dfm = getMusicInfo(channel);
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
	    EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, null, dfm);
	    
		// get music
	    //PlayMusicTask musicTask = new PlayMusicTask();
	    //musicTask.execute(dfm.musicUrl);
	    musicThread = new PlayMusicThread(mPlayer, dfm.musicUrl);
	    musicThread.start();
		
	    // update appwidget view image
	    GetPictureTask picTask = new GetPictureTask(sessionid);
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
		EasyDoubanFmWidget.updateWidgetProgress(this, false);
		
		this.sessionId = -1;
		unregisterReceiver(receiver); 
		Debugger.verbose("DoubanFm Control Service unregistered");
		receiver = null;
		
		if (musicThread != null) {
			try  {
				musicThread.join();
			} catch (Exception e) {
				
			}
		}
		
		stopAllMusic();
		
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
    	protected Bitmap doInBackground(String... params) {
    		try {
    	    	URL url = new URL((String)params[0]);
    	    	HttpURLConnection conn = (HttpURLConnection)url.openConnection();
    	    	conn.connect();
    	    	InputStream is = conn.getInputStream();
    	    	Bitmap bmp = BitmapFactory.decodeStream(is);
    	    	is.close();
    	    	return bmp;
        	} catch (Exception e) {
        		Debugger.dumpException(e);
        		return null;
        	}
        	
    	}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
            
        }
    	@Override
        protected void onPostExecute(Bitmap bmp) {
    		if (session == getSessionId()) {
    			EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, bmp, null);
    			EasyDoubanFmWidget.updateWidgetProgress(DoubanFmService.this, false);
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
    	@Override
    	protected Integer doInBackground(DoubanFmMusic... params) {
    		showStatusBarNotification(fmMusic);
    		DoubanFmMusic dfm = params[0];
    		
    		String musicurl = dfm.musicUrl;
    		String filename = dfm.title + ".mp3"; 
    		HttpGet httpGet = new HttpGet(musicurl);
    		httpGet.setHeader("User-Agent", "Android-2.2.1");

    		HttpResponse httpResponse = null;
    		try {
    			httpResponse = new DefaultHttpClient().execute(httpGet);
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
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
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
				return 0;
			} catch (Exception e) {
				Debugger.error("Error writing file to SD card: " + e.toString());
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
    			Debugger.info("Downloading success");
	    		Intent i = new Intent(SESSION_DOWNLOADED);  
			    i.putExtra("session", s);  
			    sendBroadcast(i);
			    break;
    		}
    		case -1: {
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
    
    protected void showStatusBarNotification(DoubanFmMusic dfm) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Notification notification = new Notification(R.drawable.stat_sys_download_anim4, 
				"下载中",
                System.currentTimeMillis());
		
		PendingIntent contentIntent = null;
		if (dfm != null) {
			contentIntent = PendingIntent.getActivity(this, 0,
						new Intent(this, DoubanFmService.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT);
				
	        notification.setLatestEventInfo(this, "正在下载", dfm.title, contentIntent);
	
	        nm.notify(0, notification);
	        
		}
		else {
			
		}
	}
    
    protected void cancelStatusBarNotification() {
    	NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	nm.cancel(0);
    }
    
    private void favorMusic() {
    	Debugger.error("This is not supported yet");
    }
    
    private void hateMusic() {
    	Debugger.error("This is not supported yet");
    }
    
    private void downloadMusic() {
    	DownloadMusicTask musicTask = new DownloadMusicTask();
    	Debugger.verbose("start an async task of downloading");
	    musicTask.execute(fmMusic);
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
            
        }
    }  
}
