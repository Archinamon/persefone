package mobi.anoda.archinamon.kernel.persefone.ui.adapter;

import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.support.annotation.NonNull;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class ListItem<ModelObject> implements AbsAdapterItem, OnClickListener {

    protected int                      mPosition;
    protected int                      mResourceId;
    protected AnodaApplicationDelegate mAppDelegate;
    protected AbstractActivity         mContext;
    protected LayoutInflater           mInflater;
    protected ModelObject              mDataModel;

    public ListItem(ModelObject model) {
        this.mDataModel = model;
    }

    public final <App extends AnodaApplicationDelegate> void setAppDelegate(App appDelegate) {
        this.mAppDelegate = appDelegate;
    }

    public final <ContextImpl extends AbstractActivity> void setContextInstance(ContextImpl context) {
        this.mContext = context;
    }

    public final void setInflater(@NonNull LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public final void setPositionId(int position) {
        this.mPosition = position;
    }

    public abstract int getViewType();

    public final int getPosition() {
        return this.mPosition;
    }

    public final ModelObject getDataModel() {
        return this.mDataModel;
    }

    public /*virtual*/ void setAsLast() {}
}
