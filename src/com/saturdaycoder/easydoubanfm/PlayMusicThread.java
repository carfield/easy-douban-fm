package com.saturdaycoder.easydoubanfm;
import android.media.MediaPlayer;
import java.io.IOException;
public class PlayMusicThread extends Thread {
	private String url;
	private MediaPlayer mPlayer;
	public PlayMusicThread(MediaPlayer mPlayer, String url) {
		this.url = url;
		this.mPlayer = mPlayer;
	}
	
	@Override 
	public void run() {
		
		synchronized(mPlayer) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();
			
			try {	
				mPlayer.setDataSource(url);
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
		}
	}
}
