package com.saturdaycoder.easydoubanfm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;


public class DoubanFmPlayer {
	private static final int MAX_HISTORY_COUNT = 20;
	private static final int MAX_PENDING_COUNT = 20;
	private static final int MAX_PREBUFFER_PLAYER_COUNT = 2;
	
	private static final int NO_REASON = -1;
	
	private boolean isOpen = false;
	private Context context = null;
	private Database db = null;
	private ArrayList<MusicInfo> musicHistory = new ArrayList<MusicInfo>();
	private ArrayList<MusicInfo> pendingMusicList = new ArrayList<MusicInfo>();
	
	private MusicInfo curMusic = null;
	private MusicInfo lastMusic = null;
	private final Object musicSessionLock = new Object();
	
	private Bitmap curPic = null;
	private Bitmap defaultAlbumPic = null;
	private GetPictureTask picTask = null;
	
	private char lastStopReason = DoubanFmApi.TYPE_NEW;
	private final Object stopReasonLock = new Object();
	
	//private PlayMusicThread playThread = null;	
	
	private int[] appWidgetIds = null;
	private final Object appWidgetIdsLock = new Object();
	
	//private Handler mainHandler = null;
	private LoginSession loginSession = null;
	private final Object loginLock = new Object();
	
	private AudioManager audioManager = null;

	private MediaPlayer mPlayer;
	
	public void setAppWidgetIds(int[] ids) {
		synchronized(appWidgetIdsLock) {
			appWidgetIds = ids;
		}
	}
	
	public DoubanFmPlayer(Context context) {
		this.isOpen = false;
		this.context = context;
		this.audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		this.picTask = new GetPictureTask();
		
		synchronized(DoubanFmPlayer.class) {
			// get stored channel table
			db = new Database(context);		
			// the API isn't updated by douban.com. So write it by 
			// myself instead of fetching from douban.com
			db.clearChannels();
			for(FmChannel fc: FmChannel.AllChannels) {
				db.saveChannel(fc);
			}
		}
		
		defaultAlbumPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album);
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public boolean isPlaying() {
		if (mPlayer == null)
			return false;
		else 
			return mPlayer.isPlaying();
	}
	
	public MusicInfo getCurMusic() {
		return curMusic;
	}
	
	public Bitmap getCurPic() {
		return curPic;
	}
	
