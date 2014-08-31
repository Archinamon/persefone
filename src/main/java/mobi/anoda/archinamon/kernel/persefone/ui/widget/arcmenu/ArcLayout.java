/*
 * Copyright (C) 2012 Capricorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mobi.anoda.archinamon.kernel.persefone.ui.widget.arcmenu;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.utils.AnimationBundle;
import mobi.anoda.archinamon.kernel.persefone.ui.utils.Ease;
import mobi.anoda.archinamon.kernel.persefone.ui.utils.Tweener;

/**
 * A Layout that arranges its children around its center. The arc can be set by calling {@link #setArc(float, float) setArc()}. You can override the method {@link #onMeasure(int, int) onMeasure()}, otherwise it is always WRAP_CONTENT.
 *
 * @author Capricorn
 */
public class ArcLayout extends ViewGroup {

    /**
     * children will be set the same size.
     */
    private int mChildSize;
    private              int   mChildPadding        = 30;
    private              int   mLayoutPadding       = 10;
    public static final  float DEFAULT_FROM_DEGREES = 270.0f;
    public static final  float DEFAULT_TO_DEGREES   = 360.0f;
    private              float mFromDegrees         = DEFAULT_FROM_DEGREES;
    private              float mToDegrees           = DEFAULT_TO_DEGREES;
    private static final int   MIN_RADIUS           = 150;
    /* the distance between the layout's center and any child's center */
    private int mRadius;
    private boolean         mExpanded         = false;
    private AnimationBundle mTargetAnimations = new AnimationBundle();

    public ArcLayout(Context context) {
        super(context);
    }

