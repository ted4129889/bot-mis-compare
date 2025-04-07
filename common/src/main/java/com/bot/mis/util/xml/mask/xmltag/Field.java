/* (C) 2024 */
package com.bot.mis.util.xml.mask.xmltag;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class Field {
    @JacksonXmlProperty(localName = "fieldName")
    private String fieldName;

    @JacksonXmlProperty(localName = "maskType")
    private String maskType;
}
