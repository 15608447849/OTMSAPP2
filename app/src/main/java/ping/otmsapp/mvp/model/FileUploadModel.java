package ping.otmsapp.mvp.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import cn.hy.otms.rpcproxy.dts.FileUploadInfo;
import cn.hy.otms.rpcproxy.dts.IFileUploadServicePrx;
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
    public boolean uploadImage(File image, String serverFilePath, String serverFileName) {

        if (image.length()==0) return true;
        printParam("上传文件",image,serverFileName,serverFileName);
        RandomAccessFile raf = null;
        try{
            final IFileUploadServicePrx prx = getProxy();
            FileUploadInfo info =new FileUploadInfo(serverFilePath,serverFileName,image.length());

            String tag  = getProxy().request(info);
            if(tag ==null || tag.length() == 0) return false;
            if(tag.equals("wait")){
                Thread.sleep(2000);
                return uploadImage(image,serverFilePath,serverFileName);
            }
            raf = new RandomAccessFile(image,"r");
            byte[] bytes = new byte[1024 * 2];
            long pos  = 0L;
            long len = image.length();
            int size = 0;

            while (pos < len){

                if (len - pos >= bytes.length) {
                    size = bytes.length;
                }else{
                    size = (int)(len - pos);
                }
                //移动到起点
                raf.seek(pos);

                //读取数据
                raf.read(bytes,0,size);

                //写入数据
                prx.transfer(tag,pos,bytes);

                //起点下移
                pos+=size;
            }

            raf.close();
            prx.complete(tag);

            return true;
        }catch (Exception e){
            e.printStackTrace();
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
