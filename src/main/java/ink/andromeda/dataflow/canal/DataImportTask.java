package ink.andromeda.dataflow.canal;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;


/**
 * 利用canal读取数据库binlog文件
 */
@Slf4j
// @Component
public class DataImportTask {

    @Resource
    private TaskExecutor taskExecutor;

    @Value("${canal.zkServers}")
    private String zkServers;
    @Value("${canal.destination}")
    private String destination;
    @Value("${canal.subscribe}")
    private String subscribe;

    @PostConstruct
    public void start() {
        taskExecutor.execute(this::run);
    }

    private void run() {
        log.info("data import start...zkServers->{},destination->{}", zkServers, destination);

        // 基于zookeeper动态获取canal server的地址，建立链接，其中一台server发生crash，可以支持failover
//                CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("alicanal.db-test01", 11111), destination, "", "");
        CanalConnector connector = CanalConnectors.newClusterConnector(zkServers, destination, "", "");

        int batchSize = 1000;
        try {
            connector.connect();
            //订阅关注的表
            connector.subscribe(subscribe);
            for (;;) {
                long batchId = -1;
                Message message = null;
                try {
                    //获取指定数量的数据 不设置阻塞时间
                    message = connector.getWithoutAck(batchSize);
                    batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        try {
                            log.info("no message, please wait... ");
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            log.error("canal sleep error,", e);
                        }
                    } else {
                        log.info("deal with message start, size->{}", size);
                        for (CanalEntry.Entry entry : message.getEntries()) {
                            log.info("found one entry: schemaName={}, tableName={}", entry.getHeader().getSchemaName(), entry.getHeader().getTableName());
                        }

                    }
                    //提交确认
                    connector.ack(batchId);
                } catch (Exception e) {
                    //失败后不回滚，记录下错误日志
                    log.error("canal deal with message error,batchId->{},message->{}", batchId, message, e);
                }
            }
        } catch (Exception e) {
            log.error("connect error", e);
        } finally {
            log.info("connector disconnect... ");
            connector.disconnect();
        }
    }
}
