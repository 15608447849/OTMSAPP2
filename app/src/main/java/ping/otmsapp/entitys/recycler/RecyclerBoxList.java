package ping.otmsapp.entitys.recycler;

import java.util.ArrayList;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

public class RecyclerBoxList extends JsonLocalSqlStorage {
    public ArrayList<RecyclerBox> list = new ArrayList<>();
    public ArrayList<RecyclerCarton> list2 = new ArrayList<>();
}
