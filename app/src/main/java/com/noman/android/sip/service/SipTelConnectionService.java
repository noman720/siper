
package com.noman.android.sip.service;

/**
 * Created by Noman on 12/30/2016.
 */

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.RemoteConference;
import android.telecom.TelecomManager;
import android.util.Log;

import com.noman.android.sip.util.Messages;
import com.noman.android.sip.util.Protocol;

import java.util.ArrayList;
import java.util.List;


public class SipTelConnectionService extends ConnectionService {

    private static final String TAG = "SipTelConnectionService";

    /**
     * Intent extra used to pass along the video state for a new test sipAudioCall.
     */
    public static final String EXTRA_HANDLE = "extra_handle";


    public SipTelConnectionService() {
    }


    final class MyConnection extends Connection {
        private final boolean mIsIncoming;

        private BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "mEventReceiver: received!");
                int extra = intent.getIntExtra(Messages.TAG_SIP_TO_TEL_EXTRA, -1);
                switch (extra){
                    case Messages.SIP_TO_TEL_EXTRA_END_CALL:
                        setDisconnected(new DisconnectCause(DisconnectCause.MISSED));
                        destroyCall(MyConnection.this);
                        destroy();
                        break;

                }

            }
        };

        MyConnection(boolean isIncoming) {
            mIsIncoming = isIncoming;
            // Assume all calls are video capable.
            int capabilities = getConnectionCapabilities();
            capabilities |= CAPABILITY_SUPPORTS_VT_LOCAL_BIDIRECTIONAL;
            capabilities |= CAPABILITY_SUPPORTS_VT_REMOTE_BIDIRECTIONAL;
            capabilities |= CAPABILITY_CAN_UPGRADE_TO_VIDEO;
            capabilities |= CAPABILITY_MUTE;
            capabilities |= CAPABILITY_SUPPORT_HOLD;
            capabilities |= CAPABILITY_HOLD;
            capabilities |= CAPABILITY_RESPOND_VIA_TEXT;
            setConnectionCapabilities(capabilities);

//            if (isIncoming) {
//                putExtra(Connection.EXTRA_ANSWERING_DROPS_FG_CALL, true);
//            }
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                    mEventReceiver, new IntentFilter(Protocol.INFO_BROADCAST_SIP_TO_TEL));
            Log.d(TAG, "mEventReceiver: registered!");
//            final IntentFilter filter =
//                    new IntentFilter(TestCallActivity.ACTION_SEND_UPGRADE_REQUEST);
//            filter.addDataScheme("int");
//            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
//                    mUpgradeRequestReceiver, filter);
        }

        void startOutgoing() {
            setDialing();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setActive();
//                    activateCall(MyConnection.this);
                }
            }, 4000);
        }

        /** ${inheritDoc} */
        @Override
        public void onAbort() {
            log("Destroyed sipAudioCall");
            destroyCall(this);
            destroy();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_ABORT);
        }

        /** ${inheritDoc} */
        @Override
        public void onAnswer(int videoState) {
            log("Answered Call");
            setActive();
            updateConferenceable();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_ANSWER);
        }

        /** ${inheritDoc} */
        @Override
        public void onPlayDtmfTone(char c) {
            log("DTMF played: "+c);
            if (c == '1') {
                setDialing();
            }
        }

        /** ${inheritDoc} */
        @Override
        public void onStopDtmfTone() {
            log("DTMF stopped!");
        }

        /** ${inheritDoc} */
        @Override
        public void onDisconnect() {
            log("Disconnected");
            setDisconnected(new DisconnectCause(DisconnectCause.REMOTE));
            destroyCall(this);
            destroy();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_DISCONNECT);
        }

        /** ${inheritDoc} */
        @Override
        public void onHold() {
            log("Hold Call");
            setOnHold();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_HOLD);
        }

        /** ${inheritDoc} */
        @Override
        public void onReject() {
            log("Reject sipAudioCall");
            setDisconnected(new DisconnectCause(DisconnectCause.REJECTED));
            destroyCall(this);
            destroy();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_REJECT);
        }

        /** ${inheritDoc} */
        @Override
        public void onUnhold() {
            log("Unhold Call");
            setActive();
            sendLocalBroadcast(Messages.TEL_TO_SIP_EXTRA_UNHOLD);
        }

        public void cleanup() {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                    mEventReceiver);
        }


        public void sendLocalBroadcast(int action){
            Intent intent = new Intent(Protocol.INFO_BROADCAST_TEL_TO_SIP);
            intent.putExtra(Messages.TAG_TEL_TO_SIP_EXTRA, action);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

    }


    static void log(String msg) {
        Log.w(TAG, msg);
    }

    @Override
    public Connection onCreateOutgoingConnection(
            PhoneAccountHandle connectionManagerAccount,
            final ConnectionRequest request) {
        Log.d(TAG, "onCreateOutgoingConnection");
//        return makeConnection(request, false);
        return null;
    }

    @Override
    public Connection onCreateIncomingConnection(
            PhoneAccountHandle connectionManagerAccount,
            final ConnectionRequest request) {
        PhoneAccountHandle accountHandle = request.getAccountHandle();
        ComponentName componentName = new ComponentName(getApplicationContext(), this.getClass());


        log("onCreateIncomingConnection conManager: "+connectionManagerAccount.getId()+" | componentName: "+connectionManagerAccount.getComponentName());
        log("onCreateIncomingConnection request: "+request.getAccountHandle().getId()+" | componentName: "+request.getAccountHandle().getComponentName());


        if (accountHandle != null && componentName.equals(accountHandle.getComponentName())) {
            final MyConnection connection = new MyConnection(true);
            // Get the stashed intent extra that determines if this is a video sipAudioCall or audio sipAudioCall.
            Bundle extras = request.getExtras();
            Uri providedHandle = extras.getParcelable(EXTRA_HANDLE);

            log("set address: "+request.getAddress());
            setAddress(connection, providedHandle);
            addCall(connection);
            return connection;
        } else {
            return Connection.createFailedConnection(new DisconnectCause(DisconnectCause.ERROR,
                    "Invalid inputs: " + accountHandle + " " + componentName));
        }
    }

    @Override
    public void onConference(Connection a, Connection b) {
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference remoteConference) {

    }

    private final List<MyConnection> mCalls = new ArrayList<>();
    private final Handler mHandler = new Handler();


    @Override
    public boolean onUnbind(Intent intent) {
        log("onUnbind");
        return super.onUnbind(intent);
    }

    private void addCall(MyConnection connection) {
        mCalls.add(connection);
        updateConferenceable();
    }

    private void destroyCall(MyConnection connection) {
        connection.cleanup();
        mCalls.remove(connection);
        updateConferenceable();
    }

    private void updateConferenceable() {
        List<Connection> freeConnections = new ArrayList<>();
        freeConnections.addAll(mCalls);
        for (int i = 0; i < freeConnections.size(); i++) {
            if (freeConnections.get(i).getConference() != null) {
                freeConnections.remove(i);
            }
        }
        for (int i = 0; i < freeConnections.size(); i++) {
            Connection c = freeConnections.remove(i);
            c.setConferenceableConnections(freeConnections);
            freeConnections.add(i, c);
        }
    }

    private void setAddress(Connection connection, Uri address) {
        connection.setAddress(address, TelecomManager.PRESENTATION_ALLOWED);
        Log.d(TAG, "address: "+address);
//        if ("5551234".equals(address.getSchemeSpecificPart())) {
//            connection.setCallerDisplayName("Hello World", TelecomManager.PRESENTATION_ALLOWED);
//        }
    }

}
