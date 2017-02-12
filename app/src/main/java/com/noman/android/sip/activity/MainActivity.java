
package com.noman.android.sip.activity;

/**
 * Created by Noman on 12/30/2016.
 */

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.noman.android.sip.R;
import com.noman.android.sip.model.SipUser;
import com.noman.android.sip.preferences.SipSettings;
import com.noman.android.sip.service.SIPerConnectionService;
import com.noman.android.sip.service.SipService;
import com.noman.android.sip.util.Helper;
import com.noman.android.sip.util.Messages;
import com.noman.android.sip.util.PreferenceUtil;
import com.noman.android.sip.util.Protocol;

import java.util.Arrays;
import java.util.List;

/**
 * Handles all calling, receiving calls, and UI interaction in the AndroidSIP app.
 */
public class MainActivity extends Activity implements View.OnTouchListener {

    public static String TAG = "MainActivity";
    public String sipAddress = null;

//    public SipManager sipManager = null;
//    public SipProfile sipProfile = null;
//    public SipAudioCall sipAudioCall = null;
//    public IncomingCallReceiver callReceiver;
//
    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int SET_DEFAULT = 4;
//    private static final int HANG_UP = 5;

    private static final int REQUEST_CODE_SET_DEFAULT_DIALER = 1;

    //UI components
    private Button btnPhoneAccount;
    private Button btnSipAccount;
    private Button btnSipRegister;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        //check permission
        checkAllPermissions();

    }

    private void create(){
        //init preference
        PreferenceUtil.init(getApplicationContext());

        SipService.initSipManager(getApplicationContext());

//
//        // "Push to talk" can be a serious pain when the screen keeps turning off.
//        // Let's prevent that.
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//        initializeManager();

        //show account information
        updateAccountConfiguration();


        //Phone Register Button
        btnPhoneAccount = (Button) findViewById(R.id.btnCallingAccount);
        btnPhoneAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: register calling account
                boolean callAccountState = PreferenceUtil.hasPhoneAccount();
                SipUser sipUser = Helper.getSipUser(getBaseContext());
                if (callAccountState){
                    //unregister here
                    SipService.unregisterCallingAccount(getBaseContext(), "SIPer-"+sipUser.getName(), sipUser.getName()+"@"+sipUser.getDomain());
                }else {
                    //register here
                    SipService.registerCallingAccount(getBaseContext(), "SIPer", "A simple native api based SIP application @Noman");
                    //TODO: register sip account
                }
                updatePhoneAccountButtonStatus();
            }
        });

        updatePhoneAccountButtonStatus();

        btnSipAccount = (Button) findViewById(R.id.btnSipAccount);
        btnSipAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean sipAccountState = PreferenceUtil.hasSipAccount();
                SipUser sipUser = Helper.getSipUser(getBaseContext());
                if (sipAccountState){
                    //remove sip account here
                    SipService.removeSipAccount(getBaseContext());
                }else {
                    //add  sip account here
                    SipService.addSipAccount(getBaseContext(), sipUser.getName(), sipUser.getDomain(), sipUser.getPassword());
                }

                updateSIPAccountButtonStatus();
            }
        });
        updateSIPAccountButtonStatus();

        btnSipRegister = (Button) findViewById(R.id.btnSipRegister);
        btnSipRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, 1);


                boolean isRegistered = PreferenceUtil.isSipRegistered();
                if (isRegistered){
                    //unregister sip account here
                    SipService.unregisterSipAccount(getBaseContext());
                }else {
                    //register sip account here
                    SipService.registerSipAccount(getBaseContext());
                }
                updateSIPRegisterButtonStatus();
            }
        });
        updateSIPRegisterButtonStatus();


        IntentFilter intentFilter = new IntentFilter(Protocol.INFO_BROADCAST_CALLING_ACCOUNT);
        intentFilter.addAction(Protocol.INFO_BROADCAST_SIP_ACCOUNT);
        intentFilter.addAction(Protocol.INFO_BROADCAST_SIP_REGISTRATION);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Protocol.INFO_BROADCAST_CALLING_ACCOUNT)){
                    Toast.makeText(context, intent.getStringExtra(Messages.TAG_CALLING_ACCOUNT_STATUS), Toast.LENGTH_SHORT).show();
                    updatePhoneAccountButtonStatus();
                } else if (intent.getAction().equals(Protocol.INFO_BROADCAST_SIP_ACCOUNT)){
                    Toast.makeText(context, intent.getStringExtra(Messages.TAG_SIP_ACCOUNT_STATUS), Toast.LENGTH_SHORT).show();
                    updateSIPAccountButtonStatus();
                } else if (intent.getAction().equals(Protocol.INFO_BROADCAST_SIP_REGISTRATION)){
                    Toast.makeText(context, intent.getStringExtra(Messages.TAG_SIP_REGISTRATION_STATUS), Toast.LENGTH_SHORT).show();
                    updateSIPRegisterButtonStatus();
                    updateStatus(intent.getStringExtra(Messages.TAG_SIP_REGISTRATION_STATUS));
                }
            }
        }, intentFilter);

    }

    private void checkAllPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            create();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.RECORD_AUDIO
                    },
                    100);

            Log.d(TAG, "requesting for permission...");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){
                    for (int i=0; i<grantResults.length; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(getApplicationContext(), "You can't use this app", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        }
                    }
                    create(); //create ui
                } else {
                    Toast.makeText(getApplicationContext(), "You can't use this app", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                return;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        printCurrentActivity();
    }

    @Override
    public void onResume(){
        super.onResume();
        printCurrentActivity();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
//        initializeManager();

    }

    private void printCurrentActivity(){
        //check the current activity name
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.d(TAG, "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (sipAudioCall != null) {
//            sipAudioCall.close();
//        }
//
//        closeLocalProfile();

//        if (callReceiver != null) {
//            this.unregisterReceiver(callReceiver);
//        }
        SipService.destroySipManager(getApplicationContext());
    }

//    public void initializeManager() {
//        if(sipManager == null) {
//          sipManager = SipManager.newInstance(this);
//        }
//
//        initializeLocalProfile();
//    }

//    /**
//     * Logs you into your SIP provider, registering this device as the location to
//     * send SIP calls to for your SIP address.
//     */
//    public void initializeLocalProfile() {
//        if (sipManager == null) {
//            return;
//        }
//
//        if (sipProfile != null) {
//            closeLocalProfile();
//        }
//
//        SipUser sipUser = Helper.getSipUser(getBaseContext());
//
//        if (sipUser.getName().length() == 0 || sipUser.getDomain().length() == 0 || sipUser.getPassword().length() == 0) {
//            showDialog(UPDATE_SETTINGS_DIALOG);
//            return;
//        }
//
//        try {
//            SipProfile.Builder builder = new SipProfile.Builder(sipUser.getName(), sipUser.getDomain());
//            builder.setPassword(sipUser.getPassword());
//            builder.setPort(5060);
//            builder.setProtocol("UDP");
//            builder.setSendKeepAlive(true);
//            builder.setAutoRegistration(true);
//            sipProfile = builder.build();
//
//            Intent intent = new Intent();
//            intent.setAction(Protocol.ACTION_RECEIVE_INCOMING_CALL);
//            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
//            sipManager.open(sipProfile, pi, null);
//
//
//            //register phone account (phone setting)
//            registerPhoneAccount(getApplicationContext(), sipProfile.getUriString());
//
//            Log.i(TAG, "Opened sip profile: "+ sipProfile.getUriString());
//            Log.i(TAG, "isOpened: "+ sipManager.isOpened(sipProfile.getUriString()));
//
//            // This listener must be added AFTER sipManager.open is called,
//            // Otherwise the methods aren't guaranteed to fire.
//
//            sipManager.setRegistrationListener(sipProfile.getUriString(), new SipRegistrationListener() {
//                public void onRegistering(String localProfileUri) {
//                    updateStatus("Registering with SIP Server...");
//                }
//
//                public void onRegistrationDone(String localProfileUri, long expiryTime) {
//                    updateStatus("Ready");
//                }
//
//                public void onRegistrationFailed(String localProfileUri, int errorCode,
//                                                 String errorMessage) {
//                    updateStatus("Registration failed.  Please check settings.");
//                    Log.e(TAG, "uri: "+localProfileUri+" | errorCode: "+errorCode+" | errorMessage: "+errorMessage);
//                }
//            });
//
//
//        } catch (ParseException pe) {
//            updateStatus("Connection Error.");
//            pe.printStackTrace();
//        } catch (SipException se) {
//            updateStatus("Connection error.");
//            se.printStackTrace();
//        }
//    }
//
//    /**
//     * Closes out your local profile, freeing associated objects into memory
//     * and unregistering your device from the server.
//     */
//    public void closeLocalProfile() {
//        if (sipManager == null) {
//            return;
//        }
//        try {
//            if (sipProfile != null) {
//                sipManager.close(sipProfile.getUriString());
//            }
//        } catch (Exception ee) {
//            Log.d(TAG, "Failed to close local profile.", ee);
//        }
//    }




    /**
     * Make an outgoing sipAudioCall.
     */
    public void initiateCall() {
//        placeCall();
//        fireIncomingCall(getApplicationContext(), null, VideoProfile.STATE_AUDIO_ONLY);
        SipService.outgoingCall(getApplicationContext(), sipAddress);
//        SipService.incomingCall(getApplicationContext(), null);
//        SipService.checkCallingAccounts(getApplicationContext());


//        updateStatus(sipAddress);
//
//        try {
//            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
//                // Much of the client's interaction with the SIP Stack will
//                // happen via listeners.  Even making an outgoing sipAudioCall, don't
//                // forget to set up a listener to set things up once the sipAudioCall is established.
//                @Override
//                public void onCallEstablished(SipAudioCall call) {
//                    call.startAudio();
//                    call.setSpeakerMode(true);
//                    call.toggleMute();
//                    updateStatus(call);
//                }
//
//                @Override
//                public void onCallEnded(SipAudioCall call) {
//                    updateStatus("Ready.");
//                }
//            };
//
//            sipAudioCall = sipManager.makeAudioCall(sipProfile.getUriString(), sipAddress, listener, 30);
//
//        }
//        catch (Exception e) {
//            Log.i(TAG, "Error when trying to close sipManager.", e);
//            if (sipProfile != null) {
//                try {
//                    sipManager.close(sipProfile.getUriString());
//                } catch (Exception ee) {
//                    Log.i(TAG, "Error when trying to close sipManager.", ee);
//                    ee.printStackTrace();
//                }
//            }
//            if (sipAudioCall != null) {
//                sipAudioCall.close();
//            }
//        }
    }

    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateAccount(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                labelView.setText(status);
            }
        });
    }
    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipStatus);
                labelView.setText(status);
            }
        });
    }

