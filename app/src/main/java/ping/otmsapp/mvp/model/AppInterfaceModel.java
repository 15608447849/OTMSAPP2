package ping.otmsapp.mvp.model;

import java.io.File;

import cn.hy.otms.rpcproxy.appInterface.AppInterfaceServicePrx;
import cn.hy.otms.rpcproxy.appInterface.AppSchedvech;
import cn.hy.otms.rpcproxy.appInterface.DispatchInfo;
import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import cn.hy.otms.rpcproxy.appInterface.WarnsInfo;
import cn.hy.otms.rpcproxy.comm.cstruct.BoolMessage;
import ping.otmsapp.entitys.except.Abnormal;
import ping.otmsapp.entitys.recycler.RecyclerBox;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.contract.CostContract;
import ping.otmsapp.mvp.contract.HistoryContract;
import ping.otmsapp.mvp.contract.SingeRecycleContract;
import ping.otmsapp.mvp.contract.WarnContract;
import ping.otmsapp.tools.JsonUtil;
import ping.otmsapp.zerocice.IceServerAbs;

public class AppInterfaceModel extends IceServerAbs<AppInterfaceServicePrx> implements WarnContract.Model,HistoryContract.Model,CostContract.Model, SingeRecycleContract.Model {
    /**
     * 获取调度信息
     */
    public DispatchInfo dispatchInfoSync(final int userid, final String schedth){
        try{
            //printParam("获取调度单",userid,schedth);
            //用户码
            return getProxy().heartbeatByDriverC(convert(userid,schedth));
        }catch (Exception e){
           LLog.print(e);
        }
        return null;
    }


