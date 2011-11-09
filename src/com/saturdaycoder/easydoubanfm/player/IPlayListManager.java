package com.saturdaycoder.easydoubanfm.player;

import java.util.List;

public interface IPlayListManager {
	void registerObserver(IPlayListObserver o);
	void unregisterObserver(IPlayListObserver o);
	
	void requestList(char reason);
	
	void requestListAsync(char reason);
	
	void clearList();
	
	List<MusicInfo> getList();
	
	MusicInfo getNext();
	
	MusicInfo getLast();
}
