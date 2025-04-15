package com.bot.mask;


import com.bot.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.path.PathValidator;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.xmltag.Field;
import com.bot.util.xml.vo.XmlData;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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

    private XmlData xmlData = null;

    @Override
    public boolean exportMaskedFile(Connection conn, String xmlFileName, String tableName, String env) {

        // 驗證 XML 路徑 並 開始解析
        String allowedTableName = validXmlFile(xmlFileName, tableName, env);

        // 查詢 DB
        List<Map<String, Object>> sqlData = new ArrayList<>();
        if (allowedTableName == null) {
            return false;
        } else {
            sqlData = getSqlData(conn, SQL_SELECT_TABLE + allowedTableName);
        }

        //  套用遮蔽 並 輸出檔案(輸出原始檔案以及遮蔽檔案)
        if (Objects.isNull(sqlData) || sqlData.size() == 0) {
            LogProcess.info("tableName=" + allowedTableName + ",資料庫無資料，不產生SQL檔案");
            return false;
        } else {
            dataMask(sqlData, allowedTableName);
        }


        return true;
    }


    private String validXmlFile(String xmlTableName, String tableName, String env) {

        String allowedTableName = tableName;

        String allowedPath = FilenameUtils.normalize(maskXmlFilePath);
        // 組合後的檔案名稱
        String xml = FilenameUtils.normalize(maskXmlFilePath + xmlTableName + XML_EXTENSION);

        if (pathValidator.isSafe(allowedPath, xml)) {
            // 沒有檔案時略過
            // parse Xml
            try {
                xmlData = xmlParser.parseXmlFile(xml);

                if (xmlData == null) {
                    return null;
                }

                if ("prod".equals(env)) {
                    allowedTableName = xmlData.getTable().getTableName();
                }
                LogProcess.warn("allowedTableName =" + allowedTableName);

                // get SQL data
                return allowedTableName;

            } catch (Exception e) {
                LogProcess.warn("xml file fail", e);
                return null;
            }

        } else {
            LogProcess.info("檔案路徑不安全，無法允許");
            return null;
        }

    }


    private void dataMask(List<Map<String, Object>> sqlData, String tableName) {

        try {

            // <field>
            List<Field> fields = xmlData.getFieldList();

            //產出未遮蔽資料
            dataMasker.maskData(sqlData, fields, false);
            writeFile(generateSQL(tableName, sqlData), outputFileOriginalPath + tableName + SQL_EXTENSION);

            //產出遮蔽資料
            dataMasker.maskData(sqlData, fields, true);
            writeFile(generateSQL(tableName, sqlData), outputFilePath + tableName + SQL_EXTENSION);

            LogProcess.info("tableName=" + tableName + ",產生SQL檔案");

        } catch (Exception e) {
            LogProcess.info("XmlToInsertGenerator.sqlConvInsertTxt error");
        }

    }

    private List<String> generateSQL(String tableName, List<Map<String, Object>> maskedSqlData) {

        StringBuilder result;
        List<String> fileContents = new ArrayList<>();

        if (maskedSqlData.size() == 0) {
            return new ArrayList<>();
        }

        String delContent = SQL_DELETE_TABLE + tableName + STR_SEMICOLON;

        fileContents.add(delContent);

        for (Map<String, Object> mask : maskedSqlData) {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Object objValues;
            result = new StringBuilder(BUFFER_CAPACITY);
            for (Map.Entry<String, Object> entry : mask.entrySet()) {

                columns.append(entry.getKey()).append(STR_DOT);
                objValues = entry.getValue();
                if (objValues instanceof Map) {
                    Map<String, Object> valuesMap = (Map<String, Object>) objValues;
                    values.append(formatValue(valuesMap.get(PARAM_VALUE))).append(" ,");
                }
            }
            String tmp =
                    String.format(
                            "INSERT INTO %s (%s) VALUES (%s);",
                            tableName,
                            columns.substring(0, columns.length() - 1),
                            values.substring(0, values.length() - 1));

            result.append(tmp);
            fileContents.add(result.toString());
        }
        return fileContents;
    }

    private String formatValue(Object val) {

        if (val == null) {
            return "NULL";
        }
        return val instanceof String ? "'" + val + "'" : val.toString();
    }

    /**
     * 輸出檔案
     *
     * @param fileContents 資料串
     * @param outFileName  輸出檔案名
     */
    private void writeFile(List<String> fileContents, String outFileName) {

        if (fileContents != null) {
            textFileUtil.deleteFile(outFileName);

            try {
                textFileUtil.writeFileContent(outFileName, fileContents, CHARSET);
            } catch (Exception e) {
                LogProcess.info("Error Message : Problem writing to file ");
            }
        } else {
            LogProcess.info("檔案：" + outFileName + " 無資料，未產出。");
        }
    }

    private List<Map<String, Object>> getSqlData(Connection connection, String sql) {

        List<Map<String, Object>> result = new ArrayList<>();

        //允許的SQL語法
        if (allowedSqlFlag(sql)) {

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

                ResultSet rs = pstmt.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        String columnType = metaData.getColumnTypeName(i);
                        int columnLength = metaData.getColumnDisplaySize(i);

                        Map<String, Object> columnInfo = new HashMap<>();
                        columnInfo.put(PARAM_VALUE, value);
                        columnInfo.put(PARAM_TYPE, columnType);
                        columnInfo.put(PARAM_LENGTH, columnLength);

                        row.put(columnName, columnInfo);
                    }
                    result.add(row);
                }

            } catch (SQLException e) {
                LogProcess.warn("Error executing SQL");

            }
        } else {
            LogProcess.info("not allowed SQL ");
        }

        return result;

    }

    private boolean allowedSqlFlag(String sql) {

        List<String> whiteList = Arrays.asList("select", "insert", "delete");
        List<String> blackList = Arrays.asList("drop", "update", "truncate", "--");

        boolean flag = false;

        sql = sql.toLowerCase();

        //黑名單
        for (String keyword : blackList) {
            if (sql.contains(keyword)) {
                LogProcess.info("SQL 語法含有禁止的關鍵字: " + keyword);
                throw new SecurityException("SQL 語法含有禁止的關鍵字: " + keyword);
            }
        }
        //白名單
        for (String keyword : whiteList) {
            if (sql.contains(keyword)) {
                flag = true;
            }
        }

        return flag;
    }


}
