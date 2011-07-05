package com.saturdaycoder.easydoubanfm;
import java.util.Timer;
import java.util.TimerTask;

import android.text.format.DateFormat;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;

public class EasyDoubanFm extends Activity {
	//private WidgetContent widgetContent;

	private static EasyDoubanFm _this = null;
	Button buttonChannel;
	ImageView imageCover;
	TextView textArtist;
	TextView textTitle;
	ImageButton buttonSkip;
	ImageButton buttonPlayPause;
	TextView textButtonPlayPause;
	ImageButton buttonDownload;
	ImageButton buttonBan;
	ImageButton buttonRateUnrate;
	TextView textButtonRateUnrate;
	ImageButton buttonMenu;
	ProgressBar progressBar;
	TextView textPosition;
	
	int curPos;
	int duration;
	
	public static void setPrepareProgress(int progress) {
		Debugger.debug("EasyDoubanFm.setPrepareProgress");
		if (_this == null) {
			Debugger.debug("no activity active, skip updating");
			return;
		}
		
		_this.imageCover.setVisibility(ImageView.GONE);
		_this.progressBar.setVisibility(ProgressBar.VISIBLE);
	}
	
	public static void updatePosition(int pos, int dur) {
		Debugger.debug("EasyDoubanFm.updatePosition");
		if (_this == null) {
			Debugger.debug("no activity active, skip updating");
			return;
		}
		synchronized(EasyDoubanFm.class) {
			_this.curPos = pos;
			_this.duration = dur;
		}

		_this.textPosition.setText(DateFormat.format("mm:ss", pos).toString() 
				+ " / " + DateFormat.format("mm:ss", dur).toString());	
	}
	
