package com.saturdaycoder.easydoubanfm.player;

public interface IPlayerObserver {
	void onPosition(long pos, long total);
	void onFinished();
	void onPaused();
	void onResumed();
	void onSkipped();
	void onRated();
	void onUnrated();
	void onChannelSwitched(int chanId);
}
