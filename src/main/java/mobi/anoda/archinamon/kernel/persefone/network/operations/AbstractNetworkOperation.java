package mobi.anoda.archinamon.kernel.persefone.network.operations;

import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.network.client.ExtAndroidHttpClient;
import mobi.anoda.archinamon.kernel.persefone.network.json.IJson;
import mobi.anoda.archinamon.kernel.persefone.network.json.ProjectionArray;
import mobi.anoda.archinamon.kernel.persefone.network.json.ProjectionObject;
import mobi.anoda.archinamon.kernel.persefone.utils.LogHelper;

/** @author archinamon */
public abstract class AbstractNetworkOperation implements NetworkOperation {

    private static final String              TAG         = AbstractNetworkOperation.class.getSimpleName();
    private static final int                 STATUS_OK   = 200;
    private static final int                 BUFFER_SIZE = 8192;
    protected            List<NameValuePair> mPostValues = new ArrayList<>();
    private              byte[]              mBuffer     = new byte[BUFFER_SIZE];
    private           String      mUrl;
    private           Credentials mCredentials;
    private           HttpEntity  mEntity;
    private           ErrorReport mErrorReport;
    private           int         mResponseCode;
    private           String      mResponseMessage;

    public AbstractNetworkOperation(String url, Credentials credentials) {
        mUrl = url;
        mCredentials = credentials;
    }

    public void addValues(BasicNameValuePair... values) {
        mPostValues.addAll(getValuesList(values));
    }

    protected abstract List<NameValuePair> getValuesList(BasicNameValuePair... values);

    public void setEntity(HttpEntity entity) {
        mEntity = entity;
    }

    public abstract HttpUriRequest getHttpUriRequest(String url);

    @Implement
    public String getStringBody(AnodaApplicationDelegate appContext) {
        InputStream inputStream = null;

        try {
            int size;
            StringBuilder stringBuilder = new StringBuilder();
            HttpUriRequest request = getHttpUriRequest(mUrl);
            if (request == null) {
                return null;
            }

            ExtAndroidHttpClient.modifyRequestToAcceptGzipResponse(request);

            setupCredentials(request);

            setupEntity(request);

            inputStream = getInputStream(appContext.getHttpClient()
                                                   .execute(request));

            logHttpRequest(request);

            if (inputStream != null) {
                while ((size = inputStream.read(mBuffer)) > -1) {
                    stringBuilder.append(new String(mBuffer, 0, size));
                }
                Log.d(TAG, stringBuilder.toString());
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            LogHelper.println_error(TAG, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }

    @Implement
    public IJson getJsonProjection(AnodaApplicationDelegate appContext) {
        String rawString = getStringBody(appContext);
        try {
            if (rawString.startsWith("{")) return new ProjectionObject(rawString);
            if (rawString.startsWith("[")) return new ProjectionArray(rawString);
        } catch (Exception ignore) {
        }
        return null;
    }

    @Implement
    public ErrorReport obtainErrorReport() {
        return mErrorReport;
    }

    public final void execute(AnodaApplicationDelegate appContext) {
        getStringBody(appContext);
    }

    public final int getResponseCode() {
        return mResponseCode;
    }

    public final String getResponseMessage() {
        return mResponseMessage;
    }

    public void buildErrorReport(String code, String body) {
        mErrorReport = ErrorReport.newReport(body, Integer.valueOf(code));
    }

    private InputStream getInputStream(HttpResponse response) {
        try {
            StatusLine status = response.getStatusLine();
            mResponseCode = status.getStatusCode();
            mResponseMessage = status.getReasonPhrase();
            switch (status.getStatusCode()) {
                case STATUS_OK:
                    HttpEntity entity = response.getEntity();
                    InputStream is = ExtAndroidHttpClient.getUngzippedContent(entity);
                    return new BufferedInputStream(is, BUFFER_SIZE);
            }

            mErrorReport = new ErrorReport(null);
            mErrorReport.mMessage = status.getReasonPhrase();
            mErrorReport.mResponseCode = status.getStatusCode();
        } catch (IOException ignore) {
        }
        return null;
    }

    private void setupCredentials(HttpRequest request) {
        if (mCredentials != null) {
            request.setHeader(BasicScheme.authenticate(mCredentials, HTTP.UTF_8, false));
        }
    }

    private void setupEntity(HttpRequest request) {
        if (request instanceof HttpPost && mEntity != null) {
            HttpPost post = (HttpPost) request;
            post.setEntity(mEntity);
        }
    }

    private void logHttpRequest(HttpUriRequest request) {
        Log.i(TAG,
              request.getURI()
                     .toASCIIString());
        for (Header header : request.getAllHeaders()) {
            Log.i(TAG, "Header: NAME: " + header.getName() + " VALUE: " + header.getValue());
        }
        if (request instanceof HttpPost) {
            HttpPost post = (HttpPost) request;
            HttpEntity entity = post.getEntity();

            if (entity == null) {
                return;
            }

            try {
                for (NameValuePair parameter : URLEncodedUtils.parse(entity)) {
                    Log.i(TAG, "Post Param: NAME: " + parameter.getName() + " VALUE: " + parameter.getValue());
                }
            } catch (IOException ignore) {
            }
        }
    }
}
