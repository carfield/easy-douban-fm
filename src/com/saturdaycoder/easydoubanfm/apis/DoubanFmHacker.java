package com.saturdaycoder.easydoubanfm.apis;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import com.saturdaycoder.easydoubanfm.Debugger;
import com.saturdaycoder.easydoubanfm.channels.FmChannel;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

public class DoubanFmHacker {
	public static FmChannel[] hackChannelTable() throws IOException {
		Debugger.verbose("Start HACKING channel table");
		String uri = "http://www.douban.fm";
		HttpGet httpGet = new HttpGet(uri);

		try {
			Debugger.verbose("request is:");
			Debugger.verbose(httpGet.getRequestLine().toString());
			for (Header h: httpGet.getAllHeaders()) {
				Debugger.verbose(h.toString());
			}

			
			HttpContext context = new BasicHttpContext(); 
			DefaultHttpClient hc = new DefaultHttpClient();
			
			HttpResponse httpResponse = null;
			try {
				httpResponse = hc.execute(httpGet, context);
			} catch (Exception e) {
				try {
					HttpUriRequest currentReq = (HttpUriRequest) context.getAttribute( 
		                    ExecutionContext.HTTP_REQUEST);
		            HttpHost currentHost = (HttpHost)  context.getAttribute( 
		                    ExecutionContext.HTTP_TARGET_HOST);
		            String currentUrl = currentHost.toURI() + currentReq.getURI();
		            Debugger.debug("currentUrl = \"" + currentUrl + "\"");
					httpGet = new HttpGet(currentUrl);
					httpResponse = hc.execute(httpGet);
				} catch (Exception ex) {
					ex.printStackTrace();
					return null;
				}
			}
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
				String s = new String(b);
				int i =0;
				while (i < s.length()) {
					Debugger.error(s.substring(i, (s.length() - i >= 3000)? (i + 3000): s.length()));
					i += 3000;
				}
				
				return extractChannelsFromHtml(s);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		//return null;
	}
	
	private static FmChannel[] extractChannelsFromHtml(String s) {
		String scriptstr = null;
		while ((s = getFirstScriptString(s)) != null) {
			if (s.contains("channelInfo")) {
				return null;
			}
			else continue;
		}
		return null;
	}

	private static String getFirstScriptString(String s) {
		//String scriptstr = s;
		int start = s.indexOf("<script type=\"text/javascript\">");
		int end = s.indexOf("</script>");
		if (end < start + 31) {
			return null;
		}
		return s.substring(start + 31, end - 9);
		
	}
}
