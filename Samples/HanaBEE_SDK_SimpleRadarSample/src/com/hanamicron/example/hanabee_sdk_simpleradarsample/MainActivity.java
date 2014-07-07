package com.hanamicron.example.hanabee_sdk_simpleradarsample;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanamicron.beacon.bluetooth.HanaBEE;
import com.hanamicron.beacon.model.BeaconInfo;
import com.hanamicron.beacon.model.PointDouble;
import com.hanamicron.beacon.util.PositionConverter;

public class MainActivity extends Activity {

	private final String TAG = MainActivity.class.getSimpleName();

	private HanaBEE myHanaBEESDK;
	private ScrollView svResultHolder;
	private TextView tvResultOut;
	private StringBuffer sbResultData = new StringBuffer();

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

				setRadarInfo(trackingResult.x, trackingResult.y, myHanaBEESDK.getPositionNodeList());

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

	ArrayList<BeaconInfo> positionNodeList = new ArrayList<BeaconInfo>();
	ArrayList<BeaconInfo> rangeNodeList = new ArrayList<BeaconInfo>();

	public void initBLE() {

		myHanaBEESDK = HanaBEE.getInstance(this);

		positionNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		positionNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		positionNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		positionNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		myHanaBEESDK.setPositionNodeInfo(positionNodeList);

		rangeNodeList.add(new BeaconInfo(0, 0, "90:59:AF:2A:AD:3F"));
		rangeNodeList.add(new BeaconInfo(0, 8, "90:59:AF:2A:94:68"));
		// rangeNodeList.add(new BeaconInfo(8, 8, "90:59:AF:2A:C4:04"));
		// rangeNodeList.add(new BeaconInfo(8, 0, "90:59:AF:2A:AD:4C"));

		myHanaBEESDK.setRangeNodeInfo(rangeNodeList);

	}

	private void initUI() {

		svResultHolder = (ScrollView) findViewById(R.id.svResultHolder);
		tvResultOut = (TextView) findViewById(R.id.tvResultOut);

		rlRadarHolder = (RelativeLayout) findViewById(R.id.rlRadarHolder);
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

				if (positionConverter == null) {
					radarHeight = radarWidth; // assume that it's square
					positionConverter = PositionConverter.getInstance(positionNodeList, radarWidth, radarHeight);
				}

				initBeaconPosition(positionNodeList);
			}
		});

		rlRadarMapUser = (RelativeLayout) findViewById(R.id.rlRadarMapUser);
		rlRadarMapUser.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		rlBeaconHolder = (RelativeLayout) findViewById(R.id.rlBeaconHolder);

	}

	public void initBeaconPosition(ArrayList<BeaconInfo> beaconList) {
		rlBeaconHolder.removeAllViews();
		int couponNumber = 0;
		for (final BeaconInfo b : beaconList) {
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

	private void setRadarInfo(double x, double y, ArrayList<BeaconInfo> positionNodeList) {

		if (positionConverter == null) {
			radarHeight = radarWidth; // assume that it's square
			positionConverter = PositionConverter.getInstance(positionNodeList, radarWidth, radarHeight);
		}

		Point userPoint = positionConverter.getUserScreenPos(x, y);

		rlRadarMapUser.animate().x(userPoint.x).y(userPoint.y);
		Log.d(TAG, "X:" + x + " Y:" + y + " outX:" + userPoint.x + " outY:" + userPoint.y);

	}

	private OnClickListener beaconClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			BeaconInfo b = (BeaconInfo) view.getTag();
			Toast.makeText(MainActivity.this, b.macAddr + " " + b.couponNumber, Toast.LENGTH_SHORT).show();
		}

	};

}
