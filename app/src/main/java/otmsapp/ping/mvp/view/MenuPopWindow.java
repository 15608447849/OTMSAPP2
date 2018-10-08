package otmsapp.ping.mvp.view;

import android.content.Context;
import android.widget.PopupWindow;

import otmsapp.ping.mvp.contract.MenuContract;

/**
 * Created by Leeping on 2018/10/8.
 * email: 793065165@qq.com
 */
public class MenuPopWindow extends PopupWindow implements MenuContract.View {
    private MenuContract.Presenter presenter;

    private Context context;

    public MenuPopWindow(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void showProgressBar() {
    }
    @Override
    public void hindProgressBar() {
    }
    @Override
    public void toast(String message) {
    }

    @Override
    public void bindPresenter(MenuContract.Presenter presenter) {
        presenter.bindView(this);
        this.presenter = presenter ;
    }

    @Override
    public void unbindPresenter() {
        this.presenter.unbindView();
        this.presenter = null;
    }
}
