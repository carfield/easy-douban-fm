package com.saturdaycoder.easydoubanfm;

public interface IDoubanFmService {
	void nextMusic();
	void nextMusic(int channel);
	//void stopMusic();
	void likeMusic(boolean like);
	void banMusic();
	void closeFM();
	void selectChannel(int id);
	boolean login(String email, String passwd);
}
