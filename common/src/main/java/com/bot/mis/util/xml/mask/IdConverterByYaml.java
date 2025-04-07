/* (C) 2025 */
package com.bot.mis.util.xml.mask;

import java.io.InputStream;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class IdConverterByYaml {

    private char[] idArray;
    private char[] unifiedArray;

    @Value("${idConvertFile}")
    String yamlFileName;

    private void IdConverterByYaml() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream =
                getClass().getClassLoader().getResourceAsStream(yamlFileName)) {
            if (inputStream == null) {
                throw new IllegalStateException("couldn't find YAML, please check " + yamlFileName);
            }

            var config = yaml.load(inputStream);
            var idMapping = (Map<String, String>) ((Map<?, ?>) config).get("ID");
            var unifiedMapping = (Map<String, String>) ((Map<?, ?>) config).get("Unified");

            if (idMapping == null || unifiedMapping == null) {
                throw new IllegalStateException("YAML 格式錯誤，缺少 ID 或 Unified 配置！");
            }

            idArray = new char[10];
            unifiedArray = new char[10];
            for (int i = 0; i <= 9; i++) {
                idArray[i] = idMapping.getOrDefault(String.valueOf(i), String.valueOf(i)).charAt(0);
                unifiedArray[i] =
                        unifiedMapping.getOrDefault(String.valueOf(i), String.valueOf(i)).charAt(0);
            }
        } catch (Exception e) {
            throw new RuntimeException("load YAML error", e);
        }
    }

    public String maskIdDigits(String idNumber) {
        StringBuilder idResult = new StringBuilder();
        for (char c : idNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                idResult.append(idArray[c - '0']);
            } else {
                idResult.append(c);
            }
        }
        return idResult.toString();
    }

    public String maskUnifiedDigits(String idNumber) {
        StringBuilder unifiedResult = new StringBuilder();
        for (char c : idNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                unifiedResult.append(unifiedArray[c - '0']);
            } else {
                unifiedResult.append(c);
            }
        }
        return unifiedResult.toString();
    }
}
