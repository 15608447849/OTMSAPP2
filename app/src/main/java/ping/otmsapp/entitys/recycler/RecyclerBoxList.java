package ping.otmsapp.entitys.recycler;

import java.util.ArrayList;

import ping.otmsapp.entitys.JsonLocalSqlStorage;
import ping.otmsapp.entitys.except.Abnormal;

public class RecyclerBoxList extends JsonLocalSqlStorage {
    public ArrayList<RecyclerBox> list = new ArrayList<>();
}
