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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

public class DoubanFmDownloader {
	private boolean isOn = false;
	
	public void open() {
		
	}
	
	public void close() {
		
	}
	
	public boolean isOn() {
		return isOn;
	}
	
	private static final int DOWNLOAD_BUFFER = 102400;
	/*private class DownloadTask extends AsyncTask<String, Integer, Integer> {
		private int sessionId;
		private int lastProgress;
		private int progress;
		private String url;
		private String filename;
		public DownloadTask(int sessionId) {
			this.sessionId = sessionId;
			progress = -1;
			lastProgress = 0;
		}
		public int getProgress() {
			return progress;
		}
		@Override
    	protected void onProgressUpdate(Integer... progress) {
			this.progress = progress[0];
			Debugger.verbose("Download progress: " + this.progress);
			
			if (isCancelled()) {
				return;
			}
			
			Notification n = notifications.get(this.sessionId);
			if (n == null) {
				return;
			}
			
			if (this.progress == 0 || this.progress == 100
					|| this.progress - lastProgress > 3
					) {
				n.contentView.setProgressBar(R.id.progressDownloadNotification, 
						100, this.progress, false);
				String text = String.valueOf(this.progress) + "%";
				if (totalBytes != -1 && downloadedBytes != -1)
					text += " (" + downloadedBytes/1024 + "K/" + totalBytes/1024 + "K)";
				n.contentView.setTextViewText(R.id.textDownloadSize, 
						getResources().getString(R.string.text_download_cancel)+ text);
				
				Intent i = new Intent(ACTION_DOWNLOADER_CANCEL);
				i.putExtra(EXTRA_DOWNLOADER_SESSION_ID, sessionId);
				PendingIntent pi = PendingIntent.getBroadcast(DoubanFmService.this, 
						0, i, PendingIntent.FLAG_CANCEL_CURRENT);
				n.contentIntent = pi;
				
				notificationManager.notify(this.sessionId, n);
				
				this.lastProgress = this.progress;
			}
			
			
		}
		
		@Override
        protected void onPostExecute(Integer result) {
			switch (result) {
			case DOWNLOAD_ERROR_OK:
				Debugger.info("Download finish");
				notifyDownloadOk(this.sessionId, this.url, this.filename);
				
				File musicfile = null;
				musicfile = getDownloadFile(filename);
				
				if (musicfile != null) {
					SingleMediaScanner scanner = new SingleMediaScanner(DoubanFmService.this, musicfile);
				}
				
				break;
			case DOWNLOAD_ERROR_IOERROR:
			case DOWNLOAD_ERROR_CANCELLED:
				File f = getDownloadFile(filename);
				if (f.exists()) {
					f.delete();
				}
				Debugger.info("Download failed");
				notifyDownloadFail(this.sessionId, this.url, this.filename);
				break;
			default:
				break;
			}
			tasks.remove(this.sessionId);
			Debugger.info("remaining task " + tasks.size());

		}
		
		@Override
    	protected void onCancelled() {
			File f = getDownloadFile(filename);
			if (f.exists()) {
				f.delete();
			}
			
			tasks.remove(this.sessionId);
			notifyDownloadFail(this.sessionId, this.url, this.filename);

		}
		private long totalBytes = -1;
		private long downloadedBytes = -1;
		@Override
    	protected Integer doInBackground(String... params) {
			// param 0: url of music
			// param 1: download filename
			
			if (params.length < 2) {
				Debugger.error("Download task requires more arguments than " + params.length);
				return DOWNLOAD_ERROR_CANCELLED;
			}
			this.url = params[0];
			this.filename = params[1];
			Debugger.info("url = " + this.url + ", filename = " + this.filename); 
			
			// Step 1. get bytes
			HttpGet httpGet = new HttpGet(url);
    		httpGet.setHeader("User-Agent", 
    				String.valueOf(Preference.getClientVersion()));

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
					
					Debugger.debug("writing file " + tmpl + ", " + downloadedBytes + "/" + totalBytes);
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
	private Context context;
	private Map<Integer, DownloadTask> tasks;
	private boolean writable;
*/	
	
	/*public boolean isRunning() {
		Debugger.info("downloader task list size: " + tasks.size());
		return tasks.size() > 0;
	}
	
	public Downloader(Context context) {
		this.context = context;
		Debugger.info("DOWNLOADER CREATE");
		tasks = new HashMap<Integer, DownloadTask>();
		//sessionId = 1;
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
	

	private boolean getExternalStorageState() {
		return writable;
	}

	public int download(int sid, String url, String filename) {
		Debugger.debug("DownloadService.download(" + url + ", " + filename + ")");
		DownloadTask task = null;
		//int ret = -1;
		//synchronized(this) {
		task = new DownloadTask(sid);
		tasks.put(sid, task);
		//ret = sessionId;
		//++sessionId;
		//}
		Debugger.debug("execute task " + sid);
		notifyDownloading(sid, url, filename);
		task.execute(url, filename);
		return sid;
	}
	

	public int getProgress(int sessionId) {
		DownloadTask task = tasks.get(sessionId);
		if (task != null) {
			return task.getProgress();
		}
		else {
			return -1;
		}
	}
	
	//@Override
	public void cancel(int sessionId) {
		DownloadTask task = tasks.get(sessionId);
		if (task != null) {
			task.cancel(true);
			tasks.remove(sessionId);
		}
	}
	
	public void cancelAll() {
		Set<Integer> keyset = tasks.keySet();
		Iterator<Integer> it = keyset.iterator();
		while (it.hasNext()) {
			Integer sid = it.next();
			DownloadTask t = tasks.get(sid);
			t.cancel(true);
		}
		tasks.clear();
	}*/
}
