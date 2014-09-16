package mobi.anoda.archinamon.kernel.persefone.network.json;

import android.util.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mobi.anoda.archinamon.kernel.persefone.network.json.Projection.Error;

/**
 * @author: Archinamon
 */
public class JSONHashMap<SuccessMap extends HashMap<String, String>, ErrorMap extends HashMap<String, String>> extends HashMap<SuccessMap, ErrorMap> {

    public static final  String TAG              = JSONHashMap.class.getSimpleName();
    static final         String RESULT_TAG       = "field_result";
    static final         String ERROR_TAG        = "field_error";
    private static final String ERR_DEFAULT_CODE = "000";
    private static final String ERR_DEFAULT_MSG  = "default error msg";
    private ImmutableMap  mError;
    private ImmutableMap  mResult;
    private ImmutableList mArray;

    JSONHashMap(boolean hasErrors) {
        if (!hasErrors) {
            mError = ImmutableMap.<String, String>builder()
                                 .put(Error.ERROR_CODE, ERR_DEFAULT_CODE)
                                 .put(Error.ERROR_MESSAGE, ERR_DEFAULT_MSG)
                                 .build();
        }
    }

    public void putJSON(HashMap<String, HashMap> obj) {
        try {
            if (obj.containsKey(RESULT_TAG)) {
                addResult(obj.get(RESULT_TAG));
            }

            if (obj.containsKey(ERROR_TAG)) {
                addError(obj.get(ERROR_TAG));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,
                  e.getMessage() != null
                  ? e.getMessage()
                  : "exception in parsing json results");
        }
    }

    public void putJSONArray(HashMap<String, Object> obj) {
        try {
            addError((HashMap) obj.get(ERROR_TAG));
            try {
                addArray((ArrayList) obj.get(RESULT_TAG));
            } catch (Exception ignore) {}
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,
                  e.getMessage() != null
                  ? e.getMessage()
                  : "exception in parsing json results");
        }
    }

    void addError(@Nonnull HashMap data) {
        mError = ImmutableMap.<String, String>builder()
                             .putAll(data)
                             .build();
    }

    void addResult(@Nonnull HashMap data) {
        mResult = ImmutableMap.<String, String>builder()
                              .putAll(data)
                              .build();
    }

    void addArray(@Nonnull ArrayList data) {
        mArray = ImmutableList.<HashMap>builder()
                              .addAll(data)
                              .build();
    }

    public boolean isEmpty() {
        return mError == null && (mArray != null
               ? mArray.isEmpty()
               : mResult == null || mResult.isEmpty());
    }

    public boolean hasError() {
        return (mError != null && !mError.isEmpty()) && (mResult == null || mArray == null);
    }

    @Nullable
    public ImmutableMap getResult() {
        return mResult;
    }

    @Nullable
    public ImmutableMap getError() {
        return mError;
    }

    @Nullable
    public ImmutableList getArray() {
        return mArray;
    }
}