	public static void updateContents(WidgetContent content) {
		Debugger.debug("EasyDoubanFm.updateContents");
		if (_this == null) {
			Debugger.debug("no activity active, skip updating");
			return;
		}
		
		// channel text
		_this.buttonChannel.setText(content.channel);
		// picture
		if (content.picture == null) {
			_this.progressBar.setVisibility(ProgressBar.GONE);
			_this.imageCover.setVisibility(ImageView.VISIBLE);
			_this.imageCover.setImageResource(R.drawable.default_album);
		}
		else {
			_this.imageCover.setVisibility(ImageView.VISIBLE);
			_this.progressBar.setVisibility(ProgressBar.GONE);
			_this.imageCover.setImageBitmap(content.picture);
		}
		// music artist
		_this.textArtist.setText(content.artist);
		// music title
		_this.textTitle.setText(content.title);
		// pause
		_this.buttonPlayPause.setImageResource(content.paused? R.drawable.btn_play: R.drawable.btn_pause);
		_this.textButtonPlayPause.setText(content.paused? _this.getResources().getString(R.string.button_name_play): _this.getResources().getString(R.string.button_name_pause));
		_this.mHandler.removeCallbacks(_this.mPositionTask);
		if (!content.paused) {
			_this.mHandler.postDelayed(_this.mPositionTask, 1000);
		}
		// rate/unrate
		_this.buttonRateUnrate.setImageResource(content.rated? R.drawable.btn_rated: R.drawable.btn_unrated);
		_this.textButtonRateUnrate.setText(content.rated? _this.getResources().getString(R.string.button_name_unrate): _this.getResources().getString(R.string.button_name_rate));
		// on/off
		if (content.onState == EasyDoubanFmWidget.STATE_OFF) {
			_this.buttonPlayPause.setImageResource(R.drawable.btn_play);
			_this.textButtonPlayPause.setText(_this.getResources().getString(R.string.button_name_play));
			_this.mHandler.removeCallbacks(_this.mPositionTask);
			
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        setContentView(R.layout.main_activity);
        
		buttonChannel = (Button)findViewById(R.id.buttonChannel);
		imageCover = (ImageView)findViewById(R.id.imageCover);
		textArtist = (TextView)findViewById(R.id.textArtist);
		textTitle = (TextView)findViewById(R.id.textTitle);
		buttonSkip = (ImageButton)findViewById(R.id.buttonNext);
		buttonPlayPause = (ImageButton)findViewById(R.id.buttonPlayPause);
		textButtonPlayPause = (TextView)findViewById(R.id.textButtonPlayPause);
		buttonDownload = (ImageButton)findViewById(R.id.buttonDownload);
		buttonBan = (ImageButton)findViewById(R.id.buttonHate);
		buttonRateUnrate = (ImageButton)findViewById(R.id.buttonLike);
		textButtonRateUnrate = (TextView)findViewById(R.id.textButtonRateUnrate);
		buttonMenu = (ImageButton)findViewById(R.id.buttonMenu);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		textPosition = (TextView)findViewById(R.id.textMusicPosition);
		
		positionTimer = new Timer();
		buttonSkip.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DoubanFmService.ACTION_PLAYER_SKIP);
				i.setComponent(new ComponentName(EasyDoubanFm.this, DoubanFmService.class));
				startService(i);
			}
			
		});
		
		buttonPlayPause.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DoubanFmService.ACTION_PLAYER_PLAYPAUSE);
				i.setComponent(new ComponentName(EasyDoubanFm.this, DoubanFmService.class));
				startService(i);
			}
			
		});
		
		buttonDownload.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_DOWNLOAD);
				i.setComponent(new ComponentName(EasyDoubanFm.this, DoubanFmService.class));
				startService(i);
			}
			
		});
		
		buttonBan.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DoubanFmService.ACTION_PLAYER_TRASH);
				i.setComponent(new ComponentName(EasyDoubanFm.this, DoubanFmService.class));
				startService(i);
			}
			
		});
		
		buttonRateUnrate.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DoubanFmService.ACTION_PLAYER_RATEUNRATE);
				i.setComponent(new ComponentName(EasyDoubanFm.this, DoubanFmService.class));
				startService(i);
			}
			
		});
		
		buttonChannel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EasyDoubanFm.this, ChannelSelectorActivity.class);
				startActivity(i);
			}
		});
		
		buttonMenu.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(EasyDoubanFm.this, PreferenceActivity.class);
				startActivity(i);
			}
			
		});
    }
    
    Timer positionTimer;
    UpdatePositionTask positionTask = new UpdatePositionTask();
    @Override
    protected void onStart() {
    	super.onStart();    	
 	
    	_this = this;
    	Intent i = new Intent(DoubanFmService.ACTION_ACTIVITY_UPDATE);
    	i.setComponent(new ComponentName(this, DoubanFmService.class));
    	startService(i);
    	
    	//positionTimer = new Timer();
    	//positionTimer.schedule(positionTask, 0, 1000);
		mHandler.removeCallbacks(mPositionTask);
        //mHandler.postDelayed(mPositionTask, 1000);
    }
    
    @Override
    protected void onStop() {

    	super.onStop();
    	mHandler.removeCallbacks(mPositionTask);
    	//positionTimer.cancel();
    	_this = null;
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
 
    Handler mHandler = new Handler();
	private Runnable mPositionTask = new Runnable() {
		   public void run() {
				synchronized(EasyDoubanFm.class) {
					curPos += 1000;
				}
				textPosition.setText(DateFormat.format("mm:ss", curPos).toString() 
						+ " / " + DateFormat.format("mm:ss", duration).toString());	
				
				mHandler.postDelayed(mPositionTask, 1000);
		   }
		};	
		
	private class UpdatePositionTask extends TimerTask {
		@Override
		public void run() {
			synchronized(EasyDoubanFm.class) {
				curPos ++;
			}
			textPosition.setText(DateFormat.format("mm:ss", curPos).toString() 
					+ " / " + DateFormat.format("mm:ss", duration).toString());	
		}
	}
	

      
}