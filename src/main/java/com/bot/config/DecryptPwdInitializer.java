/* (C) 2025 */
package com.bot.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class DecryptPwdInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String SALT = "5c0744940b5c369b"; // 一個隨機鹽值（每次加密可以使用相同的鹽值）
    private static final String VAR_PW = "spring.datasource.password";
    private static final String VAR_ENC_SECRET_KEY = "spring.datasource.enc-secret-key";
    private static final String DEC_PW_NAME = "decrypted-password-properties";
    private static final String PROFILE = "spring.profiles.active";
    private static final String LOCAL = "local";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // 取得應用環境
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        // 讀取加密的密碼
        String encryptedPassword = environment.getProperty(VAR_PW);
        String secretKey = environment.getProperty(VAR_ENC_SECRET_KEY);
        String profile = environment.getProperty(PROFILE);

        //        if (!LOCAL.equals(profile)) {
        // 若加密密碼存在，進行解密
        if (encryptedPassword != null) {

            // 使用同加密時使用的金鑰及鹽值
            TextEncryptor encryptor = Encryptors.text(secretKey, SALT);
            String decryptedPassword = encryptor.decrypt(encryptedPassword);

            // 設置解密後的密碼到 Spring 環境中
            Map<String, Object> properties = new HashMap<>();
            properties.put(VAR_PW, decryptedPassword);
            environment
                    .getPropertySources()
                    .addFirst(new MapPropertySource(DEC_PW_NAME, properties));

        }
    }
    //    }
}
