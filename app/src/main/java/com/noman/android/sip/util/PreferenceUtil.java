package com.noman.android.sip.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Noman on 12/30/2016.
 */

public class PreferenceUtil {

    private static SharedPreferences sharedPreferences  = null;

    public static void init(Context context){
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static void setCallingAccount(boolean isCreated){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("has_phone_account", isCreated);
        editor.commit();
    }

    public static boolean hasPhoneAccount(){
        return sharedPreferences.getBoolean("has_phone_account", false);
    }

    public static void setSipAccount(boolean isCreated){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("has_sip_account", isCreated);
        editor.commit();
    }

    public static boolean hasSipAccount(){
        return sharedPreferences.getBoolean("has_sip_account", false);
    }

    public static void setSipRegistered(boolean isRegistered){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_sip_registered", isRegistered);
        editor.commit();
    }

    public static boolean isSipRegistered(){
        return sharedPreferences.getBoolean("is_sip_registered", false);
    }

}
