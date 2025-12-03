package com.bot.reader;

import com.bot.domain.FieldDef;
import com.bot.domain.RowData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileParser {
    private final List<FieldDef> defs;

    public FileParser(List<FieldDef> defs) {
        this.defs = defs;
    }

    public RowData parseLine(String line) {
        Map<String, String> fieldMap = new HashMap<>();
        StringBuilder fullBuilder = new StringBuilder();
        StringBuilder keyGroup = new StringBuilder();
        line = line.replace("*"," ").replace("9"," ");
        //初始位置
        int sPos = 0;

        for (FieldDef def : defs) {


            String remaining = line.substring(sPos);

            int safeCut = getSafeSubstringLength(remaining, def.getLength());

            String value = remaining.substring(0, safeCut);

            fieldMap.put(def.getName(), value);

            // key hash
            if (def.isKey()) {
                keyGroup.append(def.getName()).append("=").append(value.trim()).append(",");
            }

            // full hash
            fullBuilder.append(value);

            sPos += safeCut;

//            LogProcess.info(log,"value = {} , def = {}",value,def);

        }
        String kg = keyGroup.substring(0, keyGroup.length() - 1);
        return new RowData(
                line,
                kg,
                hash(kg),
                hash(fullBuilder.toString()),
                fieldMap
        );
    }

    private String hash(String s) {
        // Apache commons
        return DigestUtils.md5Hex(s);
    }

    /**
     * 根據指定的 byte 長度與編碼，回傳可以安全 substring 的字元位置。
     *
     * @param str      要處理的字串
     * @param maxBytes 最多 byte 數
     * @return 回傳正確的位置截斷位置可用於 substring(0, result) 的字元位置
     */
    public int getSafeSubstringLength(String str, int maxBytes) {
        Charset charset = Charset.forName("Big5");

        int currentBytes = 0;

        for (int i = 0; i < str.length(); i++) {
            String ch = str.substring(i, i + 1);
            int byteLen = ch.getBytes(charset).length;

            // 若超過定義好的長度，回傳安全的 index
            if (currentBytes + byteLen > maxBytes) {
                return i;
            }
            currentBytes += byteLen;
        }
        // 全部都沒超過就整段可以用
        return str.length();
    }
}
