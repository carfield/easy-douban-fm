package com.saturdaycoder.easydoubanfm;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.*;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
public class PlayerSettingActivity extends Activity {

	CheckBox boxShake;
	Spinner spinnerShake;
	//EditText editShakeThreshold;
	TextView textShakeHint;
	SeekBar seekBarShakeThreshold;
	
	CheckBox boxMediaButton;
	Spinner spinnerMediaButton;
	
	CheckBox boxLongMediaButton;
	Spinner spinnerLongMediaButton;
	TextView textLongMediaButtonHint;
	//EditText editMediaButtonLongPressThreshold;
	SeekBar seekBarLongPressThreshold;

	CheckBox boxCameraButton;
	Spinner spinnerCameraButton;
	
	CheckBox boxShutdownOnIdleButton;
	//EditText editShutdownOnIdle;
	SeekBar seekBarIdleThreshold;
	
	//CheckBox boxAutoClose;
	//EditText editAutoCloseMinutes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playersetting);

		
		boxShake = (CheckBox)findViewById(R.id.cbShakeEnable);
		textShakeHint = (TextView)findViewById(R.id.textShakeHint);
		//editShakeThreshold = (EditText)findViewById(R.id.editShakeThreshold);
		seekBarShakeThreshold = (SeekBar)findViewById(R.id.seekBarShakeThreshold);
		boxMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonEnable);
		boxLongMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonLongEnable);
		textLongMediaButtonHint = (TextView)findViewById(R.id.textMediaButtonLongHint);
		boxCameraButton = (CheckBox)findViewById(R.id.cbCameraButtonEnable);
		boxShutdownOnIdleButton = (CheckBox)findViewById(R.id.cbShutdownOnIdleEnable);
		spinnerShake = (Spinner)findViewById(R.id.spinnerActionShake);
		spinnerMediaButton = (Spinner)findViewById(R.id.spinnerActionMediaButton);
		spinnerLongMediaButton = (Spinner)findViewById(R.id.spinnerActionMediaButtonLong);
		spinnerCameraButton = (Spinner)findViewById(R.id.spinnerActionCameraButton);
		//editShutdownOnIdle = (EditText)findViewById(R.id.editShutdownIdleTime);
		//editMediaButtonLongPressThreshold = (EditText)findViewById(R.id.editMediaButtonLongPressThreshold);
		//boxAutoClose = (CheckBox)findViewById(R.id.cbAutoCloseEnable);
		//editAutoCloseMinutes = (EditText)findViewById(R.id.editAutoCloseTime);
		seekBarLongPressThreshold = (SeekBar)findViewById(R.id.seekBarLongPressThreshold);
		seekBarIdleThreshold = (SeekBar)findViewById(R.id.seekBarShutdownIdleTime);
		// get stored value
		boolean shakeEnabled = Preference.getShakeEnable(this); 
		boxShake.setChecked(shakeEnabled);
		seekBarShakeThreshold.setEnabled(shakeEnabled);
		spinnerShake.setEnabled(shakeEnabled);
		//editShakeThreshold.setText(String.valueOf(Preference.getShakeThreshold(this)));
		int shakeaccuracy  = Global.shakeLevels[2];
		try {
			shakeaccuracy = Global.shakeLevels[Preference.getShakeThresholdLevel(this)];
		} catch (Exception e) {
			
		}
		textShakeHint.setText(getString(R.string.text_shake_accuracy) + shakeaccuracy);
		
		seekBarShakeThreshold.setMax(Global.shakeLevels.length - 1);
		seekBarShakeThreshold.setProgress(Preference.getShakeThresholdLevel(this));
		
		boolean mediaButtonEnabled = Preference.getMediaButtonEnable(this);
		boxMediaButton.setChecked(mediaButtonEnabled);
		spinnerMediaButton.setEnabled(mediaButtonEnabled);
		
		boolean longMediaButtonEnabled = Preference.getMediaButtonLongEnable(this);
		boxLongMediaButton.setChecked(longMediaButtonEnabled);
		//editMediaButtonLongPressThreshold.setEnabled(longMediaButtonEnabled);
		seekBarLongPressThreshold.setMax(Global.longPressLevels.length - 1);
		seekBarLongPressThreshold.setProgress(Preference.getLongPressThresholdLevel(this));
		spinnerLongMediaButton.setEnabled(longMediaButtonEnabled);
		double longpresst  = Global.longPressLevels[2] / 1000.0;
		try {
			longpresst = Global.longPressLevels[Preference.getLongPressThresholdLevel(this)] / 1000.0;
		} catch (Exception e) {
			
		}
		textLongMediaButtonHint.setText(getString(R.string.radio_media_button_long_1)
				+ longpresst + getString(R.string.radio_media_button_long_2));
		//editMediaButtonLongPressThreshold.setText(String.valueOf(Preference.getMediaButtonLongPressThreshold(this)));
		
		boolean cameraButtonEnabled = Preference.getCameraButtonEnable(this);
		boxCameraButton.setChecked(cameraButtonEnabled);
		spinnerCameraButton.setEnabled(cameraButtonEnabled);
		
		boolean shutDownOnIdleEnabled = Preference.getShutdownOnIdleEnable(this);
		boxShutdownOnIdleButton.setChecked(shutDownOnIdleEnabled);
		int idlemins = Global.idleLevels[3];
		try {
			idlemins = Global.idleLevels[Preference.getIdleThresholdLevel(this)];
		} catch (Exception e) {
			
		}
		boxShutdownOnIdleButton.setText(getString(R.string.radio_shutdown_on_idle_1)
				+ idlemins + getString(R.string.radio_shutdown_on_idle_2));
		//editShutdownOnIdle.setEnabled(shutDownOnIdleEnabled);
		//editShutdownOnIdle.setText(String.valueOf(Preference.getMaxIdleTime(this)));
		seekBarIdleThreshold.setMax(Global.idleLevels.length - 1);
		seekBarIdleThreshold.setProgress(Preference.getIdleThresholdLevel(this));
		
		
		boxShake.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				seekBarShakeThreshold.setEnabled(checked);
				Preference.setShakeEnable(PlayerSettingActivity.this, checked);
				spinnerShake.setEnabled(checked);
			}
		});
		
	
		/*editShakeThreshold.addTextChangedListener(new TextWatcher() {
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
		});*/
		seekBarShakeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					Debugger.debug("selected shake level " + progress);
					Preference.setShakeThresholdLevel(PlayerSettingActivity.this, progress);
					int shakeaccuracy  = Global.shakeLevels[2];
					try {
						shakeaccuracy = Global.shakeLevels[progress];
					} catch (Exception e) {
						
					}
					textShakeHint.setText(getString(R.string.text_shake_accuracy) + shakeaccuracy);
				}
			}
		});
		
		seekBarLongPressThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					Debugger.debug("selected longpress level " + progress);
					Preference.setLongPressThresholdLevel(PlayerSettingActivity.this, progress);
					double longpresst  = Global.longPressLevels[2] / 1000.0;
					try {
						longpresst = Global.longPressLevels[progress] / 1000.0;
					} catch (Exception e) {
						
					}
					textLongMediaButtonHint.setText(getString(R.string.radio_media_button_long_1)
							+ longpresst + getString(R.string.radio_media_button_long_2));
				}
			}
		});
		
		
		seekBarIdleThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					Debugger.debug("selected idle level " + progress);
					Preference.setIdleThresholdLevel(PlayerSettingActivity.this, progress);
					int idlemins = 15;
					try {
						idlemins = Global.idleLevels[progress];
					} catch (Exception e) {
						
					}
					boxShutdownOnIdleButton.setText(getString(R.string.radio_shutdown_on_idle_1)
							+ idlemins + getString(R.string.radio_shutdown_on_idle_2));
				}
			}
		});

		/*editMediaButtonLongPressThreshold.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void onTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void afterTextChanged(Editable editable) {
				String s = editable.toString();
				Debugger.info("MEDIA BUTTON LONG PRESS THRESHOLD set to " + s);
				try {
					int i = Integer.parseInt(s);
					if (i > 0) {
						Preference.setMediaButtonLongPressThreshold(PreferenceActivity.this, i);
					}
				} catch (Exception e) {
					
				}
			}
		});
		
		editShutdownOnIdle.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void onTextChanged(CharSequence str, int i, int j, int k) {
				
			}
			@Override
			public void afterTextChanged(Editable editable) {
				String s = editable.toString();
				Debugger.info("MAX IDLE TIME set to " + s);
				try {
					int i = Integer.parseInt(s);
					if (i >= 0) {
						Preference.setMaxIdleTime(PreferenceActivity.this, i);
					}
				} catch (Exception e) {
					
				}
			}
		});*/
		
		boxMediaButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setMediaButtonEnable(PlayerSettingActivity.this, checked);
				spinnerMediaButton.setEnabled(checked);
			}
		});
		
		boxLongMediaButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setMediaButtonLongEnable(PlayerSettingActivity.this, checked);
				spinnerLongMediaButton.setEnabled(checked);
				//editMediaButtonLongPressThreshold.setEnabled(checked);
				seekBarLongPressThreshold.setEnabled(checked);
			}
		});
		
		boxCameraButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setCameraButtonEnable(PlayerSettingActivity.this, checked);
				spinnerCameraButton.setEnabled(checked);
			}
		});
		
		boxShutdownOnIdleButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setShutdownOnIdleEnable(PlayerSettingActivity.this, checked);
				//editShutdownOnIdle.setEnabled(checked);
				seekBarIdleThreshold.setEnabled(checked);
			}
		});
		
		/*boxAutoClose.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setAutoCloseEnable(PreferenceActivity.this, checked);
			}
		});*/
		

        //准备一个数组适配器
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.options_act, android.R.layout.simple_spinner_item);
        //设置下拉样式  android里面给大家提供了丰富的样式和功能图片
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为下拉列表设置适配器
        spinnerShake.setAdapter(adapter);
        spinnerMediaButton.setAdapter(adapter);
        spinnerLongMediaButton.setAdapter(adapter);
        spinnerCameraButton.setAdapter(adapter);
  
        spinnerShake.setSelection(Preference.getQuickAction(this, 
        		Global.QUICKCONTROL_SHAKE), false);
        spinnerMediaButton.setSelection(Preference.getQuickAction(this, 
        		Global.QUICKCONTROL_MEDIA_BUTTON), false);
        spinnerLongMediaButton.setSelection(Preference.getQuickAction(this, 
        		Global.QUICKCONTROL_MEDIA_BUTTON_LONG), false);
        spinnerCameraButton.setSelection(Preference.getQuickAction(this, 
        		Global.QUICKCONTROL_CAMERA_BUTTON), false);
        
        OnItemSelectedListener oislShake =  new OnItemSelectedListener() {
  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Preference.setQuickAction(PlayerSettingActivity.this, 
                		  Global.QUICKCONTROL_SHAKE, position);
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerShake.setOnItemSelectedListener(oislShake);
		
        OnItemSelectedListener oislMediaButton =  new OnItemSelectedListener() {
        	  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Preference.setQuickAction(PlayerSettingActivity.this, 
                		  Global.QUICKCONTROL_MEDIA_BUTTON, position);
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerMediaButton.setOnItemSelectedListener(oislMediaButton);
        
        OnItemSelectedListener oislLongMediaButton =  new OnItemSelectedListener() {
      	  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Preference.setQuickAction(PlayerSettingActivity.this, 
                		  Global.QUICKCONTROL_MEDIA_BUTTON_LONG, position);
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerLongMediaButton.setOnItemSelectedListener(oislLongMediaButton);
        
        
        OnItemSelectedListener oislCameraButton =  new OnItemSelectedListener() {
        	  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Preference.setQuickAction(PlayerSettingActivity.this, 
                		  Global.QUICKCONTROL_CAMERA_BUTTON, position);
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerCameraButton.setOnItemSelectedListener(oislCameraButton);
        
        
        
	}
	
	@Override
	public void onResume() {
		super.onResume();

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
