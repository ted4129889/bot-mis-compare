/* (C) 2024 */
package com.bot.mis.util.xml.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class XmlField {

    @JacksonXmlProperty(localName = "id")
    private String id;

    @JacksonXmlProperty(localName = "fieldName")
    private String fieldName;

    @JacksonXmlProperty(localName = "fieldType")
    private String fieldType;

    @JacksonXmlProperty(localName = "format")
    private String format;

    @JacksonXmlProperty(localName = "length")
    private String length;

    @JacksonXmlProperty(localName = "align")
    private String align;

    @JacksonXmlProperty(localName = "oTableName")
    private String oTableName;

    @JacksonXmlProperty(localName = "oFieldName")
    private String oFieldName;

    // Getters and Setters
}
