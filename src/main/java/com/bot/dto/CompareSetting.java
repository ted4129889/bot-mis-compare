package com.bot.dto;

import java.util.List;

public class CompareSetting {
    private boolean exportOnlyErrorFile;//
    private boolean exportUseMask;//
//    private boolean includeHeader;
//    private List<String> selectedFields;

    // 私有建構子（不直接 new，只能用 Builder）
    private CompareSetting(Builder builder) {
        this.exportOnlyErrorFile = builder.exportOnlyErrorFile;
        this.exportUseMask = builder.exportUseMask;
//        this.includeHeader = builder.includeHeader;
//        this.selectedFields = builder.selectedFields;
    }

    // 提供靜態 builder() 方法
    public static Builder builder() {
        return new Builder();
    }

    // Builder 類別
    public static class Builder {
        private boolean exportOnlyErrorFile;
        private boolean exportUseMask;

        public Builder exportOnlyErrorFile(boolean exportOnlyErrorFile) {
            this.exportOnlyErrorFile = exportOnlyErrorFile;
            return this;
        }

        public Builder exportUseMask(boolean exportUseMask) {
            this.exportUseMask = exportUseMask;
            return this;
        }


        public CompareSetting build() {
            return new CompareSetting(this);
        }
    }

    // Getter
    public boolean isExportOnlyErrorFile() {
        return exportOnlyErrorFile;
    }

    public boolean isExportUseMask() {
        return exportUseMask;
    }

}