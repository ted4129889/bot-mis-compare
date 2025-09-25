package com.bot.util.excel;

import com.bot.util.log.LogProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ----------------------- MakeCsv 檔案工具 ------------------*
 *
 * @author Ted Lin
 */
@Slf4j
@Component("MakeCsv")
@Scope("prototype")
public class MakeCsv {
    private static final String CHARSET_BIG5 = "Big5";
    private static final String CHARSET_UTF8 = "UTF-8";

    /**
     * 將資料寫入 CSV，支援 Big5 編碼，並自動建立資料夾
     *
     * @param dataList 資料列表
     * @param filePath 檔案完整路徑（例如：ComparisonResult/result.csv）
     * @throws IOException
     * @return 回傳筆數
     */
    public int writeToCsvBig5(List<Map<String, String>> dataList, String filePath) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            System.out.println("無資料可寫入 CSV。");
            return 0;
        }

        filePath = FilenameUtils.normalize(filePath);
        // 建立資料夾（如果不存在）
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                System.out.println("已建立資料夾：" + parentDir.getAbsolutePath());
            }
        }

        // 取欄位順序（用第一筆）
        List<String> headers = new ArrayList<>(dataList.get(0).keySet());
        int cnt = 0;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), Charset.forName(CHARSET_BIG5)))) {

            // 寫入標題列
//            writer.write(String.join(",", headers));
//            writer.newLine();

            // 寫入每筆資料
            for (Map<String, String> row : dataList) {
                cnt++;
                List<String> values = row.entrySet().stream()
                        .map(entry -> escapeCsv(entry.getValue()))
                        .collect(Collectors.toList());
                writer.write(String.join(",", values));
                writer.newLine();
            }
        }
        LogProcess.info(log,"export CSV（Big5） file name ： " + filePath);
        return cnt;
    }

    // 處理 CSV 特殊字元
    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }


    /**
     * 將資料寫入 CSV 檔案
     *
     * @param dataList 資料列表，每筆為欄位名對應值的 Map
     * @param filePath 檔案儲存路徑（包含 .csv）
     */
    public void writeToCsv(List<Map<String, String>> dataList, String filePath) throws IOException {
        if (dataList == null || dataList.isEmpty()) {
            System.out.println("無資料可寫入 CSV。");
            return;
        }
        filePath = FilenameUtils.normalize(filePath);
        // 建立資料夾（如果不存在）
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                System.out.println("已建立資料夾：" + parentDir.getAbsolutePath());
            }
        }
        // 取得欄位名稱順序（第一筆的 keySet）
        List<String> headers = new ArrayList<>(dataList.get(0).keySet());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 寫入標題列
            writer.write(String.join(",", headers));
            writer.newLine();

            // 寫入每筆資料
            for (Map<String, String> row : dataList) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    String value = row.getOrDefault(header, "");
                    values.add(escapeCsv(value)); // 處理可能有逗號、換行的欄位
                }
                writer.write(String.join(",", values));
                writer.newLine();
            }
        }

        System.out.println("CSV 檔案已建立：" + filePath);
    }


    private void checkFolder(String fileName){
        // 檢查並建立資料夾
        File file = new File(fileName);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                LogProcess.info(log,"已建立資料夾：" + parentDir.getAbsolutePath());
            } else {
                LogProcess.warn(log,"資料夾建立失敗：" + parentDir.getAbsolutePath());
            }
        }

    }
}
