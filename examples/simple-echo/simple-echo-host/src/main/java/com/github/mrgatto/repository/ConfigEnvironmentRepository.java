package com.github.mrgatto.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.github.mrgatto.model.ConfigEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ConfigEnvironmentRepository {

    @Autowired
    private  DynamoDBMapper dynamoDBMapper;

    public ConfigEnvironment getConfigEnvironment(String key){

        ConfigEnvironment configEnvironment = dynamoDBMapper.load(ConfigEnvironment.class,key);

        return configEnvironment;
    }
}
