package otmsapp.ping.mvp.presenter;

import java.util.Calendar;
import java.util.Date;

import IceInternal.Ex;
import cn.hy.otms.rpcproxy.appInterface.AppSchedvech;
import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.log.LLog;
import otmsapp.ping.mvp.basics.PresenterViewBind;
import otmsapp.ping.mvp.contract.HistoryContract;
import otmsapp.ping.mvp.model.AppInterfaceModel;
import otmsapp.ping.tools.JsonUti;
import otmsapp.ping.tools.StrUtil;

public class HistoryPresenter extends PresenterViewBind<HistoryContract.View> implements HistoryContract.Presenter{


    private UserInfo userInfo = new UserInfo().fetch();
    private HistoryContract.Model model = new AppInterfaceModel();

    @Override
    public void query(int year, int month, int day) {
        month++;
        if (!isBindView()) return;
        view.updateDataText(StrUtil.format("%d-%d-%d",year,month,day));
        view.showProgressBar();
        try{

            StringBuffer sb = new StringBuffer();
            sb.append(year);
            if (month<10) sb.append("0").append(month);
            else sb.append(month);
            if (day<10) sb.append(0).append(day);
            else sb.append(day);

            AppSchedvech[] arr = model.getHistoryTask(userInfo.userId,sb.toString());
            LLog.print(JsonUti.javaBeanToJson(arr));

        }catch (Exception e){
            e.printStackTrace();
        }
        view.hindProgressBar();
    }
}
