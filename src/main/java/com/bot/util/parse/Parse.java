//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.bot.util.parse;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bot.util.format.FormatUtil;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Parse {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(Parse.class);
    private Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    @Autowired
    private FormatUtil formatUtil;

    public Parse() {
    }

    public <T> String decimal2String(T value, int precision, int scale) {
        String text = "";

        try {
            if (value instanceof Integer) {
                text = Integer.toString((Integer)value);
            } else if (value instanceof Double) {
                text = Double.toString((Double)value);
            } else if (value instanceof Float) {
                text = Float.toString((Float)value);
            } else if (value instanceof Long) {
                text = Long.toString((Long)value);
            } else {
                if (!(value instanceof BigDecimal)) {
                    return null;
                }

                text = value.toString();
            }

            String format = String.format("%%0%d.%df", precision, scale);
            return String.format(format, new BigDecimal(text));
        } catch (Exception var6) {
            return null;
        }
    }

    public <T> String decimal2StringPadZero(T value, int precision) {
        String text = "";

        try {
            if (value instanceof Integer) {
                text = Integer.toString((Integer)value);
            } else if (value instanceof Double) {
                text = Double.toString((Double)value);
            } else if (value instanceof Float) {
                text = Float.toString((Float)value);
            } else if (value instanceof Long) {
                text = Long.toString((Long)value);
            } else {
                if (!(value instanceof BigDecimal)) {
                    return null;
                }

                text = value.toString();
            }

            return this.formatUtil.pad9(text.trim().replaceAll("\\.", ""), precision);
        } catch (Exception var5) {
            return null;
        }
    }

    /** @deprecated */
    @Deprecated
    public BigDecimal stringToBigDecimal(String value) {
        BigDecimal res = null;

        try {
            res = new BigDecimal(value.replaceAll(",", "").trim());
        } catch (Exception var4) {
        }

        return res;
    }

    public BigDecimal string2BigDecimal(String value) {
        BigDecimal res = null;

        try {
            res = new BigDecimal(value.replaceAll(",", "").trim());
        } catch (Exception var4) {
        }

        return res;
    }

    public BigDecimal comp2BigDecimal(byte[] data, int scale) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.BIG_ENDIAN);
        long integerValue;
        switch (data.length) {
            case 2:
                integerValue = (long)buffer.getShort();
                break;
            case 4:
                integerValue = (long)buffer.getInt();
                break;
            case 8:
                integerValue = buffer.getLong();
                break;
            default:
                return null;
        }

        return BigDecimal.valueOf(integerValue).movePointLeft(scale);
    }

    public Long string2Long(String value) {
        try {
            Long l = Long.parseLong(value.trim());
            return l;
        } catch (Exception var4) {
            return null;
        }
    }

    public Integer string2Integer(String value) {
        try {
            Integer integer = Integer.parseInt(value.trim());
            return integer;
        } catch (Exception var4) {
            return null;
        }
    }

    public Short string2Short(String value) {
        Short shot = null;

        try {
            shot = Short.parseShort(value.trim());
        } catch (Exception var4) {
        }

        return shot;
    }

    public boolean isNumeric(String str) {
        if (Objects.isNull(str)) {
            return false;
        } else {
            Matcher m = this.pattern.matcher(str.trim());
            return m.matches();
        }
    }

}
