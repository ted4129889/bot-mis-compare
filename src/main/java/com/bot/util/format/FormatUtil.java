//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bot.util.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.Character.UnicodeBlock;
import java.util.Objects;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FormatUtil {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(FormatUtil.class);

    public FormatUtil() {
    }

    public String padX(String s, int n) {
        if (Objects.isNull(s)) {
            s = "";
        }

        s = s.replace("\t", "");
        char[] ch = s.toCharArray();
        int i = n;
        int len = n;
        char[] var6 = ch;
        int var7 = ch.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            char c = var6[var8];
            if ((this.isChinese(c) || !this.isPrintableAsciiChar(c)) && c != '\n') {
                --i;
                len -= 2;
            } else {
                --len;
            }

            if (len <= 0) {
                break;
            }
        }

        String re = s.length() >= i ? s.substring(0, i) : s;
        if (len == -1) {
            ++i;
        }

        return String.format("%1$-" + i + "s", re);
    }

    public String padLeft(String s, int width) {
        return String.format("%" + width + "s", s);
    }

    public String pad9(String n, int width) {
        String format = String.format("%%0%dd", width);
        String var10000 = String.format(format, 0);
        String result = var10000 + n;
        return this.right(result, width);
    }

    public String rightPad9(String n, int width) {
        String format = String.format("%%0%dd", width);
        String result = n + String.format(format, 0);
        return this.left(result, width);
    }

    public String right(String s, int width) {
        return s.length() <= width ? s : s.substring(s.length() - width);
    }

    public String left(String s, int width) {
        return s.length() <= width ? s : s.substring(0, width);
    }

    public String pad9(String n, int width, int afterDecimalPoint) {
        if (Objects.isNull(n)) {
            n = "0";
        }

        String[] ss = n.split("\\.");
        String s1 = ss[0];
        String s2 = "0";
        if (ss.length > 1) {
            s2 = ss[1];
        }

        String result = this.pad9(s1, width);
        if (afterDecimalPoint > 0) {
            result = result + this.rightPad9(s2, afterDecimalPoint);
        }

        if (result.length() > width + afterDecimalPoint) {
            result = result.substring(width + afterDecimalPoint - result.length() - 1);
        }

        return result;
    }

    public <T> String vo2JsonString(T sourceVo) {
        try {
            return new ObjectMapper().writeValueAsString(sourceVo);
        } catch (Throwable var3) {
            return var3.toString();
        }
    }

    public <T> T jsonString2Vo(String text, Class<T> sourceVo) {
        try {
            return !Objects.isNull(text) && !text.trim().isEmpty() ? (new ObjectMapper()).readValue(text, sourceVo) : sourceVo.getDeclaredConstructor().newInstance();
        } catch (Throwable var4) {
            return null;
        }
    }

    private boolean isChinese(char c) {
        if ("「」".indexOf(c) != -1) {
            return false;
        } else {
            Character.UnicodeBlock ub = UnicodeBlock.of(c);
            return ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == UnicodeBlock.GENERAL_PUNCTUATION;
        }
    }

    private boolean isPrintableAsciiChar(char ch) {
        if ("「」".indexOf(ch) != -1) {
            return true;
        } else {
            return ' ' <= ch && ch <= '~';
        }
    }
}
