/* (C) 2023 */
package com.bot.util.files;

import com.bot.util.log.LogProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Scope("prototype")
public class TextFileUtil {
    @Value("${app.file-processing.max-lines}")
    private int maxLines;

    @Value("${app.file-processing.timeout}")
    private long timeout;

    public TextFileUtil() {
        // YOU SHOULD USE @Autowired ,NOT new TextFileUtil()
    }


    /**
     * Reads the contents of the specified file and returns it as a list of strings, with each
     * string representing a line from the file. The file is read using the specified charset.
     *
     * @param filePath    The path to the file whose contents are to be read.
     * @param charsetName The name of the charset to use for decoding the file content. Supported
     *                    charsets are "UTF-8" and "BIG5".
     * @return List of strings where each string is a line read from the file specified by filePath.
     */
    /**
     * 以指定的位元組長度分段讀取檔案內容，保持包含 CR (0x0D) 與 LF (0x0A) 字元。
     *
     * <p>將檔案分段讀取，每段長度約為 {@code length} 位元組，並依序存入 {@code List<byte[]>} 中回傳。
     * 若最後一段未滿長度，仍會以指定長度填充（可能含多餘資料）。
     *
     * @param filePath 要讀取之檔案完整路徑字串。
     * @param length   要讀取的位元組長度（包含 CR LF 符號），每次讀取的 buffer 大小。
     * @return List<byte [ ]> 讀取後的檔案內容清單，每筆元素長度固定為 {@code length} 位元組。
     */
    public List<byte[]> readFileByByte(String filePath, int length) {
        LogProcess.info(log, "readFileByByte");
        LogProcess.info(log, "filePath = {}", filePath);
        byte[] buffer = new byte[length];
        int bytesRead;
        List<byte[]> fileContents = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(Paths.get(filePath).toFile())) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] completeRecord = new byte[length];
                System.arraycopy(buffer, 0, completeRecord, 0, length);
                fileContents.add(completeRecord);
            }
        } catch (Exception e) {
            LogProcess.error(log, "readFileByByte error");
            LogProcess.error(
                    log, "error = {}", e);
        }
        return fileContents;
    }

    /**
     * 處理檔案結尾可能存在的不完整記錄，確保每一筆讀取到的資料都是獨立且正確的。 *
     */
    public List<byte[]> readFileByByte2(String filePath, int recordLength) {
        LogProcess.info(log, "readFileByByte");
        LogProcess.info(log, "filePath = {}", filePath);

        List<byte[]> fileContents = new ArrayList<>();
        try (FileInputStream inputStream = new FileInputStream(Paths.get(filePath).toFile())) {
            byte[] buffer = new byte[recordLength]; // 創建一個緩衝區，大小為每筆記錄的長度
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // 在此處處理每一筆記錄
                // 檢查是否讀到了完整的記錄長度
                if (bytesRead == recordLength) {
                    // 如果讀到了完整的記錄，直接加入清單
                    byte[] completeRecord = new byte[recordLength];
                    System.arraycopy(buffer, 0, completeRecord, 0, recordLength);
                    fileContents.add(completeRecord);
                } else {
                    // 如果讀取到的位元組數小於記錄長度，表示已經到達檔案結尾
                    byte[] partialRecord = new byte[bytesRead];
                    System.arraycopy(buffer, 0, partialRecord, 0, bytesRead);
                    fileContents.add(partialRecord);
                }
            }
        } catch (Exception e) {
            LogProcess.error(log, "readFileByByte error");
            LogProcess.error(
                    log, "error = {}", e);
        }
        return fileContents;
    }

    /**
     * Reads the contents of the specified file and returns it as a list of strings, with each
     * string representing a line from the file. The file is read using the specified charset.
     *
     * @param filePath The path to the file whose contents are to be read.
     * @param charsetName The name of the charset to use for decoding the file content. Supported
     *     charsets are "UTF-8" and "BIG5".
     * @return List of strings where each string is a line read from the file specified by filePath.
     * @throws LogicException if an I/O error occurs during reading of the file.
     */
