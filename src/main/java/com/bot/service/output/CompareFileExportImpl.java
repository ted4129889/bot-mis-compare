package com.bot.service.output;


import com.bot.dto.CompareSetting;
import com.bot.service.compare.CompareDataService;
import com.bot.service.output.templates.CompareResultBean;
import com.bot.util.excel.MakeCsv;
import com.bot.util.excel.MakeExcel;
import com.bot.util.files.TextFileUtil;
import com.bot.util.log.LogProcess;
import com.bot.util.mask.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CompareFileExportImpl {

    @Value("${localFile.mis.compare_result.csv}")
    private String resultCsvFolder;

    @Value("${localFile.mis.compare_result.excel}")
    private String resultExcelFolder;
    @Value("${localFile.mis.compare_result.txt}")
    private String resultTxt;
    @Autowired
    private MakeExcel makeExcel;
    @Autowired
    private MakeCsv makeCsv;
    @Autowired
    private CompareDataService compareDataService;
    @Autowired
    private TextFileUtil textFileUtil;
    @Autowired
    private MaskUtil maskUtil;

    private static final String CHARSET_BIG5 = "Big5";
    private static final String CHARSET_UTF8 = "UTF-8";
    private final String BOT_DATA = "BotData";
    private final String MIS_DATA = "MisData";

    private final String MISSING_DATA = "MissingData";

    private final String EXTRA_DATA = "ExtraData";

    private final String RESULT_DATA = "ComparisonResults";


    public String chooseExportFileType = "";

    private List<Map<String, String>> comparisonResult = new ArrayList<>();
    private List<Map<String, String>> oldDataResult = new ArrayList<>();
    private List<Map<String, String>> newDataResult = new ArrayList<>();
    private Map<String, Map<String, String>> missingResult = new LinkedHashMap<>();
    private Map<String, Map<String, String>> extraResult = new LinkedHashMap<>();

    public List<CompareResultBean> outputResultRpt = new ArrayList<>();

    boolean isShowComparisonData = true;
    boolean isShowOldData = true;
    boolean isShowNewData = true;
    boolean isShowMissingData = true;
    boolean isShowExtraData = true;
    public String dateTimeStr = "";
    public String dateTimeStr2 = "";
    public LocalDateTime dateTime;

    private int dataCount = 0;

    public void run(String fileName, List<Map<String, String>> getOldDataResult, List<Map<String, String>> getNewDataResult, List<Map<String, String>> getComparisonResult, Map<String, Map<String, String>> getMissingData, Map<String, Map<String, String>> getExtraData, CompareSetting setting) {

        fileName = fileName.replace(".txt", "");
        dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        dateTimeStr2 = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

        oldDataResult = getOldDataResult;
        newDataResult = getNewDataResult;
        comparisonResult = getComparisonResult;
        missingResult = getMissingData;
        extraResult = getExtraData;

        isShowOldData = true;
        isShowNewData = true;

        if (comparisonResult.isEmpty()) {
            isShowComparisonData = false;
        } else {
            isShowComparisonData = true;
        }
        if (missingResult.isEmpty()) {
            isShowMissingData = false;
        } else {
            isShowMissingData = true;
        }
        if (extraResult.isEmpty()) {
            isShowExtraData = false;
        } else {
            isShowExtraData = true;
        }

//        LogProcess.info("oldDataResult =" + oldDataResult);
//        LogProcess.info("newDataResult =" + newDataResult);
//        LogProcess.info("comparisonResult =" + comparisonResult);

        //正常是全產，遇到勾選的時候才會選擇要只有產錯誤的
        boolean isExportOnlyErrorFile = setting.isExportOnlyErrorFile();

        if (isExportOnlyErrorFile) {
            //不出 A、B檔案：
            isShowOldData = false;
            isShowNewData = false;

        }


        //輸出檔案選項

        switch (chooseExportFileType) {
            case "excel":
                exportExcel(fileName);
                exportTextFile(fileName);
                break;
            case "csv":
                exportCsv(fileName);
                exportTextFile(fileName);
                break;
            case "Both":
                exportCsv(fileName);
                exportExcel(fileName);
                exportTextFile(fileName);
                break;
            default:
                LogProcess.info(log, "chooseExportFileType is null");
                break;
        }


    }

    private void exportCsv(String fileName) {

        String outPutPath = resultCsvFolder + fileName + "_" + dateTimeStr + "/" + fileName + "_";


        try {
            //台銀檔案資料
            String outPutFile = outPutPath + BOT_DATA + "_" + dateTimeStr + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            if (isShowOldData) {
                dataCount = makeCsv.writeToCsvBig5(oldDataResult, outPutFile);
            }


            //MIS檔案資料
            outPutFile = outPutPath + MIS_DATA + "_" + dateTimeStr + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            if (isShowNewData) {
                dataCount = makeCsv.writeToCsvBig5(newDataResult, outPutFile);
            }
            //比對結果
            outPutFile = outPutPath + RESULT_DATA + "_" + dateTimeStr + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);


            if (isShowComparisonData) {
                dataCount = makeCsv.writeToCsvBig5(mapConvert(comparisonResult), outPutFile);
            }

            //缺少的資料
            outPutFile = outPutPath + MISSING_DATA + "_" + dateTimeStr + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);

            if (isShowMissingData) {
                dataCount = makeCsv.writeToCsvBig5(mapConvert(missingResult, MISSING_DATA), outPutFile);
            }
            //多餘的資料
            outPutFile = outPutPath + EXTRA_DATA + "_" + dateTimeStr + ".csv";
            //刪除檔案
            textFileUtil.deleteFile(outPutFile);


            if (isShowExtraData) {
                dataCount = makeCsv.writeToCsvBig5(mapConvert(extraResult, EXTRA_DATA), outPutFile);
            }
        } catch (IOException e) {
            LogProcess.error(log, "csv output error");

        }
    }

    private List<Map<String, String>> mapConvert(List<Map<String, String>> list) {
        List<Map<String, String>> tmpList = new ArrayList<>();

        Map<String, String> tmpMap = new LinkedHashMap<>();
        tmpMap.put("pk", "用PrimaryKey值查詢差異");
        tmpMap.put("pkGrp", "PrimaryKey欄位");
        tmpMap.put("col", "錯誤欄位");
        tmpMap.put("oldData", "原始檔案的值");
        tmpMap.put("newData", "新產出檔案的值");

        tmpList.add(tmpMap);

        tmpList.addAll(list);

        return tmpList;

    }

    private Map<String, String> missingHeader() {
        Map<String, String> tmpMap = new LinkedHashMap<>();

        tmpMap.put("pk", "用PrimaryKey值查詢差異,Bot Data");
        tmpMap.put("pkCol", "PrimaryKey欄位");
        tmpMap.put("value", "PrimaryKey值");

        return tmpMap;
    }

    private Map<String, String> extraHeader() {
        Map<String, String> tmpMap = new LinkedHashMap<>();

        tmpMap.put("pk", "用PrimaryKey值查詢差異,查看Mis Data");
        tmpMap.put("pkCol", "PrimaryKey欄位");
        tmpMap.put("value", "PrimaryKey值");

        return tmpMap;
    }

    private Map<String, String> comparisonHeader() {
        Map<String, String> tmpMap = new LinkedHashMap<>();
        tmpMap.put("desc", "資料結果，第幾筆看「MisData」");
        tmpMap.put("pkGrp", "PrimaryKey欄位");
        tmpMap.put("pk", "PrimaryKey值");
        tmpMap.put("col", "錯誤欄位");
        tmpMap.put("oldData", "原始檔案的值");
        tmpMap.put("newData", "新產出檔案的值");

        return tmpMap;
    }


    private List<Map<String, String>> mapConvert(Map<String, Map<String, String>> map, String headerType) {
        List<Map<String, String>> tmpList = new ArrayList<>();

        if (EXTRA_DATA.equals(headerType)) {
            tmpList.add(extraHeader());
        }
        if (MISSING_DATA.equals(headerType)) {
            tmpList.add(missingHeader());
        }

        Map<String, String> tmpMap = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> r : map.entrySet()) {
            tmpMap = new LinkedHashMap<>();

            String[] keyGrp = r.getKey().split(",");
            String key = keyGrp[0];
            String keyCol = keyGrp[1];
            String value = r.getValue().toString();

            tmpMap.put("pk", key);
            tmpMap.put("pkCol", keyCol);
            tmpMap.put("value", value);

            tmpList.add(tmpMap);
        }

        return tmpList;

    }

    private void exportExcel(String fileName) {

        String outPutFile = resultExcelFolder + fileName + "_ComparisonResults_" + dateTimeStr + ".xlsx";

//        LogProcess.info("outPutFile = " + outPutFile);

        //刪除檔案
        textFileUtil.deleteFile(outPutFile);

        //開起檔案
        makeExcel.open(outPutFile, BOT_DATA);

        if (isShowOldData) {
            //台銀檔案資料
            botFilePage();
        }

        if (isShowNewData) {
            //MIS檔案資料
            misFilePage();
        }
        if (isShowComparisonData) {
            //比對結果
            comparePage();
        }
        if (isShowMissingData) {
            //缺少的資料
            missPage();
        }
        if (isShowExtraData) {
            //多於的資料
            extraPage();
        }

        makeExcel.close();
    }


    private void botFilePage() {


        makeExcel.useSheet(BOT_DATA);
//        LogProcess.info(log,"oldDataResult = " + oldDataResult.size());

        int col = 0;
        int row = 0;

        for (Map<String, String> r : oldDataResult) {
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
//        LogProcess.info(log,"newDataResult = " + newDataResult.size());

        int col = 0;
        int row = 0;

        for (Map<String, String> r : newDataResult) {
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

        int col = 1;
        int row = 1;
//        LogProcess.info(log,"comparisonResult = " + comparisonResult.size());


        makeExcel.setValue(row, col, "用PrimaryKey值查詢差異");
        makeExcel.setValue(row, col + 1, "PrimaryKey欄位");
        makeExcel.setValue(row, col + 2, "錯誤的欄位");
        makeExcel.setValue(row, col + 3, "原始檔案的值");
        makeExcel.setValue(row, col + 4, "新產出檔案的值");

        for (Map<String, String> r : comparisonResult) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                String value = e.getValue();

//                LogProcess.info("value = " + value);
                //第一筆給予鏈結
                if (col == 1 && row != 1) {
//                    LogProcess.info("value = " + value);
                    String number = value.replaceAll("[^0-9]", ""); // 移除所有非數字
//                    makeExcel.setLinkToSheetRow(MIS_DATA, Integer.parseInt(number) + 1);
                }
                makeExcel.setValue(row, col, value);
                col++;
            }
        }
        makeExcel.autoSizeColumn();

        if (comparisonResult.isEmpty()) {
            makeExcel.setValue(row + 1, col, "資料無誤");
        }

    }

    private void missPage() {

        makeExcel.newSheet(MISSING_DATA);
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "用PrimaryKey值查詢差異,查看 Bot Data");
        makeExcel.setValue(row, col + 1, "PrimaryKey欄位");
        makeExcel.setValue(row, col + 2, "PrimaryKey值");

        for (Map.Entry<String, Map<String, String>> r : missingResult.entrySet()) {
            col = 1;
            row++;
            String[] keyGrp = r.getKey().split(",");
            String key = keyGrp[0];
            String keyCol = keyGrp[1];
            String value = r.getValue().toString();

            //第一筆給予鏈結
            if (col == 1 && row != 1) {
//                makeExcel.setLinkToSheetRow(BOT_DATA, Integer.parseInt(num) + 1);
            }
            makeExcel.setValue(row, col, key);
            makeExcel.setValue(row, col + 1, keyCol);
            makeExcel.setValue(row, col + 2, value);
        }

        makeExcel.autoSizeColumn();

        if (missingResult.isEmpty()) {
            makeExcel.setValue(row + 1, col, "資料無誤");
        }

    }

    private void extraPage() {

        makeExcel.newSheet(EXTRA_DATA);
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "用PrimaryKey值查詢差異,查看 Mis Data");
        makeExcel.setValue(row, col + 1, "PrimaryKey欄位");
        makeExcel.setValue(row, col + 2, "PrimaryKey值");

        for (Map.Entry<String, Map<String, String>> r : extraResult.entrySet()) {
            col = 1;
            row++;
            String[] keyGrp = r.getKey().split(",");
            String key = keyGrp[0];
            String keyCol = keyGrp[1];
            String value = r.getValue().toString();

            //第一筆給予鏈結
            if (col == 1 && row != 1) {
//                makeExcel.setLinkToSheetRow(BOT_DATA, Integer.parseInt(num) + 1);
            }
            makeExcel.setValue(row, col, key);
            makeExcel.setValue(row, col + 1, keyCol);
            makeExcel.setValue(row, col + 2, value);
        }
        makeExcel.autoSizeColumn();

        if (extraResult.isEmpty()) {
            makeExcel.setValue(row + 1, col, "資料無誤");
        }

    }


    private void exportTextFile(String fileName) {


        //扣1是因為扣除欄位
        int botTotal = oldDataResult.size() - 1;
        int misTotal = newDataResult.size() - 1;
        int diffColCount = comparisonResult.size();//差異欄位數
        int diffCount = compareDataService.getDiffCount();//差異筆數
        int missCount = missingResult.size();
        int extraCount = extraResult.size();

        int errorCount = diffCount + missCount + extraCount;

        double accuracyPercent = 0.0;
        accuracyPercent = 100.0 - Math.round(errorCount * 10000.0 / botTotal) / 100.0;
        System.out.println("accuracyPercent = " + accuracyPercent + "%");
        String note = "";

        outputResultRpt.add(new CompareResultBean(fileName, botTotal, misTotal, diffCount, diffColCount, missCount, extraCount, accuracyPercent, note));


    }

    public void init() {
        outputResultRpt.clear();
        dateTimeStr = "";
        dateTimeStr2 = "";
    }


    public void show() {

    }

}