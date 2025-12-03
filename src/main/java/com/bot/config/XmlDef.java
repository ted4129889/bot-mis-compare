package com.bot.config;

import com.bot.mask.config.FileConfig;
import com.bot.util.log.LogProcess;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.vo.XmlData;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class XmlDef {

    @Value("${localFile.mis.batch.input}")
    private String inputPath;
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
    List<String> tmpXmlFileName = new ArrayList<>();
    public List<XmlData> xmlDataList = new ArrayList<>();
    public Map<String, FileConfig> jsonFile = new LinkedHashMap<>();
    private static final String COMPARISON_RESULT_FOLDER = "ComparisonResult";

    @PostConstruct
    public void init() {
        readDefinitionXml();
    }

    public void readDefinitionXml() {
        try {
            //允許的路徑(XML)
            //日批定義檔
            String dailyBatchFileDefinitionFile = FilenameUtils.normalize(botMaskXmlFilePath);
            //月批定義檔
            String monthlyBatchFileDefinitionFile = FilenameUtils.normalize(botMaskXmlFilePath2);

            //日批定義檔list
            List<XmlData> xmlDataListD;
            //月批定義檔list
            List<XmlData> xmlDataListM;
            //xml class
            XmlFile xmlFile;

            //解析XML儲存到list
            xmlFile = xmlParser.parseXmlRtnXmlFile(dailyBatchFileDefinitionFile);
            xmlDataListD = xmlFile.getDataList();
            xmlDataList.addAll(xmlDataListD);

            //解析XML儲存到list
            xmlFile = xmlParser.parseXmlRtnXmlFile(monthlyBatchFileDefinitionFile);
            xmlDataListM = xmlFile.getDataList();
            xmlDataList.addAll(xmlDataListM);

            LogProcess.info(log, "讀取 external-config/xml/file 內的定義檔，共有" + xmlDataList.size() + "組 <data> 格式");


            //儲存檔案名稱
            tmpXmlFileName =
                    xmlDataList.stream()
                            .map(XmlData::getFileName)
                            .toList();

            //允許的路徑(JSON)
            String fieldSettingFile = FilenameUtils.normalize(fieldSettinngFile);

            LogProcess.info(log, "config file is exist: " + fieldSettingFile);
            File allowedFile = new File(fieldSettingFile);

            ObjectMapper mapper = new ObjectMapper();
            jsonFile = mapper.readValue(allowedFile, new TypeReference<>() {
            });

            //先新增一個資料夾，放比對結果用
            File folder = new File(COMPARISON_RESULT_FOLDER);
            if (!folder.exists()) {
                boolean created = folder.mkdirs(); // 可建立多層路徑
                if (created) {
                    System.out.println("created folder：ComparisonResult");
                } else {
                    System.out.println("create fail");
                }
            }
            LogProcess.info(log, "xmlDataList List = {}", xmlDataList.size());

            LogProcess.info(log, "jsonFile List = {}", jsonFile.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
