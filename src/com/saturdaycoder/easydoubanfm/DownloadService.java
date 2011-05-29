package com.saturdaycoder.easydoubanfm;
import android.app.*;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import java.io.*;
import android.os.*;
import android.content.*;
import java.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class DownloadService extends Service implements IDownloadService {
	
	private static final String ACTION_DOWNLOAD = "com.saturdaycoder.easydoubanfm.download";
	private static final String ACTION_CLEAR_NOTIFICATION = "com.saturdaycoder.easydoubanfm.clear_notification";
	private static final String ACTION_NULL = "com.saturdaycoder.easydoubanfm.null";
	private static final String EXTRA_DOWNLOAD_SESSION = "session";
	private static final String EXTRA_DOWNLOAD_URL = "url";
	private static final String EXTRA_DOWNLOAD_FILENAME = "filename";
	
	private Map<Integer, DownloadTask> tasks;
	
	private File getDownloadCacheFile(String filename){
		return new File(Environment.getDownloadCacheDirectory(), filename);
	}
	
	private File getDownloadFile(String filename) {
		
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return null;
		}
		
		File dir = new File(android.os.Environment.getExternalStorageDirectory(), 
				Preference.getDownloadDirectory(this));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(Environment.getExternalStorageDirectory(), 
				Preference.getDownloadDirectory(this) + "/" + filename);
		return file;
	}
	
	private boolean writable;
	private int sessionId;
	private boolean getExternalStorageState() {
		return writable;
	}
	
	@Override
	public int download(String url, String filename) {
		Debugger.debug("DownloadService.download(" + url + ", " + filename + ")");
		DownloadTask task = null;
		int ret = -1;
		synchronized(this) {
			task = new DownloadTask(sessionId);
			tasks.put(sessionId, task);
			ret = sessionId;
			++sessionId;
		}
		Debugger.debug("execute task " + ret);
		notifyDownloading(ret, url, filename);
		task.execute(url, filename);
		return ret;
	}
	
	@Override
	public int getProgress(int sessionId) {
		DownloadTask task = tasks.get(sessionId);
		if (task != null) {
			return task.getProgress();
		}
		else {
			return -1;
		}
	}
	
	@Override
	public void cancel(int sessionId) {
		DownloadTask task = tasks.get(sessionId);
		if (task != null) {
			task.cancel(true);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	private final IBinder mBinder = new DownloadService.LocalBinder();	
	public class LocalBinder extends Binder {
		
		DownloadService getService() {
			return DownloadService.this;
		}
	}
	
	@Override
	public void onCreate() {
		Debugger.info("DOWNLOAD SERVICE ONCREATE");
		super.onCreate();

		tasks = new HashMap<Integer, DownloadTask>();
		
		sessionId = 1;
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	    filter.addAction(ACTION_DOWNLOAD);
	    filter.addAction(ACTION_CLEAR_NOTIFICATION);
	    if (mReceiver == null) {
	    	mReceiver = new DownloadEventReceiver();
	    }
	    registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		Debugger.info("DOWNLOAD SERVICE ONDESTROY");
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
		}
		
		mReceiver = null;
		
		// Cancel not-finished tasks
		Debugger.debug("remaining tasks " + tasks.keySet().size());
		
		Set<Integer> keyset = tasks.keySet();
		Iterator<Integer> it = keyset.iterator();
		while(it.hasNext()) {
			int sid = it.next();
			DownloadTask task = tasks.get(sid);
			if (task != null) {
				task.cancel(true);
			}
		}
		Debugger.debug("cleared all remaining tasks");
		tasks.clear();
		//tasks = null;
		//Debugger.info("DOWNLOAD SERVICE ONDESTROY");
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Debugger.info("DOWNLOAD SERVICE ONSTART");
		super.onStart(intent, startId);
	}
	
	private DownloadEventReceiver mReceiver;
	private class DownloadEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ACTION_CLEAR_NOTIFICATION)) {
				int sessionId = intent.getIntExtra(EXTRA_DOWNLOAD_SESSION, -1);
				NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				if (sessionId != -1) {
					nm.cancel(sessionId);
				}
			}
			if (action.equals(ACTION_DOWNLOAD)) {
				String url = intent.getStringExtra(EXTRA_DOWNLOAD_URL);
				String filename = intent.getStringExtra(EXTRA_DOWNLOAD_FILENAME);
				download(url, filename);
			}
		}
	}
	
	private static final int DOWNLOAD_ERROR_OK = 0;
	private static final int DOWNLOAD_ERROR_IOERROR = -1;
	private static final int DOWNLOAD_ERROR_CANCELLED = -2;
	//private static final int 
	
	
	private class DownloadTask extends AsyncTask<String, Integer, Integer> {
		private int sessionId;
		private int progress;
		private String url;
		private String filename;
		public DownloadTask(int sessionId) {
			this.sessionId = sessionId;
			progress = -1;
		}
		public int getProgress() {
			return progress;
		}
		@Override
    	protected void onProgressUpdate(Integer... progress) {
			this.progress = progress[0];
			Debugger.debug("Download progress: " + this.progress);
		}
		
		@Override
        protected void onPostExecute(Integer result) {
			switch (result) {
			case DOWNLOAD_ERROR_OK:
				Debugger.info("Download finish");
				notifyDownloadOk(this.sessionId, this.url, this.filename);
				break;
			case DOWNLOAD_ERROR_IOERROR:
			case DOWNLOAD_ERROR_CANCELLED:
				Debugger.info("Download failed");
				notifyDownloadFail(this.sessionId, this.url, this.filename);
				break;
			default:
				break;
			}
			tasks.remove(this.sessionId);
			Debugger.info("remaining task " + tasks.size());
			// exit service if no pending tasks
			if (tasks.size() == 0) {
				Debugger.info("Download service closed himself");
				stopSelf();
				
			}
		}
		
		@Override
    	protected void onCancelled() {
			tasks.remove(this.sessionId);
			notifyDownloadFail(this.sessionId, this.url, this.filename);
			// exit service if no pending tasks
			if (tasks.size() == 0) {
				stopSelf();
			}
		}
		
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
    				String.valueOf(Preference.getClientVersion(DownloadService.this)));

    		HttpResponse httpResponse = null;
    		try {
    			httpResponse = new DefaultHttpClient().execute(httpGet);
    			publishProgress(10);
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
			
			byte b[] = null;
			try {
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				b = new byte[length];
				int l = 0;
				while (l < length) {
					if (isCancelled())
						return DOWNLOAD_ERROR_CANCELLED;
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
					double prog = 10 + ((double)l / length * 80);
					publishProgress((int)prog);
					//Debugger.info("read " + prog + "%");
				}
				
			} catch (Exception e) {
				Debugger.error("Error getting content of music: " + e.toString());
				return DOWNLOAD_ERROR_IOERROR;
			}
			
			Debugger.info("got all bytes");
			
			if (isCancelled())
				return DOWNLOAD_ERROR_CANCELLED;
			
			// Step 2. write into external storage
			try {
				File musicfile = null;
				musicfile = getDownloadFile(filename);
				
				if (musicfile == null) {
					Debugger.error("can not get download file");
					return DOWNLOAD_ERROR_IOERROR;
				}
				Debugger.info("got download file, start writing");
				OutputStream os = new FileOutputStream(musicfile);
				os.write(b);
				os.flush();
				os.close();
				publishProgress(100);
				return DOWNLOAD_ERROR_OK;
			} catch (Exception e) {
				Debugger.error("Error writing file to external storage: " + e.toString());
				return DOWNLOAD_ERROR_IOERROR;
			}
		}
	}
	
	private void notifyDownloading(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Intent i = new Intent(ACTION_NULL);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_sys_download, 
				"下载中",
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, "下载中", filename, pi);
        nm.notify(sessionId, notification);
	}
	
	private void notifyDownloadOk(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				
		Intent i = new Intent(ACTION_CLEAR_NOTIFICATION);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_sys_download_done, 
				"下载完成",
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, "下载完成", filename, pi);
        nm.notify(sessionId, notification);
	}
	
	private void notifyDownloadFail(int sessionId, String url, String filename) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		Intent i = new Intent(ACTION_CLEAR_NOTIFICATION);
		i.putExtra(EXTRA_DOWNLOAD_SESSION, sessionId);
		i.putExtra(EXTRA_DOWNLOAD_URL, url);
		i.putExtra(EXTRA_DOWNLOAD_FILENAME, filename);
		PendingIntent pi = PendingIntent.getBroadcast(this, 
				0, i, 0);

		Notification notification = new Notification(android.R.drawable.stat_notify_error, 
				"下载失败",
                System.currentTimeMillis());
        notification.setLatestEventInfo(this, "下载失败", filename, pi);
        nm.notify(sessionId, notification);
		
	}
}
