package com.saturdaycoder.easydoubanfm;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
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
	Button buttonLogin;
	Button buttonLogout;
	//bool loggedIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		setContentView(R.layout.channelselector);


		listChannels = (ListView)findViewById(R.id.lvChannels);
		buttonLogin = (Button)findViewById(R.id.buttonLogin);
		buttonLogout = (Button)findViewById(R.id.buttonLogout);
		db = new Database(this);
		FmChannel[] channels = db.getChannels();
		channelList = new ArrayList<FmChannel>();

		/*mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        bindService(new Intent(ChannelSelectorActivity.this, DoubanFmService.class), 
        		mServiceConn, 0);*/
		
		if (channels != null) {
			for (int i = 0; i < channels.length; ++i) {
				channelList.add(channels[i]);
			}	
		}
		
		
		buttonLogin.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
    			intent.setClass(ChannelSelectorActivity.this, LoginActivity.class);
    			startActivityForResult(intent, 0);
    			
			}
		});
		
		buttonLogout.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				Preference.setLogin(ChannelSelectorActivity.this, false);
				Preference.setAccountPasswd(ChannelSelectorActivity.this, null);
				
				popNotify(getResources().getString(R.string.notify_logout_succ));
				
				//buttonLogin.setVisibility(Button.VISIBLE);
				//buttonLogout.setVisibility(Button.GONE);
				loadChannelList();
			}
		});

		
		listChannels.setOnItemClickListener(new OnItemClickListener() {
			
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int position, long id) 
        	{
        		if (position >= channelList.size()) {
        			Debugger.error("selected " + position 
        					+ " but channel table size is " 
        					+ channelList.size());
        			return;
        		}
        		
                
                FmChannel chan = channelList.get(position);
                if (chan == null) {
                	Debugger.error("null pointer in channelList");
                }

                Debugger.info("##### User selected channel id = " + chan.channelId);
                
                
                
                Intent i = new Intent(DoubanFmService.CONTROL_SELECT_CHANNEL);
                i.putExtra("channel", chan.channelId);
                sendBroadcast(i);
                
                ChannelSelectorActivity.this.finish();
        	}
		});
		
		
	}
	
    @Override
    protected void onResume() {
    	loadChannelList();
    	boolean loggedIn = Preference.getLogin(this);
		if (!loggedIn) {
			buttonLogin.setVisibility(Button.VISIBLE);
			buttonLogout.setVisibility(Button.GONE);
		} else {
			buttonLogin.setVisibility(Button.GONE);
			buttonLogout.setVisibility(Button.VISIBLE);
		}
		super.onResume();
    }
	
	 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Debugger.debug( "onActivityResult: " + requestCode + ", " + resultCode );
    	switch (requestCode) {
    	case 0: {
    		loadChannelList();
    	}
    	default:
    		break;
    	}
    }
	
	private void loadChannelList() {
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
		
		for (int i = 0; i < channelList.size(); ++i) {
			FmChannel chan = channelList.get(i);
			
			boolean login = Preference.getLogin(this);
			
			String chanName = chan.getDisplayName(login);//(chan.channelId == 0)? "公共频道": chan.name;
			
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
	
    private void popNotify(String msg)
    {
        Toast.makeText(ChannelSelectorActivity.this, msg,
                Toast.LENGTH_LONG).show();
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
