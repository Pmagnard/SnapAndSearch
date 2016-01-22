package pmag.snapandsearch;

import pmag.snapandsearch.search.R;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Common behavior of the app activities
 * @author FR067458
 *
 */
public abstract class SnapAndSearchAbstractActivity extends Activity implements SnapAndSearchInterface {

	/**
	 * Adding menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		MenuItem menuItemVersion = menu.findItem(R.id.menuitem_version_id);
		String versionName = "";
		PackageInfo packageInfo = null;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(MY_APP_NAME, "Error trying to access the version name of the app", e);
		}
		if (packageInfo != null) {
			versionName = packageInfo.versionName;
		}
		menuItemVersion.setTitle("Version " + versionName);
		return true;
	}

}
