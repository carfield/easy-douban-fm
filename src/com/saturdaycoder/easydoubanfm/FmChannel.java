package com.saturdaycoder.easydoubanfm;
import android.content.res.*;
import android.content.*;
public class FmChannel {
	//public static Context context;
	//private static final String randomChannelName = "随机频道";
	
	public String abbrEn;
	public String nameEn;
	public int channelId;
	public String name;
	public int seqId;
	public String getDisplayName(boolean login) {
		//if (channelId == 0) {
		//	return login? name: randomChannelName;
		//}
		return name;
	}
	public static boolean channelNeedLogin(int chanId) {
		return (chanId == 0);
	}
	public static int getFirstPublicChannel() {
		for (int i = 0; i < AllChannels.length; ++i) {
			if (AllChannels[i].channelId > 0) {
				return AllChannels[i].channelId;
			}
		}
		return 0;
	}
	public static boolean isChannelIdValid(int id) {
		//boolean chanFound = false;
		
		for (int i = 0; i < AllChannels.length; ++i) {
			if (id == AllChannels[i].channelId) {
				//chanFound = true;
				//break;
				return true;
			}
		}
		return false;
	}
	
	public FmChannel(int channelId, String abbrEn, String nameEn, String name, int seqId) {
		this.channelId = channelId;
		this.seqId = seqId;
		this.name = name;
		this.nameEn = nameEn;
		this.abbrEn = abbrEn;
	}
	public static final FmChannel PrivateChannel = new FmChannel(0, "", "Personal Radio", "私人频道", 0);
	
	public static final FmChannel[] AllChannels = new FmChannel[] {
		PrivateChannel,
		new FmChannel(1, "CH", "Chinese", "华语", 1),
		new FmChannel(2, "EN", "Euro-American", "欧美", 2),
		new FmChannel(6, "HK", "Cantonese", "粤语", 3),
		new FmChannel(22, "FR", "French", "法语", 4),
		new FmChannel(17, "JPA", "Japanese", "日语", 5),
		new FmChannel(18, "KRA", "Korea", "韩语", 6),
		
		
		new FmChannel(8, "Folk", "Folk", "民谣", 7),
		new FmChannel(7, "Rock", "Rock", "摇滚", 8),
		new FmChannel(13, "Jazz", "Jazz", "爵士", 9),
		new FmChannel(27, "Cla", "Classic", "古典", 10),
		new FmChannel(9, "Easy", "Easy Listening", "轻音乐", 11),
		new FmChannel(14, "Elec", "Electronic", "电子", 12),
		new FmChannel(16, "R&B", "R&B", "R&B", 13),
		new FmChannel(15, "Rap", "Rap", "说唱", 14),
		new FmChannel(10, "Ori", "Original", "电影原声", 15),
		
		new FmChannel(3, "70", "70", "七零", 16),
		new FmChannel(4, "80", "80", "八零", 17),
		new FmChannel(5, "90", "90", "九零", 18),

		new FmChannel(26, "Ar", "Artist", "豆瓣音乐人", 19),
		
		
		new FmChannel(20, "FEM", "Female", "女声", 20),
		new FmChannel(28, "Ani", "Anime", "动漫", 21),
		new FmChannel(32, "Caf", "Cafe", "咖啡", 22),
		new FmChannel(38, "Grad", "Graduation", "毕业生", 23),
		new FmChannel(41, "Red", "Red", "红歌", 24),
		new FmChannel(36, "Samsung", "Samsung", "三星时光", 25),
		new FmChannel(34, "Lee", "Lee", "Lee", 26),
	};
}
