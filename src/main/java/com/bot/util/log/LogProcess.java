package com.bot.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.function.Consumer;

public final class LogProcess {

    private static final Logger logger = LoggerFactory.getLogger(LogProcess.class);

    // UI 消費者（例如 TextArea::appendText）
    private static volatile Consumer<String> uiLogger;

    // UI 最小顯示等級（預設 INFO）。可依需求調整。
    public enum Level { TRACE, DEBUG, INFO, WARN, ERROR, OFF }
    private static volatile Level uiMinLevel = Level.INFO;

    private LogProcess() {}

    /** 註冊 UI logger（傳 null 等於關閉 UI 輸出） */
    public static void setUiLogger(Consumer<String> consumer) {
        uiLogger = consumer;
    }

    /** 設定 UI 最小等級（例如在 dev 顯示 DEBUG，在 prod 顯示 INFO） */
    public static void setUiMinLevel(Level level) {
        uiMinLevel = (level == null) ? Level.OFF : level;
    }

    // ================== INFO ==================
    public static void info(Logger logger ,String message, Object... args) {
        if (logger.isInfoEnabled()) logger.info(message, args);
        logToUI(Level.INFO, "[INFO]",logger, message, args);
    }

    // ================== DEBUG ==================
    public static void debug(Logger logger ,String message, Object... args) {
        if (logger.isDebugEnabled()) logger.debug(message, args);
        logToUI(Level.DEBUG, "[DEBUG]",logger, message, args);
    }

    // ================== TRACE ==================
    public static void trace(Logger logger ,String message, Object... args) {
        if (logger.isTraceEnabled()) logger.trace(message, args);
        logToUI(Level.TRACE, "[TRACE]",logger, message, args);
    }

    // ================== WARN ==================
    public static void warn(Logger logger ,String message, Object... args) {
        if (logger.isWarnEnabled()) logger.warn(message, args);
        logToUI(Level.WARN, "[WARN]",logger, message, args);
    }

    public static void warn(Logger logger ,String message, Throwable t) {
        if (logger.isWarnEnabled()) logger.warn(message, t);
        logToUI(Level.WARN, "[WARN]",logger, messageWithThrowable(message, t));
    }

    // ================== ERROR ==================
    public static void error(Logger logger ,String message, Object... args) {
        if (logger.isErrorEnabled()) logger.error(message, args);
        logToUI(Level.ERROR, "[ERROR]",logger, message, args);
    }

    public static void error(Logger logger ,String message, Throwable t) {
        if (logger.isErrorEnabled()) logger.error(message, t);
        logToUI(Level.ERROR, "[ERROR]",logger, messageWithThrowable(message, t));
    }

    // ================== Helpers ==================

    private static void logToUI(Level level, String prefix, Logger logger ,String message, Object... args) {
        Consumer<String> sink = uiLogger;
        if (sink == null) return;                   // 未註冊就略過
        if (!uiLevelEnabled(level)) return;         // 低於 UI 最小等級就不印

        // 使用 SLF4J 的 MessageFormatter 正確處理 {} 參數（含跳脫）
        String formatted = (args == null || args.length == 0)
                ? message
                : MessageFormatter.arrayFormat(message, args).getMessage();

        sink.accept(prefix + " " + formatted);
    }

    private static boolean uiLevelEnabled(Level level) {
        if (uiMinLevel == Level.OFF) return false;
        // 依嚴重度排序：TRACE < DEBUG < INFO < WARN < ERROR
        int v = ord(level), min = ord(uiMinLevel);
        return v >= min;
    }

    private static int ord(Level l) {
        return switch (Objects.requireNonNull(l)) {
            case TRACE -> 0;
            case DEBUG -> 1;
            case INFO  -> 2;
            case WARN  -> 3;
            case ERROR -> 4;
            case OFF   -> Integer.MAX_VALUE;
        };
    }

    private static String messageWithThrowable(String msg, Throwable t) {
        if (t == null) return msg;
        // 常用做法：UI 顯示簡短錯誤＋第一層 stack；避免整串太長
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(msg + " | " + t.getClass().getSimpleName() + ": " + t.getMessage());
        if (t.getStackTrace().length > 0) {
            pw.println(" at " + t.getStackTrace()[0]); // 只抓第一層
        }
        return sw.toString().trim();
    }
}
