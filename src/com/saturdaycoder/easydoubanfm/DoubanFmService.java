package com.saturdaycoder.easydoubanfm;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.net.Uri;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import android.media.MediaPlayer;
import org.json.*;

import com.saturdaycoder.easydoubanfm.EasyDoubanFm.DoubanFmReceiver;

import android.media.AudioManager;
import android.widget.RemoteViews;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DoubanFmService extends Service  implements IDoubanFmService {

	private MediaPlayer mPlayer;
	private int sessionId = -1;
	public static final String SESSION_STARTED = "easydoubanfm_music_started";
	public static final String SESSION_FINISHED = "easydoubanfm_music_finished";
	public static final String SESSION_STOPPED = "easydoubanfm_music_stopped";
	public static final String SESSION_FAILED = "easydoubanfm_music_failed";
	
	//public static final String DOUBAN_FM_CONTROL = "easydoubanfm_control";
	public static final String DOUBAN_FM_START = "easydoubanfm_start";
	public static final String DOUBAN_FM_STOP = "easydoubanfm_stop";
	public static final String DOUBAN_FM_CLOSE = "easydoubanfm_close";
	public static final String DOUBAN_FM_PLAYPAUSE = "easydoubanfm_playpause";
	public static final String DOUBAN_FM_DOWNLOAD = "easydoubanfm_download";
	public static final String DOUBAN_FM_ADDTOFAVORIATE = "easydoubanfm_addtofavorite";
	public static final String DOUBAN_FM_ADDTOTRASH = "easydoubanfm_addtotrash";
	
	//public static final String DOUBAN_FM_ARG_SESSION = "session";
	//public static final String DOUBAN_FM_ARG_COMMAND = "command";
	
	//public static final int DOUBAN_FM_CONTROL_START = 1;
	//public static final int DOUBAN_FM_CONTROL_STOP = 2;
	//public static final int DOUBAN_FM_CONTROL_CLOSE = 3;
	
	private final IBinder mBinder = new LocalBinder();
	
	

	

	
	public class LocalBinder extends Binder {
		DoubanFmService getService() {
			return DoubanFmService.this;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Debugger.verbose("Service onStart");
		super.onStart(intent, startId);

		startMusic(++this.sessionId, 1);
	}
	
	@Override
	public int getSessionId() {
		return this.sessionId;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		sessionId = 1;
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
		filter.addAction(DoubanFmService.DOUBAN_FM_START);
		filter.addAction(DoubanFmService.DOUBAN_FM_STOP);
		filter.addAction(DoubanFmService.DOUBAN_FM_CLOSE);
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
				Intent intent = new Intent(SESSION_STOPPED);  
			    intent.putExtra("session", sessionid);  
			    sendBroadcast(intent);
			    Debugger.info("session " + sessionid + " STOPPED");
			} catch (Exception e) {
				Debugger.dumpException(e);
			}
		}
	}
	
	private void stopAllMusic() {
		try {
			if (mPlayer.isPlaying())
				mPlayer.stop();
			Intent intent = new Intent(SESSION_FAILED);  
		    intent.putExtra("session", this.sessionId);  
		    sendBroadcast(intent);  
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
	public void startMusic(int sessionid, int channel) {
		//stopAllMusic();
		
		if (mPlayer.isPlaying()) 
			stopAllMusic();
		
		Debugger.info("session " + sessionid + " STARTED");
		
		DoubanFmMusic dfm = getMusicInfo(channel);
		if (dfm == null) {
			Intent intent = new Intent(SESSION_FAILED);  
		    intent.putExtra("session", sessionid);  
		    sendBroadcast(intent);  
		    return;
		} 
		
		
		this.sessionId = sessionid;
		
		
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
	    PlayMusicTask musicTask = new PlayMusicTask();
	    musicTask.execute(dfm.musicUrl);
		
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
		stopAllMusic();
		this.sessionId = -1;
		unregisterReceiver(receiver); 
		Debugger.verbose("DoubanFm Control Service unregistered");
		receiver = null;
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
    		if (session == getSessionId())
    			EasyDoubanFmWidget.updateWidgetInfo(DoubanFmService.this, bmp, null);
    		
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
    			mPlayer.setDataSource(DoubanFmService.this, Uri.parse(musicurl));
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
	    		Intent i = new Intent(SESSION_STOPPED);  
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
    
    public class DoubanFmControlReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context arg0, Intent arg1) {  
            String action = arg1.getAction(); 
            Bundle b = arg1.getExtras();

            Debugger.verbose("received broadcast: " + action);
            if (action.equals(DoubanFmService.DOUBAN_FM_START)) {
            	Debugger.info("Douban service received START command");
            	int sessionid = getSessionId() + 1;
            	startMusic(sessionid, 0);
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_STOP)) {
            	Debugger.info("Douban service received STOP command");
            	int sessionid = getSessionId();
            	//stopMusic(sessionid);
            	stopAllMusic();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_CLOSE)) {
            	Debugger.info("Douban service received CLOSE command");
            	closeService();
            	return;
            }
            if (action.equals(DoubanFmService.DOUBAN_FM_DOWNLOAD)) {
            	Debugger.info("Douban service received DOWNLOAD command");
            	closeService();
            	return;
            }
        }
    }  
}
