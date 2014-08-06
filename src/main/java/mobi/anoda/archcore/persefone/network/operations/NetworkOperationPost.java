package mobi.anoda.archcore.persefone.network.operations;

import org.apache.http.NameValuePair;
import org.apache.http.auth.Credentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author archinamon
 */
public class NetworkOperationPost extends AbstractNetworkOperation {

    public NetworkOperationPost(String url) {
        super(url, null);
    }

    public NetworkOperationPost(String url, Credentials credentials) {
        super(url, credentials);
    }

    @Override
    protected List<NameValuePair> getValuesList(BasicNameValuePair... values) {
        if (values == null) {
            return null;
        }
        ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
        data.addAll(Arrays.asList(values));
        return data.size() > 0 ? data : null;
    }

    @Override
    public HttpUriRequest getHttpUriRequest(String url) {
        HttpPost request = new HttpPost(url);
        try {
            if (mPostValues.size() > 0) {
                request.setEntity(new UrlEncodedFormEntity(mPostValues, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return request;
    }
}
