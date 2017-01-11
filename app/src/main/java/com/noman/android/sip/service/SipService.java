package com.noman.android.sip.service;

/**
 * Created by Noman on 12/30/2016.
 */

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;

import com.noman.android.sip.R;
import com.noman.android.sip.receiver.IncomingCallReceiver;
import com.noman.android.sip.util.Messages;
import com.noman.android.sip.util.PreferenceUtil;
import com.noman.android.sip.util.Protocol;

import java.text.ParseException;
import java.util.Arrays;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SipService extends IntentService {

    private static final String TAG = "SipService";

    private static final String EXTRA_SIP_USER = "com.noman.android.sip.service.extra.SIP_USER";
    private static final String EXTRA_SIP_DOMAIN = "com.noman.android.sip.service.extra.SIP_DOMAIN";
    private static final String EXTRA_SIP_PASS = "com.noman.android.sip.service.extra.SIP_PASS";
    private static final String EXTRA_CALLING_ACC_NAME = "com.noman.android.sip.service.extra.CALLING_ACC_NAME";
    private static final String EXTRA_CALLING_ACC_DESC = "com.noman.android.sip.service.extra.CALLING_ACC_DESC";
    private static final String EXTRA_REMOTE_PEER = "com.noman.android.sip.service.extra.REMOTE_PEER";

    public SipService() {
        super("SipService");
    }

    /**
     * Static helper method for registering calling account method
     * @param context
     * @param accName
     * @param shortDesc
     */
    public static void registerCallingAccount(Context context, String accName, String shortDesc) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_REGISTER_CALLING_ACCOUNT);
        intent.putExtra(EXTRA_CALLING_ACC_NAME, accName);
        intent.putExtra(EXTRA_CALLING_ACC_DESC, shortDesc);
        context.startService(intent);
    }

    /**
     * Static helper method for uregistering calling account method
     * @param context
     * @param accName
     * @param shortDesc
     */
    public static void unregisterCallingAccount(Context context, String accName, String shortDesc) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_UNREGISTER_CALLING_ACCOUNT);
        intent.putExtra(EXTRA_CALLING_ACC_NAME, accName);
        context.startService(intent);
    }

    /**
     * Static helper method for adding sip account
     * @param context
     * @param user
     * @param domain
     * @param password
     */
    public static void addSipAccount(Context context, String user, String domain, String password) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_ADD_SIP_ACCOUNT);
        intent.putExtra(EXTRA_SIP_USER, user);
        intent.putExtra(EXTRA_SIP_DOMAIN, domain);
        intent.putExtra(EXTRA_SIP_PASS, password);
        context.startService(intent);
    }

    /**
     * Static helper method for removing sip account
     * @param context
     */
    public static void removeSipAccount(Context context) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_REMOVE_SIP_ACCOUNT);
        context.startService(intent);
    }

    /**
     * Static helper method for registering sip account
     * @param context
     */
    public static void registerSipAccount(Context context) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_REGISTER_SIP_ACCOUNT);
        context.startService(intent);
    }

    /**
     * Static helper method for unregistering sip account
     * @param context
     */
    public static void unregisterSipAccount(Context context) {
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_UNREGISTER_SIP_ACCOUNT);
        context.startService(intent);
    }


    /**
     * Static helper method for incoming call
     * @param context
     * @param incomingCallIntent
     */
    private static BroadcastReceiver callScreenEventsReceiver = null;
    public static void incomingCall(final Context context, Intent incomingCallIntent) {
        if (sipManager == null)
            return;

        try {
            final SipAudioCall sipAudioCall = sipManager.takeAudioCall(incomingCallIntent, new SipAudioCall.Listener() {
                @Override
                public void onReadyToCall(SipAudioCall call) {
                    super.onReadyToCall(call);
                    Log.d(TAG, "onReadyToCall");
                }

                @Override
                public void onCalling(SipAudioCall call) {
                    super.onCalling(call);
                    Log.d(TAG, "onCalling");
                }

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    super.onRinging(call, caller);
                    Log.d(TAG, "onRinging");
                }

                @Override
                public void onRingingBack(SipAudioCall call) {
                    super.onRingingBack(call);
                    Log.d(TAG, "onRingingBack");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    Log.d(TAG, "onCallEstablished");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    super.onCallEnded(call);
                    Log.d(TAG, "onCallEnded");
                    sendLocalBroadcast(Messages.SIP_TO_TEL_EXTRA_END_CALL);
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(callScreenEventsReceiver);
                }

                @Override
                public void onCallBusy(SipAudioCall call) {
                    super.onCallBusy(call);
                    Log.d(TAG, "onCallBusy");
                }

                @Override
                public void onCallHeld(SipAudioCall call) {
                    super.onCallHeld(call);
                    Log.d(TAG, "onCallHeld");
                }

                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    super.onError(call, errorCode, errorMessage);
                    Log.d(TAG, "onError=> errorCode: "+errorCode+" | errorMessage: "+errorMessage);
                }

                @Override
                public void onChanged(SipAudioCall call) {
                    super.onChanged(call);
                    Log.d(TAG, "onChanged");
                }

                public void sendLocalBroadcast(int action){
                    Intent intent = new Intent(Protocol.INFO_BROADCAST_SIP_TO_TEL);
                    intent.putExtra(Messages.TAG_SIP_TO_TEL_EXTRA, action);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            });


            callScreenEventsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "callScreenEventsReceiver: received!");
                    int extra = intent.getIntExtra(Messages.TAG_TEL_TO_SIP_EXTRA, -1);
                    switch (extra){
                        case Messages.TEL_TO_SIP_EXTRA_ANSWER:
                            try {
                                sipAudioCall.answerCall(30);
                                sipAudioCall.startAudio();
                            } catch (SipException se){
                                se.printStackTrace();
                            }
                            break;

                        case Messages.TEL_TO_SIP_EXTRA_DISCONNECT:
                            try {
                                sipAudioCall.endCall();
                            } catch (SipException se){
                                se.printStackTrace();
                            }
                            break;

                        case Messages.TEL_TO_SIP_EXTRA_REJECT:
                            try {
                                sipAudioCall.endCall();
                            } catch (SipException se){
                                se.printStackTrace();
                            }
                            break;

                        case Messages.TEL_TO_SIP_EXTRA_HOLD:
                            try {
                                sipAudioCall.holdCall(30);
                            } catch (SipException se){
                                se.printStackTrace();
                            }
                            break;

                        case Messages.TEL_TO_SIP_EXTRA_UNHOLD:
                            try {
                                sipAudioCall.continueCall(30);
                            } catch (SipException se){
                                se.printStackTrace();
                            }
                            break;
                    }

                }
            };

            LocalBroadcastManager.getInstance(context).registerReceiver(
                    callScreenEventsReceiver, new IntentFilter(Protocol.INFO_BROADCAST_TEL_TO_SIP));

            //open incoming call screen
            Intent intent = new Intent(context, SipService.class);
            intent.setAction(Protocol.ACTION_INCOMING_CALL);
            intent.putExtra(EXTRA_REMOTE_PEER, sipAudioCall.getPeerProfile().getUriString());
            context.startService(intent);
        } catch (SipException se) {
            se.printStackTrace();
        }

