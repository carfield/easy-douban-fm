package com.saturdaycoder.easydoubanfm;

public interface IDoubanFmService {
	public abstract void startMusic(int sessionid, int channel);
	public abstract void stopMusic(int sessionid);
	public abstract int getSessionId();
	public abstract void closeService();
}
