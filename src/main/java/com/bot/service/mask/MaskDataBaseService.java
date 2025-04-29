package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.allowedTable.AllowedDevTableName;
import com.bot.util.xml.mask.allowedTable.AllowedLocalTableName;
import com.bot.util.xml.mask.allowedTable.AllowedProdTableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
public class MaskDataBaseService {
    @Value("${spring.profiles.active}")
    private String nowEnv;
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;
    @Value("${localFile.mis.batch.output}")
    private String outputFilePath;

    @Value("${localFile.mis.batch.output_original_data}")
    private String outputFileOriginalPath;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int dbMaxPoolSize;

    @Autowired
    private TextFileUtil textFileUtil;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataMasker dataMasker;
    @Autowired
    private XmlParser xmlParser;

    @Autowired
    private MaskExportService maskExportService;
    private static final String CHARSET = "BIG5";
    private static final int BUFFER_CAPACITY = 5000;
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_LENGTH = "length";
    private final String SQL_EXTENSION = ".sql";
    private final String STR_SEMICOLON = ";";
    private final String STR_DOT = " ,";

    // fix SQL Injection
    private final String SQL_SELECT_TABLE = "select * from ";
    private final String SQL_DELETE_TABLE = "DELETE FROM ";
    private final String STR_POINT = ".";
    private final String STR_DBO = "dbo";
    private final String XML_EXTENSION = ".xml";
    private int index = 0;
    public int totalCnt = 0;
    public int tableCnt = 0;
    private String allEnv = "";
    private int reservedForSystem = 4;
    private int threadPoolSize = 1;
    String param = "";

    public boolean exec(String env, String date) {
        LogProcess.info("執行資料庫資料遮蔽處理...");

        // 取得邏輯核心數（包含超執行緒）
        int cores = Runtime.getRuntime().availableProcessors();

        // 保留 4 條線程給系統用
        reservedForSystem = 5;

        int calculated = cores - reservedForSystem;

        // 動態計算 ThreadPool 大小
        threadPoolSize = Math.max(2, Math.min(calculated, 8));  // 至少 1 條線程

        LogProcess.info("dbMaxPoolSize = " + dbMaxPoolSize);

        LogProcess.info("threadPoolSize = " + threadPoolSize);

        totalCnt = 0;
        tableCnt = 0;
        param = date;
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
            LogProcess.error("連線錯誤", e);
            return false;
        }

        LogProcess.info("總計應有 " + tableCnt + " 個允許 SQL 資料表");
        LogProcess.info("有 " + totalCnt + " 個SQL檔案產生");

        return true;
    }

    private <T extends Enum<T>> void handleEnvTables(
            T[] tables,
            Function<T, String> getTableNameFunc,
            Function<String, String> xmlNameBuilder,
            String env
    ) {


        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize); // 控制同時處理的數量

        Arrays.stream(tables).forEach(tableEnum -> executor.submit(() -> {
            try (Connection conn = dataSource.getConnection()) { // 每個任務自己開連線
                String tableName = getTableNameFunc.apply(tableEnum);
                String xmlFileName = xmlNameBuilder.apply(tableName);

                boolean success = maskExportService.exportMaskedFile(conn, xmlFileName, tableName, env, param);
                synchronized (this) {
                    tableCnt++;
                    if (success) totalCnt++;
                }
            } catch (SQLException e) {
                LogProcess.error("資料庫連線失敗: " + tableEnum.name(), e);
            }
        }));

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            LogProcess.error("遮蔽任務中斷", e);
            Thread.currentThread().interrupt();
        }
    }

    private String buildXmlName(String tableName) {
        String[] parts = tableName.split("\\.");
        if (parts.length < 2) return tableName;
        return parts[parts.length - 2] + STR_POINT + STR_DBO + STR_POINT + parts[parts.length - 1];
    }

}
