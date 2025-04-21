/* (C) 2024 */
package com.bot.util.xml.mask;

import com.bot.log.LogProcess;
import com.bot.util.files.TextFileUtil;
import com.bot.util.xml.mask.xmltag.Mapping;
import com.bot.util.xml.vo.XmlData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

@Component
public class IdMapping {
    @Value("${localFile.mis.xml.mask.convert}")
    private String xmlFileDir;

    public static TextFileUtil textFileUtil = new TextFileUtil();
    private static final String UNIFIED_NUMBER = "UNIFIED_NUMBER";
    private static final String CHARSET_BIG5 = "BIG5";
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String version = "1.0";
    private HashMap<Integer, String> mappingHashMap;
    private HashMap<Integer, String> unifiedMappingHashMap;

    @PostConstruct
    public void init() throws IOException {
        loadMappings();
    }

    public void loadMappings() throws IOException {
        String randomMask = xmlFileDir + "randomMask.xml";
        String unifiedMask = xmlFileDir + "unifiedMask.xml";

        File randomMaskFile = new File(randomMask);
        if (!randomMaskFile.exists()) {
            generateRandomXml(randomMask);
            LogProcess.info("config file is not exist,gen: " + randomMaskFile);
        } else {
            LogProcess.info("config file is exist: " + randomMaskFile);
        }

        File unifiedMaskFile = new File(unifiedMask);
        if (!unifiedMaskFile.exists()) {
            generateUnifiedXml(unifiedMask);
            LogProcess.info("config file is not exist,gen: " + unifiedMaskFile);
        } else {
            LogProcess.info("config file is exist: " + unifiedMaskFile);
        }

        XmlParser xmlParser = new XmlParser();
        XmlData randomMaskData = xmlParser.parseXmlFile(randomMask);
        XmlData unifiedMaskData = xmlParser.parseXmlFile(unifiedMask);

        mappingHashMap = new HashMap<>();
        unifiedMappingHashMap = new HashMap<>();
        List<Mapping> mappings = randomMaskData.getMappingList();
        List<Mapping> unifiedMappings = unifiedMaskData.getMappingList();

        for (Mapping mapping : mappings) {
            mappingHashMap.put(mapping.getNumber(), mapping.getChara());
        }

        for (Mapping unifiedMapping : unifiedMappings) {
            unifiedMappingHashMap.put(unifiedMapping.getNumber(), unifiedMapping.getChara());
        }
    }

    public HashMap<Integer, String> getMapping(String idType) {
        return UNIFIED_NUMBER.equals(idType) ? unifiedMappingHashMap : mappingHashMap;
    }

    private void generateXml(String fileName, Function<Integer, String> charaGenerator) {
        List<String> mappings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String chara = charaGenerator.apply(i);
            mappings.add(buildMappingXml(i, chara));
        }
        String content = wrapWithXml(mappings);
        textFileUtil.writeFileContent(fileName, List.of(content), CHARSET_UTF8);
    }

    private void generateRandomXml(String fileName) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$*?";
        SecureRandom random = new SecureRandom();
        generateXml(fileName, i -> {
            String tmp = chars.replace(String.valueOf(i), "");
            char selected = tmp.charAt(random.nextInt(tmp.length()));
            return String.valueOf(selected);
        });
    }

    private void generateUnifiedXml(String fileName) {
        String chars = "0123456789";
        SecureRandom random = new SecureRandom();
        int offset = random.nextInt(chars.length());
        generateXml(fileName, i -> String.valueOf((i + offset) % 10));
    }

    private String wrapWithXml(List<String> mappings) {
        StringBuilder s = new StringBuilder();
        s.append("<?xml version=\"").append(version).append("\" encoding=\"").append(CHARSET_UTF8).append("\"?>\r\n");
        s.append("<data>\r\n");
        mappings.forEach(s::append);
        s.append("</data>\r\n");
        return s.toString();
    }

    private String buildMappingXml(int number, String chara) {
        return "    <mapping>\r\n" +
                "        <number>" + number + "</number>\r\n" +
                "        <chara>" + chara + "</chara>\r\n" +
                "    </mapping>\r\n";
    }


}
