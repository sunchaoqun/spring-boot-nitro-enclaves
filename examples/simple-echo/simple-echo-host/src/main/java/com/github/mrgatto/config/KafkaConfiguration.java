package com.github.mrgatto.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrgatto.model.ConfigEnvironment;
import com.github.mrgatto.repository.ConfigEnvironmentRepository;

@EnableKafka
@Configuration
public class KafkaConfiguration {

    // @Value("${bootstrap.servers}")
    private String BootstrapServers;

    @Value("${dynamodb.init.environment.enabled}")
    private boolean dynamodbInitEnvironmentEnabled;

    @Autowired
    private ConfigEnvironmentRepository configEnvironmentRepository;

    @Bean
    public ConsumerFactory<String,String> consumerFactory(){

        if(dynamodbInitEnvironmentEnabled){
            ConfigEnvironment configEnvironment = configEnvironmentRepository.getConfigEnvironment("MSK_BOOTSTRAP_SERVERS");
            BootstrapServers = configEnvironment.getValue();
        }

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,BootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG,"group_id");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ProducerFactory<String,byte[]> producerFactory(){

        if(dynamodbInitEnvironmentEnabled){
            ConfigEnvironment configEnvironment = configEnvironmentRepository.getConfigEnvironment("MSK_BOOTSTRAP_SERVERS");
            BootstrapServers = configEnvironment.getValue();
        }

        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> concurrentKafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

        return concurrentKafkaListenerContainerFactory;
    }

    ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public KafkaTemplate<String,byte[]> kafkaTemplate(){

        KafkaTemplate kafkaTemplate = new KafkaTemplate<>(producerFactory());
        return kafkaTemplate;
    }
}

