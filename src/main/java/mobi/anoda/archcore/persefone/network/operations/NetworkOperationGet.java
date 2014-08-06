package mobi.anoda.archcore.persefone.network.operations;

import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mobi.anoda.archcore.persefone.annotation.Implement;

/**
 *
 * @author archinamon
 */
public class NetworkOperationGet extends AbstractNetworkOperation {

    private static final String LOG_TAG = NetworkOperationGet.class.getSimpleName();

    public NetworkOperationGet(String url, Credentials credentials) {
        super(url, credentials);
    }

    public NetworkOperationGet(String url) {
        super(url, null);
    }

    @Implement
    protected List<NameValuePair> getValuesList(BasicNameValuePair... values) {
        if (values == null) {
            return null;
        }
        ArrayList<NameValuePair> data = new ArrayList<>();
        data.addAll(Arrays.asList(values));
        return data.size() > 0 ? data : null;
    }

    @Implement
    public HttpUriRequest getHttpUriRequest(String url) {
        if (mPostValues != null) {
            url += "?" + URLEncodedUtils.format(mPostValues, "utf-8");
        }

        Log.i(LOG_TAG, "URL: " + url);

        HttpGet request = new HttpGet(url);
        return request;
    }
}
