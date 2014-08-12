package mobi.anoda.archinamon.kernel.persefone.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.cookie.BasicClientCookie;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.http.entity.mime.CountableMultiPartEntity;
import mobi.anoda.archinamon.kernel.persefone.network.json.IJson;
import mobi.anoda.archinamon.kernel.persefone.network.operations.AbstractNetworkOperation;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperationDelete;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperationGet;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperationPost;
import mobi.anoda.archinamon.kernel.persefone.network.operations.NetworkOperationPut;
import mobi.anoda.archinamon.kernel.persefone.upload.Extras;
import mobi.anoda.archinamon.kernel.persefone.upload.FileToUpload;
import mobi.anoda.archinamon.kernel.persefone.upload.IUploadService;
import mobi.anoda.archinamon.kernel.persefone.upload.NameValue;
import mobi.anoda.archinamon.kernel.persefone.upload.NotificationConfig;
import mobi.anoda.archinamon.kernel.persefone.upload.Request;
import mobi.anoda.archinamon.kernel.persefone.upload.UploadAction;
import mobi.anoda.archinamon.kernel.persefone.utils.AtomicString;
import mobi.anoda.archinamon.kernel.persefone.utils.Common;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/**
 * Service to upload files as a multi-part form data in background using HTTP POST with notification center progress display.
 *
 * @author alexbbb (Alex Gotev)
 * @author eliasnaur
 */
public class UploadService extends AbstractService {

    public static final  String TAG                         = UploadService.class.getSimpleName();
    private static final int    UPLOAD_NOTIFICATION_ID      = 0xfa1166; // Something unique
    private static final int    UPLOAD_NOTIFICATION_ID_DONE = 0xed1123; // Something unique
    private NotificationManager   mNotificationManager;
    private Notification.Builder  mNotification;
    private PowerManager.WakeLock mWakeLock;
    private NotificationConfig    mNotificationConfig;
    private int                   mLastPublishedProgress;
    private volatile AtomicString mLastUploadId = new AtomicString();

    private /*synthetic*/ final IUploadService.Stub mBinder = new IUploadService.Stub() {

        @Implement
        public void upload(final Request task) {
            if (task != null) {
                try {
                    task.validate();
                } catch (MalformedURLException e) {
                    LogHelper.println_error(TAG, e);
                }

                mNotificationConfig = task.getNotificationConfig();
                mLastUploadId.updateQuery(task.getUploadId());
                final String url = task.getServerUrl();
                final Request.Method method = task.getMethod();
                final ArrayList<FileToUpload> files = task.getFilesToUpload();
                final ArrayList<NameValue> headers = task.getHeaders();
                final ArrayList<NameValue> parameters = task.getParameters();

                mLastPublishedProgress = 0;
                mWakeLock.acquire();
                try {
                    createNotification();
                    handleFileUpload(url, method, files, headers, parameters);
                } catch (Exception exception) {
                    broadcastError(exception);
                } finally {
                    mWakeLock.release();
                }
            }
        }

        @Implement
        public void addCookie(final String name, final String value, final String domain, final String path) {
            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setDomain(domain);
            cookie.setPath(path);

            mAppDelegate.getHttpClient()
                        .getCookieStore()
                        .addCookie(cookie);
        }
    };

