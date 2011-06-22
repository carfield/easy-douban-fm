package com.saturdaycoder.easydoubanfm;
import android.app.*;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
public class PreferenceActivity extends Activity {
	
	CheckBox boxMediaButton;
	CheckBox boxShake;
	EditText editShakeThreshold;
	CheckBox boxCameraButton;
	CheckBox boxVolumeButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preference);
		
		boxShake = (CheckBox)findViewById(R.id.cbShakeEnable);
		editShakeThreshold = (EditText)findViewById(R.id.editShakeThreshold);
		boxMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonEnable);
		boxCameraButton = (CheckBox)findViewById(R.id.cbCameraButtonEnable);
		boxVolumeButton = (CheckBox)findViewById(R.id.cbVolumeButtonEnable);
		
		// get stored value
		boxShake.setChecked(Preference.getShakeEnable(this));
		editShakeThreshold.setText(String.valueOf(Preference.getShakeThreshold(this)));
		boxMediaButton.setChecked(Preference.getMediaButtonEnable(this));
		boxCameraButton.setChecked(Preference.getCameraButtonEnable(this));
		boxVolumeButton.setChecked(Preference.getVolumeButtonEnable(this));
		
		
		
		boxShake.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				editShakeThreshold.setEnabled(checked);
				Preference.setShakeEnable(PreferenceActivity.this, checked);
				
			}
		});
		
	
		editShakeThreshold.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void onTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void afterTextChanged(Editable editable) {
				String s = editable.toString();
				Debugger.info("SHAKE THRESHOLD set to " + s);
				try {
					int i = Integer.parseInt(s);
					if (i > 0) {
						Preference.setShakeThreshold(PreferenceActivity.this, i);
					}
				} catch (Exception e) {
					
				}
			}
		});
		
		boxMediaButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setMediaButtonEnable(PreferenceActivity.this, checked);
			}
		});
		
		boxCameraButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setCameraButtonEnable(PreferenceActivity.this, checked);
			}
		});
		
		boxVolumeButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setVolumeButtonEnable(PreferenceActivity.this, checked);
			}
		});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
