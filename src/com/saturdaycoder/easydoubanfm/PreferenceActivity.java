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
	
	CheckBox boxMediaButton;
	CheckBox boxShake;
	EditText editShakeThreshold;
	CheckBox boxCameraButton;
	CheckBox boxShutdownOnIdleButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preference);
		
		boxShake = (CheckBox)findViewById(R.id.cbShakeEnable);
		editShakeThreshold = (EditText)findViewById(R.id.editShakeThreshold);
		boxMediaButton = (CheckBox)findViewById(R.id.cbMediaButtonEnable);
		boxCameraButton = (CheckBox)findViewById(R.id.cbCameraButtonEnable);
		boxShutdownOnIdleButton = (CheckBox)findViewById(R.id.cbShutdownOnIdleEnable);
		
		// get stored value
		boxShake.setChecked(Preference.getShakeEnable(this));
		editShakeThreshold.setText(String.valueOf(Preference.getShakeThreshold(this)));
		boxMediaButton.setChecked(Preference.getMediaButtonEnable(this));
		boxCameraButton.setChecked(Preference.getCameraButtonEnable(this));
		boxShutdownOnIdleButton.setChecked(Preference.getVolumeButtonEnable(this));
		
		
		
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
		
		boxShutdownOnIdleButton.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				Preference.setVolumeButtonEnable(PreferenceActivity.this, checked);
			}
		});
		
		Spinner spinnerSwipe = (Spinner)findViewById(R.id.spinnerActionSwipe);
        //准备一个数组适配器
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.options_act, android.R.layout.simple_spinner_item);
        //设置下拉样式  android里面给大家提供了丰富的样式和功能图片
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为下拉列表设置适配器
        spinnerSwipe.setAdapter(adapter);
  
        //定义子元素选择监听器
        OnItemSelectedListener oislSwipe =  new OnItemSelectedListener() {
  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Toast.makeText(PreferenceActivity.this,"选择的血型： " +
                  parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
  
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        //为下拉列表绑定事件监听器
        spinnerSwipe.setOnItemSelectedListener(oislSwipe);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
