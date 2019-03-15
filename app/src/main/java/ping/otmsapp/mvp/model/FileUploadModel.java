package ping.otmsapp.mvp.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import cn.hy.otms.rpcproxy.dts.FileUploadInfo;
import cn.hy.otms.rpcproxy.dts.IFileUploadServicePrx;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.contract.CostContract;
import ping.otmsapp.zerocice.IceServerAbs;

public class FileUploadModel extends IceServerAbs<IFileUploadServicePrx> implements CostContract.Model{

    private AppInterfaceModel model = new AppInterfaceModel();

    @Override
    public SureFeeInfo[] getCostBill(int userId, String y_m_d) {
        return model.getCostBill(userId,y_m_d);
    }

    @Override
    public boolean optionCostBill(int userId, long train, int opCode) {
        return model.optionCostBill(userId,train,opCode);
    }

    @Override
    public boolean uploadFile(File file, String serverFilePath, String serverFileName) {

        if (file.length()==0) return true;
        printParam("上传文件",file,"文件大小:"+ ( file.length()/1024.0 )+" KB");
        RandomAccessFile raf = null;
        try{
            final IFileUploadServicePrx prx = getProxy();
            FileUploadInfo info =new FileUploadInfo(serverFilePath,serverFileName,file.length());

            String tag  = getProxy().request(info);

            if(tag ==null || tag.length() == 0) return false;

            if(tag.equals("wait")){
                printParam("上传文件",file,"服务器队列满额-下次再次尝试");
                Thread.sleep((int)(1+Math.random()*10) * 1000); //随机 1-10秒
                return false;
            }
            raf = new RandomAccessFile(file,"r");
            byte[] bytes = new byte[512];
            long pos  = 0L;
            long len = file.length();
            int size;
            long time = System.currentTimeMillis();
            while (pos < len){
                size = (int)(len - pos);
                if (size > bytes.length) size = bytes.length;

                //移动到起点
                raf.seek(pos);

                //读取数据
                raf.read(bytes,0,size);
//                long t = System.currentTimeMillis();
//                Log.d("文件传输","size = " + size);

                //写入数据
                prx.transfer(tag,pos,bytes);
//             Log.d("文件传输",file+"- pos = " + pos);
                //起点下移
                pos+=size;
//                LLog.print(".0 - "+ pos,"len = "+ len);
//                Log.d("文件传输",flag+" - 方法调用时间: "+ (System.currentTimeMillis() - t) + String.format("ms , %d / %d - 当前百分比:%.2f",pos,len,( pos * 1.0 / len )));
            }

            raf.close();
            //通知文件上传完成
            prx.complete(tag);
            printParam("上传文件",file,"总耗时: "+ ((System.currentTimeMillis() - time)/1000.0) +" 秒" );
            return true;
        }catch (Exception e){
            LLog.print(e);
        }finally {
            if (raf!=null) {
                try {
                    raf.close();
                } catch (IOException ignored) { }
            }
        }
        return false;
    }

    public boolean addBackCard(String despatchId,String storeId,String fileName){
       return model.addBackCard(despatchId,storeId,fileName);
    }

}
