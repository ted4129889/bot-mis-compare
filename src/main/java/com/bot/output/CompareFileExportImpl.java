package com.bot.output;


import com.bot.compare.CompareDataService;
import com.bot.log.LogProcess;
import com.bot.util.excel.MakeExcel;
import com.bot.util.files.TextFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CompareFileExportImpl {
    @Autowired
    private MakeExcel makeExcel;
    @Autowired
    private CompareDataService compareDataService;
    @Autowired
    private TextFileUtil textFileUtil;

    public void run(String fileName, List<Map<String, String>> aData, List<Map<String, String>> bData, List<String> dataKey, List<String> filterColList) {

        String outPutFile = fileName +"_ComparisonResults.xlsx";
        //刪除檔案
        textFileUtil.deleteFile(outPutFile);

        //開起檔案
        makeExcel.open(outPutFile, "BotData");

        //台銀檔案資料
        botFilePage(aData);

        //MIS檔案資料
        misFilePage(bData);

        //處理比對資料(篩選部分)
        compareDataService.parseData(aData, bData, dataKey, filterColList);

        //比對結果
        comparePage();

        missPage();

        extraPage();

        makeExcel.close();
    }

    private void botFilePage(List<Map<String, String>> aData) {


        makeExcel.useSheet("BotData");

        int col = 0;
        int row = 0;

        for (Map<String, String> r : aData) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                makeExcel.setValue(row, col, e.getValue());
                col++;
            }
        }
        makeExcel.autoSizeColumn(1, col);


    }

    private void misFilePage(List<Map<String, String>> bData) {
        makeExcel.newSheet("MisData");

        int col = 0;
        int row = 0;

        for (Map<String, String> r : bData) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                makeExcel.setValue(row, col, e.getValue());
                col++;
            }
        }
        makeExcel.autoSizeColumn(1, col);

    }

    private void comparePage() {


        makeExcel.newSheet("ComparisonResults");
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 0;
        int row = 0;

        for (Map<String, String> r : compareDataService.getResult()) {
            col = 1;
            row++;
            for (Map.Entry<String, String> e : r.entrySet()) {
                makeExcel.setValue(row, col, e.getValue());
                col++;
            }
        }
        makeExcel.autoSizeColumn(1, col);


    }

    private void missPage() {

        makeExcel.newSheet("MissingData");
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "以下為產出檔案缺少的資料，第幾筆看「BotData」");
        makeExcel.setValue(row, col + 1, "主鍵");

        for (Map.Entry<String, Map<String, String>> r : compareDataService.getMissingData().entrySet()) {
            col = 1;
            row++;
            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String key = col_1[1];

            makeExcel.setValue(row, col, "第" + num + "筆");
            makeExcel.setValue(row, col + 1, key);
        }

        makeExcel.autoSizeColumn(1, col);

    }

    private void extraPage() {

        makeExcel.newSheet("ExtraData");
        //比對結果的層級
        //先抓主key 以舊的為準 把新的抓來比

        int col = 1;
        int row = 1;


        makeExcel.setValue(row, col, "以下為產出檔案多出來的資料，第幾筆看「MisData」");
        makeExcel.setValue(row, col + 1, "主鍵");

        for (Map.Entry<String, Map<String, String>> r : compareDataService.getExtraData().entrySet()) {
            col = 1;
            row++;
            String[] col_1 = r.getKey().split("#");
            String num = col_1[0];
            String key = col_1[1];

            makeExcel.setValue(row, col, "第" + num + "筆");
            makeExcel.setValue(row, col + 1, key);
        }
        makeExcel.autoSizeColumn(1, col);


    }

}