//    public List<String> readFileContent(String filePath, String charsetName) throws LogicException {
//        LogProcess.info(log,  "readFileContent");
//        LogProcess.info(log,  "filePath = {}", filePath);
//        LogProcess.info(log,  "charsetName = {}", charsetName);
//        Path path = Paths.get(filePath);
//        Charset charset;
//        List<String> fileContents = new ArrayList<>();
//
//        if ("UTF-8".equalsIgnoreCase(charsetName)) {
//            charset = StandardCharsets.UTF_8;
//        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
//            charset = Charset.forName("CP950");
//        } else if ("CP950".equalsIgnoreCase(charsetName)) {
//            charset = Charset.forName("CP950");
//        } else {
//            throw new LogicException("E999", "Unsupported charset:" + charsetName);
//        }
//
//        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
//            String line;
//            while ((line = reader.readLine()) != null) fileContents.add(line);
//
//            while (!fileContents.isEmpty()
//                    && fileContents.get(fileContents.size() - 1).trim().isEmpty()) {
//                fileContents.remove(fileContents.size() - 1);
//            }
//        } catch (IOException e) {
//            LogProcess.error(
//                    log,  ExceptionDump.exception2String(e));
//            //            if ("BIG5".equalsIgnoreCase(charsetName)) {
//            //                fileContents.clear();
//            //                fileContents.addAll(this.readFileContent(filePath, "CP950"));
//            //            } else
//            // Appropriately handle the exception
//            throw new LogicException("E999", "Error reading file");
//        }
//        LogProcess.info(
//                log,
//                false,
//                LogType.NORMAL.getCode(),
//                "fileContents.size = {}",
//                fileContents.size());
//        return fileContents;
//    }


    /**
     * Reads the contents of the specified file and returns it as a list of strings, with each
     * string representing a line from the file. The file is read using the specified charset.
     *
     * @param filePath    The path to the file whose contents are to be read.
     * @param charsetName The name of the charset to use for decoding the file content. Supported
     *                    charsets are "UTF-8" and "BIG5".
     * @return List of strings where each string is a line read from the file specified by filePath.
     */
    public List<String> readFileContent2(String filePath, String charsetName, int lineSize) {

        String normalizedPath = FilenameUtils.normalize(filePath);
        Path path = Paths.get(normalizedPath);

        List<String> fileContents = new ArrayList<>();

        Charset charset = null;
        if ("UTF-8".equalsIgnoreCase(charsetName)) {
            charset = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName("Big5");
        } else {
            charset = Charset.forName(charsetName);
        }

        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.IGNORE)
                .onUnmappableCharacter(CodingErrorAction.IGNORE);
        //CodingErrorAction.REPORT 只顯示警告錯誤的字串
        //CodingErrorAction.REPLACE 將錯誤的字串替換成�
        //CodingErrorAction.IGNORE 忽略
//        decoder.replaceWith("~");

        int lineCount = 0;
        try (InputStream rawIn = Files.newInputStream(path);

             Reader in = new InputStreamReader(rawIn, decoder);
             BufferedReader reader = new BufferedReader(in, 8192)) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                // 簡單偵測：若這行包含替代字元，打警告，之後再決定是否回頭抓原始 HEX
                if (line.indexOf('\uFFFD') >= 0) {
                   // LogProcess.warn(log, "Line {} has invalid bytes  => {}", lineCount, line);
                   // LogProcess.warn(log, "Line {} has invalid bytes  => {}", lineCount, line.length());
                }
                if (line.length() != lineSize) {
                    line = line.substring(line.length() - lineSize);
                }
                fileContents.add(line.trim());
            }
            LogProcess.info(log, "1 source data count = {}", lineCount);

        } catch (IOException e) {
            LogProcess.info(log, "2 source data count = {}", lineCount);
            LogProcess.error(log, "Error Message: file is problem = {}", e.getMessage(), e);
        }
        return fileContents;
    }

    /**
     * Writes the provided list of lines to the file at the specified path using the given charset.
     * This method will create a new file if it does not exist, or it will append to the file if it
     * already exists.
     *
     * @param filePath    The path to the file where the lines will be written.
     * @param lines       The content to write to the file, with each string in the list representing a
     *                    separate line.
     * @param charsetName The name of the charset to use for encoding the file content. Supported
     *                    charsets are "UTF-8" and "BIG5".
     */
    public void writeFileContent(String filePath, List<String> lines, String charsetName) {
        maxLines = 1000;
        timeout = 5000;
        String allowedPath = FilenameUtils.normalize(filePath);

        Path path = Paths.get(allowedPath);
        Charset charset = null;
        CharsetEncoder encoder;

        if ("UTF-8".equalsIgnoreCase(charsetName)) {
            charset = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName("Big5");
        } else {
        }
        encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
            //如果有沒有資料夾，則建立一個資料夾
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
//
            if (!Files.exists(path)) Files.createFile(path);

            try (FileOutputStream fos = new FileOutputStream(allowedPath, true);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, encoder);
                 BufferedWriter writer = new BufferedWriter(osw)) {

                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
                LogProcess.error(log, "Error Message: not found file",e);
            }
        } catch (Exception e) {
            LogProcess.error(log, "Error Message: There is a problem with the file",e);
        }
    }

    public void deleteDir(String dirPath) {
        Path path = Paths.get(dirPath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            try {
                Files.walkFileTree(
                        path,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                    throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                                    throws IOException {
                                if (exc != null) throw exc;
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException e) {
                LogProcess.info(log, "Error Message: delete dir fail");
            }
        }
    }


    public void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        if (Files.exists(path) && !Files.isDirectory(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                LogProcess.info(log, "Error Message: delete file fail");
            }
        }
    }

    public boolean exists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }


    private boolean isValidInput(String input) {
        if (input == null || input.length() == 0) {
            LogProcess.info(log, "isValidInput is null");
            return false;
        }
        return true;
        // 允許：中文、英數字、底線(_)、@、.、-、空格
//        if (input.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_@.,?+*-\\-\\s\\u3000]+$")) {
//            return true;
//        } else {
//            LogProcess.info("input =" + input);
//            LogProcess.info("Not Match");
//            return false;
//        }
    }

    public String replaceDateWithPlaceholder(String fileName) {
        // 偵測 7 或 8 碼數字（yyyyMMdd 格式）
        Pattern pattern = Pattern.compile("(\\d{7}|\\d{8})");
//        fileName = fileName.replace(".txt", "");

        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            // 只替換第一個符合的 8 碼數字為 [yyyymmdd]
            return matcher.replaceFirst("[yyyymmdd]");
        }

        return fileName;
    }

    public List<String> readFileContent(String filePath, String charsetName) {

        return readFileContent(filePath, charsetName, false);
    }


    public List<String> readFileContent(String filePath, String charsetName, boolean strictMode) {
        String normalizedPath = FilenameUtils.normalize(filePath);
        Path path = Paths.get(normalizedPath);

        // 選字集：BIG5 一律用 MS950；其他照你的參數
        Charset cs;
        if (charsetName == null || "UTF-8".equalsIgnoreCase(charsetName)) {
            cs = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName) || "MS950".equalsIgnoreCase(charsetName) || "CP950".equalsIgnoreCase(charsetName)) {
            cs = Charset.forName("MS950");
        } else if ("CP1047".equalsIgnoreCase(charsetName) || "CP037".equalsIgnoreCase(charsetName) || "CP500".equalsIgnoreCase(charsetName) || "CP937".equalsIgnoreCase(charsetName)) {
            cs = Charset.forName(charsetName); // EBCDIC （依實際來源決定）
        } else {
            cs = Charset.forName(charsetName);
        }

