package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import com.bot.util.path.PathValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MaskRunSqlService {
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${localFile.mis.batch.output}")
    private String allowedPath;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PathValidator pathValidator;
    private int count = 0;
    private int reservedForSystem = 4;
    private int threadPoolSize = 1;
    private int totalCnt = 0;
    private static final String CHARSET = "BIG5";
    private static final int STATEMENT_BATCH_SIZE = 1000;

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
        // 取得邏輯核心數（包含超執行緒）
        int cores = Runtime.getRuntime().availableProcessors();

        // 保留 5 條線程給系統用
        reservedForSystem = 5;

        int calculated = cores - reservedForSystem;

        // 動態計算 ThreadPool 大小
        threadPoolSize = Math.max(2, Math.min(calculated, 8));  // 至少 2 條線程

        long start = System.nanoTime();

        int totalCnt = 0;
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);  // 線程數可動態算

        List<String> sqlPaths = getSafeSQLFilePaths(allowedPath);

        for (String path : sqlPaths) {

            executor.submit(() -> {
                long duration = 0L;
                try (Connection conn = dataSource.getConnection()) {
                    conn.setAutoCommit(false);

                    String sql = readSqlFromFile(path);
                    int count = executeSqlStatements(conn, sql, path);

                    duration = duration + (System.nanoTime() - start);

                    LogProcess.info("執行完成, " + path + "共新增 " + count + " 筆資料,耗時: " + duration + "ns");
                } catch (SQLException e) {
                    LogProcess.error("SQL 執行失敗: " + path, e);
                }
            });

            totalCnt++;
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            LogProcess.error("任務中斷", e);
            Thread.currentThread().interrupt();
        }

        LogProcess.info("共成功執行 " + totalCnt + " 個 SQL 檔案。\n");
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
            LogProcess.warn("SQL檔案路徑尚未產生(batch-file/output)");
            return List.of();
        }
    }

    private String readSqlFromFile(String filePath) {
        try {
            // 指定用 BIG5 解碼
            return Files.readString(Paths.get(filePath), Charset.forName(CHARSET));
        } catch (IOException e) {
            LogProcess.warn("讀取 SQL 檔失敗：" + filePath, e);
            return "";
        }
    }

    /**
     * 將多段 SQL 語句（以 INSERT INTO 為分隔）逐一執行
     *
     * @return 執行成功的語句數
     */
    private int executeSqlStatements(Connection conn, String sql, String filePath) {
        if (sql == null || sql.isBlank()) {
            LogProcess.warn("空 SQL 檔案，跳過：" + filePath);
            return 0;
        }
        String[] segments = sql.split("(?i)(?=(?:DELETE\\s+FROM|INSERT\\s+INTO))");
        int count = 0;
        int batchCount = 0;
        try (Statement stmt = conn.createStatement()) {
            for (String segment : segments) {
                String stmtSql = segment.trim();
                if (stmtSql.isEmpty()) continue;

                if (!stmtSql.matches("(?i)^(insert|delete)\\s+.*")) {
                    LogProcess.warn("疑似非法 SQL 被過濾：" + stmtSql);
                    return 0;
                }
                // fortify-disable-next-line SQLInjection
                stmt.addBatch(stmtSql);

                batchCount++;

                if (batchCount >= STATEMENT_BATCH_SIZE) {
                    int[] results = stmt.executeBatch();
                    conn.commit();
                    count += results.length;
                    batchCount = 0;
                }
            }
            // 處理剩餘的
            if (batchCount > 0) {
                int[] results = stmt.executeBatch();
                conn.commit();
                count += results.length;
            }

        } catch (SQLException e) {

            LogProcess.error("executeSqlStatements 執行失敗: " + filePath);
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                LogProcess.error("Rollback 失敗");
            }
        }
        return count;
    }
}