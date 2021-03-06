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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;

/**
 * A custom view that looks like the menu in <a href="https://path.com">Path
 * 2.0</a> (for iOS).
 * 
 * @author Capricorn
 * 
 */
public class ArcMenu extends RelativeLayout {

    private ArcLayout mArcLayout;
    private ToggleButton mDilateSwitcher;
    private CompoundButton.OnCheckedChangeListener mDilateListener = new CompoundButton.OnCheckedChangeListener() {

        @Implement
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mArcLayout.switchState(true);
        }
    };

    public ArcMenu(Context context) {
        super(context);
        init(context);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        applyAttrs(attrs);
    }

    private void init(Context context) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.arc_menu, this);

        mArcLayout = (ArcLayout) findViewById(R.id.item_layout);

        mDilateSwitcher = (ToggleButton) findViewById(R.id.control_switcher);
        mDilateSwitcher.setOnCheckedChangeListener(mDilateListener);
    }

    private void applyAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ArcLayout, 0, 0);

            float fromDegrees = a.getFloat(R.styleable.ArcLayout_fromDegrees, ArcLayout.DEFAULT_FROM_DEGREES);
            float toDegrees = a.getFloat(R.styleable.ArcLayout_toDegrees, ArcLayout.DEFAULT_TO_DEGREES);
            mArcLayout.setArc(fromDegrees, toDegrees);

            int defaultChildSize = mArcLayout.getChildSize();
            int newChildSize = a.getDimensionPixelSize(R.styleable.ArcLayout_childSize, defaultChildSize);
            mArcLayout.setChildSize(newChildSize);

            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int dilateBottom = mDilateSwitcher.getHeight() / 2;
        int self = getHeight() / 2;
        final int margin = dilateBottom - self;

        MarginLayoutParams margins = MarginLayoutParams.class.cast(getLayoutParams());
        margins.bottomMargin = margin < 0 ? margin : (~(margin - 1));
        setLayoutParams(margins);
    }

    public boolean isExpanded() {
        return mArcLayout.isExpanded();
    }

    public void openArc() {
        if (!mArcLayout.isExpanded()) mDilateSwitcher.setChecked(true);
    }

    public void closeArc() {
        if (mArcLayout.isExpanded()) mDilateSwitcher.setChecked(false);
    }

    public void qualifyControlHint(@DrawableRes int imgRes, @StringRes int description) {
        findViewById(R.id.control_switcher).setBackgroundResource(imgRes);
        findViewById(R.id.control_switcher).setContentDescription(getResources().getString(description));

        Common.makeToastable(findViewById(R.id.control_switcher));
    }

    public void addItem(View item, OnClickListener listener) {
        mArcLayout.addView(item);
        item.setVisibility(mArcLayout.isExpanded() ? VISIBLE : INVISIBLE);
        item.setOnClickListener(getItemClickListener(listener));
    }

    private OnClickListener getItemClickListener(final OnClickListener listener) {
        return new OnClickListener() {

            @Override
            public void onClick(final View viewClicked) {
                Animation animation = bindItemAnimation(viewClicked, true, 350);
                animation.setAnimationListener(new AnimationListener() {

                    @Implement public void onAnimationStart(Animation animation) {}
                    @Implement public void onAnimationRepeat(Animation animation) {}

                    @Implement
                    public void onAnimationEnd(Animation animation) {
                        postDelayed(new Runnable() {

                            @Implement
                            public void run() {
                                itemDidDisappear();
                            }
                        }, 0);
                    }
                });

                final int itemCount = mArcLayout.getChildCount();
                for (int i = 0; i < itemCount; i++) {
                    View item = mArcLayout.getChildAt(i);
                    if (viewClicked != item) {
                        bindItemAnimation(item, false, 350);
                    }
                }

                mArcLayout.invalidate();

                //drop switcher state to default avoiding switch anim processing
                mDilateSwitcher.setOnCheckedChangeListener(null);
                mDilateSwitcher.setChecked(false);
                mDilateSwitcher.setOnCheckedChangeListener(mDilateListener);

                if (listener != null) {
                    listener.onClick(viewClicked);
                }
            }
        };
    }

    private Animation bindItemAnimation(final View child, final boolean isClicked, final long duration) {
        Animation animation = createItemDisappearAnimation(duration, isClicked);
        child.setAnimation(animation);

        return animation;
    }

    private void itemDidDisappear() {
        final int itemCount = mArcLayout.getChildCount();
        for (int i = 0; i < itemCount; i++) {
            View item = mArcLayout.getChildAt(i);
            item.setVisibility(INVISIBLE);
            item.clearAnimation();
        }

        mArcLayout.switchState(false);
    }

    private static Animation createItemDisappearAnimation(final long duration, final boolean isClicked) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, isClicked ? 2.0f : 0.0f, 1.0f, isClicked ? 2.0f : 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

        animationSet.setDuration(duration);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setFillEnabled(true);

        return animationSet;
    }
}