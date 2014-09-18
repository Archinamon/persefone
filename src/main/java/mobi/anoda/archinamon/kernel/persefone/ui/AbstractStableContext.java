package mobi.anoda.archinamon.kernel.persefone.ui;

import android.app.Application;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.View;
import org.intellij.lang.annotations.MagicConstant;
import java.util.Observable;
import java.util.Observer;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.annotation.ProxyMethod;
import mobi.anoda.archinamon.kernel.persefone.service.AbstractService;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.utils.ActivityUtils;

/**
 * Created by matsukov-ea on 18.09.2014.
 */
public abstract class AbstractStableContext extends Observable implements Observer {

    private volatile AbstractActivity         mUiContext; //user interface context;
    private volatile AbstractService          mRiContext; //remote interface context;
    private volatile Context                  mNsContext; //neighbour context, e.g. BroadcastReceiver
    private final    AnodaApplicationDelegate mAppContext;//application reference, always nonnull if any app task exists

    public AbstractStableContext() throws ClassCastException {
        Application androidAppContext = ActivityUtils.getApplicationContext();
        if (androidAppContext instanceof AnodaApplicationDelegate)
            this.mAppContext = (AnodaApplicationDelegate) androidAppContext;
        else throw new ClassCastException("Should be defined AnodaApplicationDelegate extended implementation");
    }

    @Implement
    public void update(Observable observable, Object data) {
        if (data instanceof AbstractActivity) {
            mUiContext = (AbstractActivity) data;
        } else if (data instanceof AbstractService) {
            mRiContext = (AbstractService) data;
        } else if (data instanceof Context) {
            mNsContext = (Context) data;
        } else {
            throw new IllegalArgumentException("param 'data' should be a Context child");
        }
    }

    public final boolean isUiContextRegistered() {
        return this.mUiContext != null;
    }

    public final boolean isRiContextRegistered() {
        return this.mRiContext != null;
    }

    public final boolean isNsContextRegistered() {
        return this.mNsContext != null;
    }

    @Nullable
    @ProxyMethod
    public View findViewById(@IdRes int id) {
        if (isUiContextRegistered())
            return mUiContext.findViewById(id);

        return null;
    }

    @ProxyMethod
    public Object getSystemService(@MagicConstant(valuesFromClass = Context.class) String name) {
        return obtainStable().getSystemService(name);
    }

    private Context obtainStable() {
        if (mUiContext != null) return mUiContext; //ui has the highest priority to reference above
        if (mRiContext != null) return mRiContext;
        if (mNsContext != null) return mNsContext;
        return mAppContext;
    }
}
