package com.saturdaycoder.easydoubanfm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

public class DoubanFmDownloader {
	private boolean isOpen = false;
	private Context context = null;
	private Database db = null;
	private HashMap<Integer, DownloadTask> downloadMap = new HashMap<Integer, DownloadTask>();
	private HashMap<Integer, Notification> notificationMap = new HashMap<Integer, Notification>();
	//private HashMap<Integer, Integer> progressMap = new HashMap<Integer, Integer>();
	
	private NotificationManager notManager = null;
	
	private static final int DOWNLOAD_ERROR_OK = 0;
	private static final int DOWNLOAD_ERROR_IOERROR = -1;
	private static final int DOWNLOAD_ERROR_CANCELLED = -2;
	
	private static final int NO_REASON = -1;
	private static final int INVALID_DOWNLOAD_ID = -1;
	
	public DoubanFmDownloader(Context context) {
		this.context = context;
		db = new Database(context);
		notManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void open() {
		synchronized(this) {
			if (!isOpen) {
				isOpen = true;
			}
		}
	}
	
	public void close() {
		synchronized(this) {
			if (isOpen) {
				isOpen = false;
			}
		}
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public int download(String url, String filename) {
		Debugger.verbose("Downloader.download url=\"" + url + "\"");
		if (filename == null || filename.equals("") || url == null || url.equals("")) {
			return INVALID_DOWNLOAD_ID;
		}
		
		int id = db.addDownload(url, filename);
		if (id != INVALID_DOWNLOAD_ID) {
			DownloadTask task = new DownloadTask();
			downloadMap.put(id, task);
			task.execute(url, filename);
			
			notifyDownloadStateChanged(DoubanFmService.STATE_STARTED,  
					filename, url, NO_REASON);
		}
		return id;
	}
	
	public void cancel(String url) {
		
		int id = db.getDownloadIdByUrl(url);
		
		if (id == INVALID_DOWNLOAD_ID) {
			Debugger.debug("can not get invalid download id by url: " + url);
			return;
		}
		
		Debugger.debug("cancel download of url " + url);
		DownloadTask task = downloadMap.get(id);
		if (task != null) {
			task.cancel(true);
		}
	}
	
	public void cancel(int id) {
		Debugger.debug("cancel download of id " + id);
		String url = db.getDownloadUrlById(id);
		if (url == null) {
			return;
		}
		DownloadTask task = downloadMap.get(id);
		if (task != null) {
			task.cancel(true);
		}
	}
	
	public void clearNotification(String url) {
		
	}
	
	public void clearNotification(int id) {
		
	}
	private File getDownloadFile(String filename) {
        
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return null;
        }
        
        File dir = new File(android.os.Environment.getExternalStorageDirectory(), 
                        Preference.getDownloadDirectory(context));
        if (!dir.exists()) {
                dir.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory(), 
                        Preference.getDownloadDirectory(context) + "/" + filename);
        return file;
	}

	private void notifyDownloadStateChanged(int downloadState, //String artist, String title,
											String filename, String url, int reason) {
		int id = db.getDownloadIdByUrl(url);
		
		if (id == INVALID_DOWNLOAD_ID)
			return;
		
		switch(downloadState) {
		case DoubanFmService.STATE_STARTED: {
			Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_CANCEL);
			if (filename == null) {
				filename = db.getFilenameByUrl(url);
			}
			i.putExtra(DoubanFmService.EXTRA_DOWNLOADER_DOWNLOAD_FILENAME, filename);
			i.putExtra(DoubanFmService.EXTRA_MUSIC_URL, url);
			ComponentName cn = new ComponentName(context, DoubanFmService.class);
			i.setComponent(cn);
			
			PendingIntent pi = PendingIntent.getService(context, 
					0, i, 0);

			Notification notification = new Notification(android.R.drawable.stat_sys_download, 
					context.getResources().getString(R.string.text_downloading),
	                System.currentTimeMillis());
			
			notificationMap.put(id, notification);
			
			notification.contentView = new android.widget.RemoteViews(context.getPackageName(),
					R.layout.download_notification_1); 
			notification.contentView.setTextViewText(R.id.textDownloadFilename, filename);
			notification.contentView.setTextViewText(R.id.textDownloadSize, 
					context.getResources().getString(R.string.text_download_cancel));
			notification.contentView.setProgressBar(R.id.progressDownloadNotification, 100,0, false);

			notification.contentIntent = pi;      

			notManager.notify(id, notification);
			break;
		}
		case DoubanFmService.STATE_CANCELLED: {
			Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_DOWNLOAD);
			if (filename == null) {
				filename = db.getFilenameByUrl(url);
			}
			i.putExtra(DoubanFmService.EXTRA_DOWNLOADER_DOWNLOAD_FILENAME, filename);
			i.putExtra(DoubanFmService.EXTRA_MUSIC_URL, url);

			PendingIntent pi = PendingIntent.getService(context, 
					0, i, 0);

			Notification notification = new Notification(android.R.drawable.stat_notify_error, 
					context.getResources().getString(R.string.text_download_cancelled),
	                System.currentTimeMillis());
	        notification.setLatestEventInfo(context, 
	        		context.getResources().getString(R.string.text_download_cancelled_long), 
	        		filename, pi);
	        notManager.notify(id, notification);
	        
	        break;	
		}
		case DoubanFmService.STATE_FINISHED: {
			Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_DOWNLOAD);
			if (filename == null) {
				filename = db.getFilenameByUrl(url);
			}
			i.putExtra(DoubanFmService.EXTRA_DOWNLOADER_DOWNLOAD_FILENAME, filename);
			i.putExtra(DoubanFmService.EXTRA_MUSIC_URL, url);

			PendingIntent pi = PendingIntent.getService(context, 
					0, i, 0);

			Notification notification = new Notification(R.drawable.stat_sys_install_complete, 
					context.getResources().getString(R.string.text_download_ok),
	                System.currentTimeMillis());
	        notification.setLatestEventInfo(context, 
	        		context.getResources().getString(R.string.text_download_ok_long), 
	        		filename, pi);
	        notManager.notify(id, notification);
	        
	        break;			
		}
		case DoubanFmService.STATE_ERROR: {
			Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_DOWNLOAD);
			if (filename == null) {
				filename = db.getFilenameByUrl(url);
			}
			i.putExtra(DoubanFmService.EXTRA_DOWNLOADER_DOWNLOAD_FILENAME, filename);
			i.putExtra(DoubanFmService.EXTRA_MUSIC_URL, url);

