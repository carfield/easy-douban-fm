package com.saturdaycoder.easydoubanfm.player;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.Preference;
import com.saturdaycoder.easydoubanfm.apis.DoubanFmApi;

import android.os.AsyncTask;
public class DoubanPlayListManager implements IPlayListManager {
	private int numPrefetch;
	private int numHistory;
	private int fetchThreshold = 2;
	//private User user;
	//private Cookie cookie;
	private LoginSession session = null;
	private int channel;
	
	//private MusicInfo curMusic = null;
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
	
	public DoubanPlayListManager(int numPrefetch, int numHistory) {
		this.numPrefetch = numPrefetch;
		this.numHistory = numHistory;
	}
	
	public void setLoginSession(LoginSession session) {
		this.session = session;
	}
	
	public void setChannel(int chan) {
		this.channel = chan;
	}
	/*public void reset() {
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
	
	public MusicInfo forward(char reason) {
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
	*/
	private Integer doPrefetch(char reason) {
		String cursid = "";
		
		String[] historySids = null;
		if (historyList.size() > 0) {
			
			cursid = historyList.peek().sid;
				
			if (historyList.size() > 0) {
				MusicInfo[] infos = new MusicInfo[historyList.size()];
				historySids = new String[historyList.size()];
				infos = historyList.toArray(infos);
				for (int i = 0; i < historySids.length; ++i)
					historySids[i] = infos[i].sid;
			}
		}
		MusicInfo[] musics = null;		

		
		try {
			musics = DoubanFmApi.report(session, channel, 
						cursid, reason, historySids);
		} catch (IOException e) {
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

	@Override
	public void requestList(char reason) {
		doPrefetch(reason);
	}
	
	
	
	@Override
	public void requestListAsync(char reason) {
		new AsyncPlayListFetcher().execute(reason);
	}

	@Override
	public void clearList() {
		synchronized(playList) {
			playList.clear();
		}
	}

	@Override
	public List<MusicInfo> getList() {
		// don't support
		return null;
	}

	@Override
	public MusicInfo getNext() {
		if (playList.size() < 1) {
			doPrefetch(DoubanFmApi.TYPE_NEW);
		}
		
		MusicInfo mi = playList.poll();
		if (mi == null) {
			Debugger.warn("null playlist item. back");
			return null;
		}
		
		// save history
		historyList.add(mi);
		
		// keep only the latest history
		while (historyList.size() > numHistory) {			
			historyList.poll();
		}
		
		// if not enough list items, prefetch it
		if (playList.size() < fetchThreshold) {
			requestListAsync(DoubanFmApi.TYPE_END);
		}
		return mi;
	}

	@Override
	public MusicInfo getLast() {
		// TODO Auto-generated method stub
		return null;
	}
}
