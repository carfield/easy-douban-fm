package com.saturdaycoder.easydoubanfm;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class DoubanFmDatabase extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "userdb.sqlite";
	private static final int DB_VERSION = 1;
	private Context context;
	protected DoubanFmDatabase(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
		this.context = context;
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE channels (channel_id INTEGER primary key, "
				+ " abbr_en TEXT DEFAULT '', "
				+ " name_en TEXT DEFAULT '', "
				+ " name TEXT DEFAULT '', "
				+ " seq_id INTEGER)");
		//selectChannel(0);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		db.execSQL("DROP TABLE IF EXISTS channels");
		onCreate(db);
	}
	
	public void clearChannels() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("delete from channels");
	}
	
	public int saveChannel(DoubanFmChannel dfc) {
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
	
	public DoubanFmChannel[] getChannels() {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<DoubanFmChannel> list = new ArrayList<DoubanFmChannel>();
		Cursor cursor = db.rawQuery("select * from channels order by seq_id asc", null);
		if (cursor.moveToFirst()) 
		{
			do {
				DoubanFmChannel dfc = new DoubanFmChannel();
				dfc.channelId = cursor.getInt(0);
				dfc.abbrEn = cursor.getString(1);
				dfc.nameEn = cursor.getString(2);
				dfc.name = cursor.getString(3);
				dfc.seqId = cursor.getInt(4);
				list.add(dfc);
			} while(cursor.moveToNext());
			DoubanFmChannel[] dfcs = new DoubanFmChannel[list.size()];
			return list.toArray(dfcs);
		}
		else return null;
	}
	
	public DoubanFmChannel getChannelInfo(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from channels where channel_id=" + id, null);
		if (cursor.moveToFirst()) 
		{
			DoubanFmChannel dfc = new DoubanFmChannel();
			dfc.channelId = cursor.getInt(0);
			dfc.abbrEn = cursor.getString(1);
			dfc.nameEn = cursor.getString(2);
			dfc.name = cursor.getString(3);
			dfc.seqId = cursor.getInt(4);
			return dfc;
		}
		else return null;
	}
	private static SharedPreferences pref = null;
	public int getSelectedChannel() {
		if (pref == null)
			pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		int a = pref.getInt("selected_channel", -1);
		Debugger.info("##### retrieved channel = " + a);
		return (a == -1)? 0: a;
	}
	
	public boolean selectChannel(int c) {
		if (pref == null)
			pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		Editor ed = pref.edit();
		ed.putInt("selected_channel", c);
		ed.commit();
		Debugger.info("##### saved channel = " + c);
		return true;
	}
}
