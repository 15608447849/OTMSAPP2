package otmsapp.ping.entitys.scanner;

import android.content.Context;

import com.seuic.scanner.DecodeInfo;
import com.seuic.scanner.DecodeInfoCallBack;
import com.seuic.scanner.ScannerFactory;
import com.seuic.scanner.ScannerKey;

import otmsapp.ping.log.LLog;
import otmsapp.ping.tools.AppUtil;

/**
 * 设备密码4007770876
 */
public class ScannerApi_SEUIC extends ScannerApiThread implements DecodeInfoCallBack {

    public ScannerApi_SEUIC(Context context) {
        super(context);
    }

    private com.seuic.scanner.Scanner scanner;

    private Thread watch;

    @Override
    void init(Context context) {
        scanner = ScannerFactory.getScanner(context);
        scanner.setDecodeInfoCallBack(this);

        watch = new Thread(){
            @Override
            public void run() {
                runScanner();
            }
        };

        watch.setDaemon(true);
        watch.start();
    }

    private void runScanner() {
        scanner.enable();
        scanner.open();
        int ret = ScannerKey.open();
        if (ret > -1) {
            executeScanner();
        }
        scanner.setDecodeInfoCallBack(null);
        ScannerKey.close();
    }

    @Override
    public void stopScan() {
        super.stopScan();
        watch = null;
    }

    private void executeScanner() {
        int key;
        while (isRun){
            try {
                key = ScannerKey.getKeyEvent();
                if (key > -1 && isEnable) {
                    if (key ==  ScannerKey.KEY_DOWN){
                        scanner.startScan();
                    }else if (key ==  ScannerKey.KEY_UP){
                        scanner.stopScan();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDecodeComplete(DecodeInfo decodeInfo) {
        queue.offer(decodeInfo.barcode);
    }
}
