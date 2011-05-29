package com.saturdaycoder.easydoubanfm;

public class FmChannel {
	public String abbrEn;
	public String nameEn;
	public int channelId;
	public String name;
	public int seqId;
	public String getDisplayName(int id, boolean login) {
		if (id == 0) {
			return login? name: "公共频道";
		}
		else return name;
	}
}
