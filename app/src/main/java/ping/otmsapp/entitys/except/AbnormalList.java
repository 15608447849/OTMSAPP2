package ping.otmsapp.entitys.except;

import java.util.ArrayList;
import java.util.List;

import ping.otmsapp.entitys.JsonLocalSqlStorage;

public class AbnormalList extends JsonLocalSqlStorage {
    public ArrayList<Abnormal> list = new ArrayList<>();
}
