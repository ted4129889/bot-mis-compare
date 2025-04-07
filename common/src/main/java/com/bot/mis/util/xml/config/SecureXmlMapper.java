/* (C) 2025 */
package com.bot.mis.util.xml.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javax.xml.stream.XMLInputFactory;

public class SecureXmlMapper {
    public static XmlMapper createXmlMapper() {
        // 創建 XMLInputFactory
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

        // 禁用 DTD 和外部實體 20250110
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false); // 禁用 DTD
        xmlInputFactory.setProperty(
                XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false); // 禁用外部實體

        // 傳遞安全的 XMLInputFactory 到 XmlMapper
        return new XmlMapper(xmlInputFactory);
    }
}
