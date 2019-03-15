package ping.otmsapp.mvp.presenter;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.SoftReference;

import ping.otmsapp.R;
import ping.otmsapp.entitys.IO;
import ping.otmsapp.entitys.LogsUploader;
import ping.otmsapp.entitys.UserInfo;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.basics.PresenterViewBind;
import ping.otmsapp.mvp.contract.MenuContract;
import ping.otmsapp.mvp.view.CostActivity;
import ping.otmsapp.mvp.view.DispatchActivity;
import ping.otmsapp.mvp.view.HistoryActivity;
import ping.otmsapp.mvp.view.LoginActivity;
import ping.otmsapp.mvp.view.WarnActivity;
import ping.otmsapp.server.dispatch.DispatchOperation;
import ping.otmsapp.server.dispatch.LoopService;
import ping.otmsapp.tools.AppUtil;
import ping.otmsapp.tools.DialogUtil;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MenuPresenter extends PresenterViewBind<MenuContract.View> implements MenuContract.Presenter {

    private SoftReference<Activity> softReference ;

    public MenuPresenter(Activity activity) {
        this.softReference = new SoftReference<>(activity);
    }

    @Override
    public void openHistory() {
        if (softReference.get()!=null){
            Activity activity = softReference.get();
            Intent intent = new Intent(activity,HistoryActivity.class);
            activity.startActivity(intent);
        }
    }

    @Override
    public void openWarn() {
        if (softReference.get()!=null){
            Activity activity = softReference.get();
            Intent intent = new Intent(activity, WarnActivity.class);
            activity.startActivity(intent);
        }
    }

    @Override
    public void openCost() {
        if (softReference.get()!=null){
            Activity activity = softReference.get();
            Intent intent = new Intent(activity, CostActivity.class);
            activity.startActivity(intent);
        }
    }

    @Override
    public void createShortCut() {
        if (softReference.get()!=null){
            AppUtil.addShortcut(softReference.get(), R.drawable.ic_launcher,false);
            view.toast("创建快捷方式成功");
        }
    }

    @Override
    public void clearDispatch() {
        if (softReference.get()!=null){
            //弹窗确认
            DialogUtil.dialogSimple2(softReference.get(),  "是否清理当前存在的调度任务,清理后将影响正常的流程执行", "确定", new DialogUtil.Action0() {
                @Override
                public void onAction0() {
                    LLog.print("手动执行清理调度操作");
                    new DispatchOperation().forceDelete();
                    Activity activity = softReference.get();
                    Intent intent = new Intent(activity, DispatchActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("message","dispatch");
                    activity.startActivity(intent);
                }
            });
        }
    }

    @Override
    public void uploadLog() {
        IO.pool(new LogsUploader()); //日志上传
        view.toast("已执行日志上传");
    }

    @Override
    public void logout() {
        if (softReference.get()!=null){
            new UserInfo().remove();
            Activity activity = softReference.get();
            activity.startActivity(new Intent(activity, LoginActivity.class));
            activity.finish();
            softReference.clear();
        }
    }

    @Override
    public void exit() {
        if (softReference.get()!=null){
            Activity activity = softReference.get();
            activity.stopService(new Intent(activity, LoopService.class));
            activity.finish();
            System.exit(0);
            softReference.clear();
        }
    }
}
