/* (C) 2025 */
package com.bot.util.comparator;


import com.bot.util.log.LogProcess;
import com.bot.service.mask.config.SortFieldConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Scope("prototype")
public class ComparatorUtil {
    /**
     * 根據排序條件排序資料（List<Map>）
     *
     * @param dataList    欲排序的資料列表
     * @param sortConfigs 欄位名稱、排序 （true=ASC, false=DESC）、優先排序順序
     * @return 排序後的資料
     */
    public static List<Map<String, String>> sortByFields(List<Map<String, String>> dataList, List<SortFieldConfig> sortConfigs) {
        // 無需排序或沒有資料
        if (sortConfigs == null || sortConfigs.isEmpty() || dataList == null || dataList.size() <= 1)
            return dataList;

        // 保留第一筆資料（欄位標題 row）
        Map<String, String> headerRow = dataList.get(0);
        List<Map<String, String>> bodyRows = new ArrayList<>(dataList.subList(1, dataList.size()));

        // 按照使用者指定順序排序
        sortConfigs.sort(SortFieldConfig.sortByOrder());


        Comparator<Map<String, String>> comparator = null;

        for (SortFieldConfig config : sortConfigs) {

            String field = config.getFieldName();
            boolean asc = config.isAscending();

            Comparator<Map<String, String>> fieldComparator =
                    Comparator.comparing(
                            map -> convertComparable(map.getOrDefault(field, "")),
                            getSmartComparator(asc, true)
                    );

            comparator = (comparator == null)
                    ? fieldComparator
                    : comparator.thenComparing(fieldComparator);
        }
        // 排序第二筆以後的資料
        bodyRows.sort(comparator);

        // 合併回原始順序：標題 + 排好序的內容
        List<Map<String, String>> sorted = new ArrayList<>();
        sorted.add(headerRow);
        sorted.addAll(bodyRows);

        return sorted;
    }

    /**
     * 根據值內容轉成可比較物件（數字 → BigDecimal，其他 → String）
     */
    private static Object convertComparable(String value) {
        if (value == null || value.isEmpty()) return "";

        if (value.matches("-?\\d+(\\.\\d+)?")) {
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        LogProcess.info("convertComparable value=" + value);
        return value;
    }

    /**
     * 回傳具備 null 處理與升降序控制的比較器
     */
    private static Comparator<Object> getSmartComparator(boolean ascending, boolean nullsFirst) {
        Comparator<Object> base = (o1, o2) -> {

//            LogProcess.info("ascending =" + nullsFirst);
//            LogProcess.info("nullsFirst =" + nullsFirst);
//            LogProcess.info("o1 =" + o1);
//            LogProcess.info("o2 =" + o2);
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return nullsFirst ? -1 : 1;
            if (o2 == null) return nullsFirst ? 1 : -1;
            return ((Comparable<Object>) o1).compareTo(o2);
        };
        return ascending ? base : base.reversed();
    }
}