//    /**
//     * Updates the status box with the SIP address of the current sipAudioCall.
//     * @param call The current, active sipAudioCall.
//     */
//    public void updateStatus(SipAudioCall call) {
//        String useName = call.getPeerProfile().getDisplayName();
//        if(useName == null) {
//          useName = call.getPeerProfile().getUserName();
//        }
//        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
//
//        //show native sipAudioCall screen here
////        fireIncomingCall(getApplicationContext(), Uri.parse(call.getPeerProfile().getUriString()), VideoProfile.STATE_AUDIO_ONLY);
//    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (sipAudioCall == null) {
//            return false;
//        } else if (event.getAction() == MotionEvent.ACTION_DOWN && sipAudioCall != null && sipAudioCall.isMuted()) {
//            sipAudioCall.toggleMute();
//        } else if (event.getAction() == MotionEvent.ACTION_UP && !sipAudioCall.isMuted()) {
//            sipAudioCall.toggleMute();
//        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, CALL_ADDRESS, 0, "Call someone");
        menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        menu.add(0, SET_DEFAULT, 0, "Set Default");
//        menu.add(0, HANG_UP, 0, "End Current Call.");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CALL_ADDRESS:
                showDialog(CALL_ADDRESS);
                break;
            case SET_AUTH_INFO:
                updatePreferences();
                break;
            case SET_DEFAULT:
                setDefault();
                break;
