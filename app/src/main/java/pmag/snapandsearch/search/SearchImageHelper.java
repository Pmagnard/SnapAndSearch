package pmag.snapandsearch.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class SearchImageHelper {

	private String logAppName = "SearchImageHelper";

	/* set debug mode in Android mode or System.out mode */
	private boolean androidDebugMode = false;

	// timeout for HTTP connections in ms
	private final static int HTTP_TIMEOUT = 60000;
	
	/**
	 * Constructor with Android Debug Mode and log application name for debug
	 * @param androidDebugMode
	 */
	public SearchImageHelper(boolean androidDebugMode, String logAppName) {
		this.androidDebugMode = androidDebugMode;
		this.logAppName = logAppName;
	}
	
	private void logError(boolean androidMode, String logId, String logMessage, Throwable t) {
		if (androidMode) {
			Log.e(logId, logMessage, t);
		} else {
			System.err.println(logId + " " + logMessage + t);
		}
	}

	private HttpResponse executeHttpRequest(HttpClient httpClient, HttpRequestBase request) {
		HttpResponse response = null;
		try {
			response = httpClient.execute(request);
		} catch (Exception e) {
			Log.e(logAppName, "error on HTTPClient execute", e);
		}

		return response;
	}


	/**
	 * search by image using Microsoft Bing service
	 * 
	 * @param imageFile
	 */
	public String searchBingByImage(File imageFile) {
		// HTTP Client with some parameters
		HttpParams httpParams = new BasicHttpParams();
		// set HTTP timeout 
		httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HTTP_TIMEOUT);
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

		HttpResponse response = null;
		try {
			response = uploadImageToBing(imageFile, httpClient);
		} catch (IOException e) {
			logError(androidDebugMode, logAppName, "Error trying to upload and image", e);
		}

		if (response == null) {
			// error
			return null;
		}

		StringBuilder responseBody = new StringBuilder();
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity httpEntity = response.getEntity();
			if (httpEntity != null) {
				BufferedReader buffer;
				try {
					buffer = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));

					String line = null;
					while ((line = buffer.readLine()) != null) {
						responseBody.append(line);
					}
				} catch (Exception e) {
					logError(androidDebugMode, logAppName, "Error getting content of the response", e);
				}
			}
		}
		return responseBody.toString();
	}

	private HttpResponse uploadImageToBing(File imageFile, HttpClient httpClient) throws IOException {

		{

			HttpPost httpPost = new HttpPost("http://wp.bingvision.ar.glbdns.microsoft.com/ImageSearchV2.ashx");
			FileInputStream fis = new FileInputStream(imageFile);
			byte[] imageBytes = new byte[(int) imageFile.length()];
			fis.read(imageBytes, 0, (int) imageFile.length());
			ByteArrayEntity entity = new ByteArrayEntity(imageBytes);
			httpPost.setEntity(entity);
			httpPost.setHeader("Content-Type", "image/jpeg");
			httpPost.setHeader("Pragma", "no-cache");

			return executeHttpRequest(httpClient, httpPost);

		}

	}
}
