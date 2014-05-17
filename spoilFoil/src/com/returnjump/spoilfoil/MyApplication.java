package com.returnjump.spoilfoil;

import com.parse.Parse;

import android.app.Application;
import android.content.Context;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Parse App ID and Client Key can be found in /res/values/secret.xml
	    Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));

        // Set default preference values if first time entering using the app
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

    public static View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
