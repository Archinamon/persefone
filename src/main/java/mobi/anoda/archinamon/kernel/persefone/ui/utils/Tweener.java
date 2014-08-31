package mobi.anoda.archinamon.kernel.persefone.ui.utils;

import android.animation.Animator.AnimatorListener;
import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Tweener {

    private static final String  TAG   = Tweener.class.getSimpleName();
    private static final boolean DEBUG = false;
    private ObjectAnimator mAnimator;
    private static HashMap<Object, Tweener> sTweens = new HashMap<>();

    public Tweener(ObjectAnimator anim) {
        mAnimator = anim;
    }

    public ObjectAnimator getAnimator() {
        return this.mAnimator;
    }

    private static void remove(Animator animator) {
        Iterator<Entry<Object, Tweener>> iter = sTweens.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Object, Tweener> entry = iter.next();
            if (entry.getValue().mAnimator == animator) {
                if (DEBUG) {
                    Log.v(TAG, "Removing tweener " + sTweens.get(entry.getKey())
                               + " sTweens.size() = " + sTweens.size());
                }
                iter.remove();
                break; // an animator can only be attached to one object
            }
        }
    }

    public static Tweener to(Object object, long duration, Object... vars) {
        long delay = 0;
        AnimatorUpdateListener updateListener = null;
        AnimatorListener listener = null;
        TimeInterpolator interpolator = null;

        // Iterate through arguments and discover properties to animate
        ArrayList<PropertyValuesHolder> props = new ArrayList<>(vars.length / 2);
        for (int i = 0; i < vars.length; i += 2) {
            if (!(vars[i] instanceof String)) {
                throw new IllegalArgumentException("Key must be a string: " + vars[i]);
            }
            String key = (String) vars[i];
            Object value = vars[i + 1];

            if ("simultaneousTween".equals(key)) {
                // TODO
            } else if ("ease".equals(key)) {
                interpolator = (TimeInterpolator) value; // TODO: multiple interpolators?
            } else if ("onUpdate".equals(key) || "onUpdateListener".equals(key)) {
                updateListener = (AnimatorUpdateListener) value;
            } else if ("onStage".equals(key) || "onStageListener".equals(key)) {
                listener = (AnimatorListener) value;
            } else if ("delay".equals(key)) {
                delay = ((Number) value).longValue();
            } else if ("syncWith".equals(key)) {
                // TODO
            } else if (value instanceof float[]) {
                props.add(PropertyValuesHolder.ofFloat(key, ((float[]) value)[0], ((float[]) value)[1]));
            } else if (value instanceof int[]) {
                props.add(PropertyValuesHolder.ofInt(key, ((int[]) value)[0], ((int[]) value)[1]));
            } else if (value instanceof Number) {
                float floatValue = ((Number) value).floatValue();
                props.add(PropertyValuesHolder.ofFloat(key, floatValue));
            } else {
                throw new IllegalArgumentException("Bad argument for key \"" + key + "\" with value " + value.getClass());
            }
        }

        // Re-use existing tween, if present
        Tweener tween = sTweens.get(object);
        ObjectAnimator anim;
        if (tween == null) {
            anim = ObjectAnimator.ofPropertyValuesHolder(object, props.toArray(new PropertyValuesHolder[props.size()]));
            tween = new Tweener(anim);
            sTweens.put(object, tween);
            if (DEBUG) {
                Log.v(TAG, "Added new Tweener " + tween);
            }
        } else {
            anim = sTweens.get(object).mAnimator;
            replace(props, object); // Cancel all animators for given object
        }

        if (interpolator != null) {
            anim.setInterpolator(interpolator);
        }

        // Update animation with properties discovered in loop above
        anim.setStartDelay(delay);
        anim.setDuration(duration);
        if (updateListener != null) {
            anim.removeAllUpdateListeners(); // There should be only one
            anim.addUpdateListener(updateListener);
        }
        if (listener != null) {
            anim.removeAllListeners(); // There should be only one.
            anim.addListener(listener);
        }
        anim.addListener(mCleanupListener);

        return tween;
    }

    // Listener to watch for completed animations and remove them.
    private static AnimatorListener mCleanupListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            remove(animation);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            remove(animation);
        }
    };

    public static void reset() {
        if (DEBUG) {
            Log.v(TAG, "Reset()");
            if (sTweens.size() > 0) {
                Log.v(TAG, "Cleaning up " + sTweens.size() + " animations");
            }
        }
        sTweens.clear();
    }

    private static void replace(ArrayList<PropertyValuesHolder> props, Object... args) {
        for (final Object killobject : args) {
            Tweener tween = sTweens.get(killobject);
            if (tween != null) {
                tween.mAnimator.cancel();
                if (props != null) {
                    tween.mAnimator.setValues(props.toArray(new PropertyValuesHolder[props.size()]));
                } else {
                    sTweens.remove(tween);
                }
            }
        }
    }
}
