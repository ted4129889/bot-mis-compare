package com.bot.util.templates;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

//TODO 還沒寫好
public class TemplateUtil {

    public static String fillTemplate(String resourcePath, Map<String, String> values) throws IOException {
        InputStream inputStream = TemplateUtil.class.getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new FileNotFoundException("Template not found: " + resourcePath);
        }
        String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        return fillTemplateContent(template, values);
    }

    public static String fillTemplateContent(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }
}
