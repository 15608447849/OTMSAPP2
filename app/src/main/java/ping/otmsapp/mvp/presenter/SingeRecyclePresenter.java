package ping.otmsapp.mvp.presenter;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.SingeRecycleContract;
import ping.otmsapp.mvp.model.AppInterfaceModel;
import ping.otmsapp.mvp.view.LoginActivity;

/**
 * Created by Leeping on 2019/3/15.
 * email: 793065165@qq.com
 */
public class SingeRecyclePresenter extends PresenterViewBind<SingeRecycleContract.View> implements SingeRecycleContract.Presenter {

    private SingeRecycleContract.Model model = new AppInterfaceModel();

    private UserInfo userInfo = new UserInfo().fetch();

    private List<String> boxList = new ArrayList<>();

    @Override
    public void init() {
        if (isBindView()){
            view.setInfo(userInfo.name,userInfo.id+"");
            view.uploadNumber(boxList.size()+"");
        }
    }

    @Override
    public void logout(Activity activity) {
        userInfo.remove();
        activity.startActivity(new Intent(activity, LoginActivity.class));
        activity.finish();
    }

    @Override
    public void scanHandle(String codeBar) {
        if (isUpload) {
            view.toast("上传中");
        }
        if (boxList.contains(codeBar)){
            view.toast("重复: " + codeBar);
            return;
        }
        boxList.add(codeBar);
        view.uploadNumber(boxList.size()+"");
    }

    private volatile boolean isUpload = false;
    @Override
    public void upload() {
        if(isUpload) {
            view.toast("上传中");
        }
        isUpload = true;
        Iterator<String> iterator = boxList.iterator();
        String boxCode;
        if (iterator.hasNext()){
            boxCode = iterator.next();
            if (model.uploadSingeRecycle(boxCode,userInfo.name,userInfo.id)) iterator.remove();
        }
        isUpload = false;
        view.toast("上传成功");
        view.uploadNumber(boxList.size()+"");
    }
}
