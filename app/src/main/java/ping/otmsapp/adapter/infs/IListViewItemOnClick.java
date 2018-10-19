package ping.otmsapp.adapter.infs;

import android.view.View;

import org.jetbrains.annotations.NotNull;

public interface IListViewItemOnClick {
    void onItemViewClicked(@NotNull View view, int position);
}
