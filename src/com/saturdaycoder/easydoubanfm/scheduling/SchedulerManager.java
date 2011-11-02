package com.saturdaycoder.easydoubanfm.scheduling;

import java.util.Date;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.Preference;
import com.saturdaycoder.easydoubanfm.notifications.DoubanFmNotificationManager;

import android.content.Context;
import android.os.CountDownTimer;

public class SchedulerManager {
	private Context context = null;
	private static SchedulerManager inst = null;
	private SchedulerTask stopTask;
	private SchedulerTask startTask;
	
	public static SchedulerManager getInstance(Context context) {
		synchronized(SchedulerManager.class) {
			if (inst == null) {
				synchronized(SchedulerManager.class) {
					if (inst == null) {
						inst = new SchedulerManager(context);
					}
				}
			}
		}
		if (inst == null) {
			Debugger.error("SchedulerManager null");
		}
		return inst;
	}
	
	private SchedulerManager(Context context) {
		this.context = context;
		stopTask = new SchedulerStopTask(context);
		startTask = new SchedulerStartTask(context);
		stopTask.registerObserver(DoubanFmNotificationManager.getInstance(context));
		startTask.registerObserver(DoubanFmNotificationManager.getInstance(context));
	}
	
	public boolean getStopTimerEnabled() {
		assert(context != null);
		return stopTask.isScheduled();
	}
	
	public boolean getStartTimerEnabled() {
		assert(context != null);
		return startTask.isScheduled();
	}
	
	public Date getScheduledStopTime() {
		assert (context != null);
		if (stopTask.isScheduled())
			return stopTask.getFinishTime();
		else 
			return null;
	}
	
	public Date getScheduledStartTime() {
		assert (context != null);
		if (startTask.isScheduled())
			return startTask.getFinishTime();
		else 
			return null;
	}
	
	
	public boolean isStopScheduled() {
		assert(context != null);
		return stopTask.isScheduled();
	}
	
	public boolean isStartScheduled() {
		assert(context != null);
		return startTask.isScheduled();
	}
	
	public void scheduleStopAt(Date stoptime) {
		assert (context != null);
		stopTask.cancel();
		stopTask.schedule(stoptime);
		Preference.setScheduledStopTime(context, stoptime.getTime());
		Preference.setLastScheduledStopTime(context, stoptime.getTime());
	}
	
	public void cancelScheduleStop() {
		assert(context != null);
		stopTask.cancel();
		Preference.setScheduledStopTime(context, 0);
	}
	
	public void scheduleStartAt(Date starttime) {
		assert (context != null);
		startTask.cancel();
		startTask.schedule(starttime);
		Preference.setScheduledStartTime(context, starttime.getTime());
		Preference.setLastScheduledStartTime(context, starttime.getTime());
	}
	
	public void cancelScheduleStart() {
		assert(context != null);
		startTask.cancel();
		Preference.setScheduledStartTime(context, 0);
	}

}
