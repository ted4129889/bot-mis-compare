package com.bot.dataprocess;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class JobResult {
    private boolean success;
    private String message;
    private Map<String, Path> outputFilesMap = new HashMap<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Path> getOutputFilesMap() {
        return outputFilesMap;
    }

    public void putOutputFileMap(String fileName, Path path) {
        this.outputFilesMap.put(fileName, path);
    }
}
