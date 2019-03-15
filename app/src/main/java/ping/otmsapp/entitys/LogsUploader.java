package ping.otmsapp.entitys;

import java.io.File;
import java.io.IOException;

import ping.otmsapp.entitys.upload.FileUploadItem;
import ping.otmsapp.entitys.upload.FileUploadItemList;
import ping.otmsapp.log.LLog;

/**
 * Created by Leeping on 2019/2/22.
 * email: 793065165@qq.com
 * 日志上传
 */
public class LogsUploader implements Runnable{
    @Override
    public void run() {
        UserInfo info = new UserInfo().fetch();
        if (info == null) return;
        File dirs = new File(LLog.getBuild().getLogFolderPath());
        if (!dirs.exists()) return;
        File[] files = dirs.listFiles();
        FileUploadItemList list = new FileUploadItemList().fetch();
        if (list == null) list = new FileUploadItemList();
        tag:for (File file : files){
            try {
                for (FileUploadItem it : list.list){
                    if(it.localFullPath.equals(file.getCanonicalPath())){
                        continue tag;
                    }
                }

                FileUploadItem item = new FileUploadItem(2);
                item.localFullPath = file.getCanonicalPath();
                item.serverPath = "/app/logs/" + info.id + "/";
                item.isDel = false;
                list.list.add(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        list.save();
    }
}
