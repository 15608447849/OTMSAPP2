package otmsapp.ping.entitys.except;

import java.util.ArrayList;
import java.util.List;

import otmsapp.ping.entitys.JsonLocalSqlStorage;

public class AbnormalList extends JsonLocalSqlStorage {
    public ArrayList<Abnormal> list = new ArrayList<>();
}
