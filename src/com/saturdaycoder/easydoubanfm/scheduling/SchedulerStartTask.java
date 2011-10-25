package com.saturdaycoder.easydoubanfm.scheduling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.DoubanFmService;
import com.saturdaycoder.easydoubanfm.Global;

public class SchedulerStartTask extends SchedulerTask {
	Context context;
	
	public SchedulerStartTask(Context context) {
		super(Global.SCHEDULE_TYPE_START_PLAYER);
		this.context = context;
	}
	
	@Override
	public void onTicked(long remainingMillis) {
		super.onTicked(remainingMillis);
		Debugger.debug("StartTask.onTicked " + remainingMillis);
	}

	@Override
	public void onFinished() {
		Intent i = new Intent(Global.ACTION_PLAYER_ON);
		i.setComponent(new ComponentName(context, DoubanFmService.class));
		super.onFinished();
		Debugger.debug("StartTask.onFinished");
        context.startService(i);
	}
}
