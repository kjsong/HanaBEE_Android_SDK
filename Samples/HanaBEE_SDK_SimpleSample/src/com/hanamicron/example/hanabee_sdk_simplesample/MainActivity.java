package com.hanamicron.example.hanabee_sdk_simplesample;

import java.util.ArrayList;

import android.app.Activity;
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

	private HanaBEE myHanaBEESDK;
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
	}

	@Override
	protected void onResume() {

		sbResultData.setLength(0);

		myHanaBEESDK.startPositionScan(new HanaBEE.PositionScanCallback() {
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

		myHanaBEESDK.startRangeScan(new HanaBEE.RangeScanCallback() {

			@Override
			public void onRangeScan(String macAddr, double range) {

				if (sbResultData.length() > 10000) {
					sbResultData.setLength(0);
				}

				String out = String.format("MAC: %s. Range:%f\n", macAddr, range);
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

		super.onResume();
	}

	@Override
	protected void onPause() {
		myHanaBEESDK.stopPositionScan();
		myHanaBEESDK.stopRangeScan();
		super.onPause();
	}

	public void initBLE() {

		myHanaBEESDK = HanaBEE.getInstance(this);

		ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
		positionNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		positionNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		positionNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		positionNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		myHanaBEESDK.setPositionNodeInfo(positionNodeList);

		ArrayList<BeaconInfo> rangeNodeList = new ArrayList<BeaconInfo>();
		rangeNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		rangeNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		// rangeNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		// rangeNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		myHanaBEESDK.setRangeNodeInfo(rangeNodeList);

	}
}
