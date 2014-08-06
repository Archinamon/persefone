package mobi.anoda.archcore.persefone.ui.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import org.jetbrains.annotations.NotNull;
import mobi.anoda.archcore.persefone.BuildConfig;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class PinnableListView extends ListView {

    //-- inner classes

    public static interface IOnViewPinnedCallback {

        void onViewPinned(View pinnedView);

        void onViewUnpinned();
    }

    /** List adapter to be implemented for being used with PinnedSectionListView adapter. */
    public static interface PinnedSectionAdapter extends ListAdapter {
        /** This method shall return 'true' if views of given type has to be pinned. */
        boolean isItemViewTypePinned(int viewType);
    }

    /** Wrapper class for pinned section view and its position in the list. */
    static class PinnedSection {

        public View mView;
        public int  mPosition;
        public long mId;
    }

    //-- class fields

    // fields used for handling touch events
    private final Rect   mTouchRect  = new Rect();
    private final PointF mTouchPoint = new PointF();
    private int         mTouchSlop;
    private View        mTouchTarget;
    private MotionEvent mDownEvent;

    private Context mContext;

    // fields used for drawing shadow under a pinned section
    private GradientDrawable mShadowDrawable;
    private int              mSectionsDistanceY;
    private int              mShadowHeight;

    private IOnViewPinnedCallback mPinCallback;

    /** Delegating listener, can be null. */
    OnScrollListener mDelegateOnScrollListener;

    /** Shadow for being recycled, can be null. */
    PinnedSection mRecycleSection;

    /** shadow instance with a pinned view, can be null. */
    PinnedSection mPinnedSection;

    /** Pinned view Y-translation. We use it to stick pinned view to the next section. */
    int mTranslateY;

    /** Scroll listener which does the magic */
    private final OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mDelegateOnScrollListener != null) { // delegate
                mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (mDelegateOnScrollListener != null) { // delegate
                mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            // get expected adapter or fail fast
            ListAdapter adapter = getAdapter();
            if (adapter == null || visibleItemCount == 0)
                return; // nothing to do

            final boolean isFirstVisibleItemSection = isItemViewTypePinned(adapter, adapter.getItemViewType(firstVisibleItem));

            if (isFirstVisibleItemSection) {
                View sectionView = getChildAt(0);
                if (sectionView != null && sectionView.getTop() == getPaddingTop()) { // view sticks to the top, no need for pinned shadow
                    destroyPinnedShadow();
                } else { // section doesn't stick to the top, make sure we have a pinned shadow
                    ensureShadowForPosition(firstVisibleItem, firstVisibleItem, visibleItemCount);
                }

            } else { // section is not at the first visible position
                int sectionPosition = findCurrentSectionPosition(firstVisibleItem);
                if (sectionPosition > -1) { // we have section position
                    ensureShadowForPosition(sectionPosition, firstVisibleItem, visibleItemCount);
                } else { // there is no section for the first visible item, destroy shadow
                    destroyPinnedShadow();
                }
            }
        }
    };

    /** Default change observer. */
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            recreatePinnedShadow();
        }

        @Override
        public void onInvalidated() {
            recreatePinnedShadow();
        }
    };

    //-- constructors

    public PinnableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinnableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }

    private void initView() {
        setOnScrollListener(mOnScrollListener);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        initShadow(true);
    }

    //-- public API methods

    public void setShadowVisible(boolean visible) {
        initShadow(visible);
        if (mPinnedSection != null) {
            View v = mPinnedSection.mView;
            invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom() + mShadowHeight);
        }
    }

    public void setPinCallback(IOnViewPinnedCallback callback) {
        mPinCallback = callback;
    }

    //-- pinned section drawing methods

    public void initShadow(boolean visible) {
        if (visible) {
            if (mShadowDrawable == null) {
                mShadowDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, new int[] {Color.parseColor("#ffa0a0a0"),
                                                                                          Color.parseColor("#50a0a0a0"),
                                                                                          Color.parseColor("#00a0a0a0")});
                mShadowHeight = (int) (8 * mContext.getResources().getDisplayMetrics().density);
            }
        } else {
            if (mShadowDrawable != null) {
                mShadowDrawable = null;
                mShadowHeight = 0;
            }
        }
    }

    /** Create shadow wrapper with a pinned view for a view at given position */
    void createPinnedShadow(int position) {
        // try to recycle shadow
        PinnedSection pinnedShadow = mRecycleSection;
        mRecycleSection = null;

        // create new shadow, if needed
        if (pinnedShadow == null) pinnedShadow = new PinnedSection();
        // request new view using recycled view, if such
        View pinnedView = findDecorAdapter(getAdapter()).getView(position, pinnedShadow.mView, PinnableListView.this);
        assert pinnedView != null;

        // read layout parameters
        LayoutParams layoutParams = (LayoutParams) pinnedView.getLayoutParams();
        if (layoutParams == null) { // create default layout params
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }

        pinnedView.setLayoutParams(layoutParams);

        int heightMode = MeasureSpec.getMode(layoutParams.height);
        int heightSize = MeasureSpec.getSize(layoutParams.height);

        if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

        int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
        if (heightSize > maxHeight) heightSize = maxHeight;

        // measure & layout
        int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
        int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        pinnedView.measure(ws, hs);
        pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
        mTranslateY = 0;

        // initialize pinned shadow
        pinnedShadow.mView = pinnedView;
        pinnedShadow.mPosition = position;
        pinnedShadow.mId = findDecorAdapter(getAdapter()).getItemId(position);

        // store pinned shadow
        mPinnedSection = pinnedShadow;

        if (mPinCallback != null) {
            mPinCallback.onViewPinned(pinnedView);
        }
    }

    /** Destroy shadow wrapper for currently pinned view */
    void destroyPinnedShadow() {
        if (mPinnedSection != null) {
            // keep shadow for being recycled later
            mRecycleSection = mPinnedSection;
            mPinnedSection = null;

            if (mPinCallback != null) {
                mPinCallback.onViewUnpinned();
            }
        }
    }

    /** Makes sure we have an actual pinned shadow for given position. */
    void ensureShadowForPosition(int sectionPosition, int firstVisibleItem, int visibleItemCount) {
        if (visibleItemCount < 2) { // no need for creating shadow at all, we have a single visible item
            destroyPinnedShadow();
            return;
        }

        if (mPinnedSection != null
            && mPinnedSection.mPosition != sectionPosition) { // invalidate shadow, if required
            destroyPinnedShadow();
        }

        if (mPinnedSection == null) { // create shadow, if empty
            createPinnedShadow(sectionPosition);
        }

        // align shadow according to next section position, if needed
        int nextPosition = sectionPosition + 1;
        if (nextPosition < getCount()) {
            int nextSectionPosition = findFirstVisibleSectionPosition(nextPosition,
                                                                      visibleItemCount - (nextPosition - firstVisibleItem));
            if (nextSectionPosition > -1) {
                View nextSectionView = getChildAt(nextSectionPosition - firstVisibleItem);
                final int bottom = mPinnedSection.mView.getBottom() + getPaddingTop();

                if (nextSectionView != null) {
                    mSectionsDistanceY = nextSectionView.getTop() - bottom;
                }

                if (mSectionsDistanceY < 0) {
                    // next section overlaps pinned shadow, move it up
                    mTranslateY = mSectionsDistanceY;
                } else {
                    // next section does not overlap with pinned, stick to top
                    mTranslateY = 0;
                }
            } else {
                // no other sections are visible, stick to top
                mTranslateY = 0;
                mSectionsDistanceY = Integer.MAX_VALUE;
            }
        }
    }

    int findFirstVisibleSectionPosition(int firstVisibleItem, int visibleItemCount) {
        ListAdapter adapter = findDecorAdapter(getAdapter());

        for (int childIndex = 0; childIndex < visibleItemCount; childIndex++) {
            int position = firstVisibleItem + childIndex;
            int viewType = adapter.getItemViewType(position);
            if (isItemViewTypePinned(adapter, viewType)) return position;
        }
        return -1;
    }

    int findCurrentSectionPosition(int fromPosition) {
        ListAdapter adapter = findDecorAdapter(getAdapter());

        if (adapter instanceof SectionIndexer) {
            // try fast way by asking section indexer
            SectionIndexer indexer = (SectionIndexer) adapter;
            int sectionPosition = indexer.getSectionForPosition(fromPosition);
            int itemPosition = indexer.getPositionForSection(sectionPosition);
            int typeView = adapter.getItemViewType(itemPosition);
            if (isItemViewTypePinned(adapter, typeView)) {
                return itemPosition;
            } // else, no luck
        }

        // try slow way by looking through to the next section item above
        for (int position=fromPosition; position>=0; position--) {
            int viewType = adapter.getItemViewType(position);
            if (isItemViewTypePinned(adapter, viewType)) return position;
        }
        return -1; // no candidate found
    }

    void recreatePinnedShadow() {
        destroyPinnedShadow();
        ListAdapter adapter = findDecorAdapter(getAdapter());
        if (adapter != null && adapter.getCount() > 0) {
            int firstVisiblePosition = getFirstVisiblePosition();
            int sectionPosition = findCurrentSectionPosition(firstVisiblePosition);
            if (sectionPosition == -1) return; // no views to pin, exit
            ensureShadowForPosition(sectionPosition,
                                    firstVisiblePosition, getLastVisiblePosition() - firstVisiblePosition);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener listener) {
        if (listener == mOnScrollListener) {
            super.setOnScrollListener(listener);
        } else {
            mDelegateOnScrollListener = listener;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        post(new Runnable() {
            @Override public void run() {
                recreatePinnedShadow();
            }
        });
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        // assert adapter in debug mode
        adapter = findDecorAdapter(adapter);
        if (BuildConfig.DEBUG && findDecorAdapter(adapter) != null) {
            if (adapter.getViewTypeCount() < 2)
                throw new IllegalArgumentException("Does your adapter handle at least two types" +
                                                   " of views in getViewTypeCount() method: items and sections?");
        }

        // unregister observer at old adapter and register on new one
        ListAdapter oldAdapter = getAdapter();
        if (oldAdapter != null) oldAdapter.unregisterDataSetObserver(mDataSetObserver);
        if (adapter != null) adapter.registerDataSetObserver(mDataSetObserver);

        // destroy pinned shadow, if new adapter is not same as old one
        if (oldAdapter != adapter) destroyPinnedShadow();

        super.setAdapter(adapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mPinnedSection != null) {
            int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
            int shadowWidth = mPinnedSection.mView.getWidth();
            if (parentWidth != shadowWidth) {
                recreatePinnedShadow();
            }
        }
    }

    @Override
    protected void dispatchDraw(@NotNull Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mPinnedSection != null) {
            // prepare variables
            int pLeft = getListPaddingLeft();
            int pTop = getListPaddingTop();
            View view = mPinnedSection.mView;

            // draw child
            canvas.save();

            int clipHeight = view.getHeight() +
                             (mShadowDrawable == null ? 0 : Math.min(mShadowHeight, mSectionsDistanceY));
            canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop + clipHeight);

            canvas.translate(pLeft, pTop + mTranslateY);
            drawChild(canvas, mPinnedSection.mView, getDrawingTime());

            if (mShadowDrawable != null && mSectionsDistanceY > 0) {
                mShadowDrawable.setBounds(mPinnedSection.mView.getLeft(),
                                          mPinnedSection.mView.getBottom(),
                                          mPinnedSection.mView.getRight(),
                                          mPinnedSection.mView.getBottom() + mShadowHeight);
                mShadowDrawable.draw(canvas);
            }

            canvas.restore();
        }
    }

    //-- touch handling methods

    @Override
    public boolean dispatchTouchEvent(@NotNull MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN
            && mTouchTarget == null
            && mPinnedSection != null
            && isPinnedViewTouched(mPinnedSection.mView, x, y)) { // create touch target

            // user touched pinned view
            mTouchTarget = mPinnedSection.mView;
            mTouchPoint.x = x;
            mTouchPoint.y = y;

            // copy down event for eventually be used later
            mDownEvent = MotionEvent.obtain(ev);
        }

        if (mTouchTarget != null) {
            final boolean handled = isPinnedViewTouched(mTouchTarget, x, y) && mTouchTarget.dispatchTouchEvent(ev);

            if (handled) {
                invalidate();
            }

            if (action == MotionEvent.ACTION_UP) { // perform onClick on pinned view
                super.dispatchTouchEvent(ev);
                performPinnedItemClick();
                clearTouchTarget();
            } else if (action == MotionEvent.ACTION_CANCEL) { // cancel
                clearTouchTarget();
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (Math.abs(y - mTouchPoint.y) > mTouchSlop) {
                    // cancel sequence on touch target
                    MotionEvent event = MotionEvent.obtain(ev);
                    if (event != null) {
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        mTouchTarget.dispatchTouchEvent(event);
                        event.recycle();
                    }

                    // provide correct sequence to super class for further handling
                    super.dispatchTouchEvent(mDownEvent);
                    super.dispatchTouchEvent(ev);
                    clearTouchTarget();
                }
            }

            return true;
        }

        // call super if this was not our pinned view
        return super.dispatchTouchEvent(ev);
    }

    private boolean isPinnedViewTouched(View view, float x, float y) {
        view.getHitRect(mTouchRect);

        // by taping top or bottom padding, the list performs on click on a border item.
        // we don't add top padding here to keep behavior consistent.
        mTouchRect.top += mTranslateY;

        mTouchRect.bottom += mTranslateY + getPaddingTop();
        mTouchRect.left += getPaddingLeft();
        mTouchRect.right -= getPaddingRight();
        return mTouchRect.contains((int)x, (int)y);
    }

    private void clearTouchTarget() {
        mTouchTarget = null;
        if (mDownEvent != null) {
            mDownEvent.recycle();
            mDownEvent = null;
        }
    }

    private boolean performPinnedItemClick() {
        if (mPinnedSection == null) return false;

        OnItemClickListener listener = getOnItemClickListener();
        if (listener != null) {
            View view =  mPinnedSection.mView;
            playSoundEffect(SoundEffectConstants.CLICK);
            if (view != null) {
                view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
            }
            listener.onItemClick(this, view, mPinnedSection.mPosition, mPinnedSection.mId);
            return true;
        }
        return false;
    }

    public static boolean isItemViewTypePinned(ListAdapter adapter, int viewType) {
        adapter = findDecorAdapter(adapter);
        return adapter != null && ((PinnedSectionAdapter) adapter).isItemViewTypePinned(viewType);
    }

    private static PinnedSectionAdapter findDecorAdapter(ListAdapter adapter) {
        if (adapter instanceof PinnedSectionAdapter)
            return (PinnedSectionAdapter) adapter;

        if (adapter instanceof HeaderViewListAdapter)
            return findDecorAdapter(((HeaderViewListAdapter) adapter).getWrappedAdapter());

        if (adapter instanceof BaseAdapterDecorator)
            return findDecorAdapter(((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter());

        return null;
    }
}