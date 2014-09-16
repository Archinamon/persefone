package mobi.anoda.archinamon.kernel.persefone.utils.fonts;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;
import org.intellij.lang.annotations.MagicConstant;

/**
 * author: Archinamon
 * project: FavorMe
 */
public interface IAssetFont {

    public static final class Applier {

        public synchronized void doOp(TextView view, IAssetFont font) {
            Context context = view.getContext();
            Typeface tp = Typeface.createFromAsset(context.getAssets(), font.getPath());

            view.setTypeface(tp);
        }
    }

    public abstract static class Builder {

        private final IAssetFont.Applier fApplier = new IAssetFont.Applier();
        private volatile TextView   mvTarget;
        private volatile IAssetFont mvFont;
        private volatile int        mvNativeStyle;

        @SuppressWarnings("unchecked")
        protected final <T extends Builder> T withFont(IAssetFont font) {
            this.mvFont = font;
            return (T) this;
        }

        public Builder into(final TextView target) {
            this.mvTarget = target;
            return this;
        }

        public Builder from(@MagicConstant(flagsFromClass = Typeface.class) int nativeStyle) {
            this.mvNativeStyle = nativeStyle;
            return this;
        }

        public void apply() {
            if (mvTarget != null) {
                if (mvNativeStyle > -1)
                    mvTarget.setTypeface(Typeface.defaultFromStyle(mvNativeStyle));
                if (mvFont != null) fApplier.doOp(mvTarget, mvFont);
            }
        }
    }

    static String BASE = "fonts/";
    static String TTF  = ".ttf";
    static String OTF  = ".otf";

    String getPath();
}
