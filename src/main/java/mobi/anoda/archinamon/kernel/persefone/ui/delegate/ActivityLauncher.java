package mobi.anoda.archinamon.kernel.persefone.ui.delegate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.IntentCompat;
import java.util.ArrayList;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.signal.broadcast.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.AbstractFragment;

/**
 * Created by matsukov-ea on 22.09.2014.
 */
public final class ActivityLauncher {

    public static final String CUSTOM_DATA = ".ui:key_data";
    private final    StableContext    mStableContext;
    private          AbstractFragment mFragment;
    private volatile boolean          isViaFragment;

    public ActivityLauncher(StableContext stableContext) {
        this.mStableContext = stableContext;
        this.isViaFragment = false;
    }

    public void setFragment(AbstractFragment fragment) {
        this.mFragment = fragment;
        this.isViaFragment = true;
    }

    public void startActivity(Broadcastable action) {
        Intent activityArgs = new Intent(action.getAction()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startSimpleInner(activityArgs);
    }

    public void startDisorderedActivity(Broadcastable action) {
        Intent activityArgs = new Intent(action.getAction()).setPackage(mStableContext.getPackageName());

        startSimpleInner(activityArgs);
    }

    public void startDisorderedActivity(Broadcastable action, Bundle params) {
        Intent activity = new Intent(action.getAction());
        activity.setPackage(mStableContext.getPackageName());
        activity.putExtras(params);

        startSimpleInner(activity);
    }

    public void startDisorderedActivity(Broadcastable action, Class<? extends Activity> activity) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), activity).setClassName(mStableContext.getPackageName(), activity.getName())
                                                                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                               .setAction(action.getAction())
                                                                               .setPackage(mStableContext.getPackageName());

        startSimpleInner(intent);
    }

    public void startDisorderedActivity(Broadcastable action, Class<? extends Activity> activity, Bundle params) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), activity).setClassName(mStableContext.getPackageName(), activity.getName())
                                                                               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                                               .setAction(action.getAction())
                                                                               .setPackage(mStableContext.getPackageName())
                                                                               .putExtras(params);
        startSimpleInner(intent);
    }


    public void startActivity(Broadcastable action, Bundle params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(params);
        startSimpleInner(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, Data params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(CUSTOM_DATA, params);
        startSimpleInner(i);
    }

    public <Data extends Parcelable> void startActivity(Broadcastable action, ArrayList<Data> params) {
        Intent i = new Intent(action.getAction());
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putParcelableArrayListExtra(CUSTOM_DATA, params);
        startSimpleInner(i);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void switchWorkflow(Class c, T data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        intent.putExtra(CUSTOM_DATA, data);

        startWorkflow(c, intent);
    }

    public void switchWorkflow(Class c) {
        startWorkflow(c, null);
    }

    /* Launch new top activity with anim */
    public void openActivityWithTaskRecreate(Class c) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c).addFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK)
                                                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startSimpleInner(intent);
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.grow_fade_in, R.anim.shrink_fade_out);
    }

    /* Switch activity with anim */
    public void enterActivity(Class c) {
        startActivity(c, null);
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity */
    public void enterActivity(Class c, int data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c).putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void enterActivity(Class c, T data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        if (data instanceof Bundle) {
            intent.putExtras((Bundle) data);
        } else {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startActivity(c, intent);
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity with anim */
    public <T extends Parcelable> void enterActivity(Class c, ArrayList<T> data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c).putExtra(CUSTOM_DATA, data);

        startActivity(c, intent);
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result with anim */
    public <T extends Parcelable> void enterActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startForResultInner(intent, code);
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_right, R.anim.out_left);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, T data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startForResultInner(intent, code);
    }

    /* Switch activity for result without anim */
    public void openActivityForResult(Class c, int code, int data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c).putExtra(CUSTOM_DATA, data);

        startForResultInner(intent, code);
    }

    /* Switch activity for result without anim */
    public <T extends Parcelable> void openActivityForResult(Class c, int code, ArrayList<T> data) {
        Intent intent = new Intent(mStableContext.obtainAppContext(), c);
        if (data != null) {
            intent.putExtra(CUSTOM_DATA, data);
        }

        startForResultInner(intent, code);
    }

    /* Exit to concrete activity with anim */
    public void exitActivity(Class c) {
        if (c != null) {
            startActivity(c, null);
        }

        mStableContext.obtainUiContext()
                      .finish();
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Return to previous activity with anim */
    public void exitActivity() {
        mStableContext.obtainUiContext()
                      .finish();
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Transcend result to launcher-activity and finish with anim */
    public void deliverResult(int r, Intent i) {
        mStableContext.obtainUiContext()
                      .setResult(r, i);
        mStableContext.obtainUiContext()
                      .finish();
        mStableContext.obtainUiContext()
                      .overridePendingTransition(R.anim.in_left, R.anim.out_right);
    }

    /* Helper to open new activity with anim */
    private void startActivity(Class c, Intent i) {
        Intent intent = i != null ? i : new Intent(mStableContext.obtainAppContext(), c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startSimpleInner(intent);
    }

    /* Helper to open new activity with anim */
    private void startWorkflow(Class c, Intent i) {
        Intent intent = i != null ? i : new Intent(mStableContext.obtainAppContext(), c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startSimpleInner(intent);
    }

    private void startSimpleInner(Intent command) {
        if (isViaFragment)
            mFragment.startActivity(command);
        else
            mStableContext.startActivity(command);
    }

    private void startForResultInner(Intent command, int resultCode) {
        if (isViaFragment)
            mFragment.startActivityForResult(command, resultCode);
        else
            mStableContext.obtainUiContext()
                          .startActivityForResult(command, resultCode);
    }
}
