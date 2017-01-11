
package com.noman.android.sip.preferences;

/**
 * Created by Noman on 12/30/2016.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.noman.android.sip.R;
import com.noman.android.sip.util.Protocol;


/**
 * Handles SIP authentication settings for the SIPer app.
 */
public class SipSettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static String TAG = "SipSettings";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Note that none of the preferences are actually defined here.
        // They're all in the XML file res/xml/preferences.xml.
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Intent intent = new Intent(Protocol.ACTION_BROADCAST_SIP_SETTING);
//        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "key: "+key+" | value: "+sharedPreferences.getString(key, ""));
    }
}
