package mobi.anoda.archinamon.kernel.persefone.upload;

import android.os.Parcel;
import android.os.Parcelable;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * Contains the configuration of the upload notification.
 *
 * @author alexbbb (Alex Gotev)
 */
public class NotificationConfig implements Parcelable {

    // This is used to regenerate the object.
    // All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<NotificationConfig> CREATOR = new Parcelable.Creator<NotificationConfig>() {

        @Implement
        public NotificationConfig createFromParcel(final Parcel in) {
            return new NotificationConfig(in);
        }

        @Implement
        public NotificationConfig[] newArray(final int size) {
            return new NotificationConfig[size];
        }
    };
    private final int     mIconResourceID;
    private final String  mTitle;
    private final String  mMessage;
    private final String  mCompleted;
    private final String  mError;
    private final boolean isAutoClearOnSuccess;

    public NotificationConfig() {
        mIconResourceID = android.R.drawable.ic_menu_upload;
        mTitle = "File Upload";
        mMessage = "uploading in progress";
        mCompleted = "upload completed successfully!";
        mError = "error during upload";
        isAutoClearOnSuccess = false;
    }

    public NotificationConfig(final int iconResourceID, final String title, final String message, final String completed, final String error, final boolean autoClearOnSuccess) throws IllegalArgumentException {

        if (title == null || message == null || completed == null || error == null) {
            throw new IllegalArgumentException("You can't provide null parameters");
        }

        this.mIconResourceID = iconResourceID;
        this.mTitle = title;
        this.mMessage = message;
        this.mCompleted = completed;
        this.mError = error;
        this.isAutoClearOnSuccess = autoClearOnSuccess;
    }

    public final int getIconResourceID() {
        return mIconResourceID;
    }

    public final String getTitle() {
        return mTitle;
    }

    public final String getMessage() {
        return mMessage;
    }

    public final String getCompleted() {
        return mCompleted;
    }

    public final String getError() {
        return mError;
    }

    public final boolean isAutoClearOnSuccess() {
        return isAutoClearOnSuccess;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeInt(mIconResourceID);
        parcel.writeString(mTitle);
        parcel.writeString(mMessage);
        parcel.writeString(mCompleted);
        parcel.writeString(mError);
        parcel.writeByte((byte) (isAutoClearOnSuccess ? 1 : 0));
    }

    private NotificationConfig(Parcel in) {
        mIconResourceID = in.readInt();
        mTitle = in.readString();
        mMessage = in.readString();
        mCompleted = in.readString();
        mError = in.readString();
        isAutoClearOnSuccess = in.readByte() == 1;
    }
}