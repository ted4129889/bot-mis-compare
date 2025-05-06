package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import com.bot.util.path.PathValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MaskRunSqlService {
    @Value("${localFile.mis.batch.output}")
    private String allowedPath;

    @Autowired
    private PathValidator pathValidator;
    @Autowired
    private MaskSqlWorkerService sqlWorker;

    public boolean exec(String env) {
        if ("prod".equals(env)) {
            return false;
        } else {
            executeAllSqlFiles();
            return true;
        }
    }


    private void executeAllSqlFiles() {
        LogProcess.info("執行 SQL 檔案語法...");

        allowedPath = FilenameUtils.normalize(allowedPath);

        List<String> sqlPaths = getSafeSQLFilePaths(allowedPath);
        if (sqlPaths.isEmpty()) {
            LogProcess.warn("找不到任何 SQL 檔案可執行。");
            return;
        }

        CountDownLatch latch = new CountDownLatch(sqlPaths.size());
        final int[] successCount = {0};
        List<String> failList = Collections.synchronizedList(new ArrayList<>());

        for (String path : sqlPaths) {
            sqlWorker.runSql(path, success -> {
                synchronized (this) {
                    if (success) {
                        successCount[0]++;
                    } else {
                        failList.add(path);
                    }
                }
                latch.countDown();
            });
        }

        try {
            boolean completed = latch.await(1, TimeUnit.HOURS);  // 最多等一小時
            if (!completed) {
                LogProcess.warn("部分 SQL 檔案執行逾時！");
            }
        } catch (InterruptedException e) {
            LogProcess.error("等待 SQL 任務時被中斷！");
            Thread.currentThread().interrupt();
        }

        LogProcess.info("總共派發 " + sqlPaths.size() + " 個 SQL 檔案任務。");
        LogProcess.info("成功執行 " + successCount[0] + " 個 SQL 檔案。");

        if (!failList.isEmpty()) {
            LogProcess.warn("以下 SQL 檔案執行失敗：");
            for (String failPath : failList) {
                LogProcess.warn(" - " + failPath);
            }
        } else {
            LogProcess.info("所有 SQL 檔案皆成功執行！");
        }
    }

    public List<String> getSafeSQLFilePaths(String rootPath) {
        String normalizedBase = FilenameUtils.normalize(rootPath);
        try (Stream<Path> paths = Files.walk(Paths.get(normalizedBase), 1)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(p -> pathValidator.isSafe(normalizedBase, FilenameUtils.normalize(p)))
                    .filter(p -> p.toLowerCase().endsWith(".sql"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LogProcess.warn("SQL 檔案路徑尚未產生 (batch-file/output)");
            return List.of();
        }
    }

//
//    /**
//     * 將多段 SQL 語句（以 INSERT INTO 為分隔）逐一執行
//     *
//     * @return 執行成功的語句數
//     */
//    private int executeSqlStatements(Connection conn, String sql, String filePath) {
//        if (sql == null || sql.isBlank()) {
//            LogProcess.warn("空 SQL 檔案，跳過：" + filePath);
//            return 0;
//        }
//        String[] segments = sql.split("(?i)(?=(?:DELETE\\s+FROM|INSERT\\s+INTO))");
//        int count = 0;
//        int batchCount = 0;
//        try (Statement stmt = conn.createStatement()) {
//            for (String segment : segments) {
//                String stmtSql = segment.trim();
//                if (stmtSql.isEmpty()) continue;
//
//                stmt.addBatch(stmtSql);
//                batchCount++;
//
//                if (batchCount >= STATEMENT_BATCH_SIZE) {
//                    int[] results = stmt.executeBatch();
//                    conn.commit();
//                    count += results.length;
//                    batchCount = 0;
//                }
//            }
//            // 處理剩餘的
//            if (batchCount > 0) {
//                int[] results = stmt.executeBatch();
//                conn.commit();
//                count += results.length;
//            }
//
//        } catch (SQLException e) {
//
//            LogProcess.error("executeSqlStatements 執行失敗: " + filePath);
//            try {
//                conn.rollback();
//            } catch (SQLException rollbackEx) {
//                LogProcess.error("Rollback 失敗");
//            }
//        }
//        return count;
//    }
}