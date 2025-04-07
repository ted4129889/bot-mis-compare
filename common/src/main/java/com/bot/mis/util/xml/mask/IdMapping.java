/* (C) 2024 */
package com.bot.mis.util.xml.mask;

import com.bot.mis.util.xml.mask.xmltag.Mapping;
import com.bot.mis.util.xml.vo.XmlData;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IdMapping {
    @Value("${localFile.mis.xml.mask.convert}")
    private String xmlFileDir;

    private static final String UNIFIED_NUMBER = "UNIFIED_NUMBER";

    private HashMap<Integer, String> mappingHashMap;
    private HashMap<Integer, String> unifiedMappingHashMap;

    @PostConstruct
    public void init() throws IOException {
        loadMappings();
    }

    private void loadMappings() throws IOException {
        String randomMask = xmlFileDir + "randomMask.xml";
        String unifiedMask = xmlFileDir + "unifiedMask.xml";
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
}
