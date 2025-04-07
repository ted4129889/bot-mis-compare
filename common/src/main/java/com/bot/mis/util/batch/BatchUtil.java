/* (C) 2024 */
package com.bot.mis.util.batch;

import com.bot.mis.util.batchVo.BatchLabel;
import com.bot.txcontrol.jpa.transaction.TransactionCase;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class BatchUtil {

    private static final long TIME_THRESHOLD_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private BatchUtil() {
        // YOU SHOULD USE @Autowired ,NOT new BatchUtil()
    }

    public Date refreshBatchTransaction(TransactionCase batchTransaction, Date startTime) {
        Date nowTime = new Date();
        long diffTime = nowTime.getTime() - startTime.getTime();
        if (diffTime >= TIME_THRESHOLD_MILLIS) {
            batchTransaction.commit();
            startTime = nowTime;
        }
        return startTime;
    }

    public BatchLabel getBatchLabel(Map<String, String> map) {
        BatchLabel batchLabel = new BatchLabel();
        batchLabel.setInputDate(map.get("BBSDY"));
        return batchLabel;
    }
}
