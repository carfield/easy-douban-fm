package com.saturdaycoder.easydoubanfm;

public class DoubanFmMusic {
	String albumtitle;
	String company;
	double rating_avg;
	String album;
	String artist;
	String title;
	String pictureUrl;
	String musicUrl;
	
	@Override
	public String toString() {
		return "AlbumTitle: " + albumtitle + ", company: " + company
				+ ", rating_avg: " + rating_avg + ", album: " + album
				+ ", artist: " + artist + ", title: " + title
				+ ", pictureUrl: " + pictureUrl + ", musicUrl: " + musicUrl;
	}
}
