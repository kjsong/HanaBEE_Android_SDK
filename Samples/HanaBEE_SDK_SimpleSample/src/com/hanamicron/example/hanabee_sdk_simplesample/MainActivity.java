package com.hanamicron.example.hanabee_sdk_simplesample;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hanamicron.beacon.bluetooth.HanaBEE;
import com.hanamicron.beacon.model.BeaconInfo;
import com.hanamicron.beacon.model.PointDouble;

public class MainActivity extends Activity {

	private final String TAG = MainActivity.class.getSimpleName();

	private final int REQUEST_BLUETOOTH_ON = 1000;

	private HanaBEE mHanaBEE;
	private ScrollView svResultHolder;
	private TextView tvResultOut;
	private StringBuffer sbResultData = new StringBuffer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		svResultHolder = (ScrollView) findViewById(R.id.svResultHolder);
		tvResultOut = (TextView) findViewById(R.id.tvResultOut);

		initBLE();
		checkBluetooth();

		startScan();
	}

	private void checkBluetooth() {
		Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBluetooth, REQUEST_BLUETOOTH_ON);
	}

	@Override
	protected void onResume() {
		sbResultData.setLength(0);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mHanaBEE.stopPositionScan();
		mHanaBEE.stopInfoScan();
		super.onPause();
	}

	public void initBLE() {

		// Turn on bluetooth by your own code
		mHanaBEE = HanaBEE.getInstance(this);
		
		// or, force start bluetooth.
		// mHanaBEE = HanaBEE.getInstance(this, true);

		// Interval should be less than 1000 (default).
		// mHanaBEE.setScanInterval(10000);

		ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
		positionNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		positionNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		positionNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		positionNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		mHanaBEE.setPositionNode(positionNodeList);

		ArrayList<BeaconInfo> infoNodeList = new ArrayList<BeaconInfo>();
		infoNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		infoNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		// rangeNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		// rangeNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		mHanaBEE.setInfoNode(infoNodeList);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_BLUETOOTH_ON:
			if (mHanaBEE.isEnabledBluetooth()) {
				startScan();
			}
			break;

		default:
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startScan() {
		if (mHanaBEE != null) {
			mHanaBEE.startPositionScan(new HanaBEE.PositionScanCallback() {
				@Override
				public void onPositionScan(PointDouble trackingResult) {

					if (sbResultData.length() > 10000) {
						sbResultData.setLength(0);
					}

					String out = String.format("posX: %.2f, posY: %.2f\n", trackingResult.x, trackingResult.y);
					Log.d(TAG, out);
					sbResultData.append(out);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvResultOut.setText(sbResultData.toString());
							svResultHolder.post(new Runnable() {
								@Override
								public void run() {
									svResultHolder.fullScroll(View.FOCUS_DOWN);
								}
							});
						}
					});
				}
			});
		}

		if (mHanaBEE != null) {
			mHanaBEE.startInfoScan(new HanaBEE.InfoScanCallback() {

				@Override
				public void onInfoScan(String macAddr, BeaconInfo beaconInfo) {

					if (sbResultData.length() > 10000) {
						sbResultData.setLength(0);
					}

					String out = String.format("MAC: %s. Range:%f\n", macAddr, beaconInfo.getRange());
					// Log.d(TAG, out);
					sbResultData.append(out);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvResultOut.setText(sbResultData.toString());
							svResultHolder.post(new Runnable() {
								@Override
								public void run() {
									svResultHolder.fullScroll(View.FOCUS_DOWN);
								}
							});
						}
					});
				}
			});
		}
	}

}
