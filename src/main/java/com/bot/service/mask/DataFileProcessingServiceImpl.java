package com.bot.service.mask;


import com.bot.dto.CompareSetting;
import com.bot.service.compare.CompareDataService;
import com.bot.service.output.templates.CompareResultRpt;
import com.bot.util.files.FileNameUtil;
import com.bot.util.log.LogProcess;
import com.bot.service.mask.config.FileConfig;
import com.bot.service.mask.config.SortFieldConfig;
import com.bot.service.output.CompareFileExportImpl;
import com.bot.util.files.TextFileUtil;
import com.bot.util.mask.MaskUtil;
import com.bot.util.parse.Parse;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.mask.xmltag.Field;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import com.bot.util.xml.vo.XmlFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Slf4j
@Component
public class DataFileProcessingServiceImpl implements DataFileProcessingService {
//    @Value("${localFile.mis.batch.bot_output}")
//    private String botOutputPath;

    @Value("${localFile.mis.batch.output}")
    private String outputPath;
    @Value("${localFile.mis.xml.file_def}")
    private String botMaskXmlFile;
    @Value("${localFile.mis.json.field_setting.directory}")
    private String fieldSettinngFile;

    @Autowired
    private XmlParser xmlParser;
    @Autowired
    private Parse parse;
    @Autowired
    private TextFileUtil textFileUtil;
    @Autowired
    private FileNameUtil fileNameUtil;


    @Autowired
    private MaskUtil maskUtil;
    @Autowired
    private DataMasker dataMasker;
    @Autowired
    private CompareFileExportImpl compareFileExportImpl;
    @Autowired
    private CompareResultRpt compareResultRpt;
    @Autowired
    private CompareDataService compareDataService;
    //畫面區域1
    private static final String TEXTAREA_1 = "textarea1";
    //畫面區域2
    private static final String TEXTAREA_2 = "textarea2";
    private static final String CHARSET_BIG5 = "Big5";
    private static final String CHARSET_UTF8 = "UTF-8";

    CompareSetting settings;

    List<Map<String, String>> cList = new ArrayList<>();

    List<Map<String, String>> aFileData = new ArrayList<>();
    List<Map<String, String>> bFileData = new ArrayList<>();
    //存放定義檔案(DailyBatchFileDefinition.xml) 不會變
    List<XmlData> xmlDataList = new ArrayList<>();
    //遮蔽欄位名稱
    List<String> maskFieldList = new ArrayList<>();
    List<XmlField> xmlFieldList = new ArrayList<>();
    Map<String, FileConfig> jsonFile = new LinkedHashMap<>();
    /**
     * 先存下定義檔的fileName
     */
    List<String> tmpXmlFileName = new ArrayList<>();
    List<String> dataKey = new ArrayList<>();

    List<String> columnAList = new ArrayList<>();
    List<String> columnBList = new ArrayList<>();
    List<String> columnList = new ArrayList<>();
    List<String> columnAllList = new ArrayList<>();
    List<XmlField> tmpXmlFieldList = new ArrayList<>();
    XmlData tmpXmlData = new XmlData();
    boolean existflag = false;
    String outPutFile = "";

    List<XmlField> xmlFieldList_H = new ArrayList<>();
    List<XmlField> xmlFieldList_B = new ArrayList<>();
    List<XmlField> xmlFieldList_F = new ArrayList<>();
    private List<Map<String, String>> comparisonResult = new ArrayList<>();
    private Map<String, Map<String, String>> missingResult = new LinkedHashMap<>();
    private Map<String, Map<String, String>> extraResult = new LinkedHashMap<>();
    private boolean headerMode = false;
    private boolean bodyMode = false;
    private boolean headerBodyMode = false;

    @Override
    public boolean exec() {
        return exec("", "");
    }

    @Override
    public boolean exec(String cFilePath, String uiTextArea) {
        return exec(cFilePath, uiTextArea, null, null, null, null);
    }

