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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CompareExecService {

    private static final Charset DATA_CHARSET = Charset.forName("MS950");
    private static final int LOG_PROGRESS_SIZE = 100000;
    private static final int OUTPUT_PAGE_SIZE = 500000;

    private int aCount = 0;
    private int bCount = 0;
    private int missCount = 0;
    private int extraCount = 0;
    private int diffCount = 0;

    String missTxt = "_miss.txt";
    String extraTxt = "_extra.txt";
    String diffTxt = "_diff.txt";

    @Value("${common.separator}")
    private String SEPARATOR;

    @Value("${localFile.mis.compare_result.main}")
    private String resultMain;

    @Autowired
    private FormatData formatData;

    @Autowired
    private CompareResultRpt compareResultRpt;

    public CompareResultBean compare(Path fileA, Path fileB, List<FieldDef> defs, String fileName, String fileType) throws Exception {
        LineParser parser = new LineParser();
        String finalFileName = timestampedFileName(fileName);
        Path outputFolder = resultFolder(fileName, fileType);

        PagedOutput missOutput = new PagedOutput(outputFolder, finalFileName, missTxt);
        PagedOutput extraOutput = new PagedOutput(outputFolder, finalFileName, extraTxt);
        PagedOutput diffOutput = new PagedOutput(outputFolder, finalFileName, diffTxt);

        try (RocksDbManager db = new RocksDbManager(rocksDbPath(outputFolder).toString());
             OutputReporter reporter = new OutputReporter()) {

            resetCounters();
            indexByPrimaryKey(fileA, defs, parser, db, finalFileName);
            compareByPrimaryKey(fileB, defs, parser, db, reporter, extraOutput, diffOutput, finalFileName);
            writeMissingByPrimaryKey(db, reporter, missOutput, finalFileName);

            return exportTextFile(Path.of(fileName).getFileName().toString());
        }
    }

    public CompareResultBean compare2(Path fileA, Path fileB, List<FieldDef> defs, String fileName, String fileType) throws Exception {
        LineParser parser = new LineParser();
        String finalFileName = timestampedFileName(fileName);
        Path outputFolder = resultFolder(fileName, fileType);

        Path missOutPutPath = outputPath(outputFolder, finalFileName, missTxt);
        Path extraOutPutPath = outputPath(outputFolder, finalFileName, extraTxt);
        Path diffOutPutPath = outputPath(outputFolder, finalFileName, diffTxt);

        try (RocksDbManager db = new RocksDbManager(rocksDbPath(outputFolder).toString());
             OutputReporter reporter = new OutputReporter()) {

            resetCounters();
            indexByFullHash(fileA, defs, parser, db, finalFileName);
            compareByFullHash(fileB, defs, parser, db, reporter, extraOutPutPath, diffOutPutPath, finalFileName);
            writeMissingByFullHash(db, reporter, missOutPutPath, finalFileName);

            return exportTextFile(Path.of(fileName).getFileName().toString());
        }
    }

    private void indexByPrimaryKey(Path fileA, List<FieldDef> defs, LineParser parser, RocksDbManager db, String finalFileName) throws Exception {
        try (BufferedReader br = newDataReader(fileA)) {
            String line;
            long count = 0;

            while ((line = br.readLine()) != null) {
                aCount++;

                RowData rawA = parser.parse(formatData.getReplaceSpace(line, " "), defs, SEPARATOR);
                if (rawA == null) continue;

                String value = "0|" + rawA.getFullHash() + "|" + rawA.toJson();
                db.put(rawA.getKeyHash(), value);

                if (++count % LOG_PROGRESS_SIZE == 0) {
                    // reserved for progress logging
                }
            }

            LogProcess.info(log, "bot file {} ,totalCnt = {} ", finalFileName, count);
        }
    }

    private void compareByPrimaryKey(
            Path fileB,
            List<FieldDef> defs,
            LineParser parser,
            RocksDbManager db,
            OutputReporter reporter,
            PagedOutput extraOutput,
            PagedOutput diffOutput,
            String finalFileName
    ) throws Exception {
        try (BufferedReader br = newDataReader(fileB)) {
            String line;
            long count = 0;

            while ((line = br.readLine()) != null) {
                bCount++;

                RowData rawB = parser.parse(formatData.getReplaceSpace(line, " "), defs, SEPARATOR);
                if (rawB == null) continue;

                String keyHash = rawB.getKeyHash();
                String fullHashB = rawB.getFullHash();
                String valueInA = db.get(keyHash);

                if (valueInA == null) {
                    extraCount++;
                    if (extraCount % OUTPUT_PAGE_SIZE == 0) {
                        extraOutput.advance();
                    }
                    reporter.writeExtra(rawB, extraOutput.current());
                } else {
                    String[] parts = valueInA.split("\\|", 3);
                    String fullHashA = parts[1];
                    String rawA = parts[2];

                    db.put(keyHash, "1|" + fullHashA + "|" + rawA);

                    if (!fullHashA.equals(fullHashB)) {
                        diffCount++;
                        if (diffCount % OUTPUT_PAGE_SIZE == 0) {
                            diffOutput.advance();
                        }

                        String diffResult = compareFields(RowData.fromJson(rawA), rawB, defs);
                        reporter.writeFieldDiff(diffResult, diffOutput.current());
                    }
                }

                if (++count % LOG_PROGRESS_SIZE == 0) {
                    // reserved for progress logging
                }
            }

            LogProcess.info(log, "mis file {} ,totalCnt = {} ", finalFileName, count);
            LogProcess.info(log, "mis file {} ,extraCount = {} ", finalFileName, extraCount);
            LogProcess.info(log, "bot file vs. mis file,diffCount = {} ", diffCount);
        }
    }

    private void writeMissingByPrimaryKey(RocksDbManager db, OutputReporter reporter, PagedOutput missOutput, String finalFileName) throws IOException {
        RocksIterator it = db.newIterator();
        try {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                String value = new String(it.value());
                String[] parts = value.split("\\|", 3);
                String flag = parts[0];
                String rawA = parts[2];

                if ("0".equals(flag)) {
                    missCount++;
                    if (missCount % OUTPUT_PAGE_SIZE == 0) {
                        missOutput.advance();
                    }
                    reporter.writeMissing(rawA, missOutput.current());
                }
            }
        } finally {
            it.close();
        }

        LogProcess.info(log, "mis file {} ,missCount = {} ", finalFileName, missCount);
    }

    private void indexByFullHash(Path fileA, List<FieldDef> defs, LineParser parser, RocksDbManager db, String finalFileName) throws Exception {
        long seq = 0;

        try (BufferedReader br = newDataReader(fileA)) {
            String line;
            long count = 0;

            while ((line = br.readLine()) != null) {
                aCount++;

                RowData rawA = parser.parse(formatData.getReplaceSpace(line, " "), defs, SEPARATOR);
                String hash = rawA.getFullHash();
                String key = hash + "|" + String.format("%010d", seq++);
                String value = "0|" + hash + "|" + rawA.toJson();

                db.put(key, value);

                if (++count % LOG_PROGRESS_SIZE == 0) {
                    // reserved for progress logging
                }
            }

            LogProcess.info(log, "bot file {} ,totalCnt = {} ", finalFileName, count);
        }
    }

    private void compareByFullHash(
            Path fileB,
            List<FieldDef> defs,
            LineParser parser,
            RocksDbManager db,
            OutputReporter reporter,
            Path extraOutPutPath,
            Path diffOutPutPath,
            String finalFileName
    ) throws Exception {
        try (BufferedReader br = newDataReader(fileB)) {
            String line;
            long count = 0;
            RocksIterator it = db.newIterator();

            try {
                while ((line = br.readLine()) != null) {
                    bCount++;

                    RowData rawB = parser.parse(formatData.getReplaceSpace(line, " "), defs, SEPARATOR);
                    String hashB = rawB.getFullHash();
                    String searchKey = hashB + "|";

                    it.seek(searchKey.getBytes(StandardCharsets.UTF_8));

                    boolean matched = false;
                    while (it.isValid()) {
                        String foundKey = new String(it.key());
                        if (!foundKey.startsWith(searchKey)) break;

                        String valueInA = new String(it.value());
                        String[] parts = valueInA.split("\\|", 3);
                        String flag = parts[0];
                        String hashA = parts[1];
                        String rawAJson = parts[2];

                        if ("0".equals(flag)) {
                            matched = true;
                            db.put(it.key().toString(), "1|" + hashA + "|" + rawAJson);

                            if (!hashA.equals(hashB)) {
                                diffCount++;
                                reporter.writeFieldDiff(
                                        compareFields(RowData.fromJson(rawAJson), rawB, defs),
                                        diffOutPutPath
                                );
                            }
                            break;
                        }

                        it.next();
                    }

                    if (!matched) {
                        extraCount++;
                        reporter.writeExtra(rawB, extraOutPutPath);
                    }

                    if (++count % LOG_PROGRESS_SIZE == 0) {
                        // reserved for progress logging
                    }
                }
            } finally {
                it.close();
            }

            LogProcess.info(log, "mis file {} ,totalCnt = {} ", finalFileName, count);
            LogProcess.info(log, "mis file {} ,extraCount = {} ", finalFileName, extraCount);
            LogProcess.info(log, "bot file vs. mis file,diffCount = {} ", diffCount);
        }
    }

    private void writeMissingByFullHash(RocksDbManager db, OutputReporter reporter, Path missOutPutPath, String finalFileName) throws IOException {
        RocksIterator it = db.newIterator();
        try {
            for (it.seekToFirst(); it.isValid(); it.next()) {
                String value = new String(it.value());
                String[] parts = value.split("\\|", 3);
                String flag = parts[0];
                String rawAJson = parts[2];

                if ("0".equals(flag)) {
                    missCount++;
                    reporter.writeMissing(rawAJson, missOutPutPath);
                }
            }
        } finally {
            it.close();
        }

        LogProcess.info(log, "mis file {} ,missCount = {} ", finalFileName, missCount);
    }

    private BufferedReader newDataReader(Path file) throws IOException {
        return Files.newBufferedReader(file, DATA_CHARSET);
    }

    private void resetCounters() {
        aCount = 0;
        bCount = 0;
        missCount = 0;
        extraCount = 0;
        diffCount = 0;
    }

    private String timestampedFileName(String fileName) {
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return fileName + "_" + dateTimeStr;
    }

    private Path resultFolder(String fileName, String fileType) {
        return Path.of(resultMain).resolve(fileType).resolve(fileName);
    }

    private Path pagedOutputPath(Path outputFolder, String finalFileName, int page, String suffix) {
        return outputFolder.resolve(finalFileName + "_" + page + suffix);
    }

    private Path outputPath(Path outputFolder, String finalFileName, String suffix) {
        return outputFolder.resolve(finalFileName + suffix);
    }

    private Path rocksDbPath(Path outputFolder) {
        return outputFolder.resolve(Path.of("rocksdb", "A_DB"));
    }

    private class PagedOutput {
        private final Path outputFolder;
        private final String finalFileName;
        private final String suffix;
        private int page = 1;
        private Path currentPath;

        private PagedOutput(Path outputFolder, String finalFileName, String suffix) {
            this.outputFolder = outputFolder;
            this.finalFileName = finalFileName;
            this.suffix = suffix;
            this.currentPath = pagedOutputPath(outputFolder, finalFileName, page, suffix);
        }

        private Path current() {
            return currentPath;
        }

        private void advance() {
            page++;
            currentPath = pagedOutputPath(outputFolder, finalFileName, page, suffix);
        }
    }

    private String compareFields(RowData a, RowData b, List<FieldDef> defs) {
        List<String> groupNames = new ArrayList<>();
        StringBuilder fieldName = new StringBuilder();

        for (FieldDef def : defs) {
            if (!def.getName().contains("separator")) {
                fieldName.append(def.getName()).append("+");
            }

            if (def.getName().contains("separator")) {
                String resultKey = fieldName.toString().trim();
                resultKey = resultKey.substring(0, resultKey.length() - 1);
                groupNames.add(resultKey);
                fieldName = new StringBuilder();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("key : ").append(a.getKeyRaw()).append("\n");
        sb.append("bot data : ").append(a.getFieldMap()).append("\n");
        sb.append("mis data : ").append(b.getFieldMap()).append("\n");

        for (String groupName : groupNames) {
            String v1 = a.getFieldMap().get(groupName);
            String v2 = b.getFieldMap().get(groupName);

            if (!Objects.equals(v1, v2)) {
                sb.append(String.format(
                        "欄位[%s] 不同: bot='%s', mis='%s'%n",
                        groupName, v1, v2));
            }
        }

        sb.append("----------------------------------\n");
        return sb.toString();
    }

    private CompareResultBean exportTextFile(String fileName) {
        int botTotal = aCount;
        int misTotal = bCount;
        int diffColCount = diffCount;
        int errorCount = diffCount + missCount + extraCount;

        double accuracyPercent = botTotal == 0
                ? 0.0
                : 100.0 - Math.round(errorCount * 10000.0 / botTotal) / 100.0;
        String note = "";

        return new CompareResultBean(fileName, botTotal, misTotal, diffCount, diffColCount, missCount, extraCount, accuracyPercent, note);
    }
}
