/* (C) 2023 */
package com.bot.util.mask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("MaskUtil")
@Scope("prototype")
public class MaskUtil {

    //儲存上一次的訊息
    private static String latestMessage = "";

    /**
     * 傳回遮蔽過後的新 List<Map<String, String>>
     *
     * @param dataList    原始資料
     * @param maskKeyList 要遮蔽的 key 名稱列表
     * @return 遮蔽過的新 List
     */
    public  List<Map<String, String>> maskKeysMultipleData(List<Map<String, String>> dataList, List<String> maskKeyList) {
        if (dataList == null || maskKeyList == null || maskKeyList.isEmpty()) {
            return dataList;
        }

        List<Map<String, String>> maskedList = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, String> row = dataList.get(i);
            Map<String, String> newRow = new LinkedHashMap<>();

            if (i == 0) {
                // 首筆，直接原樣拷貝
                newRow.putAll(row);
            } else {
                // 從第2筆開始遮蔽
                for (Map.Entry<String, String> entry : row.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    newRow.put(key, value);
                }
            }
            maskedList.add(newRow);
        }

        return maskedList;
    }


    /**
     * 傳回遮蔽過後的新 Map<String, String>
     *
     * @param dataMap     單一筆資料 Map
     * @param maskKeyList 要遮蔽的 key 名稱列表
     * @return 遮蔽過的新 Map
     */
    public  Map<String, String> maskKeysSingleData(Map<String, String> dataMap, List<String> maskKeyList ) {
        if (dataMap == null || maskKeyList == null || maskKeyList.isEmpty()) {
            return dataMap;  // 如果是空的，直接回傳原本
        }

        Map<String, String> maskedMap = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : dataMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            maskedMap.put(key, value);
        }
        return maskedMap;
    }

    /**
     * 根據 primaryKey 移除 maskKeyList 裡重複的欄位，
     * 回傳新的 List<String>
     *
     * @param maskKeyList 要遮蔽的欄位列表
     * @param primaryKey  主鍵欄位列表
     * @return 移除後的新 List
     */
    public List<String> removePrimaryKeysFromMaskKeys(List<String> maskKeyList, List<String> primaryKey) {
        latestMessage = ""; // 每次先清空

        if (maskKeyList == null || maskKeyList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> primarySet = new HashSet<>(primaryKey != null ? primaryKey : List.of());
        List<String> resultList = new ArrayList<>();
        boolean hasOverlap = false;

        for (String key : maskKeyList) {
            if (primarySet.contains(key)) {
                hasOverlap = true;
            } else {
                resultList.add(key);
            }
        }

        if (hasOverlap) {
            latestMessage = "有機敏欄位為key值，故不遮蔽";
        }

        return resultList;
    }

    public String getLatestMessage() {
        return latestMessage;
    }
}