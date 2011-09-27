package com.saturdaycoder.easydoubanfm;

public class Utility {
	public static int getSdkVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}
	
	public static String getSdkVersionName() {
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
			return "Android";
		}
	}
	
    public static String getUnixFilename(String artist, String title, String url) {
    	if (artist == null || title == null || url == null) {
    		return null;
    	}
    	String fileextension = "";
    	String tmp = url;
    	int indDot = -1;
    	while (true) {
    		int i = tmp.indexOf('.');
    		if (i == -1) {
    			break;
    		} else {
    			tmp = tmp.substring(i + 1);
    			indDot = i;
    		}
    	} 
    	if (indDot == -1)
    		fileextension = ".mp3";
    	else fileextension = "." + tmp;//.substring(indDot);
    	
    	String name = artist + "_-_" + title;
    	Debugger.debug("before transform: " + name);
    	name = name.replace(" ", "_");
    	name = name.replace("/", "-");
    	name = name.replace("\\", "-");
    	name = name.replace(":", "-");
    	name = name.replace("?", "-");
    	name = name.replace("*", "-");
    	name = name.replace("\"", "-");
    	name = name.replace("<", "-");
    	name = name.replace(">", "-");
    	name = name.replace("|", "-");
    	
    	int namelen = (name.length() + fileextension.length() > 255)? 
    			(255 - fileextension.length()): name.length();
    	name = name.substring(0, namelen);
    	Debugger.debug("after transform: " + name + fileextension);
    	return name + fileextension;
    }
    
	public static int getClientVersion() {
		return 586;
	}
}
