package com.saturdaycoder.easydoubanfm;
import android.view.View;
import android.app.Activity;
import android.os.Bundle;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.os.IBinder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.*;

public class EasyDoubanFm extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        setContentView(R.layout.main);
        
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
      
}