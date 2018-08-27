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
package org.hibernate.json.dialect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.postgresql.util.PGobject;

/**
 * Define a Jackson Serializer/Deserializer use to persist
 *
 * The implementation is a hibernate custom type
 *
 * @author Marvin H Froeder
 * @author Regis Leray
 */
public class JsonUserType implements UserType, DynamicParameterizedType {

    private static final int[] SQL_TYPES = { Types.JAVA_OBJECT };
    private Class<?> returnedClass;

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        } else if (x == null || y == null) {
            return false;
        } else {
            return x.equals(y);
        }
    }

    public int hashCode(Object x) throws HibernateException {
        return null == x ? 0 : x.hashCode();
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
        Object result = resultSet.getObject(strings[0]);
        if (result instanceof PGobject) {
            return convertJsonToObject(((PGobject) result).getValue());
        } else if(result != null) {
            return convertJsonToObject((String) result);
        }

        return null;
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        Dialect dialect = Dialect.getDialect();
        if(dialect instanceof PostgisJsonDialect) {
            PGobject dataObject = new PGobject();
            dataObject.setType("json");
            if (o != null) {
                dataObject.setValue(convertObjectToJson(o));
            }
            preparedStatement.setObject(i, dataObject);

        } else if(o != null) {
            preparedStatement.setObject(i, convertObjectToJson(o));
        } else {
            preparedStatement.setObject(i, null);
        }
    }

    public Object convertJsonToObject(String content) {
        if ((content == null) || (content.isEmpty())) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType type = createJavaType(mapper);
            if (type == null)
                return mapper.readValue(content, returnedClass);

            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String convertObjectToJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object deepCopy(Object value) throws HibernateException {
        String json = convertObjectToJson(value);
        return convertJsonToObject(json);
    }

    /**
     * Optionnal
     */
    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return deepCopy(original);
    }

    /**
     * (optional operation)
     *
     * @param value
     *
     * @throws HibernateException
     */
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    /**
     * (optional operation)
     *
     * @param cached
     * @param owner
     *
     * @return the instance cached
     *
     * @throws HibernateException
     */
    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return deepCopy(cached);
    }

    /**
     * By default we are expecting to use a simple object / not a collection (Set, List)
     *
     * @param mapper : instance jackson object mapper
     *
     * @return A jackson JavaType to specify wich object represent the json string representation
     *
     */
    public JavaType createJavaType(ObjectMapper mapper) {
        try {
            return SimpleType.construct(returnedClass());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public void setParameterValues(Properties parameters) {
        final ParameterType reader = (ParameterType) parameters.get(PARAMETER_TYPE);

        if (reader != null)
            this.returnedClass = reader.getReturnedClass();

    }

    public Class<?> returnedClass() {
        return this.returnedClass;
    }
}
