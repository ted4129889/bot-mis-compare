/* (C) 2024 */
package com.bot.mis.jpa.anq.convertmap;

import com.bot.mis.jpa.anq.config.AliasToEntityLinkHashMapResultTransformer;
import jakarta.persistence.Query;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Scope("prototype")
@Transactional
@EnableTransactionManagement
public class ConvertMap {

    public List<Map<String, String>> convert(Query query) {

        query =
                query.unwrap(NativeQueryImpl.class)
                        .setResultTransformer(AliasToEntityLinkHashMapResultTransformer.INSTANCE);

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (Object obj : query.getResultList()) {
            Map<String, Object> row = (LinkedHashMap<String, Object>) obj;
            Map<String, String> m = new LinkedHashMap<String, String>();
            Set<String> set = row.keySet();
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = "";

                if (row.get(key) != null && row.get(key) instanceof Clob) {
                    Clob c = (Clob) row.get(key);
                    try {
                        value = c.getSubString(1, (int) c.length());
                    } catch (SQLException e) {
                        m.put(key, "");
                    }
                } else if (row.get(key) != null) value = row.get(key).toString();

                m.put(key, value);
            }
            result.add(m);
        }
        return result;
    }
}
