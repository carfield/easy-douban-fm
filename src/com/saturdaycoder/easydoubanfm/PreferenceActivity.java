package com.saturdaycoder.easydoubanfm;
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
import android.widget.Spinner;
import android.widget.Toast;
public class PreferenceActivity extends Activity {

	CheckBox boxShake;
	Spinner spinnerShake;
	EditText editShakeThreshold;
	
	CheckBox boxMediaButton;
	Spinner spinnerMediaButton;
	
	CheckBox boxLongMediaButton;
	Spinner spinnerLongMediaButton;
	EditText editMediaButtonLongPressThreshold;

	CheckBox boxCameraButton;
	Spinner spinnerCameraButton;
	
	CheckBox boxShutdownOnIdleButton;
	EditText editShutdownOnIdle;
	
	//CheckBox boxAutoClose;
	//EditText editAutoCloseMinutes;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preference);
		
		boxShake = (CheckBox)findViewById(R.id.cbShakeEnable);
		editShakeThreshold = (EditText)findViewById(R.id.editShakeThreshold);
		boxMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonEnable);
		boxLongMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonLongEnable);
		boxCameraButton = (CheckBox)findViewById(R.id.cbCameraButtonEnable);
		boxShutdownOnIdleButton = (CheckBox)findViewById(R.id.cbShutdownOnIdleEnable);
		spinnerShake = (Spinner)findViewById(R.id.spinnerActionShake);
		spinnerMediaButton = (Spinner)findViewById(R.id.spinnerActionMediaButton);
		spinnerLongMediaButton = (Spinner)findViewById(R.id.spinnerActionMediaButtonLong);
		spinnerCameraButton = (Spinner)findViewById(R.id.spinnerActionCameraButton);
		editShutdownOnIdle = (EditText)findViewById(R.id.editShutdownIdleTime);
		editMediaButtonLongPressThreshold = (EditText)findViewById(R.id.editMediaButtonLongPressThreshold);
		//boxAutoClose = (CheckBox)findViewById(R.id.cbAutoCloseEnable);
		//editAutoCloseMinutes = (EditText)findViewById(R.id.editAutoCloseTime);
		
		// get stored value
		boolean shakeEnabled = Preference.getShakeEnable(this); 
		boxShake.setChecked(shakeEnabled);
		editShakeThreshold.setEnabled(shakeEnabled);
		spinnerShake.setEnabled(shakeEnabled);
		editShakeThreshold.setText(String.valueOf(Preference.getShakeThreshold(this)));
		
		boolean mediaButtonEnabled = Preference.getMediaButtonEnable(this);
		boxMediaButton.setChecked(mediaButtonEnabled);
		spinnerMediaButton.setEnabled(mediaButtonEnabled);
		
		boolean longMediaButtonEnabled = Preference.getMediaButtonLongEnable(this);
		boxLongMediaButton.setChecked(longMediaButtonEnabled);
		editMediaButtonLongPressThreshold.setEnabled(longMediaButtonEnabled);
		spinnerLongMediaButton.setEnabled(longMediaButtonEnabled);
		editMediaButtonLongPressThreshold.setText(String.valueOf(Preference.getMediaButtonLongPressThreshold(this)));
		
		boolean cameraButtonEnabled = Preference.getCameraButtonEnable(this);
		boxCameraButton.setChecked(cameraButtonEnabled);
		spinnerCameraButton.setEnabled(cameraButtonEnabled);
		
		boolean shutDownOnIdleEnabled = Preference.getShutdownOnIdleEnable(this);
		boxShutdownOnIdleButton.setChecked(shutDownOnIdleEnabled);
		editShutdownOnIdle.setEnabled(shutDownOnIdleEnabled);
		editShutdownOnIdle.setText(String.valueOf(Preference.getMaxIdleTime(this)));
		
		
		boxShake.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				editShakeThreshold.setEnabled(checked);
				Preference.setShakeEnable(PreferenceActivity.this, checked);
				spinnerShake.setEnabled(checked);
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
		
		editMediaButtonLongPressThreshold.addTextChangedListener(new TextWatcher() {
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
		});
		
		boxMediaButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setMediaButtonEnable(PreferenceActivity.this, checked);
				spinnerMediaButton.setEnabled(checked);
			}
		});
		
		boxLongMediaButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setMediaButtonLongEnable(PreferenceActivity.this, checked);
				spinnerLongMediaButton.setEnabled(checked);
				editMediaButtonLongPressThreshold.setEnabled(checked);
			}
		});
		
		boxCameraButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setCameraButtonEnable(PreferenceActivity.this, checked);
				spinnerCameraButton.setEnabled(checked);
			}
		});
		
		boxShutdownOnIdleButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setShutdownOnIdleEnable(PreferenceActivity.this, checked);
				editShutdownOnIdle.setEnabled(checked);
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
                  Preference.setQuickAction(PreferenceActivity.this, 
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
                  Preference.setQuickAction(PreferenceActivity.this, 
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
                  Preference.setQuickAction(PreferenceActivity.this, 
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
                  Preference.setQuickAction(PreferenceActivity.this, 
                		  Global.QUICKCONTROL_CAMERA_BUTTON, position);
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerCameraButton.setOnItemSelectedListener(oislCameraButton);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
