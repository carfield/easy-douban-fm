package com.saturdaycoder.easydoubanfm;
import java.util.ArrayList;
import java.util.HashMap;

import com.saturdaycoder.easydoubanfm.channels.FmChannel;

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
	private ListView listChannels;
	Database db;
	ArrayList<FmChannel> channelList;
	IDoubanFmService mDoubanFm;
	ServiceConnection mServiceConn;
	Button buttonLogin;
	Button buttonLogout;

	int pendingSelChanId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Debugger.verbose("ChannelSelector onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.channelselector);

		pendingSelChanId = -1;
		
		listChannels = (ListView)findViewById(R.id.lvChannels);
		buttonLogin = (Button)findViewById(R.id.buttonLogin);
		buttonLogout = (Button)findViewById(R.id.buttonLogout);
		db = new Database(this);
		FmChannel[] channels = db.getChannels();
		channelList = new ArrayList<FmChannel>();

	
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
				
				buttonLogin.setVisibility(Button.VISIBLE);
				buttonLogout.setVisibility(Button.GONE);
				try {
					loadChannelList();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
                
                // if the channel need login
                if (FmChannel.channelNeedLogin(chan.channelId)
                		&& !Preference.getLogin(ChannelSelectorActivity.this)) {
                	pendingSelChanId = chan.channelId;
                	Intent intent = new Intent();

        			intent.setClass(ChannelSelectorActivity.this, LoginActivity.class);
        			startActivityForResult(intent, 0);
                }
                else {
	                Intent i = new Intent(Global.ACTION_PLAYER_SELECT_CHANNEL);
	                i.setComponent(new ComponentName(ChannelSelectorActivity.this, DoubanFmService.class));
	                i.putExtra(Global.EXTRA_CHANNEL, chan.channelId);
	                //startService(i);
	                new AsyncServiceStarter().execute(i);
	                
	                ChannelSelectorActivity.this.finish();
                }
        	}
		});
	}
	
	private class AsyncServiceStarter extends AsyncTask<Intent, Integer, Integer> {

		@Override
		protected Integer doInBackground(Intent... params) {
			if (params.length < 1)
				return 0;
			startService(params[0]);
			return 0;
		}
		
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	boolean loggedIn = Preference.getLogin(this);
		if (!loggedIn) {
			buttonLogin.setVisibility(Button.VISIBLE);
			buttonLogout.setVisibility(Button.GONE);
		} else {
			buttonLogin.setVisibility(Button.GONE);
			buttonLogout.setVisibility(Button.VISIBLE);
		}
		
    	try {
			loadChannelList();
		} catch (Exception e) {
			e.printStackTrace();
			popNotify("读取频道列表出错，请稍后再试");
		}

		
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	try {
			Debugger.debug( "onActivityResult: " + requestCode + ", " + resultCode );
			switch (requestCode) {
			case 0: {
				if (resultCode != RESULT_OK) {
					return;
				}
				Database db = new Database(this);
				if (db.isChannelIdValid(pendingSelChanId)) {
					Intent i = new Intent(Global.ACTION_PLAYER_SELECT_CHANNEL);
			        i.putExtra(Global.EXTRA_CHANNEL, pendingSelChanId);
			        startService(i);
			        
			        ChannelSelectorActivity.this.finish();
				}
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
