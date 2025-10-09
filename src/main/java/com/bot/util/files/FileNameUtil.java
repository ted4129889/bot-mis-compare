/* (C) 2023 */
package com.bot.util.files;


import com.bot.util.log.LogProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Scope("prototype")
public class FileNameUtil {
    /**
     * 判斷來源檔名是否符合預期前綴條件
     * <p>
     * 處理流程：
     * 1. 去除 ".txt" 結尾
     * 2. 替換民國/西元日期為 [yyyymmdd]
     * 3. 去除 ".Conv" 結尾
     * 4. 比對是否包含 expectedPrefix
     *
     * @param fileName    來源檔名（可能包含日期、.Conv、.txt）
     * @param xmlFileName 定義檔檔案名稱
     * @return true = 匹配；false = 不匹配
     */
    public boolean isFileNameMatch(String fileName, String xmlFileName) {
        if (fileName == null || xmlFileName == null) return false;

        String tmpFileName = fileName.toLowerCase();
        // 1. 移除 .txt 結尾（不分大小寫）
        if (tmpFileName.toLowerCase().endsWith(".txt")) {
            tmpFileName = tmpFileName.substring(0, fileName.length() - 4);
        }

        // 2. 移除日期（7 碼民國或 8 碼西元），改成 [yyyymmdd]
        String dateRegex = "\\.(\\d{7}|\\d{8})";
        String dateRegex2 = "(\\d{7}|\\d{8})";
        String dateRegex3 = "\\_(\\d{7}|\\d{8})";


        // fas 中間檔需要處理，其他都是介面檔
        if (tmpFileName.toLowerCase().startsWith("fas") || tmpFileName.toLowerCase().startsWith("misbh_fas")) {
            tmpFileName = tmpFileName.replace("(?i)\\.conv$", ""); // (?i) 表示不區分大小寫
            tmpFileName = tmpFileName.replace(".[yyyymmdd]", "");
            tmpFileName = tmpFileName.replace("misbh_", "");
        }else{
            tmpFileName = tmpFileName.replace(dateRegex, ".[yyyymmdd]");
            tmpFileName = tmpFileName.replace(dateRegex2, "[yyyymmdd]");
            tmpFileName = tmpFileName.replace(dateRegex3, "_[yyyymmdd]");
        }

        return tmpFileName.trim().equals(xmlFileName.trim().toLowerCase());
    }


    /**
     * 判斷檔案名稱是否以 "Misbh_Fas" 或 "Fas" 開頭（不區分大小寫）
     *
     * @param fileName 檔案名稱或完整路徑（會自動取出檔名）
     * @return true = 是指定格式的檔案；false = 否
     */
    public boolean isFasFile(String fileName) {
        if (fileName == null) return false;

        // 只要名稱中包含 Faslnclfl（不分大小寫）就直接排除
        if (fileName.toLowerCase().contains("faslnclfl".toLowerCase())) {
            LogProcess.info(log,"fileName = faslnclfl,與外送處理方式相同");
            return false;
        }

        // 先移除路徑，只保留檔名
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String name = lastSlash >= 0 ? fileName.substring(lastSlash + 1) : fileName;

        // 移除副檔名
        int dot = name.lastIndexOf('.');
        String baseName = dot > 0 ? name.substring(0, dot) : name;
        boolean result = baseName.toLowerCase().startsWith("misbh_fas") || baseName.toLowerCase().startsWith("fas");
//        LogProcess.info("isFasFile = {}",result);

        // 比對開頭（不區分大小寫）
        return result;
    }

}
