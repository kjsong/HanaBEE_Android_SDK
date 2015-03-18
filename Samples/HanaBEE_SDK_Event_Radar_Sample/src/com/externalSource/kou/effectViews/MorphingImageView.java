package com.externalSource.kou.effectViews;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;


public class MorphingImageView extends ImageView {

	private final int RESTART_ANIMATION = 1000;
	private int animationDuration = 300;
	private float scaleUp = 1.3f;
	private float scaledown = 0.77f;

	public static enum ANIMATION_MODE {
		ANIMATION_SCALE, ANIMATION_BUMP
	}

	private ANIMATION_MODE animationMode = ANIMATION_MODE.ANIMATION_SCALE;

	public MorphingImageView(Context context) {
		super(context);
		startAnimation();
	}

	public MorphingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		startAnimation();
	}

	/**
	 * default : 1200 This means period of animation sequence.
	 */
	public void setAnimationSpeed(int speed) {
		animationDuration = (int) (speed / (float) 4);
	}

	public void setAnimationScaleUpPercent(float scaleUp) {
		this.scaleUp = scaleUp;
		scaledown = 1 / this.scaleUp;
	}

	public void setAnimationMode(ANIMATION_MODE mode) {
		animationMode = mode;
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case RESTART_ANIMATION:
				startAnimation();
				break;
			}
		}
	};

	public void startAnimation() {

		clearAnimation();
		switch (animationMode) {
		case ANIMATION_BUMP:
			startImageBumpEffect();
			break;
		case ANIMATION_SCALE:
			startImageScaleEffect();
			break;
		}

	}

	private void startImageBumpEffect() {

		float anim1ToX = scaleUp;
		float anim1ToY = scaledown;

		float anim2ToX = scaledown;
		float anim2ToY = scaleUp;

		float anim3ToX = scaledown;
		float anim3ToY = scaleUp;

		float anim4ToX = scaleUp;
		float anim4ToY = scaledown;

		startAnimationSet(anim1ToX, anim1ToY, anim2ToX, anim2ToY, anim3ToX, anim3ToY, anim4ToX, anim4ToY);

	}

	private void startImageScaleEffect() {

		float anim1ToX = scaleUp;
		float anim1ToY = scaleUp;

		float anim2ToX = scaledown;
		float anim2ToY = scaledown;

		float anim3ToX = scaledown;
		float anim3ToY = scaledown;

		float anim4ToX = scaleUp;
		float anim4ToY = scaleUp;

		startAnimationSet(anim1ToX, anim1ToY, anim2ToX, anim2ToY, anim3ToX, anim3ToY, anim4ToX, anim4ToY);
	}

	private void startAnimationSet(float anim1ToX, float anim1ToY, float anim2ToX, float anim2ToY, float anim3ToX, float anim3ToY, float anim4ToX, float anim4ToY) {
		clearAnimation();

		Animation anim1ScaleUp = new ScaleAnimation(1, anim1ToX, 1, anim1ToY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim1ScaleUp.setStartOffset(0);
		anim1ScaleUp.setDuration(animationDuration);

		Animation anim2ScaleDown = new ScaleAnimation(1, anim2ToX, 1, anim2ToY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim2ScaleDown.setStartOffset(animationDuration * 1);
		anim2ScaleDown.setDuration(animationDuration);

		Animation anim3ScaleDown = new ScaleAnimation(1, anim3ToX, 1, anim3ToY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim3ScaleDown.setStartOffset(animationDuration * 2);
		anim3ScaleDown.setDuration(animationDuration);

		Animation anim4ScaleUp = new ScaleAnimation(1, anim4ToX, 1, anim4ToY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		anim4ScaleUp.setStartOffset(animationDuration * 3);
		anim4ScaleUp.setDuration(animationDuration);

		final AnimationSet animSet = new AnimationSet(true);
		animSet.setFillEnabled(true);

		animSet.addAnimation(anim1ScaleUp);
		animSet.addAnimation(anim2ScaleDown);
		animSet.addAnimation(anim3ScaleDown);
		animSet.addAnimation(anim4ScaleUp);
		animSet.setInterpolator(new LinearInterpolator());

		// animSet.setRepeatCount(Animation.INFINITE); // not working
		// animSet.setRepeatMode(Animation.INFINITE); // not working

		animSet.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				handler.sendEmptyMessage(RESTART_ANIMATION);
			}
		});
		startAnimation(animSet);

	}
}
