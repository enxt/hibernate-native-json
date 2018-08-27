package org.hibernate.json.dialect;

import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StringType;

import java.sql.Types;

public class MariaDBJsonDialect extends MariaDBDialect {
    public MariaDBJsonDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "json");
        this.registerFunction("json_text", new SQLFunctionTemplate(StringType.INSTANCE, "JSON_UNQUOTE(JSON_EXTRACT(?1, CONCAT('$.',?2)))"));
    }
}
