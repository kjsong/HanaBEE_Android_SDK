package com.hanamicron.example.hanabee_sdk_simplesample;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
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
import com.hanamicron.beacon.util.PositionConverter;

public class MainActivity extends Activity implements OnClickListener {

	private final String TAG = MainActivity.class.getSimpleName();
	public final int REQUEST_BLUETOOTH = 10000;

	private final int DISPLAY_LENGTH = 5000;

	private Hanabee mHanabee;

	private ScrollView svResultHolder;
	private TextView tvResultOut;
	private StringBuffer sbResultData = new StringBuffer();
	private StringBuffer sbResultDataEvent = new StringBuffer();

	private Button btnStart;

	private Button btnStateEvent;
	private Button btnEventOnly;
	private Button btnRadar;

	private int screenMode = 0; // 0: State+Event, 1: Event Only, 2: radar

	private PositionConverter positionConverter;
	private RelativeLayout rlRadarHolder = null;
	private RelativeLayout rlBeaconHolder = null;
	private RelativeLayout rlRadarMapUser = null;
	private int radarWidth;
	private int radarHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
		initBLE();

		// Position (Radar)
		final ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
//		positionNodeList.add(new BeaconInfo(0, 0, "E0:C7:9D:61:C3:28"));
//		positionNodeList.add(new BeaconInfo(0, 8, "D0:39:72:CD:CF:68"));
//		positionNodeList.add(new BeaconInfo(8, 8, "D0:39:72:CD:CF:79"));
//		positionNodeList.add(new BeaconInfo(8, 0, "D0:39:72:CD:CF:87"));
		mHanabee.setPositionNodeList(positionNodeList);

		// Proximity (State and Event)
		ProximityList proximityList = new ProximityList();
//		proximityList.addProximityRegion("E0:C7:9D:61:C3:28", 10, 1);

		mHanabee.setProximityList(proximityList);

