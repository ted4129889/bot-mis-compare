/* (C) 2024 */
package com.bot.mis.util.string;

import static com.bot.txcontrol.mapper.MapperCase.formatUtil;

import com.bot.txcontrol.util.parse.Parse;
import com.bot.txcontrol.util.text.format.CutAndCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class StringUtil {

    ////////////////////  Shared components //////////////////////
    @Autowired private Parse parse;

    private String uscode;

    private StringUtil() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    public String substr(String str, int st, int end) {
        int slen = str.length();
        int len = end - st;
        if (slen == 0) return formatUtil.padX(" ", len);
        int gend = end;
        if (slen < gend) gend = slen;
        String tmp = "";
        // 取字串
        if (st < gend) {
            tmp = str.substring(st, gend);
        }
        // 補空白
        return String.format("%-" + len + "s", tmp);
    }

    public String substrBUR(String str, int st, int end) {
        int len = end - st;
        // 取字串
        String tmp = "";
        if (len >= 0) tmp = CutAndCount.stringCutBaseOnBUR(str, st, len);

        tmp = formatUtil.padX(tmp, len);
        // 補空白
        return tmp;
    }

    // 全形字前後補空白
    public String cnvBUR(String str) {
        String tmp = "";
        StringBuilder rest = new StringBuilder();
        boolean BURfg = false;
        boolean lBURfg = false;
        for (int i = 0; i < str.length(); i++) {
            tmp = str.substring(i, i + 1);
            if (CutAndCount.isHasWholeChar(tmp)) BURfg = true;
            else BURfg = false;
            if ((BURfg && !lBURfg) || (!BURfg && lBURfg)) {
                rest.append("~");
            }
            rest.append(tmp);
            lBURfg = BURfg;
        }
        if (lBURfg) rest.append(tmp);
        return rest.toString();
    }

    public String CutBUR(String str, int st, int end) {
        int len = end - st;
        boolean BURfg = false;
        boolean lBURfg = false;
        int rlen = 0;
        int lrlen = 0;
        String rest = "";
        // 取字串
        String tmp = "";
        if (len >= 0) rest = CutAndCount.stringCutBaseOnBUR(str, st, len);
        rest = formatUtil.padX(rest, len);
        for (int i = 0; i < rest.length(); i++) {
            tmp = rest.substring(i, i + 1);
            if (CutAndCount.isHasWholeChar(tmp)) BURfg = true;
            else BURfg = false;
            if ((BURfg && !lBURfg) || (!BURfg && lBURfg)) rlen++;
            if (BURfg) rlen += 2;
            else rlen++;
            if (rlen > len || (BURfg && rlen == len)) {
                rest = rest.substring(0, i) + formatUtil.padX(" ", len - lrlen);
                break;
            } else if (rlen == len) {
                rest = rest.substring(0, i + 1);
                break;
            } else {
                lBURfg = BURfg;
                lrlen = rlen;
            }
        }
        // 補空白
        return rest;
    }

    public String geterrpgm(String errmsg) {
        String errpgm = "";
        int st = errmsg.lastIndexOf(" [");
        if (st >= 0) errpgm = errmsg.substring(st);
        return errpgm;
    }

    public boolean isAlphabetic(String str) {
        char chr;
        for (int i = 0; i < str.length(); i++) {
            chr = str.charAt(i);
            if ((chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z') || chr == ' ') {
            } else return false;
        }
        return true;
    }

    public boolean isBlank(String str) {
        if (str == null) return true;
        if (str.isEmpty()) return true;
        if (str.isBlank()) return true;
        return false;
    }

    public String lTrim(String str) {
        return str.replaceAll("\\s+$", "");
    }

    // 比較原則空白<字母<數字
    // 0=相等,  >0=大於, <0=小於
    public int strCompare(String str1, String str2) {
        if (parse.isNumeric(str1) && parse.isNumeric(str2)) {
            return parse.string2Integer(str1) - parse.string2Integer(str2);
        }
        int len1 = str1.length();
        int len2 = str2.length();
        int len = len1;
        int end = 0;
        int rtn = 0;
        String tmp1 = str1;
        String tmp2 = str2;
        // 長度短的要補空白
        if (len1 > len2) {
            tmp2 = String.format("%-" + len + "s", tmp2);
        } else if (len1 < len2) {
            len = len2;
            tmp1 = String.format("%-" + len + "s", tmp1);
        }
        for (int i = 0; i < len; i++) {
            end = i + 1;
            rtn = strCompareto(tmp1.substring(i, end), tmp2.substring(i, end));
            if (rtn != 0) return rtn;
        }
        return 0;
    }

    private int strCompareto(String dv1, String dv2) {
        // 依EBCDIC碼比大字,空白<字母<數字
        // 0.空白,1.字母, 10-19為數字
        // 預設為空白
        int dn1 = 0;
        int dn2 = 0;
        if (!dv1.isBlank()) {
            if (parse.isNumeric(dv1)) dn1 = parse.string2Integer(dv1) + 10;
            else dn1 = 1;
        }
        if (!dv2.isBlank()) {
            if (parse.isNumeric(dv2)) dn2 = parse.string2Integer(dv2) + 10;
            else dn2 = 1;
        }
        // 2個都是字母用compareTo比大小
        if (dn1 == 1 && dn2 == 1) {
            return dv1.compareTo(dv2);
        } else {
            return dn1 - dn2;
        }
    }

    public boolean equalOr(String str, String fstr) {
        String tstr[] = fstr.split(",");
        for (int i = 0; i < tstr.length; i++) {
            if (str.equals(tstr[i])) return true;
        }
        return false;
    }
}
