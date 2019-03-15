package ping.otmsapp.entitys.upload;

import java.util.HashMap;

/**
 * Created by Leeping on 2019/2/22.
 * email: 793065165@qq.com
 */
public class FileUploadItem {
    public String localFullPath;
    public String serverPath;
    public String serverName;
    public HashMap<String,String> param = new HashMap<>();
    public boolean isDel = true;
    public int type = 0;

    public FileUploadItem(int type) {
        this.type = type;
    }
}
