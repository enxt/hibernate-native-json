/**
 * Copyright (C) 2016 Marvin Herman Froeder (marvin@marvinformatics.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.type.json;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.StringType;

public class MariaDBSqlJsonFunctionsBuilder implements MetadataBuilderContributor {
    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                "group_concat",
                new StandardSQLFunction(
                        "group_concat",
                        StandardBasicTypes.STRING
                )
        );
        metadataBuilder.applySqlFunction(
                "json_text",
                new SQLFunctionTemplate(StringType.INSTANCE, "JSON_UNQUOTE(JSON_EXTRACT(?1, CONCAT('$.',?2)))")
        );
    }
//
//    public static <E extends Comparable<E>> ComparableExpression<E> jsonText(Path<?> path, Path<E> property) {
//        return Expressions.comparableTemplate(property.getType(), "json_text({0}, {1})", path, property.getMetadata().getName());
//    }
//
//    public static <E extends Comparable<E>> ComparableExpression<E> jsonText(MapPath<?, E, ?> path, String property) {
//        return Expressions.comparableTemplate(path.getValueType(), "json_text({0}, {1})", path, property);
//    }

}
