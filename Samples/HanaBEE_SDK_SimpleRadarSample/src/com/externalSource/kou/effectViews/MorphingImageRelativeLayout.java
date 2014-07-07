package com.externalSource.kou.effectViews;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.externalSource.kou.effectViews.MorphingImageView.ANIMATION_MODE;

public class MorphingImageRelativeLayout extends RelativeLayout {

	private ImageView ivMain;
	private MorphingImageView ivEffect;

	public MorphingImageRelativeLayout(Context context) {
		super(context);
		init(context);
	}

	public MorphingImageRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.width = 200;
		lp.height = 200;
		setLayoutParams(lp);

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}

				ivMain = new ImageView(getContext());
				ivEffect = new MorphingImageView(getContext());

				RelativeLayout.LayoutParams mlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				mlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
				ivMain.setLayoutParams(mlp);
				RelativeLayout.LayoutParams elp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				elp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

				addView(ivMain);
				addView(ivEffect);

			}
		});
	}

	public void setAnimationSpeed(int speed) {
		ivEffect.setAnimationSpeed(speed);
	}

	public void setAnimationScaleUpPercent(float scaleUp) {
		ivEffect.setAnimationScaleUpPercent(scaleUp);
	}

	public void setAnimationMode(ANIMATION_MODE mode) {
		ivEffect.setAnimationMode(mode);
	}
}