//        //TODO: test
//        Intent intent = new Intent(context, SipService.class);
//        intent.setAction(Protocol.ACTION_INCOMING_CALL);
//        intent.putExtra(EXTRA_REMOTE_PEER, "sip:6001@192.168.0.45");
//        context.startService(intent);

    }


    /**
     * Static helper method for outgoing call
     * @param context
     * @param uri
     */
    public static void outgoingCall(Context context, String uri){
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_OUTGOING_CALL);
        intent.putExtra(EXTRA_REMOTE_PEER, uri);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Protocol.ACTION_REGISTER_CALLING_ACCOUNT.equals(action)) {
                final String accName = intent.getStringExtra(EXTRA_CALLING_ACC_NAME);
                final String shortDesc = intent.getStringExtra(EXTRA_CALLING_ACC_DESC);
                handleRegisterCallingAccount(accName, shortDesc);
            } else if (Protocol.ACTION_UNREGISTER_CALLING_ACCOUNT.equals(action)) {
                final String accName = intent.getStringExtra(EXTRA_CALLING_ACC_NAME);
                handleUnregisterCallingAccount(accName);
            } else if (Protocol.ACTION_ADD_SIP_ACCOUNT.equals(action)) {
                final String user = intent.getStringExtra(EXTRA_SIP_USER);
                final String domain = intent.getStringExtra(EXTRA_SIP_DOMAIN);
                final String password = intent.getStringExtra(EXTRA_SIP_PASS);
                handleAddSipAccount(user, domain, password);
            } else if (Protocol.ACTION_REMOVE_SIP_ACCOUNT.equals(action)) {
                handleRemoveSipAccount();
            } else if (Protocol.ACTION_REGISTER_SIP_ACCOUNT.equals(action)){
                handleRegisterSipAccount();
            } else if (Protocol.ACTION_UNREGISTER_SIP_ACCOUNT.equals(action)){
                handleUnregisterSipAccount();
            } else if (Protocol.ACTION_INCOMING_CALL.equals(action)) {
                final String remotePeer = intent.getStringExtra(EXTRA_REMOTE_PEER);
                sendIncomingCallIntent(getApplicationContext(), Uri.parse(remotePeer));
            } else if (Protocol.ACTION_OUTGOING_CALL.equals(action)){
                final String remotePeer = intent.getStringExtra(EXTRA_REMOTE_PEER);
                sendOutgoingCallIntent(getApplicationContext(), remotePeer);
            } else if (Protocol.ACTION_TEST.equals(action)){
                handleCheckCallingAccounts();
            }
        }
    }

    /**
     * Handle action ACTION_REGISTER_CALLING_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleRegisterCallingAccount(String accName, String shortDesc) {
        Log.d(TAG, "handleRegisterCallingAccount: " + accName + " | " + shortDesc);
        Intent intent = new Intent(Protocol.INFO_BROADCAST_CALLING_ACCOUNT);
        try {
            Context context = getApplicationContext();
            TelecomManager telecomManager =
                    (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);

            PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(
                    new ComponentName(getApplicationContext(), SipTelConnectionService.class),
                    Protocol.ID_CALL_PROVIDER);


            //Register phone account
            telecomManager.registerPhoneAccount(PhoneAccount.builder(
                    phoneAccountHandle,
                    accName)
                    .setAddress(Uri.parse(shortDesc))
                    .setSubscriptionAddress(Uri.parse(shortDesc))
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER |
                            PhoneAccount.CAPABILITY_VIDEO_CALLING)
                    .setHighlightColor(Color.GREEN)
                    // TODO: Add icon tint (Color.RED)
                    .setIcon(Icon.createWithResource(
                            context, R.drawable.icon))
                    .setShortDescription(shortDesc)
                    .setSupportedUriSchemes(Arrays.asList("tel, sip"))
                    .build());

            intent.putExtra(Messages.TAG_CALLING_ACCOUNT_STATUS, Messages.CALLING_ACCOUNT_REGISTER_STATUS_SUCCESS);
            PreferenceUtil.setCallingAccount(true);
            Log.i(TAG, "registerPhoneAccount...");
        } catch (SecurityException se) {
            se.printStackTrace();
            intent.putExtra(Messages.TAG_CALLING_ACCOUNT_STATUS, Messages.CALLING_ACCOUNT_REGISTER_STATUS_FAIL);
            PreferenceUtil.setCallingAccount(false);
        }

        //send status to ui
        sendByBroadcast(intent);
//        throw new UnsupportedOperationException("handleRegisterCallingAccount: Not yet implemented");
    }

    /**
     * Handle action ACTION_UNREGISTER_CALLING_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleUnregisterCallingAccount(String accName) {
        Log.d(TAG, "handleUnregisterCallingAccount: " + accName);
        Intent intent = new Intent(Protocol.INFO_BROADCAST_CALLING_ACCOUNT);
        try {
            TelecomManager telecomManager =
                    (TelecomManager) getApplicationContext().getSystemService(Context.TELECOM_SERVICE);

            PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(
                    new ComponentName(getApplicationContext(), SipTelConnectionService.class),
                    Protocol.ID_CALL_PROVIDER);

            //Unregister phone account
            telecomManager.unregisterPhoneAccount(phoneAccountHandle);

            intent.putExtra(Messages.TAG_CALLING_ACCOUNT_STATUS, Messages.CALLING_ACCOUNT_UNREGISTER_STATUS_SUCCESS);
            PreferenceUtil.setCallingAccount(false);
            Log.i(TAG, "unregisterPhoneAccount...");
        } catch (SecurityException se) {
            se.printStackTrace();
            intent.putExtra(Messages.TAG_CALLING_ACCOUNT_STATUS, Messages.CALLING_ACCOUNT_UNREGISTER_STATUS_FAIL);
        }

        //send status to ui
        sendByBroadcast(intent);
//        throw new UnsupportedOperationException("handleRegisterCallingAccount: Not yet implemented");
    }


    /***********************************************************************************************/
    /**************************** SIP **************************************************************/
    /***********************************************************************************************/
    private static SipManager sipManager = null;
    private static SipProfile sipProfile = null;
    private static IncomingCallReceiver callReceiver = null;
    private static Handler mHandler = null;

    public static void initSipManager(Context context) {
        if (SipManager.isVoipSupported(context) && SipManager.isApiSupported(context)) {
            if (sipManager == null) {
                sipManager = SipManager.newInstance(context);
                Log.d(TAG, "sipManager init....");

                // Set up the intent filter.  This will be used to fire an
                // IncomingCallReceiver when someone calls the SIP address used by this
                // application.
                IntentFilter filter = new IntentFilter();
                filter.addAction(Protocol.ACTION_RECEIVE_INCOMING_CALL);
                callReceiver = new IncomingCallReceiver();
                context.registerReceiver(callReceiver, filter);

                //handler
                mHandler = new Handler();

                //init preferences
                PreferenceUtil.setSipRegistered(false);
            }
        }

    }

    public static void destroySipManager(Context context) {
        if (SipManager.isVoipSupported(context) && SipManager.isApiSupported(context)) {
            if (sipManager != null) {
                try {
                    sipManager.close(sipProfile.getUriString());
                    Log.d(TAG, "destroy sipManager....");
                    context.unregisterReceiver(callReceiver);
                    sipProfile = null;
                } catch (SipException se){
                    se.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Handle action ACTION_ADD_SIP_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleAddSipAccount(String user, String domain, String password) {
        final Intent broadcastIntent = new Intent(Protocol.INFO_BROADCAST_SIP_ACCOUNT);
        try {
            if (sipManager == null)
                return;

            Log.d(TAG, "adding sipProfile: " + sipProfile);

            //if already exist profile, then close it
            if (sipProfile != null) {
                sipManager.close(sipProfile.getUriString());
                Thread.sleep(1000);
            }

            SipProfile.Builder builder = new SipProfile.Builder(user, domain);
            builder.setPassword(password);
            builder.setPort(5060);
//            builder.setProtocol("UDP");
            builder.setSendKeepAlive(true);
//            builder.setAutoRegistration(true);
            sipProfile = builder.build();

            Intent intent = new Intent();
            intent.setAction(Protocol.ACTION_RECEIVE_INCOMING_CALL);
            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, Intent.FILL_IN_DATA);
            //pass pending intent for incoming call
            sipManager.open(sipProfile, pi, null);

            Log.i(TAG, "SIP url: " + sipProfile.getUriString());
            Log.i(TAG, "isOpened: " + sipManager.isOpened(sipProfile.getUriString()));
            broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_CREATE_STATUS_SUCCESS);
            PreferenceUtil.setSipAccount(true);
        } catch (ParseException pe) {
            Log.d(TAG, "Connection Error.");
            pe.printStackTrace();
            broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_CREATE_STATUS_FAIL);
            PreferenceUtil.setSipAccount(false);
        } catch (SipException se) {
            Log.d(TAG, "Connection error.");
            se.printStackTrace();
            broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_CREATE_STATUS_FAIL);
            PreferenceUtil.setSipAccount(false);
        } catch (Exception e) {
            Log.d(TAG, "Connection error.");
            e.printStackTrace();
            broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_CREATE_STATUS_FAIL);
            PreferenceUtil.setSipAccount(false);
        }
