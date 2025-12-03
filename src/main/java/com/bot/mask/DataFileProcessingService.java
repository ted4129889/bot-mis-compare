package com.bot.mask;


import com.bot.config.CompareSetting;
import com.bot.mask.config.FileConfig;

import java.util.List;
import java.util.Map;

public interface DataFileProcessingService {


    /**
     * 執行檔案處理(比對使用)
     */
    boolean exec(String filePath, String area);

    /**
     * 執行檔案處理(比對使用)
     */
    boolean exec(Map<String, String> oldFileNameList, Map<String, String> newFileNameList, Map<String, FileConfig> fieldSettingList, CompareSetting setting);

    /**
     * 取得檔案的所有欄位名稱
     */
    List<String> getColumnList();

    /**
     * 取得定義檔(Xml)中的所有檔案名稱
     */
    List<String> getXmlAllFileName();

    /**
     * 處理檔案欄位匹配(UI畫面)
     * */
    void processPairingColumn(String fileName);




}
