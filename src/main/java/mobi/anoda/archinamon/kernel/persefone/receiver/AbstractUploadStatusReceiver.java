package mobi.anoda.archinamon.kernel.persefone.receiver;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.service.UploadService;
import mobi.anoda.archinamon.kernel.persefone.upload.Extras;
import mobi.anoda.archinamon.kernel.persefone.upload.UploadAction;

/**
 * Abstract broadcast receiver from which to inherit when creating a receiver for {@link UploadService}.
 * 
 * It provides the boilerplate code to properly handle broadcast messages coming from the upload service and dispatch
 * them to the proper handler method.
 * 
 * @author alexbbb (Alex Gotev)
 * @author eliasnaur
 * 
 */
public abstract class AbstractUploadStatusReceiver extends AbstractReceiver {

    @Implement
    public void onReceive(@Nonnull final String action, @Nullable Intent data) {
        if (data != null) {
            if (UploadAction.POST_STATUS.isEqual(action)) {
                final int status = data.getIntExtra(Extras.STATUS, 0);
                final String uploadId = data.getStringExtra(Extras.UPLOAD_ID);

                switch (status) {
                    case Extras.STATUS_ERROR:
                        final Exception exception = (Exception) data.getSerializableExtra(Extras.ERROR_EXCEPTION);
                        onError(uploadId, exception);
                        break;
                    case Extras.STATUS_COMPLETED:
                        final int responseCode = data.getIntExtra(Extras.SERVER_RESPONSE_CODE, 0);
                        final String responseMsg = data.getStringExtra(Extras.SERVER_RESPONSE_MESSAGE);
                        onCompleted(uploadId, responseCode, responseMsg);
                        break;
                    case Extras.STATUS_IN_PROGRESS:
                        final int progress = data.getIntExtra(Extras.PROGRESS, 0);
                        onProgress(uploadId, progress);
                        break;
                }
            }
        }
    }

    /**
     * Register this upload receiver. It's recommended to register the receiver in Activity's onResume method.
     * 
     * @param activity activity in which to register this receiver
     */
    public void register(final Activity activity) {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UploadAction.POST_STATUS.getAction());
        activity.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister this upload receiver. It's recommended to unregister the receiver in Activity's onPause method.
     * 
     * @param activity activity in which to unregister this receiver
     */
    public void unregister(final Activity activity) {
        activity.unregisterReceiver(this);
    }

    /**
     * Called when the upload progress changes.
     * 
     * @param uploadId unique ID of the upload request
     * @param progress value from 0 to 100
     */
    public abstract void onProgress(final String uploadId, final int progress);

    /**
     * Called when an error happens during the upload.
     * 
     * @param uploadId unique ID of the upload request
     * @param exception exception that caused the error
     */
    public abstract void onError(final String uploadId, final Exception exception);

    /**
     * Called when the upload is completed successfully.
     * 
     * @param uploadId unique ID of the upload request
     * @param serverResponseCode status code returned by the server
     * @param serverResponseMessage string containing the response received from the server
     */
    public abstract void onCompleted(final String uploadId, final int serverResponseCode, final String serverResponseMessage);
}