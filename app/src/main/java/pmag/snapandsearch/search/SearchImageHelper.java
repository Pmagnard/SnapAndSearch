package pmag.snapandsearch.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import pmag.snapandsearch.SnapAndSearchInterface;

public class SearchImageHelper {
	private final static List<String> XMLTAGS_TO_IGNORE = Arrays.asList("ARQueryMatches", "ProductMatches", "BarcodeMatches", "Thumbnail", "ProductId", "SearchQuery");
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
	private boolean isTagToBeDisplayed(String tagName) {

		return ((tagName!=null) &&(!XMLTAGS_TO_IGNORE.contains(tagName)));
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
	 * Create a test search result
	 * @param imageFile
	 * @return a list of fake results
	 */
	public List<SearchResult> searchTestByImage(File imageFile) {
		List<SearchResult> searchResults = new ArrayList();
		// first result
		SearchResult searchResult = new SearchResult();
		searchResult.setComment("Image 1");
		searchResult.setImage(imageFile);
		searchResult.setImageName(imageFile.getName());
		searchResult.setUrl("http://www.google.fr");
		searchResults.add(searchResult);
		// second result
		searchResult = new SearchResult();
		searchResult.setComment("Image 2");
		searchResult.setImage(imageFile);
		searchResult.setImageName(imageFile.getName());
		searchResult.setUrl("http://www.google.com");
		searchResults.add(searchResult);
		// third result
		searchResult = new SearchResult();
		searchResult.setComment("Image 3");
		searchResult.setImage(imageFile);
		searchResult.setImageName(imageFile.getName());
		searchResult.setUrl("http://www.google.com");
		searchResults.add(searchResult);
		// fourth result
		searchResult = new SearchResult();
		searchResult.setComment("Image 4");
		searchResult.setImage(imageFile);
		searchResult.setImageName(imageFile.getName());
		searchResult.setUrl("http://www.google.com");
		searchResults.add(searchResult);
		return searchResults;

	}

	/**
	 * search by image using Microsoft Bing service
	 *
	 * @param imageFile
	 */
	public List<SearchResult> searchBingByImage(File imageFile) {
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
		return parseXMLResult(responseBody.toString());
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

	private List<SearchResult> parseXMLResult(String searchStringResult) {
        List<SearchResult> searchResults = new ArrayList<>();
        StringBuffer textBuffer = new StringBuffer();
         String tagName=null;

        XmlPullParserFactory xmlParserFactory;
        XmlPullParser xmlParser;
        try {
            xmlParserFactory = XmlPullParserFactory.newInstance();
            xmlParserFactory.setNamespaceAware(true);
             xmlParser = xmlParserFactory.newPullParser();
            xmlParser.setInput(new StringReader(searchStringResult));
        } catch (XmlPullParserException e) {
            Log.e(SnapAndSearchInterface.MY_APP_NAME, "error parsing XML file", e);
             return null;
        }


         int eventType;
         try {
             eventType = xmlParser.getEventType();
         } catch (XmlPullParserException e) {
             Log.e(SnapAndSearchInterface.MY_APP_NAME, "error parsing XML file", e);
             return null;
         }
         while (eventType != XmlPullParser.END_DOCUMENT) {
             if (eventType == XmlPullParser.START_DOCUMENT) {
                 Log.i(SnapAndSearchInterface.MY_APP_NAME, "Start document");
             } else if (eventType == XmlPullParser.START_TAG) {
                 tagName = xmlParser.getName();
                 Log.i(SnapAndSearchInterface.MY_APP_NAME, "Start tag " + tagName);
                 if (isTagToBeDisplayed(tagName)) {
                    SearchResult searchResult = new SearchResult();

                     textBuffer.append("** " + tagName);
                     textBuffer.append(System.getProperty("line.separator"));

                     //show attributes
                     for (int n=0; n < xmlParser.getAttributeCount(); n++) {
                         textBuffer.append(xmlParser.getAttributeName(n) + ": ");
                         textBuffer.append(xmlParser.getAttributeValue(n)+ System.getProperty("line.separator"));
                     }
                    searchResult.setComment(textBuffer.toString());
                    searchResults.add(searchResult);
                 }
             } else if (eventType == XmlPullParser.END_TAG) {
                 Log.i(SnapAndSearchInterface.MY_APP_NAME, "End tag " + xmlParser.getName());
             } else if (eventType == XmlPullParser.TEXT) {
                 Log.i(SnapAndSearchInterface.MY_APP_NAME, "Text " + xmlParser.getText());
                 if (isTagToBeDisplayed(tagName)) {
                     textBuffer.append(xmlParser.getText());
                     textBuffer.append(System.getProperty("line.separator"));
                 }
             }
             try {
                 eventType = xmlParser.next();
             } catch (Exception e) {
                 Log.e(SnapAndSearchInterface.MY_APP_NAME, "error parsing XML file", e);
                 return null;
             }
         }
         Log.i(SnapAndSearchInterface.MY_APP_NAME, "End document");
         return searchResults;
    }
}