    /**
     * 更改调度单中某订单对应的箱子信息
     * 状态（0未装卸，1已装，2已卸）
     */
    public BoolMessage changeBoxStateSync(long schedth, final String cusid, String boxNo, int state, long time){
        try{
            printParam("修改箱子状态",schedth,boxNo,state,time);
            //状态,箱号,调度车次,机构码, 装载或者卸货对应的时间
            return getProxy().updateBoxStatus(convert(
                    state,
                    boxNo,
                    schedth,
                    cusid,
                    time));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }

    /**
     * 修改调度单状态
     */
    public BoolMessage changeDispatchStateSync(final long schedtn,final String userid, final int state,final long time){
        try{
            printParam("改变调度状态",schedtn,userid,state,time);
            //车次号,用户码,状态,修改时间
            return getProxy().updateSchedvechStatus(convert(schedtn,userid,state,time));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }

    /**
     * 改变车辆状态
     */
    public BoolMessage changeVehicleStateSync(final long schedtn,final String vechid,final int status){
        try{
            printParam("改变车辆状态",vechid,status);
            return getProxy().updateVehStatus(convert(schedtn,vechid,status));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }

    /**
     * 轨迹信息传送
     * 车牌,车次,轨迹记录对象
     * state : 2.实时 4.历史(完成)
     */
    public int addTrail(final long schedtn,final String vechid,String traceJson,int flag,int state){
        try{
            printParam("传送轨迹",schedtn,vechid," 轨迹点:" + flag,"状态码:"+state );
            //车次号,车牌号,轨迹点数,轨迹json,状态码
            return getProxy().addTrail(convert(
                    schedtn,
                    vechid,
                    flag,
                    traceJson,
                    state
            ));
        }catch (Exception e){
           LLog.print(e);
        }
        return  -1;
    }


    /**
     * 异常反馈
     */
    public BoolMessage addAbnormal(Abnormal abnormal){
        try{
            printParam("添加箱子异常信息", JsonUtil.javaBeanToJson(abnormal));

            //异常发生人用户码,异常车次,异常机构码,异常箱号,异常类型,发生异常时间,异常说明
            //异常处理人用户码,异常处理机构码,异常处理时间,异常处理说明
            return getProxy().changeAbnormal(convert(
                    abnormal.abnormalUserCode,abnormal.carNumber,abnormal.abnormalCustomerAgency,abnormal.abnormalBoxNumber,abnormal.abnormalType,abnormal.abnormalTime,abnormal.abnormalRemakes,
                    abnormal.handlerUserCode, abnormal.handleCustomerAgency,abnormal.handlerTime,abnormal.handlerRemakes
            ));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }


    /**
     * 修改回收箱
     */
    public BoolMessage updateRecycleBoxSync(RecyclerBox recyclerBox){
        try{
            printParam("回收箱-正常扫码", JsonUtil.javaBeanToJson(recyclerBox));
            //调度车次,用户码,箱号,回收类型,回收时间,回收时门店ID
            return getProxy().updateRecycle(convert(
                    recyclerBox.carNumber,
                    recyclerBox.userCode,
                    recyclerBox.boxNo,
                    recyclerBox.type,
                    recyclerBox.time,
                    recyclerBox.storeId
            ));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }

    /**
     * 回收箱子数量
     * 调度车次,门店编号,调剂个数,退货个数,回收时间
     */
    public BoolMessage updateRecycleBoxNumberSync(final long schedtn,final String cusid,final int backTypeNum,final int transferTypeNumber,final long time) {
        try{
            printParam("回收箱-手输纸箱",schedtn,cusid,backTypeNum,transferTypeNumber,time);
            //调度车次,用户码,箱号,回收类型,回收时间
            return getProxy().grade(convert(
                    schedtn,
                    cusid,
                    transferTypeNumber,
                    backTypeNum,
                    time
            ));
        }catch (Exception e){
            LLog.print(e);
        }
        return null;
    }

    /**
     * 获取预警信息
     */
    public WarnsInfo queryTimeLaterWarnInfoByDriver(long trainNo, long time){
        try {
            printParam("获取预警数据",trainNo,time);
            return getProxy().queryWarnsInfo(convert("TimeLater",2,time,trainNo));
        } catch (Exception e) {
            LLog.print(e);
        }
        return null;
    }

    @Override
    public boolean handleWarn(String codeBar,long time){
        try {
            printParam("处理预警数据",codeBar,time);
            return getProxy().updateWarnState(convert(codeBar,time));
        } catch (Exception e) {
            LLog.print(e);
        }
        return false;
    }


    @Override
    public AppSchedvech[] getHistoryTask(int userId, String y_m_d) {
        try {
            printParam("获取历史任务",userId,y_m_d);
            return  getProxy().queryVechInfoPage(convert(userId,y_m_d));
        } catch (Exception e) {
            LLog.print(e);
        }
        return null;
    }

    @Override
    public SureFeeInfo[] getCostBill(int userId, String y_m_d) {
        try {
            printParam("获取运费账单",userId,y_m_d);
            return  getProxy().appSureFeeInfo(convert(userId,y_m_d));
        } catch (Exception e) {
            LLog.print(e);
        }
        return null;
    }

    @Override
    public boolean optionCostBill(int userId, long train, int opCode) {
        try {
            printParam("确认费用信息",userId,train,opCode);
            BoolMessage boolMessage = getProxy().updateFeeStatu(convert(train,opCode,userId,System.currentTimeMillis()));
            return boolMessage.flag;
        } catch (Exception e) {
            LLog.print(e);
        }
        return false;
    }

    @Override
    public boolean uploadFile(File image, String serverFilePath, String serverFileName) {
        //node
        return false;
    }

    public boolean addBackCard(String despatchId,String storeId,String fileName){
        try {
            printParam("上传回单信息",despatchId,storeId,fileName);
            //调度车次、门店编码、文件名
            BoolMessage boolMessage = getProxy().addBackCard(convert(despatchId,storeId,fileName));
            return boolMessage.flag;
        } catch (Exception e) {
            LLog.print(e);
        }
        return false;
    }

    @Override
    public boolean uploadSingeRecycle(String boxCode, String name, int code) {
        try {
            printParam("单页回收上传箱号",boxCode,name,code);
            //调度车次、门店编码、文件名
            BoolMessage boolMessage = getProxy().appDrtRecy(convert(boxCode,name,code));
            return boolMessage.flag;
        } catch (Exception e) {
            LLog.print(e);
        }
        return false;
    }
}
