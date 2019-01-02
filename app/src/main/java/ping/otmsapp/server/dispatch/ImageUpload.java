package ping.otmsapp.server.dispatch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ping.otmsapp.entitys.dispatch.VehicleInfo;
import ping.otmsapp.entitys.upload.BillImage;
import ping.otmsapp.entitys.upload.BillImageList;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.model.FileUploadModel;

public class ImageUpload extends Thread{

    private volatile boolean isStart = true;

    public void stopUploadLoop(){
        isStart = false;
        billUploadSync();
    }
   public ImageUpload(){
       start();
   }

    @Override
    public void run() {
        while (isStart) {
            synchronized (this){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            billUpload();
        }
    }

    public void billUploadSync(){
        synchronized (this){
            this.notify();
        }
    }

    private FileUploadModel fileUploadModel = new FileUploadModel();

    private volatile boolean isRunning = false;


    public void billUpload(){
        try{
            if (isRunning) return;
            BillImageList list = new BillImageList().fetch();
            if (list == null || list.list.size() == 0) return;

            isRunning = true;

            ArrayList<String> delList = new ArrayList<>();

            File file;
            boolean result;
            for (BillImage it : list.list){
                file = new File(it.path);
                if (!file.exists()){
                    delList.add(it.path);
                    continue;
                }
                result= fileUploadModel.uploadImage(file,"/sched/img/"+it.dispatchId+"/",file.getName());
                if (result){
                    //通知信息
                    result = fileUploadModel.addBackCard(it.dispatchId,it.storeId,file.getName());//上传回单
                    if (result) delList.add(it.path);

                }
            }


            if (delList.size()>0){
                Iterator<String> it;
                list = new BillImageList().fetch();
                Iterator<BillImage> iterator = list.list.iterator();
                BillImage billImage;
                String path;
                while (iterator.hasNext()){
                    billImage = iterator.next();
                    it = delList.iterator();
                    while (it.hasNext()){
                        path = it.next();
                        if(path.equals(billImage.path)){
                            iterator.remove();
                            it.remove();
//                            LLog.print("删除:"+path);
                            File f =  new File(path);
                            boolean flag = f.delete();
//                            LLog.print("删除文件:" + flag );
                            break;
                        }
                    }
                }

                list.save();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        isRunning = false;
//        LLog.print("-------------------------------------------------------------------------------");
    }
}
