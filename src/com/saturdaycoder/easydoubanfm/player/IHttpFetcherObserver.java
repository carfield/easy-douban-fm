package com.saturdaycoder.easydoubanfm.player;

public interface IHttpFetcherObserver {
	void onHttpFetchSuccess(String url);
	void onHttpFetchProgress(int progress);
	void onHttpFetchFailure(String url, int reason);
}