    @Override
    public boolean exec(String cFilePath, String uiTextArea, Map<String, String> oldFileNameMap, Map<String, String> newFileNameMap, Map<String, FileConfig> fieldSettingList, CompareSetting setting) {

        //允許的路徑
//        String tbotOutputPath = FilenameUtils.normalize(botOutputPath);

        if (oldFileNameMap != null && newFileNameMap != null) {
            pairingProfile3(oldFileNameMap, newFileNameMap, fieldSettingList, setting);

        } else {
//            pairingProfile(tbotOutputPath);
        }
        return true;
    }

    /**
     * 讀取xml定義檔 和 json檔案
     */
    @PostConstruct
    private void readDefinitionXml() {

        try {

            //允許的路徑(XML)
            String dailyBatchFileDefinitionFile = FilenameUtils.normalize(botMaskXmlFile);

            XmlFile xmlFile;

            xmlFile = xmlParser.parseXmlFile2(dailyBatchFileDefinitionFile);
            xmlDataList = xmlFile.getDataList();

            LogProcess.info(log,"讀取 external-config/xml/bot_output 資料夾下的 DailyBatchFileDefinition.xml 定義檔內有" + xmlDataList.size() + "組 <data> 格式");

            for (XmlData data : xmlDataList) {
                tmpXmlFileName.add(data.getFileName());
            }


            //允許的路徑(JSON)
            String fieldSettingFile = FilenameUtils.normalize(fieldSettinngFile);

            LogProcess.info(log,"config file is exist: " + fieldSettingFile);
            File allowedFile = new File(fieldSettingFile);

            ObjectMapper mapper = new ObjectMapper();
            jsonFile = mapper.readValue(allowedFile, new TypeReference<>() {
            });


            File folder = new File("ComparisonResult");

            if (!folder.exists()) {
                boolean created = folder.mkdirs(); // 可建立多層路徑
                if (created) {
                    System.out.println("created folder：ComparisonResult");
                } else {
                    System.out.println("create fail");
                }
            }


        } catch (IOException e) {

            throw new RuntimeException(e);
        }


    }


    @Override
    public List<String> getColumnList() {
        LogProcess.info(log,"columnList =" + columnAllList);
        return columnAllList;
    }

    @Override
    public List<String> getXmlAllFileName() {
        return tmpXmlFileName;
    }

    /**
     *
     */
    //TODO 此方法目前未使用到
    private void pairingProfile(String tbotOutputPath) {
        int calcuTotal = 0;
        //台銀原檔案路徑
        List<String> folderList = getFilePaths(tbotOutputPath);
        LogProcess.info(log,"在batch-file/bot_output資料夾內的檔案有" + folderList.size() + "個，清單如下...");
        LogProcess.info(log,folderList.toString());
        for (String requestedFilePath : folderList) {
            //允許路徑
            requestedFilePath = FilenameUtils.normalize(requestedFilePath);
            try {
                for (XmlData data : xmlDataList) {
                    if (requestedFilePath.contains(data.getFileName())) {

                        List<String> outputData = performMasking(requestedFilePath, data, "");

                        //重新指向資料夾路徑
                        String reNamePath = requestedFilePath.replace("bot_output", "bot_output_mask");

                        //確認允許路徑
                        String maskPath = FilenameUtils.normalize(reNamePath);

                        //刪除原檔案
                        textFileUtil.deleteFile(maskPath);

                        //輸出檔案
                        textFileUtil.writeFileContent(maskPath, outputData, CHARSET_BIG5);

                        calcuTotal++;
                    }
                }

            } catch (Exception e) {
                LogProcess.info(log,"pairingProfile error");
            }
        }

        LogProcess.info(log,"產出遮蔽後的檔案在 batch-file/bot_output_mask 資料夾,有" + calcuTotal + "個檔案");
    }

