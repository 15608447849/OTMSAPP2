package ping.otmsapp.entitys;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.hy.otms.rpcproxy.sysmanage.UpdateResponsePackage;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.model.SysModel;
import ping.otmsapp.tools.AppUtil;

public class VersionUpdater {

    private SysModel model = new SysModel();

    /**
     * 检查是否存在新版本
     */
    public boolean isExistNewVersion(Context context,String remoteFileName){
        int remoteVersionCode = getRemoteVersionCode(remoteFileName);
        int localVersionCode = getLocalVersionCode(context);
        LLog.print("开启应用,当前版本: "+ localVersionCode+" ,服务器版本: "+ remoteVersionCode);
        return remoteVersionCode>localVersionCode;
    }
    private int getRemoteVersionCode(String remoteFileName) {
        UpdateResponsePackage updateResponsePackage =
                model.getFileBytes(remoteFileName,1);
        if (updateResponsePackage!=null && updateResponsePackage.status == 0){
            String versionStr = new String(updateResponsePackage.packageInfo);
            return Integer.parseInt(versionStr);
        }
        return -1;
    }
    private int getLocalVersionCode(Context context) {
        return AppUtil.getVersionCode(context);
    }

    /**
     * 下载新版本APK
     */
    public File downloadNewApk(Context context,String remoteFileName){
        File dirs = new File(context.getExternalCacheDir()+"/apks");
        if (!dirs.exists()) dirs.mkdirs();
        File file = new File(dirs, System.currentTimeMillis()+".apk");
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file,"rw");
            int index = 1;
            while (true){
                UpdateResponsePackage updateResponsePackage = model.getFileBytes(remoteFileName,index);
                if (updateResponsePackage==null || updateResponsePackage.status!=0) return null;
                raf.seek((updateResponsePackage.index-1) * updateResponsePackage.size); //起点
                raf.write(updateResponsePackage.packageInfo);
                index++;
                if (index>updateResponsePackage.total) break;
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            file.delete();
        }finally {
            if (raf!=null){
                try {
                    raf.close();
                    raf = null;
                } catch (IOException ignored) { }
            }
        }
        return null;
    }
}
