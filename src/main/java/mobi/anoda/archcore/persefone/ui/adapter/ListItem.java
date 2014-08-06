package mobi.anoda.archcore.persefone.ui.adapter;

import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import org.jetbrains.annotations.NotNull;
import mobi.anoda.archcore.persefone.AnodaApplicationDelegate;
import mobi.anoda.archcore.persefone.ui.activity.AbstractActivity;

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

    public final void setInflater(@NotNull LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    public final void setPositionId(int position) {
        this.mPosition = position;
    }

    public abstract int getViewType();

    public /*virtual*/ void setAsLast() {}
}
