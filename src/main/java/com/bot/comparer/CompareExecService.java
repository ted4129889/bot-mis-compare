package com.bot.comparer;

import com.bot.db.RocksDbManager;
import com.bot.domain.FieldDef;
import com.bot.domain.RowData;
import com.bot.output.templates.CompareResultBean;
import com.bot.output.templates.CompareResultRpt;
import com.bot.reader.LineParser;
import com.bot.util.log.LogProcess;
import com.bot.util.text.FormatData;
import com.bot.writer.OutputReporter;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CompareExecService {

    private int aCount = 0;
    private int bCount = 0;
    private int missCount = 0;
    private int extraCount = 0;
    private int diffCount = 0;


    @Value("${localFile.mis.compare_result.main}")
    private String resultMain;
    @Autowired
    private FormatData formatData;

    @Autowired
    private CompareResultRpt compareResultRpt;

    public CompareResultBean compare(Path fileA, Path fileB, List<FieldDef> defs, String fileName, String fileType) throws Exception {

        LocalDateTime dateTime = LocalDateTime.now();

        String dateTimeStr = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String dateTimeStr2 = dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));

        String finalFileName = fileName + "_" + dateTimeStr;

        Path finalOutputFolder = Path.of(resultMain).resolve(fileType).resolve(fileName);
        Path aFileOutPutPath = finalOutputFolder.resolve(finalFileName + "_bot.txt");
        Path bFileOutPutPath = finalOutputFolder.resolve(finalFileName + "_misbh.txt");
        Path missOutPutPath = finalOutputFolder.resolve(finalFileName + "_miss.txt");
        Path extraOutPutPath = finalOutputFolder.resolve(finalFileName + "_extra.txt");
        Path diffOutPutPath = finalOutputFolder.resolve(finalFileName + "_diff.txt");
        Path rockDbOutPutPath = finalOutputFolder.resolve("rocksdb\\A_DB");

        System.out.println("finalOutputFolder :  " + finalOutputFolder);
        System.out.println("missOutPutPath :  " + missOutPutPath);
        System.out.println("extraOutPutPath :  " + extraOutPutPath);
        System.out.println("diffOutPutPath :  " + diffOutPutPath);
        System.out.println("rockDbOutPutPath :  " + rockDbOutPutPath);

        try (RocksDbManager db = new RocksDbManager(rockDbOutPutPath.toString())) {

            aCount = 0;
            bCount = 0;
            missCount = 0;
            extraCount = 0;
            diffCount = 0;


            // 1:讀 A，建RocksDB
            try (BufferedReader br = Files.newBufferedReader(fileA, Charset.forName("MS950"))) {

                String line;
                long count = 0;

                while ((line = br.readLine()) != null) {
                    aCount++;

                    //轉map
                    RowData rawA = LineParser.parseLine(formatData.getReplaceSpace(line, " "), defs);

//                    OutputReporter.reportFileA(rawA, aFileOutPutPath);
                    // A key hash
                    String keyHash = rawA.getKeyHash();

                    // A full raw hash
                    String fullHash = rawA.getFullHash();

                    // flag=0 + fullHash + A raw
                    String value = "0|" + fullHash + "|" + rawA.toJson();

                    if (line.contains("ABOC,130,BJ,CN,00000000335,4,0000001,202506")) {
                        LogProcess.info(log, "A value ={},{}", keyHash, value);
                    }
                    //寫進RocksDb
                    db.put(keyHash, value);

                    if (++count % 100000 == 0) {
                        System.out.println("A file indexed: " + count);
                    }
                }

                System.out.println("A file indexed: " + count);
            }

            // 2:讀 B 比對 A
            try (BufferedReader br = Files.newBufferedReader(fileB, Charset.forName("MS950"))) {

                String line;
                long count = 0;

                while ((line = br.readLine()) != null) {
                    bCount++;
                    //轉map
                    RowData rawB = LineParser.parseLine(formatData.getReplaceSpace(line, " "), defs);
//                    OutputReporter.reportFileA(rawB, bFileOutPutPath);
                    // B key hash
                    String keyHash = rawB.getKeyHash();

                    // B full raw hash
                    String fullHashB = rawB.getFullHash();

                    if (line.contains("ABOC,130,BJ,CN,00000000335,4,0000001,202506")) {
                        LogProcess.info(log, "B value ={},{}", keyHash, fullHashB);
                    }


                    //用 B key hash 找 A raw data
                    String valueInA = db.get(keyHash);

                    if (line.contains("ABOC,130,BJ,CN,00000000335,4,0000001,202506")) {
                        LogProcess.info(log, "valueInA ={}", valueInA);
                    }
                    //找不到，表示 B 多資料
                    if (valueInA == null) {
                        extraCount++;
                        //寫到多的檔案
                        OutputReporter.reportExtra(rawB, extraOutPutPath);
                    } else {
                        String[] parts = valueInA.split("\\|", 3); // fullHash | map

                        String oldFlag = parts[0];
                        String fullHashA = parts[1];
                        String rawA = parts[2];

                        // 更新 flag = 1
                        String newValue = "1|" + fullHashA + "|" + rawA;
                        db.put(keyHash, newValue);

                        if (line.contains("ABOC,130,BJ,CN,00000000335,4,0000001,202506")) {

                            LogProcess.info(log, "A =>B value ={},{}", keyHash, newValue);

                            LogProcess.info(log, "fullHashA ={}", fullHashA);
                            LogProcess.info(log, "fullHashB ={}", fullHashB);

                        }

                        // 比 hash → 欄位差異
                        if (!fullHashA.equals(fullHashB)) {
                            diffCount++;

                            String diffResult = compareFields(RowData.fromJson(rawA), rawB, defs);
                            //有符合，但是內容有差異
                            OutputReporter.reportFieldDiff(diffResult, diffOutPutPath);
                        }
                    }

                    if (++count % 100000 == 0) {
                        System.out.println("B file compared: " + count);
                    }
                }
            }

            //3：找 B 少資料
            RocksIterator it = db.newIterator();

            for (it.seekToFirst(); it.isValid(); it.next()) {

                String value = new String(it.value());

                String[] parts = value.split("\\|", 3);
                String flag = parts[0];
                String fullHash = parts[1];
                String rawA = parts[2];

                if (value.contains("ABOC,130,BJ,CN,00000000335,4,0000001,202506")) {
                    LogProcess.info(log, "flag => {}", flag);
                    LogProcess.info(log, "fullHash => {}", fullHash);
                    LogProcess.info(log, "rawA => {}", rawA);
                }

                if ("0".equals(flag)) {
                    missCount++;
                    // A 有， B 沒有
                    OutputReporter.reportMissing(rawA, missOutPutPath);
                }
            }

            it.close();

            return exportTextFile(Path.of(fileName).getFileName().toString());

        }
    }


    private String compareFields(RowData a, RowData b, List<FieldDef> defs) {
        StringBuilder sb = new StringBuilder();
        sb.append("key : ").append(a.getKeyRaw()).append("\n");
        sb.append("bot data : ").append(a.getFieldMap()).append("\n");
        sb.append("mis data : ").append(b.getFieldMap()).append("\n");

        for (FieldDef def : defs) {
            String v1 = a.getFieldMap().get(def.getName());
            String v2 = b.getFieldMap().get(def.getName());

            if (!Objects.equals(v1, v2)) {
                sb.append(String.format("欄位[%s] 不同: bot='%s', mis='%s'\n",
                        def.getName(), v1, v2));
            }
        }
        sb.append("----------------------------------\n");
        return sb.toString();
    }

    //計算要輸出結果檔案的筆數資訊
    private CompareResultBean exportTextFile(String fileName) {

        //扣1是因為扣除欄位
        int botTotal = aCount;
        int misTotal = bCount;
        int diffColCount = diffCount;//差異欄位數
        int errorCount = diffCount + missCount + extraCount;

        double accuracyPercent = 0.0;
        accuracyPercent = 100.0 - Math.round(errorCount * 10000.0 / botTotal) / 100.0;
        System.out.println("accuracyPercent = " + accuracyPercent + "%");
        String note = "";

        return new CompareResultBean(fileName, botTotal, misTotal, diffCount, diffColCount, missCount, extraCount, accuracyPercent, note);
    }

}
