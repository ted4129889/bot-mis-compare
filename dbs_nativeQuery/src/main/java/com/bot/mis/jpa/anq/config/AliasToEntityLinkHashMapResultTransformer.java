/* (C) 2024 */
package com.bot.mis.jpa.anq.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.transform.ResultTransformer;

public class AliasToEntityLinkHashMapResultTransformer implements ResultTransformer {

    public static final AliasToEntityLinkHashMapResultTransformer INSTANCE =
            new AliasToEntityLinkHashMapResultTransformer();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {

        Map result = new LinkedHashMap(tuple.length);
        for (int i = 0; i < tuple.length; ++i) {
            String alias = aliases[i];
            if (alias != null) {
                result.put(alias, tuple[i]);
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List transformList(List list) {
        return list;
    }
}
