package pmag.snapandsearch.search;

import java.io.File;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;

import pmag.snapandsearch.SnapAndSearchAbstractActivity;
import pmag.snapandsearch.SnapAndSearchInterface;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SearchActivity extends SnapAndSearchAbstractActivity implements SnapAndSearchInterface {

    private class SearchTimer extends AsyncTask<String, Void, List<SearchResult>> {
        private String searchedImageFileName;
        private SearchImageHelper imageSearcher = new SearchImageHelper(true, MY_APP_NAME);

        @Override
        protected List<SearchResult> doInBackground(String... param) {
            // search by image
            searchedImageFileName = param[0];
            List<SearchResult> listSearchResult = null;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            // switch to run the search service checked in user preferences
            if (settings.getBoolean("prefServiceBing", false)) {
                listSearchResult = imageSearcher.searchBingByImage(new File(searchedImageFileName));
            } else if (settings.getBoolean("prefServiceTest", false)) {
                listSearchResult = imageSearcher.searchTestByImage(new File(searchedImageFileName));
            } else if (settings.getBoolean("prefServiceJustvisual", false)) {
                listSearchResult = imageSearcher.searchJustvisualByImage(new File(searchedImageFileName));
            }
            return listSearchResult;
        }

        @Override
        protected void onPostExecute(List<SearchResult> searchResults) {

            ListIterator<SearchResult> results;
            SearchResult result = null;
            // dismiss the progress circle
            ProgressBar progressCycle = (ProgressBar) findViewById(R.id.progress_bar_id);
            progressCycle.setVisibility(View.GONE);

            if (searchResults != null) {
                results = searchResults.listIterator();
                LinearLayout layout = (LinearLayout) findViewById(R.id.linear_layout_id);

                while (results.hasNext()) {
                    result = results.next();
                    // set the image component
                    if (result.getImage() != null) {
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setImageDrawable(Drawable.createFromPath(result.getImage().getPath()));
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        ViewGroup.LayoutParams imageParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                        imageView.setLayoutParams(imageParams);
                        layout.addView(imageView);
                    } else if (result.getImageUrl() != null) {
                        WebView webView = new WebView(getApplicationContext());
                        WebSettings webSettings = webView.getSettings();
                        webSettings.setLoadsImagesAutomatically(true);
                        webSettings.setSupportZoom(true);
                        webSettings.setBuiltInZoomControls(true);
                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.getSettings().setUseWideViewPort(true);
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.setWebViewClient(new WebViewClient());
                        webView.loadUrl(result.getImageUrl());
                        //TODO set display setting for webView
                        layout.addView(webView);
                    }
                    TextView text = null;
                    if (result.getImageName() != null) {
                        // set the text components
                        text = new TextView(getApplicationContext());
                        text.setText(result.getImageName());
                        text.setGravity(Gravity.CENTER_HORIZONTAL);
                        layout.addView(text);
                    }
                    if (result.getComment() != null) {
                        text = new TextView(getApplicationContext());
                        text.setText(result.getComment());
                        text.setGravity(Gravity.CENTER_HORIZONTAL);
                        layout.addView(text);
                    }
                    if (result.getUrl() != null) {
                        text = new TextView(getApplicationContext());
                        text.setText(result.getUrl());
                        //TODO make the tap to the link open a browser
                        //make the link(s) in the text clickable
                        text.setMovementMethod(LinkMovementMethod.getInstance());
                        text.setLinksClickable(true);
                        text.setGravity(Gravity.CENTER_HORIZONTAL);
                        layout.addView(text);
                    }
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
