package com.externalSource.kou.AutoRotatingImageView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AutoRotatingImageView extends ImageView {

	private int UPDATE_INTERVAL_MILLIS = 10;
	private float UPDATE_DEGREE_AMOUNT = 2.5f;// plus : CW, minus : CCW

	private Rect rect = new Rect();
	private float degree = 0; // comment

	private Handler handler = new Handler();

	public AutoRotatingImageView(Context context) {
		super(context);
		init();
	}

	public AutoRotatingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AutoRotatingImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setUpdateIntervalMillis(int millis) {
		UPDATE_INTERVAL_MILLIS = millis;
	}

	/**
	 * setUpdateDegreeAmount
	 * 
	 * @param degree
	 *            plus:CW, minus:CCW
	 */
	public void setUpdateDegreeAmount(float degree) {
		UPDATE_DEGREE_AMOUNT = degree;
	}

	private void init() {
		handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MILLIS);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.getClipBounds(rect);

		int height = rect.bottom - rect.top;
		int width = rect.right - rect.left;

		canvas.rotate(degree, height / 2, width / 2);
		degree += UPDATE_DEGREE_AMOUNT;
		degree = degree % 360;

		super.onDraw(canvas);
		canvas.restore();
	}

	private Runnable updateRunnable = new Runnable() {

		@Override
		public void run() {
			invalidate();
			handler.postDelayed(updateRunnable, UPDATE_INTERVAL_MILLIS);
		}
	};
}
