package ping.otmsapp.server.dispatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ping.otmsapp.entitys.upload.FileUploadItem;
import ping.otmsapp.entitys.upload.FileUploadItemList;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.model.FileUploadModel;
import ping.otmsapp.tools.JsonUtil;

/**
 * 回单上传
 */
public class FileUploader extends DispatchThreadAbs{

    private FileUploadModel fileUploadModel = new FileUploadModel();

    private volatile boolean isRunning = false;

    @Override
    void execute() {
        if (isRunning) return;
        isRunning = true;
        fileUploadExecute();
        isRunning = false;
    }


    private void fileUploadExecute() {
        try{

            FileUploadItemList list = new FileUploadItemList().fetch();
            if (list == null || list.list.size() == 0) return;
            ArrayList<String> delList = new ArrayList<>();
            File file;
            boolean result;
            for (FileUploadItem it : list.list){
                file = new File(it.localFullPath);
                if (!file.exists()){
                    delList.add(it.localFullPath);
                    continue;
                }
                result= fileUploadModel.uploadFile(
                        file,it.serverPath,
                        it.serverName == null? file.getName(): it.serverName
                        );
                if (result){
                    delList.add(it.localFullPath); //添加到删除列表
                    //通知信息
                    if (it.type == 1){
                        //上传回单
                        result = fileUploadModel.addBackCard(
                                it.param.get("dispatchId"),
                                it.param.get("storeId"),
                                file.getName()
                        );
                        LLog.print("上传回单: "+ JsonUtil.javaBeanToJson(it)+" 结果: "+ result);
                    }

                }
            }
            if (delList.size()>0){
                Iterator<String> it;
                list = new FileUploadItemList().fetch();
                Iterator<FileUploadItem> iterator = list.list.iterator();
                FileUploadItem item;
                String path;
                while (iterator.hasNext()){
                    item = iterator.next();
                    it = delList.iterator();
                    while (it.hasNext()){
                        path = it.next();
                        if(path.equals(item.localFullPath)){
                            iterator.remove();//删除记录
                            it.remove();
                            if(item.isDel){
                                boolean flag = new File(path).delete();
                                LLog.print("删除文件 - " + path+" - "+ flag );
                            }
                            break;
                        }
                    }
                }
                list.save();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
