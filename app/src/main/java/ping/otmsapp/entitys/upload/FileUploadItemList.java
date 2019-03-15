package ping.otmsapp.entitys.upload;

import java.util.ArrayList;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

public class FileUploadItemList extends JsonLocalSqlStorage {
    public ArrayList<FileUploadItem> list = new ArrayList<>();
}
