package ping.otmsapp.entitys;

public class UserInfo extends JsonLocalSqlStorage{
    /**
     * 用户码
     * */
    public int id;
    /**
     * 姓名
     */
    public String name;
    /**
     * 公司名
     */
    public String compName;
    /**
     * 角色名
     */
    public String roleName;
    /**
     * 角色码
     */
    public long roleCode;

}
