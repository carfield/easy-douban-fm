package com.saturdaycoder.easydoubanfm.scheduling;

import java.util.Date;

public interface ISchedulerObserver {
	
	void onTaskEnabled(int type, Date when);
	void onTaskDisabled(int type);
	void onTaskFinished(int type);
	void onTaskTicked(int type, long millisUntilFinish);
}
