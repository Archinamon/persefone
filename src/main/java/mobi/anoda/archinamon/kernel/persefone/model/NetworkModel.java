package mobi.anoda.archinamon.kernel.persefone.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.http.message.BasicNameValuePair;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.annotation.EvaluationTag;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.utils.WordUtils;

/**
 * @author: Archinamon
 * @project: FavorMe
 */
public abstract class NetworkModel extends ModelPropagator implements ISignalEvaluable<BasicNameValuePair> {

    private volatile ImmutableList<BasicNameValuePair> mNetworkProjection;

    protected NetworkModel(ImmutableMap map) {
        super(map);
    }

    @Implement
    public final ImmutableList<BasicNameValuePair> getPackage() {
        return mNetworkProjection;
    }

    @Implement
    public final ISignalEvaluable packModel() {
        List<BasicNameValuePair> projection = new ArrayList<>();

        //trick to avoid "Ambiguous method call" error
        final Class klass = ((Object) this).getClass();

        Method[] methods;
        Field[] fields;
        if (klass.getSuperclass() != NetworkModel.class) {
            Method[] superMethods = klass.getSuperclass().getDeclaredMethods();
            Method[] instMethods = klass.getDeclaredMethods();

            methods = new Method[instMethods.length + superMethods.length];
            System.arraycopy(superMethods, 0, methods, 0, superMethods.length);
            System.arraycopy(instMethods, 0, methods, superMethods.length, instMethods.length);

            Field[] superFields = klass.getSuperclass().getDeclaredFields();
            Field[] instFields = klass.getDeclaredFields();

            fields = new Field[instFields.length + superFields.length];
            System.arraycopy(superFields, 0, fields, 0, superFields.length);
            System.arraycopy(instFields, 0, fields, superFields.length, instFields.length);
        } else {
            methods = klass.getDeclaredMethods();
            fields = klass.getDeclaredFields();
        }

        projection.addAll(processMethods(methods));
        projection.addAll(processFields(fields));

        mNetworkProjection = ImmutableList.copyOf(projection);

        return this;
    }

    private List<BasicNameValuePair> processMethods(final Method[] methods) {
        List<BasicNameValuePair> projection = new ArrayList<>();

        for (Method m : methods) {
            if (m.isAnnotationPresent(EvaluationTag.class)) {
                final EvaluationTag anno = m.getAnnotation(EvaluationTag.class);
                final String key = anno.value();
                BasicNameValuePair pair = null;

                boolean isAccessable = true;
                try {
                    if (!(isAccessable = m.isAccessible())) {
                        m.setAccessible(true);
                    }

                    String value = (String) m.invoke(this);

                    if (!WordUtils.isEmpty(value)) {
                        pair = new BasicNameValuePair(key, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pair != null) {
                        projection.add(pair);
                    }

                    if (!isAccessable) {
                        m.setAccessible(false);
                    }
                }
            }
        }

        return projection;
    }

    private List<BasicNameValuePair> processFields(final Field[] methods) {
        List<BasicNameValuePair> projection = new ArrayList<>();

        for (Field m : methods) {
            if (m.isAnnotationPresent(EvaluationTag.class)) {
                final EvaluationTag anno = m.getAnnotation(EvaluationTag.class);
                final String key = anno.value();
                BasicNameValuePair pair = null;

                boolean isAccessable = true;
                try {
                    if (!(isAccessable = m.isAccessible())) {
                        m.setAccessible(true);
                    }

                    String value = (String) m.get(this);

                    if (!WordUtils.isEmpty(value)) {
                        pair = new BasicNameValuePair(key, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (pair != null) {
                        projection.add(pair);
                    }
                    if (!isAccessable) {
                        m.setAccessible(false);
                    }
                }
            }
        }

        return projection;
    }
}
