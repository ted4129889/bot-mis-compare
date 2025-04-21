package com.bot.compare;


import com.bot.mask.config.SortFieldConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public interface CompareDataService {
    //TODO 分析資料、比對資料、輸出結果

    /**
     * 將兩個相同檔案資料做比對(aData 與 bData 比對)
     *
     * @param aData 原始資料
     * @param bData 要比對的資料
     * @param dataKey 資料主鍵
     * @param filterColList 需要比對的欄位(預設全欄位比)
     * @param sortFieldConfig 需要排序的欄位
     * */
    void parseData(List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey,List<String> filterColList,List<SortFieldConfig> sortFieldConfig);


    Map<String, Map<String, String>> getMatchData();

    Map<String, Map<String, String>> getMissingData();

    Map<String, Map<String, String>> getExtraData();
    List<Map<String, String>> getComparisonResult();

    List<Map<String, String>> getNewDataResult();

    List<Map<String, String>> getOldDataResult();



}
