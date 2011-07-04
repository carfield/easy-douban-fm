package com.saturdaycoder.easydoubanfm;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
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
	private EventListener eventListener;
	private static EasyDoubanFm _this = null;
	Button buttonChannel;
	ImageView imageCover;
	TextView textArtist;
	TextView textTitle;
	ImageButton buttonSkip;
	ImageButton buttonPlayPause;
	ImageButton buttonDownload;
	ImageButton buttonBan;
	ImageButton buttonRateUnrate;
	ImageButton buttonMenu;
	
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
			_this.imageCover.setImageResource(R.drawable.default_album);
		}
		else {
			_this.imageCover.setImageBitmap(content.picture);
		}
		// music artist
		_this.textArtist.setText(content.artist);
		// music title
		_this.textTitle.setText(content.title);
		// pause
		_this.buttonPlayPause.setImageResource(content.paused? R.drawable.btn_play: R.drawable.btn_pause);
		// rate/unrate
		_this.buttonRateUnrate.setImageResource(content.rated? R.drawable.btn_rated: R.drawable.btn_unrated);
		// on/off
		if (content.onState == EasyDoubanFmWidget.STATE_OFF) {
			_this.buttonPlayPause.setImageResource(R.drawable.btn_play);
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        setContentView(R.layout.main_activity);
        eventListener = new EventListener();
        
		buttonChannel = (Button)findViewById(R.id.buttonChannel);
		imageCover = (ImageView)findViewById(R.id.imageCover);
		textArtist = (TextView)findViewById(R.id.textArtist);
		textTitle = (TextView)findViewById(R.id.textTitle);
		buttonSkip = (ImageButton)findViewById(R.id.buttonNext);
		buttonPlayPause = (ImageButton)findViewById(R.id.buttonPlayPause);
		buttonDownload = (ImageButton)findViewById(R.id.buttonDownload);
		buttonBan = (ImageButton)findViewById(R.id.buttonHate);
		buttonRateUnrate = (ImageButton)findViewById(R.id.buttonLike);
		buttonMenu = (ImageButton)findViewById(R.id.buttonMenu);
		
		
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
				
			}
		});
		
		buttonMenu.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
			
		});
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	IntentFilter ift = new IntentFilter();
    	ift.addAction(DoubanFmService.EVENT_CHANNEL_CHANGED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_PREPARE_PROGRESS);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_POWER_STATE_CHANGED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_STATE_CHANGED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_PROGRESS);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_PICTURE_STATE_CHANGED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_RATED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_UNRATED);
    	ift.addAction(DoubanFmService.EVENT_PLAYER_MUSIC_BANNED);
    	registerReceiver(eventListener, ift);
    	
    	_this = this;
    	Intent i = new Intent(DoubanFmService.ACTION_ACTIVITY_UPDATE);
    	i.setComponent(new ComponentName(this, DoubanFmService.class));
    	startService(i);
    }
    
    @Override
    protected void onStop() {
    	//if (eventListener)
    	try {
    		unregisterReceiver(eventListener);
    	} catch (Exception e) {
    		
    	}
    	super.onStop();
    	
    	_this = null;
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    
    private class EventListener extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if (context == null || intent == null) {
    			return;
    		}
    		
    		String action = intent.getAction();
    		if (action.equals(DoubanFmService.EVENT_CHANNEL_CHANGED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_PREPARE_PROGRESS)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_POWER_STATE_CHANGED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_STATE_CHANGED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_PROGRESS)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_PICTURE_STATE_CHANGED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_RATED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_UNRATED)) {
    			
    		}
    		if (action.equals(DoubanFmService.EVENT_PLAYER_MUSIC_BANNED)) {
    			
    		}
    	}
    }
      
}