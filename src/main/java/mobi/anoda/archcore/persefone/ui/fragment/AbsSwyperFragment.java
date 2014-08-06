package mobi.anoda.archcore.persefone.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Parcelable;
import mobi.anoda.archcore.persefone.ui.TaggedView;
import mobi.anoda.archcore.persefone.ui.activity.AbstractDrawerActivity;
import mobi.anoda.archcore.persefone.ui.fragment.interfaces.SwypeNotifyer;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class AbsSwyperFragment extends AbstractFragment implements TaggedView {

    public static final              String  TAG      = AbsSwyperFragment.class.getSimpleName();
    protected static final transient Handler sHandler = new Handler();
    protected AbstractDrawerActivity mContext;

    public static void riseOnOpenDrawer(AbsSwyperFragment ctx, boolean single) {
        ctx.onDrawerOpened(single, 0L);
    }

    public static void riseOnCloseDrawer(AbsSwyperFragment ctx, boolean single) {
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
