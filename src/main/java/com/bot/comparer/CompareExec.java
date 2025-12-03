package com.bot.comparer;

import com.bot.config.XmlDef;
import com.bot.domain.FieldDef;
import com.bot.domain.RowData;
import com.bot.output.templates.CompareResultBean;
import com.bot.reader.FileParser;
import com.bot.mask.config.FileConfig;
import com.bot.util.log.LogProcess;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CompareExec {

    @Autowired
    XmlDef xmlDef;
    @Autowired
    CompareExecService compareExecService;

    public CompareResultBean exec(String pathA ,String pathB,String fileName,String fileType) {

        //初始化，定義檔資料載入
//        xmlDef.readDefinitionXml();
        //取得檔案定義檔
        List<XmlData> xmlDataList = xmlDef.xmlDataList;
        //取得檔案設定檔
        Map<String, FileConfig> jsonFile = xmlDef.jsonFile;

        //取得定義檔案的內容
        String finalFileName1 = fileName;
        XmlData xmlData = xmlDataList.stream()
                .filter(v -> finalFileName1.equals(v.getFileName()))
                .findFirst()
                .orElse(null);

        //取得欄位訊息
        List<XmlField> fieldList = xmlData.getBody().getFieldList();

        //檔案設定(key或sort)
        FileConfig cfg = jsonFile.getOrDefault(fileName, null);

        //取得key
        List<String> keyList = cfg.getPrimaryKeys();

        LogProcess.info(log, "fieldList List = {}", fieldList);
        LogProcess.info(log, "key List = {}", keyList);

        //
        List<FieldDef> defs = convert(fieldList, keyList);
        LogProcess.info(log, "defs List = {}", defs);


        FileParser parser = new FileParser(defs);
        Map<String, RowData> mapA = null;

        LogProcess.info(log, "FileParser parser = {}", parser);
        LogProcess.info(log, "pathA = {}", Paths.get(pathA));
        LogProcess.info(log, "pathB = {}", Paths.get(pathB));

        fileName = fileName.replace(".txt", "");

        CompareResultBean compareResultBean = new CompareResultBean();
        try {

            compareResultBean = compareExecService.compare(Path.of(pathA), Path.of(pathB), defs,fileName,fileType);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return compareResultBean;
    }

    private List<FieldDef> convert(List<XmlField> fieldList, List<String> keyList) {
        List<FieldDef> result = new ArrayList<>();

        //計算重複欄位用
        Map<String, Integer> nameCount = new HashMap<>();

        // 起始位置累積用
        int pos = 0;

        for (XmlField f : fieldList) {

            String rawName = f.getFieldName();

            // 計算欄位出現次數
            int count = nameCount.getOrDefault(rawName, 0) + 1;
            nameCount.put(rawName, count);

            // 如果有重複的name，在後面加上流水號
            String finalName = (count == 1) ? rawName : rawName + count;

            boolean isPK = keyList.contains(rawName);

            result.add(new FieldDef(
                    finalName,
                    pos,
                    Integer.parseInt(f.getLength()),
                    isPK
            ));

            pos += Integer.parseInt(f.getLength());
        }
        return result;
    }


}
