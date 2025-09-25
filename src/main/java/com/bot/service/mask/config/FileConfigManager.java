package com.bot.service.mask.config;


import com.bot.util.log.LogProcess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
@Slf4j
@Component
public class FileConfigManager {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File CONFIG_FILE = new File("external-config/json/FieldSetting.json");

    @Value("${localFile.mis.xml.mask.convert}")
    private String xmlFileDir;

    private static Map<String, FileConfig> fieldSettingMap = new LinkedHashMap<>();


    public static Map<String, FileConfig> getConfigMap() {
        fieldSettingMap = load();
        return fieldSettingMap;
    }

    /**
     * 載入 JSON 檔案
     */
    public static Map<String, FileConfig> load() {

        try {
            if (!CONFIG_FILE.exists() || CONFIG_FILE.length() == 0) {
                return new LinkedHashMap<>();
            }
            return mapper.readValue(CONFIG_FILE, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    /**
     * 儲存 JSON 檔案
     */

    public static void save(Map<String, FileConfig> configMap) {

        try {
            if (configMap == null || configMap.isEmpty()) {
                LogProcess.info(log,"configMap is empty. Skipping save.");
                return;
            }
//            LogProcess.info("正在寫入 configMap = " + mapper.writeValueAsString(configMap));

            mapper.writerWithDefaultPrettyPrinter().writeValue(CONFIG_FILE, configMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *     一鍵初始化所有檔案設定 & 寫入 JSON
      */
    public static void ensureAllFilesExistAndSave(List<String> allFileNames) {


        Map<String, FileConfig> configMap = load();
//        LogProcess.info("configMap =" + configMap);

        // 檢查每個檔案名稱
        for (String fileName : allFileNames) {
            if (!configMap.containsKey(fileName)) {
                // 不存在 → 補上一個空的 FileConfig
                FileConfig emptyConfig = new FileConfig();
                emptyConfig.setPrimaryKeys(new ArrayList<>());
                emptyConfig.setSortFields(new ArrayList<>());
                configMap.put(fileName, emptyConfig);
            }
        }

        // 寫回 JSON
        save(configMap);
    }

    /**
     * 在json檔案中找指定的檔案名稱的欄位設定
     */
    public static Optional<FileConfig> getConfigByFileName(String fileName) {
        Map<String, FileConfig> configMap = load(); // 載入整個 JSON 檔案
        LogProcess.info(log,"load configMap ==" + configMap.toString());


        return Optional.ofNullable(configMap.get(fileName)); // 根據檔名找設定
    }

    /**
     * 覆寫欄位
     */
    public static void updateOneFile(Map<String, FileConfig> updData) {

        if (updData != null && updData != null) {
            save(updData);
        } else {
            LogProcess.info(log,"Skip update: fileName or config is null");
        }
        ;
    }
}