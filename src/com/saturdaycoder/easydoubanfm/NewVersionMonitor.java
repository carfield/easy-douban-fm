package com.saturdaycoder.easydoubanfm;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.enterprisedt.net.ftp.FTPClient;

public class NewVersionMonitor {
	
	private Context context;
	public NewVersionMonitor(Context context) {
		this.context = context;
	}
	
	public String getLatestVersion() {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect();
			ftp.login("saturdaycoderguest", "dontchangeit");
			ftp.chdir("/drivehqshare/saturdaycoder/EasyDoubanFm");
			String[] filenames = ftp.dir();
			Debugger.verbose("ftp files: ");
			for (String f: filenames) {
				if (f.contains("version")) {
					String ver = f.split("_")[1];
					return ver;
				}
			}
			return getInstalledVersion();
		} catch (Exception e) {
			return getInstalledVersion();
		}
	}
	
	private String getInstalledVersion() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0";
		}
	}
	
	private int getInstalledVersionCode() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getLatestVersionCode() {
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect();
			ftp.login("saturdaycoderguest", "dontchangeit");
			ftp.chdir("/drivehqshare/saturdaycoder/EasyDoubanFm");
			String[] filenames = ftp.dir();
			Debugger.verbose("ftp files: ");
			for (String f: filenames) {
				if (f.contains("code")) {
					String ver = f.split("_")[1];
					return Integer.parseInt(ver);
				}
			}
			return getInstalledVersionCode();
		} catch (Exception e) {
			return getInstalledVersionCode();
		}
	}
	
	public boolean hasNewerVersion() {
		return (getLatestVersionCode() > getInstalledVersionCode());
	}
	
	public List<Integer> getVersionCodeList() {
		//return false;
		ArrayList<Integer> fileList = new ArrayList<Integer>();
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect();
			ftp.login("saturdaycoderguest", "dontchangeit");
			ftp.chdir("/drivehqshare/saturdaycoder/EasyDoubanFm");
			String[] filenames = ftp.dir();
			Debugger.verbose("ftp files: ");
			for (String f: filenames) {
				Debugger.verbose("### " + f);
				String ver = f.split(".")[0];
				try {
					fileList.add(Integer.parseInt(ver));
				} catch (Exception e) {
					
				}
			}
			return fileList;
		} catch (Exception e) {
			return null;
		}
	}
	
	
}
