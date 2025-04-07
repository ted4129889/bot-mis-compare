/* (C) 2024 */
package com.bot.mis.util.xml.vo;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class FieldProperties {
    String fieldName;
    String fieldType;
    String format;
    int length;
    String align;
    String oTableName;
    String oFieldName;
}
