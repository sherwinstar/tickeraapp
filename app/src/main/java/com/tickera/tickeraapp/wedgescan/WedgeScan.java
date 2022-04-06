package com.tickera.tickeraapp.wedgescan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;

interface WedgeScanCallback {
    void onScanSuccess(String type, String result);
}

public class WedgeScan  {

    public static final String ACTION_DECODE = "com.android.decodewedge.decode_action";
    public static final String BARCODE_STRING = "com.android.decode.intentwedge.barcode_string";

    //start stop scan broadcast
    public static final String ACTION_START_DECODE = "com.android.decode.action.START_DECODE";
    public static final String ACTION_STOP_DECODE = "com.android.decode.action.STOP_DECODE";

    /* 扫码结果接收广播地址 */
    public static final String ACTION_BROADCAST_RECEIVER = "com.android.decodewedge.decode_action";
    public static final String EXTRA_BARCODE_STRING = "com.android.decode.intentwedge.barcode_string"; /* 扫码结果 */
    public static final String EXTRA_BARCODE_TYPE = "com.android.decode.intentwedge.barcode_type";    /* 条码类型 */

    private BroadcastReceiver receiver = null;
    private IntentFilter filter = null;
    private Activity activity = null;
    private WedgeScanCallback scanCallback = null;

    private Intent mSendIntent;
    private String barcodestring;
    private String barcodeType;
    private Boolean openProperty= false;

    WedgeScan(Activity activity, WedgeScanCallback callback) {
        super();
        this.activity = activity;
        scanCallback = callback;
    }

    // Creates an intent and stop decoding.
    public void stopDecode() {
        Intent myintent = new Intent();
        myintent.setAction(ACTION_STOP_DECODE);
        activity.sendBroadcast(myintent);
    }

    // Creates an intent and start decoding.
    public void startDecode() {
        Intent myintent = new Intent();
        myintent.setAction(ACTION_START_DECODE);
        activity.sendBroadcast(myintent);
    }

    public void startScan() {
        if(openProperty ==false) {
            mSendIntent = new Intent("com.android.action.setPropertyInt");
            mSendIntent.putExtra("PropertyID", 98);
            mSendIntent.putExtra("PropertyInt", 1);
            activity.sendBroadcast(mSendIntent);
            SystemClock.sleep(50);
            startDecode();
            openProperty = true;
        }
    }

    public void stopScan() {
        if(openProperty ==true) {
            stopDecode();
            SystemClock.sleep(50);
            mSendIntent = new Intent("com.android.action.setPropertyInt");
            mSendIntent.putExtra("PropertyID", 98);
            mSendIntent.putExtra("PropertyInt", 0);
            activity.sendBroadcast(mSendIntent);
            openProperty = false;
        }
    }

    public void register() {
        // Register dynamically decode wedge intent broadcast receiver.
        receiver = new WedgeScan.DecodeWedgeIntentReceiver();
        filter = new IntentFilter();
        filter.addAction(ACTION_DECODE);
        activity.registerReceiver(receiver, filter);
    }

    public void unregister() {
        // Unregister our BroadcastReceiver.
        activity.unregisterReceiver(receiver);
        receiver = null;
        filter = null;
    }

    // Receives action ACTION_BROADCAST_RECEIVER
    public class DecodeWedgeIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent wedgeIntent) {
            String action = wedgeIntent.getAction();
            if (action.equals(ACTION_BROADCAST_RECEIVER)) {
                // Read content of result intent.

                barcodestring = wedgeIntent.getStringExtra(EXTRA_BARCODE_STRING);
                barcodeType = wedgeIntent.getStringExtra(EXTRA_BARCODE_TYPE);

                if (barcodestring != null && scanCallback != null) {
                    scanCallback.onScanSuccess(barcodeType, barcodestring);
                }
            }
        }
    }
}
