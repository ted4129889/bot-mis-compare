package com.bot.service.mask;


import com.bot.util.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.path.PathValidator;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.xmltag.Field;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.*;

@Service
public class MaskExportServiceImpl implements MaskExportService {
    @Value("${spring.profiles.active}")
    private String nowEnv;
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;
    @Value("${localFile.mis.batch.output}")
    private String outputFilePath;
    @Value("${localFile.mis.batch.output_original_data}")
    private String outputFileOriginalPath;

    @Autowired
    private PathValidator pathValidator;
    @Autowired
    private DataMasker dataMasker;
    @Autowired
    private XmlParser xmlParser;
    @Autowired
    private TextFileUtil textFileUtil;

    private static final String CHARSET = "BIG5";
    private static final int BUFFER_CAPACITY = 1024;
    private static final String SQL_EXTENSION = ".sql";
    private static final String SQL_DELETE_PREFIX = "DELETE FROM ";
    private static final String SQL_INSERT_TEMPLATE = "INSERT INTO %s (%s) VALUES (%s);";
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_LENGTH = "length";

    @Override
    public boolean exportMaskedFile(Connection conn, String xmlFileName, String tableName, String env, String param) {
        // 驗證並取得允許的表名
        String allowedTable = validXmlFile(xmlFileName, tableName, env);
        if (allowedTable == null) return false;
        // 刪除舊檔
        textFileUtil.deleteFile(outputFileOriginalPath + allowedTable + SQL_EXTENSION);
        textFileUtil.deleteFile(outputFilePath + allowedTable + SQL_EXTENSION);

        final int batchSize = 1000;
        boolean hasDeleted = false;

        long start = System.nanoTime();
        long duration = 0L;

        XmlData xmlData = null;
        try {
            xmlData = xmlParser.parseXmlFile(
                    FilenameUtils.normalize(maskXmlFilePath + xmlFileName + ".xml"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if ("prod".equals(env)) {
            allowedTable = xmlData.getTable().getTableName();
        }

        String sql = getSql(param, allowedTable, xmlData);

        List<Map<String, Object>> batchList = new ArrayList<>(batchSize);
        try (PreparedStatement pstmt = conn.prepareStatement(
                sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            pstmt.setFetchSize(batchSize);
            try (ResultSet rs = pstmt.executeQuery()) {

                // 解析欄位定義
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // 無資料則跳過
                if (!rs.isBeforeFirst()) {
                    LogProcess.info("No data found for table: " + allowedTable);
                    return false;
                } else {
//                    LogProcess.info("The data exists for table: " + allowedTable);
                }

                List<XmlField> fields = xmlData.getXmlFieldList();
                while (rs.next()) {
                    // 每筆 new 一個 Map，避免重用同一物件
                    Map<String, Object> rowMap = new HashMap<>(colCount);
                    for (int i = 1; i <= colCount; i++) {
                        String columnName = meta.getColumnName(i);
                        Object value = rs.getObject(i);
                        String columnType = meta.getColumnTypeName(i);

                        int columnLength = meta.getColumnDisplaySize(i);

                        Map<String, Object> colInfo = new HashMap<>(3);
                        colInfo.put(PARAM_VALUE, value);
                        colInfo.put(PARAM_TYPE, columnType);
                        colInfo.put(PARAM_LENGTH, columnLength);
                        rowMap.put(columnName, colInfo);


                    }

                    batchList.add(rowMap);

                    if (batchList.size() >= batchSize) {
                        writeBatchFiles(batchList, allowedTable, fields, hasDeleted);
                        hasDeleted = true;
                        batchList.clear();
                    }
                }
                // 處理剩餘不足 batchSize 的資料
                if (!batchList.isEmpty()) {
                    writeBatchFiles(batchList, allowedTable, fields, hasDeleted);
                }

                //統計耗時
                duration = duration + (System.nanoTime() - start);

                LogProcess.info("產生SQL檔案 = " + outputFilePath + allowedTable + SQL_EXTENSION + ",耗時: " + duration + "ns");


            }
        } catch (SQLException | IOException e) {

            LogProcess.warn("Invalid object name '" + allowedTable + "'");
//            LogProcess.warn("Error executing exportMaskedFile", e);
            return false;
        }
        return true;
    }

    private static String getSql(String param, String allowedTable, XmlData xmlData) {
        String sql = "SELECT * FROM " + allowedTable;

        String paramCol = "";
        if (xmlData.getParamDate() != null) {
            paramCol = xmlData.getParamDate();
            sql = "SELECT * FROM " + allowedTable + " WHERE (" +
                    "    CASE " +
                    "        WHEN LEN(CAST(" + paramCol + " AS VARCHAR)) = 7 THEN " +
                    " CAST(CAST(LEFT(CAST(" + paramCol + "  AS VARCHAR), 3) AS INT) + 1911 AS VARCHAR(4)) + RIGHT(CAST(" + paramCol + "  AS VARCHAR), 4)" +
                    "        ELSE " + paramCol + " END ) = CAST(" + param + " AS VARCHAR)";
        }
        return sql;
    }

    /**
     * 根據給定 rows 列表，先輸出原始，再輸出遮蔽後的 SQL 檔案
     */
    private void writeBatchFiles(
            List<Map<String, Object>> rows,
            String tableName,
            List<XmlField> fields,
            boolean deleteFlag
    ) throws IOException {
        // 原始資料
        List<String> originalSql = generateSqlLines(rows, tableName, deleteFlag);
        textFileUtil.writeFileContent(
                outputFileOriginalPath + tableName + SQL_EXTENSION,
                originalSql, CHARSET);
        // 2. 遮蔽後資料
        List<Map<String, Object>> maskedRows = deepCopyRows(rows);
        dataMasker.maskData(maskedRows, fields, true);
        List<String> maskedSql = generateSqlLines(maskedRows, tableName, deleteFlag);

        textFileUtil.writeFileContent(
                outputFilePath + tableName + SQL_EXTENSION,
                maskedSql, CHARSET);
    }

    //避免遮蔽時改到原始資料，省掉原來不必要的
    private List<Map<String, Object>> deepCopyRows(List<Map<String, Object>> original) {
        List<Map<String, Object>> copy = new ArrayList<>(original.size());
        for (Map<String, Object> row : original) {
            Map<String, Object> newRow = new HashMap<>(row.size());
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                // 內層 value 也是 Map
                Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
                newRow.put(entry.getKey(), new HashMap<>(valueMap));
            }
            copy.add(newRow);
        }
        return copy;
    }


    /**
     * 根據 rows 和 deleteFlag 產生 SQL 語句列表
     */
    private List<String> generateSqlLines(
            List<Map<String, Object>> rows,
            String tableName,
            boolean deleteFlag
    ) {
        List<String> lines = new ArrayList<>(rows.size() + 1);
        if (!deleteFlag) {
            lines.add(SQL_DELETE_PREFIX + tableName + ";");
        }

        for (Map<String, Object> row : rows) {
            StringBuilder colSb = new StringBuilder(BUFFER_CAPACITY);
            StringBuilder valSb = new StringBuilder(BUFFER_CAPACITY);

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String column = entry.getKey();
                Object raw = entry.getValue();

                colSb.append(column).append(",");
                if (raw instanceof Map) {
                    Map<String, Object> valuesMap = (Map<String, Object>) raw;
                    Object value = valuesMap.get("value");
                    valSb.append(formatValue(value)).append(",");
                }
            }
            colSb.setLength(colSb.length() - 1);
            valSb.setLength(valSb.length() - 1);
            lines.add(String.format(SQL_INSERT_TEMPLATE, tableName, colSb.toString(), valSb.toString()));
        }
        return lines;
    }

    private String validXmlFile(String xmlTableName, String tableName, String env) {
        String xmlFullPath = FilenameUtils.normalize(maskXmlFilePath + xmlTableName + ".xml");
        if (!pathValidator.isSafe(FilenameUtils.normalize(maskXmlFilePath), xmlFullPath)) {
            LogProcess.info("檔案路徑不安全，無法允許");
            return null;
        }
        try {
            XmlData xmlData = xmlParser.parseXmlFile(xmlFullPath);
            if (xmlData == null) {
                return null;
            }
            String allowed = tableName;
            if ("prod".equals(env)) {
                allowed = xmlData.getTable().getTableName();
            }
            return allowed;
        } catch (Exception e) {
            LogProcess.warn("xml file parsing fail", e);
            return null;
        }
    }

    private String formatValue(Object val) {
        if (val == null) {
            return "NULL";
        }
        if (val instanceof String) {
            return "N'" + val + "'";
        }
        return val.toString();
    }

}