    /**
     * 單支檔案處理(配合UI畫面)
     *
     * @param cFile      檔案名稱(含路徑)
     * @param uiTextArea 清單類型(原始檔案清單、比對檔案清單)
     */
    public void pairingProfile2(String cFile, String uiTextArea) {

        //允許路徑
        cFile = FilenameUtils.normalize(cFile);

        String comparecFile = Paths.get(cFile).getFileName().toString();
        cList = new ArrayList<>();
        dataKey = new ArrayList<>();


        try {
            existflag = false;
            for (XmlData data : xmlDataList) {
                if (comparecFile.equals(data.getFileName())) {
                    LogProcess.info(log,"cFile = " + comparecFile);
                    LogProcess.info(log,"data.getFileName() = " + data.getFileName());
                    outPutFile = data.getFileName();
                    existflag = true;

                    performMasking(cFile, data, uiTextArea);
//                    LogProcess.warn(log,"cList= {}",cList);
//                    LogProcess.warn(log,"cList= {}",cList.size());

                    if (TEXTAREA_2.equals(uiTextArea)) {
//                        LogProcess.warn(log,"bcList= {}",cList);
//                        LogProcess.warn(log,"bcList= {}",cList.size());
                        bFileData = new ArrayList<>();
                        bFileData.addAll(cList);


                    } else if (TEXTAREA_1.equals(uiTextArea)) {
//                        LogProcess.warn(log,"acList= {}",cList);
//                        LogProcess.warn(log,"acList= {}",cList.size());
                        aFileData = new ArrayList<>();
                        aFileData.addAll(cList);
                    }

                }

            }


        } catch (Exception e) {
            LogProcess.warn(log,"pairingProfile2 error");
        }

    }

    /**
     * 比對資料夾內的所有檔案
     */
    public void pairingProfile3(Map<String, String> oldFileNameMap, Map<String, String> newFileNameMap, Map<String, FileConfig> fieldSettingList, CompareSetting setting) {
        //台銀原檔案路徑
        LogProcess.info(log,"oldFileNameList = " + oldFileNameMap);
        LogProcess.info(log,"newFileNameList = " + newFileNameMap);
        LogProcess.info(log,"fieldSettingList = " + fieldSettingList);
        LogProcess.info(log,"setting = " + setting);

        this.settings = setting;

        String oFilePath = "";
        String nFilePath = "";
        if (!oldFileNameMap.isEmpty() && !newFileNameMap.isEmpty()) {
            //以原始的檔案為主 去匹配 要比對的檔案
            for (Map.Entry<String, String> o : oldFileNameMap.entrySet()) {

                String fileName = o.getKey();
                String tmpFileName = textFileUtil.replaceDateWithPlaceholder(fileName);


                if (newFileNameMap.get(fileName) != null) {
                    LogProcess.info(log,"file name = " + fileName);

                    FileConfig thisFileConfig = fieldSettingList.get(tmpFileName);

                    List<String> dataKeyList = thisFileConfig.getPrimaryKeys();
                    LogProcess.info(log,"dataKey = " + dataKeyList);

                    List<SortFieldConfig> thisSortFieldConfig = thisFileConfig.getSortFields();

//                    LogProcess.info("sortOrderMap = " + thisSortFieldConfig);
                    tmpFileName = tmpFileName.replace(".txt", "");
                    oFilePath = FilenameUtils.normalize(o.getValue());
                    nFilePath = FilenameUtils.normalize(newFileNameMap.get(o.getKey()));

//                    LogProcess.info("oFilePath = " + oFilePath);
//                    LogProcess.info("nFilePath = " + nFilePath);
                    //確認檔案名稱 是否存在定義檔
                    if (tmpXmlFileName.stream().anyMatch(tmpFileName::equals)) {
                        //原始檔案
                        pairingProfile2(oFilePath, TEXTAREA_1);
                        //比對檔案
                        pairingProfile2(nFilePath, TEXTAREA_2);
//                        LogProcess.info("aFileData name = " + aFileData);
//                        LogProcess.info("bFileData name = " + bFileData);
                        LogProcess.info(log,"columnList name = " + columnAllList);
                        //開始比對
                        compareDataService.parseData(aFileData, bFileData, dataKeyList, columnAllList, thisSortFieldConfig, maskUtil.removePrimaryKeysFromMaskKeys(maskFieldList, dataKeyList), headerBodyMode);

                        //執行結果
                        compareFileExportImpl.run(fileName, compareDataService.getOldDataResult(), compareDataService.getNewDataResult(), compareDataService.getComparisonResult(), compareDataService.getMissingData(), compareDataService.getExtraData(), setting);
                    }

                }


            }
        }

        //最後輸出執行結果檔案
        compareResultRpt.exec(compareFileExportImpl.outputResultRpt, compareFileExportImpl.dateTimeStr, compareFileExportImpl.dateTimeStr2);
        compareFileExportImpl.init();

    }

