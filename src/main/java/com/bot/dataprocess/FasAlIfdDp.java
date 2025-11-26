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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("FasAlIfdDp")
public class FasAlIfdDp implements Job {
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

        LogProcess.info(log, " 開始執行 FasAlIfdDp Job 流程");

        // 實際邏輯放這裡
        // 例如讀檔案、處理資料、匯出報表等
        try {
            Thread.sleep(1000); // 模擬運行
            LogProcess.info(log, "FasAlIfdDp Job 執行完成");
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
            outputPath = outputPath.resolve(inputFileName+".new");
        }


        LogProcess.info(log, "實際輸入檔案: {}", inputPath);

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("輸入檔案不存在: " + inputPath);
        }


        JobResult result = new JobResult();
        result.putOutputFileMap(outputPath.getFileName().toString(),outputPath);
        long writeCount = 0;   // 計算總筆數，或你可換成每檔案計數


        try (BufferedReader br =
                     Files.newBufferedReader(inputPath, Charset.forName(CHARSET_BIG5));
             BufferedWriter w = Files.newBufferedWriter(
                     outputPath,
                     Charset.forName(CHARSET_BIG5),
                     StandardOpenOption.CREATE,
                     StandardOpenOption.APPEND);
             ) {

            String line;
            while ((line = br.readLine()) != null) {

                if (line == null) continue;

                if (line.isEmpty()) continue;



                // 寫入資料
                w.write(insertDecimalFromEnd(line,36));
                w.newLine();
                writeCount++;

                // 每 10000 筆 flush 一次（避免 buffer 太大）
                if (writeCount % 10000 == 0) {
                    try {
                        w.flush();
                    } catch (IOException e) {
                        LogProcess.error(log, "flush 失敗: {}", e.getMessage());
                    }
                }

            }

        } catch (IOException ex) {
            LogProcess.error(log, "處理輸入檔案錯誤: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);

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
