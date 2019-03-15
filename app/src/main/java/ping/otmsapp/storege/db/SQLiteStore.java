package ping.otmsapp.storege.db;

import android.content.Context;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Leeping on 2018/6/28.
 * email: 793065165@qq.com
 * string-string (k-v) 数据存储
 */

public class SQLiteStore implements Closeable {
    private SQLiteStore() {
    }

    private static final class Holder{
        private final static SQLiteStore DB_STORE = new SQLiteStore();
    }

    public static SQLiteStore get(){
        return Holder.DB_STORE;
    }


    private void checkInit(){
        if (sql==null) throw new IllegalStateException("未初始化数据库对象");
    }

    private LocalSql sql;

    public void init(Context context){
        if (sql!=null) return;
        sql = new LocalSql();
        sql.init(context);
    }


    public LocalSql getSql() {
        checkInit();
        return sql;
    }

    @Override
    public void close() throws IOException {
        checkInit();
        sql.close();
    }

}