	public void open() {
		synchronized(loginLock) {
			this.loginSession = null;
		}
		
		notifyPowerStateChanged(DoubanFmService.STATE_PREPARE);
		
		
		if (!isOpen) {
			synchronized(this) {
				if (!isOpen) {
					if (mPlayer != null) {
						mPlayer.reset();
						mPlayer.release();
						mPlayer = null;
					}
					
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
							//reportState(PlayState.ERROR);
							Debugger.error("media player onError: what=" + what + " extra=" + extra);
							nextMusic();
						    return true;
						}
					});
					mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
						
						@Override
						public boolean onInfo(MediaPlayer mp, int what, int extra) {
							Debugger.warn("media player onInfo: what=" + what + " extra=" + extra);
							return true;
						}
					});
					mPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
						
						@Override
						public void onSeekComplete(MediaPlayer mp) {
							Debugger.info("media player onSeekComplete");
							
						}
					});
					mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						
						@Override
						public void onPrepared(MediaPlayer mp) {
							Debugger.info("media player onPrepare");
							mPlayer.seekTo(0);
							mPlayer.start();
						}
					});
					mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							Debugger.info("media player onCompletion");
							//reportState(PlayState.COMPLETED);
							nextMusic();
						}
					});
			        
					
			        musicHistory = new ArrayList<MusicInfo>();
			        pendingMusicList = new ArrayList<MusicInfo>();	


					// check login
					boolean login = Preference.getLogin(context);
					if (login) {
						String email = Preference.getAccountEmail(context);
						String passwd = Preference.getAccountPasswd(context);
						login(email, passwd);
					}
					else {
						notifyLoginStateChanged(DoubanFmService.STATE_IDLE, NO_REASON);
						
						loginSession = null;
					}


		
					// get the currently selected channel
					int ci = Preference.getSelectedChannel(context);
										
					// if channel not exist, or it need login but not logged in
					// find the first public channel to select
					if (!FmChannel.isChannelIdValid(ci)
							|| (FmChannel.channelNeedLogin(ci) && loginSession == null)) {
						// selected channel not exist
						ci = FmChannel.getFirstPublicChannel();
						
						Preference.selectChannel(context, ci);
						
					}
					
					
					FmChannel c = db.getChannelInfo(ci);
					
					notifyPowerStateChanged( DoubanFmService.STATE_STARTED);
					
					notifyChannelChanged(c.channelId, c.getDisplayName(login));					
					
					isOpen = true;
					
					nextMusic();

				}
			}
		}
	}
	
	public void close() {
		if (isOpen) {
			synchronized(this) {
				if (isOpen) {
					mPlayer.reset();
					mPlayer.release();
					mPlayer = null;
					
					
					Debugger.info("DoubanFmService closeFM");
					
					//notifyChannelChanged(-1, context.getResources().getString(R.string.text_channel_closing));
					
					notifyPowerStateChanged(DoubanFmService.STATE_PREPARE);

					curMusic = lastMusic = null;
					curPic = null;
					
					if (picTask != null) {
						picTask.cancel(true);
						picTask = null;
					}
					
					notifyPowerStateChanged( DoubanFmService.STATE_IDLE);
					
					isOpen = false;
				}
			}
		}
		
	}
	
	public void skipMusic() {
		// according to Douban API, should send a "type=s" report.
		// but considering efficiency, I just pick one new music from pending list
		nextMusic();
	}
	
	public void pauseMusic() {
		try {
			mPlayer.pause();
			
		} catch (IllegalStateException e) {
			mPlayer.reset();
		}
		notifyMusicPaused();
	}
	
	public void resumeMusic() {
		try {
			mPlayer.start();
			notifyMusicResumed();
		} catch (IllegalStateException e) {
			mPlayer.reset();
			nextMusic();
		}
		
	}
	
	public void playPauseMusic() {
		if (mPlayer.isPlaying()) {
			pauseMusic();
		} else {
			resumeMusic();
		}
	}
	
	public void rateMusic() {
		synchronized(musicSessionLock) {
			if (curMusic == null || curMusic.isRated())
				return;			
			
			synchronized(stopReasonLock) {
				lastStopReason = DoubanFmApi.TYPE_RATE;
				pendingMusicList.clear();
				try {
					fillPendingList();
					
					notifyMusicRated(true);

					curMusic.rate(true);
				} catch (Exception e) {
					Debugger.error("error rating music: " + e.toString());
				}
				lastStopReason = DoubanFmApi.TYPE_NEW;
			}
		}
	}
	
	public void unrateMusic() {
		synchronized(musicSessionLock) {
			if (curMusic == null || !curMusic.isRated())
				return;
			
			synchronized(stopReasonLock) {
				lastStopReason = DoubanFmApi.TYPE_UNRATE;
				pendingMusicList.clear();
				try {
					fillPendingList();
					
					notifyMusicRated(false);
					
					curMusic.rate(false);
				} catch (Exception e) {
					Debugger.error("error rating music: " + e.toString());
				}
				lastStopReason = DoubanFmApi.TYPE_NEW;		
			}
		}
	}
	
	public boolean isMusicRated() {
		synchronized(musicSessionLock) {
			if (curMusic != null)
				return curMusic.isRated();
			else return false;
		}
	}
	
	public void banMusic() {
    	if (curMusic == null) {
    		return;
    	}
    	
    	synchronized(stopReasonLock) {
			lastStopReason = DoubanFmApi.TYPE_BYE;
			pendingMusicList.clear();
			try {
				fillPendingList();
				
				notifyMusicBanned();
				
			} catch (Exception e) {
				Debugger.error("error in banMusic: " + e.toString());
			}
			lastStopReason = DoubanFmApi.TYPE_NEW;
    	}
		
		nextMusic();		
	}
	
	public void selectChannel(int id) {
		Debugger.info("SELECT CHANNEL to " + id);
		FmChannel chan = db.getChannelInfo(id);
		if (chan == null) {
			Debugger.error("#### channel user selected is invalid");
			id = 0;
			chan = db.getChannelInfo(id);
			Preference.selectChannel(context, id);
		}
		else {
			Preference.selectChannel(context, id);
		}
		
		notifyChannelChanged(chan.channelId, chan.name);
		
		pendingMusicList.clear();
		lastStopReason = DoubanFmApi.TYPE_NEW;

		nextMusic();
		
	}
	
	public void forwardChannel() {
		
	}
	
	public void backwardChannel() {
		
	}
	
	public void randomChannel() {
		
	}
	
	public void login(String username, String passwd) {
		if (username == null || username.equals("") 
				|| passwd == null || passwd.equals("")) {
			
			notifyLoginStateChanged(DoubanFmService.STATE_ERROR, DoubanFmService.REASON_API_REQUEST_ERROR);
			
			return;
		}
			
		synchronized(loginLock) {
			try {
				notifyLoginStateChanged(DoubanFmService.STATE_PREPARE, NO_REASON);
				
				loginSession = DoubanFmApi.login(username, passwd, Utility.getClientVersion());
				if (loginSession != null) {
					Preference.setAccountEmail(context, username);
					Preference.setAccountPasswd(context, passwd);
					Preference.setLogin(context, true);
					
					notifyLoginStateChanged(DoubanFmService.STATE_STARTED, NO_REASON);
					
				} else {
					Preference.setAccountEmail(context, username);
					Preference.setAccountPasswd(context, null);
					Preference.setLogin(context, false);
					
					notifyLoginStateChanged(DoubanFmService.STATE_ERROR, DoubanFmService.REASON_API_REQUEST_ERROR);
					
				}
				
				
			} catch (Exception e) {
				Debugger.error("IO ERROR loging in: " + e.toString());
				
				notifyLoginStateChanged(DoubanFmService.STATE_ERROR, 
												DoubanFmService.REASON_NETWORK_IO_ERROR);
				
				loginSession = null;
			}
		}
		//return (session != null);		
	}
	
	public void logout() {
		synchronized(loginLock) {
			Preference.setLogin(context, false);
			loginSession = null;
			
			notifyLoginStateChanged(DoubanFmService.STATE_IDLE, NO_REASON);
		}
	}
	
	private void notifyMusicPrepareProgress(int progress) {
		EasyDoubanFmWidget.setProgress(context, progress);
	}
	
	private void notifyPowerStateChanged(int powerState) {
		WidgetContent wc = EasyDoubanFmWidget.getContent(context);
		switch(powerState) {
		case DoubanFmService.STATE_PREPARE:
			wc.onState = EasyDoubanFmWidget.STATE_PREPARE;
			EasyDoubanFmWidget.updateContent(context, wc, null);
			break;
		case DoubanFmService.STATE_IDLE:
			wc.onState = EasyDoubanFmWidget.STATE_OFF;
			wc.channel = context.getResources().getString(R.string.text_channel_unselected);
			wc.rated = false;
			wc.artist = wc.title = "";
			wc.picture = defaultAlbumPic;
			EasyDoubanFmWidget.updateContent(context, wc, null);
			break;
		case DoubanFmService.STATE_STARTED:
			wc.onState = EasyDoubanFmWidget.STATE_ON;
			EasyDoubanFmWidget.updateContent(context, wc, null);
			break;
		default:
			break;
		}
		
		//Intent i = new Intent(DoubanFmService.EVENT_PLAYER_POWER_STATE_CHANGED);
		//i.putExtra(DoubanFmService.EXTRA_STATE, powerState);
		//context.sendBroadcast(i);
	}
	
	private void notifyMusicStateChanged(int musicState, MusicInfo musicInfo, int reason) {
		WidgetContent content = EasyDoubanFmWidget.getContent(context);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		switch(musicState) {
		case DoubanFmService.STATE_PREPARE:
			content.artist = content.title = "";
			content.rated = false;
			content.picture = defaultAlbumPic;
			EasyDoubanFmWidget.updateContent(context, content, null);
			break;
		case DoubanFmService.STATE_STARTED:	 {
			if (musicInfo != null) {
				content.artist = musicInfo.artist;
				content.rated = musicInfo.isRated();
				content.title = musicInfo.title;
			}
			EasyDoubanFmWidget.updateContent(context, content, null);
			Notification fgNotification = new Notification(R.drawable.icon,
					content.artist + " -- " + content.title,
			        System.currentTimeMillis());
			fgNotification.flags |= Notification.FLAG_NO_CLEAR;
			Intent it = new Intent(DoubanFmService.ACTION_NULL);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
			fgNotification.setLatestEventInfo(context, content.artist, content.title, pi);	
			notificationManager.notify(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
			break;
		}
		case DoubanFmService.STATE_FINISHED: {
			content.artist = content.title = "";
			content.rated = false;
			content.picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album);
			EasyDoubanFmWidget.updateContent(context, content, null);
			break;
		}
		
		case DoubanFmService.STATE_CANCELLED:
		case DoubanFmService.STATE_IDLE:
		case DoubanFmService.STATE_FAILED:
			break;
		case DoubanFmService.STATE_ERROR:
			
		case DoubanFmService.STATE_MUSIC_SKIPPED:
			break;
		case DoubanFmService.STATE_MUSIC_PAUSED:
			
		case DoubanFmService.STATE_MUSIC_RESUMED:
			
		default:
			break;
		}
	}
	
	private void notifyPictureStateChanged(int picState, Bitmap pic, int reason) {
		WidgetContent content = EasyDoubanFmWidget.getContent(context);
		switch (picState) {
		case DoubanFmService.STATE_ERROR:
		case DoubanFmService.STATE_CANCELLED:
		case DoubanFmService.STATE_IDLE:
			//break;
		case DoubanFmService.STATE_PREPARE:
			content.picture = defaultAlbumPic;
			EasyDoubanFmWidget.updateContent(context, content, null);
			break;
		case DoubanFmService.STATE_STARTED:
		case DoubanFmService.STATE_FINISHED:
			if (pic != null) {
				content.picture = pic;
				EasyDoubanFmWidget.updateContent(context, content, null);
			}
			break;
		default:
		}
		
		
	}

	private void notifyMusicProgress(int progress) {
		
	}
	
	private void notifyChannelChanged(int chanId, String chanName) {
		WidgetContent content = EasyDoubanFmWidget.getContent(context);
		content.channel = chanName;
		EasyDoubanFmWidget.updateContent(context, content, null);
	}
	
	private void notifyLoginStateChanged(int loginState, int reason) {
		WidgetContent content = EasyDoubanFmWidget.getContent(context);
		switch (loginState) {
		case DoubanFmService.STATE_ERROR:
		case DoubanFmService.STATE_IDLE:
			content.channel = context.getResources().getString(R.string.text_login_idle);
			break;
		case DoubanFmService.STATE_PREPARE:
			content.channel = context.getResources().getString(R.string.text_login_inprocess);
			break;
		case DoubanFmService.STATE_STARTED:
		case DoubanFmService.STATE_FINISHED:
			content.channel = context.getResources().getString(R.string.text_login_ok);
			break;
		}
		EasyDoubanFmWidget.updateContent(context, content, null);
	}
	
	private void notifyMusicRated(boolean rated) {
		WidgetContent content = EasyDoubanFmWidget.getContent(context);
		content.rated = rated;
		EasyDoubanFmWidget.updateContent(context, content, null);
	}
	
	private void notifyMusicPaused() {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(DoubanFmService.SERVICE_NOTIFICATION_ID);
	}
	
	private void notifyMusicResumed() {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (curMusic != null) {
			Notification fgNotification = new Notification(R.drawable.icon,
					curMusic.artist + " -- " + curMusic.title,
			        System.currentTimeMillis());
			fgNotification.flags |= Notification.FLAG_NO_CLEAR;
			Intent it = new Intent(DoubanFmService.ACTION_NULL);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
			fgNotification.setLatestEventInfo(context, curMusic.artist, curMusic.title, pi);	
			notificationManager.notify(DoubanFmService.SERVICE_NOTIFICATION_ID, fgNotification);
		}
	}
	
	private void notifyMusicBanned() {
		
	}
	
	private void nextMusic() {
		
		mPlayer.reset();
		
		notifyMusicStateChanged( DoubanFmService.STATE_PREPARE, null, NO_REASON);
		
		notifyMusicPrepareProgress(20);
		
		int chan = Preference.getSelectedChannel(context);
		
		synchronized(musicSessionLock) {
			lastMusic = curMusic;
			curMusic = getNextMusic();
			if (lastMusic != null) {
				musicHistory.add(0, lastMusic);
				Debugger.verbose("adding music to history");
			}
			while (musicHistory.size() > MAX_HISTORY_COUNT) {
				musicHistory.remove(musicHistory.size() - 1);
				Debugger.verbose("remove old history");
			}
			
			notifyMusicPrepareProgress(40);
			
			if (curMusic == null) {
			    Debugger.error("curMusic == null!!");
			    
				notifyMusicStateChanged( DoubanFmService.STATE_ERROR, null, 
								DoubanFmService.REASON_NETWORK_IO_ERROR);
			    return;
			} 
			
			// mPlayer prepare
			mPlayer.reset();
			try {
				mPlayer.setDataSource(curMusic.musicUrl);
			} catch (IOException e) {
				notifyMusicStateChanged(DoubanFmService.STATE_ERROR, null,
								DoubanFmService.REASON_NETWORK_IO_ERROR);
			    return;
			}
			mPlayer.prepareAsync();

			// report music info (artist, title, rated)
	    	notifyMusicStateChanged( DoubanFmService.STATE_STARTED, curMusic, NO_REASON);
	    	
			notifyMusicPrepareProgress( 60);
			
			
			// report picture ready
			
		    
		    // update appwidget view image
		    if (picTask != null) {
		    	picTask.cancel(true);
		    }
			picTask = new GetPictureTask();
		    picTask.execute(curMusic.pictureUrl);
		    
		    // secretly pre-fetch play list
		    if (pendingMusicList.size() < 1) {
		    	try {
		    		fillPendingList();
		    	} catch (IOException e) {
		    		Debugger.error("network error filling pending list: " + e.toString());
		    	}
		    }	
		}
	}
    
	private void fillPendingList() throws IOException {
		String[] historySids = null;
		if (musicHistory.size() > 0) {
			historySids = new String[musicHistory.size()];
			for (int i = 0; i < historySids.length; ++i)
				historySids[i] = musicHistory.get(i).sid;
		}
		MusicInfo[] musics = null;
		int channel = Preference.getSelectedChannel(context);
		musics = DoubanFmApi.report(loginSession, channel, 
					((curMusic != null)? curMusic.sid: ""), lastStopReason, historySids);

		if (musics == null || musics.length == 0) {
			Debugger.error("musics == 0");
			//popNotify(getResources().getString(R.string.err_get_songs));
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
	
	private MusicInfo getNextMusic() {
		if (pendingMusicList.size() == 0) {
			// pre-fetch
			try {
				fillPendingList();
			} catch (IOException e) {
				//popNotify(getResources().getString(R.string.text_network_error));
				return null;
			}
		} 
		Debugger.verbose("pending list size is " + pendingMusicList.size() + " return ");
		if (pendingMusicList.size() > 0)
			return pendingMusicList.remove(0);
		else return null;
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
        	if (params.length < 1)
        		return null;
        	
    		HttpGet httpGet = new HttpGet(params[0]);
    		httpGet.setHeader("User-Agent", 
    				String.valueOf(Utility.getSdkVersionName()));
    		httpGet.setHeader("Connection", "Keep-Alive");
    		HttpResponse httpResponse = null;
    		try {
    			httpResponse = new DefaultHttpClient().execute(httpGet);
				if (isCancelled())
					return null;
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
				if (isCancelled())
					return null;
				Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, length);
				return bmp;
			} catch (Exception e) {
				Debugger.error("Error getting picture: " + e.toString());
				return null;
			}
        	
    	}
    	@Override
    	protected void onProgressUpdate(Integer... progress) {
    		if (progress.length < 1)
    			return;
    		
        }
    	@Override
        protected void onPostExecute(Bitmap bmp) {
    		if (bmp == null) {
        		notifyPictureStateChanged(DoubanFmService.STATE_IDLE, null, NO_REASON);
    		}
     		
    		curPic = bmp;
    		
    		if (isCancelled()) {
    			return;
    		}
    		
    		notifyPictureStateChanged(DoubanFmService.STATE_STARTED, bmp, NO_REASON);
    		
        }
    }
}
