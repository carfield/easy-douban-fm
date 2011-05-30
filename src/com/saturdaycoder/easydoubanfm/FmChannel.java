package com.saturdaycoder.easydoubanfm;
import android.content.res.*;
import android.content.*;
public class FmChannel {
	//public static Context context;
	private static final String publicChannelName = "公共频道";
	
	public String abbrEn;
	public String nameEn;
	public int channelId;
	public String name;
	public int seqId;
	public String getDisplayName(boolean login) {
		if (channelId == 0) {
			return login? name: publicChannelName;
		}
		else return name;
	}
}
