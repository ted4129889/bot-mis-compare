package com.bot.service.compare;


import com.bot.service.mask.config.SortFieldConfig;
import com.bot.util.comparator.ComparatorUtil;
import com.bot.util.log.LogProcess;
import com.bot.util.mask.MaskUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CompareDataServiceImpl implements CompareDataService {
    @Value("${localFile.mis.batch.compare.new.directory}")
    private String newFilePath;
    @Value("${localFile.mis.batch.compare.old.directory}")
    private String oldFilePath;
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;
    @Value("${localFile.mis.xml.output.directory}")
    private String outputPath;
    public List<String> columns = new ArrayList<>();
    public Map<String, Map<String, String>> matchMap = new LinkedHashMap<>();
    public Map<String, Map<String, String>> missingMap = new LinkedHashMap<>();
    public Map<String, Map<String, String>> extraMap = new LinkedHashMap<>();

    List<Map<String, String>> result = new ArrayList<>();
    List<Map<String, String>> oldDataResult = new ArrayList<>();
    List<Map<String, String>> newDataResult = new ArrayList<>();

    @Autowired
    MaskUtil maskUtil;

    private final String customPrimaryKey = "row";
    private String groupKey = "";

    @Override
    public void parseData(List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey, List<String> filterColList, List<SortFieldConfig> sortFieldConfig, List<String> maskFieldList, boolean headerBodyMode) {
        //最後輸出結果
        result = new ArrayList<>();
        //符合的資料
        matchMap = new LinkedHashMap<>();
        //不符合的資料(缺少)
        missingMap = new LinkedHashMap<>();
        //不符合的資料(多餘)
        extraMap = new LinkedHashMap<>();
        //欄位
        columns = new ArrayList<>();

        LogProcess.info("dataKey = " + dataKey);
        LogProcess.info("filterColList = " + filterColList);
//        LogProcess.info("aData bef = " + aData);
//        LogProcess.info("bData bef= " + bData);


        //將資料挑選欄位
        List<Map<String, String>> aDataTmp = aData;
        List<Map<String, String>> aDataResult = IntStream.range(0, aDataTmp.size())
                .mapToObj(i -> {
                    Map<String, String> row = aDataTmp.get(i);
                    return filterFieldsBySelection(row, filterColList, dataKey, i); // 流水號從 0 開始
                })
                .collect(Collectors.toList());


        List<Map<String, String>> bDataTmp = bData;
        List<Map<String, String>> bDataResult = IntStream.range(0, bDataTmp.size())
                .mapToObj(i -> {
                    Map<String, String> row = bDataTmp.get(i);
                    return filterFieldsBySelection(row, filterColList, dataKey, i);
                })
                .collect(Collectors.toList());
//        LogProcess.info("aData = " + aData);
//        LogProcess.info("bData = " + bData);

        //將資料做排序
        aDataResult = ComparatorUtil.sortByFields(aDataResult, sortFieldConfig);

        bDataResult = ComparatorUtil.sortByFields(bDataResult, sortFieldConfig);

//        LogProcess.info("aData sort = " + aDataResult);
//        LogProcess.info("bData sort= " + bDataResult);

        //原始檔案
        oldDataResult = new ArrayList<>();
        oldDataResult.addAll(maskUtil.maskKeysMultipleData(aDataResult, maskFieldList));
        //比對的檔案
        newDataResult = new ArrayList<>();
        newDataResult.addAll(maskUtil.maskKeysMultipleData(bDataResult, maskFieldList));

        //將資料賦予Key，用來比對
        Map<String, Map<String, String>> aDataMap = buildIndex(aDataResult, dataKey);
        Map<String, Map<String, String>> bDataMap = buildIndex(bDataResult, dataKey);

//        LogProcess.info("aMapIndex = " + aDataMap);
//        LogProcess.info("bMapIndex = " + bDataMap);


        //扣掉表頭
        int index = -1;
        // 以 A 為主比對 B
        for (Map.Entry<String, Map<String, String>> entry : aDataMap.entrySet()) {
            index++;
            String key = entry.getKey();

            //是否表頭資料與內容資料同時存在
            if (headerBodyMode) {
                //第1筆為表頭的欄位名稱，第3筆為內容的欄位名稱
                if (index == 0 || index == 2) {
                    groupKey = key;
                    for (Map.Entry<String, String> col : entry.getValue().entrySet()) {
                        columns.add(col.getValue());
                    }
                }
            } else {
                //第1筆為表頭的欄位名稱，
                if (index == 0) {
                    groupKey = key;
                    for (Map.Entry<String, String> col : entry.getValue().entrySet()) {
                        columns.add(col.getValue());
                    }
                }
            }

            if (bDataMap.containsKey(key)) {

                processData(index, entry.getKey(), entry.getValue(), bDataMap.get(key), maskFieldList);
                // A 和 B 都有

                matchMap.put(index + "#" + groupKey + "#" + entry.getKey(), entry.getValue());
            } else {
                // B 缺少 A 的資料
                missingMap.put(index + "#" + groupKey + "#" + entry.getKey(), entry.getValue());
            }
        }

        index = -1;
        // 檢查 B 有但 A 沒有的資料（B 多出來）
        for (Map.Entry<String, Map<String, String>> entry : bDataMap.entrySet()) {
            index++;
            String key = entry.getKey();
            if (!aDataMap.containsKey(key)) {
                // B 多出來
                extraMap.put(index + "#" + groupKey + "#" + entry.getKey(), entry.getValue());
            }
        }

//        LogProcess.info("columns = " + columns);
//        LogProcess.info("matchMap = " + matchMap);
//        LogProcess.info("missingMap = " + missingMap);
//        LogProcess.info("extraMap = " + extraMap);
//        LogProcess.info("result = " + result);
    }

    @Override
    public Map<String, Map<String, String>> getMatchData() {
        return matchMap;
    }

    @Override
    public Map<String, Map<String, String>> getMissingData() {
        return missingMap;
    }

    @Override
    public Map<String, Map<String, String>> getExtraData() {
        return extraMap;
    }

    @Override
    public List<Map<String, String>> getComparisonResult() {
        return result;
    }

    @Override
    public List<Map<String, String>> getNewDataResult() {
        return newDataResult;
    }

    @Override
    public List<Map<String, String>> getOldDataResult() {
        return oldDataResult;
    }

    /***
     * 將匹配到的資料串，根據欄位一一比對處理
     * */
    private void processData(int index, String key, Map<String, String> aRow, Map<String, String> bRow, List<String> maskFieldList) {

        Map<String, String> map = new LinkedHashMap<>();


        //已知的檔案欄位
        for (String c : columns) {

            //用欄位對 A B資料 取得同一個欄位值


            if (!Objects.equals(aRow.get(c), bRow.get(c))) {
                map = new LinkedHashMap<>();
                String desc = "";
                if (!Objects.equals(bRow.get(customPrimaryKey), "")) {
                    desc = "第" + index + "筆";
                } else {
                    desc = "第" + bRow.get(customPrimaryKey) + "筆";
                }

                String oldData = aRow.get(c);
                String newData = bRow.get(c);
//                if (maskFieldList.contains(c)) {
//                    oldData = "ooo";
//                    newData = "ooo";
//                }

                map.put("desc", desc);
                map.put("pkGrp", groupKey);
                map.put("pk", key);
                map.put("col", c);
                map.put("oldData", oldData);
                map.put("newData", newData);
                result.add(map);
            }

        }

    }

    /**
     * @param dataList         檔案資料 List<Map<String, String>>
     * @param primaryKeyFields 主鍵 List<String>
     * @return Map<String, Map < String, String>> 每筆資料與primarykey匹配
     */
    private Map<String, Map<String, String>> buildIndex(
            List<Map<String, String>> dataList,
            List<String> primaryKeyFields
    ) {

        return dataList.stream().collect(
                Collectors.toMap(
                        row -> {
                            // 如果沒指定主鍵欄位，就給流水號
                            if (primaryKeyFields == null || primaryKeyFields.isEmpty()) {
                                return row.get(customPrimaryKey);
                            } else {
                                return makeCompositeKey(row, primaryKeyFields);
                            }
                        },
                        row -> row,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                )
        );


    }

    /**
     * 匹配每筆資料的primarykey
     *
     * @param row              單筆資料 Map<String, String>
     * @param primaryKeyFields 主鍵 List<String>
     * @return 回傳資料主鍵的值 String
     *
     * <p>
     * <p>
     * 範例
     * Set<String> keysA = aData.stream()
     * .map(row -> makeCompositeKey(row, dataKey))
     * .collect(Collectors.toSet());
     * <p>
     * Set<String> keysB = bData.stream()
     * .map(row -> makeCompositeKey(row, dataKey))
     * .collect(Collectors.toSet());
     * <p>
     * // A與B都有的
     * Set<String> match = new HashSet<>(keysA);
     * match.retainAll(keysB);
     * // 新資料有少，沒有才是正常
     * Set<String> missing = new HashSet<>(keysA);
     * missing.removeAll(keysB);
     * // 新資料有多，沒有才是正常
     * Set<String> extra = new HashSet<>(keysB);
     * extra.removeAll(keysA);
     */
    private String makeCompositeKey(Map<String, String> row, List<String> primaryKeyFields) {
        return primaryKeyFields.stream()
                .map(row::get)
                .collect(Collectors.joining(","));
    }

    /**
     * 篩選欄位
     *
     * @param original       資料來源 Map<String, String>
     * @param selectedFields 需要的欄位 List<String>
     * @param dataKey        主鍵清單 List<String>
     * @return 篩選過後的資料 Map<String, String>
     */
    public Map<String, String> filterFieldsBySelection(Map<String, String> original, List<String> selectedFields, List<String> dataKey, int rowNum) {


        //若沒有選擇 哪個欄位要比對的話則跳過(UI上暫時拿掉這功能)
//        if (selectedFields == null || selectedFields.isEmpty()) {
//            return filtered;
//        }
        Map<String, String> filtered = new LinkedHashMap<>();

        // 先放自訂主鍵在最前面（如果需要補）
        if (dataKey == null || dataKey.isEmpty()) {
            // rowNum=0 表示表頭
            filtered.put(customPrimaryKey, rowNum == 0 ? customPrimaryKey : String.valueOf(rowNum));
        }

        // 再放選取欄位（保持順序）
        original.entrySet().stream()
                .filter(entry -> selectedFields.contains(entry.getKey()))
                .forEach(entry -> filtered.put(entry.getKey(), entry.getValue()));
//        filtered = original.entrySet().stream()
//                .filter(entry -> selectedFields.contains(entry.getKey()))
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (a, b) -> a,
//                        LinkedHashMap::new
//                ));

//        //若沒有key則自動補上 自訂的key
//        if (dataKey == null || dataKey.isEmpty()) {
//            //rowNum=0 表示表頭
//            filtered.put(customPrimaryKey, rowNum == 0 ? customPrimaryKey : String.valueOf(rowNum));
//        }

        LogProcess.info("filtered = " + filtered);
        return filtered;
    }


}