//        CharsetDecoder decoder = cs.newDecoder();
//        if (strictMode) {
//            decoder.onMalformedInput(CodingErrorAction.REPORT)
//                    .onUnmappableCharacter(CodingErrorAction.REPORT);
//        } else {
//            decoder.onMalformedInput(CodingErrorAction.REPLACE)
//                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
//                    .replaceWith("?"); // 需要固定欄寬可改成 "□" 或全形空白 "　"
//        }
        //置換使用
        CharsetDecoder decLenient = cs.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith("?");
        //拋錯使用
        CharsetDecoder decStrict = cs.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        List<String> out = new ArrayList<>();
        int lineNo = 0;

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path), 64 * 1024)) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
            int prev = -1, c;

            while ((c = in.read()) != -1) {
                if (c == '\n') {
                    byte[] bytes = buf.toByteArray();
                    if (prev == '\r' && bytes.length > 0) bytes = Arrays.copyOf(bytes, bytes.length - 1);
                    buf.reset();
                    lineNo++;

                    //放進 List
                    String s;
                    try {
                        s = decLenient.decode(ByteBuffer.wrap(bytes)).toString();
                    } finally {
                        decLenient.reset();
                    }
                    if (lineNo == 1 && !s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);
                    out.add(s);

                    //若失敗或出現替代字，記錄錯誤
                    boolean bad = false;
                    try {
                        decStrict.decode(ByteBuffer.wrap(bytes));
                    } catch (CharacterCodingException ex) {
                        bad = true;
                    } finally {
                        decStrict.reset();
                    }

                    // Big5  & EBCDIC
                    int illegalAt = firstIllegalBig5Index(bytes);
                    boolean maybeEbcdic = looksLikeEbcdic(bytes);
                    boolean hasBadStrict = bad;
                    boolean hasReplacement = s.indexOf('\uFFFD') >= 0 || s.indexOf('?') >= 0;
                    boolean hasIllegalBig5 = illegalAt >= 0;
                    boolean hasEbcdicLike = maybeEbcdic;

                    // 只有其中任一條件成立才顯示
                    if (hasBadStrict || hasReplacement || hasIllegalBig5 || hasEbcdicLike) {
                        // 取 HEX 視窗（避免太長）
                        int start = Math.max(0, (illegalAt >= 0 ? illegalAt : 0) - 8);
                        int end = Math.min(bytes.length, start + 32);
                        String window = bytesToHex(Arrays.copyOfRange(bytes, start, end));

                        // 分別顯示原因，讓 log 更明確
                        if (hasBadStrict) {
                           // LogProcess.warn(log, "[Line {}] ⚠️ 嚴格解碼失敗 (badStrict=true) HEX[..]={}", lineNo, window);
                        }
                        if (hasReplacement) {
                           // LogProcess.warn(log, "[Line {}] ⚠️ 出現替代字 (� 或 ?) HEX[..]={}", lineNo, window);
                        }
                        if (hasIllegalBig5) {
                           // LogProcess.warn(log, "[Line {}] ⚠️ 非法 Big5 位元組 (illegalAt={}) HEX[..]={}", lineNo, illegalAt, window);
                        }
                        if (hasEbcdicLike) {
//                           // LogProcess.warn(log, "[Line {}] ⚠️ 疑似 EBCDIC 編碼 HEX[..]={}", lineNo, window);
                        }
                    }
                } else {
                    buf.write(c);
                }
                prev = c;
            }

            // 結尾最後一筆（沒有換行符）
            if (buf.size() > 0) {
                byte[] bytes = buf.toByteArray();
                lineNo++;

                String s;
                try {
                    s = decLenient.decode(ByteBuffer.wrap(bytes)).toString();
                } finally {
                    decLenient.reset();
                }
                if (lineNo == 1 && !s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);
                out.add(s);

                boolean bad = false;
                try {
                    decStrict.decode(ByteBuffer.wrap(bytes));
                } catch (CharacterCodingException ex) {
                    bad = true;
                } finally {
                    decStrict.reset();
                }

                int illegalAt = firstIllegalBig5Index(bytes);
                boolean maybeEbcdic = looksLikeEbcdic(bytes);
                boolean hasBadStrict = bad;
                boolean hasReplacement = s.indexOf('\uFFFD') >= 0 || s.indexOf('?') >= 0;
                boolean hasIllegalBig5 = illegalAt >= 0;
                boolean hasEbcdicLike = maybeEbcdic;

                // 只有其中任一條件成立才顯示
                if (hasBadStrict || hasReplacement || hasIllegalBig5 || hasEbcdicLike) {
                    // 取 HEX 視窗（避免太長）
                    int start = Math.max(0, (illegalAt >= 0 ? illegalAt : 0) - 8);
                    int end = Math.min(bytes.length, start + 32);
                    String window = bytesToHex(Arrays.copyOfRange(bytes, start, end));

                    // 分別顯示原因，讓 log 更明確
                    if (hasBadStrict) {
                       // LogProcess.warn(log, "[Line {}] ⚠️ 嚴格解碼失敗 (badStrict=true) HEX[..]={}", lineNo, window);
                    }
                    if (hasReplacement) {
                       // LogProcess.warn(log, "[Line {}] ⚠️ 出現替代字 (� 或 ?) HEX[..]={}", lineNo, window);
                    }
                    if (hasIllegalBig5) {
                       // LogProcess.warn(log, "[Line {}] ⚠️ 非法 Big5 位元組 (illegalAt={}) HEX[..]={}", lineNo, illegalAt, window);
                    }
                    if (hasEbcdicLike) {
//                       // LogProcess.warn(log, "[Line {}] ⚠️ 疑似 EBCDIC 編碼 HEX[..]={}", lineNo, window);
                    }
                }
            }
