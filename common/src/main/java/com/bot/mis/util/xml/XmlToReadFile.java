/* (C) 2024 */
package com.bot.mis.util.xml;

import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.xml.config.SecureXmlMapper;
import com.bot.mis.util.xml.vo.XmlData;
import com.bot.mis.util.xml.vo.XmlField;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import com.bot.txcontrol.util.parse.Parse;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public class XmlToReadFile {

    @Value("${localFile.mis.batch.output}")
    private String fileDir;

    @Value("${localFile.mis.xml.input.directory}")
    private String xmlInputFileDir;

    @Value("${localFile.mis.batch.input}")
    private String inputFileDir;

    @Autowired private TextFileUtil textFileUtil;
    @Autowired private Parse parse;
    private static final String CHARSET_BIG5 = "BIG5";
    private final String CHARSET_UTF8 = "UTF-8";
    private final String STR_SEPARATOR = "separator";
    private static final String TXT_EXTEMSION = ".txt";
    private static final String XML_EXTEMSION = ".xml";
    private final String INPUT_FILE_DETAIL = "D";
    private final String INPUT_FILE_HEADER = "H";
    private final String BOTTON_LINE = "_";
    private final String COMMA = ",";

    /**
     * 讀xml.input的欄位設定檔與TXT資料檔 對應成 List<Map<String, String>> 格式<br>
     *
     * @param fileName 檔案名稱(XML與TXT同名)
     * @return List<Map < String, String>> 查询结果的列表
     */
    public List<Map<String, String>> exec(String fileName) {

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "XmlToReadFile exec...");

        List<Map<String, String>> tmpList = new ArrayList<>();
        try {
            // XML檔案路徑(讀取)
            String xml = xmlInputFileDir + fileName + XML_EXTEMSION;

            // 解析XML檔案格式
            XmlMapper xmlMapper = SecureXmlMapper.createXmlMapper();

            File file = new File(xml);
            XmlData xmlData = xmlMapper.readValue(file, XmlData.class);

            // 取得XML定義好的欄位規格
            List<XmlField> fieldList = xmlData.getTxt().getFieldList();
            String fileType = "";

            // 一個檔案有表頭跟內容不同格式
            if (fileName.toUpperCase().contains(BOTTON_LINE + INPUT_FILE_DETAIL)
                    || fileName.toUpperCase().contains(BOTTON_LINE + INPUT_FILE_HEADER)) {
                fileType = fileName.split(BOTTON_LINE)[1];
                fileName = fileName.split(BOTTON_LINE)[0];
            }

            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileName = {}", fileName);

            // TXT檔案路徑 讀取
            String txt = inputFileDir + fileName + TXT_EXTEMSION;
            // 讀取檔案內容
            List<String> lines = textFileUtil.readFileContent(txt, CHARSET_BIG5);

            if (INPUT_FILE_DETAIL.equals(fileType)) {
                // 保留第一筆
                if (!lines.isEmpty()) {
                    lines = lines.subList(0, 1);
                }
            } else if (INPUT_FILE_HEADER.equals(fileType)) {
                // 保留第一筆以外的資料
                if (lines.size() > 1) {
                    lines = lines.subList(1, lines.size());
                } else {
                    lines = new ArrayList<>(); // 若只有一筆或空，回傳空的 List
                }
            } else {
                // 其他情況，不做處理
            }

            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "lines = {}", lines);

            int splitCnt = 0;
            int xmlFieldCnt = 0;
            boolean haveComma = false;
            // 有無逗號判斷使用split或是substring
            // 確認資料內容有沒有使用逗號
            if (!lines.isEmpty()) {
                int c = 0;

                for (String s : lines) {
                    if (s.contains(COMMA)) {
                        haveComma = true;
                    }
                    c++;
                }
            }
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "splitCnt = {}", splitCnt);
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "haveComma = {}", haveComma);
            for (XmlField f : fieldList) {

                if (f.getFieldName().equalsIgnoreCase(STR_SEPARATOR)) {
                    continue;
                }
                xmlFieldCnt++;
            }
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "xmlFieldCnt = {}", xmlFieldCnt);
            Map<String, String> map;

            // 判斷有無區隔符號
            if (!haveComma) {
                for (String s : lines) {
                    int tCnt = 0;

                    int index = 0;
                    int start = 0;
                    int end = 0;
                    ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "s = {}", s);
                    map = new LinkedHashMap<>();

                    for (XmlField f : fieldList) {
                        start = index == 0 ? 0 : end;
                        end = end + parse.string2Integer(f.getLength());
                        if (f.getFieldName().equalsIgnoreCase(STR_SEPARATOR)) {
                            continue;
                        }
                        ApLogHelper.info(
                                log,
                                false,
                                LogType.NORMAL.getCode(),
                                "before col = {},sLen = {},eLen = {},value ={}",
                                f.getFieldName(),
                                start,
                                end,
                                s.substring(start, end));

                        tCnt = 0;
                        for (char c : s.toCharArray()) {

                            if (isFullWidth(c)) {
                                tCnt = tCnt + 2;
                            } else {
                                tCnt = tCnt + 1;
                            }
                            if (start < tCnt && tCnt < end && (isFullWidth(c))) {
                                end = end - 1;
                            }
                            tCnt++;
                        }
                        ApLogHelper.info(
                                log,
                                false,
                                LogType.NORMAL.getCode(),
                                "after col = {},sLen = {},eLen = {},value ={}",
                                f.getFieldName(),
                                start,
                                end,
                                s.substring(start, end));

                        map.put(f.getFieldName(), s.substring(start, end));

                        index++;
                    }

                    tmpList.add(map);
                }
            } else {
                String[] r;
                for (String s : lines) {
                    ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "s = {}", s);
                    int index = 0;
                    // 分組
                    r = s.split(COMMA);
                    // 確認資料長度
                    ApLogHelper.info(
                            log, false, LogType.NORMAL.getCode(), "split.length = {}", r.length);
                    if (xmlFieldCnt == r.length) {
                        Map<String, String> m = new LinkedHashMap<>();
                        for (XmlField f : fieldList) {

                            if (f.getFieldName().equalsIgnoreCase(STR_SEPARATOR)) {
                                continue;
                            } else {
                                m.put(f.getFieldName(), r[index]);
                            }
                            index++;
                        }
                        tmpList.add(m);
                    }
                }
            }

        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("", "XmlToReadFile.exec error");
        }

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "tmpList = {}", tmpList);

        return tmpList;
    }

    /** 判斷是否為全行字或中文字 */
    public boolean isFullWidth(char ch) {
        return (ch >= 0xFF00 && ch <= 0xFFEF)
                || // Fullwidth and Halfwidth Forms
                (ch >= 0x4E00 && ch <= 0x9FFF); // CJK Unified Ideographs
    }
}
