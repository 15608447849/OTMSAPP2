package ping.otmsapp.mvp.presenter;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.entitys.cost.FeeDetail;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.CostContract;
import ping.otmsapp.mvp.model.FileUploadModel;
import ping.otmsapp.tools.JsonUti;
import ping.otmsapp.tools.StrUtil;

public class CostPresenter  extends PresenterViewBind<CostContract.View> implements CostContract.Presenter {

    private UserInfo userInfo = new UserInfo().fetch();
    private CostContract.Model model = new FileUploadModel();

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

            SureFeeInfo[] arr = model.getCostBill(userInfo.id,sb.toString());
            LLog.print(JsonUti.javaBeanToJson(arr));
            //转换数据
            List<FeeDetail> list = new ArrayList<>();
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
    public void convert(SureFeeInfo[] array, List<FeeDetail> list) {
        FeeDetail detail;
        for (SureFeeInfo sureFeeInfo : array){
           detail = new FeeDetail();
           detail.trainNo = sureFeeInfo.schedtn;
           detail.plateNo = sureFeeInfo.vechid;
           detail.mileage =  (sureFeeInfo.gpsm+sureFeeInfo.zcgpsm) / 1000f;
           detail.boxTotal = sureFeeInfo.sumCnt;
           detail.storeTotal = sureFeeInfo.custom;
           detail.shouldFee = sureFeeInfo.initFee/100f;
           detail.actual =  sureFeeInfo.lastFee/100f;
           list.add(detail);
        }
    }

    @Override
    public void rejectCostBill(FeeDetail feeDetail) {
        if (!isBindView()) return;
        view.showProgressBar();
        try {
            boolean flag = model.optionCostBill(userInfo.id,feeDetail.trainNo,0);
            if (flag){
                view.toast("已驳回订单,车次号:"+feeDetail.trainNo );
                view.refreshList();
            }else{
                view.toast("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.hindProgressBar();

    }

    @Override
    public void sureCostBill(FeeDetail feeDetail) {
        if (!isBindView()) return;
        view.showProgressBar();
        try {
            boolean flag = model.optionCostBill(userInfo.id,feeDetail.trainNo,2);
            if (flag){
                view.toast("确认成功,车次号:"+feeDetail.trainNo );
                view.refreshList();
            }else{
                view.toast("操作失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.hindProgressBar();
    }

    @Override
    public void preUploadImage(final FeeDetail feeDetail){
        if (!isBindView()) return;
        File file = new File(Environment.getExternalStorageDirectory(),feeDetail.trainNo+".png");
        if (!file.exists()){
            try {
                if (!file.createNewFile()){
                    throw new IOException();
                };
            } catch (IOException e) {
                e.printStackTrace();
                view.toast("图片文件无法创建");
                return;
            }
        }
        view.selectPicture(file);//打开图片选择
    }

    @Override
    public void uploadImage(File image) {
        if (!isBindView()) return;
        if (image==null || !image.exists() ){
            view.toast("上传失败,图片不存在");
            return;
        }
        view.showProgressBar();
        try {
            boolean flag = model.uploadImage(image,"/sched/img/",image.getName());
            if (flag){
                view.toast("上传成功");
            }else{
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.toast("上传失败");
        }
        view.hindProgressBar();
    }
}
