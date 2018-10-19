package ping.otmsapp.mvp.basics;

import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;
import ping.otmsapp.mvp.contract.LoginContract;

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