//            LogProcess.info(log, "out out out = {}", out);

            LogProcess.info(log, "source data count = {}", lineNo);

        } catch (IOException e) {
            LogProcess.error(log, "I/O error after line {}: {}", lineNo, e.getMessage(), e);
        }

        return out;
    }

    // byte[] -> HEX
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    // Big5 ：抓第一個不合法位置（回傳 -1 代表看起來都合法或純 ASCII）
    private int firstIllegalBig5Index(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            int x = b[i] & 0xFF;
            if (x >= 0x81 && x <= 0xFE) {       // lead
                if (i + 1 >= b.length) return i;                // 尾端殘缺
                int y = b[i + 1] & 0xFF;
                boolean ok = (y >= 0x40 && y <= 0x7E) || (y >= 0xA1 && y <= 0xFE);
                if (!ok) return i;
                else i++;  // 合法雙位元組，跳過第二個
            } // 其他 ASCII 直接過
        }
        return -1;
    }

    //判斷： EBCDIC
    private boolean looksLikeEbcdic(byte[] b) {
        int d = 0, l = 0, so = 0;
        for (byte v : b) {
            int x = v & 0xFF;
            if (x >= 0xF0 && x <= 0xF9) d++;                  // '0'..'9'
            if (x == 0x0E || x == 0x0F) so++;                 // SO/SI
            if ((x >= 0xC1 && x <= 0xE9)) l++;                // A..Z/a..z
        }
        return (d + l) >= 4 || so > 0;
    }
}
