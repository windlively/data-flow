package ink.andromeda.dataflow.demo;

import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface DemoDao {


    Map<String, Object> findBookInfoById(int id);

}
