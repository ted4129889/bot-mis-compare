package com.bot.util.excel;

import com.bot.log.LogProcess;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Objects;

/**
 * ----------------------- MakeExcel EXCEL 檔案工具(re) ------------------*
 *
 * @author Ted Lin
 */

@Component("MakeExcel")
@Scope("prototype")
public class MakeExcel {

    private Workbook openedWorkbook = null;

    public Sheet openedSheet = null;

    private String fileName = null;

    private boolean isOpened = false;
    private boolean isClosed = false;

    public MakeExcel() {
        // 自動檢測未關閉情況
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isOpened && !isClosed) {
                System.err.println("Warning: The Excel file is not closed correctly, the system will automatically execute close()");
//                close();
            }
        }));
    }

    /**
     * 開啟檔案<br>
     *
     * @param fileName  指定檔案名稱(若檔案名稱不存在則改為新增)
     * @param sheetName 指定頁籤名稱(若頁籤名稱不存在則改為新增)
     */
    public void open(String fileName, Object sheetName) {
        isOpened = true;
        isClosed = false;
        //檢查檔案名稱
        //若檔案名稱中沒有副檔名，則補上預設副檔名(.xlsx)
        if (fileName.contains(".xlsx") || fileName.contains(".xls")) {

        } else {
            fileName = fileName + ".xlsx";
        }

        //目前只有EXCEL
        openExcel(fileName, sheetName);

        this.fileName = fileName;


    }


    /**
     * 讀取EXCEL
     *
     * @param fileName  Excel檔名
     * @param sheetName 可指定 sheet index or sheet name
     */
    private void openExcel(String fileName, Object sheetName) {
        checkOpenFiles();
        File file = new File(fileName);
        // 若檔案不存在則建立新檔案
        if (!file.exists()) {
            //判斷xlsx or xls
            if (fileName.contains(".xlsx")) {
                openedWorkbook = new XSSFWorkbook();
            } else {
                openedWorkbook = new HSSFWorkbook();
            }
            //新建頁籤
            newSheet(sheetName.toString());

        } else {

            try (FileInputStream fis = new FileInputStream(fileName)) {
                //判斷xlsx or xls
                if (fileName.contains(".xlsx")) {
                    openedWorkbook = new XSSFWorkbook(fis);
                } else {
                    openedWorkbook = new HSSFWorkbook(fis);
                }

                LogProcess.info("openExcel fileName = " + fileName);

                //判斷使用的sheetName 是用 字串名稱 或是 數字表示(0表示第一個頁籤)
                if (sheetName instanceof String) {
                    openedSheet = openedWorkbook.getSheet(sheetName.toString());
                } else {
                    openedSheet = openedWorkbook.getSheetAt(Integer.parseInt(sheetName.toString()) - 1);
                }
                LogProcess.info("openExcel sheetname = " + sheetName);

                //若沒有此頁籤，則新增
                if (openedSheet == null) {
                    newSheet(sheetName.toString());
                }

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    }

    /**
     * 讀取指定列/欄值<br>
     *
     * @param row 列
     * @param col 欄
     * @return Object 欄位值
     */
    public Object getValue(int row, int col) {
        checkOpenFiles();

        Row prow = openedSheet.getRow(row - 1);

        if (prow == null) {
            return "";
        } else {
            Cell tmpCell = prow.getCell(col - 1);
            if (tmpCell == null) {
                return "";
            }
            Object result = getCellValue(tmpCell);

            LogProcess.info("Get Value = " + result.toString());
            return result;
        }
    }


    /**
     * 取得 欄位內容（支援不同資料型態）
     *
     * @param cell cell資料
     */
    public String getCellValue(Cell cell) {
        Object result = null;
        switch (cell.getCellType()) {
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
                if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                    result = cell.getNumericCellValue();
                } else if (cell.getCachedFormulaResultType() == CellType.STRING) {
                    result = cell.getStringCellValue();
                } else {
                    result = cell.getCachedFormulaResultType().toString();
                }
                break;
            case STRING:
            default:
                result = cell.getStringCellValue();
        }
        return result.toString().trim();
    }

    /**
     * 設定指定列/欄值<br>
     *
     * @param row 列
     * @param col 欄
     * @param val 設定值
     * @return Object 欄位值
     */
    public void setValue(int row, int col, Object val) {
        checkOpenFiles();

        Row pRow = openedSheet.getRow(row - 1);
        if (pRow == null) {
            pRow = openedSheet.createRow(row - 1);
        }

        Cell pCell = pRow.getCell(col - 1);
        if (pCell == null) {
            pCell = pRow.createCell(col - 1);
        }

        sellValueType(pCell, val);

        LogProcess.info("set Value = " + val.toString());
    }


    /**
     * 設定 欄位內容（支援不同資料型態）
     *
     * @param cell  cell資料
     * @param value 設值
     */
    // 根據 Object 型別設定值
    private void sellValueType(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            CellStyle dateStyle = cell.getSheet().getWorkbook().createCellStyle();
            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy/MM/dd HH:mm:ss"));
            cell.setCellStyle(dateStyle);
            cell.setCellValue((java.util.Date) value);
        } else {
            throw new IllegalArgumentException("不支援的資料型別: " + value.getClass());
        }
    }


    /**
     * 關閉檔案
     */
    public void close() {

        try {
            if (fileName == "") {
                openedWorkbook.close();
            } else {
                saveExcel();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        LogProcess.info("Excel close");
    }

    private void saveExcel() throws IOException {
        if (!isOpened) {
            throw new IllegalStateException("請先呼叫 open 方法");
        }
        if (isClosed) {
            throw new IllegalStateException("Workbook 已經關閉，無法重複關閉");
        }

        try (FileOutputStream fos = new FileOutputStream(new File(fileName))) {
            openedWorkbook.write(fos);
        } finally {
            openedWorkbook.close();
            isClosed = true;
        }
    }

    /**
     * 新增Sheet<br>
     *
     * @param sheetName 新增sheet名稱
     */
    public void newSheet(String sheetName) {
        openedSheet = openedWorkbook.createSheet(sheetName);

    }

    /**
     * 指定要使用的Sheet<br>
     *
     * @param sheetName 指定的sheet名稱
     */
    public void useSheet(String sheetName) {
        useSheet(sheetName, "");
    }

    /**
     * 指定要使用的Sheet 並更改 Sheet名稱<br>
     *
     * @param sheetName    指定 sheet名稱
     * @param newSheetName 重新命名 sheet新名稱
     */
    public void useSheet(String sheetName, String newSheetName) {
        checkOpenFiles();
        //取得Sheet
        openedSheet = openedWorkbook.getSheet(sheetName);

        //若指定的sheet不存在則新增
        if (openedSheet == null) {
            newSheet(sheetName);
        }

        // 參數newSheetName若為空，則表示無須重新命名
        if (!Objects.equals(newSheetName, "")) {
            // 取得頁籤索引
            int sheetIndex = openedWorkbook.getSheetIndex(sheetName);
            // 判斷頁籤是否存在
            if (sheetIndex != -1) {
                // 修改頁籤名稱
                openedWorkbook.setSheetName(sheetIndex, newSheetName);
                LogProcess.info("Sheet 名稱修改成功：" + newSheetName);
            } else {
                LogProcess.info("找不到頁籤：" + sheetName);
            }
        }


    }

    /**
     * 檢查使用方法時是否有先開啟檔案('open'方法)
     */
    private void checkOpenFiles() {
        if (!isOpened) {
            throw new IllegalStateException("請先呼叫 open 方法");
        }
        if (isClosed) {
            throw new IllegalStateException("Workbook 已經關閉，無法操作");
        }
    }


    /**
     * 指定要使用的Sheet<br>
     *
     * @param minCol 最小欄位
     * @param maxCol 最大欄位
     */
    public void autoSizeColumn(int minCol, int maxCol) {
        for (int i = 0; i < 3; i++) {
            openedSheet.autoSizeColumn(i);
        }

    }

}
