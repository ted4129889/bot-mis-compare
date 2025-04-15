package com.bot.mask;


import com.bot.filter.CheakSafePathUtil;
import com.bot.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.allowedTable.AllowedDevTableName;
import com.bot.util.xml.mask.allowedTable.AllowedLocalTableName;
import com.bot.util.xml.mask.allowedTable.AllowedProdTableName;
import com.bot.util.xml.mask.xmltag.Field;
import com.bot.util.xml.vo.XmlData;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.util.*;
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
    private int totalCnt = 0;
    private int tableCnt = 0;
    private String allEnv = "";

    public boolean exec(String env) {
        LogProcess.info("執行資料庫資料遮蔽處理...");
        totalCnt = 0;
        tableCnt = 0;

        try (Connection connection = dataSource.getConnection()) {
            if (connection == null) {
                LogProcess.warn("資料庫連線失敗");
                return false;
            }

            switch (env) {
                case "local" ->
                        handleEnvTables(connection, AllowedLocalTableName.values(), AllowedLocalTableName::getTableName, this::buildXmlName, env);
                case "dev" ->
                        handleEnvTables(connection, AllowedDevTableName.values(), AllowedDevTableName::getTableName, this::buildXmlName, env);
                case "prod" ->
                        handleEnvTables(connection, AllowedProdTableName.values(), AllowedProdTableName::getTableName, t -> t, env);
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
        LogProcess.info("實際處理成功 " + totalCnt + " 個遮蔽檔案");
        return true;
    }

    private <T extends Enum<T>> void handleEnvTables(
            Connection conn,
            T[] tables,
            Function<T, String> getTableNameFunc,
            Function<String, String> xmlNameBuilder,
            String env
    ) {
        Arrays.stream(tables).forEach(tableEnum -> {

            String tableName = getTableNameFunc.apply(tableEnum);
            String xmlFileName = xmlNameBuilder.apply(tableName);

            boolean success = maskExportService.exportMaskedFile(conn, xmlFileName, tableName, env);
            tableCnt++;
            if (success) totalCnt++;
        });
    }

    private String buildXmlName(String tableName) {
        String[] parts = tableName.split("\\.");
        if (parts.length < 2) return tableName;
        return parts[parts.length - 2] + STR_POINT + STR_DBO + STR_POINT + parts[parts.length - 1];
    }

//    public boolean exec(String env) {
//        LogProcess.info("執行資料庫資料遮蔽處理...");
//        totalCnt = 0;
//        tableCnt = 0;
//        try (Connection connection = dataSource.getConnection()) {
//            //確認資料庫連線
//            if (connection != null) {
//
//                allEnv = env;
//                switch (env) {
//                    case "local":
//                        for (AllowedLocalTableName name : AllowedLocalTableName.values()) {
//                            String[] xmlFileSplit = name.getTableName().split("\\.");
//                            String tmpXmlFileName =
//                                    xmlFileSplit[xmlFileSplit.length - 2]
//                                            + STR_POINT
//                                            + STR_DBO
//                                            + STR_POINT
//                                            + xmlFileSplit[xmlFileSplit.length - 1];
//                            validFileAndExportFile(connection, tmpXmlFileName, name.getTableName());
//
//                        }
//                        break;
//                    case "dev":
//                        for (AllowedDevTableName name : AllowedDevTableName.values()) {
//                            String[] xmlFileSplit = name.getTableName().split("\\.");
//                            String tmpXmlFileName =
//                                    xmlFileSplit[xmlFileSplit.length - 2]
//                                            + STR_POINT
//                                            + STR_DBO
//                                            + STR_POINT
//                                            + xmlFileSplit[xmlFileSplit.length - 1];
//                            validFileAndExportFile(connection, tmpXmlFileName, name.getTableName());
//
//                        }
//                        break;
//
//                    case "prod":
//                        for (AllowedProdTableName name : AllowedProdTableName.values()) {
//
//                            validFileAndExportFile(connection, name.getTableName(), name.getTableName());
//
//                        }
//                        break;
//                    default:
//                        return false;
//                }
//            } else {
//                LogProcess.info("Database connection error");
//            }
//        } catch (SQLException e) {
//            LogProcess.info("Database connection error");
//        }
//        LogProcess.info("總計應有" + tableCnt+ "個允許路徑下的SQL檔案");
//        LogProcess.info("實際有" + totalCnt+ " 個SQL檔案產生");
//
//        return true;
//
//    }
//
//    private void validFileAndExportFile(Connection connection, String xmlTableName, String tableName) {
//        ;
//        String allowedPath = FilenameUtils.normalize(maskXmlFilePath);
//        // 組合後的檔案名稱
//        String xml = FilenameUtils.normalize(maskXmlFilePath + xmlTableName + XML_EXTENSION);
//
//        if (CheakSafePathUtil.isSafeFilePath(allowedPath, xml)) {
//            File file = new File(xml);
//            // 沒有檔案時 略過
//            if (!file.exists()) {
//                LogProcess.info("not find file = " + xml);
//            } else {
//                sqlConvInsertTxt(connection, xml, tableName);
//            }
//        } else {
//            LogProcess.info("檔案路徑不安全，無法允許");
//        }
//
//    }
//
//
//    public void sqlConvInsertTxt(Connection connection, String xmlFileName, String tableName) {
//
//        try {
//            //允許的檔案路徑
//            String allowedPath = FilenameUtils.normalize(maskXmlFilePath);
//            xmlFileName = FilenameUtils.normalize(xmlFileName);
//            //再次確認路徑(白名單)
//            if (CheakSafePathUtil.isSafeFilePath(allowedPath, xmlFileName)) {
//                // parse Xml
//                XmlData xmlData = xmlParser.parseXmlFile(xmlFileName);
//                tableCnt++;
//                if ("prod".equals(allEnv)) {
//                    tableName = xmlData.getTable().getTableName();
//                }
//                // get SQL data
//                List<Map<String, Object>> sqlData = getSqlData(connection, SQL_SELECT_TABLE + tableName);
//
//                if (sqlData.size() == 0) {
//                    LogProcess.info("tableName=" + tableName + ",資料庫無資料，不產生SQL檔案");
//                } else {
//
//                    // <field>
//                    List<Field> fields = xmlData.getFieldList();
//
//                    //產出未遮蔽資料
//                    dataMasker.maskData(sqlData, fields, false);
//                    writeFile(generateSQL(tableName, sqlData), outputFileOriginalPath + tableName + SQL_EXTENSION);
//
//                    //產出遮蔽資料
//                    dataMasker.maskData(sqlData, fields, true);
//                    writeFile(generateSQL(tableName, sqlData), outputFilePath + tableName + SQL_EXTENSION);
//                    totalCnt++;
//                    LogProcess.info("tableName=" + tableName + ",產生SQL檔案");
//                }
//
//            } else {
//                LogProcess.info("檔案路徑不安全，無法允許");
//            }
//
//        } catch (Exception e) {
//            LogProcess.info("XmlToInsertGenerator.sqlConvInsertTxt error");
//        }
//
//    }
//
//    private List<String> generateSQL(String tableName, List<Map<String, Object>> maskedSqlData) {
//
//        StringBuilder result;
//        List<String> fileContents = new ArrayList<>();
//
//        if (maskedSqlData.size() == 0) {
//            return new ArrayList<>();
//        }
//
//        String delContent = SQL_DELETE_TABLE + tableName + STR_SEMICOLON;
//
//        fileContents.add(delContent);
//
//        for (Map<String, Object> mask : maskedSqlData) {
//            StringBuilder columns = new StringBuilder();
//            StringBuilder values = new StringBuilder();
//            Object objValues;
//            result = new StringBuilder(BUFFER_CAPACITY);
//            for (Map.Entry<String, Object> entry : mask.entrySet()) {
//
//                columns.append(entry.getKey()).append(STR_DOT);
//                objValues = entry.getValue();
//                if (objValues instanceof Map) {
//                    Map<String, Object> valuesMap = (Map<String, Object>) objValues;
//                    values.append(formatValue(valuesMap.get(PARAM_VALUE))).append(" ,");
//                }
//            }
//            String tmp =
//                    String.format(
//                            "INSERT INTO %s (%s) VALUES (%s);",
//                            tableName,
//                            columns.substring(0, columns.length() - 1),
//                            values.substring(0, values.length() - 1));
//
//            result.append(tmp);
//            fileContents.add(result.toString());
//        }
//        return fileContents;
//    }
//
//    private String formatValue(Object val) {
//
//        if (val == null) {
//            return "NULL";
//        }
//        return val instanceof String ? "'" + val + "'" : val.toString();
//    }
//
//    /**
//     * 輸出檔案
//     *
//     * @param fileContents 資料串
//     * @param outFileName  輸出檔案名
//     */
//    private void writeFile(List<String> fileContents, String outFileName) {
//
//        if (fileContents != null) {
//            textFileUtil.deleteFile(outFileName);
//
//            try {
//                textFileUtil.writeFileContent(outFileName, fileContents, CHARSET);
//            } catch (Exception e) {
//                LogProcess.info("Error Message : Problem writing to file ");
//            }
//        } else {
//            LogProcess.info("檔案：" + outFileName + " 無資料，未產出。");
//        }
//    }
//
//    private List<Map<String, Object>> getSqlData(Connection connection, String sql) {
//
//        List<Map<String, Object>> result = new ArrayList<>();
//
//        //允許的SQL語法
//        if (allowedSqlFlag(sql)) {
//
//            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//
//                ResultSet rs = pstmt.executeQuery();
//                ResultSetMetaData metaData = rs.getMetaData();
//                int columnCount = metaData.getColumnCount();
//
//                while (rs.next()) {
//                    Map<String, Object> row = new HashMap<>();
//                    for (int i = 1; i <= columnCount; i++) {
//                        String columnName = metaData.getColumnName(i);
//                        Object value = rs.getObject(i);
//                        String columnType = metaData.getColumnTypeName(i);
//                        int columnLength = metaData.getColumnDisplaySize(i);
//
//                        Map<String, Object> columnInfo = new HashMap<>();
//                        columnInfo.put(PARAM_VALUE, value);
//                        columnInfo.put(PARAM_TYPE, columnType);
//                        columnInfo.put(PARAM_LENGTH, columnLength);
//
//                        row.put(columnName, columnInfo);
//                    }
//                    result.add(row);
//                }
//
//            } catch (SQLException e) {
//                LogProcess.info("Error executing SQL");
////                LogProcess.info("" + e.getMessage());
//            }
//        } else {
//            LogProcess.info("not allowed SQL ");
//        }
//
//        return result;
//
//    }
//
//    private boolean allowedSqlFlag(String sql) {
//
//        List<String> whiteList = Arrays.asList("select", "insert", "delete");
//        List<String> blackList = Arrays.asList("drop", "update", "truncate", "--");
//
//        boolean flag = false;
//
//        sql = sql.toLowerCase();
//
//        //黑名單
//        for (String keyword : blackList) {
//            if (sql.contains(keyword)) {
//                LogProcess.info("SQL 語法含有禁止的關鍵字: " + keyword);
//                throw new SecurityException("SQL 語法含有禁止的關鍵字: " + keyword);
//            }
//        }
//        //白名單
//        for (String keyword : whiteList) {
//            if (sql.contains(keyword)) {
//                flag = true;
//            }
//        }
//
//        return flag;
//    }
}
