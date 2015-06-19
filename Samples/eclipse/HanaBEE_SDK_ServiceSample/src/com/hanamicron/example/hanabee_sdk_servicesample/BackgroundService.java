package com.hanamicron.example.hanabee_indoor_sdk_servicesample;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.hanamicron.beacon.bluetooth.Hanabee;
import com.hanamicron.beacon.bluetooth.Hanabee.ErrorCode;
import com.hanamicron.beacon.bluetooth.Hanabee.ProximityEvent;
import com.hanamicron.beacon.bluetooth.Hanabee.ProximityState;
import com.hanamicron.beacon.bluetooth.result.HanabeeResultCallback;
import com.hanamicron.beacon.model.BeaconHanabee;
import com.hanamicron.beacon.model.BeaconInfo;
import com.hanamicron.beacon.model.BeaconiBeacon;
import com.hanamicron.beacon.proximity.ProximityList;

public class BackgroundService extends Service {
	private final static String TAG = BackgroundService.class.getSimpleName();

	static final int BACKGROUND_SERVICE_MSG_START_INIT = 50000;
	static final int BACKGROUND_SERVICE_MSG_START_SCAN = 50001;
	static final int BACKGROUND_SERVICE_MSG_START_STOP = 50002;
	static final int BACKGROUND_SERVICE_MSG_RESULT = 50003;
	static final int BACKGROUND_SERVICE_MSG_UNBIND = 50004;

	static final String MSG_MAC_ADDRESS = "MSG_MAC_ADDRESS";
	static final String MSG_UUID = "MSG_UUID";
	static final String MSG_MAJOR = "MSG_MAJOR";
	static final String MSG_MINOR = "MSG_MINOR";
	static final String MSG_RSSI = "MSG_RSSI";

	private Hanabee mHanabee;
	private Handler handler = new Handler(); // To show Toast in service, you need to get main thread.

	@Override
	public void onCreate() {
		Log.d(TAG, "BackgroundService - onCreate()");
		super.onCreate();
		initBLE();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY; // run until explicitly stopped.
	}

	private Messenger messengerToActivity = null;
	final Messenger messengerFromActivity = new Messenger(new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			messengerToActivity = msg.replyTo;

			switch (msg.what) {

			case BACKGROUND_SERVICE_MSG_START_SCAN:
				startScan();
				break;

			case BACKGROUND_SERVICE_MSG_START_STOP:
				stopScan();
				break;

			}
			return false;
		}

	}));

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "BackgroundService - onBind()");
		return messengerFromActivity.getBinder();

	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "BackgroundService - onUnbind()");
		stopSelf();
		return super.onUnbind(intent);
	}

	public void initBLE() {

		// Turn on bluetooth by user popup.
		// mHanabee = Hanabee.getInstance(getApplicationContext());

		// or, force start bluetooth.
		mHanabee = Hanabee.getInstance(getApplicationContext(), true);

		// Interval should be less than 1000 (default).
		mHanabee.setScanInterval(1000);

		// Position (Radar)
		// final ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
		// positionNodeList.add(new BeaconInfo(0, 0, "00:18:9A:25:00:01"));
		// positionNodeList.add(new BeaconInfo(0, 8, "00:18:9A:25:00:02"));
		// positionNodeList.add(new BeaconInfo(8, 8, "00:18:9A:25:00:F2"));
		// positionNodeList.add(new BeaconInfo(8, 0, "00:18:9A:25:00:F3"));
		// mHanabee.setPositionNodeList(positionNodeList);

		// Proximity (State and Event)
		ProximityList proximityList = new ProximityList();
		proximityList.addProximityRegion("00:18:9A:25:FF:EE", 5, 1);
		mHanabee.setProximityList(proximityList);

	}

	private void sendResult(BeaconInfo device, int rssi) {

		Bundle b = new Bundle();

		// static final String MSG_MAC_ADDRESS = "MSG_MAC_ADDRESS";
		// static final String MSG_UUID = "MSG_UUID";
		// static final String MSG_MAJOR = "MSG_MAJOR";
		// static final String MSG_MINOR = "MSG_MINOR";
		// static final String MSG_RSSI = "MSG_RSSI";

		b.putString(MSG_MAC_ADDRESS, device.getMacAddress());
		b.putString(MSG_UUID, device.getUUID());
		b.putInt(MSG_MAJOR, device.getMajor());
		b.putInt(MSG_MINOR, device.getMinor());
		b.putInt(MSG_RSSI, rssi);

		Message msg = Message.obtain(null, BACKGROUND_SERVICE_MSG_RESULT);
		msg.setData(b);
		try {
			messengerToActivity.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void startScan() {

		if (mHanabee.isRunning()) {
			Log.d(TAG, "Hanabee is already running!");
		} else {

			mHanabee.startHanabee(new HanabeeResultCallback() {

				@Override
				public void onProximity(String macAddress, float radius, ProximityEvent event, ProximityState state, float range, double rssi) {
					Log.d(TAG, "onProximity macAddress: " + macAddress + ", event:" + event + " rssi:" + rssi);
					if (event != ProximityEvent.NO_EVENT) {
						final String macAddressForToast = macAddress;
						final ProximityEvent eventForToast = event;
						handler.post(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), "onProximity:  " + macAddressForToast + ", " + eventForToast.toString(), Toast.LENGTH_SHORT).show();
							}
						});
					}
				}

				@Override
				public void onProximity(String uuid, int major, int minor, float radius, ProximityEvent event, ProximityState state, float range, double rssi, String macAddress) {
					Log.d(TAG, "onProximity UUID");
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

					sendResult(hanabee, rssi);
				}

				@Override
				public void oniBeacon(final BeaconiBeacon iBeacon, int rssi) {
					Log.d(TAG, "iBeacon. uuid:" + iBeacon.getUUID16());

					sendResult(iBeacon, rssi);
				}

			});
		}

	}

	private void stopScan() {
		mHanabee.stopHanabee();
	}

}