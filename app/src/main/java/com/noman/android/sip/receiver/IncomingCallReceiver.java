/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.noman.android.sip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.util.Log;

import com.example.android.sip.WalkieTalkieActivity;
import com.noman.android.sip.activity.MainActivity;
import com.noman.android.sip.service.SipService;
import com.noman.android.sip.util.Protocol;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";

    /**
     * Processes the incoming sipAudioCall, answers it, and hands it over to the
     * MainActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //got incoming call, pass it to service
        Log.d(TAG, "Incoming call.....");
        SipService.sipIncomingCall(context, intent);

//        SipAudioCall incomingCall = null;
//        try {
//
//            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
//                @Override
//                public void onRinging(SipAudioCall call, SipProfile caller) {
//                    try {
//                        call.answerCall(30);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//
//            Log.d(TAG, "Incoming call.....");
//
//            MainActivity mainActivity = (MainActivity) context;
//
//            incomingCall = mainActivity.manager.takeAudioCall(intent, listener);
//            incomingCall.answerCall(30);
//            incomingCall.startAudio();
//            incomingCall.setSpeakerMode(true);
//            if(incomingCall.isMuted()) {
//                incomingCall.toggleMute();
//            }
//
//            mainActivity.call = incomingCall;
//
//            mainActivity.updateStatus(incomingCall);
//
//        } catch (Exception e) {
//            if (incomingCall != null) {
//                incomingCall.close();
//            }
//        }
    }

}
