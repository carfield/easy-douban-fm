package com.saturdaycoder.easydoubanfm;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.*;
import android.os.*;
import android.content.*;
import android.text.format.DateFormat;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;
import android.view.Window;
import android.content.ServiceConnection;

public class ChannelSelectorActivity extends Activity {
	//private Button buttonConfirmChannel;
	//private Button buttonUpdateChannels;
	private ListView listChannels;
	Database db;
	ArrayList<FmChannel> channelList;
	IDoubanFmService mDoubanFm;
	ServiceConnection mServiceConn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.channelselector);
		
		
		
		//buttonConfirmChannel = (Button)findViewById(R.id.buttonConfirmChannel);
		//buttonUpdateChannels = (Button)findViewById(R.id.buttonUpdateChannels);
		listChannels = (ListView)findViewById(R.id.lvChannels);
		
		db = new Database(this);
		FmChannel[] channels = db.getChannels();
		channelList = new ArrayList<FmChannel>();

		mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        bindService(new Intent(ChannelSelectorActivity.this, DoubanFmService.class), 
        		mServiceConn, Context.BIND_AUTO_CREATE);
		
		if (channels != null) {
			for (int i = 0; i < channels.length; ++i) {
				channelList.add(channels[i]);
			}	
		}
		
		
		

		
		listChannels.setOnItemClickListener(new OnItemClickListener() {
			
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int position, long id) 
        	{
        		if (position >= channelList.size()) {
        			Debugger.error("selected " + position + " but channel table size is " 
        					+ channelList.size());
        			return;
        		}
        		
                
                FmChannel chan = channelList.get(position);
                if (chan == null) {
                	Debugger.error("null pointer in channelList");
                }
                if(mDoubanFm == null) {
                	Debugger.error("service not bound");
                }
                Debugger.info("##### User selected channel id = " + chan.channelId);
                mDoubanFm.selectChannel(chan.channelId);
                ChannelSelectorActivity.this.finish();
        	}
		});
		
		loadChannelList();
	}
	
	private void loadChannelList() {
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
		
		for (int i = 0; i < channelList.size(); ++i) {
			FmChannel chan = channelList.get(i);
			
			
			String chanName = (chan.channelId == 0)? "公共频道": chan.name;
			
		    HashMap<String, Object> map = new HashMap<String, Object>(); 
		    
		    map.put("channelName", chanName); 
		    listItem.add(map); 
		    
		} 
		SimpleAdapter listItemAdapter = new SimpleAdapter(ChannelSelectorActivity.this,
			listItem,
			R.layout.channelitem,
		    new String[] {
				"channelName"},  
		    
		    new int[] {
				R.id.channelName} 
		); 
		
		
		listChannels.setAdapter(listItemAdapter);  
	}
	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//if (mDoubanFm != null)
		//	unbindService(mServiceConn);
	}
}
