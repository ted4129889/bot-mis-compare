/* (C) 2024 */
package com.bot.mis.util.xml.vo;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import jakarta.persistence.Entity;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class XmlSql {

    @JacksonXmlText private String sqlText;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "param")
    private List<String> paramList;
    // Getters and Setters
}
