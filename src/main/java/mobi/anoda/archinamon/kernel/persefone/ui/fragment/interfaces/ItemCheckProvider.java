package mobi.anoda.archinamon.kernel.persefone.ui.fragment.interfaces;

import android.view.View;

/**
 * @author: Archinamon
 * @project: Xozzer
 */
public interface ItemCheckProvider {

    public boolean isItemChecked(int position);

    public void setItemChecked(int position, View view);

    public void setAllItemsChecked();
}
