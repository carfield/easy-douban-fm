package com.saturdaycoder.easydoubanfm;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import com.saturdaycoder.easydoubanfm.channels.FmChannel;

import android.content.ContentValues;

public class Database extends SQLiteOpenHelper {
	private static final int INVALID_DOWNLOAD_ID = -1;
	
	private static final String DATABASE_NAME = "userdb.sqlite";
	private static final int DB_VERSION = 2;
	
	protected Database(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE channels (channel_id INTEGER primary key, "
				+ " abbr_en TEXT DEFAULT '', "
				+ " name_en TEXT DEFAULT '', "
				+ " name TEXT DEFAULT '', "
				+ " seq_id INTEGER)");
		db.execSQL("CREATE TABLE downloads (id INTEGER primary key, "
				+ " url TEXT DEFAULT '', "
				+ " filename TEXT DEFAULT '')");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS channels");
		db.execSQL("DROP TABLE IF EXISTS downloads");
		onCreate(db);
	}
	
	public void clearDownloads() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from downloads");
	}
	
	public void clearChannels() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from channels");
	}
	
	public int addDownload(String url, String filename) {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		int existId = getDownloadIdByUrl(url);
		if (existId != INVALID_DOWNLOAD_ID) {
			return existId;
		}
		
		Cursor cursor = db.rawQuery("select id from downloads order by id asc", null);
		if (cursor.moveToFirst()) 
		{
			do {
				list.add(cursor.getInt(0));
			} while(cursor.moveToNext());
			int[] ids = new int[list.size()];
			for (int i = 0; i < ids.length; ++i) {
				ids[i] = list.get(i);
			}
		}
		
		int i = Global.NOTIFICATION_ID_DOWNLOAD_MIN;
		for (; i < Integer.MAX_VALUE; ++i) {
			if (!list.contains(i)) {
				break;
			}
		}
		
		if (i == Integer.MAX_VALUE)
			return INVALID_DOWNLOAD_ID;
		
		ContentValues values = new ContentValues();
		values.put("id", i);
		values.put("url", url);
		values.put("filename", filename);
		
		long res = db.insertOrThrow("downloads", null, values);		
		
		if (res == -1)
			return INVALID_DOWNLOAD_ID;
		else
			return i;
	}
	
	public void removeDownload(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from downloads where id=" + id);
	}
	
	public void removeDownload(String url) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from downloads where url='" + url + "'");
	}
	
	public int[] getDownloadIds() {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<Integer> list = new ArrayList<Integer>();
		Cursor cursor = db.rawQuery("select id from downloads order by id asc", null);
		if (cursor.moveToFirst()) 
		{
			do {
				list.add(cursor.getInt(0));
			} while(cursor.moveToNext());
			int[] ids = new int[list.size()];
			for (int i = 0; i < ids.length; ++i)
				ids[i] = list.get(i);
			return ids;
		}
		return null;
	}
	
	public String[] getDownloadUrls() {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<String> list = new ArrayList<String>();
		Cursor cursor = db.rawQuery("select url from downloads order by id asc", null);
		if (cursor.moveToFirst()) 
		{
			do {
				list.add(cursor.getString(0));
			} while(cursor.moveToNext());
			String[] urls = new String[list.size()];
			return list.toArray(urls);
		}
		return null;
	}
	
	public int getDownloadIdByUrl(String url) {
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.rawQuery("select id from downloads where url='" + url + "'", null);
		if (cursor.moveToFirst()) 
		{
			return cursor.getInt(0);
		}
		return INVALID_DOWNLOAD_ID;
	}
	public String getFilenameByUrl(String url) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select filename from downloads where url='" + url + "'", null);
		if (cursor.moveToFirst()) 
		{
			return cursor.getString(0);
		}
		return null;
	}
	
	public String getFilenameById(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select filename from downloads where id=" + id, null);
		if (cursor.moveToFirst()) 
		{
			return cursor.getString(0);
		}
		return null;
	}
	public String getDownloadUrlById(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select url from downloads where id=" + id, null);
		if (cursor.moveToFirst()) 
		{
			return cursor.getString(0);
		}
		return null;
	}
	
	public int saveChannel(FmChannel dfc) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("channel_id", dfc.channelId);
		values.put("abbr_en", dfc.abbrEn);
		values.put("name_en", dfc.nameEn);
		values.put("name", dfc.name);
		values.put("seq_id", dfc.seqId);
		
		long res = db.insertOrThrow("channels", null, values);
		if (res == -1) {
			ContentValues valuesUpdate = new ContentValues();
			valuesUpdate.put("abbr_en", dfc.abbrEn);
			valuesUpdate.put("name_en", dfc.nameEn);
			valuesUpdate.put("name", dfc.name);
			valuesUpdate.put("seq_id", dfc.seqId);
			
			return db.update("channels", valuesUpdate, "channel_id=?", 
					new String[]{ String.valueOf(dfc.channelId)});
		} else {
			return 1;
		}
	}
	
	public FmChannel[] getChannels() {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<FmChannel> list = new ArrayList<FmChannel>();
		Cursor cursor = db.rawQuery("select * from channels order by seq_id asc", null);
		if (cursor.moveToFirst()) 
		{
			do {
				FmChannel dfc = new FmChannel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
				list.add(dfc);
			} while(cursor.moveToNext());
			FmChannel[] dfcs = new FmChannel[list.size()];
			return list.toArray(dfcs);
		}
		else return null;
	}
	
	public FmChannel getChannelInfo(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from channels where channel_id=" + id, null);
		if (cursor.moveToFirst()) 
		{
			FmChannel dfc = new FmChannel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
			return dfc;
		}
		else return null;
	}
	
	public int getChannelIndex(int chanId) {
		int i = 0;
		FmChannel[] chans = getChannels();
		for (; i < chans.length; ++i) {
			if (chans[i].channelId == chanId) {
				return i;
			}
		}
		
		return -1;
		
		
	}
	public int getFirstPublicChannel() {
		FmChannel[] chans = getChannels();
		for (int i = 0; i < chans.length; ++i) {
			if (chans[i].channelId > 0) {
				return chans[i].channelId;
			}
		}
		return 0;
	}
	public boolean isChannelIdValid(int id) {
		FmChannel[] chans = getChannels();
		for (int i = 0; i < chans.length; ++i) {
			if (id == chans[i].channelId) {

				return true;
			}
		}
		return false;
	}
	
}