//            case HANG_UP:
//                if(sipAudioCall != null) {
//                    try {
//                      sipAudioCall.endCall();
//                    } catch (SipException se) {
//                        Log.d(TAG, "Error ending sipAudioCall.", se);
//                    }
//                    sipAudioCall.close();
//                }
//                break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CALL_ADDRESS:
                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(this)
                        .setTitle("Call Someone.")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        EditText textField = (EditText)
                                                (textBoxView.findViewById(R.id.calladdress_edit));
                                        sipAddress = textField.getText().toString();
                                        initiateCall();

                                    }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                        })
                        .create();

            case UPDATE_SETTINGS_DIALOG:
                return new AlertDialog.Builder(this)
                        .setMessage("Please Configure SIP Account.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                updatePreferences();
                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                        })
                        .create();
        }
        return null;
    }

    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SipSettings.class);
        startActivityForResult(settingsActivity, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "requestCode: "+requestCode+" | resultCode:"+resultCode);
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "User accepted request to become default dialer", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "User declined request to become default dialer", Toast.LENGTH_LONG).show();
            }
        }

        updateAccountConfiguration();
    }

    private void updateAccountConfiguration(){
        SipUser sipUser = Helper.getSipUser(getBaseContext());

        Log.i(TAG, "user: "+sipUser.getName() + " | domain: "+sipUser.getDomain()+" | password: "+sipUser.getPassword());

        if (sipUser.getName().length() == 0 || sipUser.getDomain().length() == 0) {
            updateAccount("No account configured!");
            showDialog(UPDATE_SETTINGS_DIALOG);
            return;
        }

        updateAccount("MyAcc: "+sipUser.getName()+"@"+sipUser.getDomain());
    }

    private Bundle createCallIntentExtras() {
        Bundle extras = new Bundle();
        extras.putString("com.android.server.telecom.testapps.CALL_EXTRAS", "Yorke was here");

        Bundle intentExtras = new Bundle();
        intentExtras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, extras);
        Log.i("Santos xtr", intentExtras.toString());
        return intentExtras;
    }


    /**
     * Creates and sends the intent to add an incoming sipAudioCall through Telecom.
     *
     * @param context The current context.
     * @param videoState The video state requested for the incoming sipAudioCall.
     */
    public void sendIncomingCallIntent(Context context, Uri handle, int videoState) {
        PhoneAccountHandle phoneAccount = new PhoneAccountHandle(
                new ComponentName(context, SIPerConnectionService.class),
                "CALL_PROVIDER_ID");
        // For the purposes of testing, indicate whether the incoming sipAudioCall is a video sipAudioCall by
        // stashing an indicator in the EXTRA_INCOMING_CALL_EXTRAS.
        Bundle extras = new Bundle();
//        extras.putInt(SIPerConnectionService.EXTRA_START_VIDEO_STATE, videoState);
        if (handle != null) {
            extras.putParcelable(SIPerConnectionService.EXTRA_HANDLE, handle);
        }
//        TelecomManager.from(context).addNewIncomingCall(phoneAccount, extras);
        final TelecomManager telecomManager =
                (TelecomManager) getSystemService(Context.TELECOM_SERVICE);

        telecomManager.addNewIncomingCall(phoneAccount, extras);
    }


    /**
     * Registers a phone account with telecom.
     */
    public void registerPhoneAccount(Context context, String sipUriString) {
        TelecomManager telecomManager =
                (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        Icon icon = Icon.createWithResource(getPackageResourcePath(), R.drawable.icon);
        telecomManager.registerPhoneAccount(PhoneAccount.builder(
                new PhoneAccountHandle(
                        new ComponentName(context, SIPerConnectionService.class),
                        "CALL_PROVIDER_ID"),
                "SIPer")
                .setAddress(Uri.parse(sipUriString))
                .setSubscriptionAddress(Uri.parse(sipUriString))
                .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER |
                        PhoneAccount.CAPABILITY_VIDEO_CALLING)
                .setHighlightColor(Color.GREEN)
                // TODO: Add icon tint (Color.RED)
                .setIcon(icon)
                .setShortDescription(sipUriString+"(Noman)")
                .setSupportedUriSchemes(Arrays.asList("tel, sip"))
                .build());

        Log.i(TAG, "registerPhoneAccount...");
    }

    private void updatePhoneAccountButtonStatus(){
        boolean hasPhoneAccount = PreferenceUtil.hasPhoneAccount();
        Log.d(TAG, "hasPhoneAccount: "+hasPhoneAccount);
        if (hasPhoneAccount){
            //now registered, so state change to unregister
            btnPhoneAccount.setBackgroundColor(Color.RED);
            btnPhoneAccount.setText("Unregister phone account");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openPhoneAccountSettingsActivity();
                }
            }, 2000);

        }else {
            //now unregistered, so state change to register
            btnPhoneAccount.setBackgroundColor(Color.GREEN);
            btnPhoneAccount.setText("Register phone account");
        }

    }


    private void openPhoneAccountSettingsActivity(){

        if (SipService.isEnabledCallingAccount(getApplicationContext())){
            return;
        }

        //check the current activity name
        /*ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        Log.d(TAG, "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());*/


        //open setting activity to enable account manually
        final Intent sipSettingsIntent = new Intent();
        final String sipSettingsComponentName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            sipSettingsComponentName = "com.android.phone/.settings.PhoneAccountSettingsActivity";
            sipSettingsComponentName = "com.android.server.telecom/.settings.EnableAccountPreferenceActivity";
        } else {
            sipSettingsComponentName = "com.android.phone/.sip.SipSettings";
        }
        final ComponentName sipSettingsComponent = ComponentName.unflattenFromString(sipSettingsComponentName);
        sipSettingsIntent.setComponent(sipSettingsComponent);
        sipSettingsIntent.setAction("android.intent.action.MAIN");
        sipSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(sipSettingsIntent);
        } catch(final Exception e) {
            Log.e(TAG, "Error starting intent", e);
        }


        //put SMS to test
//        SMSUtils.putSMS(getApplicationContext());

    }


    private void updateSIPAccountButtonStatus(){
        boolean hasSipAccount = PreferenceUtil.hasSipAccount();
        Log.d(TAG, "hasSipAccount: "+hasSipAccount);
        if (hasSipAccount){
            //now added, so state change to remove
            btnSipAccount.setBackgroundColor(Color.RED);
            btnSipAccount.setText("remove sip account");
        }else {
            //now removed, so state change to add
            btnSipAccount.setBackgroundColor(Color.GREEN);
            btnSipAccount.setText("add sip account");
        }
    }

    private void updateSIPRegisterButtonStatus(){
        boolean isRegistered = PreferenceUtil.isSipRegistered();
        Log.d(TAG, "isRegistered: "+isRegistered);
        if (isRegistered){
            //now register, so state change to remove
            btnSipRegister.setBackgroundColor(Color.RED);
            btnSipRegister.setText("unregister sip account");
        }else {
            //now removed, so state change to add
            btnSipRegister.setBackgroundColor(Color.GREEN);
            btnSipRegister.setText("register sip account");
        }
    }

    private void setDefault() {
        final Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, this.getPackageName());
        startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER);
    }
}
