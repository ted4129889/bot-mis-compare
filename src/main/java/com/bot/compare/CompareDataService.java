package com.bot.compare;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public interface CompareDataService {
    //TODO 分析資料、比對資料、輸出結果

    void parseData(List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dynamicKeys,List<String> filterColList);

    Map<String, Map<String, String>> getMatchData();

    Map<String, Map<String, String>> getMissingData();

    Map<String, Map<String, String>> getExtraData();

    List<Map<String, String>> getResult();


}
