package com.saturdaycoder.easydoubanfm.player;

public interface IPlayListObserver {
	void onPlayListChanged(int count);
	void onPlayListError(int reason);
	//void onPlayListChanged(int count);
	//void onPlayListError(int reason);
}
