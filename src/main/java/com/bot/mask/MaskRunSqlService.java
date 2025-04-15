package com.bot.mask;


import com.bot.filter.CheakSafePathUtil;
import com.bot.log.LogProcess;
import com.bot.util.path.PathValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private int index = 0;

    private int totalCnt = 0;

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
        try (Connection conn = dataSource.getConnection()) {
            if (conn != null) {
                List<String> sqlPaths = getSafeSQLFilePaths(allowedPath);
                for (String path : sqlPaths) {
                    String sql = readSqlFromFile(path);
                    executeSqlStatements(conn, sql, path);
                }
                LogProcess.info("共成功執行 " + totalCnt + " 筆 SQL 檔案。");
            }
        } catch (Exception e) {
            LogProcess.error("SQL 執行失敗", e);
        }
    }   private List<String> getSafeSQLFilePaths(String rootPath) {
        String normalizedBase = FilenameUtils.normalize(rootPath);
        try (Stream<Path> paths = Files.walk(Paths.get(normalizedBase), 1)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(p -> pathValidator.isSafe(normalizedBase, FilenameUtils.normalize(p)))
                    .filter(p -> p.toLowerCase().endsWith(".sql"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LogProcess.warn("讀取 SQL 路徑失敗", e);
            return List.of();
        }
    }

    private String readSqlFromFile(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (Exception e) {
            LogProcess.warn("讀取 SQL 檔失敗：" + filePath, e);
            return "";
        }
    }

    private void executeSqlStatements(Connection conn, String sql, String filePath) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
            totalCnt++;
            LogProcess.info("成功執行 SQL 檔案：" + filePath);
        } catch (SQLException e) {
            LogProcess.warn("SQL 檔案執行失敗：" + filePath, e);
        }
    }

}