		initUIBeaconPosition(positionNodeList);

	}

	private void initUI() {

		svResultHolder = (ScrollView) findViewById(R.id.svResultHolder);
		tvResultOut = (TextView) findViewById(R.id.tvResultOut);

		rlRadarHolder = (RelativeLayout) findViewById(R.id.rlRadarHolder);
		rlRadarMapUser = (RelativeLayout) findViewById(R.id.rlRadarMapUser);
		rlRadarMapUser.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		rlBeaconHolder = (RelativeLayout) findViewById(R.id.rlBeaconHolder);

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(this);

		btnStateEvent = (Button) findViewById(R.id.btnStateEvent);
		btnEventOnly = (Button) findViewById(R.id.btnEventOnly);
		btnRadar = (Button) findViewById(R.id.btnRadar);

		btnStateEvent.setOnClickListener(this);
		btnEventOnly.setOnClickListener(this);
		btnRadar.setOnClickListener(this);

		setScreenMode(screenMode);
	}

	public void initBLE() {

		// Turn on bluetooth by user popup.
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH);
		mHanabee = Hanabee.getInstance(getApplicationContext());

		// or, force start bluetooth.
		// mHanaBEE = mHanabee.getInstance(this, true);

		// Interval should be less than 1000 (default).
		// mHanabee.setScanInterval(1000);

	}

	public void initUIBeaconPosition(ArrayList<BeaconInfo> positionNodeList) {

		if (radarWidth == 0 && radarHeight == 0) {
			radarWidth = rlRadarHolder.getMeasuredWidth();
			radarHeight = rlRadarHolder.getMeasuredHeight();
		}
		radarHeight = radarWidth; // assume that it's square
		positionConverter = PositionConverter.getInstance(positionNodeList, radarWidth, radarHeight);

		rlBeaconHolder.removeAllViews();
		int couponNumber = 0;
		for (final BeaconInfo b : positionNodeList) {
			b.couponNumber = couponNumber++;
			Point p = positionConverter.getBeaconScreenPos(b);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = p.x;
			lp.topMargin = p.y;

			ImageView ivBeacon = new ImageView(this);
			Drawable rd1 = getResources().getDrawable(R.drawable.radar_map_beacon_signal);

			ivBeacon.setImageDrawable(rd1);
			ivBeacon.setLayoutParams(lp);
			rlBeaconHolder.addView(ivBeacon);
			ivBeacon.setTag(b);
			ivBeacon.setOnClickListener(beaconClickListener);

		}
	}

	private OnClickListener beaconClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			BeaconInfo b = (BeaconInfo) view.getTag();
			Toast.makeText(MainActivity.this, b.getMacAddress() + " " + b.couponNumber, Toast.LENGTH_SHORT).show();
		}

	};

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

		case R.id.btnStateEvent:
			screenMode = 0;
			tvResultOut.setText("");
			tvResultOut.setText(sbResultData.toString());

			setScreenMode(screenMode);
			break;

		case R.id.btnEventOnly:
			screenMode = 1;
			tvResultOut.setText("");
			tvResultOut.setText(sbResultDataEvent.toString());

			setScreenMode(screenMode);
			break;

		case R.id.btnRadar:
			screenMode = 2;
			tvResultOut.setText("");

			rlRadarHolder.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

				@SuppressWarnings("deprecation")
				@Override
				public void onGlobalLayout() {
					if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						rlRadarHolder.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						rlRadarHolder.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}

					radarWidth = rlRadarHolder.getMeasuredWidth();
					radarHeight = rlRadarHolder.getMeasuredHeight();

					initUIBeaconPosition(mHanabee.getPositionNodeList());
				}
			});

			setScreenMode(screenMode);
			break;

		}

	}

	private void setScreenMode(int mode) {// 0: State+Event, 1: Event Only, 2: radar

		switch (mode) {
		case 0:
			btnStateEvent.setBackgroundColor(Color.GREEN);
			btnEventOnly.setBackgroundColor(Color.GRAY);
			btnRadar.setBackgroundColor(Color.GRAY);

			rlRadarHolder.setVisibility(View.GONE);
			svResultHolder.setVisibility(View.VISIBLE);
			break;
		case 1:
			btnStateEvent.setBackgroundColor(Color.GRAY);
			btnEventOnly.setBackgroundColor(Color.GREEN);
			btnRadar.setBackgroundColor(Color.GRAY);

			rlRadarHolder.setVisibility(View.GONE);
			svResultHolder.setVisibility(View.VISIBLE);
			break;
		case 2:
			btnStateEvent.setBackgroundColor(Color.GRAY);
			btnEventOnly.setBackgroundColor(Color.GRAY);
			btnRadar.setBackgroundColor(Color.GREEN);

			rlRadarHolder.setVisibility(View.VISIBLE);
			svResultHolder.setVisibility(View.GONE);
			break;
		}
	}

	private void setRadarInfo(double x, double y) {
		if (positionConverter != null) {
			Point userPoint = positionConverter.getUserScreenPos(x, y);
			rlRadarMapUser.animate().x(userPoint.x).y(userPoint.y);
			Log.d(TAG, "X:" + x + " Y:" + y + " outX:" + userPoint.x + " outY:" + userPoint.y);
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
					displayProximity(macAddress, radius, event, state, range, rssi);
				}

				@Override
				public void onProximity(String uuid, int major, int minor, float radius, ProximityEvent event, ProximityState state, float range, double rssi, String macAddress) {
					displayProximity(uuid, radius, event, state, range, rssi);
				}

				@Override
				public void onPosition(String floorID, double x, double y) {
					setRadarInfo(x, y);
				}

				@Override
				public void onError(ErrorCode error, String detailedReason) {
					displayError(error, detailedReason);
				}

				@Override
				public void onHanabee(BeaconHanabee hanabee, int rssi) {
					Log.d(TAG, "Hanabee Beacon.  uuid8:" + hanabee.getUUID08() + "batt:" + hanabee.getBattery());
				}

				@Override
				public void oniBeacon(BeaconiBeacon iBeacon, int rssi) {
					Log.d(TAG, "iBeacon. uuid:" + iBeacon.getUUID16());
				}

			});
		}

	}

	// //////////////////////////////////////
	// Display data
	// //////////////////////////////////////

	public void displayProximity(final String id, final float radius, final ProximityEvent event, final ProximityState state, final float range, final double rssi) {
		if (sbResultData.length() > DISPLAY_LENGTH) {
			sbResultData.setLength(0);
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Display Event only, but state information is available also.

				if (event == ProximityEvent.NO_EVENT) {
					sbResultData.append(id + " E:" + event + ", S:" + state + ", rad:" + radius + ", R:" + new DecimalFormat("#####.##").format(range) + "<br/>");
				} else {
					sbResultData.append(id + " E:" + event + ", S:" + state + ", rad:" + radius + ", R:" + new DecimalFormat("#####.##").format(range) + "<br/>");
					sbResultDataEvent.append(id + " E:" + event + ", S:" + state + ", rad:" + radius + ", R:" + new DecimalFormat("#####.##").format(range) + "<br/>");
				}

				String outputStr = "";
				if (screenMode == 0) {
					outputStr = sbResultData.toString();

				} else if (screenMode == 1) {
					outputStr = sbResultDataEvent.toString();
				}

				outputStr = getColoredString(outputStr);
				tvResultOut.setText(Html.fromHtml(outputStr), TextView.BufferType.SPANNABLE);
				svResultHolder.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public void displayPosition(String floorID, double x, double y) {
		if (sbResultData.length() > DISPLAY_LENGTH) {
			sbResultData.setLength(0);
		}

		sbResultData.append("[Position]" + floorID + ": (" + x + "," + y + ")\n");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvResultOut.setText(sbResultData.toString());
				svResultHolder.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	public void displayError(ErrorCode error, String detailedReason) {
		if (sbResultData.length() > DISPLAY_LENGTH) {
			sbResultData.setLength(0);
		}

		sbResultData.append("[Error]" + error + ":" + detailedReason + "\n");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvResultOut.setText(sbResultData.toString());
				svResultHolder.fullScroll(View.FOCUS_DOWN);
			}
		});
	}

	final String[] patterns = {//
	"APPROACH",//
			"APPEAR",//
			"ENTER",//
			"EXPIRED",//
			"LEAVE",//
			"STAYIN",//
			"STAYOUT",//
			"VANISHED",//
	};

	public String getColoredString(String outputStr) {
		for (String p : patterns) {
			outputStr = outputStr.replace(p, "<font color='red'>" + p + "</font>");
		}
		return outputStr;
	}
}
