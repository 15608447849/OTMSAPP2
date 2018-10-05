package otmsapp.ping.entitys.recycler;

import java.util.ArrayList;

import otmsapp.ping.entitys.JsonLocalSqlStorage;
import otmsapp.ping.entitys.except.Abnormal;

public class RecyclerBoxList extends JsonLocalSqlStorage {
    public ArrayList<RecyclerBox> list = new ArrayList<>();
}
