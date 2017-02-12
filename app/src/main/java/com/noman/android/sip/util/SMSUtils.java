package com.noman.android.sip.util;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 * Created by noman on 1/17/17.
 */

public class SMSUtils {

    private static final String TAG = "SMSUtils";

    public static void putSMS(Context context){
        ContentValues values = new ContentValues();
        values.put("address", "+12345678"); // phone number to send
        values.put("date", System.currentTimeMillis()+"");
        values.put("read", "1"); // if you want to mark is as unread set to 0
        values.put("type", "2"); // 2 means sent message
        values.put("body", "This is my message!");

        Uri uri = Uri.parse("content://sms/inbox");
        Uri rowUri = context.getContentResolver().insert(uri,values);

        Log.d(TAG, "SMS_INSERT_URI: " + rowUri.toString());

    }

}
