package mobi.anoda.archinamon.kernel.persefone.upload;

import android.os.Parcel;
import android.os.Parcelable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;

/**
 * Represents an upload request.
 *
 * @author alexbbb (Alex Gotev)
 * @author eliasnaur
 */
public class Request implements Parcelable {

    public static enum Method {

        POST,
        GET,
        PUT,
        DELETE
    }

    public static final String                      TAG     = Request.class.getSimpleName();
    public static final Parcelable.Creator<Request> CREATOR = new Parcelable.Creator<Request>() {

        @Implement
        public Request createFromParcel(Parcel source) {
            return new Request(source);
        }

        @Implement
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
    private       NotificationConfig      mNotificationConfig;
    private final String                  mUploadId;
    private final String                  mFileUrl;
    private final ArrayList<FileToUpload> mFilesToUpload;
    private final ArrayList<NameValue>    mHeaders;
    private final ArrayList<NameValue>    mParameters;
    private       Method                  mMethod;

    private Request(Parcel src) {
        this.mNotificationConfig = src.readParcelable(NotificationConfig.class.getClassLoader());

        final String[] strings = new String[2];
        src.readStringArray(strings);

        this.mUploadId = strings[0];
        this.mFileUrl = strings[1];
        this.mMethod = Method.values()[src.readInt()];
        this.mFilesToUpload = new ArrayList<>();
        this.mHeaders = new ArrayList<>();
        this.mParameters = new ArrayList<>();

        src.readTypedList(this.mFilesToUpload, FileToUpload.CREATOR);
        src.readTypedList(this.mHeaders, NameValue.CREATOR);
        src.readTypedList(this.mParameters, NameValue.CREATOR);
    }

    /**
     * Creates a new upload request.
     *
     * @param uploadId  unique ID to assign to this upload request. It's used in the broadcast receiver when receiving updates.
     * @param serverUrl URL of the server side script that handles the multipart form upload
     */
    public Request(final String uploadId, final String serverUrl) {
        this.mUploadId = uploadId;
        mNotificationConfig = new NotificationConfig();
        mFileUrl = serverUrl;
        mFilesToUpload = new ArrayList<>();
        mHeaders = new ArrayList<>();
        mParameters = new ArrayList<>();
        mMethod = Method.POST;
    }

    /**
     * Sets custom notification configuration.
     *
     * @param iconResourceID     ID of the notification icon. You can use your own app's R.drawable.your_resource
     * @param title              Notification title
     * @param message            Text displayed in the notification when the upload is in progress
     * @param completed          Text displayed in the notification when the upload is completed successfully
     * @param error              Text displayed in the notification when an error occurs
     * @param autoClearOnSuccess true if you want to automatically clear the notification when the upload gets completed successfully
     */
    public void setNotificationConfig(final int iconResourceID, final String title, final String message, final String completed, final String error, final boolean autoClearOnSuccess) {
        mNotificationConfig = new NotificationConfig(iconResourceID, title, message, completed, error, autoClearOnSuccess);
    }

    /**
     * Validates the upload request and throws exceptions if one or more parameters are not properly set.
     *
     * @throws IllegalArgumentException if request protocol or URL are not correctly set
     * @throws MalformedURLException    if the provided server URL is not valid
     */
    public void validate() throws IllegalArgumentException, MalformedURLException {
        if (mFileUrl == null || "".equals(mFileUrl)) {
            throw new IllegalArgumentException("Request URL cannot be either null or empty");
        }

        if (!mFileUrl.startsWith("http")) {
            throw new IllegalArgumentException("Specify either http:// or https:// as protocol");
        }

        // Check if the URL is valid
        new URL(mFileUrl);

        if (mFilesToUpload.isEmpty()) {
            throw new IllegalArgumentException("You have to add at least one file to upload");
        }
    }

    /**
     * Adds a file to this upload request.
     *
     * @param path          Absolute path to the file that you want to upload
     * @param parameterName Name of the form parameter that will contain file's data
     * @param fileName      File name seen by the server side script
     * @param contentType   Content type of the file. Set this to null if you don't want to set a content type.
     */
    public void addFileToUpload(final String path, final String parameterName, final String fileName, final String contentType) {
        mFilesToUpload.add(new FileToUpload(path, parameterName, fileName, contentType));
    }

    /**
     * Adds a header to this upload request.
     *
     * @param headerName  header name
     * @param headerValue header value
     */
    public void addHeader(final String headerName, final String headerValue) {
        mHeaders.add(new NameValue(headerName, headerValue));
    }

    /**
     * Adds a parameter to this upload request.
     *
     * @param paramName  parameter name
     * @param paramValue parameter value
     */
    public void addParameter(final String paramName, final String paramValue) {
        mParameters.add(new NameValue(paramName, paramValue));
    }

    /**
     * Adds a parameter with multiple values to this upload request.
     *
     * @param paramName parameter name
     * @param array     values
     */
    public void addArrayParameter(final String paramName, final String... array) {
        for (String value : array) {
            mParameters.add(new NameValue(paramName, value));
        }
    }

    /**
     * Adds a parameter with multiple values to this upload request.
     *
     * @param paramName parameter name
     * @param list      values
     */
    public void addArrayParameter(final String paramName, final List<String> list) {
        for (String value : list) {
            mParameters.add(new NameValue(paramName, value));
        }
    }

    /**
     * Sets the HTTP method to use. By default it's set to POST.
     *
     * @param method new HTTP method to use
     */
    public void setMethod(final Method method) {
        if (method != null) {
            this.mMethod = method;
        }
    }

    /**
     * Gets the HTTP method to use.
     *
     * @return
     */
    public Method getMethod() {
        return mMethod;
    }

    /**
     * Gets the upload ID of this request.
     *
     * @return
     */
    public String getUploadId() {
        return mUploadId;
    }

    /**
     * Gets the URL of the server side script that will handle the multipart form upload.
     *
     * @return
     */
    public String getServerUrl() {
        return mFileUrl;
    }

    /**
     * Gets the list of the files that has to be uploaded.
     *
     * @return
     */
    public ArrayList<FileToUpload> getFilesToUpload() {
        return mFilesToUpload;
    }

    /**
     * Gets the list of the headers.
     *
     * @return
     */
    public ArrayList<NameValue> getHeaders() {
        return mHeaders;
    }

    /**
     * Gets the list of the parameters.
     *
     * @return
     */
    public ArrayList<NameValue> getParameters() {
        return mParameters;
    }

    /**
     * Gets the upload notification configuration.
     *
     * @return
     */
    public NotificationConfig getNotificationConfig() {
        return mNotificationConfig;
    }

    @Implement
    public int describeContents() {
        return 0;
    }

    @Implement
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mNotificationConfig, flags);
        dest.writeStringArray(new String[] {this.mUploadId,
                                            this.mFileUrl});
        dest.writeInt(this.mMethod.ordinal());
        dest.writeTypedList(this.mFilesToUpload);
        dest.writeTypedList(this.mHeaders);
        dest.writeTypedList(this.mParameters);
    }
}