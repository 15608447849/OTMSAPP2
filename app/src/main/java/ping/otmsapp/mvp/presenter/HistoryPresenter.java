package ping.otmsapp.mvp.presenter;

import java.util.ArrayList;
import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.AppFee;
import cn.hy.otms.rpcproxy.appInterface.AppSchedvech;
import cn.hy.otms.rpcproxy.appInterface.Line;
import cn.hy.otms.rpcproxy.appInterface.Rcuorg;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.entitys.history.DispatchDetail;
import ping.otmsapp.entitys.history.StoreDetail;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.HistoryContract;
import ping.otmsapp.mvp.model.AppInterfaceModel;
import ping.otmsapp.tools.StrUtil;

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

            AppSchedvech[] arr = model.getHistoryTask(userInfo.id,sb.toString());
            //转换数据
            List<DispatchDetail> list = new ArrayList<>();
            if (arr!=null && arr.length>0){
                convert(arr,list);
            }
            view.updateList(list);
        }catch (Exception e){
            e.printStackTrace();
        }
        view.hindProgressBar();
    }

    @Override
    public void convert(AppSchedvech[] array, List<DispatchDetail> list) {
        DispatchDetail dispatchDetail;
        StoreDetail storeDetail;

        Line line;
        AppFee appFee;
        for (AppSchedvech appSchedvech : array) {
            dispatchDetail = new DispatchDetail();
            dispatchDetail.trainNo = appSchedvech.schedtn;

            if (appSchedvech.appFee.length==1){ //费用信息
                appFee = appSchedvech.appFee[0];
                dispatchDetail.totalFee = appFee.totalFee;
                dispatchDetail.initialFee = appFee.iniamount;
                dispatchDetail.abnormalFee = appFee.chgFee;
            }

            dispatchDetail.storeDetails = new ArrayList<>();
            line = appSchedvech.line;
            for (Rcuorg rcuorg : line.rcuorgList) {
                storeDetail = new StoreDetail();
                storeDetail.address = rcuorg.addr;
                storeDetail.simName = rcuorg.cusabbname;
                storeDetail.boxNoList = new CharSequence[rcuorg.lpn.length];
                for (int i = 0 ;i < rcuorg.lpn.length;i++) {
                    storeDetail.boxNoList[i] = rcuorg.lpn[i].lpn;//添加箱号
                }
                dispatchDetail.storeDetails.add(storeDetail);//添加门店
            }
            list.add(dispatchDetail);
        }
    }
}
