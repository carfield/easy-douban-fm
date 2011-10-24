package com.saturdaycoder.easydoubanfm.scheduling;

import java.util.ArrayList;
import java.util.Date;

import android.os.CountDownTimer;

public class SchedulerTask {
	public final static int TASK_STOP = 0;
	public final static int TASK_START = 1;
	
	protected int type;
	protected Date finishTime;
	protected long remainingMillis;
	protected CountDownTimer timer;
	protected ArrayList<ISchedulerObserver> observerList = new ArrayList<ISchedulerObserver>();
	public SchedulerTask(int type) {
		this.type = type;
	}
	
	public void registerObserver(ISchedulerObserver o) {
		if (!observerList.contains(o))
			observerList.add(o);
	}
	
	public void unregisterObserver(ISchedulerObserver o) {
		if (observerList.contains(o))
			observerList.remove(o);
	}
	
	protected void notifyOnEnabled(Date when) {
		for (ISchedulerObserver o: observerList)
			o.onTaskEnabled(type, when);
	}
	protected void notifyOnDisabled() {
		for (ISchedulerObserver o: observerList)
			o.onTaskDisabled(type);
	}
	protected void notifyOnTicked(long millisUntilFinish) {
		for (ISchedulerObserver o: observerList)
			o.onTaskTicked(type, millisUntilFinish);
	}
	protected void notifyOnFinished() {
		for (ISchedulerObserver o: observerList)
			o.onTaskFinished(type);
	}
	
	public void schedule(Date date) {
		if (timer != null) {
			timer.cancel();
		}
		
		finishTime = new Date(date.getTime());
		long millisInFuture = date.getTime() - System.currentTimeMillis();
		
		timer = new CountDownTimer(millisInFuture, 10 * 1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				onFinished();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				remainingMillis = millisUntilFinished;
				onTicked(remainingMillis);
			}
			
		};
		
		timer.start();
		
		notifyOnEnabled(finishTime);
	}
	
	public boolean isScheduled() {
		return timer != null;
	}
	
	public Date getFinishTime() {
		if (isScheduled())
			return finishTime;
		else 
			return null;
	}
	
	public long getMillisUntilFinish() {
		if (isScheduled()) {
			return remainingMillis;
		}
		else {
			return -1;
		}
		
	}
	
	public void cancel() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			
		}
		notifyOnDisabled();
	}
	
	public void onTicked(long remainingMillis) {
		notifyOnTicked(remainingMillis);
	}
	
	public void onFinished() {
		notifyOnFinished();
		timer = null;
	}
}
