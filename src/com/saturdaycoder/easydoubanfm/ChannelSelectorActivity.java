package com.saturdaycoder.easydoubanfm;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.Button;
import android.widget.ListView;
public class ChannelSelectorActivity extends Activity {
	private Button buttonConfirmChannel;
	private Button buttonUpdateChannels;
	private ListView listChannels;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channelselector);
		
		buttonConfirmChannel = (Button)findViewById(R.id.buttonConfirmChannel);
		buttonUpdateChannels = (Button)findViewById(R.id.buttonUpdateChannels);
		listChannels = (ListView)findViewById(R.id.lvChannels);
		
		DoubanFmDatabase db = new DoubanFmDatabase(this);
		loadChannelList();
	}
	
	private void loadChannelList() {
		
	}
	@Override
	protected void onStart() {
		
	}
	
	@Override
	protected void onResume() {
		
	}
	
	@Override
	protected void onPause() {
		
	}
	
	@Override
	protected void onStop() {
		
	}
	
	@Override
	protected void onDestroy() {
		
	}
}
