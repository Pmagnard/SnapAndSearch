package pmag.snapandsearch.search;

import java.io.File;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import pmag.snapandsearch.SnapAndSearchAbstractActivity;
import pmag.snapandsearch.SnapAndSearchInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SearchActivity extends SnapAndSearchAbstractActivity implements SnapAndSearchInterface {
	/* List of XML tags to ignore in the result of the search */
	/* available mode are : Test, JustVisual, GoogleVision, BingVisual */
	private static String switchMode = "Test";


	
	private class SearchTimer extends AsyncTask<String, Void, List<SearchResult>> {
		private String searchedImageFileName;
		private SearchImageHelper imageSearcher = new SearchImageHelper(true, MY_APP_NAME);

		@Override
		protected List<SearchResult> doInBackground(String... param) {
			// search by image
			searchedImageFileName = param[0];
			List<SearchResult> listSearchResult = null;
			switch (switchMode) {
				case "Test":
					listSearchResult = imageSearcher.searchTestByImage(new File(searchedImageFileName));
					// no real search
				break;
				case "BingVisual":
					listSearchResult = imageSearcher.searchBingByImage(new File(searchedImageFileName));
					break;
			}
			return listSearchResult;
		}

		@Override
		protected void onPostExecute(List<SearchResult> searchResults) {

			ListIterator<SearchResult> results = searchResults.listIterator();
			SearchResult result= null;
			// dismiss the progress circle
			ProgressBar progressCycle = (ProgressBar) findViewById(R.id.progress_bar_id);
			progressCycle.setVisibility(View.GONE);

			if (results != null) {
				LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.linear_layout_id);

				while (results.hasNext()) {
					result = results.next();
					LinearLayout layout = new LinearLayout(getApplicationContext());
					layout.setOrientation(LinearLayout.VERTICAL);
					// get current screen width
					Display display = getWindowManager().getDefaultDisplay();
					int screenWidth= display.getWidth();
					// set width and height of the layout - layout is horizontally stretched to screen width
					// layout content is centered
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth, LinearLayout.LayoutParams.FILL_PARENT);
					params.gravity= Gravity.CENTER;
					layout.setLayoutParams(params);

					// set the image component
					ImageView imageView = new ImageView(getApplicationContext());
					imageView.setImageDrawable(Drawable.createFromPath(result.getImage().getPath()));
					//ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
					//imageView.setLayoutParams(params);
					layout.addView(imageView);

					// set the text components
					TextView text = new TextView(getApplicationContext());
					text.setText(result.getImageName());
					text.setGravity(Gravity.CENTER_HORIZONTAL);
					layout.addView(text);

					text = new TextView(getApplicationContext());
					text.setText(result.getComment());
					text.setGravity(Gravity.CENTER_HORIZONTAL);
					layout.addView(text);

					text = new TextView(getApplicationContext());
					text.setText(result.getUrl());
					text.setLinksClickable(true);
					text.setGravity(Gravity.CENTER_HORIZONTAL);
					layout.addView(text);



					verticalLayout.addView(layout);
				}

			}
			//remove the file now
			File delFile = new File(searchedImageFileName);
			Log.d(MY_APP_NAME, "File size is " + delFile.length() + " bytes");
			if (!delFile.delete()) {
				Log.i(MY_APP_NAME, "Image file couldn't be deleted");
			}
			
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
