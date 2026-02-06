package com.bot.mask.config;


import com.bot.util.log.LogProcess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
@Slf4j
@Component
@EqualsAndHashCode
public class FileConfigManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final File CONFIG_FILE =
            new File("external-config/json/FieldSetting.json");

    // 👉 唯一的快取來源
    private static Map<String, FileConfig> fieldSettingMap = new LinkedHashMap<>();


    /*  啟動初始化  */

    public static void loadAll() {
        fieldSettingMap = loadFromFile();
    }


    private static Map<String, FileConfig> loadFromFile() {

        try {
            if (!CONFIG_FILE.exists() || CONFIG_FILE.length() == 0) {
                return new LinkedHashMap<>();
            }

            return mapper.readValue(CONFIG_FILE, new TypeReference<>() {});

        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }


    public static Map<String, FileConfig> getConfigMap() {
        return fieldSettingMap;
    }


    public static FileConfig get(String fileName) {
        return fieldSettingMap.get(fileName);
    }


    public static void put(String fileName, FileConfig config) {
        fieldSettingMap.put(fileName, config);
    }


    /*  儲存  */

    public static void saveToFile() {

        try {
            if (fieldSettingMap == null || fieldSettingMap.isEmpty()) {
                LogProcess.info(log, "configMap is empty. Skip save.");
                return;
            }

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(CONFIG_FILE, fieldSettingMap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*  初始化 */
    public static void ensureAllFilesExist(List<String> allFileNames) {

        for (String fileName : allFileNames) {

            if (!fieldSettingMap.containsKey(fileName)) {

                FileConfig emptyConfig = new FileConfig();
                emptyConfig.setPrimaryKeys(new ArrayList<>());
                emptyConfig.setSortFields(new ArrayList<>());

                fieldSettingMap.put(fileName, emptyConfig);
            }
        }

        saveToFile();
    }


}