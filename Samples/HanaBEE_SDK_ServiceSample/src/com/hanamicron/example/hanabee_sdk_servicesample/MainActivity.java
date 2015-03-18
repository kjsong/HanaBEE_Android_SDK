package com.hanamicron.example.hanabee_sdk_servicesample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

	private final String TAG = MainActivity.class.getSimpleName();

	private Button btnStart;
	private Button btnStop;

	boolean isBackGroundServiceBound = false;
	private Messenger messengerToService = null;
	private Messenger messengerFromService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initUI();
		initClass();
		initService();
	}

	@Override
	protected void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	private void initUI() {
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStop = (Button) findViewById(R.id.btnStop);

		btnStart.setOnClickListener(this);
		btnStop.setOnClickListener(this);
	}

	private void initClass() {
		messengerFromService = new Messenger(new Handler(new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				handleMessageMainActivity(msg);
				return false;
			}
		}));
	}

	private void initService() {
		doBindService();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			messengerToService = new Messenger(service);
			if (messengerToService != null) {
				try {
					Message msg = Message.obtain(null, BackgroundService.BACKGROUND_SERVICE_MSG_START_INIT);
					msg.replyTo = messengerFromService;
					messengerToService.send(msg);
				} catch (RemoteException e) {
				}
			}

		}

		public void onServiceDisconnected(ComponentName className) {
			messengerToService = null;
		}
	};

	private void doBindService() {
		bindService(new Intent(this, BackgroundService.class), mConnection, Context.BIND_AUTO_CREATE);
		isBackGroundServiceBound = true;
	}

	private void doUnbindService() {
		if (isBackGroundServiceBound) {
			if (messengerToService != null) {
				try {
					Message msg = Message.obtain(null, BackgroundService.BACKGROUND_SERVICE_MSG_UNBIND);
					msg.replyTo = messengerFromService;
					messengerToService.send(msg);
				} catch (RemoteException e) {
				}
			}
			unbindService(mConnection);
			isBackGroundServiceBound = false;
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnStart:
			startBackgroundScan();
			break;

		case R.id.btnStop:
			stopBackgroundScan();
			break;
		}

	}

	private void handleMessageMainActivity(Message msg) {
		switch (msg.what) {

		}
	}

	private void startBackgroundScan() {
		Log.d(TAG, "startBackgroundScan()");
		if (isBackGroundServiceBound) {
			if (messengerToService != null) {
				try {
					Message msg = Message.obtain(null, BackgroundService.BACKGROUND_SERVICE_MSG_START_SCAN);
					msg.replyTo = messengerFromService;
					messengerToService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	private void stopBackgroundScan() {
		Log.d(TAG, "stopBackgroundScan()");
		if (isBackGroundServiceBound) {
			if (messengerToService != null) {
				try {
					Message msg = Message.obtain(null, BackgroundService.BACKGROUND_SERVICE_MSG_START_STOP);
					msg.replyTo = messengerFromService;
					messengerToService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}
}
