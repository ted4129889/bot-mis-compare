package com.bot.service.output.templates;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CompareResultBean {
    private String fileName;
    private int botTotal;
    private int misTotal;
    private int diffColCount;
    private int diffCount;
    private int missCount;
    private int extraCount;
    private String note;

    public CompareResultBean(String fileName, int botTotal, int misTotal,int diffCount,  int diffColCount, int missCount, int extraCount, String note) {
        this.fileName = fileName;
        this.botTotal = botTotal;
        this.misTotal = misTotal;
        this.diffCount = diffCount;
        this.diffColCount = diffColCount;
        this.missCount = missCount;
        this.extraCount = extraCount;
        this.note = note;
    }

}
