package otmsapp.ping.mvp.basics;

import otmsapp.ping.mvp.basics.IPresenter;
import otmsapp.ping.mvp.basics.IView;
import otmsapp.ping.mvp.contract.LoginContract;

public class PresenterViewBind<View extends IView> implements IPresenter<View> {

    protected View view;
    @Override
    public void bindView(View view) {
        this.view = view;
    }

    @Override
    public boolean isBindView() {
        return this.view != null;
    }

    @Override
    public void unbindView() {
        this.view = null;
    }
}
