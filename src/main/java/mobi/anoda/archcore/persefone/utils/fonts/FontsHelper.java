package mobi.anoda.archcore.persefone.utils.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.jetbrains.annotations.Nullable;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class FontsHelper {

    public static Typeface getCustomFont(Context context, AssetFont font) {
        return Typeface.createFromAsset(context.getAssets(), font.getPath());
    }

    public static void applyFonts(View viewRoot, Typeface font, View... ignored) {
        if (viewRoot instanceof ViewGroup) {
            final ViewGroup container = (ViewGroup) viewRoot;
            final int size = container.getChildCount();
            if (size > 0) {
                for (int position = 0; position < size; position++) {
                    View child = container.getChildAt(position);
                    if (child instanceof TextView) {
                        if (!isIgnoring(child, ignored)) {
                            ((TextView) child).setTypeface(font);
                        }
                    } else {
                        applyFonts(child, font);
                    }
                }
            }
        }
    }

    private static boolean isIgnoring(View checkFor, @Nullable View... checkWith) {
        if (checkWith != null && checkWith.length != 0) {
            for (View view : checkWith) {
                if (view == checkFor) {
                    return true;
                }
            }
        }

        return false;
    }
}
