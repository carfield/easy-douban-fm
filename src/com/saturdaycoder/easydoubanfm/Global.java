package com.saturdaycoder.easydoubanfm;

public class Global {

	static final int SERVICE_COMMAND_DELAY = 200;
	static final String BROADCAST_PREFIX = "com.saturdaycoder.easydoubanfm";
	
	// actions for player
	public static final String ACTION_PLAYER_SKIP = BROADCAST_PREFIX + ".action.PLAYER_SKIP";
	public static final String ACTION_PLAYER_NEXT_CHANNEL = BROADCAST_PREFIX + ".action.PLAYER_NEXT_CHANNEL";
	public static final String ACTION_PLAYER_PLAYPAUSE = BROADCAST_PREFIX + ".action.PLAYER_PLAYPAUSE";
	public static final String ACTION_PLAYER_PAUSE = BROADCAST_PREFIX + ".action.PLAYER_PAUSE";
	public static final String ACTION_PLAYER_RESUME = BROADCAST_PREFIX + ".action.PLAYER_RESUME";
	public static final String ACTION_PLAYER_ON = BROADCAST_PREFIX + ".action.PLAYER_ON";
	public static final String ACTION_PLAYER_OFF = BROADCAST_PREFIX + ".action.PLAYER_OFF";
	public static final String ACTION_PLAYER_ONOFF = BROADCAST_PREFIX + ".action.PLAYER_ONOFF";
	public static final String ACTION_PLAYER_RATE = BROADCAST_PREFIX + ".action.PLAYER_RATE";
	public static final String ACTION_PLAYER_UNRATE = BROADCAST_PREFIX + ".action.PLAYER_UNRATE";
	public static final String ACTION_PLAYER_RATEUNRATE = BROADCAST_PREFIX + ".action.PLAYER_RATEUNRATE";
	public static final String ACTION_PLAYER_TRASH = BROADCAST_PREFIX + ".action.PLAYER_TRASH";
	public static final String ACTION_PLAYER_SELECT_CHANNEL = BROADCAST_PREFIX + ".action.PLAYER_SELECT_CHANNEL";
	public static final String ACTION_PLAYER_LOGIN = BROADCAST_PREFIX + ".action.PLAYER_LOGIN";
	public static final String ACTION_PLAYER_LOGOUT = BROADCAST_PREFIX + ".action.PLAYER_LOGOUT";
	
	// actions for scheduler
	public static final String ACTION_SCHEDULER_COMMAND = BROADCAST_PREFIX + ".action.SCHEDULER_COMMAND";
	
	// actions for downloader
	public static final String ACTION_DOWNLOADER_DOWNLOAD = BROADCAST_PREFIX + ".action.DOWNLOADER_DOWNLOAD";
	public static final String ACTION_DOWNLOADER_CANCEL = BROADCAST_PREFIX + ".action.DOWNLOADER_CANCEL";
	public static final String ACTION_DOWNLOADER_CLEAR_NOTIFICATION = BROADCAST_PREFIX + ".action.DOWNLOADER_CLEAR_NOTIFICATION";
	
	// extra for other, i.e. ui, etc.
	public static final String ACTION_WIDGET_UPDATE = BROADCAST_PREFIX + ".action.UPDATE_WIDGET";
	public static final String ACTION_ACTIVITY_UPDATE = BROADCAST_PREFIX + ".action.UPDATE_ACTIVITY";
	public static final String ACTION_NULL = BROADCAST_PREFIX + ".action.NULL";
	
	// extra for player
	public static final String EXTRA_MUSIC_URL = "extra.MUSIC_URL";
	public static final String EXTRA_PICTURE_URL = "extra.MUSIC_URL";
	public static final String EXTRA_LOGIN_USERNAME = "extra.LOGIN_USERNAME";
	public static final String EXTRA_LOGIN_PASSWD = "extra.LOGIN_PASSWD";
	
		
	// extra for scheduler
	public static final String EXTRA_SCHEDULE_TYPE = "extra.SCHEDULE_TYPE";
	public static final String EXTRA_SCHEDULE_TIME = "extra.SCHEDULE_TIME";
	
	//extra for downloader
	public static final String EXTRA_DOWNLOADER_DOWNLOAD_FILENAME = "extra.DOWNLOAD_FILENAME";
	
