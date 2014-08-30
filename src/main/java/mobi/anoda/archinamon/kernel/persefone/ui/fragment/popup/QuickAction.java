package mobi.anoda.archinamon.kernel.persefone.ui.fragment.popup;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

public class QuickAction extends CustomPopupWindow {

    public static final int ANIM_GROW_FROM_LEFT   = 1;
    public static final int ANIM_GROW_FROM_RIGHT  = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_AUTO             = 4;

    private final View                  mRootView;
    private final ImageView             mArrowUp;
    private final ImageView             mArrowDown;
    private final Animation             mTrackAnimation;
    private final LayoutInflater        mInflater;
    private       int                   mAnimationStyle;
    private       boolean               isAnimateTrack;
    private       ViewGroup             mTrack;
    private       ArrayList<ActionItem> mActionsList;

    public QuickAction(View anchor) {
        super(anchor);
        Context context = anchor.getContext();

        mActionsList = new ArrayList<>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = mInflater.inflate(R.layout.quickaction, null);
        mArrowDown = (ImageView) mRootView.findViewById(R.id.arrow_down);
        mArrowUp = (ImageView) mRootView.findViewById(R.id.arrow_up);

        build(mRootView);

        mTrackAnimation = AnimationUtils.loadAnimation(anchor.getContext(), R.anim.rail);
        mTrackAnimation.setInterpolator(new Interpolator() {

            @Implement
            public float getInterpolation(float time) {
                float inner = 1.55F * time - 1.1F;
                return 1.2F - inner * inner;
            }
        });
        mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);
        mAnimationStyle = ANIM_AUTO;
        isAnimateTrack = true;
    }

    /**
     * Get action item {@link View}
     *
     * @param title    action item title
     * @param icon     {@link Drawable} action item icon
     * @param listener {@link View.OnClickListener} action item listener
     *
     * @return action item {@link View}
     */
    private View getActionItem(String title, Drawable icon, View.OnClickListener listener) {
        LinearLayout container = (LinearLayout) mInflater.inflate(R.layout.action_item, null);
        ImageView img = (ImageView) container.findViewById(R.id.icon);
        TextView text = (TextView) container.findViewById(R.id.title);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
        } else {
            text.setVisibility(View.GONE);
        }

        if (listener != null) {
            container.setOnClickListener(listener);
        }

        return container;
    }

    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

        param.leftMargin = requestedX - arrowWidth / 2;

        hideArrow.setVisibility(View.INVISIBLE);
    }

    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

        switch (mAnimationStyle) {
            case ANIM_GROW_FROM_LEFT:
                mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                break;

            case ANIM_AUTO:
                if (arrowPos <= screenWidth / 4) {
                    mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
                } else if (arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
                    mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
                } else {
                    mPopupFrame.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
                }

                break;
        }
    }

    /**
     * Create action list
     */
    private void createActionList() {
        View view;
        String title;
        Drawable icon;
        OnClickListener listener;
        int index = 1;

        for (ActionItem item : mActionsList) {
            title = item.getTitle();
            icon = item.getIcon();
            listener = item.getCallback();

            view = getActionItem(title, icon, listener);
            view.setFocusable(true);
            view.setClickable(true);

            mTrack.addView(view, index);

            index++;
        }
    }

    /**
     * Set animation style
     *
     * @param animStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int animStyle) {
        mAnimationStyle = animStyle;
    }

    /**
     * Add action item
     *
     * @param actionitem {@link ActionItem}
     */
    public void addActionItem(ActionItem actionitem) {
        mActionsList.add(actionitem);
    }

    public void showPopup() {
        createActionList();
        show();

        int anchorLocation[] = new int[2];
        mActionView.getLocationOnScreen(anchorLocation);
        Rect rect = new Rect(anchorLocation[0], anchorLocation[1], anchorLocation[0] + mActionView.getWidth(), anchorLocation[1] + mActionView.getHeight());
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mRootView.measure(View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST));

        int rootWidth = mTrack.getMeasuredWidth();
        int rootHeight = mTrack.getMeasuredHeight();

        View header = mRootView.findViewById(R.id.header);
        View footer = mRootView.findViewById(R.id.footer);

        header.setLayoutParams(new LinearLayout.LayoutParams(rootWidth, header.getLayoutParams().height));
        footer.setLayoutParams(new LinearLayout.LayoutParams(rootWidth, footer.getLayoutParams().height));

        int viewWidth = mActionView.getWidth();
        int startX = rect.left + (viewWidth - rootWidth) / 2;
        int startY = rect.top - rootHeight;

        boolean onTop = true;

        if (rootHeight > mActionView.getTop()) {
            startY = rect.bottom;
            onTop = false;
        }

        showArrow(onTop ? R.id.arrow_up : R.id.arrow_down, rootWidth / 2);
        setAnimationStyle(viewWidth, rect.centerX(), onTop);
        mPopupFrame.showAtLocation(mActionView, Gravity.NO_GRAVITY, startX, startY);

        if (isAnimateTrack) {
            mTrack.startAnimation(mTrackAnimation);
        }
    }
}
