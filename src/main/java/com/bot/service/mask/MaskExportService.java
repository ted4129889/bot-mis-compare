package com.bot.service.mask;


import java.sql.Connection;

public interface MaskExportService {

    /**
     * 匯出遮蔽後的檔案資料
     * @param conn 資料庫連線
     * @param xmlFileName XML 檔名
     * @param tableName 資料表名稱
     * @param env 資料環境
     * @paran paran 放入參數(目前皆為日期)
     * @return 是否匯出成功
     */
    boolean exportMaskedFile(Connection conn, String xmlFileName, String tableName,String env,String param);
}
