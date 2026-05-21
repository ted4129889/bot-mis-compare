package com.bot.reader;

import com.bot.domain.FieldDef;
import com.bot.domain.RowData;
import com.bot.util.log.LogProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class LineParser {

    static boolean show = false;

    private String detectedSeparator;
    private boolean useSplitMode;

    public static void resetDetection() {
        // Kept for backward compatibility. New code should create one LineParser per compare run.
    }

    public void reset() {
        detectedSeparator = null;
        useSplitMode = false;
    }

    private static String commonReplace(String line) {
//        return line.replaceAll("[☆□]", "?").replaceAll("[*?]", " ").replaceAll("\"", " ");
        return line;
    }

    public static String detectSeparator(String line, List<FieldDef> defs, String separator) {
        return new LineParser().detectSeparatorForFile(line, defs, separator);
    }

    public String detectSeparatorForFile(String line, List<FieldDef> defs, String separator) {
        if (detectedSeparator != null) {
            return detectedSeparator;
        }

        String configuredSeparator = separator == null ? "" : separator;
        Charset charset = Charset.forName("MS950");

        line = commonReplace(line);

        LogProcess.info(log, "Detect parser mode from first data line = {}", line);

        byte[] lineBytes = line.getBytes(charset);

        //初始位置
        int sPos = 0;

        for (FieldDef def : defs) {
            int length = def.getLength();

            byte[] fieldBytes = Arrays.copyOfRange(lineBytes, sPos, sPos + length);
            String value = new String(fieldBytes, charset).trim();

            LogProcess.info(log, "separator = {}", value);
            if (def.getName().contains("separator")) {
                LogProcess.info(log, "configured separator = {}", separator);
                if (!configuredSeparator.isBlank()) {
                    detectedSeparator = value;
                    useSplitMode = true;
                    LogProcess.info(log, "use parseLineBySplit 1");
                } else if (",".equals(value) || "$".equals(value) || configuredSeparator.equals(value)) {
                    detectedSeparator = value;
                    useSplitMode = true;
                    LogProcess.info(log, "use parseLineBySplit 2");
                } else {
                    detectedSeparator = "";
                    useSplitMode = false;
                    LogProcess.info(log, "use parseLineByFixed1");
                    return "";
                }

                return value;
            }
            sPos += length;
        }

        detectedSeparator = "";
        useSplitMode = false;
        LogProcess.info(log, "use parseLineByFixed2");
        return "";
    }

    public static RowData parseLine(String line, List<FieldDef> defs, String separator) {
        return new LineParser().parse(line, defs, separator);
    }

    public RowData parse(String line, List<FieldDef> defs, String separator) {
        detectSeparatorForFile(line, defs, separator);

        if (useSplitMode) {
            return parseLineBySplit(line, defs, detectedSeparator);
        } else {
            return parseLineByFixed(line, defs);
        }
    }


    /**
     * 使用固定長度處理字串
     *
     * @param line 資料行
     * @param defs 定義檔欄位(List<FieldDef>)
     * @return RowData
     *
     */
    public static RowData parseLineByFixed(String line, List<FieldDef> defs) {
        Map<String, String> fieldMap = new HashMap<>();
        StringBuilder fullBuilder = new StringBuilder();
        StringBuilder keyGroup = new StringBuilder();
        show = false;
        Charset charset = Charset.forName("MS950");

        if (line.isEmpty()) return null;

        line = commonReplace(line);
        byte[] lineBytes = line.getBytes(charset);

        int sPos = 0;
        try {
            for (FieldDef def : defs) {
                int length = def.getLength();
                byte[] fieldBytes = Arrays.copyOfRange(lineBytes, sPos, sPos + length);
                String value = new String(fieldBytes, charset);

                fieldMap.put(def.getName().trim(), value);

                if (def.isKey()) {
                    keyGroup.append(def.getName()).append("=").append(value.trim()).append(",");
                }

                fullBuilder.append(value);
                sPos += length;
            }

            String kg = keyGroup.length() > 0 ? keyGroup.substring(0, keyGroup.length() - 1) : "";
            return new RowData(line, kg, hash(kg), hash(fullBuilder.toString()), fieldMap);
        } catch (Exception e) {
            LogProcess.error(log, "error lines = {}", line, e);
            throw e;
        }
    }


    /**
     * 使用分隔符號處理字串
     *
     * @param line      資料行
     * @param defs      定義檔欄位(List<FieldDef>)
     * @param separator 分隔符號
     * @return RowData
     *
     */
    public static RowData parseLineBySplit(String line, List<FieldDef> defs, String separator) {
        Map<String, String> fieldMap = new HashMap<>();
        StringBuilder fullBuilder = new StringBuilder();
        StringBuilder keyGroup = new StringBuilder();

        if (line.isEmpty()) return null;

        line = commonReplace(line);

        Charset charset = Charset.forName("MS950");
        String regex = Pattern.quote(separator);
        String[] lines = line.split(regex, -1);

        int idx = 0;
        int length = 0;
        StringBuilder fieldName = new StringBuilder();
        boolean isKey = false;

        String resultKey;
        int keyLen;
        try {
            for (FieldDef def : defs) {
                if (!def.getName().contains("separator")) {
                    fieldName.append(def.getName()).append("+");
                    length += def.getLength();

                    if (def.isKey()) {
                        isKey = true;
                    }
                }

                // 遇到 separator 時候才開始紀錄前面欄位(才算1位)
                if (def.getName().contains("separator")) {
                    String value = lines[idx].trim();
                    idx++;

                    // 依照欄位長度補滿
                    value = padToByteLength(value, length, charset);

                    resultKey = fieldName.toString().trim();
                    keyLen = resultKey.length();
                    resultKey = resultKey.substring(0, keyLen - 1);

                    // 放入 map
                    fieldMap.put(resultKey, value);

                    // full hash
                    fullBuilder.append(value);

                    // key hash
                    if (isKey) {
                        keyGroup.append(resultKey).append(" = ").append(value.trim()).append(",");
                    }
//                    LogProcess.debug(log, "fieldMap = {}", fieldMap );

                    isKey = false;
                    length = 0;
                    fieldName = new StringBuilder();
                }
            }
            // 取出 value
            String value = lines[idx].trim();

            // 依照欄位長度補滿
            value = padToByteLength(value, length, charset);

            // 放入 map
            fieldMap.put(fieldName.toString().trim(), value);

            // full hash
            fullBuilder.append(value);


            // key hash
            if (isKey) {
                keyGroup.append(fieldName).append("=").append(value.trim()).append(",");
            }

            String kg = keyGroup.length() > 0 ? keyGroup.substring(0, keyGroup.length() - 1) : "";
            return new RowData(line, kg, hash(kg), hash(fullBuilder.toString()), fieldMap);
        } catch (Exception e) {
            LogProcess.error(log, "error lines = {}", Arrays.toString(lines), e);
            throw e;
        }
    }

    private static String hash(String s) {
        return DigestUtils.md5Hex(s);
    }

    /**
     * 根據指定的 byte 長度與編碼，回傳可以安全 substring 的字元位置。
     *
     * @param str      要處理的字串
     * @param maxBytes 最多 byte 數
     * @return 回傳正確的位置截斷位置可用於 substring(0, result) 的字元位置
     */
    public static int getSafeSubstringLength(String str, int maxBytes) {
        Charset charset = Charset.forName("MS950");
        byte[] bytes = str.getBytes(charset);

        //實際字串長度
        int bytesLen = bytes.length;
//        if (show) LogProcess.info(log, "bytesLen = {}", bytesLen);

        int currentBytes = 0;
        for (int i = 0; i < str.length(); i++) {
            String ch = str.substring(i, i + 1);
            int byteLen = ch.getBytes(charset).length;

            // 若超過定義好的長度，回傳安全的 index
            if (currentBytes + byteLen > maxBytes) {
                return i;
            }
            currentBytes += byteLen;
        }

        str = str + " ".repeat(maxBytes - str.length());
        // 全部都沒超過就整段可以用
        return str.length();
    }

    public static String replaceUnmappableChar(String input) {
        CharsetEncoder encoder = Charset.forName("MS950").newEncoder();
        StringBuilder sb = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (encoder.canEncode(c)) {
                sb.append(c);
            } else {
                sb.append("?");
            }
        }
        return sb.toString();
    }

    /**
     * 補長度
     *
     */
    private static String padToByteLength(String value, int byteLength, Charset charset) {
        if (value == null) value = "";

        byte[] bytes = value.getBytes(charset);

        if (bytes.length >= byteLength) {
            return new String(Arrays.copyOf(bytes, byteLength), charset);
        }

        // 需要補空白
        int padSize = byteLength - bytes.length;
        StringBuilder sb = new StringBuilder(value);

        for (int i = 0; i < padSize; i++) {
            sb.append(" "); // 半形空白
        }

        return sb.toString();
    }
}
