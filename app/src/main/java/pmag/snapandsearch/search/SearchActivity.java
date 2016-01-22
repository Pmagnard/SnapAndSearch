package pmag.snapandsearch.search;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import pmag.snapandsearch.SnapAndSearchAbstractActivity;
import pmag.snapandsearch.SnapAndSearchInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class SearchActivity extends SnapAndSearchAbstractActivity implements SnapAndSearchInterface {
	/* List of XML tags to ignore in the result of the search */
	private final static List<String> XMLTAGS_TO_IGNORE = Arrays.asList("ARQueryMatches", "ProductMatches", "BarcodeMatches", "Thumbnail", "ProductId", "SearchQuery");
	
	private class SearchTimer extends AsyncTask<String, Void, String> {
		private String imageFileName;
		
		@Override
		protected String doInBackground(String... param) {
			// search by image
			SearchImageHelper imageSearcher = new SearchImageHelper(true, MY_APP_NAME);
			imageFileName = param[0];
			return imageSearcher.searchBingByImage(new File(imageFileName));
		}
		private boolean isTagToBeDisplayed(String tagName) {
			
			return ((tagName!=null) &&(!XMLTAGS_TO_IGNORE.contains(tagName)));
		}
		@Override
		protected void onPostExecute(String searchResult) {

			
			String parsedSearchResult = null;
			if (searchResult != null) {
				Log.i(MY_APP_NAME, searchResult );
				parsedSearchResult= parseXMLResult(searchResult);
			}

		
			//parsing the resulting XML 
			if ((searchResult != null) && (searchResult.length() > 0)) {
				if ((parsedSearchResult == null) || (parsedSearchResult.length()== 0)) {
					parsedSearchResult = "NO MATCH";
				}
			}
			else {
				parsedSearchResult = "NO INTERNET CONNECTION";
			}
			//displaying the result
			TextView textView = (TextView) findViewById(R.id.text_result_id);
			//textView.setMovementMethod(new ScrollingMovementMethod()); // make the text area scrollable
			textView.setText(parsedSearchResult);
					

			
			// dismiss the progress circle
			ProgressBar progressCycle = (ProgressBar) findViewById(R.id.progress_bar_id);
			progressCycle.setVisibility(View.GONE);
			
			//set the image in the image view
			ImageView imageView = (ImageView) findViewById(R.id.image_view_id);
			imageView.setImageDrawable(Drawable.createFromPath(imageFileName));
			
			//remove the file now
			File delFile = new File(imageFileName);
			Log.d(MY_APP_NAME, "File size is " + delFile.length() + " bytes");
			if (!delFile.delete()) {
				Log.i(MY_APP_NAME, "Image file couldn't be deleted");
			}
			
		}
		
		private String parseXMLResult(String searchResult) {
			StringBuffer textBuffer = new StringBuffer();
	 		String tagName=null;

			XmlPullParserFactory xmlParserFactory;
			XmlPullParser xmlParser;
			try {
				xmlParserFactory = XmlPullParserFactory.newInstance();
				xmlParserFactory.setNamespaceAware(true);
		 		xmlParser = xmlParserFactory.newPullParser();
		        xmlParser.setInput(new StringReader(searchResult));
			} catch (XmlPullParserException e) {
				Log.e(MY_APP_NAME, "error parsing XML file", e);
 				return null;
			}
		
	 		
	 		int eventType;
	 		try {
	 			eventType = xmlParser.getEventType();
	 		} catch (XmlPullParserException e) {
	 			Log.e(MY_APP_NAME, "error parsing XML file", e);
	 			return null;
	 		}
	 		while (eventType != XmlPullParser.END_DOCUMENT) {
	 			if (eventType == XmlPullParser.START_DOCUMENT) {
	 				Log.i(MY_APP_NAME, "Start document");
	 			} else if (eventType == XmlPullParser.START_TAG) {
	 				tagName = xmlParser.getName();
	 				Log.i(MY_APP_NAME, "Start tag " + tagName);
	 				if (isTagToBeDisplayed(tagName)) {
	 					textBuffer.append("** " + tagName);
	 					textBuffer.append(System.getProperty("line.separator"));
	 					//show attributes
	 					for (int n=0; n < xmlParser.getAttributeCount(); n++) {
	 						textBuffer.append(xmlParser.getAttributeName(n) + ": ");
	 						textBuffer.append(xmlParser.getAttributeValue(n)+ System.getProperty("line.separator"));
	 					}
	 				}
	 			} else if (eventType == XmlPullParser.END_TAG) {
	 				Log.i(MY_APP_NAME, "End tag " + xmlParser.getName());
	 			} else if (eventType == XmlPullParser.TEXT) {
	 				Log.i(MY_APP_NAME, "Text " + xmlParser.getText());
	 				if (isTagToBeDisplayed(tagName)) {
	 					textBuffer.append(xmlParser.getText());
	 					textBuffer.append(System.getProperty("line.separator"));
	 				}
	 			}
	 			try {
	 				eventType = xmlParser.next();
	 			} catch (Exception e) {
	 				Log.e(MY_APP_NAME, "error parsing XML file", e);
	 				return null;
	 			}
	 		}
	 		Log.i(MY_APP_NAME, "End document");
	 		return textBuffer.toString();
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		Log.d(MY_APP_NAME, "Entering search and result activity");
		String resultImageFilePath = getIntent().getStringExtra(RESULT_FILE_NAME_PARAM);
		Log.i(MY_APP_NAME, "Result file name is " + resultImageFilePath);

		new SearchTimer().execute(resultImageFilePath);
		
		
	}
	
}
