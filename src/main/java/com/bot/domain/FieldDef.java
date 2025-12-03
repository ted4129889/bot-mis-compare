package com.bot.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@ToString
public class FieldDef {

    //欄位名稱
    private final String name;
    //起始位置
    private final int start;
    //長度
    private final int length;
    //是否為key
    private final boolean isKey;

    public FieldDef(String name, int start, int length, boolean isKey) {
        this.name = name;
        this.start = start;
        this.length = length;
        this.isKey = isKey;
    }

}
