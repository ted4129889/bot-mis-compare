/* (C) 2025 */
package com.bot.util.comparator.common;

import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import com.bot.util.log.LogProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CompareDbService {

    // PS 有資料庫連線就不算工具類了，就是一般的業務邏輯處理

    @Autowired
    private DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(CompareDbService.class);

    @Autowired
    public CompareDbService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 插入數據到 MISBHDB.DATACOMPARISON
     *
     * @param fileName 文件名稱
     * @param type     數據類型 (NEW/OLD)
     * @param dataKey  唯一標識
     * @param json     JSON 數據
     */
    public void insertData(String fileName, String type, String dataKey, String json) {
        checkKeyAndData(dataKey, json);

        String sql =
                "INSERT INTO MISBHDB.DATA_COMPARISON (FILENAME, TYPE, DATA_KEY, VALUE) VALUES"
                        + " (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, type);
            pstmt.setString(3, dataKey);

            // 處理 JSON 存儲，防止超過 4000 字元
            if (json.length() > 4000) {
                pstmt.setNClob(4, new StringReader(json));
            } else {
                pstmt.setString(4, json);
            }

            pstmt.executeUpdate();
            log.info("成功插入數據: fileName={}, type={}, dataKey={}", fileName, type, dataKey);

        } catch (SQLException e) {
            log.error(
                    "插入數據失敗: fileName={}, type={}, dataKey={}, error={}",
                    fileName,
                    type,
                    dataKey,
                    e.getMessage(),
                    e);
            throw new RuntimeException("Error inserting data: " + e.getMessage(), e);
        }
    }

    /**
     * 根據 fileName 從 MISBHDB.DATACOMPARISON 表格中撈取資料
     *
     * @param fileName 文件名稱
     * @return 撈取到的 JSON 數據，如果沒有找到則返回 null
     */
    public Map<String, String> getData(String fileName, String dataType) {
        String sql =
                "SELECT DATA_KEY, VALUE FROM MISBHDB.DATA_COMPARISON WHERE FILENAME = ? AND"
                        + " TYPE = ?";
        Map<String, String> dataMap = null;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, dataType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dataMap = new HashMap<>();
                    dataMap.put("DATA_KEY", rs.getString("DATA_KEY"));

                    // 處理 DATA_VALUE 是 CLOB 的情況
                    if (rs.getObject("VALUE") instanceof Clob) {
                        Clob clob = rs.getClob("VALUE");
                        dataMap.put("VALUE", convertClobToString(clob));
                    } else {
                        dataMap.put("VALUE", rs.getString("VALUE"));
                    }
                }
            }

        } catch (SQLException | IOException e) {
            LogProcess.error(String.format("撈取數據失敗: fileName=%s, type=%s, error=%s", fileName, e.getMessage(), e));
        }

        return dataMap;
    }

    /**
     * 驗證 dataKey 和 jsonData 是否有效
     */
    private void checkKeyAndData(String key, String jsonData) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (jsonData == null || jsonData.isEmpty()) {
            throw new IllegalArgumentException("JsonData cannot be null or empty");
        }
    }

    private String convertClobToString(Clob clob) throws SQLException, IOException {
        if (clob == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        try (Reader reader = clob.getCharacterStream();
             BufferedReader br = new BufferedReader(reader)) {

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
