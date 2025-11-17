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
@Component("CUSDACNO")
public class CUSDACNO implements Job {
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
    public JobResult execute(JobContext ctx) {

        LogProcess.info(log, " 開始執行 CUSDACNO Job 流程");

        // 實際邏輯放這裡
        // 例如讀檔案、處理資料、匯出報表等
        try {
            Thread.sleep(1000); // 模擬運行
            LogProcess.info(log, "CUSDACNO Job 執行完成");
        } catch (InterruptedException e) {
            log.error("執行 CUSDACNO Job 發生錯誤", e);
        }

        Path inputPath = ctx.inputDir();
        Path outputPath = ctx.outputDir().getParent();
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
        }

        LogProcess.info(log, "實際輸入檔案: {}", inputPath);

        if (!Files.exists(inputPath)) {
            throw new IllegalArgumentException("輸入檔案不存在: " + inputPath);
        }

        LogProcess.info(log, "開始拆分檔案: {}", inputPath);

        JobResult result = new JobResult();

        Map<String, BufferedWriter> writers = new HashMap<>();

        long writeCount = 0;   // 計算總筆數，或你可換成每檔案計數

        try (BufferedReader br =
                     Files.newBufferedReader(inputPath, Charset.forName(CHARSET_BIG5))) {

            String line;
            while ((line = br.readLine()) != null) {

                if (line == null) continue;
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.length() < 3) {
                    LogProcess.info(log, "略過行(長度不足3): {}", line);
                    continue;
                }

                String brno = line.substring(0, 3);

                int brnoInt;
                try {
                    brnoInt = Integer.parseInt(brno);
                } catch (NumberFormatException nfe) {
                    LogProcess.info(log, "略過行(前三碼非數字): {}", line);
                    continue;
                }

                if (brnoInt <= 0 || brnoInt > 999) {
                    LogProcess.info(log, "略過行(前三碼不在 001~999): {}", line);
                    continue;
                }

                // 計算每 30 的區間後綴
                // 1~30 → 030
                // 31~60 → 060
                // 61~90 → 090
                // ...
                final String tmpBrno = (brnoInt <= 10)
                        ? "010"
                        : String.format("%03d", ((brnoInt + 9) / 10) * 10);

                BufferedWriter w = writers.get(tmpBrno);

                // 若尚未建立 writer → 建立新輸出檔
                if (w == null) {
                    Path outFile = outputPath.resolve(inputFileName + "_" + tmpBrno);

                    //如果存在就先刪除
                    Files.deleteIfExists(outFile);

                    LogProcess.info(log, "產生拆分的檔案: {}", outFile);

                    String key = inputFileName + "_" + tmpBrno;
                    // 新檔案加入回傳清單
                    if (!result.getOutputFilesMap().containsKey(key)) {
                        result.putOutputFileMap(key, outFile);
                    }

                    try {
                        w = Files.newBufferedWriter(
                                outFile,
                                Charset.forName(CHARSET_BIG5),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.APPEND
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("建立輸出檔失敗: " + outFile, e);
                    }

                    writers.put(tmpBrno, w);

                }


                // 寫入資料
                w.write(line);
                w.newLine();
                writeCount++;

                // 每 10000 筆 flush 一次（避免 buffer 太大）
                if (writeCount % 10000 == 0) {
                    try {
                        w.flush();
//                        LogProcess.info(log, "已寫入 {} 筆資料，強制 flush.", writeCount);
                    } catch (IOException e) {
                        LogProcess.error(log, "flush 失敗: {}", e.getMessage());
                    }
                }

            }

        } catch (IOException ex) {
            LogProcess.error(log, "處理輸入檔案錯誤: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);

        } finally {

            // 關閉所有 writer
            for (Map.Entry<String, BufferedWriter> entry : writers.entrySet()) {
                try {
                    entry.getValue().close();
//                    LogProcess.info(log, "已關閉 Writer: {}", entry.getKey());
                } catch (IOException e) {
                    LogProcess.error(log, "關閉 Writer 失敗: key={}, err={}",
                            entry.getKey(), e.getMessage());
                }
            }
        }

        result.setSuccess(true);
        result.setMessage("拆分完成，共 " + result.getOutputFilesMap().size() + " 個檔案");

        return result;
    }


}
