package otmsapp.ping.mvp.basics;

import android.app.Activity;
import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ViewBindPresenter<Presenter extends IPresenter> extends Activity implements IView{

    protected Presenter presenter = createPresenterImp();

    private Presenter createPresenterImp() {
        ParameterizedType parameterizedType = (ParameterizedType)this.getClass().getGenericSuperclass();
        Type[] typeArr = parameterizedType.getActualTypeArguments();
        Class cls =(Class) typeArr[0];
        try {
            Constructor cons = cls.getConstructor();//获取有参构造
            return (Presenter) cons.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter!=null){
            presenter.bindView(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (presenter!=null){
            presenter.unbindView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter!=null) presenter=null;
    }
}
