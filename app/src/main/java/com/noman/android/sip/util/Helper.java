package com.noman.android.sip.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.noman.android.sip.model.SipUser;

/**
 * Created by User on 12/30/2016.
 */

public class Helper {

    public static SipUser getSipUser(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = prefs.getString("namePref", "");
        String domain = prefs.getString("domainPref", "");
        String password = prefs.getString("passPref", "");

        SipUser sipUser = new SipUser();
        sipUser.setName(username);
        sipUser.setDomain(domain);
        sipUser.setPassword(password);

        return sipUser;
    }
}
