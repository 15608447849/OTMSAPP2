package ping.otmsapp.entitys

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import ping.otmsapp.R
import ping.otmsapp.log.LLog
import ping.otmsapp.tools.AppUtil

/**
 * 默认版本升级
 */
class DefaultVersionUpImp(val context:Context) : Runnable{

    private val updater  = VersionUpdater()
    private val versionFileName = "app.version"
    private val apkFileName = "app.apk"
    override fun run() {
        //检查
        if (updater.isExistNewVersion(context,versionFileName)){
            //下载
           val file =  updater.downloadNewApk(context,apkFileName)
            if (file!=null){
                AppUtil.installApk(context, file.canonicalPath)
            }
        }
    }
}