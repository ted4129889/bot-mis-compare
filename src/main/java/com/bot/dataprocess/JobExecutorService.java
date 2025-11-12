package com.bot.dataprocess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class JobExecutorService {

    private final Map<String, Job> jobMap;

    public JobExecutorService(Map<String, Job> jobMap) {
        this.jobMap = jobMap;
    }

    /**
     * 依照名稱執行對應 Job
     *
     * @param jobName 外部傳入的名稱，如 "APPLE"
     */
    public void runJob(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new IllegalArgumentException("jobName 不可為空");
        }

        // 統一轉大寫以對應 @Component("XXXXX")
        String key = jobName.toUpperCase(Locale.ROOT);

        Job job = jobMap.get(key);

        if (job == null) {
            log.error("找不到對應的 Job，jobName = {}", key);
            throw new IllegalArgumentException("無對應 Job: " + key);
        }

        log.info("開始執行 Job: {}", key);
        job.execute();
        log.info("完成執行 Job: {}", key);
    }
}
