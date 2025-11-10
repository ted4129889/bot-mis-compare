package com.bot.service.output.templates;

import com.bot.util.log.LogProcess;
import com.bot.util.templates.TemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Component("CompareResultRpt")
public class CompareResultRpt {
    public void exec(List<CompareResultBean> records, String date1, String date2) {
        String template = null;
        try {
            //每50筆換一頁(不含表頭表尾)
            int pageSize = 50;
            //先確認有幾頁
            int totalPages = (int) Math.ceil(records.size() / (double) pageSize);


            // 累加區
            int totalBot = 0;
            int totalMis = 0;
            int totalDiff = 0;
            int totalColDiff = 0;
            int totalMiss = 0;
            int totalExtra = 0;
            double totalAccuracy = 0.0;
            StringBuilder fullReport = new StringBuilder();

            for (int page = 0; page < totalPages; page++) {
                int start = page * pageSize;
                int end = Math.min(start + pageSize, records.size());

                // 每頁 detail 組合
                StringBuilder detailBuilder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    CompareResultBean r = records.get(i);
                    //%-5d	整數欄位，寬度 5，靠左對齊
                    //%-18s	字串欄位，寬度 18，靠左對齊
                    //%-8d	整數，寬度 8，靠左對齊
                    //%-9d	整數，寬度 9，靠左對齊
                    //%-10s	字串，寬度 10，靠左對齊
                    //%n	換行符號，依據系統自動為 \r\n 或 \n
                    //- 表示靠左對齊（預設是靠右）
                    //d 表示數值（decimal）
                    //s 表示字串（string）
                    //數字是「最小欄位寬度」
                    detailBuilder.append(String.format(
                            "%-5d %-14s %10d %10d %10d %10d %10d %10.2f%% %-10s%n",
                            i + 1,
                            r.getFileName(),
                            r.getBotTotal(),
                            r.getMisTotal(),
                            r.getDiffCount(),
                            r.getMissCount(),
                            r.getExtraCount(),
                            r.getAccuracy(),
                            r.getNote()
                    ));

                    // 總計累加
                    totalBot += r.getBotTotal();
                    totalMis += r.getMisTotal();
                    totalDiff += r.getDiffCount();
                    totalColDiff += r.getDiffColCount();
                    totalMiss += r.getMissCount();
                    totalExtra += r.getExtraCount();
                    totalAccuracy += r.getAccuracy();
                }
                totalAccuracy = totalAccuracy / end;
                // 每頁都重新載入樣板（不同頁數變數）
                String tplPath = "templates/CompareResultRpt.tpl";

                InputStream inputStream = TemplateUtil.class.getClassLoader().getResourceAsStream(tplPath);
                if (inputStream == null) {
                    throw new FileNotFoundException("Template not found: " + tplPath);
                }
                template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);


                // 每頁填入資料
                template = template.replace("{{DETAIL}}", detailBuilder.toString())
                        .replace("{{BATCHDATE}}", date2)
                        .replace("{{PAGE}}", String.valueOf(page + 1))
                        .replace("{{TOTALPAGE}}", String.valueOf(totalPages));

                // 最後一頁才加 SUMMARY
                if (page == totalPages - 1) {
                    String summary = String.format(
                            "                總計 %10d %10d %10d %10d %10d %10.2f%%",
                            totalBot, totalMis, totalDiff, totalMiss, totalExtra, totalAccuracy);
                    template = template.replace("{{SUMMARY}}", summary)
                            .replace("{{FILETOTAL}}", String.valueOf(records.size()));
                } else {
                    //因為還沒最後一頁，所以不需要
                    template = template.replace("{{SUMMARY}}", "").replace("檔案總數：{{FILETOTAL}}筆", "");
                }

                // 合併每一頁
                fullReport.append(template).append("\n\n\n");
            }

            Files.writeString(Path.of("ComparisonResult/CheckResultList_" + date1 + ".txt"), fullReport);
            LogProcess.info(log, "報表產出完成");

        } catch (IOException e) {
            LogProcess.error(log, "read or write template fail:" + e.getMessage());
        }
    }
}
