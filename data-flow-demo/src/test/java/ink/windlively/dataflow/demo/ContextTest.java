package ink.windlively.dataflow.demo;

import ink.windlively.dataflow.core.DataRouter;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.SpringELExpressionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(properties = "spring.profiles.active=FAT")
@Slf4j
public class ContextTest {

    @Autowired
    SpringELExpressionService expressionService;

    @Test
    public void spElTest(){
        Map<String, Object> map = new HashMap<>();
        map.put("a", 222);
        Map<String, Object> m2 = new HashMap<>();
        m2.put("t1", new HashMap<>());
        map.put("b", m2);
        StandardEvaluationContext context = expressionService.evaluationContext();
        context.setRootObject(map);
        Object o = expressionService.executeExpression("[aa][o]");
        log.info("result: {}",o);
    }

    @Autowired
    DataRouter router;

    @Test
    public void flowTest() throws Exception {
        Map<String, Object> data = new HashMap<>();
        router.routeAndProcess(SourceEntity.builder()
                .opType("UPDATE")
                .before(new HashMap<>(0))
                .data(data)
                .schema("demo_schema")
                .name("demo_table")
                .source("demo_source")
                .build());
    }
}
