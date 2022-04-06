package com.tickera.tickeraapp.wedgescan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tickera.tickeraapp.R;

public class WedgeContinuousScanActivity extends Activity {
	// Constants for Broadcast Receiver defined below.
	private static final String TAG = "IntentWedgeSample11";

	public static final String ACTION_DECODE = "com.android.decodewedge.decode_action";
	public static final String BARCODE_STRING = "com.android.decode.intentwedge.barcode_string";

	//start stop scan broadcast
	public static final String ACTION_START_DECODE = "com.android.decode.action.START_DECODE";
	public static final String ACTION_STOP_DECODE = "com.android.decode.action.STOP_DECODE";


	private BroadcastReceiver receiver = null;
	private IntentFilter filter = null;

	private TextView mscan_result,scan_num;
	private Button decodebutton,stopBtn;
	private Intent mSendIntent;

	private String barcodestring;
 	private String barcodeType;
	private String barcodeResult;
	private Boolean openProperty= false;
	private long mCount = 0;
	private WedgeScan wedgeScan = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_continuouns_scan);
		scan_num = (TextView) findViewById(R.id.scan_num);

		mscan_result = (TextView) findViewById(R.id.scan_result);


		decodebutton = (Button) findViewById(R.id.startBtn);
		wedgeScan = new WedgeScan(this, new WedgeScanCallback() {
			@Override
			public void onScanSuccess(String type, String result) {
				mCount++;
				mscan_result.setText("type" + type + ", content: " + result);
			}
		});
		decodebutton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "startBtn");
				wedgeScan.startScan();
			}
		});

		stopBtn= (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Log.i(TAG, "stopBtn");
				wedgeScan.stopScan();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Register dynamically decode wedge intent broadcast receiver.
		wedgeScan.register();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		wedgeScan.unregister();
	}

}
