package mobi.anoda.archcore.persefone.network.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import java.util.Collection;
import mobi.anoda.archcore.persefone.annotation.Implement;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public class ProjectionArray extends JSONArray implements IJson {


    public ProjectionArray() {
        super();
    }

    public ProjectionArray(Collection copyFrom) {
        super(copyFrom);
    }

    public ProjectionArray(JSONTokener readFrom) throws JSONException {
        super(readFrom);
    }

    public ProjectionArray(String json) throws JSONException {
        this(new JSONTokener(json));
    }

    @Implement
    public int getLength() {
        return length();
    }
}
