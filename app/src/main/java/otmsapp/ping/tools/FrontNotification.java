package otmsapp.ping.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Leeping on 2018/5/2.
 * email: 793065165@qq.com
 */

public class FrontNotification {

    public static class Build{
        Context context;
        int id = 1000;
        Notification notification;
        NotificationManager notificationManager;
        Intent activityIntent;//点击通知栏-跳转到指定Activity
        int[] flags = new int[]{Notification.FLAG_FOREGROUND_SERVICE,Notification.FLAG_NO_CLEAR};
        int defaults = Notification.DEFAULT_LIGHTS;
        String groupKey = "default";
        Intent serviceIntent;//点击打开指定服务
        public Build(Context context){
            this.context = context;
            this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        public FrontNotification.Build setId(int id){
            this.id = id;
            return this;
        }

        public FrontNotification.Build setActivityIntent(Class<?> destCls){
            if (destCls!=null){
                activityIntent = new Intent(context, destCls);
            }
            return this;
        }

        public FrontNotification.Build setActivityIntent(String action, String scheme){
            activityIntent = new Intent(action, Uri.parse(scheme));
            return this;
        }

        public FrontNotification.Build setActivityIntent(Intent intent){
            this.activityIntent = intent;
            return this;
        }

        public FrontNotification.Build setServiceIntent(Class<?> destCls){
            if (destCls!=null){
                serviceIntent = new Intent(context, destCls);
            }
            return this;
        }

        public FrontNotification.Build setServiceIntent(Intent intent){
            this.serviceIntent = intent;
            return this;
        }

        public FrontNotification.Build setFlags(int[] flags){
            this.flags = flags;
            return this;
        }
        public FrontNotification.Build setDefaults(int defaults){
            this.defaults = defaults;
            return this;
        }

        public FrontNotification.Build setGroup(String groupKey){
            this.groupKey = groupKey;
            return this;
        }



        public FrontNotification autoGenerateNotification(String title, String content, String info, int icon){
            PendingIntent pIntent = null;
            if (activityIntent !=null){
                pIntent = PendingIntent.getActivity(context,0, activityIntent,0);
            }else if (serviceIntent!=null){
                pIntent = PendingIntent.getService(context,0, serviceIntent,0);
            }


            notification = geneNotify(pIntent,title,content,info,icon,defaults);

            if (flags!=null && flags.length>0){
                for (int flag : flags){
                    notification.flags |= flag;
                }
            }

            return new FrontNotification(this);
        }

        private Notification geneNotify(PendingIntent pIntent, String title, String content, String info, int icon, int defaults) {
            return new NotificationCompat.Builder(context)
                    .setSmallIcon(icon)

                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),icon))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setOngoing(true)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentInfo(info)
                    .setContentIntent(pIntent)
                    .setDefaults(defaults!=0?defaults:Notification.DEFAULT_ALL)
                    .setGroup(groupKey)
                    .build();
        }
    }

    private FrontNotification.Build build;
    private FrontNotification(FrontNotification.Build build){
        this.build = build;
    }

    public void showNotification() {
        build.notificationManager.notify(build.id, build.notification);
    }
    public void cancelNotification() {
        build.notificationManager.cancel(build.id);
    }

    public void startForeground(Service service){
        service.startForeground(build.id, build.notification);
    }
    public void stopForeground(Service service){
        service.stopForeground(false);
    }

}
