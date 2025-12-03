package com.bot.compare;


import com.bot.mask.config.SortFieldConfig;

import java.util.List;
import java.util.Map;


public interface CompareDataService {
    //TODO 分析資料、比對資料、輸出結果

    /**
     * 將兩個相同檔案資料做比對(aData 與 bData 比對)
     *
     * @param aData           原始資料
     * @param bData           要比對的資料
     * @param dataKey         資料主鍵
     * @param filterColList   需要比對的欄位(預設全欄位比)
     * @param sortFieldConfig 需要排序的欄位
     * @param maskFieldList   遮蔽的欄位清單
     * @param headerBodyMode  表頭和內容是否同時存在
     */
    void parseData(List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey, List<String> filterColList, List<SortFieldConfig> sortFieldConfig, List<String> maskFieldList, boolean headerBodyMode);


    Map<String, Map<String, String>> getMatchData();

    Map<String, Map<String, String>> getMissingData();

    Map<String, Map<String, String>> getExtraData();

    List<Map<String, String>> getComparisonResult();

    List<Map<String, String>> getNewDataResult();

    List<Map<String, String>> getOldDataResult();

    int getDiffCount();


}
