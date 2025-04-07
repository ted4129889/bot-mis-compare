/* (C) 2024 */
package com.bot.mis.util.xml.mask.xmltag;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@NoArgsConstructor
public class Table {

    @JacksonXmlProperty(localName = "tableName")
    private String tableName;

    public Table(String tableName) {
        this.tableName = tableName;
    }
}
