package com.bot.dataprocess;


import com.bot.util.files.TextFileUtil;
import com.bot.util.log.LogProcess;
import com.bot.util.parse.Parse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
@Component("DB_ACHMR")
public class DB_ACHMR implements Job {
    @Value("${localFile.mis.batch.output}")
    private String outputPath;
    @Value("${localFile.mis.xml.file_def}")
    private String botMaskXmlFilePath;
    @Value("${localFile.mis.xml.file_def2}")
    private String botMaskXmlFilePath2;
    @Value("${localFile.mis.json.field_setting.directory}")
    private String fieldSettinngFile;

    @Autowired
    TextFileUtil textFileUtil;

    @Autowired
    Parse parse;
    private final String CHARSET_BIG5 = "MS950";
    private final CharsetEncoder encoder = Charset.forName("MS950").newEncoder();


    @Override
    public JobResult execute(JobContext ctx)  {

        LogProcess.info(log, " 開始執行 DB_ACHMR Job 流程");

        // 實際邏輯放這裡
        // 例如讀檔案、處理資料、匯出報表等
        try {
            Thread.sleep(1000); // 模擬運行
            LogProcess.info(log, "DB_ACHMR Job 執行完成");
        } catch (InterruptedException e) {
            log.error("執行 FasAlIfdDp Job 發生錯誤", e);
        }

        Path inputPath = ctx.inputDir();
        Path outputPath = ctx.outputDir();
        String inputFileName = ctx.inputFileName();

        if (inputPath == null || outputPath == null || inputFileName == null || inputFileName.isBlank()) {
            throw new IllegalArgumentException("inputDir / outputDir / inputFileName 不可為空");
        }

        LogProcess.info(log, "in = {},out = {},inputFileName = {}", inputPath, outputPath, inputFileName);


        // 處理輸入檔案路徑
        if (inputPath.getFileName() != null && inputPath.getFileName().toString().equalsIgnoreCase(inputFileName)) {
        } else {
            // 沒含檔名 → 補上
            inputPath = inputPath.resolve(inputFileName);
//            outputPath = outputPath.resolve(inputFileName);
        }


        LogProcess.info(log, "實際輸入檔案: {}", inputPath);

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("輸入檔案不存在: " + inputPath);
        }


        JobResult result = new JobResult();
//        result.putOutputFileMap(outputPath.getFileName().toString(),outputPath);
        long writeCount = 0;   // 計算總筆數，或你可換成每檔案計數
        int fileIndex = 1;

        final int BATCH_SIZE = 50_000;   // 每10萬筆一個檔案
        final int FLUSH_SIZE = 10_000;    // 每1萬筆 flush

        // 建立第一個輸出檔案
        BufferedWriter writer = null;

        try (BufferedReader br = Files.newBufferedReader(inputPath, Charset.forName("BIG5"))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line == null || line.isEmpty()) continue;

                // 若 writer 尚未開啟，或已達到10萬筆，則開新檔案
                if (writer == null || writeCount % BATCH_SIZE == 0) {

                    // 關閉前一個 writer
                    if (writer != null) writer.close();

                    Path outFile = outputPath.resolve(
                            inputPath.getFileName().toString() + "_" + String.format("%02d",fileIndex) + ".sql"
                    );
                    fileIndex++;

                    writer = Files.newBufferedWriter(
                            outFile,
                            Charset.forName("BIG5"),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );

                    LogProcess.info(log, "建立新檔案: {}", outFile.toString());
                }

                // 寫入資料
                writer.write(line);
                writer.newLine();
                writeCount++;

                // 每10000筆 flush
                if (writeCount % FLUSH_SIZE == 0) {
                    try {
                        writer.flush();
                    } catch (IOException ex) {
                        LogProcess.error(log, "flush失敗: {}", ex.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("拆分檔案失敗", ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {}
            }
        }


        result.setSuccess(true);
        result.setMessage("完成");

        return result;
    }


    /**
     * 在字串中「從尾端往回 count 個字元」的位置前插入小數點。
     *
     * @param text  原始字串
     * @param count 從尾端往回的字元數，例如 36
     * @return 插入小數點後的新字串
     */
    public static String insertDecimalFromEnd(String text, int count) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 真實插入位置 = 字串長度 - count
        int insertPos = text.length() - count;

        if (insertPos <= 0 || insertPos >= text.length()) {
            throw new IllegalArgumentException("insertPos 無效：" + insertPos);
        }

        return text.substring(0, insertPos) + "." + text.substring(insertPos);
    }

}
