/* (C) 2024 */
package com.bot.mis.util.xml;

import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.xml.config.SecureXmlMapper;
import com.bot.mis.util.xml.vo.XmlBody;
import com.bot.mis.util.xml.vo.XmlData;
import com.bot.mis.util.xml.vo.XmlHeader;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import com.bot.txcontrol.util.parse.Parse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class XmlToWriteFile {
    @Value("${localFile.mis.batch.output}")
    private String fileDir;

    @Value("${localFile.mis.xml.output.directory}")
    private String xmlOutputFileDir;

    @Autowired private Parse parse;
    @Autowired private TextFileUtil textFileUtil;
    @Autowired private DataProcess dataProcess;
    private static final String CHARSET = "BIG5";
    private static final String TXT_EXTEMSION = ".txt";
    private static final String XML_EXTEMSION = ".xml";
    private static final String STR_YYYYMMDD = "[yyyymmdd]";
    private static final String NO_DATA = "no data";

    public XmlToWriteFile() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    /**
     * 執行Xml產檔案(資料來源使用SQL查詢)
     *
     * @param fileName 檔案名稱
     * @param batchDate 批次日期(執行日期)
     * @return List<String>
     */
    public List<String> exec(String fileName, int batchDate) {
        return exec(fileName, batchDate, new ArrayList<>());
    }

    /**
     * 執行Xml產檔案 (資料來源使用TXT檔案)
     *
     * @param fileName 檔案名稱
     * @param batchDate 批次日期(執行日期)
     * @param dataList 查詢資料庫資料
     * @return List<String>
     */
    public List<String> exec(String fileName, int batchDate, List<Map<String, String>> dataList) {
        //        需要一個Object[]陣列是放值，xmlData是欄位的東西
        List<String> fileContents = new ArrayList<>();

        boolean isUseSqlData = true;

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "txtToFile exec()");

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileName = {}", fileName);
        try {
            String xml = xmlOutputFileDir + fileName + XML_EXTEMSION;
            String txt = fileDir + fileName + TXT_EXTEMSION;

            if (txt.contains(STR_YYYYMMDD)) {
                txt = txt.replace(STR_YYYYMMDD, String.valueOf(batchDate));
            }
            File file = new File(xml);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + xml);
            }
            // 解析XML檔案格式
            XmlMapper xmlMapper = SecureXmlMapper.createXmlMapper();

            XmlData xmlData = xmlMapper.readValue(file, XmlData.class);
            // 表頭標籤
            XmlHeader header = xmlData.getHeader();
            // 內容標籤
            XmlBody body = xmlData.getBody();
            // 是否使用sql語法
            isUseSqlData = header.isUseSqlData();

            // 開始header處理...
            if (!header.getFieldList().isEmpty()) {

                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "headertmpList = {}", dataList);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "isUseSqlData = {}", isUseSqlData);

                fileContents.addAll(
                        dataProcess.exec(
                                dataList,
                                header.getFieldList(),
                                isUseSqlData,
                                header.getSeparator(),
                                batchDate));
            }

            // body處理...
            isUseSqlData = body.isUseSqlData();
            if (!body.getFieldList().isEmpty()) {

                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "bodytmpList = {}", dataList);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "isUseSqlData = {}", isUseSqlData);
                fileContents.addAll(
                        dataProcess.exec(
                                dataList,
                                body.getFieldList(),
                                isUseSqlData,
                                body.getSeparator(),
                                batchDate));
            }

            if (!fileContents.isEmpty()) {
                writeFile(fileContents, txt);
            } else {
                fileContents.add(NO_DATA);
                writeFile(fileContents, txt);
                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "{} output data is null", fileName);
            }
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToWriteFile.exec error");
        }

        return fileContents;
    }

    /**
     * 輸出檔案
     *
     * @param fileContents 資料串
     * @param outFileName 輸出檔案名
     */
    private void writeFile(List<String> fileContents, String outFileName) {

        textFileUtil.deleteFile(outFileName);

        try {
            textFileUtil.writeFileContent(outFileName, fileContents, CHARSET);
        } catch (LogicException e) {
            moveErrorResponse(e);
        }
    }

    private void moveErrorResponse(LogicException e) {
        //        event.setPeripheryRequest();
    }
}
