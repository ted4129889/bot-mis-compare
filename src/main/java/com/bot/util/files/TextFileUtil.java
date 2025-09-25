/* (C) 2023 */
package com.bot.util.files;

import com.bot.util.log.LogProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
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
    public List<String> readFileContent(String filePath, String charsetName, int lineSize) {

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
                    LogProcess.warn(log, "Line {} has invalid bytes  => {}", lineCount, line);
                    LogProcess.warn(log, "Line {} has invalid bytes  => {}", lineCount, line.length());
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
                LogProcess.info(log, "Error Message: not found file");
            }
        } catch (Exception e) {
            LogProcess.info(log, "Error Message: There is a problem with the file");
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

}
