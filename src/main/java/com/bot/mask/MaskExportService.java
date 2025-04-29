package com.bot.mask;


import com.bot.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.allowedTable.AllowedDevTableName;
import com.bot.util.xml.mask.allowedTable.AllowedLocalTableName;
import com.bot.util.xml.mask.allowedTable.AllowedProdTableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Function;

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
