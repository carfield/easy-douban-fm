package com.saturdaycoder.easydoubanfm.player;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.Preference;
import com.saturdaycoder.easydoubanfm.apis.DoubanFmApi;

import android.os.AsyncTask;
public class PlayListManager {
	private int numPrefetch;
	private int numHistory;
	//private User user;
	//private Cookie cookie;
	private LoginSession session = null;
	private int channel;
	
	private Queue<MusicInfo> playList = new LinkedList<MusicInfo>();
	private Queue<MusicInfo> historyList = new LinkedList<MusicInfo>();
	
	private ArrayList<IPlayListObserver> observerList = new ArrayList<IPlayListObserver>();
	
	public void registerObserver(IPlayListObserver o) {
		if (!observerList.contains(o))
			observerList.add(o);
	}
	
	public void unregisterObserver(IPlayListObserver o) {
		if (observerList.contains(o))
			observerList.remove(o);
	}
	
	public PlayListManager(int numPrefetch, int numHistory) {
		this.numPrefetch = numPrefetch;
		this.numHistory = numHistory;
	}
	
	public void reset() {
		synchronized(playList) {
			playList.clear();
		}
	}
	
	public void reset(int channel) {
		synchronized(playList) {
			playList.clear();
		}
		this.channel = channel;
	}
	
	public void reset(LoginSession session, int channel) {
		this.session = session;
		reset(channel);
	}
	
	public MusicInfo pop(char reason) {
		if (playList.size() < 1) {
			prefetch(reason);
			//while (playList.size() < 1);
		}
		
		MusicInfo mi = playList.poll();
		if (mi == null) {
			Debugger.warn("null playlist item. back");
			return null;
		}
		
		// save history
		historyList.add(mi);
		
		// keep only the latest history
		if (historyList.size() > numHistory) {			
			historyList.poll();
		}
		
		// if not enough list items, prefetch it
		if (playList.size() < 2) {
			prefetchAsync(reason);
		}
		return mi;
	}
	
	public void prefetch(char reason) {
		doPrefetch(reason);
	}
	
	public void prefetchAsync(char reason) {
		new AsyncPlayListFetcher().execute(reason);
	}
	
	private Integer doPrefetch(char reason) {
		String cursid = "";
		
		String[] historySids = null;
		if (historyList.size() > 0) {
			
			cursid = historyList.poll().sid;
				
			if (historyList.size() > 0) {
				historySids = new String[historyList.size()];
				for (int i = 0; i < historySids.length; ++i)
					historySids[i] = historyList.poll().sid;
			}
		}
		MusicInfo[] musics = null;		

		
		try {
			musics = DoubanFmApi.report(session, channel, 
						cursid, reason, historySids);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debugger.error("IO error fetching new list, current size " + playList.size());
			return -1;
		}

		if (musics == null || musics.length == 0) {
			Debugger.error("musics == 0");
			return -1;
		}
		for (MusicInfo i: musics) {
			if (playList.size() >= numPrefetch) {
				break;
			} else {
				playList.add(i);
				Debugger.debug("pending list size is " + playList.size());
			}
		}
		return 0;
	}
	
	private class AsyncPlayListFetcher extends AsyncTask<Character, Integer, Integer> {

		@Override
		protected Integer doInBackground(Character... params) {
			if (params.length < 1)
				return -1;
			
			return doPrefetch(params[0]);
		}
		
	}
}
