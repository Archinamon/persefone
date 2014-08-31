package mobi.anoda.archinamon.kernel.persefone.ui.utils;

import java.util.ArrayList;

public class AnimationBundle extends ArrayList<Tweener> {

    private static final long serialVersionUID = 0xA84D78726F127468L;
    private boolean mSuspended;

    public void start() {
        if (mSuspended) {
            return; // ignore attempts to start animations
        }
        for (Tweener anim : this) {
            anim.getAnimator().start();
        }
    }

    public void cancel() {
        for (Tweener anim : this) {
            anim.getAnimator().cancel();
        }
        clear();
    }

    public void stop() {
        for (Tweener anim : this) {
            anim.getAnimator().end();
        }
        clear();
    }

    public void setSuspended(boolean suspend) {
        mSuspended = suspend;
    }
}