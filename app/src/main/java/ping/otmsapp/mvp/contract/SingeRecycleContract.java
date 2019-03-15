package ping.otmsapp.mvp.contract;

import android.app.Activity;

import ping.otmsapp.mvp.basics.IPresenter;
import ping.otmsapp.mvp.basics.IView;

/**
 * Created by Leeping on 2019/3/15.
 * email: 793065165@qq.com
 */
public class SingeRecycleContract {

    public interface Model{
        //上传 箱号，姓名，用户码
        boolean uploadSingeRecycle(String boxCode,String name,int code);
    }

    public interface View extends IView {
        void setInfo(String name,String code);
        //更新数量
        void uploadNumber(String num);
    }

    public interface Presenter extends IPresenter<View> {
        void init();
        void logout(Activity activity);
        //扫码处理
        void scanHandle(String codeBar);
        void upload();
    }
}
