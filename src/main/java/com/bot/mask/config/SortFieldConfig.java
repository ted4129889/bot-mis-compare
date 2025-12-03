package com.bot.mask.config;


import java.util.Comparator;


public class SortFieldConfig {
    private String fieldName;
    private boolean ascending;
    private int orderIndex;

    //
    public SortFieldConfig() {
    }
    public SortFieldConfig(String fieldName, boolean ascending, int orderIndex) {
        this.fieldName = fieldName;
        this.ascending = ascending;
        this.orderIndex = orderIndex;
    }

    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public boolean isAscending() {
        return ascending;
    }
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }
    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
    // 可選：依 orderIndex 排序
    public static Comparator<SortFieldConfig> sortByOrder() {
        return Comparator.comparingInt(SortFieldConfig::getOrderIndex);
    }
}
