package com.github.mrgatto.listener;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        
        ObjectNode on = objectMapper.createObjectNode();
        try {

            actionJSON = objectMapper.readValue(msg, JsonNode.class);

            System.out.println(actionJSON);

            if(StringUtils.equals(actionJSON.get("action").asText(), "create_btc_address")){

                MyPojoData pojo = new MyPojoData();
                
            	AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            	
            	GetCallerIdentityResult getCallerIdentityResult= sts.getCallerIdentity(new GetCallerIdentityRequest());
            	
            	System.out.println("CallerIdentityResult " + getCallerIdentityResult);
            	
        		AssumeRoleResult assumeRoleResult = sts.assumeRole(new AssumeRoleRequest()
        				.withRoleArn("arn:aws:iam::925352035051:role/Admin")
        				.withExternalId("IsengardExternalId7W8xp2lkm7sr")
        				.withDurationSeconds(3600)
        				.withRoleSessionName("role-session"));

        		Credentials stsCredentials = assumeRoleResult.getCredentials();
        		
        		System.out.println("AccessKeyId " + stsCredentials.getAccessKeyId());
        		System.out.println("SecretAccessKey " + stsCredentials.getSecretAccessKey());
        		System.out.println("SessionToken " + stsCredentials.getSessionToken());
                
                on.put("action", actionJSON.get("action").asText());
                on.put("email", actionJSON.get("email").asText());
                
                on.put("AK", stsCredentials.getAccessKeyId());
                on.put("SK", stsCredentials.getSecretAccessKey());
                on.put("ST", stsCredentials.getSessionToken());
                
                pojo.setValue(on.asText());

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
