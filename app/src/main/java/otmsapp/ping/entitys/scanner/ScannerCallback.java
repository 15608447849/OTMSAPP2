package otmsapp.ping.entitys.scanner;

public interface ScannerCallback {
    /**
     * 扫码结果回调
     */
    void onScanner(String codeBar);
}
