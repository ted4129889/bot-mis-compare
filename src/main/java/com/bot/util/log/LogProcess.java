package com.bot.util.log;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogProcess {

    private static final Logger logger = LoggerFactory.getLogger(LogProcess.class);

    public static void info(String message) {
        logger.info(message);
    }


    public static void warn(String message) {
        logger.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

}
