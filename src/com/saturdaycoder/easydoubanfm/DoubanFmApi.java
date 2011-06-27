package com.saturdaycoder.easydoubanfm;
import org.json.*;
import org.apache.http.params.*;
//import java.net.SocketException;
import java.util.Date;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.*;
import org.json.JSONObject;
public class DoubanFmApi {

	public static final char TYPE_BYE = 'b';
	public static final char TYPE_END = 'e';
	public static final char TYPE_NEW = 'n';
	public static final char TYPE_PLAYING = 'p';
	public static final char TYPE_SKIP = 's';
	public static final char TYPE_RATE = 'r';
	public static final char TYPE_UNRATE = 'u';
	
	private static HttpParams httpParams = null;
	
	public static void setHttpParameters(HttpParams params) {
		DoubanFmApi.httpParams = params;
	}
	
	public static FmChannel[] getChannelTable() throws IOException {
		Debugger.verbose("Start SCANNING channel table");
		String uri = "http://www.douban.com:80/j/app/radio/channels?";
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeader("User-Agent", Utility.getSdkVersionName());
		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			
			DefaultHttpClient hc = null;
			if (httpParams == null) {
				hc = new DefaultHttpClient();
			}
			else {
				hc = new DefaultHttpClient(httpParams);
				Debugger.info("set timeout for login: " 
						+ HttpConnectionParams.getConnectionTimeout(hc.getParams())
						+ "/" 
						+ HttpConnectionParams.getSoTimeout(hc.getParams()));
			}
			HttpResponse httpResponse = hc.execute(httpGet);
			Debugger.verbose("response is:");
			Debugger.verbose(httpResponse.getStatusLine().toString());
			for (Header h: httpResponse.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			if (httpResponse.getStatusLine().getStatusCode() != 200) {
				Debugger.error("getchannel response is " + httpResponse.getStatusLine().getStatusCode());
				return null;
			} else {
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				byte b[] = new byte[length];
				int l = 0;
				while (l < length) {
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				String c = new String(b, 0, length, "UTF-8");
				JSONObject json = new JSONObject(c);
				JSONArray jsa = json.getJSONArray("channels");
				FmChannel[] channels = new FmChannel[jsa.length()];
				for (int i = 0; i < jsa.length(); ++i) {
					JSONObject co = (JSONObject)jsa.get(i);
					FmChannel chan = new FmChannel(co.getInt("channel_id"),
							co.getString("abbr_en"),
							co.getString("name_en"),
							co.getString("name"),
							co.getInt("seq_id"));
					/*JSONObject co = (JSONObject)jsa.get(i);
					chan.abbrEn = co.getString("abbr_en");
					chan.channelId = co.getInt("channel_id");
					chan.nameEn = co.getString("name_en");
					chan.name = co.getString("name");
					chan.seqId = co.getInt("seq_id");*/
					Debugger.info("scanned new channel: name=" + chan.name 
							+ " id=" + chan.channelId + " seq=" + chan.seqId);
					channels[i] = chan;
					//db.saveChannel(chan);
				}
				return channels;
			}
		} catch (IOException e) {
			Debugger.error("error scanning channel table: " + e.toString());	
			throw e;
		} catch (Exception e) {
			Debugger.error("error scanning channel table: " + e.toString());
			return null;
		}
	}
	
	public static MusicInfo[] report(LoginSession session, int channel, 
									String reportSid, char reportType, 
									String[] historySids) throws IOException {
		String url = "http://www.douban.com:80/j/app/radio/people?app_name=radio_android";
		User user = null;
		Cookie cookie = null;
		
		if (session != null) {
			user = session.user;
			cookie = session.cookie;
		}
		
		if (user != null) {
			url += "&user_id=" + user.userId + "&token=" + user.token 
					+ "&expire=" + user.expire;
		} 
		url += "&type=" + reportType;
		
		url += "&version=" + Utility.getClientVersion();
		
		if (reportSid != null && !reportSid.equals(""))
			url += "&sid=" + reportSid;
		url += "&channel=" + channel;
			
		if (historySids != null) {
			url += "&h=";
			for (int i = 0; i < historySids.length; ++i) {
				String h = historySids[i];
				if (h == null)
					continue;
				url += h + ":p";
				if (i < historySids.length - 1)
					url += "%7C";
			}
		}
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeader("User-Agent", Utility.getSdkVersionName());
		if (user != null) {
			httpGet.setHeader("Cookie", "bid=\"" + cookie.bid + "\"");
			//httpGet.setHeader("Cookie2", "$Version=1");
		}
		
		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			
			DefaultHttpClient hc = null;
			if (httpParams == null) {
				hc = new DefaultHttpClient();
			}
			else {
				hc = new DefaultHttpClient(httpParams);
				Debugger.info("set timeout for login: " 
						+ HttpConnectionParams.getConnectionTimeout(hc.getParams())
						+ "/" 
						+ HttpConnectionParams.getSoTimeout(hc.getParams()));
			}
			HttpResponse httpResponse = hc.execute(httpGet);
			Debugger.verbose("response is:");
			Debugger.verbose(httpResponse.getStatusLine().toString());
			for (Header h: httpResponse.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				byte b[] = new byte[length];
				int l = 0;
				while (l < length) {
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				String c = new String(b, 0, length, "UTF-8");
				Debugger.verbose(c);
				
				JSONObject json = new JSONObject(c);
				int r = json.getInt("r");
				
				if (r != 0) {
					// error happened
					String err = json.getString("err");
					Debugger.error("error happened: " + err);
					return null;
				}
				
				JSONArray ja = json.getJSONArray("song");
				MusicInfo[] musics = new MusicInfo[ja.length()];
				for (int i = 0; i < ja.length(); ++i) {
					MusicInfo dfm = new MusicInfo(); 
					musics[i] = dfm;
					JSONObject o = ja.getJSONObject(i);
					//Debugger.verbose(o.toString());
					try {
						dfm.album = o.getString("album");
					} catch (JSONException e) {
						dfm.album = "";
					}
					try {
						dfm.albumtitle = o.getString("albumtitle");
					} catch (JSONException e) {
						dfm.albumtitle = "";
					}
					try {
						dfm.artist = o.getString("artist");
					} catch (JSONException e) {
						dfm.artist = "";
					}
					try {
						dfm.company = o.getString("company");
					} catch (JSONException e) {
						dfm.company = "";
					}
					try {
						dfm.rating_avg = o.getDouble("rating_avg");
					} catch (JSONException e) {
						dfm.rating_avg = 0;
					}
					try {
					dfm.title = o.getString("title");
					} catch (JSONException e) {
						dfm.title = "";
						
					}
					
					dfm.pictureUrl = o.getString("picture");
					dfm.musicUrl = o.getString("url");
					dfm.sid = o.getString("sid");
					dfm.aid = o.getString("aid");
					try {
						dfm.like = o.getString("like");
					} catch (JSONException e) {
						dfm.like = "0";
					}
					Debugger.verbose("New song: " + dfm.toString());
				}
				return musics;
			} else {
				return null;
			}
		}catch (ClientProtocolException e) {
			Debugger.error("Error get: " + e.toString());
			return null;
		} catch (IOException e) {
			Debugger.error("Error get: " + e.toString());	
			//return null;
			throw e;
		} catch (Exception e) {
			Debugger.error("Error get: " + e.toString());			
			return null;
		}
	}
	
	private static String encodeUrl(String original) {
		String encoded = original;
		encoded = encoded.replace("/", "%2f").replace("@", "%40").replace("=", "%3d");
		encoded = encoded.replace(":", "%3a").replace(";", "%3b").replace("+", "%2b");
		return encoded;
	}
	

	
	private static String lastLoginErr = "";
	public static String getLoginError() {
		return lastLoginErr;
	}
	public static LoginSession login(String email, String passwd, int cliVer) throws IOException{
		if (email == null || email.equals("") || passwd == null || passwd.equals(""))
			return null;
		
		String url = "http://www.douban.com:80/j/app/login?app_name=radio_android&version=" 
				+ cliVer + "&email=" + encodeUrl(email) + "&password=" + encodeUrl(passwd);
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeader("User-Agent", Utility.getSdkVersionName());
		
		//DoubanFmSession session = new DoubanFmSession();
		
		Cookie cookie = null;
		
		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}
			
			DefaultHttpClient hc = null;
			if (httpParams == null) {
				hc = new DefaultHttpClient();
			}
			else {
				
				hc = new DefaultHttpClient(httpParams);
				Debugger.info("set timeout for login: " 
						+ HttpConnectionParams.getConnectionTimeout(hc.getParams())
						+ "/" 
						+ HttpConnectionParams.getSoTimeout(hc.getParams()));
			}
			HttpResponse httpResponse = hc.execute(httpGet);
			Debugger.verbose("response is:");
			Debugger.verbose(httpResponse.getStatusLine().toString());
			for (Header h: httpResponse.getAllHeaders()) {
				Debugger.verbose(h.toString());
				if (h.getName().equals("Set-Cookie")) {
					cookie = new Cookie();
					HeaderElement[] elements = h.getElements();
					for (HeaderElement e: elements) {
						if (e.getName().equals("bid")) {
							cookie.bid = e.getValue();
						}
						if (e.getName().equals("path")) {
							cookie.path = e.getValue();
						}
						if (e.getName().equals("domain")) {
							cookie.domain = e.getValue();
						}
						if (e.getName().equals("expires")) {
							cookie.expires = Date.parse(e.getValue());
						}
					}
				}
			}
			

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				
				InputStream is = httpResponse.getEntity().getContent();
				long len = httpResponse.getEntity().getContentLength();
				int length = (int)(len);
				byte b[] = new byte[length];
				int l = 0;
				while (l < length) {
					int tmpl = is.read(b, l, length);
					if (tmpl == -1)
						break;
					l += tmpl;
				}
				
				String c = new String(b, 0, length, "UTF-8");
				Debugger.verbose(c);
				
				JSONObject json = new JSONObject(c);
				int r = json.getInt("r");
				lastLoginErr = json.getString("err");
				if (r == 0) {
					User user = new User();
					user.userId = json.getString("user_id");
					user.token = json.getString("token");
					user.expire = json.getString("expire");
					LoginSession session = new LoginSession();
					session.user = user;
					session.cookie = cookie;
					return session;
				} else {
					return null;
				}
				
			} else {
				Debugger.error("Error login: wrong status=" + httpResponse.getStatusLine().getStatusCode());
				return null;
			}
		} catch (ClientProtocolException e) {
			Debugger.error("Error login: " + e.toString());
			lastLoginErr = e.toString();
			return null;
		} catch (IOException e) {
			Debugger.error("Error login: " + e.toString());
			throw e;
			//return null;
		} catch (Exception e) {
			Debugger.error("Error login: " + e.toString());
			lastLoginErr = e.toString();
			return null;
		}
	}
	
	
}
