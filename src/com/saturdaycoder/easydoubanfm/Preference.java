package com.saturdaycoder.easydoubanfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preference {


	//private static SharedPreferences pref = null;
	public static int getSelectedChannel(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("selected_channel", 0);
	}
	

	
	public static boolean selectChannel(Context context, int c) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt("selected_channel", c);
		ed.commit();
		//Debugger.info("##### saved channel = " + c);
		return true;
	}
	
	public static int getConnectTimeout(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("connect_timeout", 3000);
	}
	
	public static int getSocketTimeout(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("socket_timeout", 5000);
	}
	
	public static void saveAccount(Context context, String email, String passwd) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString("email", email);
		ed.putString("passwd", passwd);
		ed.commit();
		//Debugger.info("##### saved channel = " + c);
	}
	
	public static void setLogin(Context context, boolean login) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean("login", login);
		ed.commit();
	}
	
	public static boolean getLogin(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getBoolean("login", false);
	}
	
	public static String getAccountEmail(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getString("email", null);
	}
	public static void setAccountEmail(Context context, String email) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString("email", email);
		ed.commit();
	}
	
	public static void setAccountPasswd(Context context, String passwd) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString("passwd", passwd);
		ed.commit();
	}
	
	public static String getAccountPasswd(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getString("passwd", null);
	}
	
	public static void setShakeEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean("shake_enable", enable);
		ed.commit();
	}
	
	public static boolean getShakeEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getBoolean("shake_enable", true);
	}
	
	public static void setShakeThreshold(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt("shake_threshold", value);
		ed.commit();
	}
	
	public static int getShakeThreshold(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("shake_threshold", 5000);
	}
	
	public static void setMediaButtonEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean("media_button_enable", enable);
		ed.commit();
	}
	
	public static boolean getMediaButtonEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getBoolean("media_button_enable", true);
	}
	
	public static void setCameraButtonEnable(Context context, boolean enable) {
		
	}
	
	public static boolean getCameraButtonEnable(Context context) {
		return false;
	}
	
	public static void setVolumeButtonEnable(Context context, boolean enable) {
		
	}
	
	public static boolean getVolumeButtonEnable(Context context) {
		return false;
	}
	
	public static String getDownloadDirectory(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getString("download_dir", "/easydoubanfm");
	}
}
