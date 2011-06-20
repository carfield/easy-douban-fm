package com.saturdaycoder.easydoubanfm;
import java.io.File;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class SingleMediaScanner implements MediaScannerConnectionClient {

	private MediaScannerConnection mMs;
	private File mFile;
	
	public SingleMediaScanner(Context context, File f) {
	    mFile = f;
	    mMs = new MediaScannerConnection(context, this);
	    mMs.connect();
	}
	
	@Override
	public void onMediaScannerConnected() {
		Debugger.info("SCAN FILE CONNECTED");
	    mMs.scanFile(mFile.getAbsolutePath(), null);
	}
	
	@Override
	public void onScanCompleted(String path, Uri uri) {
		Debugger.info("SCAN FILE FINISHED!");
	    mMs.disconnect();
	}

}