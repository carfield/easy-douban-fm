package com.saturdaycoder.easydoubanfm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preference {
	private static final int VALUE_DEFAULT_AUTO_CLOSE_MINUTES = 60;
	private static final int VALUE_DEFAULT_MAX_IDLE_MINUTES = 30;
	private static final int VALUE_DEFAULT_SHAKE_THRESHOLD = 5000;
	private static final int VALUE_DEFAULT_SOCKET_TIMEOUT = 5000;
	private static final int VALUE_DEFAULT_CONNECTION_TIMEOUT = 3000;
	private static final int VALUE_DEFAULT_MEDIA_BUTTON_LONG_PRESS_THRESHOLD = 2;
	private static final int VALUE_DEFAULT_SELECTED_CHANNEL = 0;
	private static final int VALUE_INVALID_SCHEDULED_TIME = 0;
	private static final String VALUE_DEFAULT_DOWNLOAD_DIR = "/easydoubanfm";

	private static final String KEY_AUTO_CLOSE_TIME = "auto_close_time";
	private static final String KEY_MAX_IDLE_TIME = "max_idle_time";
	private static final String KEY_DOWNLOAD_DIR = "download_dir";
	private static final String KEY_SHUTDOWN_ON_IDLE_ENABLE = "shutdown_on_idle_enable";
	private static final String KEY_CAMERA_BUTTON_ENABLE = "camera_button_enable";
	private static final String KEY_MEDIA_BUTTON_ENABLE = "media_button_enable";
	private static final String KEY_MEDIA_BUTTON_LONG_ENABLE = "media_button_long_enable";
	private static final String KEY_SHAKE_THRESHOLD = "shake_threshold";
	private static final String KEY_MEDIA_BUTTON_LONG_PRESS_THRESHOLD = "media_button_long_press_threshold";
	private static final String KEY_SHAKE_ENABLE = "shake_enable";
	private static final String KEY_LOGIN_ENABLE = "login";
	private static final String KEY_ACCOUNT_PASSWD = "passwd";
	private static final String KEY_ACCOUNT_EMAIL = "email";
	private static final String KEY_SOCKET_TIMEOUT = "socket_timeout";
	private static final String KEY_CONNECT_TIMEOUT = "connect_timeout";
	private static final String KEY_SELECTED_CHANNEL = "selected_channel";
	private static final String PREF_SETTINGS_FILENAME = "settings";
	private static final String KEY_AUTO_CLOSE_ENABLE = "auto_close_enable";
	
	private static final String KEY_SCHEDULED_STOP = "scheduled_stop_at";
	private static final String KEY_SCHEDULED_START = "scheduled_start_at";

	//private static SharedPreferences pref = null;
	public static int getSelectedChannel(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_SELECTED_CHANNEL, VALUE_DEFAULT_SELECTED_CHANNEL);
	}
	

	
	public static boolean selectChannel(Context context, int c) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(KEY_SELECTED_CHANNEL, c);
		ed.commit();
		//Debugger.info("##### saved channel = " + c);
		return true;
	}
	
	public static int getConnectTimeout(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_CONNECT_TIMEOUT, VALUE_DEFAULT_CONNECTION_TIMEOUT);
	}
	
	public static int getSocketTimeout(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_SOCKET_TIMEOUT, VALUE_DEFAULT_SOCKET_TIMEOUT);
	}
	
	public static void saveAccount(Context context, String email, String passwd) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString(KEY_ACCOUNT_EMAIL, email);
		ed.putString(KEY_ACCOUNT_PASSWD, passwd);
		ed.commit();
		//Debugger.info("##### saved channel = " + c);
	}
	
	public static void setLogin(Context context, boolean login) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_LOGIN_ENABLE, login);
		ed.commit();
	}
	
	public static boolean getLogin(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_LOGIN_ENABLE, false);
	}
	
	public static String getAccountEmail(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getString(KEY_ACCOUNT_EMAIL, null);
	}
	public static void setAccountEmail(Context context, String email) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString(KEY_ACCOUNT_EMAIL, email);
		ed.commit();
	}
	
	public static void setAccountPasswd(Context context, String passwd) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putString(KEY_ACCOUNT_PASSWD, passwd);
		ed.commit();
	}
	
	public static String getAccountPasswd(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getString(KEY_ACCOUNT_PASSWD, null);
	}
	
	public static void setShakeEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_SHAKE_ENABLE, enable);
		ed.commit();
	}
	
	public static boolean getShakeEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_SHAKE_ENABLE, true);
	}
	
	public static void setShakeThreshold(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(KEY_SHAKE_THRESHOLD, value);
		ed.commit();
	}
	
	public static int getShakeThreshold(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_SHAKE_THRESHOLD, VALUE_DEFAULT_SHAKE_THRESHOLD);
	}
	
	public static void setMediaButtonLongPressThreshold(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(KEY_MEDIA_BUTTON_LONG_PRESS_THRESHOLD, value);
		ed.commit();
	}
	
	public static int getMediaButtonLongPressThreshold(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_MEDIA_BUTTON_LONG_PRESS_THRESHOLD, VALUE_DEFAULT_MEDIA_BUTTON_LONG_PRESS_THRESHOLD);
	}
	
	public static void setMediaButtonEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_MEDIA_BUTTON_ENABLE, enable);
		ed.commit();
	}
	
	public static boolean getMediaButtonEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_MEDIA_BUTTON_ENABLE, true);
	}
	
	public static void setMediaButtonLongEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_MEDIA_BUTTON_LONG_ENABLE, enable);
		ed.commit();
	}
	
	public static boolean getMediaButtonLongEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_MEDIA_BUTTON_LONG_ENABLE, true);
	}
	
	public static void setCameraButtonEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_CAMERA_BUTTON_ENABLE, enable);
		ed.commit();
	}
	
	public static boolean getCameraButtonEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_CAMERA_BUTTON_ENABLE, true);
	}
	
	public static void setShutdownOnIdleEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_SHUTDOWN_ON_IDLE_ENABLE, enable);
		ed.commit();
	}
	
	public static boolean getAutoCloseEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_AUTO_CLOSE_ENABLE, false);
	}
	
	public static void setAutoCloseEnable(Context context, boolean enable) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putBoolean(KEY_AUTO_CLOSE_ENABLE, enable);
		ed.commit();
	}
	
	
	public static boolean getShutdownOnIdleEnable(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getBoolean(KEY_SHUTDOWN_ON_IDLE_ENABLE, true);
	}
	
	public static String getDownloadDirectory(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getString(KEY_DOWNLOAD_DIR, VALUE_DEFAULT_DOWNLOAD_DIR);
	}
	
	public static int getQuickAction(Context context, String control) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		
		int res = Global.QUICKACT_NONE;
		if (control.equals(Global.QUICKCONTROL_SHAKE))
			res = pref.getInt(control, Global.QUICKACT_NEXT_MUSIC);
		else if (control.equals(Global.QUICKCONTROL_MEDIA_BUTTON))
			res = pref.getInt(control, Global.QUICKACT_PLAY_PAUSE);
		else if (control.equals(Global.QUICKCONTROL_MEDIA_BUTTON_LONG))
			res = pref.getInt(control, Global.QUICKACT_NEXT_CHANNEL);
		else if (control.equals(Global.QUICKCONTROL_CAMERA_BUTTON))
			res = pref.getInt(control, Global.QUICKACT_DOWNLOAD_MUSIC);
		else
			res = pref.getInt(control, Global.QUICKACT_NONE);
		
		if (res < 0 || res > Global.QUICKACT_NONE)
			res = Global.QUICKACT_NONE;
		
		return res;
	}
	
	public static void setQuickAction(Context context, String control, int action) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(control, action);
		ed.commit();
	}
	
	public static int getMaxIdleTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_MAX_IDLE_TIME, VALUE_DEFAULT_MAX_IDLE_MINUTES);
	}
	
	public static void setMaxIdleTime(Context context, int minutes) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(KEY_MAX_IDLE_TIME, minutes);
		ed.commit();
	}
	
	public static int getAutoCloseTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_AUTO_CLOSE_TIME, VALUE_DEFAULT_AUTO_CLOSE_MINUTES);
		
	}
	
	public static void setAutoCloseTime(Context context, int minutes) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt(KEY_AUTO_CLOSE_TIME, minutes);
		ed.commit();
	}
	
	public static long getScheduledStopTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getLong(KEY_SCHEDULED_STOP, VALUE_INVALID_SCHEDULED_TIME);
		
	}
	
	public static void setScheduledStopTime(Context context, long millis) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putLong(KEY_SCHEDULED_STOP, millis);
		ed.commit();
	}
	
	public static long getScheduledStartTime(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		return pref.getLong(KEY_SCHEDULED_START, VALUE_INVALID_SCHEDULED_TIME);
		
	}
	
	public static void setScheduledStartTime(Context context, long millis) {
		SharedPreferences pref = context.getSharedPreferences(PREF_SETTINGS_FILENAME, Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putLong(KEY_SCHEDULED_START, millis);
		ed.commit();
	}
}
