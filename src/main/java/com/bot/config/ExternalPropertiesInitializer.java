/* (C) 2025 */
package com.bot.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executor;
public class ExternalPropertiesInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        Path configPath = Paths.get("external-parameters.properties");

        if (Files.notExists(configPath)) {
            try {
                Files.writeString(
                        configPath,
                        "SEPARATOR=;\n",
                        StandardOpenOption.CREATE_NEW
                );
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to create external-parameters.properties", e);
            }
        }
    }
}
