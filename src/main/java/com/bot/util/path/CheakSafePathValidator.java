package com.bot.util.path;


import com.bot.filter.CheakSafePathUtil;
import org.springframework.stereotype.Component;

@Component
public class CheakSafePathValidator implements PathValidator {
    @Override
    public boolean isSafe(String basePath, String targetPath) {
        return CheakSafePathUtil.isSafeFilePath(basePath, targetPath);
    }
}
