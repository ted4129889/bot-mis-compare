package com.bot.compare;


import com.bot.log.LogProcess;
import com.bot.util.comparator.DataComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public void parseData(List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dynamicKeys, List<String> filterColList) {
        //最後輸出結果
        result = new ArrayList<>();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("desc", "資料結果，第幾筆看「MisData」");
        map.put("pk", "PrimaryKey");
        map.put("col", "欄位");
        map.put("oldData", "原始檔案的值");
        map.put("newData", "新產出檔案的值");
        result.add(map);
        //符合的資料
        matchMap = new LinkedHashMap<>();
        //不符合的資料(缺少)
        missingMap = new LinkedHashMap<>();
        //不符合的資料(多餘)
        extraMap = new LinkedHashMap<>();
        //欄位
        columns = new ArrayList<>();
        LogProcess.info("aData bef = " + aData);
        LogProcess.info("bData bef= " + bData);

        aData = aData.stream()
                .map(row -> filterFieldsBySelection(row, filterColList))
                .collect(Collectors.toList());

        bData = bData.stream()
                .map(row -> filterFieldsBySelection(row, filterColList))
                .collect(Collectors.toList());
        LogProcess.info("aData = " + aData);
        LogProcess.info("bData = " + bData);

        Map<String, Map<String, String>> aDataMap = buildIndex(aData, dynamicKeys);

        Map<String, Map<String, String>> bDataMap = buildIndex(bData, dynamicKeys);

        LogProcess.info("aMapIndex = " + aDataMap);
        LogProcess.info("bMapIndex = " + bDataMap);

        //扣掉表頭
        int index = -1;
        // 以 A 為主比對 B
        for (Map.Entry<String, Map<String, String>> entry : aDataMap.entrySet()) {
            index++;
            String key = entry.getKey();
            //第0筆為表頭
            if (index == 0) {
                for (Map.Entry<String, String> col : entry.getValue().entrySet()) {
                    columns.add(col.getValue());
                }

            }
            if (bDataMap.containsKey(key)) {
                processData(index, entry.getKey(), entry.getValue(), bDataMap.get(key));
                // A 和 B 都有
                matchMap.put(index + "#" + key, entry.getValue());
            } else {
                // B 缺少 A 的資料
                missingMap.put(index + "#" + key, entry.getValue());
            }
        }

        index = -1;
        // 檢查 B 有但 A 沒有的資料（B 多出來）
        for (Map.Entry<String, Map<String, String>> entry : bDataMap.entrySet()) {
            index++;
            String key = entry.getKey();
            if (!aDataMap.containsKey(key)) {
                // B 多出來
                extraMap.put(index + "#" + key, entry.getValue());
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
    public List<Map<String, String>> getResult() {
        return result;
    }

    /***
     * 將匹配到的資料串，根據欄位一一比對處理
     * */
    private void processData(int index, String key, Map<String, String> aRow, Map<String, String> bRow) {

        Map<String, String> map = new LinkedHashMap<>();

        String key_row = "row";

        //已知的檔案欄位
        for (String c : columns) {
            //用欄位對 A B資料 取得同一個欄位值

            if (!Objects.equals(aRow.get(c), bRow.get(c))) {
                map = new LinkedHashMap<>();

                String desc = "第" + bRow.get(key_row) + "筆";
                String oldData = aRow.get(c);
                String newData = bRow.get(c);

                LogProcess.info("desc = " + desc);

                map.put("desc", desc);
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
     * @return ap<String, Map < String, String>> 每筆資料與primarykey匹配
     */
    private Map<String, Map<String, String>> buildIndex(
            List<Map<String, String>> dataList,
            List<String> primaryKeyFields
    ) {
        return dataList.stream().collect(
                Collectors.toMap(
                        row -> makeCompositeKey(row, primaryKeyFields),
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
     * .map(row -> makeCompositeKey(row, dynamicKeys))
     * .collect(Collectors.toSet());
     * <p>
     * Set<String> keysB = bData.stream()
     * .map(row -> makeCompositeKey(row, dynamicKeys))
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
                .collect(Collectors.joining("#"));
    }

    //過濾欄位
    public Map<String, String> filterFieldsBySelection(Map<String, String> original, List<String> selectedFields) {
        Map<String, String> filtered = original.entrySet().stream()
                .filter(entry -> selectedFields.contains(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // 加回 "row" 欄位（前提是原始就有）
        //這是筆數
        if (original.containsKey("row")) {
            filtered.put("row", original.get("row"));
        }

        return filtered;
    }
}
