package otmsapp.ping.entitys.scanner;

import android.content.Context;

import java.util.concurrent.SynchronousQueue;

import otmsapp.ping.log.LLog;
import otmsapp.ping.tools.AppUtil;

public abstract class ScannerApiThread extends Thread {

    boolean isEnable = true;
    SynchronousQueue<String> queue = new SynchronousQueue<>();
    ScannerApiThread(Context context) {
        setName("scanner-"+getClass().getSimpleName()+"-"+ getId());
        setDaemon(true);
        init(context);
        start();
    }

    abstract void init(Context context);

    public void enable(){
        isEnable = true;
    }
    public void disable(){
        isEnable = false;
    }

    public void stopScan() {
        isRun = true;
    }

    ScannerCallback callback;

    public void setScanCallback(ScannerCallback scanCallback) {
        this.callback = scanCallback;
    }
    volatile boolean isRun = true;
    @Override
    public void run() {
        while (isRun){
            try {
                String code = queue.take();//堵塞获取数据
                if(this.callback!=null && isEnable) this.callback.onScanner(code);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
