package com.saturdaycoder.easydoubanfm;

public interface IDoubanFmService {
	void nextMusic();
	void nextMusic(int channel);
	void pauseMusic();
	void resumeMusic();
	void rateMusic();
	void unrateMusic();
	void banMusic();
	
	void openFM();
	void closeFM();
	
	void openDownloader();
	void closeDownloader();
	
	void selectChannel(int id);
	
	void downloadMusic();
	
	boolean login(String email, String passwd);
	
	void logout();
}
