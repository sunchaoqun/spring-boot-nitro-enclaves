package com.github.mrgatto.listener;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrgatto.host.NitroEnclaveClient;
import com.github.mrgatto.model.EnclaveRequest;
import com.github.mrgatto.model.EnclaveResponse;
import com.github.mrgatto.simlpeecho.Actions;
import com.github.mrgatto.simlpeecho.model.MyPojoData;
import com.github.mrgatto.simlpeecho.model.MyPojoDataResult;

@Service
public class KafkaConsumer {

    ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init(){

    }

    @Autowired
    NitroEnclaveClient client;

    @KafkaListener(topics = "btc",groupId = "group_id")
    public void consumeJSON(String msg) throws Exception {
        long time = System.currentTimeMillis();

        JsonNode actionJSON = null;
        try {

            actionJSON = objectMapper.readValue(msg, JsonNode.class);

            System.out.println(actionJSON);

            if(StringUtils.equals(actionJSON.get("action").asText(), "create_btc_address")){

                MyPojoData pojo = new MyPojoData();

                pojo.setValue(actionJSON.get("email").asText());

                EnclaveRequest<MyPojoData> request = new EnclaveRequest<>();
                request.setAction(Actions.ECHO.name());
                request.setData(pojo);

                EnclaveResponse<MyPojoDataResult> response = client.send(request);

                if (response.getIsError()) {
                    System.out.println(String.format("Something went wrong: %s", response.getError()));
                    System.out.println(response.getErrorStacktrace());
                } else {
                    System.out.println(response.getData().getValue());
                }

                System.out.println(String.format("Enclave execution time %sms", response.getDuration()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis()-time);
    }

}
