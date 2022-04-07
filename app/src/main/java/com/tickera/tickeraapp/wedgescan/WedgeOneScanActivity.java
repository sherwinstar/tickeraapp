package com.tickera.tickeraapp.wedgescan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tickera.tickeraapp.R;

public class WedgeOneScanActivity extends Activity {
	private static final String TAG = "IntentWedgeSample";

	/* 扫码结果接收广播地址 */
	public static final String ACTION_BROADCAST_RECEIVER = "com.android.decodewedge.decode_action";
	public static final String EXTRA_BARCODE_STRING = "com.android.decode.intentwedge.barcode_string"; /* 扫码结果 */
	public static final String EXTRA_BARCODE_TYPE = "com.android.decode.intentwedge.barcode_type";    /* 条码类型 */


	/* 控制扫码开关广播地址, 设备扫码按键默认会触发扫码，APP端一般需要接收结果即可，不需要控制扫码，除非需要在APP里面自己触发扫码 */
	public static final String ACTION_START_DECODE = "com.android.decode.action.START_DECODE";
	public static final String ACTION_STOP_DECODE = "com.android.decode.action.STOP_DECODE";

	private BroadcastReceiver receiver = null;
	private IntentFilter filter = null;

	private TextView mscan_result;
	private Button decodebutton;

	private String barcodestring;
 	private String barcodeType;
	private String barcodeResult;
	private WedgeScan wedgeScan = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_one_scan);

		mscan_result = (TextView) findViewById(R.id.scan_result);
		decodebutton = (Button) findViewById(R.id.startBtn);
		decodebutton.setOnTouchListener(new Button.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				final int action = event.getAction();
				switch (action) {
					case MotionEvent.ACTION_DOWN:
						decodebutton.setBackgroundResource(R.mipmap.android_pressed);
						try {
							startDecode();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						break;
					case MotionEvent.ACTION_UP:
						try {
							decodebutton.setBackgroundResource(R.mipmap.android_normal);
							stopDecode();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
				}
				return true;
			}
		});

		wedgeScan = new WedgeScan(this, new WedgeScanCallback() {
			@Override
			public void onScanSuccess(String type, String result) {
				mscan_result.setText("type " + type + ", content: " + result);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		wedgeScan.register();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		wedgeScan.unregister();
	}

	// Creates an intent and stop decoding.
	private void stopDecode() {
		wedgeScan.stopDecode();
	}

	// Creates an intent and start decoding.
	private void startDecode() {
		wedgeScan.startDecode();
	}

}
