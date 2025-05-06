package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import com.bot.util.xml.mask.allowedTable.AllowedDevTableName;
import com.bot.util.xml.mask.allowedTable.AllowedLocalTableName;
import com.bot.util.xml.mask.allowedTable.AllowedProdTableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class MaskDataBaseService {
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;
    @Value("${localFile.mis.batch.output}")
    private String outputFilePath;

    @Value("${localFile.mis.batch.output_original_data}")
    private String outputFileOriginalPath;

    @Autowired
    private MaskDataWorkerService maskDataWorkerService;
    private static final String CHARSET = "BIG5";
    private static final int BUFFER_CAPACITY = 5000;

    // fix SQL Injection
    private int index = 0;
    public int totalCnt = 0;
    public int tableCnt = 0;
    String param = "";

    public boolean exec(String env, String date) {
        LogProcess.info("執行資料庫資料遮蔽處理...");

        totalCnt = 0;
        tableCnt = 0;
        DataSource dataSource = new DriverManagerDataSource();
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                LogProcess.warn("資料庫連線失敗");
                return false;
            }

            switch (env) {
                case "local" ->
                        handleEnvTables(AllowedLocalTableName.values(), AllowedLocalTableName::getTableName, this::buildXmlName, env);
                case "dev" ->
                        handleEnvTables(AllowedDevTableName.values(), AllowedDevTableName::getTableName, this::buildXmlName, env);
                case "prod" ->
                        handleEnvTables(AllowedProdTableName.values(), AllowedProdTableName::getTableName, t -> t, env);
                default -> {
                    LogProcess.warn("不支援的環境參數: " + env);
                    return false;
                }
            }

        } catch (SQLException e) {
            LogProcess.error("連線錯誤");
            return false;
        }

        return true;
    }

    private <T extends Enum<T>> void handleEnvTables(
            T[] tables,
            Function<T, String> getTableNameFunc,
            Function<String, String> xmlNameBuilder,
            String env
    ) {
        if (tables == null || tables.length == 0) {
            LogProcess.warn("無任何可處理的 Table！");
            return;
        }

        CountDownLatch latch = new CountDownLatch(tables.length);  // 加上 Latch 控制

        for (T tableEnum : tables) {
            String tableName = getTableNameFunc.apply(tableEnum);
            String xmlFileName = xmlNameBuilder.apply(tableName);

            maskDataWorkerService.maskOneTable(
                    tableName,
                    xmlFileName,
                    env,
                    param,
                    success -> { // 每個任務完成回報
                        synchronized (this) {
                            tableCnt++;
                            if (success) {
                                totalCnt++;
                            }
                        }
                        latch.countDown();
                    }
            );


        }

        try {
            boolean completed = latch.await(1, TimeUnit.HOURS); // 最多等一小時
            if (!completed) {
                LogProcess.warn("部分遮蔽任務超時未完成！");
            } else {
                LogProcess.info("所有遮蔽任務完成！");
            }
        } catch (InterruptedException e) {
            LogProcess.error("等待遮蔽任務時被中斷！");
            Thread.currentThread().interrupt();
        }

        LogProcess.info("總計應有 " + tableCnt + " 個允許 SQL 資料表");
        LogProcess.info("有 " + totalCnt + " 個 SQL 檔案成功產生。");
    }

    private String buildXmlName(String tableName) {
        String[] parts = tableName.split("\\.");
        if (parts.length < 2) return tableName;
        return parts[parts.length - 2] + "." + "dbo" + "." + parts[parts.length - 1];
    }
}
