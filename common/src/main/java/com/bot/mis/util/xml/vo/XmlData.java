/* (C) 2024 */
package com.bot.mis.util.xml.vo;

import com.bot.mis.util.xml.mask.xmltag.Field;
import com.bot.mis.util.xml.mask.xmltag.Mapping;
import com.bot.mis.util.xml.mask.xmltag.Table;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class XmlData {

    @JacksonXmlProperty(localName = "txt")
    private XmlBody txt;

    @JacksonXmlProperty(localName = "sql")
    private XmlSql sql;

    @JacksonXmlProperty(localName = "header")
    private XmlHeader header;

    @JacksonXmlProperty(localName = "body")
    private XmlBody body;

    @JacksonXmlProperty(localName = "table")
    private Table table;

    // mask
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "field")
    private List<Field> fieldList = new ArrayList<>();

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "mapping")
    private List<Mapping> mappingList = new ArrayList<>();

    // Getters and Setters
}
