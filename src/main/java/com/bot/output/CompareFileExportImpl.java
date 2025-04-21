package com.bot.output;


import com.bot.compare.CompareDataService;
import com.bot.log.LogProcess;
import com.bot.mask.config.SortFieldConfig;
import com.bot.util.excel.MakeCsv;
import com.bot.util.excel.MakeExcel;
import com.bot.util.files.TextFileUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CompareFileExportImpl {

    @Value("${localFile.mis.compare_result}")
    private String resultFolder;
    @Autowired
    private MakeExcel makeExcel;
    @Autowired
    private MakeCsv makeCsv;
    @Autowired
    private CompareDataService compareDataService;
    @Autowired
    private TextFileUtil textFileUtil;

    private final String BOT_DATA = "BotData";
    private final String MIS_DATA = "MisData";

    private final String MISSING_DATA = "MissingData";

    private final String EXTRA_DATA = "ExtraData";

    private final String RESULT_DATA = "ComparisonResults";


    public String chooseExportFileType = "";


    public void run(String fileName, List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey, List<String> filterColList, List<SortFieldConfig> sortFieldConfig) {


        //處理比對資料(篩選部分)
        compareDataService.parseData(aData, bData, dataKey, filterColList, sortFieldConfig);

        LogProcess.info("chooseExportFileType =" + chooseExportFileType);
        //輸出檔案選項

        switch (chooseExportFileType) {
            case "excel":

                exportExcel(fileName);
                break;
            case "csv":
                exportCsv(fileName);
                break;
            default:
                LogProcess.info("chooseExportFileType is null");
                break;
        }


    }

    private void exportCsv(String fileName) {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String outPutPath = resultFolder + fileName + "_" + today + "/" + fileName + "_";


        try {
            //台銀檔案資料
            String outPutFile = outPutPath + BOT_DATA + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);
            List<Map<String, String>> oldDataResult = compareDataService.getOldDataResult();

            if (oldDataResult != null && !oldDataResult.isEmpty()) {
                makeCsv.writeToCsvBig5(oldDataResult, outPutFile);
            }


            //MIS檔案資料
            outPutFile = outPutPath + MIS_DATA + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            List<Map<String, String>> newDataResult = compareDataService.getNewDataResult();

            if (newDataResult != null && !newDataResult.isEmpty()) {
                makeCsv.writeToCsvBig5(newDataResult, outPutFile);
            }
            //比對結果
            outPutFile = outPutPath + RESULT_DATA + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            List<Map<String, String>> comparisonResult = compareDataService.getComparisonResult();

            if (comparisonResult != null && !comparisonResult.isEmpty()) {
                makeCsv.writeToCsvBig5(comparisonResult, outPutFile);
            }

            //缺少的資料
            outPutFile = outPutPath + MISSING_DATA + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            Map<String, Map<String, String>> missingData = compareDataService.getMissingData();

            if (missingData != null && !missingData.isEmpty()) {
                makeCsv.writeToCsvBig5(mapConvert(missingData, MISSING_DATA), outPutFile);
            }
            //多於的資料
            outPutFile = outPutPath + EXTRA_DATA + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            Map<String, Map<String, String>> extraData = compareDataService.getExtraData();

            if (extraData != null && !extraData.isEmpty()) {
                makeCsv.writeToCsvBig5(mapConvert(extraData, EXTRA_DATA), outPutFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, String>> mapConvert(Map<String, Map<String, String>> map) {
        List<Map<String, String>> tmpList = new ArrayList<>();

        Map<String, String> tmpMap = new LinkedHashMap<>();

        tmpMap.put("desc", "以下為產出檔案缺少的資料，第幾筆看「BotData」");
        tmpMap.put("pkCol", "PrimaryKey欄位");
        tmpMap.put("pk", "PrimaryKey值");

        tmpList.add(tmpMap);


        for (Map.Entry<String, Map<String, String>> r : map.entrySet()) {
            tmpMap = new LinkedHashMap<>();

            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String keyCol = col_1[1].replace(",", "+");
            String key = col_1[2].replace(",", "+");

            tmpMap.put("desc", "第" + num + "筆");
            tmpMap.put("pkCol", keyCol);
            tmpMap.put("pk", key);

            tmpList.add(tmpMap);
        }

        return tmpList;

    }

    private Map<String, String> MissingHeader() {
        Map<String, String> tmpMap = new LinkedHashMap<>();

        tmpMap.put("desc", "以下為產出檔案缺少的資料，第幾筆看「BotData」");
        tmpMap.put("pkCol", "PrimaryKey欄位");
        tmpMap.put("pk", "PrimaryKey值");

        return tmpMap;
    }

    private Map<String, String> ExtraHeader() {
        Map<String, String> tmpMap = new LinkedHashMap<>();

        tmpMap.put("desc", "以下為產出檔案多出來的資料，第幾筆看「MisData」");
        tmpMap.put("pkCol", "PrimaryKey欄位");
        tmpMap.put("pk", "PrimaryKey值");

        return tmpMap;
    }


    private List<Map<String, String>> mapConvert(Map<String, Map<String, String>> map, String headerType) {
        List<Map<String, String>> tmpList = new ArrayList<>();

        if (EXTRA_DATA.equals(headerType)) {
            tmpList.add(MissingHeader());
        }
        if (MISSING_DATA.equals(headerType)) {
            tmpList.add(ExtraHeader());
        }

        Map<String, String> tmpMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> r : map.entrySet()) {
            tmpMap = new LinkedHashMap<>();

            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String keyCol = col_1[1].replace(",", "+");
            String key = col_1[2].replace(",", "+");

            tmpMap.put("desc", "第" + num + "筆");
            tmpMap.put("pkCol", keyCol);
            tmpMap.put("pk", key);

            tmpList.add(tmpMap);
        }

        return tmpList;

    }

    private void exportExcel(String fileName) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String outPutFile = resultFolder + fileName + "_ComparisonResults_" + today + ".xlsx";

        LogProcess.info("outPutFile = " + outPutFile);

        //刪除檔案
        textFileUtil.deleteFile(outPutFile);

        //開起檔案
        makeExcel.open(outPutFile, BOT_DATA);


        //台銀檔案資料
        botFilePage();

        //MIS檔案資料
        misFilePage();

        //比對結果
        comparePage();

        //缺少的資料
        missPage();

        //多於的資料
        extraPage();

        makeExcel.close();
    }


    private void botFilePage() {


        makeExcel.useSheet(BOT_DATA);

        int col = 0;
        int row = 0;

        for (Map<String, String> r : compareDataService.getOldDataResult()) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                makeExcel.setValue(row, col, e.getValue());
                col++;
            }
        }
        makeExcel.autoSizeColumn();


    }

    private void misFilePage() {
        makeExcel.newSheet(MIS_DATA);

        int col = 0;
        int row = 0;

        for (Map<String, String> r : compareDataService.getNewDataResult()) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                makeExcel.setValue(row, col, e.getValue());
                col++;
            }
        }
        makeExcel.autoSizeColumn();

    }


    private void comparePage() {


        makeExcel.newSheet(RESULT_DATA);
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 0;
        int row = 0;

        for (Map<String, String> r : compareDataService.getComparisonResult()) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                String value = e.getValue();
                //第一筆給予鏈結
                if (col == 1 && row != 1) {
                    LogProcess.info("value = " + value);
                    String number = value.replaceAll("[^0-9]", ""); // 移除所有非數字
                    makeExcel.setLinkToSheetRow(MIS_DATA, Integer.parseInt(number) + 1);
                }
                makeExcel.setValue(row, col, value);
                col++;
            }
        }
        makeExcel.autoSizeColumn();


    }

    private void missPage() {

        makeExcel.newSheet(MISSING_DATA);
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "以下為產出檔案缺少的資料，第幾筆看「BotData」");
        makeExcel.setValue(row, col + 1, "PrimaryKey欄位");
        makeExcel.setValue(row, col + 2, "PrimaryKey值");

        for (Map.Entry<String, Map<String, String>> r : compareDataService.getMissingData().entrySet()) {
            col = 1;
            row++;
            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String keyCol = col_1[1];
            String key = col_1[2];

            //第一筆給予鏈結
            if (col == 1 && row != 1) {
                makeExcel.setLinkToSheetRow(BOT_DATA, Integer.parseInt(num) + 1);
            }
            makeExcel.setValue(row, col, "第" + num + "筆");
            makeExcel.setValue(row, col + 1, keyCol);
            makeExcel.setValue(row, col + 2, key);
        }

        makeExcel.autoSizeColumn();

    }

    private void extraPage() {

        makeExcel.newSheet(EXTRA_DATA);
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "以下為產出檔案多出來的資料，第幾筆看「MisData」");
        makeExcel.setValue(row, col + 1, "PrimaryKey欄位");
        makeExcel.setValue(row, col + 2, "PrimaryKey值");

        for (Map.Entry<String, Map<String, String>> r : compareDataService.getExtraData().entrySet()) {
            col = 1;
            row++;
            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String keyCol = col_1[1];
            String key = col_1[2];
            //第一筆給予鏈結
            if (col == 1 && row != 1) {
                makeExcel.setLinkToSheetRow(MIS_DATA, Integer.parseInt(num) + 1);
            }
            makeExcel.setValue(row, col, "第" + num + "筆");
            makeExcel.setValue(row, col + 1, keyCol);
            makeExcel.setValue(row, col + 2, key);
        }
        makeExcel.autoSizeColumn();


    }

}