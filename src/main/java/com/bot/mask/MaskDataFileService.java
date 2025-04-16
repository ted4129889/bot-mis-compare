package com.bot.mask;


import com.bot.filter.CheakSafePathUtil;
import com.bot.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.parse.Parse;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import com.bot.util.xml.vo.XmlFile;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MaskDataFileService {
    @Value("${localFile.mis.batch.bot_output}")
    private String botOutputPath;

    @Value("${localFile.mis.batch.output}")
    private String outputPath;

    @Value("${localFile.mis.xml.output.bot_directory}")
    private String botMaskXmlFilePath;
    @Autowired
    private XmlParser xmlParser;
    @Autowired
    private Parse parse;
    @Autowired
    private TextFileUtil textFileUtil;
    @Autowired
    private DataMasker dataMasker;
    private static final String CHARSET_BIG5 = "Big5";
    private static final String CHARSET_UTF8 = "UTF-8";

    public boolean exec() {
        return exec("","","");
    }

    //    功能雷同 但是中間可能會有稍微不同做法
    public boolean exec(String condition,String cNewPath,String cOldPath) {
        LogProcess.info("執行資料檔案遮蔽處理...");
        //允許的路徑
        String tbotOutputPath = FilenameUtils.normalize(botOutputPath);
        String tbotMaskXmlFilePath = FilenameUtils.normalize(botMaskXmlFilePath + "DailyBatchFileDefinition.xml");

        XmlFile xmlFile;
        List<XmlData> xmlDataList = new ArrayList<>();
        try {
            LogProcess.info("output def file = " + tbotMaskXmlFilePath);
            xmlFile = xmlParser.parseXmlFile2(tbotMaskXmlFilePath);

            xmlDataList = xmlFile.getDataList();

        } catch (IOException e) {
            throw new RuntimeException(e);

        }
        LogProcess.info("讀取 external-config/xml/bot_output 資料夾下的 DailyBatchFileDefinition.xml 定義檔內有" + xmlDataList.size() + "組 <data> 格式");
        //C = compare,M = mask  => both of DATA PROCESS
        if ("C".equals(condition)) {
            //TODO 取得新舊資料，兩個檔都要弄成Map，比對，(外加的是 可以顯示比對欄位
            pairingProfile2(xmlDataList, tbotOutputPath);
        } else {
            pairingProfile(xmlDataList, tbotOutputPath);
        }
        return true;
    }

    private void pairingProfile(List<XmlData> xmlDataList, String tbotOutputPath) {
        int calcuTotal = 0;
        //台銀原檔案路徑
        List<String> folderList = getFilePaths(tbotOutputPath);
        LogProcess.info("在batch-file/bot_output資料夾內的檔案有" + folderList.size() + "個，清單如下...");
        LogProcess.info(folderList.toString());
        for (String requestedFilePath : folderList) {
            //允許路徑
            requestedFilePath = FilenameUtils.normalize(requestedFilePath);
            try {


                for (XmlData data : xmlDataList) {
                    if (requestedFilePath.contains(data.getFileName())) {

                        LogProcess.info("bot_output file name = " + requestedFilePath);

                        List<String> outputData = performMasking(requestedFilePath, data);

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
                LogProcess.info("XmlToInsertGenerator.sqlConvInsertTxt error");
            }
        }

        LogProcess.info("產出遮蔽後的檔案在 batch-file/bot_output_mask 資料夾,有" + calcuTotal + "個檔案");
    }


    private void pairingProfile2(List<XmlData> xmlDataList, String tbotOutputPath) {
        int calcuTotal = 0;
        //台銀原檔案路徑
        List<String> folderList = getFilePaths(tbotOutputPath);
        LogProcess.info("在batch-file/bot_output資料夾內的檔案有" + folderList.size() + "個，清單如下...");
        LogProcess.info(folderList.toString());
        for (String requestedFilePath : folderList) {
            //允許路徑
            requestedFilePath = FilenameUtils.normalize(requestedFilePath);
            try {


                for (XmlData data : xmlDataList) {
                    if (requestedFilePath.contains(data.getFileName())) {

                        LogProcess.info("bot_output file name = " + requestedFilePath);

                        List<String> outputData = performMasking(requestedFilePath, data);

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
                LogProcess.info("XmlToInsertGenerator.sqlConvInsertTxt error");
            }
        }

        LogProcess.info("產出遮蔽後的檔案在 batch-file/bot_output_mask 資料夾,有" + calcuTotal + "個檔案");
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
            sqlFilePaths = paths.filter(Files::isRegularFile).filter(path -> FilenameUtils.normalize(path.toString()).toLowerCase().endsWith(".txt")).map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            LogProcess.info("Error reading SQL files");
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

    public List<String> performMasking(String fileName, XmlData xmlData) {

        List<String> result = new ArrayList<>();

        try {
            // 解析XML檔案格式

            // header處理...
            List<XmlField> xmlFieldList = xmlData.getHeader().getFieldList();
            if (!xmlFieldList.isEmpty()) {
                result.addAll(processFileData(fileName, xmlFieldList));
            }

            // body處理...
            xmlFieldList = xmlData.getBody().getFieldList();
            if (!xmlFieldList.isEmpty()) {
                result.addAll(processFileData(fileName, xmlFieldList));
            }
        } catch (Exception e) {
            LogProcess.info("XmlToReadFile.exec error");
        }

        return result;
    }

    /**
     * 匹配單筆資料定義檔欄位，並將資料做遮蔽處理
     *
     * @param xmlFieldList 定義檔欄位
     * @param line         單筆資料串
     * @return 回傳遮罩後的資料
     */
    private String processField(List<XmlField> xmlFieldList, String line) {
        Charset charset = Charset.forName("Big5");
        Charset charset2 = Charset.forName("UTF-8");

        int xmlLength = 0;
        for (XmlField xmlField : xmlFieldList) {
            xmlLength = xmlLength + parse.string2Integer(xmlField.getLength());
        }
        byte[] bytes = line.getBytes(charset);
        int dataLength = bytes.length;


        //先比對檔案資料長度是否與定義檔加總一致
        if (xmlLength != dataLength) {
            LogProcess.info("xml length = " + xmlLength + " VS data length = " + dataLength);
            return line;
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
                    String valueMask = dataMasker.applyMask(value, maskType);
                    LogProcess.info("maskType =" + maskType + ",value = \"" + value + "\" masked result => \"" + valueMask + "\"");
                    s.append(valueMask);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                s.append(value);
            }
        }
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

        String allowedPath = FilenameUtils.normalize(botOutputPath);

        // 確認檔案路徑 是否與 允許的路徑匹配
        if (CheakSafePathUtil.isSafeFilePath(allowedPath, fileName)) {
            // 讀取檔案內容
            lines = textFileUtil.readFileContent(fileName, CHARSET_BIG5);
            for (String s : lines) {
//                LogProcess.info("line =" + s);
                outputData.add(processField(xmlFieldList, s));
            }
        } else {
            LogProcess.info("not allowed to read  = " + fileName);
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

}
