package examples.mybatis;

import dynamicds.DynamicDataSource;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends DynamicDataSource<UserMapper> {
    @Select("SELECT * FROM \"user\"")
    List<User> findAllUsers();

    @Insert("INSERT INTO \"user\" (id, name) VALUES (#{id}, #{name})")
    void insertUser(User user);
}
