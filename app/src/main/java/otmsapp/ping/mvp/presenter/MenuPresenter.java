package otmsapp.ping.mvp.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.SoftReference;

import otmsapp.ping.entitys.UserInfo;
import otmsapp.ping.mvp.basics.PresenterViewBind;
import otmsapp.ping.mvp.contract.MenuContract;
import otmsapp.ping.mvp.view.LoginActivity;
import otmsapp.ping.mvp.view.WarnActivity;
import otmsapp.ping.server.LoopService;

public class MenuPresenter extends PresenterViewBind<MenuContract.View> implements MenuContract.Presenter {

    private SoftReference<Activity> softReference ;

    public MenuPresenter(Activity activity) {
        this.softReference = new SoftReference<>(activity);
    }

    @Override
    public void openHistory() {

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