package otmsapp.ping.mvp.presenter;

import otmsapp.ping.entitys.warn.WarnItem;
import otmsapp.ping.mvp.basics.PresenterViewBind;
import otmsapp.ping.mvp.contract.WarnContract;

/**
 * Created by Leeping on 2018/10/7.
 * email: 793065165@qq.com
 */
public class WarnPresenter extends PresenterViewBind<WarnContract.View> implements WarnContract.Presenter {
    @Override
    public void updateData() {

    }

    @Override
    public void removeData(WarnItem item) {

    }
}