//        throw new UnsupportedOperationException("handleAddSipAccount: Not yet implemented");

        //send status to ui
        sendByBroadcast(broadcastIntent);
    }

    /**
     * Handle action ACTION_REMOVE_SIP_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleRemoveSipAccount() {
        long delay = 0;
        //if already registered then, unregister first
        if (PreferenceUtil.isSipRegistered()){
            handleUnregisterSipAccount();
            delay = 2000;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent broadcastIntent = new Intent(Protocol.INFO_BROADCAST_SIP_ACCOUNT);
                try {
                    if (sipManager == null)
                        return;

                    Log.d(TAG, "sipProfile: " + sipProfile);

                    //if already exist profile, then close it
                    if (sipProfile != null) {
                        if (sipManager.isOpened(sipProfile.getUriString())){
                            sipManager.close(sipProfile.getUriString());
                        }
                    }
                    PreferenceUtil.setSipAccount(false);
                    broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_REMOVE_STATUS_SUCCESS);

                } catch (SipException se) {
                    Log.d(TAG, "Connection error.");
                    broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_REMOVE_STATUS_FAIL);
                    se.printStackTrace();
                } catch (Exception e) {
                    broadcastIntent.putExtra(Messages.TAG_SIP_ACCOUNT_STATUS, Messages.SIP_ACCOUNT_REMOVE_STATUS_FAIL);
                    e.printStackTrace();
                }
                //send status to ui
                sendByBroadcast(broadcastIntent);
            }
        }, delay);

    }


    /**
     * Handle action ACTION_REGISTER_SIP_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleRegisterSipAccount() {
        Log.d(TAG, "handleRegisterSipAccount");
        final Intent broadcastIntent = new Intent(Protocol.INFO_BROADCAST_SIP_REGISTRATION);
        try {
            if (sipManager == null)
                return;

            Log.d(TAG, "sipProfile: " + sipProfile);

            //if already exist profile, then close it
            if (sipProfile == null || !sipManager.isOpened(sipProfile.getUriString())) {
                return;
            }

            // This listener must be added AFTER sipManager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
            SipRegistrationListener sipRegistrationListener = new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    Log.d(TAG, "Registering with SIP Server...");
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_REGISTER_STATUS_TRYING);
                    sendByBroadcast(broadcastIntent);
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    Log.d(TAG, "Ready");
                    PreferenceUtil.setSipRegistered(true);
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_REGISTER_STATUS_SUCCESS);
                    sendByBroadcast(broadcastIntent);
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    Log.d(TAG, "Registration failed.  Please check settings.");
                    Log.e(TAG, "uri: " + localProfileUri + " | errorCode: " + errorCode + " | errorMessage: " + errorMessage);
                    PreferenceUtil.setSipRegistered(false);
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_REGISTER_STATUS_FAIL+" ["+errorMessage+"]");
                    sendByBroadcast(broadcastIntent);
                }
            };

            sipManager.register(sipProfile, 3600, sipRegistrationListener);
        } catch (SipException pe) {
            Log.d(TAG, "Connection Error.");
            pe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action ACTION_UNREGISTER_SIP_ACCOUNT in the provided background thread with the provided
     * parameters.
     */
    private void handleUnregisterSipAccount() {
        Log.d(TAG, "handleUnregisterSipAccount");
        final Intent broadcastIntent = new Intent(Protocol.INFO_BROADCAST_SIP_REGISTRATION);
        try {
            if (sipManager == null)
                return;

            Log.d(TAG, "sipProfile: " + sipProfile);

            // This listener must be added AFTER sipManager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
            SipRegistrationListener sipRegistrationListener = new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    Log.d(TAG, "Unregistering with SIP Server...");
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_UNREGISTER_STATUS_TRYING);
                    sendByBroadcast(broadcastIntent);
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    PreferenceUtil.setSipRegistered(false);
                    Log.d(TAG, "Unregistered");
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_UNREGISTER_STATUS_SUCCESS);
                    sendByBroadcast(broadcastIntent);
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    Log.d(TAG, "Registration failed.  Please check settings.");
                    Log.e(TAG, "uri: " + localProfileUri + " | errorCode: " + errorCode + " | errorMessage: " + errorMessage);
                    broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_UNREGISTER_STATUS_FAIL+" ["+errorMessage+"]");
                    sendByBroadcast(broadcastIntent);
                }

            };

            //if already exist profile, then close it
            if (sipProfile != null) {
                if (sipManager.isRegistered(sipProfile.getUriString())){
                    Log.d(TAG, "Unregistering...");
                    sipManager.unregister(sipProfile, sipRegistrationListener);
                }
            }

        } catch (SipException se) {
            Log.d(TAG, "Connection error.");
            broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_UNREGISTER_STATUS_FAIL);
            sendByBroadcast(broadcastIntent);
            se.printStackTrace();
        } catch (Exception e) {
            broadcastIntent.putExtra(Messages.TAG_SIP_REGISTRATION_STATUS, Messages.SIP_ACCOUNT_UNREGISTER_STATUS_FAIL);
            sendByBroadcast(broadcastIntent);
            e.printStackTrace();
        }

    }

    public static boolean isRegisteredSipAccount(){
        if (sipManager == null || sipProfile == null){
            return false;
        }

        try {
            if (sipManager.isRegistered(sipProfile.getUriString())){
                return true;
            }
        }catch (SipException se){
            se.printStackTrace();
        }
        return false;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IntentFilter intentFilter = new IntentFilter(Protocol.ACTION_BROADCAST_SIP_SETTING);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "sip setting done!");
            }
        }, intentFilter);

        return super.onBind(intent);
    }


    private void sendByBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    /**********************************************************************************************/
    /***************************Connect to Telecom Manager ****************************************/
    /**********************************************************************************************/
    /**
     * Creates and sends the intent to add an incoming sipAudioCall through Telecom.
     *
     * @param context The current context.
     */
    public void sendIncomingCallIntent(Context context, Uri handle) {
        PhoneAccountHandle phoneAccount = new PhoneAccountHandle(
                new ComponentName(context, SipTelConnectionService.class),
                Protocol.ID_CALL_PROVIDER);
        Bundle extras = new Bundle();
        if (handle != null) {
            extras.putParcelable(SipTelConnectionService.EXTRA_HANDLE, handle);
        }
        final TelecomManager telecomManager =
                (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        try {
            telecomManager.addNewIncomingCall(phoneAccount, extras);
        }catch (SecurityException se){
            Log.d(TAG, "Please Enable Account from Phone Setting");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Please Enable Account from Phone Setting", Toast.LENGTH_LONG).show();
                }
            });

            //reject call for security exception
            Intent intent = new Intent(Protocol.INFO_BROADCAST_TEL_TO_SIP);
            intent.putExtra(Messages.TAG_TEL_TO_SIP_EXTRA, Messages.TEL_TO_SIP_EXTRA_REJECT);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }


    /**
     * Creates and sends the intent to add an incoming sipAudioCall through Telecom.
     *
     * @param context The current context.
     */
    public void sendOutgoingCallIntent(Context context, String handle) {
        PhoneAccountHandle phoneAccountHandle = new PhoneAccountHandle(
                new ComponentName(context, SipTelConnectionService.class),
                Protocol.ID_CALL_PROVIDER);

        final TelecomManager telecomManager =
                (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        Log.d(TAG, "sendOutgoingCallIntent1");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "defaultDialer: "+telecomManager.getDefaultDialerPackage());


        telecomManager.placeCall(Uri.fromParts(PhoneAccount.SCHEME_SIP,
                handle, null), createCallIntentExtras(phoneAccountHandle));
    }

    private Bundle createCallIntentExtras(PhoneAccountHandle phoneAccountHandle) {
//        Bundle extras = new Bundle();
//        extras.putString("number", "01717717488");

        Bundle intentExtras = new Bundle();
//        intentExtras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
        intentExtras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        Log.i("Santos xtr", intentExtras.toString());
        return intentExtras;
    }






    //// TEST
    public static void checkCallingAccounts(Context context){
        Intent intent = new Intent(context, SipService.class);
        intent.setAction(Protocol.ACTION_TEST);
        context.startService(intent);
        Log.d(TAG, "checkCallingAccounts..");
    }
    public void handleCheckCallingAccounts(){
        final TelecomManager telecomManager =
                (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        for (PhoneAccountHandle acc :telecomManager.getCallCapablePhoneAccounts()){
            Log.d(TAG, "ID:"+acc.getId());
//            telecomManager.getAdnUriForPhoneAccount(acc);
        }

        Log.d(TAG, "defaultDialerPkg: "+telecomManager.getDefaultDialerPackage());
//        Log.d(TAG, "OutgoingAcc:"+telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_SIP).getId());
    }
}
