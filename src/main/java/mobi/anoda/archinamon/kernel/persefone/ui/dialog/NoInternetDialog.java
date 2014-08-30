package mobi.anoda.archinamon.kernel.persefone.ui.dialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.service.notification.NetworkNotification;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class NoInternetDialog extends AbstractDialog {

    public static final String TAG = NoInternetDialog.class.getSimpleName();
    private volatile boolean mIsNotified;

    public NoInternetDialog() {
    }

    @Implement
    public String getViewTag() {
        return NoInternetDialog.TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (mDialogParams != null) {
            setMessage(mDialogParams.getString(IEXTRA_MESSAGE));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (!mIsNotified)
            mContext.sendBroadcast(NetworkNotification.NOTIFY_DISMISS);
    }

    @Implement
    public boolean setup() {
        setDialogType(Popup.PROMPT_YES_NO);
        setTitle(Common.getApplicationName(mContext));
        setOkButton("Ok", new OnClickListener() {

            @Implement
            public void onClick(DialogInterface dialog, int which) {
                notifyRequester();
            }
        });
        setCancelButton(getString(R.string.no_internet_open_wifi), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mContext.startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
        });

        return true;
    }

    private void notifyRequester() {
        mContext.sendBroadcast(NetworkNotification.INTERNET_ACCESS_DENIED);
        mIsNotified = true;
    }
}
