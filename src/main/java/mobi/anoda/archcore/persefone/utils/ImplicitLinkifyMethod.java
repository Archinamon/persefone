package mobi.anoda.archcore.persefone.utils;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;

/**
 * author: Archinamon
 * project: FacebookDownloaderVideo
 */
public class ImplicitLinkifyMethod extends LinkMovementMethod {

    public static final String TAG = ImplicitLinkifyMethod.class.getSimpleName();
    private static ImplicitLinkifyMethod sInstance;
    private        OnClickListener       mOnClickDelegate;

    public final void setOnClickListener(OnClickListener listener) {
        mOnClickDelegate = listener;
    }

    @Override
    public boolean onTouchEvent(@NotNull TextView widget, @NotNull Spannable buffer, @NotNull MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            assert layout != null;

            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    mOnClickDelegate.onClick(widget);
                } else {
                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static ImplicitLinkifyMethod getInstance() {
        if (sInstance == null)
            sInstance = new ImplicitLinkifyMethod();

        return sInstance;
    }
}
