package com.saturdaycoder.easydoubanfm;

public interface IDoubanFmService {
	void nextMusic();
	void nextMusic(int channel);
	//void stopMusic();
	void likeMusic(boolean like);
	void banMusic();
	void closeService();
	void selectChannel(int id);
}
