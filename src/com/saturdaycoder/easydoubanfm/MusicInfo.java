package com.saturdaycoder.easydoubanfm;
import android.graphics.*;
public class MusicInfo {
	String sid;
	String aid;
	String like;
	String albumtitle;
	String company;
	double rating_avg;
	String album;
	String artist;
	String title;
	String pictureUrl;
	String musicUrl;
	//Bitmap bmp;
	public boolean isRated() {
		return !like.equals("0");
	}
	
	public void rate(boolean rate) {
		like = rate? "1": "0";
	}
	
	@Override
	public String toString() {
		
		String s = "";
		if (albumtitle != null)
			s += "AlbumTitle: " + albumtitle;
		if (company != null)
			s += ", company: " + company + ", rating_avg: " + rating_avg;
		if (album != null)
			s += ", album: " + album;
		if (artist != null)
			s += ", artist: " + artist;
		if (title != null)
			s += ", title: " + title;
		if (pictureUrl != null)
			s += ", pictureUrl: " + pictureUrl;
		if (musicUrl != null)
			s += ", musicUrl: " + musicUrl;
		if (sid != null)
			s += ", sid: " + sid;
		if (aid != null)
			s += ", aid: " + aid;
		if (like != null)
			s += ", like: " + like;
		return s;
	}
}
