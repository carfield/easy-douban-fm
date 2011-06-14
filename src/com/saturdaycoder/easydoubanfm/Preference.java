package com.saturdaycoder.easydoubanfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preference {
	public static String getSdkVersion() {
		switch (android.os.Build.VERSION.SDK_INT) {
		case 1:
			return "Android-1.0";
		case 2:
			return "Android-1.1";
		case 3:
			return "Android-1.5";
		case 4:
			return "Android-1.6";
		case 5:
			return "Android-2.0";
		case 6:
			return "Android-2.0.1";
		case 7:
			return "Android-2.1";
		case 8:
			return "Android-2.2";
		case 9:
			return "Android-2.3";
		case 10:
			return "Android-2.3.3";
		case 11:
			return "Android-3.0";
		case 12:
			return "Android-3.1";
		default:
			return "Android-unknown";
		}
	}
	//private static SharedPreferences pref = null;
	public static int getSelectedChannel(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("selected_channel", 0);
	}
	
	public static int getClientVersion(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getInt("client_version", 583);
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
	
	public static String getDownloadDirectory(Context context) {
		SharedPreferences pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		return pref.getString("download_dir", "/easydoubanfm");
	}
}
