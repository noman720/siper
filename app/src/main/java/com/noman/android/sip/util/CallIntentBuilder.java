package com.noman.android.sip.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;

/**
 * Created by noman on 1/25/17.
 */

public class CallIntentBuilder {



    private Uri mUri;
    private int mCallInitiationType;
    private PhoneAccountHandle mPhoneAccountHandle;
    private boolean mIsVideoCall = false;

    public CallIntentBuilder(Uri uri) {
        mUri = uri;
    }
    public CallIntentBuilder(String number) {
        this(PhoneNumberUtil.getCallUri(number));
    }
    public CallIntentBuilder setCallInitiationType(int initiationType) {
        mCallInitiationType = initiationType;
        return this;
    }
    public CallIntentBuilder setPhoneAccountHandle(PhoneAccountHandle accountHandle) {
        mPhoneAccountHandle = accountHandle;
        return this;
    }
    public CallIntentBuilder setIsVideoCall(boolean isVideoCall) {
        mIsVideoCall = isVideoCall;
        return this;
    }
    public Intent build() {
        return getCallIntent(
                mUri,
                mPhoneAccountHandle,
                mIsVideoCall ? VideoProfile.STATE_BIDIRECTIONAL : VideoProfile.STATE_AUDIO_ONLY,
                mCallInitiationType);
    }

    /**
     * Create a call intent that can be used to place a call.
     *
     * @param uri Address to place the call to.
     * @param accountHandle {@link PhoneAccountHandle} to place the call with.
     * @param videoState Initial video state of the call.
     * @param callIntiationType The UI affordance the call was initiated by.
     * @return Call intent with provided extras and data.
     */
    public static Intent getCallIntent(
            Uri uri, PhoneAccountHandle accountHandle, int videoState, int callIntiationType) {
        final Intent intent = new Intent(Intent.ACTION_CALL, uri);
        intent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, videoState);
        final Bundle b = new Bundle();
        b.putInt(Messages.TAG_CALL_INITIATION_TYPE, callIntiationType);
        intent.putExtra(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, b);
        if (accountHandle != null) {
            intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle);
        }
        return intent;
    }



}