			PendingIntent pi = PendingIntent.getService(context, 
					0, i, 0);

			Notification notification = new Notification(android.R.drawable.stat_notify_error, 
					context.getResources().getString(R.string.text_download_fail),
	                System.currentTimeMillis());
	        notification.setLatestEventInfo(context, 
	        		context.getResources().getString(R.string.text_download_fail_long), 
	        		filename, pi);
	        notManager.notify(id, notification);
	        
	        break;				
		}
		default:
			break;
		}
	}
	
	private void notifyDownloadProgress(String url, int progress, String detail) {
		Intent i = new Intent(DoubanFmService.ACTION_DOWNLOADER_CANCEL);
		i.putExtra(DoubanFmService.EXTRA_MUSIC_URL, url);
		ComponentName cn = new ComponentName(context, DoubanFmService.class);
		i.setComponent(cn);
		
		PendingIntent pi = PendingIntent.getService(context, 
				0, i, 0);

		int id = db.getDownloadIdByUrl(url);
		if (id == -1)
			return;
		
		Notification n = notificationMap.get(id);
		if (n == null) {
			return;
		}
		
		n.contentView.setProgressBar(R.id.progressDownloadNotification, 
				100, progress, false);
		//String text = String.valueOf(progress) + "%";
		//if (totalBytes != -1 && downloadedBytes != -1)
		//	text += " (" + downloadedBytes/1024 + "K/" + totalBytes/1024 + "K)";
		n.contentView.setTextViewText(R.id.textDownloadSize, detail);
				//context.getResources().getString(R.string.text_download_cancel)+ detail);
		
		//Intent i = new Intent(ACTION_DOWNLOADER_CANCEL);
		//i.putExtra(EXTRA_DOWNLOADER_SESSION_ID, sessionId);
		//PendingIntent pi = PendingIntent.getBroadcast(DoubanFmService.this, 
		//		0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		n.contentIntent = pi;
		
		notManager.notify(id, n);
		
		
	}
	
	private void notifyDownloadProgress(int id, int progress, String detail) {
		String url = db.getDownloadUrlById(id);
		notifyDownloadProgress(url, progress, detail);
	}
	
	private static final int DOWNLOAD_BUFFER = 102400;
	private class DownloadTask extends AsyncTask<String, Integer, Integer> {
		//private int sessionId;
		private int lastProgress;
		//private int progress;
		private String url;
		private String filename;
		private long totalBytes = -1;
		private long downloadedBytes = -1;
		
		public DownloadTask() {
			//this.sessionId = sessionId;
			//progress = -1;
			lastProgress = 0;
		}
		@Override
    	protected void onProgressUpdate(Integer... progress) {
			if (progress.length < 1)
				return;
			
			int prog = progress[0];
			Debugger.verbose("Download progress: " + prog);
			
			if (isCancelled()) {
				return;
			}
			
			if (prog == 0 || prog == 100
					|| prog - lastProgress > 4) {
				String detail = context.getResources().getString(R.string.text_download_cancel);
				if (totalBytes != -1 && downloadedBytes != -1)
					detail += " (" + downloadedBytes/1024 + "K/" + totalBytes/1024 + "K)";
				
				notifyDownloadProgress(url, prog, detail);
				lastProgress = prog;
			}
		}
		
		@Override
        protected void onPostExecute(Integer result) {
			int id = db.getDownloadIdByUrl(url);
			File musicfile = null;
			musicfile = getDownloadFile(filename);
			
			switch (result) {
			case DOWNLOAD_ERROR_OK:
				Debugger.info("Download finish");
				
				notifyDownloadStateChanged(DoubanFmService.STATE_FINISHED, filename, url, NO_REASON);
				
				
				if (musicfile != null) {
					SingleMediaScanner scanner = new SingleMediaScanner(context, musicfile);
				}
				
				break;
			case DOWNLOAD_ERROR_IOERROR:
				Debugger.info("Download error");
				
				notifyDownloadStateChanged(DoubanFmService.STATE_ERROR, filename, url, NO_REASON);

				if (musicfile.exists()) {
					musicfile.delete();
				}
				break;
			case DOWNLOAD_ERROR_CANCELLED:
				Debugger.info("Download cancelled");
				
				notifyDownloadStateChanged(DoubanFmService.STATE_CANCELLED, filename, url, NO_REASON);

				if (musicfile.exists()) {
					musicfile.delete();
				}
				
				break;
			default:
				break;
			}
			downloadMap.remove(id);
			Debugger.info("remaining task " + downloadMap.size());

			if (downloadMap.size() == 0) {
				close();
			}
		}
		
		@Override
    	protected void onCancelled() {
			File f = getDownloadFile(filename);
			if (f.exists()) {
				f.delete();
			}
			int id = db.getDownloadIdByUrl(url);
			downloadMap.remove(id);
			Debugger.info("remaining task " + downloadMap.size());
			notifyDownloadStateChanged(DoubanFmService.STATE_CANCELLED, filename, url, NO_REASON);

			if (downloadMap.size() == 0) {
				close();
			}
		}

		@Override
    	protected Integer doInBackground(String... params) {
			// param 0: url of music
			// param 1: download filename
			
			if (params.length < 2) {
				Debugger.error("Download task requires more arguments than " 
						+ params.length);
				return DOWNLOAD_ERROR_CANCELLED;
			}
			this.url = params[0];
			this.filename = params[1];
			Debugger.info("url = " + this.url + ", filename = " + this.filename); 
			
			// Step 1. get bytes
			HttpGet httpGet = new HttpGet(url);
    		httpGet.setHeader("User-Agent", 
    				String.valueOf(Utility.getSdkVersionName()));

    		HttpResponse httpResponse = null;
    		
    		HttpParams hp = new BasicHttpParams();
    		int timeoutConnection = 10000;
    		HttpConnectionParams.setConnectionTimeout(hp, timeoutConnection);
    		int timeoutSocket = 30000;
    		HttpConnectionParams.setSoTimeout(hp, timeoutSocket);
    		
    		try {
    			httpResponse = new DefaultHttpClient(hp).execute(httpGet);
     		} catch (Exception e) {
    			Debugger.error("Error getting response of downloading music: " + e.toString());
    			return DOWNLOAD_ERROR_IOERROR;
    		}
    		
    		Debugger.info("received response");
    		int statuscode = httpResponse.getStatusLine().getStatusCode();
			if (statuscode != 200) {
				Debugger.error("Error getting response of downloading music: status " + statuscode);
    			return statuscode;
			}
			
			
			
			if (isCancelled()) {
				return DOWNLOAD_ERROR_CANCELLED;
			}

			// step 2. create file
			OutputStream os = null;
			try {
				File musicfile = null;
				musicfile = getDownloadFile(filename);
				
				if (musicfile == null) {
					Debugger.error("can not get download file");
					return DOWNLOAD_ERROR_IOERROR;
				}
				Debugger.info("got download file, start writing");
				os = new FileOutputStream(musicfile);
			} catch (Exception e) {
				Debugger.error("Error writing file to external storage: " + e.toString());
				return DOWNLOAD_ERROR_IOERROR;
			}
			
			// step 3. write into file after each read
			byte b[] = new byte[DOWNLOAD_BUFFER];
			try {
				InputStream is = httpResponse.getEntity().getContent();
				totalBytes = httpResponse.getEntity().getContentLength();
				downloadedBytes = 0;
				while (downloadedBytes < totalBytes) {
					if (isCancelled())
						return DOWNLOAD_ERROR_CANCELLED;
					int tmpl = is.read(b, 0, DOWNLOAD_BUFFER);
					if (tmpl == -1)
						break;
					
					Debugger.verbose("writing file " + tmpl + ", " + downloadedBytes + "/" + totalBytes);
					os.write(b, 0, tmpl);
					downloadedBytes += tmpl;
					
					double prog = ((double)downloadedBytes / totalBytes * 100);
					publishProgress((int)prog);
					
				}
				os.flush();
				os.close();
				publishProgress(100);
				return DOWNLOAD_ERROR_OK;
			} catch (Exception e) {
				Debugger.error("Error getting content of music: " + e.toString());
				return DOWNLOAD_ERROR_IOERROR;
			}
			
		}
	}

}