    private /*synthetic*/ final CountableMultiPartEntity.ProgressListener mProgressListener = new CountableMultiPartEntity.ProgressListener() {

        @Implement
        public void transferred(long num, long total) {
            broadcastProgress(num, total);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotification = new Notification.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    private void handleFileUpload(final String url, Request.Method method, final ArrayList<FileToUpload> filesToUpload, final ArrayList<NameValue> requestHeaders, final ArrayList<NameValue> requestParameters) throws IOException {
        AbstractNetworkOperation operation;
        switch (method) {
            default:
            case GET:
                operation = new NetworkOperationGet(url);
                break;
            case POST:
                operation = new NetworkOperationPost(url);
                break;
            case PUT:
                operation = new NetworkOperationPut(url);
                break;
            case DELETE:
                operation = new NetworkOperationDelete(url);
                break;
        }

        CountableMultiPartEntity multipartEntity = new CountableMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"), mProgressListener);

        for (NameValue param : requestParameters)
            multipartEntity.addPart(param.getName(), new StringBody(param.getValue(), ContentType.TEXT_PLAIN));

        for (FileToUpload file : filesToUpload) {
            File f = file.getFile();
            if (f.exists()) {
                FileBody fb = new FileBody(f, ContentType.create(Common.getMimeType(f.getAbsolutePath())), file.getFileName());
                multipartEntity.addPart(file.getMultipartParam(), fb);
            }
        }

        operation.setEntity(multipartEntity);
        IJson response = operation.getJsonProjection(mAppDelegate);

        int serverResponseCode = operation.getResponseCode();
        String serverResponseMessage = operation.getResponseMessage();

        broadcastCompleted(serverResponseCode, serverResponseMessage, response);
    }

    private synchronized void broadcastProgress(final long uploadedBytes, final long totalBytes) {
        final int progress = (int) (uploadedBytes * 100 / totalBytes);
        if (progress <= mLastPublishedProgress) {
            return;
        }
        mLastPublishedProgress = progress;

        updateNotificationProgress(progress);

        final Bundle data = new Bundle();
        data.putString(Extras.UPLOAD_ID, mLastUploadId.getQuery().toString());
        data.putInt(Extras.STATUS, Extras.STATUS_IN_PROGRESS);
        data.putInt(Extras.PROGRESS, progress);

        sendBroadcast(UploadAction.POST_STATUS, data);
    }

    private synchronized void broadcastCompleted(final int responseCode, final String responseMessage, IJson response) {
        final String filteredMessage;
        if (responseMessage == null) {
            filteredMessage = "";
        } else {
            filteredMessage = responseMessage;
        }

        if (responseCode >= 200 && responseCode <= 299) {
            updateNotificationCompleted();
        } else {
            updateNotificationError();
        }

        final Bundle data = new Bundle();
        data.putString(Extras.UPLOAD_ID, mLastUploadId.getQuery().toString());
        data.putInt(Extras.STATUS, Extras.STATUS_COMPLETED);
        data.putInt(Extras.SERVER_RESPONSE_CODE, responseCode);
        data.putString(Extras.SERVER_RESPONSE_MESSAGE, filteredMessage);
        data.putString(Extras.API_RESPONSE_DATA, response.toString());

        sendBroadcast(UploadAction.POST_STATUS, data);
    }

    private void broadcastError(final Exception exception) {
        updateNotificationError();

        final Bundle data = new Bundle();
        data.putString(Extras.UPLOAD_ID, mLastUploadId.getQuery().toString());
        data.putInt(Extras.STATUS, Extras.STATUS_ERROR);
        data.putSerializable(Extras.ERROR_EXCEPTION, exception);

        sendBroadcast(UploadAction.POST_STATUS, data);
    }

    private void createNotification() {
        mNotification.setContentTitle(mNotificationConfig.getTitle())
                    .setContentText(mNotificationConfig.getMessage())
                    .setSmallIcon(mNotificationConfig.getIconResourceID())
                    .setProgress(100, 0, true)
                    .setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, mNotification.build());
    }

    private void updateNotificationProgress(final int progress) {
        mNotification.setContentTitle(mNotificationConfig.getTitle())
                    .setContentText(mNotificationConfig.getMessage())
                    .setSmallIcon(mNotificationConfig.getIconResourceID())
                    .setProgress(100, progress, false)
                    .setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, mNotification.build());
    }

    private void updateNotificationCompleted() {
        stopForeground(mNotificationConfig.isAutoClearOnSuccess());

        if (!mNotificationConfig.isAutoClearOnSuccess()) {
            mNotification.setContentTitle(mNotificationConfig.getTitle())
                        .setContentText(mNotificationConfig.getCompleted())
                        .setSmallIcon(mNotificationConfig.getIconResourceID())
                        .setProgress(0, 0, false)
                        .setOngoing(false);

            mNotificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, mNotification.build());
        }
    }

    private void updateNotificationError() {
        stopForeground(false);

        mNotification.setContentTitle(mNotificationConfig.getTitle())
                    .setContentText(mNotificationConfig.getError())
                    .setSmallIcon(mNotificationConfig.getIconResourceID())
                    .setProgress(0, 0, false)
                    .setOngoing(false);

        mNotificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, mNotification.build());
    }
}