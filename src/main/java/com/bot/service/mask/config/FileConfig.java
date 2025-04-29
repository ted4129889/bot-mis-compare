package com.bot.service.mask.config;


import java.util.ArrayList;
import java.util.List;


public class FileConfig {
    private List<String> primaryKeys = new ArrayList<>();

    // 改為記錄排序順序 + 升降序
    private List<SortFieldConfig> sortFields = new ArrayList<>();

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public List<SortFieldConfig> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<SortFieldConfig> sortFields) {
        this.sortFields = sortFields;
    }
}
