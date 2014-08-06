package mobi.anoda.archcore.persefone.model._void;

import java.lang.reflect.Method;
import mobi.anoda.archcore.persefone.model.JsonModel;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class VoidModel implements JsonModel {

    public static final Class<Void> TYPE = lookupType();

    @SuppressWarnings("unchecked")
    private static Class<Void> lookupType() {
        try {
            Method method = Runnable.class.getMethod("run", EmptyArray.CLASS);
            return (Class<Void>) method.getReturnType();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private VoidModel() {
    }
}
