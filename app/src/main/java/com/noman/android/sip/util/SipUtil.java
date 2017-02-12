package com.noman.android.sip.util;


/**
 * Created by noman on 1/29/17.
 */

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

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;

import com.noman.android.sip.R;
import com.noman.android.sip.service.SIPerConnectionService;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
public class SipUtil {

    static SipProfile profile = null;

    static final String LOG_TAG = "SIP";
    static final String EXTRA_INCOMING_CALL_INTENT =
            "com.android.services.telephony.sip.incoming_call_intent";
    static final String EXTRA_PHONE_ACCOUNT =
            "com.android.services.telephony.sip.phone_account";
    private SipUtil() {
    }

    /**
     * Creates a {@link PhoneAccountHandle} from the specified SIP URI.
     */
    static PhoneAccountHandle createAccountHandle(Context context, String sipUri) {
        return new PhoneAccountHandle(
                new ComponentName(context, SIPerConnectionService.class), sipUri);
    }

    /**
     * Creates a {@link PhoneAccountHandle} from the specified SIP URI.
     */
    public static PhoneAccountHandle getAccountHandle(Context context) {
        return new PhoneAccountHandle(
                new ComponentName(context, SIPerConnectionService.class), profile.getUriString());
    }


    /**
     * Determines the SIP Uri for a specified {@link PhoneAccountHandle}.
     *
     * @param phoneAccountHandle The {@link PhoneAccountHandle}.
     * @return The SIP Uri.
     */
    public static String getSipUriFromPhoneAccount(PhoneAccountHandle phoneAccountHandle) {
        if (phoneAccountHandle == null) {
            return null;
        }
        String sipUri = phoneAccountHandle.getId();
        if (TextUtils.isEmpty(sipUri)) {
            return null;
        }
        return sipUri;
    }
    /**
     * Creates a PhoneAccount for a SipProfile.
     *
     * @param context The context
     * @return The PhoneAccount.
     */
    public static PhoneAccount createPhoneAccount(Context context) {
        try {
            profile = new SipProfile.Builder(
                    "6003",
                    "192.168.30.247")
                    .setProfileName("6003")
                    .setPassword("9876")
                    .setDisplayName("6003")
                    .setSendKeepAlive(true)
                    .setAutoRegistration(true)
                    .build();
        }catch (ParseException e){
            e.printStackTrace();
        }


        PhoneAccountHandle accountHandle =
                SipUtil.createAccountHandle(context, profile.getUriString());
        final ArrayList<String> supportedUriSchemes = new ArrayList<String>();
        supportedUriSchemes.add(PhoneAccount.SCHEME_SIP);
        supportedUriSchemes.add(PhoneAccount.SCHEME_TEL);
        PhoneAccount.Builder builder = PhoneAccount.builder(accountHandle, profile.getDisplayName())
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
                .setAddress(Uri.parse(profile.getUriString()))
                .setShortDescription(profile.getDisplayName())
                .setIcon(Icon.createWithResource(
                        context, R.drawable.icon))
                .setSupportedUriSchemes(supportedUriSchemes);
        return builder.build();
    }

}