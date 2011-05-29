package com.saturdaycoder.easydoubanfm;

public interface IDownloadService {
	int download(String url, String filename);
	int getProgress(int sessionId);
	void cancel(int sessionId);
}
