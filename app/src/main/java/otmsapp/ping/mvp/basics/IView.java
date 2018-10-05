package otmsapp.ping.mvp.basics;

public interface IView {

    /**
     * 打开进度条
     */
    void showProgressBar();

    /**
     * 关闭进度条
     */
    void hindProgressBar();

    /**
     * 打印消息
     */
    void toast(String message);
}
