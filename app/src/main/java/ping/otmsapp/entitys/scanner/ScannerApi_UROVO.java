package ping.otmsapp.entitys.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;

public class ScannerApi_UROVO extends ScannerApiThread {
    private Context context;
    private ScanManager mScanManager;
    public ScannerApi_UROVO(Context context) {
        super(context);
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            String barcodeStr = new String(barcode, 0, barcodelen);

            try{
                if (isEnable){
                    queue.add(barcodeStr);
                    mScanManager.stopDecode();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        }

    };

    @Override
    void init(Context context) {
        this.context = context;
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode( 0);
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(ScanManager.ACTION_DECODE);
        }
        context.registerReceiver(mScanReceiver,filter);
    }

    @Override
    public void stopScan() {
        super.stopScan();
        if (context!=null){
            context.unregisterReceiver(mScanReceiver);
            context = null;
        }
    }
}
