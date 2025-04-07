/* (C) 2023 */
package com.bot.mis.util.sort;

import com.bot.mis.util.sort.vo.FileBuffer;
import com.bot.mis.util.sort.vo.KeyRange;
import com.bot.mis.util.sort.vo.SubstringComparator;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.dump.ExceptionDump;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalSortUtil {

    @Value("${externalSort.blockSize}")
    private int blockSize;

    @Value("${app.file-processing.max-lines}")
    private int maxLines;

    @Value("${app.file-processing.timeout}")
    private long timeout;

    private Charset charset = StandardCharsets.UTF_8;

    public void sortingFile(
            String inputFilePath,
            String outputFilePath,
            List<KeyRange> keyRanges,
            String charsetName) {
        sortingFile(
                Paths.get(inputFilePath).toFile(),
                Paths.get(outputFilePath).toFile(),
                keyRanges,
                charsetName);
    }

    public void sortingFile(File inputFile, File outputFile, List<KeyRange> keyRanges) {
        sortingFile(inputFile, outputFile, keyRanges, "UTF-8");
    }

    public void sortingFile(
            File inputFile, File outputFile, List<KeyRange> keyRanges, String charsetName) {
        ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "blockSize = {}", blockSize);
        if ("UTF-8".equalsIgnoreCase(charsetName)) {
            charset = StandardCharsets.UTF_8;
        } else if ("BIG5".equalsIgnoreCase(charsetName)) {
            charset = Charset.forName("Big5");
        } else {
            throw new LogicException("E999", "Unsupported charset:" + charsetName);
        }
        List<File> files = splitAndSort(inputFile, blockSize, keyRanges);
        mergeSortedFiles(files, outputFile, keyRanges);
        ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "finish.");
    }

    private List<File> splitAndSort(File inputFile, int blockSize, List<KeyRange> keyRanges) {
        ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "splitAndSort");

        if (blockSize <= 0) {
            throw new LogicException("E999", "blockSize must be positive");
        }

        List<File> sortedFiles = new ArrayList<>();
        File tempFile = null;

        try {
            tempFile = File.createTempFile("sorted_chunk_", ".txt");
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("E999", "切割排序暫存檔案時發生錯誤");
        }

        try (FileReader fr = new FileReader(inputFile, charset);
                BufferedReader reader = new BufferedReader(fr);
                FileOutputStream fos = new FileOutputStream(tempFile.getPath(), true);
                OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
                BufferedWriter writer = new BufferedWriter(osw); ) {

            String line;
            int lineCount = 0;
            long startTime = System.currentTimeMillis();
            while ((line = reader.readLine()) != null) {
                List<String> lines = new ArrayList<>(blockSize);

                lineCount++;
                if (lineCount > maxLines) {
                    throw new IllegalStateException("檔案過大，超過最大行數限制");
                }

                if (System.currentTimeMillis() - startTime > timeout) {
                    throw new TimeoutException("讀取時間過長，操作超時");
                }

                do {
                    line = cleanInput(line);
                    ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "readLine ={}", line);
                    lines.add(line);

                } while (lines.size() < blockSize && (line = reader.readLine()) != null);

                lines.sort(new SubstringComparator(keyRanges, charset));
                tempFile.deleteOnExit();
                for (String sortedLine : lines) {
                    ApLogHelper.debug(
                            log, false, LogType.NORMAL.getCode(), "sortedLine ={}", sortedLine);
                    writer.write(sortedLine);
                    writer.newLine();
                }
                sortedFiles.add(tempFile);
            }
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("E999", "切割排序暫存檔案時發生錯誤");
        }
        return sortedFiles;
    }

    // 清理與驗證輸入內容
    private String cleanInput(String input) {
        // 過濾控制字符並限制最大長度
        return input.replaceAll("[^\\p{Print}]", "").trim();
    }

    private void mergeSortedFiles(
            List<File> sortedFiles, File outputFile, List<KeyRange> keyRanges) {
        ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "mergeSortedFiles");

        try (FileOutputStream fos = new FileOutputStream(outputFile.getPath());
                OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
                BufferedWriter writer = new BufferedWriter(osw); ) {
            SubstringComparator comparator = new SubstringComparator(keyRanges, charset);
            PriorityQueue<FileBuffer> minHeap = new PriorityQueue<>(sortedFiles.size());
            for (File file : sortedFiles) {
                FileBuffer fileBuffer = new FileBuffer(file, comparator, charset);
                if (!fileBuffer.isEmpty()) {
                    minHeap.add(fileBuffer);
                }
            }

            while (!minHeap.isEmpty()) {
                FileBuffer buffer = minHeap.poll();
                String mergeLine = buffer.readLine();
                ApLogHelper.debug(log, false, LogType.NORMAL.getCode(), "mergeLine ={}", mergeLine);
                writer.write(mergeLine);
                writer.newLine();
                if (buffer.isEmpty()) {
                    buffer.close();
                } else {
                    minHeap.add(buffer);
                }
            }
        } catch (Exception e) {
            ApLogHelper.error(
                    log, false, LogType.NORMAL.getCode(), ExceptionDump.exception2String(e));
            throw new LogicException("E999", "合併排序暫存檔案時發生錯誤");
        }
    }
}
