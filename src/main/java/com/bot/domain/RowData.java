package com.bot.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Getter
@Setter
@ToString
public class RowData {
    // 原始整行字串
    private String raw;
    // key字串
    private String keyRaw;
    // key hash
    private String keyHash;
    // 整筆 hash
    private String fullHash;
    // 欄位拆解
    private Map<String, String> fieldMap;

    public RowData() {}
    public RowData(String raw, String keyRaw,String keyHash, String fullHash, Map<String, String> fieldMap) {
        this.raw = raw;
        this.keyRaw = keyRaw;
        this.keyHash = keyHash;
        this.fullHash = fullHash;
        this.fieldMap = fieldMap;
    }

    // ObjectMapper
    private static final ObjectMapper mapper = new ObjectMapper();

    /** 序列化成 JSON */
    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("RowData.toJson() error: " + e.getMessage(), e);
        }
    }

    /** JSON 字串 → RowData */
    public static RowData fromJson(String json) {
        try {
            return mapper.readValue(json, RowData.class);
        } catch (Exception e) {
            throw new RuntimeException("RowData.fromJson error: " + e.getMessage(), e);
        }
    }
}
