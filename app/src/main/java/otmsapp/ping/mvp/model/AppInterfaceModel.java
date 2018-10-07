package otmsapp.ping.mvp.model;

import cn.hy.otms.rpcproxy.appInterface.AppInterfaceServicePrx;
import cn.hy.otms.rpcproxy.appInterface.DispatchInfo;
import cn.hy.otms.rpcproxy.appInterface.WarnsInfo;
import cn.hy.otms.rpcproxy.comm.cstruct.BoolMessage;
import otmsapp.ping.entitys.except.Abnormal;
import otmsapp.ping.entitys.recycler.RecyclerBox;
import otmsapp.ping.tools.JsonUti;
import otmsapp.ping.zerocice.IceServerAbs;

public class AppInterfaceModel extends IceServerAbs<AppInterfaceServicePrx> {



    /**
     * 获取调度信息
     */
    public DispatchInfo dispatchInfoSync(final int userid, final String schedth){
        try{
            printParam("获取调度单",userid,schedth);
            //用户码
            return getProxy().heartbeatByDriverC(convert(userid,schedth));
        }catch (Exception e){
           e.printStackTrace();
        }
        return null;
    }


    /**
     * 更改调度单中某订单对应的箱子信息
     * 状态（0未装卸，1已装，2已卸）
     */
    public BoolMessage changeBoxStateSync(long schedth, final String cusid, String boxNo, int state, long time){
        try{
            printParam("修改箱子状态",schedth,boxNo,state);
            //状态,箱号,调度车次,机构码, 装载或者卸货对应的时间
            return getProxy().updateBoxStatus(convert(
                    state,
                    boxNo,
                    schedth,
                    cusid,
                    time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 修改调度单状态
     */
    public BoolMessage changeDispatchStateSync(final long schedtn,final String userid, final int state,final long time){
        try{
            printParam("改变调度状态",schedtn,userid,state);
            //车次号,用户码,状态,修改时间
            return getProxy().updateSchedvechStatus(convert(schedtn,userid,state,time));
        }catch (Exception e){
            e.printStackTrace();
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
            e.printStackTrace();
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
            printParam("传送轨迹",schedtn,vechid,"轨迹点:" + flag,"状态码:"+state );
            //车次号,车牌号,轨迹点数,轨迹json,状态码
            return getProxy().addTrail(convert(
                    schedtn,
                    vechid,
                    flag,
                    traceJson,
                    state
            ));
        }catch (Exception e){
           e.printStackTrace();
        }
        return  -1;
    }


    /**
     * 异常反馈
     */
    public BoolMessage addAbnormal(Abnormal abnormal){
        try{
            printParam("添加箱子异常信息", JsonUti.javaBeanToJson(abnormal));

            //异常发生人用户码,异常车次,异常机构码,异常箱号,异常类型,发生异常时间,异常说明
            //异常处理人用户码,异常处理机构码,异常处理时间,异常处理说明
            return getProxy().changeAbnormal(convert(
                    abnormal.abnormalUserCode,abnormal.carNumber,abnormal.abnormalCustomerAgency,abnormal.abnormalBoxNumber,abnormal.abnormalType,abnormal.abnormalTime,abnormal.abnormalRemakes,
                    abnormal.handlerUserCode, abnormal.handleCustomerAgency,abnormal.handlerTime,abnormal.handlerRemakes
            ));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 修改回收箱
     */
    public BoolMessage updateRecycleBoxSync(RecyclerBox recyclerBox){
        try{
            printParam("正常回收箱",JsonUti.javaBeanToJson(recyclerBox));
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
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 回收箱子数量
     * 调度车次,门店编号,调剂个数,退货个数,回收时间
     */
    public BoolMessage updateRecycleBoxNumberSync(final long schedtn,final String cusid,final int backTypeNum,final int transferTypeNumber,final long time) {
        try{
            printParam("回收-纸箱",schedtn,cusid,backTypeNum,transferTypeNumber,time);
            //调度车次,用户码,箱号,回收类型,回收时间
            return getProxy().grade(convert(
                    schedtn,
                    cusid,
                    backTypeNum,
                    transferTypeNumber,
                    time
            ));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取预警信息
     */
    public WarnsInfo queryTimeLaterWarnInfoByDriver(long trainNo, long time){
        try {
            printParam("预警数据",trainNo,time);
            return getProxy().queryWarnsInfo(convert("TimeLater",2,time,trainNo));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }







}
