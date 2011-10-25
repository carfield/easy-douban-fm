package com.saturdaycoder.easydoubanfm;

import java.util.Date;

import com.saturdaycoder.easydoubanfm.scheduling.SchedulerManager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TimePicker.OnTimeChangedListener;

public class SchedulerActivity extends Activity {

	CheckBox boxEnableStopTimer;
	Spinner spinnerStopTimers;
	TimePicker stopTimePicker;
	
	CheckBox boxEnableStartTimer;
	Spinner spinnerStartTimers;
	TimePicker startTimePicker;
	
	SchedulerManager schedManager;
	
	//IDoubanFmService mDoubanFm;
	//ServiceConnection mServiceConn;
	
	static final long[] predefinedTimerList = new long[] {
		30 * 60 * 1000,
		60 * 60 * 1000,
		2 * 60 * 60 * 1000,
		8 * 60 * 60 * 1000,
	};
	
	private void doSchedule(int type, Date time) {
		Intent i = new Intent(DoubanFmService.ACTION_SCHEDULER_COMMAND);
        i.setComponent(new ComponentName(SchedulerActivity.this, DoubanFmService.class));
        i.putExtra(DoubanFmService.EXTRA_SCHEDULE_TYPE, type);
        i.putExtra(DoubanFmService.EXTRA_SCHEDULE_TIME, time.getTime());
        startService(i);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.scheduler);		
		
		
		/*mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        bindService(new Intent(SchedulerActivity.this, DoubanFmService.class), 
        		mServiceConn, BIND_AUTO_CREATE);*/
        
		schedManager = SchedulerManager.getInstance(this);
		
		boxEnableStopTimer = (CheckBox)findViewById(R.id.cbEnableStopTimer);
		spinnerStopTimers = (Spinner)findViewById(R.id.spinnerStopTimerShortcut);
		stopTimePicker = (TimePicker)findViewById(R.id.stopTimerPicker);
		
		
		boxEnableStartTimer = (CheckBox)findViewById(R.id.cbEnableStartTimer);
		spinnerStartTimers = (Spinner)findViewById(R.id.spinnerStartTimerShortcut);
		startTimePicker = (TimePicker)findViewById(R.id.startTimerPicker);
		
		boolean stopEnabled = SchedulerManager.getInstance(this).getStopTimerEnabled();
		Date stopTime = SchedulerManager.getInstance(this).getScheduledStopTime();
		
		boxEnableStopTimer.setChecked(stopEnabled);
		if (stopEnabled && stopTime != null) {
			stopTimePicker.setCurrentHour(stopTime.getHours());
			stopTimePicker.setCurrentMinute(stopTime.getMinutes());
		} else {
			Date newdate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
			stopTimePicker.setCurrentHour(newdate.getHours());
			stopTimePicker.setCurrentMinute(newdate.getMinutes());
		}
		
		boolean startEnabled = SchedulerManager.getInstance(this).getStartTimerEnabled();
		Date startTime = SchedulerManager.getInstance(this).getScheduledStartTime();
		boxEnableStartTimer.setChecked(startEnabled);
		if (startEnabled && startTime != null) {
			startTimePicker.setCurrentHour(startTime.getHours());
			startTimePicker.setCurrentMinute(startTime.getMinutes());
		} else {
			Date newdate = new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000);
			startTimePicker.setCurrentHour(newdate.getHours());
			startTimePicker.setCurrentMinute(newdate.getMinutes());
		}
		
		boxEnableStopTimer.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				if (!checked) {
					schedManager.cancelScheduleStop();
				}
				else {
					Date stoptime = new Date();
					Integer selHour = stopTimePicker.getCurrentHour();
					Integer selMin = stopTimePicker.getCurrentMinute();
					boolean is24h = stopTimePicker.is24HourView();
					if (is24h) {
						
					}
					stoptime.setHours(selHour);
					stoptime.setMinutes(selMin);
					stoptime.setSeconds(0);
					
					Date now = new Date();
					
					Debugger.debug("stoptime hour: " + stoptime.getHours() + " min: " + stoptime.getMinutes());
					if (now.after(stoptime)) {
						stoptime.setDate(now.getDate() + 1);						
					}
					Debugger.debug("schedule stop: " + stoptime.toLocaleString());
					
					doSchedule(DoubanFmService.SCHEDULE_STOP, stoptime);
				}
			}
		});
		
		OnItemSelectedListener oislStopTimer =  new OnItemSelectedListener() {
      	  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Date now = new Date();
                  try {
                	  Date newTime = new Date(System.currentTimeMillis() + predefinedTimerList[position]);
                	  stopTimePicker.setCurrentHour(newTime.getHours());
                	  stopTimePicker.setCurrentMinute(newTime.getMinutes());
                  } catch (Exception e) {
                	  e.printStackTrace();
                  }
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	
            }
        };
        spinnerStopTimers.setOnItemSelectedListener(oislStopTimer);
        
        stopTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				boolean checked = boxEnableStopTimer.isChecked();
				if (checked) {
					Date stoptime = new Date();
					
					stoptime.setHours(stopTimePicker.getCurrentHour());
					stoptime.setMinutes(stopTimePicker.getCurrentMinute());
					
					Date now = new Date();
					
					if (now.after(stoptime)) {
						stoptime.setDate(now.getDate() + 1);
						Debugger.debug("schedule stop: " + stoptime.toLocaleString());
					}
					
					doSchedule(DoubanFmService.SCHEDULE_STOP, stoptime);
				}
			}
        	
        });
        
        
        
        boxEnableStartTimer.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked ) {
				if (!checked) {
					schedManager.cancelScheduleStart();
				}
				else {
					Date starttime = new Date();
					Integer selHour = startTimePicker.getCurrentHour();
					Integer selMin = startTimePicker.getCurrentMinute();
					boolean is24h = startTimePicker.is24HourView();
					if (is24h) {
						
					}
					starttime.setHours(selHour);
					starttime.setMinutes(selMin);
					starttime.setSeconds(0);
					
					Date now = new Date();
					
					Debugger.debug("starttime hour: " + starttime.getHours() + " min: " + starttime.getMinutes());
					if (now.after(starttime)) {
						starttime.setDate(now.getDate() + 1);						
					}
					Debugger.debug("schedule start: " + starttime.toLocaleString());
					doSchedule(DoubanFmService.SCHEDULE_START, starttime);
				}
			}
		});
		
		OnItemSelectedListener oislStartTimer =  new OnItemSelectedListener() {
      	  
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  Date now = new Date();
                  try {
                	  Date newTime = new Date(System.currentTimeMillis() + predefinedTimerList[position]);
                	  stopTimePicker.setCurrentHour(newTime.getHours());
                	  stopTimePicker.setCurrentMinute(newTime.getMinutes());
                  } catch (Exception e) {
                	  e.printStackTrace();
                  }
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            	
            }
        };
        spinnerStartTimers.setOnItemSelectedListener(oislStartTimer);
        
        startTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				boolean checked = boxEnableStartTimer.isChecked();
				if (checked) {
					Date starttime = new Date();
					
					starttime.setHours(stopTimePicker.getCurrentHour());
					starttime.setMinutes(stopTimePicker.getCurrentMinute());
					
					Date now = new Date();
					
					if (now.after(starttime)) {
						starttime.setDate(now.getDate() + 1);
						Debugger.debug("schedule stop: " + starttime.toLocaleString());
					}
					
					doSchedule(DoubanFmService.SCHEDULE_START, starttime);
				}
			}
        	
        });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
