package com.saturdaycoder.easydoubanfm.player;

public interface IHttpFetcherObserver {
	void onFetchSuccess(String url);
	void onFetchProgress(int progress);
	void onFetchFailure(String url, int reason);
}
