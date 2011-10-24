package com.saturdaycoder.easydoubanfm.scheduling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.DoubanFmService;

public class SchedulerStopTask extends SchedulerTask {

	Context context;
	
	public SchedulerStopTask(Context context) {
		super(SchedulerTask.TASK_STOP);
		this.context = context;
	}
	
	@Override
	public void onTicked(long remainingMillis) {
		super.onTicked(remainingMillis);
		Debugger.debug("StopTask.onTicked " + remainingMillis);
	}

	@Override
	public void onFinished() {
		Intent i = new Intent(DoubanFmService.ACTION_PLAYER_OFF);
		i.setComponent(new ComponentName(context, DoubanFmService.class));
		super.onFinished();
		Debugger.debug("StopTask.onFinished");
        context.startService(i);
	}

}
