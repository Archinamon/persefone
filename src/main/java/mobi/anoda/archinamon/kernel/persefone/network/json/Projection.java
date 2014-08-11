package mobi.anoda.archinamon.kernel.persefone.network.json;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import mobi.anoda.archinamon.kernel.persefone.model.JsonModel;
import mobi.anoda.archinamon.kernel.persefone.model._void.VoidModel;

/**
 * @author: Archinamon
 */
public final class Projection {

    public static class Error implements JsonModel {

        public static final String ERROR_CODE    = "code";
        public static final String ERROR_MESSAGE = "message";
    }

    public static class EmptyResponse implements JsonModel {
    }

    public static final  String                     TAG              = Projection.class.getSimpleName();
    private static final Class<? extends JsonModel> VOID             = VoidModel.class;
    private static final String                     SUPPRESS_REFLECT = "unchecked";
    private static final String                     STATUS           = "status";
    private static final String                     ERROR            = "error";
    private static final String                     DATA             = "data";
    private static       Class<? extends JsonModel> sParcelClass     = VOID;
    private static ProjectionObject sObjectProcessor;
    private static ProjectionArray  sArrayProcessor;

    public static void init(Class<? extends JsonModel> klass, IJson obj) {
        sParcelClass = klass;

        if (obj instanceof ProjectionObject) sObjectProcessor = (ProjectionObject) obj;
        if (obj instanceof ProjectionArray) sArrayProcessor = (ProjectionArray) obj;
    }

    public static boolean hasError() {
        boolean status = sObjectProcessor.optBoolean(STATUS);
        boolean error = sObjectProcessor.has(ERROR);

        return !status || error;
    }

    public static boolean isEmpty() throws JSONException {
        boolean error = sObjectProcessor.has(ERROR);

        return !error
               && sObjectProcessor.getJSONObject(DATA)
                                  .length() == 0;
    }

    public static void recycle() {
        sParcelClass = VOID;
        sObjectProcessor = null;
        sArrayProcessor = null;
    }

    public static HashMap parseError() throws JSONException {
        final HashMap<String, String> error = new HashMap<>();
        Projection.buildError(error, sObjectProcessor);

        return error;
    }

    public static JSONHashMap parseJSONObject() throws JSONException {
        final boolean isError = checkJSON(sObjectProcessor);
        final ProjectionObject data = !isError
                                      ? new ProjectionObject(sObjectProcessor.getJSONObject(DATA).toString())
                                      : null;

        return Projection.parse(isError, data);
    }

    public static JSONHashMap parseRawObject() throws JSONException {
        final boolean isError = checkJSON(sObjectProcessor);

        return Projection.parse(isError, sObjectProcessor);
    }

    public static JSONHashMap parseRawArray() throws JSONException {
        final boolean isError = sArrayProcessor == null || sArrayProcessor.getLength() == 0;

        return Projection.parse(isError, sArrayProcessor);
    }

    public static JSONHashMap parseJSONArray() throws JSONException {
        final boolean isError = checkJSON(sObjectProcessor);
        final ProjectionArray data = !isError
                                     ? new ProjectionArray(sObjectProcessor.getJSONArray(DATA).toString())
                                     : null;

        return Projection.parse(isError, data);
    }

    private static boolean checkJSON(JSONObject obj) throws JSONException {
        return !obj.optBoolean(STATUS, true);
    }

    @SuppressWarnings(SUPPRESS_REFLECT)
    private static JSONHashMap parse(final boolean isError, final IJson data) throws JSONException {
        JSONHashMap output = new JSONHashMap<HashMap<String, String>, HashMap<String, String>>(isError);

        final HashMap<String, Object> json = new HashMap<>();
        final HashMap<String, String> error = new HashMap<>();
        final HashMap<String, String> resultObj = new HashMap<>();
        final ArrayList<HashMap> resultArr = new ArrayList<>();

        if (isError) {
            Projection.buildError(error, sObjectProcessor);
        } else {
            if (data instanceof ProjectionObject) {
                Projection.buildResultFromObject(resultObj, (ProjectionObject) data);
                json.put(JSONHashMap.RESULT_TAG, resultObj);
            } else {
                Projection.buildResultFromArray(resultArr, (ProjectionArray) data);
                json.put(JSONHashMap.RESULT_TAG, resultArr);
            }
        }

        json.put(JSONHashMap.ERROR_TAG, error);

        if (data instanceof ProjectionObject) {
            output.putJSON(json);
        } else {
            output.putJSONArray(json);
        }

        return output;
    }

    @SuppressWarnings(SUPPRESS_REFLECT)
    private static void buildError(HashMap error, JSONObject root) throws JSONException {
        if (root == null) return;
        JSONObject errJson = root.getJSONObject(ERROR);
        error.put(Error.ERROR_CODE, errJson.getString(Error.ERROR_CODE));
        error.put(Error.ERROR_MESSAGE, errJson.getString(Error.ERROR_MESSAGE));
    }

    private static void buildResultFromObject(final HashMap result, final ProjectionObject obj) {
        Projector projector = Projector.getInstance(result);
        projector.parse(obj, sParcelClass);
        Projector.recycle();
    }

    @SuppressWarnings(SUPPRESS_REFLECT)
    private static void buildResultFromArray(final ArrayList result, final ProjectionArray obj) throws JSONException {
        for (int i = 0; i < obj.length(); i++) {
            HashMap<String, String> item = new HashMap<>();
            Projector projector = Projector.getInstance(item);

            JSONObject jobj = obj.optJSONObject(i);
            if (jobj == null) {
                continue;
            }

            item = projector.parse(jobj, sParcelClass)
                            .get();
            Projector.recycle();
            result.add(item);
        }
    }
}