package ping.otmsapp.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import ping.otmsapp.log.LLog;

/**
 * Created by Leeping on 2018/5/2.
 * email: 793065165@qq.com
 * 心跳服务
 */

public abstract class HearServer extends Service implements Runnable {
    private final Thread thread = new Thread(this);

    private PendingIntent pendingIntentOp; //闹钟使用

    private PowerUse power; //电源管理

    private FrontNotification notification;//前台通知栏

    private volatile boolean isRun = true;

    private long interval = 30 * 1000L;

    public void setInterval(long interval) {
        this.interval = interval;
    }

    @Override
    public void onCreate() {
        initialize();
        power = new PowerUse(getApplicationContext(),getClass().getSimpleName());
        notification = createForeNotification(new FrontNotification.Build(getApplicationContext()));
        thread.setDaemon(true);
        thread.setName(getClass()+"-"+Thread.currentThread().getId());
        thread.start();
    }

    protected void initialize(){};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        unlockSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (notification!=null) notification.stopForeground(this);
        stopAlarm();
        isRun = false;
        unlockSelf();

    }

     protected abstract FrontNotification createForeNotification(FrontNotification.Build build);

    @Override
    public void run() {
        while (isRun){
            power.startPowerWakeLockByCPU();//获取CPU
            try {
                executeTask();
            } catch (Exception e) {
                e.printStackTrace();
            }
            power.stopPowerWakeLock();//解锁CPU

            if (notification!=null) notification.startForeground(this);//刷新通知栏
            startAlarmManagerHeartbeat();//开始闹钟
            lockSelf();//线程锁定
        }
    }
    protected abstract void executeTask();
    public void startAlarmManagerHeartbeat() {
        if (pendingIntentOp==null){
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), this.getClass());
            pendingIntentOp = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        }
        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        if (am!=null){
            if (Build.VERSION.SDK_INT>=19) {
                am.setExact(AlarmManager.RTC_WAKEUP, getNextTime(),  pendingIntentOp);
            }else{
                am.set(AlarmManager.RTC_WAKEUP, getNextTime(),  pendingIntentOp);
            }
        }
    }
    public void stopAlarm(){
        if (pendingIntentOp!=null){
            AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
            assert am!=null;
            am.cancel(pendingIntentOp);
        }
    }
    protected long getNextTime() {
        return System.currentTimeMillis() + interval;
    }
    private void lockSelf() {
        synchronized (this){
            try { wait(); } catch (InterruptedException ignored) { }
        }
    }
    private void unlockSelf(){
        synchronized (this){ notifyAll(); }
    }


}
