package com.hanamicron.example.hanabee_sdk_simplesample;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hanamicron.beacon.bluetooth.Hanabee;
import com.hanamicron.beacon.bluetooth.Hanabee.ErrorCode;
import com.hanamicron.beacon.bluetooth.Hanabee.ProximityEvent;
import com.hanamicron.beacon.bluetooth.Hanabee.ProximityState;
import com.hanamicron.beacon.bluetooth.result.HanabeeResultCallback;
import com.hanamicron.beacon.model.BeaconHanabee;
import com.hanamicron.beacon.model.BeaconiBeacon;

public class MainActivity extends Activity implements OnClickListener {

	private final String TAG = MainActivity.class.getSimpleName();
	public final int REQUEST_BLUETOOTH = 10000;
	private final int DISPLAY_LENGTH = 5000;

	private ScrollView svResultHolder;
	private TextView tvResultOut;
	private Button btnStart;

	private Hanabee mHanabee;
	private StringBuffer sbResultData = new StringBuffer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
		initBLE();

		// Position (Radar)
		// final ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
		// positionNodeList.add(new BeaconInfo(0, 0, "00:18:9A:25:00:01"));
		// positionNodeList.add(new BeaconInfo(0, 8, "00:18:9A:25:00:02"));
		// positionNodeList.add(new BeaconInfo(8, 8, "00:18:9A:25:00:F2"));
		// positionNodeList.add(new BeaconInfo(8, 0, "00:18:9A:25:00:F3"));
		// mHanabee.setPositionNodeList(positionNodeList);

		// Proximity (State and Event)
		// ProximityList proximityList = new ProximityList();
		// proximityList.addProximityRegion("00:18:9A:25:FF:EE", 10, 1);
		// mHanabee.setProximityList(proximityList);
	}

	private void initUI() {
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(this);

		tvResultOut = (TextView) findViewById(R.id.tvResultOut);

		svResultHolder = (ScrollView) findViewById(R.id.svResultHolder);
	}

	public void initBLE() {

		// Turn on bluetooth by user popup.
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
		mHanabee = Hanabee.getInstance(getApplicationContext());

		// or, force start bluetooth.
		// mHanaBEE = mHanabee.getInstance(this, true);

		// Interval should be less than 1000 (default).
		mHanabee.setScanInterval(10000); // 3 sec scan -> 7 sec stop ...

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.btnStart:
			if (mHanabee.isRunning()) {
				mHanabee.stopHanabee();
				btnStart.setText("Start Hanabee");
			} else {
				startScan();
				btnStart.setText("Stop Hanabee");
			}
			break;
		}
	}

	public void startScan() {

		// //////////////////////////////////////
		// Set beacon information
		// //////////////////////////////////////

		if (mHanabee.isRunning()) {
			Log.d(TAG, "Hanabee is already running!");
		} else {

			mHanabee.startHanabee(new HanabeeResultCallback() {

				@Override
				public void onProximity(String macAddress, float radius, ProximityEvent event, ProximityState state, float range, double rssi) {
				}

				@Override
				public void onProximity(String uuid, int major, int minor, float radius, ProximityEvent event, ProximityState state, float range, double rssi, String macAddress) {
				}

				@Override
				public void onPosition(String floorID, double x, double y) {
				}

				@Override
				public void onError(ErrorCode error, String detailedReason) {
				}

				@Override
				public void onHanabee(BeaconHanabee hanabee, int rssi) {
					Log.d(TAG, "Hanabee Beacon.  uuid8:" + hanabee.getUUID08() + "batt:" + hanabee.getBattery());

					String scanResult = String.format("Hanabee MAC: %s / %s / %d / %d / %d%%\n\n",//
							hanabee.getMacAddress(), //
							hanabee.getUUID08(), hanabee.getMajor(), hanabee.getMinor(),//
							hanabee.getBattery());

					displayData(scanResult);
				}

				@Override
				public void oniBeacon(BeaconiBeacon iBeacon, int rssi) {
					Log.d(TAG, "iBeacon. uuid:" + iBeacon.getUUID16());

					String scanResult = String.format("iBeacon MAC: %s\n%s / %d / %d\n\n",//
							iBeacon.getMacAddress(), //
							iBeacon.getUUID16(), iBeacon.getMajor(), iBeacon.getMinor());

					displayData(scanResult);
				}

			});
		}

	}

	// //////////////////////////////////////
	// Display data
	// //////////////////////////////////////

	public void displayData(String scanResult) {
		if (sbResultData.length() > DISPLAY_LENGTH) {
			sbResultData.setLength(0);
		}

		sbResultData.append(scanResult);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvResultOut.setText(sbResultData.toString());
				svResultHolder.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

}
