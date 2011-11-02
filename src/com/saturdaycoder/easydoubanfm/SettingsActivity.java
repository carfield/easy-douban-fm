package com.saturdaycoder.easydoubanfm;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.*;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.*;
import android.view.*;

public class SettingsActivity extends Activity {

	Button btnPlayerSetting;
	Button btnSchedulerSetting;
	Button btnAdClick;
	//Button btnThrowEgg;
	AdView adView;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		// load ad
		adView = (AdView)findViewById(R.id.adView);
		
		btnPlayerSetting = (Button)findViewById(R.id.buttonPlayerSetting);
		btnSchedulerSetting = (Button)findViewById(R.id.buttonSchedulerSetting);
		btnAdClick = (Button)findViewById(R.id.buttonAdClick);
		//btnThrowEgg = (Button)findViewById(R.id.buttonThrowEgg);
		
		boolean showad = Preference.getShowAd(this);
		if (showad) {
			//adView.loadAd(new AdRequest());
			adView.setVisibility(AdView.VISIBLE);
			btnAdClick.setText(R.string.text_hide_ad);
		} else {
			adView.setVisibility(AdView.GONE);
			btnAdClick.setText(R.string.text_show_ad);
		}
		
		btnPlayerSetting.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();

    			intent.setClass(SettingsActivity.this, PlayerSettingActivity.class);
    			startActivity(intent);
    			
			}
		});
		btnSchedulerSetting.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();

    			intent.setClass(SettingsActivity.this, SchedulerActivity.class);
    			startActivity(intent);
    			
			}
		});
		btnAdClick.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Intent intent = new Intent();

    			//intent.setClass(SettingsActivity.this, AdClickActivity.class);
    			//startActivity(intent);
				if (Preference.getShowAd(SettingsActivity.this)) {
					
					Preference.setShowAd(SettingsActivity.this, false);
					adView.setVisibility(AdView.GONE);
					btnAdClick.setText(R.string.text_show_ad);
				} else {
					Preference.setShowAd(SettingsActivity.this, true);
					
					adView.setVisibility(AdView.VISIBLE);
					//adView.loadAd(new AdRequest());
					btnAdClick.setText(R.string.text_hide_ad);
					popNotify("感谢您的支持！广告将会显示在页面上方");
				}
			}
		});
		/*btnThrowEgg.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();

    			intent.setClass(SettingsActivity.this, PlayerSettingActivity.class);
    			startActivity(intent);
    			
			}
		});*/
		
		// Look up the AdView as a resource and load a request.
	    //AdView adView = (AdView)this.findViewById(R.id.adView);
	    //adView.loadAd(new AdRequest());
	}

	protected void onResume() {
		super.onResume();
		
		if (Preference.getShowAd(this)) {
			//adView.loadAd(new AdRequest());
			adView.setVisibility(AdView.VISIBLE);
			btnAdClick.setText(R.string.text_hide_ad);
		} else {
			adView.setVisibility(AdView.GONE);
			btnAdClick.setText(R.string.text_show_ad);
		}
	}
	
    private void popNotify(String msg)
    {
        Toast.makeText(SettingsActivity.this, msg,
                Toast.LENGTH_LONG).show();
    }

}