    public ArcLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ArcLayout, 0, 0);
            mFromDegrees = a.getFloat(R.styleable.ArcLayout_fromDegrees, DEFAULT_FROM_DEGREES);
            mToDegrees = a.getFloat(R.styleable.ArcLayout_toDegrees, DEFAULT_TO_DEGREES);
            mChildSize = Math.max(a.getDimensionPixelSize(R.styleable.ArcLayout_childSize, 0), 0);

            a.recycle();
        }
    }

    private static int computeRadius(final float arcDegrees, final int childCount, final int childSize, final int childPadding, final int minRadius) {
        if (childCount < 2) {
            return minRadius;
        }

        final float perDegrees = arcDegrees / (childCount - 1);
        final float perHalfDegrees = perDegrees / 2;
        final int perSize = childSize + childPadding;

        final int radius = (int) ((perSize / 2) / Math.sin(Math.toRadians(perHalfDegrees)));

        return Math.max(radius, minRadius);
    }

    private static Rect computeChildFrame(final int centerX, final int centerY, final int radius, final float degrees, final int size) {
        final double childCenterX = centerX + radius * Math.cos(Math.toRadians(degrees));
        final double childCenterY = centerY + radius * Math.sin(Math.toRadians(degrees));

        return new Rect((int) (childCenterX - size / 2), (int) (childCenterY - size / 2), (int) (childCenterX + size / 2), (int) (childCenterY + size / 2));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int radius = mRadius = computeRadius(Math.abs(mToDegrees - mFromDegrees), getChildCount(), mChildSize, mChildPadding, MIN_RADIUS);
        final int size = radius * 2 + mChildSize + mChildPadding + mLayoutPadding * 2;

        setMeasuredDimension(size, size);

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mChildSize, MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;
        final int radius = mExpanded ? mRadius : 0;

        final int childCount = getChildCount();
        final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);

        float degrees = mFromDegrees;
        for (int i = 0; i < childCount; i++) {
            Rect frame = computeChildFrame(centerX, centerY, radius, degrees, mChildSize);
            degrees += perDegrees;
            getChildAt(i).layout(frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    /**
     * refers to {@link LayoutAnimationController#getDelayForView(View view)}
     */
    private static long computeStartOffset(final int childCount, final boolean expanded, final int index, final float delayPercent, final long duration, Interpolator interpolator) {
        final float delay = delayPercent * duration;
        final long viewDelay = (long) (getTransformedIndex(expanded, childCount, index) * delay);
        final float totalDelay = delay * childCount;

        float normalizedDelay = viewDelay / totalDelay;
        normalizedDelay = interpolator.getInterpolation(normalizedDelay);

        return (long) (normalizedDelay * totalDelay);
    }

    private static int getTransformedIndex(final boolean expanded, final int count, final int index) {
        if (expanded) {
            return count - 1 - index;
        }

        return index;
    }

    private void createExpandAnimation(ViewPropertyAnimator animator, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long startOffset, long duration, Interpolator interpolator) {
        mTargetAnimations.stop();
        for (int i = 0; i < getChildCount(); i++) {
            final View target = getChildAt(i);
            mTargetAnimations.add(Tweener.to(target, duration,
                                             "ease", Ease.Cubic.easeOut,
                                             "alpha", 1.0f,
                                             "scaleX", 1.0f,
                                             "scaleY", 1.0f,
                                             "delay", startOffset,
                                             "onUpdate", new AnimatorUpdateListener() {

                        @Implement
                        public void onAnimationUpdate(ValueAnimator animation) {
                            invalidate();
                        }
                    }));
        }

        mTargetAnimations.start();

//        animator.setStartDelay(startOffset);
//        animator.setDuration(duration);
//        animator.setInterpolator(interpolator);
//        animator.translationX(toXDelta / 7);
//        animator.translationY(toYDelta / 7);
    }

    private void createShrinkAnimation(ViewPropertyAnimator animator, float fromXDelta, float toXDelta, float fromYDelta, float toYDelta, long startOffset, long duration, Interpolator interpolator) {
        mTargetAnimations.cancel();

        final TimeInterpolator interpolator2 = Ease.Cubic.easeOut;
        for (int i = 0; i < getChildCount(); i++) {
            final View target = getChildAt(i);
            mTargetAnimations.add(Tweener.to(target, duration,
                                             "ease", interpolator2,
                                             "alpha", 0.0f,
                                             "scaleX", 0.8f,
                                             "scaleY", 0.8f,
                                             "delay", startOffset,
                                             "onUpdate", new AnimatorUpdateListener() {

                        @Implement
                        public void onAnimationUpdate(ValueAnimator animation) {
                            invalidate();
                        }
                    }));
        }

        mTargetAnimations.start();

//        final long preDuration = duration / 2;
//
//        animator.translationX(toXDelta * 2);
//        animator.translationY(toYDelta * 2);
//        animator.setStartDelay(startOffset + preDuration);
//        animator.setDuration(duration - preDuration);
//        animator.setInterpolator(interpolator);
    }

    private void bindChildAnimation(final View child, final int index, final long duration) {
        final boolean expanded = mExpanded;
        final int centerX = getWidth() / 2;
        final int centerY = getHeight() / 2;
        final int radius = expanded ? 0 : mRadius;

        final int childCount = getChildCount();
        final float perDegrees = (mToDegrees - mFromDegrees) / (childCount - 1);
        Rect frame = computeChildFrame(centerX, centerY, radius, mFromDegrees + index * perDegrees, mChildSize);

        final int toXDelta = frame.left - child.getLeft();
        final int toYDelta = frame.top - child.getTop();

        Interpolator interpolator = mExpanded ? new AccelerateInterpolator() : new BounceInterpolator();
        final long startOffset = computeStartOffset(childCount, mExpanded, index, 0.1f, duration, interpolator);

//        ViewPropertyAnimator animator = child.animate();
//        if (animator != null) {
            if (mExpanded)
                createShrinkAnimation(null, 0, toXDelta, 0, toYDelta, startOffset, duration, interpolator);
            else
                createExpandAnimation(null, 0, toXDelta, 0, toYDelta, startOffset, duration, interpolator);

//            animator.setListener(new AnimatorListenerAdapter() {
//
//                @Override
//                public void onAnimationStart(Animator animation) {
//                    if (mExpanded) child.setVisibility(VISIBLE);
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    if (!mExpanded) child.setVisibility(INVISIBLE);
//                }
//            });
//        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setArc(float fromDegrees, float toDegrees) {
        if (mFromDegrees == fromDegrees && mToDegrees == toDegrees) {
            return;
        }

        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        requestLayout();
    }

    public void setChildSize(int size) {
        if (mChildSize == size || size < 0) {
            return;
        }

        mChildSize = size;

        requestLayout();
    }

    public int getChildSize() {
        return mChildSize;
    }

    /**
     * switch between expansion and shrinkage
     *
     * @param showAnimation
     */
    public void switchState(final boolean showAnimation) {
        if (showAnimation) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                bindChildAnimation(getChildAt(i), i, 300);
            }
        }

        mExpanded = !mExpanded;

        if (!showAnimation) {
            requestLayout();
        }
    }
}