/* (C) 2024 */
package com.bot.util.xml.mask.xmltag;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Mapping {
    @JacksonXmlProperty(localName = "number")
    private Integer number;

    @JacksonXmlProperty(localName = "chara")
    private String chara;

    @JacksonXmlProperty(localName = "convNum")
    private Integer convNum;

//    public Mapping() {}
//    public Mapping(int number, String chara) {
//        this.number = number;
//        this.chara = chara;
//    }
}
