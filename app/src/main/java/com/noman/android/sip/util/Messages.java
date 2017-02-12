package com.noman.android.sip.util;

/**
 * Created by User on 12/30/2016.
 */

public class Messages {
    public static final String TAG_CALLING_ACCOUNT_STATUS = "calling_account_status";
    public static final String CALLING_ACCOUNT_REGISTER_STATUS_SUCCESS = "Successfully calling account created.";
    public static final String CALLING_ACCOUNT_REGISTER_STATUS_FAIL = "Error to create calling account.";
    public static final String CALLING_ACCOUNT_UNREGISTER_STATUS_SUCCESS = "Successfully calling account unregistered.";
    public static final String CALLING_ACCOUNT_UNREGISTER_STATUS_FAIL = "Error to unregister calling account.";

    public static final String TAG_SIP_ACCOUNT_STATUS = "SIP_account_status";
    public static final String TAG_SIP_REGISTRATION_STATUS = "SIP_registration_status";
    public static final String SIP_ACCOUNT_CREATE_STATUS_SUCCESS = "Successfully SIP account created.";
    public static final String SIP_ACCOUNT_CREATE_STATUS_FAIL = "Error to create SIP account.";
    public static final String SIP_ACCOUNT_REMOVE_STATUS_SUCCESS = "Successfully SIP account removed.";
    public static final String SIP_ACCOUNT_REMOVE_STATUS_FAIL = "Error to remove SIP account.";
    public static final String SIP_ACCOUNT_REGISTER_STATUS_SUCCESS = "Successfully SIP account registered.";
    public static final String SIP_ACCOUNT_REGISTER_STATUS_TRYING = "SIP account registration trying...";
    public static final String SIP_ACCOUNT_REGISTER_STATUS_FAIL = "Error to create SIP account.";
    public static final String SIP_ACCOUNT_UNREGISTER_STATUS_SUCCESS = "Successfully SIP account unregistered.";
    public static final String SIP_ACCOUNT_UNREGISTER_STATUS_TRYING = "SIP account unregister trying...";
    public static final String SIP_ACCOUNT_UNREGISTER_STATUS_FAIL = "Error to unregister SIP account.";

    public static final String TAG_CALL_INITIATION_TYPE = "call_initiation_type";
    public static final int CALL_INITIATION_TYPE_NATIVE = 100;

    //sip to tel extras
    public static final String TAG_SIP_TO_TEL_EXTRA = "sip_to_tel_extra";
    public static final String TAG_SIP_TO_TEL_CALL_ID = "sip_to_tel_call_id";
    public static final int SIP_TO_TEL_EXTRA_END_CALL = 1;

    //tel to sip extras
    public static final String TAG_TEL_TO_SIP_ACTION = "tel_to_sip_action";
    public static final String TAG_TEL_TO_SIP_CALL_ID = "tel_to_sip_call_id";
    public static final int TEL_TO_SIP_EXTRA_REJECT = 1; //reject by me
    public static final int TEL_TO_SIP_EXTRA_DISCONNECT = 2; //remote disconnect
    public static final int TEL_TO_SIP_EXTRA_ANSWER = 3; //answer call
    public static final int TEL_TO_SIP_EXTRA_ABORT = 4; //abort call
    public static final int TEL_TO_SIP_EXTRA_HOLD = 5; //hold call
    public static final int TEL_TO_SIP_EXTRA_UNHOLD = 6; //hold call

}
