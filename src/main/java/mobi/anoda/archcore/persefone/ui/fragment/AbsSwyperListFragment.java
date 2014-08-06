package mobi.anoda.archcore.persefone.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AbsListView;
import mobi.anoda.archcore.persefone.ui.TaggedView;
import mobi.anoda.archcore.persefone.ui.activity.AbstractDrawerActivity;
import mobi.anoda.archcore.persefone.ui.adapter.AbstractAdapter;
import mobi.anoda.archcore.persefone.ui.fragment.interfaces.SwypeNotifyer;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbsSwyperListFragment<Elements, Adapter extends AbstractAdapter<Elements>> extends AbsListFragment<Elements, Adapter> implements TaggedView {

    public static final              String  TAG      = AbsSwyperListFragment.class.getSimpleName();
    protected AbstractDrawerActivity mContext;

    public static void riseOnOpenDrawer(AbsSwyperListFragment ctx, boolean single) {
        ctx.onDrawerOpened(single, 0L);
    }

    public static void riseOnCloseDrawer(AbsSwyperListFragment ctx, boolean single) {
        ctx.onDrawerClosed(single, 0L);
    }

    protected abstract void onDrawerOpened(boolean singleRun, final long delay);

    protected abstract void onDrawerClosed(boolean singleRun, final long delay);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof AbstractDrawerActivity) {
            this.mContext = (AbstractDrawerActivity) activity;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }

    protected final void sendBroadcast(SwypeNotifyer command) {
        mContext.sendBroadcast(new Intent(command.getAction()));
    }

    protected void sendBroadcast(SwypeNotifyer command, Integer position) {
        if (mContext != null) {
            Intent intent = new Intent(command.getAction());
            intent.putExtra(SwypeNotifyer.IDATA, position);

            mContext.sendBroadcast(intent);
        }
    }

    protected <ParcelData extends Parcelable> void sendBroadcast(SwypeNotifyer command, ParcelData data) {
        if (mContext != null) {
            Intent intent = new Intent(command.getAction());
            intent.putExtra(CUSTOM_DATA, data);

            mContext.sendBroadcast(intent);
        }
    }
}
