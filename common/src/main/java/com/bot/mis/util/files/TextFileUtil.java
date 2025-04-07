/* (C) 2023 */
package com.bot.mis.util.files;

import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.close.SafeClose;
import com.bot.txcontrol.util.dump.ExceptionDump;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class TextFileUtil {

    @Value("${app.file-processing.max-lines}")
    private int maxLines;

    @Value("${app.file-processing.timeout}")
    private long timeout;

    private TextFileUtil() {
        // YOU SHOULD USE @Autowired ,NOT new TextFileUtil()
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
    public List<String> readFileContent(String filePath, String charsetName) throws LogicException {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "readFileContent");
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "filePath = {}", filePath);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "charsetName = {}", charsetName);
        Path path = Paths.get(filePath);
        Charset charset;
        List<String> fileContents = new ArrayList<>();

        if ("UTF-8".equalsIgnoreCase(charsetName)) {
            charset = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName("Big5");
        } else {
            throw new LogicException("E999", "Unsupported charset:" + charsetName);
        }
        BufferedReader reader = null;
        try {
            // 驗證檔案是否存在以及是否可讀性
            if (!Files.exists(path) || !Files.isReadable(path)) {
                throw new IllegalArgumentException(
                        "File does not exist or does not differ：" + path);
            } else {
                reader = Files.newBufferedReader(path, charset);
                String line;
                int lineCount = 0;
                long startTime = System.currentTimeMillis();

                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (lineCount > maxLines) {
                        throw new IllegalStateException("檔案過大，超過最大行數限制");
                    }

                    if (System.currentTimeMillis() - startTime > timeout) {
                        throw new TimeoutException("讀取時間過長，操作超時");
                    }

                    fileContents.add(line);
                }
            }
        } catch (IOException | TimeoutException e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            // Appropriately handle the exception
            throw new LogicException("E999", "Error reading file");
        } finally {
            SafeClose.close(reader);
        }
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                "fileContents.size = {}",
                fileContents.size());
        return fileContents;
    }

    /**
     * Writes the provided list of lines to the file at the specified path using the given charset.
     * This method will create a new file if it does not exist, or it will append to the file if it
     * already exists.
     *
     * @param filePath The path to the file where the lines will be written.
     * @param lines The content to write to the file, with each string in the list representing a
     *     separate line.
     * @param charsetName The name of the charset to use for encoding the file content. Supported
     *     charsets are "UTF-8" and "BIG5".
     * @throws LogicException if an I/O error occurs writing to or creating the file.
     */
    public void writeFileContent(String filePath, List<String> lines, String charsetName)
            throws LogicException {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "writeFileContent");
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "filePath = {}", filePath);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "charsetName = {}", charsetName);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "lines.size = {}", lines.size());

        Path path = Paths.get(filePath);
        Charset charset;
        CharsetEncoder encoder;

        if ("UTF-8".equalsIgnoreCase(charsetName)) {
            charset = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName("Big5");
        } else {
            throw new LogicException("E999", "Unsupported charset: " + charsetName);
        }
        encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        try {
            if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());

            if (!Files.exists(path)) Files.createFile(path);

            try (FileOutputStream fos = new FileOutputStream(filePath, true);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, encoder);
                    BufferedWriter writer = new BufferedWriter(osw)) {

                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (Exception e) {
                ApLogHelper.error(
                        log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
                throw new LogicException("E999", "寫入檔案時發生錯誤(" + filePath + ")");
            }
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("E999", "產生路徑或檔案發生錯誤(" + filePath + ")");
        }
    }

    public void deleteDir(String dirPath) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "deleteDir dirPath = {}", dirPath);
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
                ApLogHelper.error(
                        log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
                throw new LogicException("E999", "刪除資料夾時發生錯誤(" + dirPath + ")");
            }
        }
    }

    public void deleteFile(String filePath) {
        ApLogHelper.info(
                log, false, LogType.NORMAL.getCode(), "deleteFile filePath = {}", filePath);
        Path path = Paths.get(filePath);
        if (Files.exists(path) && !Files.isDirectory(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                ApLogHelper.error(
                        log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
                throw new LogicException("E999", "刪除檔案時發生錯誤(" + filePath + ")");
            }
        }
    }

    public boolean exists(String filePath) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "exists filePath = {}", filePath);
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
}
