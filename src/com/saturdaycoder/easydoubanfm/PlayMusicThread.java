package com.saturdaycoder.easydoubanfm;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.io.IOException;
public class PlayMusicThread extends Thread {
	//private String url;
	private MediaPlayer mPlayer;
	private Handler mHandler;
	private Handler mainHandler;
	
	public PlayMusicThread(Handler mainHandler) { //MediaPlayer mPlayer) {
		//this.url = url;
		this.mainHandler = mainHandler;
		
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
				reportState(PlayState.ERROR);
				Debugger.info("media player onError");
			    return true;
			}
		});
		mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
			
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				Debugger.info("media player onInfo");
				return false;
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
			}
		});
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Debugger.info("media player onCompletion");
				reportState(PlayState.COMPLETED);
			}
		});
		
		
	}
	
	
	private enum PlayCommand {
		//PREBUFFER,
		START,
		//SET_POSITION,
		PAUSE,
		RESUME,
		STOP,
		RELEASE,
		QUIT
		//RESET
	}
	
	public enum PlayState {
		//PREPARED,
		STARTED,
		STOPPED,
		PAUSED,
		//PREPARING,
		COMPLETED,
		//IDLE,
		//INITIALIZED,
		IOERROR,
		ERROR,
		//END,
	}
	
	/*public void prebufferPlay(String url) {
		
	}*/
	private void reportState(PlayState state) {
		Message msg = mainHandler.obtainMessage(); //Message.obtain();
		msg.obj = state;
		mainHandler.sendMessage(msg);
	}
	
	public void pausePlay() {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.PAUSE;
		mHandler.sendMessage(msg);
	}
	
	/*public void seekPlay(int pos) {
		
	}*/
	
	public void resumePlay() {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.RESUME;
		mHandler.sendMessage(msg);
	}
	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}
	
	public void stopPlay() {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.STOP;
		mHandler.sendMessage(msg);
	}
	
	public void startPlay(String url) {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.START;
		Bundle b = new Bundle();
		b.putString("url", url);
		msg.setData(b);
		mHandler.sendMessage(msg);
	}
	
	public void quit() {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.QUIT;
		mHandler.sendMessage(msg);
	}
	
	public void releasePlay() {
		Message msg = mHandler.obtainMessage();
		msg.obj = PlayCommand.RELEASE;
		mHandler.sendMessage(msg);
	}
	
	@Override 
	public void run() {
		Looper.prepare();
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				PlayCommand cmd = (PlayCommand)msg.obj;
				switch (cmd) {
				/*case PREBUFFER: {
					mPlayer.prepareAsync();
					break;
				}*/
				case START: {
					//if (mPlayer.isPlaying()) {
					//	mPlayer.stop();
					//}
					mAudioManager.requestAudioFocus(mAudioFocusListener, 
													AudioManager.STREAM_MUSIC,
													AudioManager.AUDIOFOCUS_GAIN);
					
					
					mPlayer.reset();
					Bundle b = msg.getData();
					String url = b.getString("url");
					try {
						mPlayer.setDataSource(url);
					} catch (IOException e) {
						Debugger.error("IO Exception when setDataSource(" + url + "): " + e.toString());
						reportState(PlayState.IOERROR);
						return;
					}
					try {
						mPlayer.prepare();
					} catch (IOException e) {
						Debugger.error("IO Exception when prepare [" + url + "): " + e.toString());
						reportState(PlayState.IOERROR);
						return;
					}
					try {
						mPlayer.seekTo(0);
						mPlayer.start();
					} catch (IllegalStateException e) {
						Debugger.error("Illegal state of mediaplayer: " + e.toString());
						reportState(PlayState.ERROR);
					}
					break;
				}
				/*case SET_POSITION: {
					break;
				}*/
				case PAUSE: {
					mPlayer.pause();
					break;
				}
				case RESUME: {
					mPlayer.start();
				}
				case STOP: {
					if (mPlayer.isPlaying()) {
						mPlayer.stop();
					}
					//mPlayer.reset();
					break;
				}
				case RELEASE: {
					//mPlayer.stop();
					//mPlayer.reset();
					mPlayer.release();
					break;
				}
				case QUIT: {
					//mPlayer.stop();
					//mPlayer.reset();
					mPlayer.release();
					Looper.myLooper().quit();
					break;
				}
				default: {
					break;
				}		
				}
			}
		};
		
		Looper.loop();
		
		
		
		/*synchronized(mPlayer) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();
			
			try {	
				mPlayer.setDataSource("");
			} catch (IOException e) {
				Debugger.error("mediaplayer setDataSource error: " + e.toString());
				return;
			} catch (Exception e) {
				Debugger.error("mediaplayer setDataSource error: " + e.toString());
				return;
			}
			try {
				mPlayer.prepare();
			} catch (Exception e) {
				Debugger.error("mediaplayer prepare error: " + e.toString());
				return;
			}
			try {
				mPlayer.seekTo(0);
			} catch (Exception e) {
				Debugger.error("mediaplayer seek error: " + e.toString());
				return;
			}
			try {
				mPlayer.start();
				Debugger.debug("PlayMusic thread finished");
				return;
			} catch (Exception e) {
				Debugger.error("mediaplayer play error: " + e.toString());
				return;
			}
		}*/
	}
}
