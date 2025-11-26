package com.bot.dataprocess;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.util.Map;

@Slf4j

public record JobContext(
        Path inputDir,          // 來源資料夾
        Path outputDir,         // 輸出資料夾
        String inputFileName //來源檔案名稱
) {}