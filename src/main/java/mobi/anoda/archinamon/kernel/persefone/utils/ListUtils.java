package mobi.anoda.archinamon.kernel.persefone.utils;

import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * author: Archinamon
 * project: FavorMe
 */
public class ListUtils {

    public static boolean isEmpty(@Nullable List list) {
        return list == null || list.isEmpty();
    }

    public static <T> void fill(List<? super T> list, T object, final int size) {
        for (int i = 0; i < size; i++) list.add(object);
    }

    public static <T> void fill(List<? super T> list, T object, final int from, final int size) {
        if (from >= size) return;
        for (int i = from; i < size; i++) list.add(object);
    }

    public static boolean containsTypeSignature(@Nullable List list, Class<?> typeSignature) {
        if (isEmpty(list)) return false;

        for (Object item : list) {
            if (item.getClass() == typeSignature) return true;
        }

        return false;
    }

    /**
     * Creates an empty {@code ArrayList} instance.
     * <p/>
     * <p><b>Note:</b> if you only need an <i>immutable</i> empty List, use
     * {@link Collections#emptyList} instead.
     *
     * @return a newly-created, initially-empty {@code ArrayList}
     */
    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }

    /**
     * Creates a resizable {@code ArrayList} instance containing the given
     * elements.
     * <p/>
     * <p><b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the
     * following:
     * <p/>
     * <p>{@code List<Base> list = Lists.newArrayList(sub1, sub2);}
     * <p/>
     * <p>where {@code sub1} and {@code sub2} are references to subtypes of
     * {@code Base}, not of {@code Base} itself. To get around this, you must
     * use:
     * <p/>
     * <p>{@code List<Base> list = Lists.<Base>newArrayList(sub1, sub2);}
     *
     * @param elements the elements that the list should contain, in order
     * @return a newly-created {@code ArrayList} containing those elements
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    public static <T> String join(T[] array, String cement) {
        StringBuilder builder = new StringBuilder();

        if(array == null || array.length == 0) {
            return null;
        }

        for (T t : array) {
            builder.append(t).append(cement);
        }

        if (cement.length() > 0) {
            builder.delete(builder.length() - cement.length(), builder.length());
        }

        return builder.toString();
    }
}
