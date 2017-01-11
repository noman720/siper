
package com.example.android.sip;

/**
 * Created by Noman on 12/30/2016.
 */

import android.net.Uri;
import android.telecom.CallAudioState;
import android.telecom.Conference;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.RemoteConference;
import android.telecom.RemoteConnection;
import android.telecom.StatusHints;
import android.telecom.VideoProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySipTelConnectionServiceBack extends ConnectionService {

    private static final String TAG = "SipTelConnectionService";

    /**
     * Intent extra used to pass along the video state for a new test sipAudioCall.
     */
    public static final String EXTRA_HANDLE = "extra_handle";


    public MySipTelConnectionServiceBack() {
    }

    public final class ManagedConnection extends Connection {
        private final RemoteConnection.Callback mRemoteCallback = new RemoteConnection.Callback() {
            @Override
            public void onStateChanged(RemoteConnection connection, int state) {
                setState(state);
            }

            @Override
            public void onDisconnected(
                    RemoteConnection connection, DisconnectCause disconnectCause) {
                setDisconnected(disconnectCause);
                destroy();
            }

            @Override
            public void onRingbackRequested(RemoteConnection connection, boolean ringback) {
                setRingbackRequested(ringback);
            }

            @Override
            public void onConnectionCapabilitiesChanged(RemoteConnection connection,
                                                        int connectionCapabilities) {
                setConnectionCapabilities(connectionCapabilities);
            }

//            @Override
//            public void onConnectionPropertiesChanged(RemoteConnection connection,
//                                                      int connectionProperties) {
//                setConnectionProperties(connectionProperties);
//            }

            @Override
            public void onPostDialWait(RemoteConnection connection, String remainingDigits) {
                setPostDialWait(remainingDigits);
            }

            @Override
            public void onVoipAudioChanged(RemoteConnection connection, boolean isVoip) {
                setAudioModeIsVoip(isVoip);
            }

            @Override
            public void onStatusHintsChanged(RemoteConnection connection, StatusHints statusHints) {
                setStatusHints(statusHints);
            }

            @Override
            public void onVideoStateChanged(RemoteConnection connection, int videoState) {
                if (videoState == VideoProfile.STATE_BIDIRECTIONAL) {
//                    setVideoProvider(new TestManagedVideoProvider(connection.getVideoProvider())); //by noman

                }
                setVideoState(videoState);
            }

            @Override
            public void onAddressChanged(
                    RemoteConnection connection, Uri address, int presentation) {
                setAddress(address, presentation);
            }

            @Override
            public void onCallerDisplayNameChanged(
                    RemoteConnection connection, String callerDisplayName, int presentation) {
                setCallerDisplayName(callerDisplayName, presentation);
            }

            @Override
            public void onDestroyed(RemoteConnection connection) {
                destroy();
                mManagedConnectionByRemote.remove(mRemote);
            }

            @Override
            public void onConferenceableConnectionsChanged(
                    RemoteConnection connect,
                    List<RemoteConnection> conferenceable) {
                List<Connection> c = new ArrayList<>();
                for (RemoteConnection remote : conferenceable) {
                    if (mManagedConnectionByRemote.containsKey(remote)) {
                        c.add(mManagedConnectionByRemote.get(remote));
                    }
                }
                setConferenceableConnections(c);
            }
        };

        private final RemoteConnection mRemote;
        private final boolean mIsIncoming;

        ManagedConnection(RemoteConnection remote, boolean isIncoming) {
            mRemote = remote;
            mIsIncoming = isIncoming;
            mRemote.registerCallback(mRemoteCallback);
            setState(mRemote.getState());
            setVideoState(mRemote.getVideoState());
        }

        @Override
        public void onAbort() {
            mRemote.abort();
        }

        /** ${inheritDoc} */
        @Override
        public void onAnswer(int videoState) {
            mRemote.answer();
        }

        /** ${inheritDoc} */
        @Override
        public void onDisconnect() {
            mRemote.disconnect();
        }

        @Override
        public void onPlayDtmfTone(char c) {
            mRemote.playDtmfTone(c);
        }

        /** ${inheritDoc} */
        @Override
        public void onHold() {
            mRemote.hold();
        }

        /** ${inheritDoc} */
        @Override
        public void onReject() {
            mRemote.reject();
        }

        /** ${inheritDoc} */
        @Override
        public void onUnhold() {
            mRemote.unhold();
        }

        @Override
        public void onCallAudioStateChanged(CallAudioState state) {
            mRemote.setCallAudioState(state);
        }

        private void setState(int state) {
            log("setState: " + state);
            switch (state) {
                case STATE_ACTIVE:
                    setActive();
                    break;
                case STATE_HOLDING:
                    setOnHold();
                    break;
                case STATE_DIALING:
                    setDialing();
                    break;
                case STATE_RINGING:
                    setRinging();
                    break;
            }
        }
    }


    public final class ManagedConference extends Conference {
        private final RemoteConference.Callback mRemoteCallback = new RemoteConference.Callback() {
            @Override
            public void onStateChanged(RemoteConference conference, int oldState, int newState) {
                switch (newState) {
                    case Connection.STATE_DISCONNECTED:
                        // See onDisconnected below
                        break;
                    case Connection.STATE_HOLDING:
                        setOnHold();
                        break;
                    case Connection.STATE_ACTIVE:
                        setActive();
                        break;
                    default:
                        log("unrecognized state for Conference: " + newState);
                        break;
                }
            }

            @Override
            public void onDisconnected(RemoteConference conference,
                                       DisconnectCause disconnectCause) {
                setDisconnected(disconnectCause);
            }

            @Override
            public void onConnectionAdded(
                    RemoteConference conference,
                    RemoteConnection connection) {
                ManagedConnection c = mManagedConnectionByRemote.get(connection);
                if (c == null) {
                    log("onConnectionAdded cannot find remote connection: " + connection);
                } else {
                    addConnection(c);
                }
            }

            @Override
            public void onConnectionRemoved(
                    RemoteConference conference,
                    RemoteConnection connection) {
                ManagedConnection c = mManagedConnectionByRemote.get(connection);
                if (c == null) {
                    log("onConnectionRemoved cannot find remote connection: " + connection);
                } else {
                    removeConnection(c);
                }
            }

            @Override
            public void onConnectionCapabilitiesChanged(RemoteConference conference,
                                                        int connectionCapabilities) {
                setConnectionCapabilities(connectionCapabilities);
            }

//            @Override
//            public void onConnectionPropertiesChanged(RemoteConference conference,
//                                                      int connectionProperties) {
//                setConnectionProperties(connectionProperties);
//            }

            @Override
            public void onDestroyed(RemoteConference conference) {
                destroy();
                mRemote.unregisterCallback(mRemoteCallback);
                mManagedConferenceByRemote.remove(mRemote);
            }

        };

        @Override
        public void onPlayDtmfTone(char c) {
            mRemote.playDtmfTone(c);
        };

        @Override
        public void onStopDtmfTone() {
            mRemote.stopDtmfTone();
        };

        private final RemoteConference mRemote;

        public ManagedConference(RemoteConference remote) {
            super(null);
            mRemote = remote;
            remote.registerCallback(mRemoteCallback);
            setActive();
            for (RemoteConnection r : remote.getConnections()) {
                ManagedConnection c = mManagedConnectionByRemote.get(r);
                if (c != null) {
                    addConnection(c);
                }
            }
        }
    }

    static void log(String msg) {
        Log.w(TAG, msg);
    }

    private final Map<RemoteConference, ManagedConference> mManagedConferenceByRemote
            = new HashMap<>();
    private final Map<RemoteConnection, ManagedConnection> mManagedConnectionByRemote
            = new HashMap<>();

    @Override
    public Connection onCreateOutgoingConnection(
            PhoneAccountHandle connectionManagerAccount,
            final ConnectionRequest request) {
        return makeConnection(request, false);
    }

    @Override
    public Connection onCreateIncomingConnection(
            PhoneAccountHandle connectionManagerAccount,
            final ConnectionRequest request) {
        return makeConnection(request, true);
    }

    @Override
    public void onConference(Connection a, Connection b) {
        conferenceRemoteConnections(
                ((ManagedConnection) a).mRemote,
                ((ManagedConnection) b).mRemote);
    }

    @Override
    public void onRemoteConferenceAdded(RemoteConference remoteConference) {
        addConference(new ManagedConference(remoteConference));
    }

    Map<RemoteConnection, ManagedConnection> getManagedConnectionByRemote() {
        return mManagedConnectionByRemote;
    }

    private Connection makeConnection(ConnectionRequest request, boolean incoming) {
        RemoteConnection remote = incoming
                ? createRemoteIncomingConnection(request.getAccountHandle(), request)
                : createRemoteOutgoingConnection(request.getAccountHandle(), request);
        ManagedConnection local = new ManagedConnection(remote, incoming);
        mManagedConnectionByRemote.put(remote, local);
        return local;
    }


}
