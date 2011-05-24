package com.saturdaycoder.easydoubanfm;
import android.view.View;
import android.os.Handler;
import android.content.BroadcastReceiver;
import android.app.Activity;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Looper;
import android.app.Service;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.impl.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.*;
import android.os.AsyncTask;
public class EasyDoubanFm extends Activity {
	private IDoubanFmService mDoubanFm;
	private ServiceConnection mServiceConn;
	//private boolean mServiceIsBound;
	//private Button buttonSwitch;
	//private ImageView imageCover;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //buttonSwitch = (Button)findViewById(R.id.buttonSwitch);
        //buttonSwitch.setBackgroundResource(R.drawable.down_button_pause);
        //buttonSwitch.setText("");
        //imageCover = (ImageView)findViewById(R.id.imageCover);
        mServiceConn = new ServiceConnection(){
        	public void onServiceConnected(ComponentName className, IBinder service) {
        		mDoubanFm = (IDoubanFmService)((DoubanFmService.LocalBinder)service).getService();
        	}
        	public void onServiceDisconnected(ComponentName className) {
        		mDoubanFm = null;
        	}
        };
        
        /*bindService(new Intent(EasyDoubanFm.this, DoubanFmService.class), 
        		mServiceConn, Context.BIND_AUTO_CREATE);
        mServiceIsBound = true;*/
        
        /*buttonSwitch.setOnClickListener(new Button.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		mDoubanFm.startMusic(0, 0);
        	}
        });*/
        
        IntentFilter filter = new IntentFilter(DoubanFmService.SESSION_STARTED);  
        if (receiver == null)  {
            receiver = new DoubanFmReceiver();  
        }
        registerReceiver(receiver, filter); 
    }
    private BroadcastReceiver receiver;
    
    @Override
    protected void onDestroy() {
    	unregisterReceiver(receiver);  
    	//receiver = null;
    	/*if (mServiceIsBound) {
    		unbindService(mServiceConn);
    		mServiceIsBound = false;
    	}*/
    	super.onDestroy();
    }
    
    public class DoubanFmReceiver extends BroadcastReceiver {  
        @Override  
        public void onReceive(Context arg0, Intent arg1) {  
            String action = arg1.getAction();  
            
            if (action.equals(DoubanFmService.SESSION_STARTED)) {
            	Debugger.error("received PICTURE_DOWNLOADED");
            	Bundle b = arg1.getExtras();
            	String s = b.getString("picurl");
                
            }  
        }  
    }  
    
}