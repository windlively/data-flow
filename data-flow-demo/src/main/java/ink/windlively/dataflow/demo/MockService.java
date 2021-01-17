package ink.windlively.dataflow.demo;

import ink.windlively.dataflow.core.DataRouter;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.datasource.SwitchSource;
import ink.windlively.dataflow.datasource.dao.CommonJdbcDao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class MockService {

    private Random random = new Random();

    private long id;

    private final CommonJdbcDao commonJdbcDao;

    private final DataRouter dataRouter;

    public MockService(CommonJdbcDao commonJdbcDao,
                       DataRouter dataRouter) {
        this.commonJdbcDao = commonJdbcDao;
        this.dataRouter = dataRouter;
    }

    @SwitchSource(name = "master")
    @Scheduled(fixedRate = 2000)
    public SourceEntity produce(){
        int customerId = random.nextInt(12) + 1;
        int bookId = random.nextInt(20) + 1;
        int count = random.nextInt(5) + 1;
        BigDecimal discount = new BigDecimal(new DecimalFormat("0.00").format(random.nextDouble() * 10));

        Map<String, Object> bookInfo = commonJdbcDao.selectOne("SELECT * FROM business_db.book_info WHERE id=" + bookId);
        BigDecimal price = (BigDecimal) Objects.requireNonNull(bookInfo).get("price");

        Map<String, Object> data = new HashMap<>();
        if(id > 20) id = 0;
        data.put("id", id ++);
        data.put("customer_id", customerId);
        data.put("pay_time", new Date());
        data.put("create_time", new Date());
        data.put("book_id", bookId);
        data.put("amount", price.multiply(BigDecimal.valueOf(count)).subtract(discount));
        data.put("count", count);

        SourceEntity sourceEntity = SourceEntity.builder()
                .source("demo")
                .schema("business_db")
                .name("order_summary")
                .data(data)
                .before(Collections.emptyMap())
                .build();
        try {
            dataRouter.routeAndProcess(sourceEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sourceEntity;
    }

}
