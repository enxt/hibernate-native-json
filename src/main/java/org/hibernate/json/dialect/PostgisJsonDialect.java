package org.hibernate.json.dialect;

import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;
import org.hibernate.type.StringType;

import java.sql.Types;

public class PostgisJsonDialect extends PostgisDialect {
    public PostgisJsonDialect() {
        this.registerColumnType(Types.JAVA_OBJECT, "json");
        this.registerFunction("json_text", new SQLFunctionTemplate(StringType.INSTANCE, "?1 ->> \"$.?2\""));
    }
}
