package mobi.anoda.archinamon.kernel.persefone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.context.StableContext;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * author: Archinamon project: Multi Locker
 */
public abstract class AbstractReceiver extends BroadcastReceiver {

    protected final String TAG = Common.obtainClassTag(this);
    protected volatile StableContext mStableContext;

    protected abstract void onReceive(@NonNull final String action, @Nullable Intent data);

    @Implement
    public void onReceive(Context context, Intent intent) {
        mStableContext = StableContext.Impl.instantiate(context);

        final String action = intent.getAction();
        assert action != null;

        onReceive(action, intent);
    }

    /* Simple Throwable processor */
    protected final void logError(Throwable e) {
        LogHelper.println_error(TAG, e);
    }
}
