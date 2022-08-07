package com.github.mrgatto.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName="ConfigEnvironment")
public class ConfigEnvironment {
    private String key;
    private String value;

    @Override
    public String toString() {
        return "ConfigEnvironment{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    @DynamoDBHashKey(attributeName="key")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @DynamoDBAttribute(attributeName="value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
