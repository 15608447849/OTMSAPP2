package ping.otmsapp.adapter.infs;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class IAdapter extends BaseAdapter {

    IListViewItemOnClick callback;

    public void setCallback(IListViewItemOnClick callback) {
        this.callback = callback;
    }

    public IListViewItemOnClick getCallback() {
        return callback;
    }
}
