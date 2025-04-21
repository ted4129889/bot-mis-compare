package com.bot.output;


import com.bot.compare.CompareDataService;
import com.bot.log.LogProcess;
import com.bot.mask.config.SortFieldConfig;
import com.bot.util.excel.MakeExcel;
import com.bot.util.files.TextFileUtil;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    private CompareDataService compareDataService;
    @Autowired
    private TextFileUtil textFileUtil;

    private final String BOT_DATA = "BotData";
    private final String MIS_DATA = "MisData";

    private final String MISSING_DATA = "MissingData";

    private final String EXTRA_DATA = "ExtraData";

    private final String RESULT_DATA = "ComparisonResults";


    public void run(String fileName, List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey, List<String> filterColList, List<SortFieldConfig> sortFieldConfig) {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String outPutFile = resultFolder + fileName + "_ComparisonResults_" + today + ".xlsx";


        LogProcess.info("outPutFile = " + outPutFile);


        //刪除檔案
        textFileUtil.deleteFile(outPutFile);

        //開起檔案
        makeExcel.open(outPutFile, BOT_DATA);

        //處理比對資料(篩選部分)
        compareDataService.parseData(aData, bData, dataKey, filterColList, sortFieldConfig);

        //台銀檔案資料
        botFilePage();

        //MIS檔案資料
        misFilePage();

        //比對結果
        comparePage();

        missPage();

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