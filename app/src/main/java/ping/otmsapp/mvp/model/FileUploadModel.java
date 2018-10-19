package ping.otmsapp.mvp.model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import cn.hy.otms.rpcproxy.appInterface.SureFeeInfo;
import cn.hy.otms.rpcproxy.dts.FileUploadRequest;
import cn.hy.otms.rpcproxy.dts.FileUploadRespond;
import cn.hy.otms.rpcproxy.dts.IDataTransferService;
import cn.hy.otms.rpcproxy.dts.IDataTransferServicePrx;
import cn.hy.otms.rpcproxy.dts.TransferSequence;
import ping.otmsapp.log.LLog;
import ping.otmsapp.mvp.contract.CostContract;
import ping.otmsapp.storege.obs.IDataObjectAbs;
import ping.otmsapp.tools.StrUtil;
import ping.otmsapp.zerocice.IceServerAbs;

public class FileUploadModel extends IceServerAbs<IDataTransferServicePrx> implements CostContract.Model{

    private CostContract.Model model = new AppInterfaceModel();

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
        RandomAccessFile raf = null;
        try{
            FileUploadRequest request =new FileUploadRequest(serverFilePath,serverFileName,image.length());
            FileUploadRespond result = getProxy().request(request);
            if(result ==null || result.array.length == 0) return false;

                IDataTransferServicePrx prx = getProxy();
                raf = new RandomAccessFile(image,"r");
                byte[] data = null;
                long progress = 0;
                for(TransferSequence ts :result.array){
                    if(data == null) data = new byte[(int) ts.size];
                    raf.seek(ts.start);
                    raf.read(data,0,(int) ts.size);
                    prx.transfer(result.tag, ts, data);
                    progress+=ts.size;
                    LLog.print(StrUtil.format("已上传:%d ,文件进度:%.2f",progress,((double) progress / (double) image.length())));
                }
                prx.complete(result.tag);
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
}
