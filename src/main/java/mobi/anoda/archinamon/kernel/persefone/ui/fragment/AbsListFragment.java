package mobi.anoda.archinamon.kernel.persefone.ui.fragment;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.google.common.collect.LinkedHashMultiset;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mobi.anoda.archinamon.kernel.persefone.R;
import mobi.anoda.archinamon.kernel.persefone.annotation.Implement;
import mobi.anoda.archinamon.kernel.persefone.ui.TaggedView;
import mobi.anoda.archinamon.kernel.persefone.ui.activity.AbstractActivity;
import mobi.anoda.archinamon.kernel.persefone.ui.adapter.AbstractAdapter;
import mobi.anoda.archinamon.kernel.persefone.ui.fragment.interfaces.ItemCheckProvider;

/**
 * @author: Archinamon
 */
public abstract class AbsListFragment<Elements, CastedAdapter extends AbstractAdapter<Elements>> extends AbstractFragment implements TaggedView {

    public static interface OnItemSelectedActionListener {

        void onItemSelectedAction(int position, View view);

        void onAllItemsSelectedAction();
    }

    protected class ActionModeTransienceImpl implements ActionMode.Callback {

        @Implement
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mContext.getMenuInflater().inflate(mListManagementResId, menu);
            mContext.getMenuInflater().inflate(R.menu.edit_mode, menu);
            return true;
        }

        @Implement
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Implement
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final int i = item.getItemId();

            if (i == R.id.action_select_all) {
                mItemCheckProvider.setAllItemsChecked();
            } else if (i == R.id.action_delete) {
                onActionItemsDelete();

                mAdapter.notifyDataSetChanged();
                mode.finish();
            } else {
                onActionItemClick(mode, i);
            }

            return true;
        }

        @Implement
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mCheckedPositions.clear();
            onDestroyManagementMode();
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ImplItemCheckProvider implements ItemCheckProvider {

        @Implement
        public boolean isItemChecked(int position) {
            return mCheckedPositions.contains(position);
        }

        @Implement
        public void setItemChecked(int position, View view) {
            if (isItemChecked(position)) {
                mCheckedPositions.remove((Integer) position);
            } else {
                mCheckedPositions.add(position);
            }

            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onItemSelectedAction(position, view);
            }

            updateActionManagement();
        }

        @Implement
        public void setAllItemsChecked() {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                mCheckedPositions.add(i);
            }

            if (mOnItemSelectedListener != null) {
                mOnItemSelectedListener.onAllItemsSelectedAction();
            }

            updateActionManagement();
        }
    }

    public static final String                       TAG                = AbsListFragment.class.getSimpleName();
    protected           ArrayList<Integer>           mCheckedPositions  = new ArrayList<>();
    protected           ActionMode.Callback          mCallback          = new ActionModeTransienceImpl();
    protected           ItemCheckProvider            mItemCheckProvider = new ImplItemCheckProvider();
    private final       LinkedHashMultiset<Elements> mObjectList        = LinkedHashMultiset.create();
    private ActionMode                   mActionMode;
    private CastedAdapter                mAdapter;
    private int                          mListResourceId;
    private int                          mListManagementResId;
    private OnItemSelectedActionListener mOnItemSelectedListener;

    protected abstract void onListItemClick(AdapterView<?> parentView, View view, int position, long id);

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(mListResourceId, container, false);
        assert view != null;

        ListView list = (ListView) view.findViewById(R.id.__core__lv_fragment_list_body);
        list.setOnItemClickListener(new OnItemClickListener() {

            @Implement
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(parent, view, position, id);
            }
        });

        return view;
    }

    public void setOnItemSelectedListener(OnItemSelectedActionListener listener) {
        mOnItemSelectedListener = listener;
    }

    public void setListLayout(int id) {
        mListResourceId = id;
    }

    public void setListManagementResId(int id) {
        mListManagementResId = id;
    }

    public CastedAdapter getAdapter() {
        return mAdapter;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void onActionItemClick(ActionMode management, int itemId) {
    }

    public void onActionItemsDelete() {
    }

    public void onDestroyManagementMode() {
    }

    public void updateActionManagement() {
        final int checkedCount = mCheckedPositions.size();
        if (checkedCount > 0) {
            if (mActionMode == null) {
                mActionMode = mContext.startActionMode(mCallback);
            }
        } else {
            if (mActionMode != null) {
                mActionMode.finish();
                mActionMode = null;
            }
        }

        if (mActionMode != null) {
            mActionMode.setTitle(String.valueOf(checkedCount));
        }
    }

    protected synchronized List<Elements> getItems() {
        return new ArrayList<>(mObjectList);
    }

    protected void addItem(Elements elem) {
        mObjectList.add(elem);
        if (mAdapter != null) {
            mAdapter.add(elem);
        }
    }

    protected void addAllItems(Collection<Elements> elems) {
        mObjectList.addAll(elems);
        if (mAdapter != null) {
            mAdapter.addAll(elems);
        }
    }

    protected void removeItem(Elements elem) {
        mObjectList.remove(elem);
        if (mAdapter != null) {
            mAdapter.remove(elem);
        }
    }

    protected void removeAllItems() {
        mObjectList.clear();
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }

    @SuppressWarnings("unchecked")
    protected ListView getListView() {
        View root = getView();
        if (root != null) {
            if (root instanceof ListView)
                return (ListView) root;

            View v = root.findViewById(R.id.__core__lv_fragment_list_body);
            if (v instanceof AbsListView)
                return (ListView) v;
        }

        return null;
    }

    protected void applyAdapter(Class<? extends AbstractAdapter> adapterClass, int viewResource) {
        instantiateAdapterObj(adapterClass, mContext, viewResource, getItems());
    }

    protected void applyAdapter(Class<? extends AbstractAdapter> adapterClass, int viewResource, int dropDownResource) {
        instantiateAdapterObj(adapterClass, mContext, viewResource, dropDownResource, getItems());
    }

    @SuppressWarnings("unchecked")
    private void instantiateAdapterObj(Class<? extends AbstractAdapter> adapterClass, Object... parameters) {
        Class<?>[] typeSignature = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Object o = parameters[i];

            if (o instanceof AbstractActivity)
                typeSignature[i] = AbstractActivity.class;
            else if (o instanceof List)
                typeSignature[i] = List.class;
            else
                typeSignature[i] = o.getClass();
        }

        try {
            Constructor<? extends AbstractAdapter> adapterConstructor = adapterClass.getDeclaredConstructor(typeSignature);
            if (adapterConstructor != null) {
                mAdapter = (CastedAdapter) adapterConstructor.newInstance(parameters);
                getListView().setAdapter(mAdapter);
            }
        } catch (NoSuchMethodException | InvocationTargetException | java.lang.InstantiationException | IllegalAccessException e) {
            logError(e);
        }
    }
}
