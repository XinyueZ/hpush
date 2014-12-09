package com.hpush.views;

import android.view.View;
import android.view.View.OnClickListener;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Click listener for some animations.
 *
 * @author Xinyue Zhao
 */
public   abstract class OnViewAnimatedClickedListener3 implements OnClickListener {
	/**
	 * Impl. Event what user clicks.
	 */
	public abstract void onClick();

	@Override
	public final void onClick(final View v) {
		v.setEnabled(false);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(ObjectAnimator.ofFloat(v, "rotationX", 0, 360f));
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				onClick();
				v.setEnabled(true);
			}
		});
		animatorSet.start();
	}
}
