package otmsapp.ping.mvp.basics;

public interface IPresenter<View extends IView> {

    void bindView(View view);
    boolean isBindView();
    void unbindView();
}