	// service status
	public static final String EVENT_PLAYER_MUSIC_PREPARE_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PREPARE_PROGRESS";
	public static final String EVENT_PLAYER_POWER_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_POWER_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_PROGRESS = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_PROGRESS";
	public static final String EVENT_PLAYER_PICTURE_STATE_CHANGED = BROADCAST_PREFIX + ".event.PLAYER_PICTURE_STATE_CHANGED";
	public static final String EVENT_DOWNLOADER_STATE_CHANGED = BROADCAST_PREFIX + ".event.DOWNLOADER_STATE_CHANGED";
	public static final String EVENT_DOWNLOADER_PROGRESS = BROADCAST_PREFIX + ".event.DOWNLOADER_PROGRESS";
	public static final String EVENT_CHANNEL_CHANGED = BROADCAST_PREFIX + ".event.CHANNEL_CHANGED";
	public static final String EVENT_LOGIN_STATE_CHANGED = BROADCAST_PREFIX + ".event.LOGIN_STATE_CHANGED";
	public static final String EVENT_PLAYER_MUSIC_RATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_RATED";
	public static final String EVENT_PLAYER_MUSIC_UNRATED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_UNRATED";
	public static final String EVENT_PLAYER_MUSIC_BANNED = BROADCAST_PREFIX + ".event.PLAYER_MUSIC_BANNED";
	
	// common extra
	public static final String EXTRA_STATE = "extra.STATE";
	public static final String EXTRA_PROGRESS = "extra.PROGRESS";
	public static final String EXTRA_DOWNLOAD_SESSION = "extra.DOWNLOAD_SESSION";
	public static final String EXTRA_MUSIC_TITLE = "extra.MUSIC_TITLE";
	public static final String EXTRA_MUSIC_ARTIST = "extra.MUSIC_ARTIST";
	public static final String EXTRA_MUSIC_ISRATED = "extra.MUSIC_ISRATED";
	public static final String EXTRA_CHANNEL = "extra.CHANNEL";
	public static final String EXTRA_REASON = "extra.REASON";
	
	// picture data
	public static final String EXTRA_PICTURE = "extra.PICTURE";
	
	// schedule types
	public static final int SCHEDULE_TYPE_STOP_PLAYER = 0;
	public static final int SCHEDULE_TYPE_START_PLAYER = 1;
	
	// player states
	public static final int STATE_PREPARE = 0;
	public static final int STATE_STARTED = 1;
	public static final int STATE_FINISHED = 2;
	public static final int STATE_FAILED = 3;
	public static final int STATE_CANCELLED = 4;
	public static final int STATE_IDLE = 5;
	public static final int STATE_ERROR = 6;
	public static final int STATE_MUSIC_SKIPPED = 7;
	public static final int STATE_MUSIC_PAUSED = 8;
	public static final int STATE_MUSIC_RESUMED = 9;
	public static final int INVALID_STATE = -1;
	
	// reasons
	public static final int REASON_NETWORK_IO_ERROR = 3;
	public static final int REASON_NETWORK_INVALID_URL = 4;
	public static final int REASON_MUSIC_FINISHED = 5;
	public static final int REASON_MUSIC_BANNED = 6;
	public static final int REASON_MUSIC_RATED = 7;
	public static final int REASON_MUSIC_UNRATED = 8;
	public static final int REASON_MUSIC_SKIPPED = 9;
	public static final int REASON_MUSIC_NEW_LIST = 10;
	public static final int REASON_MUSIC_LIST_EMPTY = 11;
	public static final int REASON_DOWNLOAD_STORAGE_INVALID = 12;
	public static final int REASON_DOWNLOAD_STORAGE_FULL = 13;
	public static final int REASON_DOWNLOAD_STORAGE_IO_ERROR = 14;
	public static final int REASON_DOWNLOAD_INVALID_FILENAME = 15;
	public static final int REASON_API_REQUEST_ERROR = 16;
	public static final int REASON_CANCELLED = 17;
	
	
	public static final int DOWNLOAD_DEFAULT_SESSIONID = 4;
	
	
	public static final String QUICKCONTROL_SHAKE = "act_shake";
	public static final String QUICKCONTROL_MEDIA_BUTTON = "act_media_button";
	public static final String QUICKCONTROL_MEDIA_BUTTON_LONG = "act_media_button_long";
	public static final String QUICKCONTROL_CAMERA_BUTTON = "act_camera_button";
	
	
	public static final int QUICKACT_NEXT_MUSIC = 0;
	public static final int QUICKACT_NEXT_CHANNEL = 1;
	public static final int QUICKACT_DOWNLOAD_MUSIC = 2;
	public static final int QUICKACT_PLAY_PAUSE = 3;
	public static final int QUICKACT_NONE = 4;
	public static final int NOTIFICATION_ID_PLAYER = 1;
	public static final int NOTIFICATION_ID_SCHEDULE_STOP = NOTIFICATION_ID_PLAYER + 1;
	public static final int NOTIFICATION_ID_SCHEDULE_START = NOTIFICATION_ID_PLAYER + 2;
	public static final int DOWNLOAD_ERROR_OK = 0;
	public static final int DOWNLOAD_ERROR_IOERROR = -1;
	public static final int DOWNLOAD_ERROR_CANCELLED = -2;
	public static final int NO_REASON = -1;
	public static final int INVALID_DOWNLOAD_ID = -1;

	
	// shake threshold
	public static final int[] shakeLevels = new int[] {
		2000,
		3000,
		5000,
		7500,
		10000,
		15000,
		20000,
	};
	
	
	
}
