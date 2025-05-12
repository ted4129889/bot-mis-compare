/* (C) 2024 */
package com.bot.util.xml.vo;

import com.bot.util.xml.mask.xmltag.Field;
import com.bot.util.xml.mask.xmltag.Table;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JacksonXmlRootElement(localName = "file")
public class XmlFile {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "data")
    private List<XmlData> dataList = new ArrayList<>();


}
