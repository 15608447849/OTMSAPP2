package ping.otmsapp.mvp.presenter;

import java.util.Iterator;

import ping.otmsapp.entitys.warn.WarnItem;
import ping.otmsapp.entitys.warn.WarnList;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.WarnContract;
import ping.otmsapp.mvp.model.AppInterfaceModel;

/**
 * Created by Leeping on 2018/10/7.
 * email: 793065165@qq.com
 */
public class WarnPresenter extends PresenterViewBind<WarnContract.View> implements WarnContract.Presenter {

    private WarnContract.Model model = new AppInterfaceModel();

    @Override
    public void updateData() {
        WarnList list = new WarnList().fetch();
        if (!isBindView()) return;
        if (list == null) return;
        if (list.list.size() == 0) {
            view.toast("暂无预警信息");
            return;
        }
        view.refreshList(list.list);
    }

    @Override
    public void removeData(WarnItem item) {
        //匹配数据
        if (!isBindView()) return;
        view.showProgressBar();

        boolean f = model.handleWarn(item.code, item.time);
        if (f) {
            WarnList list = new WarnList().fetch();
            if (list != null) {
                Iterator<WarnItem> it = list.list.iterator();
                while (it.hasNext()) {
                    if (it.next().code.equals(item.code)) {
                        it.remove();
                    }
                }
                list.save();
                view.refreshList(list.list);
            }
        } else {
            view.toast("同步处理失败");
        }
        view.hindProgressBar();

    }
}
