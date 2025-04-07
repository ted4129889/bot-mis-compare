/* (C) 2024 */
package com.bot.mis.util.sort.vo;

import com.bot.mis.util.sort.eum.SortBy;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SubstringComparator implements Comparator<String> {
    private List<KeyRange> keyRanges;
    private Charset charset;

    public SubstringComparator(List<KeyRange> keyRanges, Charset charset) {
        this.keyRanges = keyRanges;
        this.charset = charset;
    }

    @Override
    public int compare(String s1, String s2) {
        for (KeyRange keyRange : keyRanges) {
            int startIndex = keyRange.startIndex() - 1;
            int endIndex = startIndex + keyRange.len();
            if (s1.length() < endIndex) {
                s1 = padSpace(s1, endIndex);
            }
            if (s2.length() < endIndex) {
                s2 = padSpace(s2, endIndex);
            }
            String substring1 = s1.substring(startIndex, endIndex);
            String substring2 = s2.substring(startIndex, endIndex);

            if (this.charset.equals(Charset.forName("BIG5"))) {
                substring1 = cutStringByByteLength(s1, startIndex, endIndex);
                substring2 = cutStringByByteLength(s2, startIndex, endIndex);
            }
            int comparison = substring1.compareTo(substring2);
            if (comparison != 0) {
                if (keyRange.sortBy() == SortBy.ASC) {
                    return comparison;
                } else if (keyRange.sortBy() == SortBy.DESC) {
                    return 0 - comparison;
                }
            }
        }
        return 0;
    }

    private String padSpace(String s, int len) {
        int stringLen = s.length();
        if (this.charset.equals(Charset.forName("BIG5"))) {
            stringLen = computeStringByteLength(s);
        }
        for (int i = stringLen; i < len; i++) {
            s += " ";
        }
        return s;
    }

    private int computeStringByteLength(String s) {
        if (s != null) {
            byte[] b = s.getBytes(Charset.forName("BIG5"));
            return b.length;
        }
        return 0;
    }

    private String cutStringByByteLength(String s, int startIndex, int endIndex) {
        if (s != null && startIndex >= 0 && endIndex > startIndex) {
            byte[] b = s.getBytes(Charset.forName("BIG5"));
            if (startIndex >= b.length) {
                return "";
            }
            endIndex = Math.min(endIndex, b.length);
            byte[] newBytes = Arrays.copyOfRange(b, startIndex, endIndex);
            return new String(newBytes, Charset.forName("BIG5"));
        }
        return s;
    }
}
