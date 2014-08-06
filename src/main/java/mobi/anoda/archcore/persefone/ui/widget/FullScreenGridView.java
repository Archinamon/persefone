package mobi.anoda.archcore.persefone.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 *
 * @author Archinamon
 */
public class FullScreenGridView extends GridView {

    public static final String TAG = FullScreenGridView.class.getSimpleName();
    boolean mExpanded = false;

    public FullScreenGridView(Context context) {
        this(context, null);
    }

    public FullScreenGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FullScreenGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded()) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void setExpanded(boolean expanded) {
        this.mExpanded = expanded;
    }
}
