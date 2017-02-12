package com.noman.android.sip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.noman.android.sip.service.SipService;
import com.noman.android.sip.util.PhoneNumberUtil;

/**
 * Created by noman on 1/19/17.
 */

public class OutgoingCall extends BroadcastReceiver {
    private static final String TAG = "OutgoingCall";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "OutgoingCall::onReceive for SIPer");
        // No need to check permission here as we are always fired with correct permission

        String action = intent.getAction();
        String number = getResultData();
        //String full_number = intent.getStringExtra("android.phone.extra.ORIGINAL_URI");
        // Escape if no number
        if (number == null) {
            return;
        }

        // If emergency number transmit as if we were not there
        if(PhoneNumberUtils.isEmergencyNumber(number)) {
            setResultData(number);
            return;
        }


        if(!Intent.ACTION_NEW_OUTGOING_CALL.equals(action)) {
            Log.e(TAG, "Not launching with correct action ! Do not process");
            setResultData(number);
            return;
        }

        //if SIPer is registered then
        //check the number is uri or not
        //if not then place call via SIPer
        //else ignore it

        // If this is an outgoing call with a valid number
//        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL) ) {
//            if (!PhoneNumberUtil.isUriNumber(number)){
//                log("Calling from native dialer");
//                SipService.outgoingCall(context, number+"@brotecs.com");
//            }
//
//            // We will treat this by ourselves
//            setResultData(null);
//            return;
//        }

        // Pass the call to pstn handle
        setResultData(number);
        return;
    }

    static void log(String msg) {
        Log.w(TAG, msg);
    }
}
