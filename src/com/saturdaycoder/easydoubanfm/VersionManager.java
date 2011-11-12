package com.saturdaycoder.easydoubanfm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FileTransferClient;
import com.enterprisedt.net.ftp.FileTransferInputStream;

public class VersionManager {
	FTPClient ftp;	
	
	private static final String ftphost = "ftp.drivehq.com";
	private static final String ftpuser = "saturdaycoderguest";
	private static final String ftppassword = "dontchangeit";
	private static final String ftppath = "/drivehqshare/saturdaycoder/EasyDoubanFm";
	public class VersionInfo {
		public int versionCode;
		public String versionName;
		public List<Integer> versionCodeList;
		public VersionInfo() {
			versionCodeList = new ArrayList<Integer>();
		}
		@Override
		public String toString() {
			String s = "version code=" + versionCode
					+ ", version name=" + versionName
					+ ", version list=";
			for (Integer v: versionCodeList) {
				s += String.valueOf(v) + ",";
			}
			return s;
		}
	}
	
	
	private Context context;
	public VersionManager(Context context) {
		this.context = context;
		ftp = new FTPClient();

	}
	
	public VersionInfo getServerVersionInfo() {
		
		
		
		int retrymax = 1;
		int sleeptime = 30000;
		
		int curretry = 0;
		while (curretry < retrymax) {
			try {
				Debugger.debug("start checking version...");
				ftp = new FTPClient();
				ftp.setRemoteHost(ftphost);
				if (!ftp.connected())
					ftp.connect();
				ftp.login(ftpuser, ftppassword);
				ftp.chdir(ftppath);
				String[] filenames = ftp.dir();
				Debugger.debug("ftp file list: " );
				for (String s: filenames) {
					Debugger.debug(s);
				}
				VersionInfo info = new VersionInfo();
				Debugger.verbose("ftp files: ");
				for (String f: filenames) {
					if (f.contains("versionname")) {
						info.versionName = f.split("_")[1];
					}
					if (f.contains("versioncode")) {
						info.versionCode = Integer.parseInt(f.split("_")[1]);
					}
					if (f.contains("apk")) {
						String vc = f.substring(0, f.indexOf(".apk"));
						
						info.versionCodeList.add(Integer.parseInt(vc));
					}
				}
				ftp.quitImmediately();
				return info;
			} catch (Exception e) {
				e.printStackTrace();
				
				curretry ++;
				
				try{
					ftp.quitImmediately();
					Thread.sleep(sleeptime);
				} catch (Exception ee) {
					
				}
			
				
			}
		}
		
		return null;
	}
	
	public String getInstalledVersion() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0";
		}
	}
	
	public File downloadVersion(Integer versionCode) {
		Debugger.info("start downloading from ftp version code " + versionCode);
		
		FileTransferClient ftptransfer = new FileTransferClient();
		try {
			String filename = String.valueOf(versionCode) + ".apk";
			String newfilename = "easydoubanfm-" + filename;
			File file;
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				return null;
			} else {
		        File dir = new File(android.os.Environment.getExternalStorageDirectory(), 
		                        Preference.getDownloadDirectory(context));
		        if (!dir.exists()) {
		                dir.mkdirs();
		        }
		        file = new File(Environment.getExternalStorageDirectory(), 
		                        Preference.getDownloadDirectory(context) + "/" + newfilename);
			}

			
			ftptransfer.setRemoteHost(ftphost);
			ftptransfer.setUserName(ftpuser);
			ftptransfer.setPassword(ftppassword);
			ftptransfer.setTimeout(60000);
			
			ftptransfer.connect();
			
			ftptransfer.changeDirectory(ftppath);
			
			FileTransferInputStream is = ftptransfer.downloadStream(filename);
			OutputStream os = new FileOutputStream(file);
			
			byte b[] = new byte[1024 * 100];
			try {
				while (true) {
					int tmpl = is.read(b, 0, b.length);
					if (tmpl == -1)
						break;
					
					os.write(b, 0, tmpl);
				}
				os.flush();
				os.close();
				
			} catch (Exception e) {
				Debugger.error("Error getting apkfile: " + e.toString());
				
			}
			
			is.close();
			
			os.flush();
			os.close();
			
			//ftp.quitImmediately();
			ftptransfer.disconnect(true);

			return file;
        } catch (Exception e) {
        	try {
        		ftptransfer.disconnect(true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (FTPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	e.printStackTrace();
        	return null;
        }
	}
	
	public int getInstalledVersionCode() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

}
