hibernate-native-json
=================

[![Release](https://jitpack.io/v/enxt/hibernate-native-json.svg)](https://jitpack.io/#enxt/hibernate-native-json)
[![Issues](https://img.shields.io/github/issues/enxt/hibernate-native-json.svg)](https://github.com/enxt/hibernate-native-json/issues) 
[![Forks](https://img.shields.io/github/forks/enxt/hibernate-native-json.svg)](https://github.com/enxt/hibernate-native-json/network) 
[![Stars](https://img.shields.io/github/stars/enxt/hibernate-native-json.svg)](https://github.com/enxt/hibernate-native-json/stargazers)


Read/Write an object to JSON / JSON to object into a database table field (declared as a string column).
This also allow to query json.

Currently supported databases:
- postgis
- mariadb

This project provided a hibernate UserType and a dialect with json support.

The UserType uses jackson object mappper to do a fast serialize/deserialize of a json string representation.  More information  [how to implements a user type](http://blog.xebia.com/2009/11/09/understanding-and-writing-hibernate-user-types/)

Check the src/test folder to see a full example.

### Example

You can serialize either a class or a Map<String, Object> (in any cases a more dynamic field is necessary).

```
@TypeDef(name = "json", typeClass = org.hibernate.type.json.JsonStringType.class)
@Entity
public class MyClass {
    @Type(type = "json")
    private Map<String, Object> extra;
}
```

Now you can persist your object as a json using your hibernate session / jpa repository.

### Querying

In order to use the new json_text function you need register this in you hibernate with the propertie (only abailable from 5.3 version of hibernate):

```
hibernate.metadata_builder_contributor=org.hibernate.type.json.MariaDBSqlJsonFunctionsBuilder
```
or
```
hibernate.metadata_builder_contributor=org.hibernate.type.json.PostgreSqlJsonFunctionsBuilder
```

ensure that it's registered

`json_text`: is equivalent to postgres `->>` get JSON object field as text
http://www.postgresql.org/docs/9.5/static/functions-json.html

`json_text`: is equivalent to mariadb `JSON_UNQUOTE(JSON_EXTRACT())` get JSON object field as text
https://mariadb.com/kb/en/library/json-functions/

This allow a HQL query like this:
```
    select
        json_text(i.label, 'value')
    from
        Item i
    where
        json_text(i.label, 'lang') = :lang
```

Witch will produce the following SQL in PostgreSQL:
```
    select
        item0_.label ->> 'value' as col_0_0_ 
    from
        items item0_ 
    where
        item0_.label ->> 'lang'=?
```

and will product the following SQL in MariaDB:
```
    select
        JSON_UNQUOTE(JSON_EXTRACT(item0_.label, '$.value')) as col_0_0_ 
    from
        items item0_ 
    where
        JSON_UNQUOTE(JSON_EXTRACT(item0_.label, '$.lang')) = ?
```



