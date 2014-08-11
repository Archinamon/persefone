package mobi.anoda.archinamon.kernel.persefone.ui.actionbar;

import android.content.Context;
import android.os.Parcelable;
import android.view.ActionProvider;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;
import mobi.anoda.archinamon.kernel.persefone.signals.Broadcastable;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Archinamon on 4/29/14.
 */
public abstract class AbstractActionProvider extends ActionProvider {

    protected AnodaApplicationDelegate mApplication;
    protected AbstractActivity         mContext;

    /**
     * Creates a new instance. ActionProvider classes should always implement a constructor that takes a single Context parameter for inflating from menu XML.
     *
     * @param context Context for accessing resources.
     */
    public AbstractActionProvider(Context context) {
        super(context);

        checkArgument(checkNotNull(context) instanceof AbstractActivity);
        mContext = (AbstractActivity) context;
        mApplication = mContext.getAppDelegate();
    }

    @ProxyMethod("AbstractActivity.sendBroadcast")
    protected void sendBroadcast(Broadcastable action) {
        mContext.sendBroadcast(action);
    }

    @ProxyMethod("AbstractActivity.sendBroadcast")
    protected void sendBroadcast(Broadcastable action, String data) {
        mContext.sendBroadcast(action, data);
    }

    @ProxyMethod("AbstractActivity.sendBroadcast")
    protected <Model extends Parcelable> void sendBroadcast(Broadcastable action, Model model) {
        mContext.sendBroadcast(action, model);
    }
}
