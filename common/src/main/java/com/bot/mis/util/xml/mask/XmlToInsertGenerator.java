/* (C) 2025 */
package com.bot.mis.util.xml.mask;

import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.xml.mask.allowedTable.AllowedTableName;
import com.bot.mis.util.xml.mask.xmltag.Field;
import com.bot.mis.util.xml.vo.XmlData;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class XmlToInsertGenerator {
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;

    @Value("${localFile.mis.batch.output}")
    private String outputFilePath;

    @Autowired private TextFileUtil textFileUtil;
    @Autowired private DataSource dataSource;
    @Autowired private DataMasker dataMasker;
    @Autowired private XmlParser xmlParser;
    private static final String CHARSET = "BIG5";
    private static final int BUFFER_CAPACITY = 5000;
    private static final String PARAM_VALUE = "value";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_LENGTH = "length";
    private final String XML_EXTENSION = ".xml";

    private final String SQL_EXTENSION = ".sql";
    private final String SPACE = "";

    // fix SQL Injection
    private final String SQL_SELECT_TABLE = "select * from %s";
    private final String SQL_DELETE_TABLE = "delete from %s ;";

    private static final String XML_PATH = "external-config/xml/mask";

    public void sqlConvInsertTxt(String xmlFile) {

        try {
            String xml = maskXmlFilePath + xmlFile.replace(XML_EXTENSION, SPACE) + XML_EXTENSION;

            // parse Xml
            XmlData xmlData = xmlParser.parseXmlFile(xml);

            // <table>
            String tableName = xmlData.getTable().getTableName();

            // <field>
            List<Field> fields = xmlData.getFieldList();

            // get SQL data
            List<Map<String, Object>> sqlData = getSqlData(tableName);

            // mask data
            dataMasker.maskData(sqlData, fields);

            // generate .txt
            writeFile(generateSQL(tableName, sqlData), outputFilePath + tableName + SQL_EXTENSION);

        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToInsertGenerator.sqlConvInsertTxt error");
        }
    }

    private List<String> generateSQL(String tableName, List<Map<String, Object>> maskedSqlData) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tableName=" + tableName);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "maskedSqlData=" + maskedSqlData);

        StringBuilder result;
        List<String> fileContents = new ArrayList<>();
        String delContent = String.format(SQL_DELETE_TABLE, tableName);

        fileContents.add(delContent);

        for (Map<String, Object> mask : maskedSqlData) {
            StringBuilder columns = new StringBuilder();
            StringBuilder values = new StringBuilder();
            Object objValues;
            result = new StringBuilder(BUFFER_CAPACITY);
            for (Map.Entry<String, Object> entry : mask.entrySet()) {

                columns.append(entry.getKey()).append(" ,");
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
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tmp=" + tmp);

            result.append(tmp);
            fileContents.add(result.toString());
        }
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileContents=" + fileContents);
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
     * @param outFileName 輸出檔案名
     */
    private void writeFile(List<String> fileContents, String outFileName) {

        textFileUtil.deleteFile(outFileName);

        try {
            textFileUtil.writeFileContent(outFileName, fileContents, CHARSET);
        } catch (LogicException e) {
            moveErrorResponse(e);
        }
    }

    private List<Map<String, Object>> getSqlData(String tableName) {
        // 白名單驗證，防止SQL Injection
        if (!AllowedTableName.contains(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }

        String sql = String.format(SQL_SELECT_TABLE, tableName);

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

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
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), "XmlToInsertGenerator.getSqlData error");
            throw new LogicException("", "XmlToInsertGenerator.getSqlData error");
        }
        return result;
    }

    private void moveErrorResponse(LogicException e) {
        //        event.setPeripheryRequest();
    }
}
