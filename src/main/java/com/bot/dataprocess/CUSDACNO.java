package com.bot.dataprocess;


import com.bot.dto.CompareSetting;
import com.bot.service.compare.CompareDataService;
import com.bot.service.mask.DataFileProcessingService;
import com.bot.service.mask.config.FileConfig;
import com.bot.service.mask.config.SortFieldConfig;
import com.bot.service.output.CompareFileExportImpl;
import com.bot.service.output.templates.CompareResultRpt;
import com.bot.util.files.FileNameUtil;
import com.bot.util.files.TextFileUtil;
import com.bot.util.log.LogProcess;
import com.bot.util.mask.MaskUtil;
import com.bot.util.parse.Parse;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import com.bot.util.xml.vo.XmlFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component("CUSDACNO")
public class CUSDACNO implements Job {
    @Value("${localFile.mis.batch.output}")
    private String outputPath;
    @Value("${localFile.mis.xml.file_def}")
    private String botMaskXmlFilePath;
    @Value("${localFile.mis.xml.file_def2}")
    private String botMaskXmlFilePath2;
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
    private static final String CHARSET_BIG5 = "Big5";

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



    @Override
    public void execute() {

        log.info(" 開始執行 CUSDACNO Job 流程");

        // 實際邏輯放這裡
        // 例如讀檔案、處理資料、匯出報表等
        try {
            Thread.sleep(1000); // 模擬運行
            log.info("CUSDACNO Job 執行完成");
        } catch (InterruptedException e) {
            log.error("執行 CUSDACNO Job 發生錯誤", e);
        }

        //TODO 先拿到檔案路徑=>
        //1.拆檔案:以brno 開頭拆檔案，假設遇到
//        try (BufferedReader br =
//                     Files.newBufferedReader(
//                             Paths.get()),
//                             Charset.forName(CHARSET_BIG5))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                lineNo++;
//                if (line == null) continue;
//                line = line.trim();
//                if (line.isEmpty()) continue;
//            }
        }

}
