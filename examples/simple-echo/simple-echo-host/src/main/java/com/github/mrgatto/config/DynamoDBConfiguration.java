package com.github.mrgatto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

@Configuration
public class DynamoDBConfiguration {

    public AmazonDynamoDB buildDynamoDBClient() {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new InstanceProfileCredentialsProvider(true))
                .build();
            return client;
    }

    @Bean
    public DynamoDBMapper getDynamoDBMapper(){
        return new DynamoDBMapper(buildDynamoDBClient());
    }
}
