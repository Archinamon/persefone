package mobi.anoda.archinamon.kernel.persefone.ui.adapter;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.AnodaApplicationDelegate;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;

/**
 * @author: Archinamon
 */
public abstract class AbstractAdapter<Element> extends BaseAdapter implements Filterable {

    public static interface TypedFilter {

        String getSearchable();
    }

    private class ArrayFilter extends Filter {

        ArrayFilter() {
            if (mOriginalValues == null) {
                synchronized (MUTEX) {
                    mOriginalValues = new ArrayList<>(mObjects);
                }
            }
        }

        @Implement
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (prefix == null || prefix.length() == 0) {
                results.values = mOriginalValues;
                results.count = mOriginalValues.size();
            } else {
                String prefixString = prefix.toString()
                                            .toLowerCase();

                ArrayList<Element> values;
                synchronized (MUTEX) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final ArrayList<Element> newValues = new ArrayList<>();

                for (final Element value : values) {
                    final String valueText;
                    if (value instanceof TypedFilter) {
                        valueText = ((TypedFilter) value).getSearchable();
                    } else {
                        valueText = value.toString()
                                         .toLowerCase();
                    }

                    switch (mSearchMode) {
                        case MODE_SEARCH_START: {
                            // First match against the whole, non-splitted value
                            if (valueText.startsWith(prefixString)) {
                                newValues.add(value);
                            } else {
                                final String[] words = valueText.split(" ");

                                // Start at index 0, in case valueText starts with space(s)
                                for (String word : words) {
                                    if (word.startsWith(prefixString)) {
                                        newValues.add(value);
                                        break;
                                    }
                                }
                            }

                            break;
                        }
                        case MODE_SEARCH_ALL: {
                            final String[] searchPrefixes = prefixString.split(" ");

                            boolean addElem = true;
                            for (String pfx : searchPrefixes) {
                                if (!valueText.contains(pfx)) {
                                    addElem = false;
                                    break;
                                }
                            }
                            if (addElem) {
                                newValues.add(value);
                            }

                            break;
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Implement
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<Element>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public static final int    MODE_SEARCH_ALL   = 0x01;
    public static final int    MODE_SEARCH_START = 0x02;
    private final       Object MUTEX             = new Object();
    protected AbstractActivity         mContext;
    protected AnodaApplicationDelegate mApplication;
    protected LayoutInflater           mInflater;
    protected List<Element>            mObjects;
    protected int                      mItemResource;
    protected int                      mDropDownResource;
    protected int mField = 0;
    protected boolean            mNotifyOnChange;
    private   ArrayList<Element> mOriginalValues;
    private   ArrayFilter        mFilter;
    private   int                mSearchMode;

    public static ArrayAdapter<CharSequence> createFromResource(Context context, int textArrayResId, int textViewResId) {
        CharSequence[] strings = context.getResources()
                                        .getTextArray(textArrayResId);
        return new ArrayAdapter<>(context, textViewResId, strings);
    }

    public AbstractAdapter(AbstractActivity context, Integer resource) {
        init(context, resource, 0, new ArrayList<Element>());
    }

    public AbstractAdapter(AbstractActivity context, Integer resource, Integer textViewResourceId) {
        init(context, resource, textViewResourceId, new ArrayList<Element>());
    }

    public AbstractAdapter(AbstractActivity context, Integer resource, Element[] objects) {
        init(context, resource, 0, Arrays.asList(objects));
    }

    public AbstractAdapter(AbstractActivity context, Integer resource, Integer textViewResourceId, Element[] objects) {
        init(context, resource, textViewResourceId, Arrays.asList(objects));
    }

    public AbstractAdapter(AbstractActivity context, Integer resource, List<Element> objects) {
        init(context, resource, 0, objects);
    }

    public AbstractAdapter(AbstractActivity context, Integer resource, Integer textViewResourceId, List<Element> objects) {
        init(context, resource, textViewResourceId, objects);
    }

    private void init(AbstractActivity context, int resource, int textViewResourceId, List<Element> objects) {
        mContext = context;
        mApplication = (AnodaApplicationDelegate) context.getApplication();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mItemResource = mDropDownResource = resource;
        mObjects = new ArrayList<>(objects);
        mField = textViewResourceId;
    }

    protected final void bindListReference(List<Element> objects) {
        // here we deliver a reference to a List object inside memory avoiding separating links
        mObjects = objects;
    }

    protected final AnodaApplicationDelegate getApplication() {
        Application app = mContext.getApplication();
        if (app instanceof AnodaApplicationDelegate) {
            return (AnodaApplicationDelegate) app;
        }

        throw new RuntimeException("Illegal application level access");
    }

    protected final String getString(int resId) {
        return mContext.getString(resId);
    }

    protected final String getString(int resId, Object... modifiers) {
        return mContext.getString(resId, modifiers);
    }

    public void add(Element object) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void setSearchModel(int mask) {
        switch (mask) {
            case MODE_SEARCH_ALL:
                break;
            case MODE_SEARCH_START:
                break;
            default: throw new IllegalStateException("Filter mode must be SEARCH_ALL or SEARCH_START");
        }

        mSearchMode = mask;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mDropDownResource);
    }

    protected final View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        assert view != null;

        try {
            if (mField == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mField);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
        }

        assert text != null;

        Element item = getItem(position);
        if (item instanceof CharSequence) {
            text.setText((CharSequence) item);
        } else {
            text.setText(item.toString());
        }

        return view;
    }

    @Override
    public boolean isEmpty() {
        return mObjects.isEmpty() && (mOriginalValues == null || mOriginalValues.isEmpty()) ;
    }

    public void addAll(Collection<? extends Element> collection) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void addAll(Element... items) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void insert(Element object, int index) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                mOriginalValues.add(index, object);
            } else {
                mObjects.add(index, object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void remove(Element object) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void clear() {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void sort(Comparator<? super Element> comparator) {
        synchronized (MUTEX) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public Context getContext() {
        return mContext;
    }

    @Implement
    public int getCount() {
        return mObjects.size();
    }

    @Implement
    public Element getItem(int position) {
        return mObjects.get(position);
    }

    @Implement
    public long getItemId(int position) {
        return position;
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);

    public int getPosition(Element item) {
        return mObjects.indexOf(item);
    }

    public void setDropDownViewResource(int resource) {
        this.mDropDownResource = resource;
    }

    @Implement
    public final Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }
}
