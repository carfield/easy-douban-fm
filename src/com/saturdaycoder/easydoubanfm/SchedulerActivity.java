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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
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
		Intent i = new Intent(Global.ACTION_SCHEDULER_COMMAND);
        i.setComponent(new ComponentName(SchedulerActivity.this, DoubanFmService.class));
        i.putExtra(Global.EXTRA_SCHEDULE_TYPE, type);
        i.putExtra(Global.EXTRA_SCHEDULE_TIME, time.getTime());
        startService(i);
        long remaining = time.getTime() - System.currentTimeMillis();
		long hours = remaining / (1000 * 60 * 60);
		long mins = remaining / (1000 * 60) - hours * 60;
		String popText = (type == Global.SCHEDULE_TYPE_START_PLAYER)? "开启": "关闭";
		popNotify("豆瓣电台将在" + hours + "小时" + mins + "分后自动" + popText);
	}
	
    private void popNotify(String msg)
    {
        Toast.makeText(this, msg,
                Toast.LENGTH_LONG).show();
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.schedulersetting);		
		
		
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
			long lastStopTime = Preference.getLastScheduledStopTime(this);
			Date newdate;
			if (lastStopTime == 0) {
				newdate = new Date(System.currentTimeMillis() + 30 * 60 * 1000);
			} else {
				newdate = new Date(lastStopTime);	
			}
			
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
			long lastStartTime = Preference.getLastScheduledStartTime(this);
			Date newdate;
			if (lastStartTime == 0) {
				newdate = new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000);
			} else {
				newdate = new Date(lastStartTime);
			}
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
					
					doSchedule(Global.SCHEDULE_TYPE_STOP_PLAYER, stoptime);
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
					
					doSchedule(Global.SCHEDULE_TYPE_STOP_PLAYER, stoptime);
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
					doSchedule(Global.SCHEDULE_TYPE_START_PLAYER, starttime);
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
					
					doSchedule(Global.SCHEDULE_TYPE_START_PLAYER, starttime);
				}
			}
        	
        });
        
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.options_timer_list, android.R.layout.simple_spinner_item);
        //设置下拉样式  android里面给大家提供了丰富的样式和功能图片
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //为下拉列表设置适配器
        spinnerStopTimers.setAdapter(adapter);
        spinnerStartTimers.setAdapter(adapter);
        
        spinnerStopTimers.setSelection(0);
        spinnerStartTimers.setSelection(0);
        
        OnItemSelectedListener oislStop =  new OnItemSelectedListener() {
        	long[] futuremillis = new long[] {
        			0,
            		30 * 60 * 1000,
            		60 * 60 * 1000,
            		2 * 60 * 60 * 1000,
            		8 * 60 * 60 * 1000,
            };
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  //Preference.setQuickAction(SchedulerActivity.this, 
                //		  Global.QUICKCONTROL_SHAKE, position);
            	if (position >= 1 && position < futuremillis.length) {
            		Date time = new Date(System.currentTimeMillis() + futuremillis[position]);
            		stopTimePicker.setCurrentHour(time.getHours());
        			stopTimePicker.setCurrentMinute(time.getMinutes());
            	}
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerStopTimers.setOnItemSelectedListener(oislStop);
        
        
        OnItemSelectedListener oislStart =  new OnItemSelectedListener() {
        	long[] futuremillis = new long[] {
        			0,
            		30 * 60 * 1000,
            		60 * 60 * 1000,
            		2 * 60 * 60 * 1000,
            		8 * 60 * 60 * 1000,
            };
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {
                  //Preference.setQuickAction(SchedulerActivity.this, 
                //		  Global.QUICKCONTROL_SHAKE, position);
            	if (position >= 1 && position < futuremillis.length) {
            		Date time = new Date(System.currentTimeMillis() + futuremillis[position]);
            		startTimePicker.setCurrentHour(time.getHours());
        			startTimePicker.setCurrentMinute(time.getMinutes());
            	}
            }
  
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerStartTimers.setOnItemSelectedListener(oislStart);
        
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
