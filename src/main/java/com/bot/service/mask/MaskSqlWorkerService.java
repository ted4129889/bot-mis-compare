package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

@Service
public class MaskSqlWorkerService {

    @Autowired
    private DataSource dataSource;

    private static final String CHARSET = "BIG5";
    private static final int STATEMENT_BATCH_SIZE = 1000;

    @Async("executionExecutor")
    public void runSql(String filePath, Consumer<Boolean> onFinish) {
        long start = System.nanoTime();
        int count = 0;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            String sql = Files.readString(Paths.get(filePath), Charset.forName(CHARSET));
            count = executeSqlStatements(conn, sql, filePath);

            long duration = System.nanoTime() - start;
            LogProcess.info("執行完成, " + filePath + " 共新增 " + count + " 筆資料, 耗時: " + duration + "ns");

            onFinish.accept(true); // 成功

        } catch (SQLException | IOException e) {
            LogProcess.error("SQL 執行失敗: " + filePath);
            onFinish.accept(false); // 失敗
        }
    }

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