package com.saturdaycoder.easydoubanfm.player;

public interface IPlayListObserver {
	void onPlayListFetchSuccess(int count);
	void onPlayListFetchFailure(int reason);
}
