package com.bot.filter;


import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

/**
 * 過濾字串符
 */
public class CheakSafePathUtil {
    /**
     * 檢查檔案路徑是否在白名單目錄內，並防止路徑遍歷
     * @param allowedPath 允許的檔案路徑
     * @param filePath 請求的檔案路徑
     * @return 如果路徑安全，回傳true；否則回傳false
     */
    public static boolean isSafeFilePath(String allowedPath,String filePath) {
        // 規範化路徑，避免路徑包含雙斜線、父級目錄（..）等攻擊
        String normalizedPath = FilenameUtils.normalize(filePath);
        // 檢查檔案路徑是否有效，並且是否以白名單目錄開頭
        if (normalizedPath != null && normalizedPath.startsWith(allowedPath)) {
            return true;
        }
        return false;
    }

}
