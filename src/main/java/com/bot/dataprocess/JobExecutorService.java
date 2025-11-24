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
     * @param ctx
     */
    public JobResult runJob(String jobName, JobContext ctx) {
//        String key = jobName == null ? "" : jobName.trim().toUpperCase(Locale.ROOT);
        String key = jobName == null ? "" : jobName.trim();
        Job job = jobMap.get(key);
        if (job == null) {
            throw new IllegalArgumentException("無對應 Job: " + key);
        }

        log.info("開始執行 Job: {}", key);
        try {
            JobResult result = job.execute(ctx);
            log.info("完成執行 Job: {}，共產生 {} 個檔案", key, result.getOutputFilesMap().size());

            return result;
        } catch (Exception e) {
            log.error("執行 Job {} 失敗", key, e);
            JobResult result = new JobResult();
            result.setSuccess(false);
            result.setMessage("執行失敗：" + e.getMessage());
            return result;
        }
    }
}
