package ping.otmsapp.mvp.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import cn.hy.otms.rpcproxy.comm.cstruct.BoolMessage;
import cn.hy.otms.rpcproxy.dts.FileUploadRequest;
import cn.hy.otms.rpcproxy.dts.FileUploadRespond;
import cn.hy.otms.rpcproxy.dts.IDataTransferServicePrx;
import cn.hy.otms.rpcproxy.dts.TransferSequence;
import ping.otmsapp.entitys.IO;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.contract.CostContract;
import ping.otmsapp.tools.StrUtil;
import ping.otmsapp.zerocice.IceServerAbs;

public class FileUploadModel extends IceServerAbs<IDataTransferServicePrx> implements CostContract.Model{

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

            FileUploadRequest request =new FileUploadRequest(serverFilePath,serverFileName,image.length());

            final FileUploadRespond result = getProxy().request(request);
            LLog.print(result.array.length);
            if(result ==null || result.array.length == 0) return false;

                final IDataTransferServicePrx prx = getProxy();
                raf = new RandomAccessFile(image,"r");
                byte[] data = null;

                long progress = 0;
                for(TransferSequence ts :result.array){
                    if (data == null) data = new byte[(int) ts.size];
                    raf.seek(ts.start);
                    raf.read(data,0,(int) ts.size);
                    prx.transfer(result.tag,ts,data);
                    progress+=ts.size;
                    final String str = StrUtil.format(serverFileName+" 大小: %d 进度 %d,百分比 %.2f",image.length(),progress,((double) progress / (double) image.length()));
                    LLog.print(str);
                }

                prx.complete(result.tag);
                final String strs = StrUtil.format("上传文件成功 %s, 服务器路径 %s",image.getAbsolutePath(),serverFilePath+serverFileName);
                LLog.print(strs);
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