    @Override
    public void processPairingColumn(String fileName) {

        //每次觸發 須將欄位初始化
        columnList = new ArrayList<>();
        dataKey = new ArrayList<>();
        fileName = fileName.replace(".txt", "");
        try {
            existflag = false;
            for (XmlData data : xmlDataList) {
                //先將檔案名稱中，有日期先轉為yyyymmdd
                if (textFileUtil.replaceDateWithPlaceholder(fileName).equals(data.getFileName())) {
                    existflag = true;
                    //為了取得 PK 以及 欄位
                    performMasking("", data, "");
                }
            }
        } catch (Exception e) {
            LogProcess.info(log,"processPairingColumn error");
        }

    }


    /**
     * 取得指定資料夾內的所有檔案名稱
     *
     * @param folderPath 資料夾路徑
     * @return List<String> 回傳資料夾清單
     */
    private List<String> getFilePaths(String folderPath) {
        List<String> sqlFilePaths = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(folderPath), 1)) { // 只讀取第一層檔案
            sqlFilePaths = paths.filter(Files::isRegularFile).filter(path -> {
                FilenameUtils.normalize(path.toString());
                return true;
            }).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            LogProcess.info(log,"Error reading SQL files");
        }
        return sqlFilePaths;
    }

    /**
     * 執行遮蔽資料處理(蔽用於遮蔽程式使用)
     *
     * @param fileName 讀取txt檔案(含路徑)
     * @param xmlData  定義檔內容
     * @return List<String> 輸出內容
     */

    public List<String> performMasking(String fileName, XmlData xmlData, String textArea) {

        List<String> result = new ArrayList<>();

        try {
            // 解析 XML 檔案格式
            xmlFieldList_H = xmlData.getHeader().getFieldList();
            xmlFieldList_B = xmlData.getBody().getFieldList();
//            xmlFieldList_F = xmlData.getBody().getFieldList();

            //為蒐集表頭及內容欄位
            columnAllList = new ArrayList<>();

            headerMode = false;
            bodyMode = false;
            headerBodyMode = false;

            // 檢查 header 和 body 是否同時存在且不一樣
            if (!xmlFieldList_H.isEmpty()) {
                headerMode = true;
            }

            if (!xmlFieldList_B.isEmpty()) {
                bodyMode = true;
            }
            //若header 與 body都存在，header只會有第一筆，其餘為body
            if (headerMode && bodyMode) {
                headerBodyMode = true;
            }
            LogProcess.info(log,"headerMode = {}",headerMode);
            LogProcess.info(log,"bodyMode = {}",bodyMode);
            LogProcess.info(log,"headerBodyMode = {}",headerBodyMode);

            // header處理
            if (headerMode) {
                //如果表頭有資料，內容沒有，表示以表頭當資料源
                if (!bodyMode) {
                    xmlColumnToMap(xmlFieldList_H, textArea);
                }
                if (!fileName.isEmpty()) {
                    result.addAll(processFileData(fileName, xmlFieldList_H));
                }
            }

            // body處理
            if (bodyMode) {
                xmlColumnToMap(xmlFieldList_B, textArea);
                if (!fileName.isEmpty()) {
                    result.addAll(processFileData(fileName, xmlFieldList_B));
                }
            }
        } catch (Exception e) {
            LogProcess.error(log,"performMasking error",e);
        }

        return result;
    }


    private boolean xmlColumnToMap(List<XmlField> xmlFieldList, String uiTextArea) {
        Map<String, String> map = new LinkedHashMap<>();

        List<String> tmpList = new ArrayList<>();
        maskFieldList = new ArrayList<>();

//        tmpXmlFieldList = new ArrayList<>();
//        tmpXmlFieldList.addAll(xmlFieldList);

        for (XmlField xmlField : xmlFieldList) {
            String fieldName = xmlField.getFieldName();

            //略過分隔符號
            if ("separator".equals(fieldName)) {
                continue;
            }
            map.put(fieldName, fieldName);
            tmpList.add(fieldName);

            if (xmlField.getMaskType() != null) {
                maskFieldList.add(fieldName);
            }

            if (fieldName.isEmpty()) {
                fieldName = "filler";
            }
            //蒐集表頭及內容的欄位
            columnAllList.add(fieldName);

        }
        //
        if (TEXTAREA_2.equals(uiTextArea)) {
            columnBList = new ArrayList<>();
            columnBList.addAll(tmpList);

        } else if (TEXTAREA_1.equals(uiTextArea)) {
            columnAList = new ArrayList<>();
            columnAList.addAll(tmpList);
        } else {
            //讓ListView1和2隨時可取得
            columnList = new ArrayList<>();
            columnList.addAll(tmpList);
            return true;
        }

        if (columnAList.toString().equals(columnBList.toString())) {
            columnList = new ArrayList<>();
            columnList.addAll(tmpList);
        } else {
            columnList = new ArrayList<>();
        }

        cList.add(map);

        return true;

    }

    /**
     * 匹配單筆資料定義檔欄位，並將資料做遮蔽處理
     *
     * @param xmlFieldList 定義檔欄位
     * @param line         單筆資料串
     * @return 回傳遮罩後的資料
     */
    private String processField(List<XmlField> xmlFieldList, String line, int index) {
        Charset charset = Charset.forName("Big5");
        Charset charset2 = Charset.forName("UTF-8");

        Map<String, String> map = new LinkedHashMap<>();

        int xmlLength = 0;
        for (XmlField xmlField : xmlFieldList) {
            xmlLength = xmlLength + parse.string2Integer(xmlField.getLength());
        }

        byte[] bytes = line.getBytes(charset);
        int dataLength = bytes.length;

//        LogProcess.info("xmlFieldList :" + xmlFieldList );
        //先比對檔案資料長度是否與定義檔加總一致
        if (xmlLength != dataLength) {
            LogProcess.warn(log,"xml length = " + xmlLength + " VS data length = " + dataLength);
            LogProcess.warn(log,"line = {}" + line);
//            return line;
        }
        //起始位置
        int sPos = 0;
        StringBuilder s = new StringBuilder();

        //XML定義檔的格式
        for (XmlField xmlField : xmlFieldList) {

            //取得定義黨內的 欄位名稱、長度、遮蔽代號
            String fieledName = xmlField.getFieldName();
            int length = parse.string2Integer(xmlField.getLength());
            String maskType = xmlField.getMaskType();

            // 取得可以安全使用的 substring 結尾 index
            String remaining = line.substring(sPos);
            int safeCut = getSafeSubstringLength(remaining, length, charset);

            // 切出這個欄位字串
            String value = remaining.substring(0, safeCut);

            // 更新 char index 位置（要考慮已用掉的實際字元數）
            sPos += safeCut;

            //略過分隔符號
            if ("separator".equals(fieledName)) {
                s.append(value);
                continue;
            }

            //判斷有無遮蔽欄位
            if (!Objects.isNull(maskType)) {
                try {
                    //進行遮蔽處理
                    String valueMask = "";

                    if (this.settings.isExportUseMask()) {
                        valueMask = dataMasker.applyMask(value, maskType);
                    } else {
                        valueMask = value;
                    }
                    s.append(valueMask);
                    map.put(fieledName, valueMask);
                } catch (IOException ex) {
                    LogProcess.warn(log,"", ex);
                }
            } else {
                s.append(value);
                map.put(fieledName, value);
            }

        }

        cList.add(map);
        return s.toString();
    }

    /**
     * 匹配單筆資料定義檔欄位，並將資料做遮蔽處理
     *
     * @param fileName     檔案名稱(用於確認路徑)
     * @param xmlFieldList 定義檔欄位
     * @return 回傳遮罩後的資料
     */
    private List<String> processFileData(String fileName, List<XmlField> xmlFieldList) {
//        LogProcess.info("processFileData ...");
        // 讀取檔案內容
        List<String> lines = new ArrayList<>();

        //輸出資料
        List<String> outputData = new ArrayList<>();
        //計算筆數用
        int index = 0;
        int h = 0;
        int b = 0;
        fileName = FilenameUtils.normalize(fileName);
//        LogProcess.info("fileNamefileNamefileName = " +fileName);
//        LogProcess.info("fileNamefileNamefileName = " +fileName);
        // 確認檔案路徑 是否與 允許的路徑匹配
        int LINE_LENGTH = 0;
        for (XmlField field : xmlFieldList) {
            LINE_LENGTH += Integer.parseInt(field.getLength());
        }

        // 讀取檔案內容
        lines = textFileUtil.readFileContent(fileName, CHARSET_BIG5);

        for (String s : lines) {

            index++;

            if (headerBodyMode) {
                b++;

                if (headerMode) {
                    //表頭正常只會有一筆
//                        LogProcess.info("s1 =" + s);
//                        LogProcess.info("xmlFieldList = " +xmlFieldList);
                    outputData.add(processField(xmlFieldList_H, s, index));
                    headerMode = false;
                    break;
                }
                if (bodyMode) {
                    if (b > 1) {
//                        LogProcess.info("s2 =" + s);
//                        LogProcess.info("xmlFieldList = " +xmlFieldList);
                        outputData.add(processField(xmlFieldList_B, s, index));
                    }
                }
            } else {
                outputData.add(processField(xmlFieldList, s, index));
            }
//                LogProcess.info("line =" + s);

        }

        return outputData;

    }

    /**
     * 根據指定的 byte 長度與編碼，回傳可以安全 substring 的字元位置。
     *
     * @param str      要處理的字串
     * @param maxBytes 最多 byte 數
     * @param charset  使用的字元編碼（例如 "Big5"、"UTF-8"）
     * @return 回傳正確的位置截斷位置可用於 substring(0, result) 的字元位置
     */
    public int getSafeSubstringLength(String str, int maxBytes, Charset charset) {

        int currentBytes = 0;

        for (int i = 0; i < str.length(); i++) {
            String ch = str.substring(i, i + 1);
            int byteLen = ch.getBytes(charset).length;

            if (currentBytes + byteLen > maxBytes) {
                return i; // 回傳最後安全的 index
            }
            currentBytes += byteLen;
        }
        return str.length(); // 全部都沒超過就整段可以用
    }


    /**
     * [yyyymmdd]置換 核對是否同一個檔案
     */
    public boolean matchesPattern(String pattern, String fileName) {
        // 轉換 pattern:
        // 將 [yyyymmdd] → \d{8} 換成8碼(日期)
        String regex = pattern.replace("[yyyymmdd]", "\\\\d{8}");

        // 處理句點 (因為 . 在正則裡代表「任何字元」)
        regex = regex.replace(".", "\\\\.");

        // 3完整比對整個檔案名
        return fileName.matches(regex);
    }


}
