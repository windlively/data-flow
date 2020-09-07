package net.wecash.abakuscoresystem;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.CoreSystemEventApplication;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.service.HistoricalDataService;
import ink.andromeda.dataflow.service.sync.CoreTableSyncService;
import net.abakus.coresystem.redis.RedisClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(
        classes = CoreSystemEventApplication.class,
        properties = "spring.profiles.active=PRO",
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class EventApplicationTests {

    @Autowired
    HistoricalDataService historicalDataService;

    @Autowired
    CoreTableSyncService coreTableSyncService;

    @Test
    void contextLoads() {
    }

    @Test
    void lmLoanSyncServiceTest(){
        coreTableSyncService.coreTableSync(SourceEntity.builder()
                .data(new JSONObject()
                        .fluentPut("LOAN_NO", "123")
                        .fluentPut("ORIG_PRCP", "1000.00")
                        .fluentPut("LOAN_TNR", 9)
                        .fluentPut("LOAN_TYP", "abc")
                        .fluentPut("LOAN_PAYM_TYP", "1")
                        .fluentPut("THD_CNT", 10)
                )
                .opType("INSERT")
                .schema("abak")
                .table("lm_loan")
                .build(),true);
        while (true);
    }

    @Test
    void lmPmShdSyncServiceTest(){
        historicalDataService.coreTableHistoricalDataSync(SourceEntity.builder()
                .data(new JSONObject()
                        .fluentPut("LOAN_NO", "111222")
                        .fluentPut("PS_INSTM_AMT", "1000.00")
                        .fluentPut("INT_STATE", 9)
                        .fluentPut("LOAN_TYP", "abc")
                        .fluentPut("LOAN_PAYM_TYP", "1")
                        .fluentPut("THD_CNT", 10)
                )
                .opType("INSERT")
                .schema("abakus")
                .table("lm_pm_shd")
                .build(), "0000000", "No123456", "1", true);
    }

    @Autowired
    RedisClient redisClient;

    @Test
    void getSyncDetail(){
        long length = redisClient.llen("cs-datachannel:batch-sync:937afed915444e609f022f5faa39f368");
        int i = 0;
        int step = 200;
        while (i < length){
            List<String> list = redisClient.lrange("cs-datachannel:batch-sync:937afed915444e609f022f5faa39f368", i, i + step - 1);
            for (String m : list) {
                if(m.contains("Empty"))
                    log.error(m);
            }
            i += step;
        }
    }

}
