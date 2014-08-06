package mobi.anoda.archcore.persefone.network.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.Map;
import mobi.anoda.archcore.persefone.annotation.Implement;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public final class ProjectionObject extends JSONObject implements IJson {

    public ProjectionObject() {
        super();
    }

    public ProjectionObject(Map m) {
        super(m);
    }

    public ProjectionObject(JSONTokener tokener) throws JSONException {
        super(tokener);
    }

    public ProjectionObject(String json) throws JSONException {
        super(json);
    }

    public ProjectionObject(JSONObject copyFrom, String[] names) throws JSONException {
        super(copyFrom, names);
    }

    @Implement
    public int getLength() {
        return length();
    }
}
