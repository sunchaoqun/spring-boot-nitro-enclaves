package com.github.mrgatto.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.github.mrgatto.autoconfigure.EnableNitroEnclavesHostSide;
import com.github.mrgatto.host.NitroEnclaveClient;
import com.github.mrgatto.model.EnclaveRequest;
import com.github.mrgatto.model.EnclaveResponse;
import com.github.mrgatto.simlpeecho.Actions;
import com.github.mrgatto.simlpeecho.model.MyPojoData;
import com.github.mrgatto.simlpeecho.model.MyPojoDataResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class EchoController {

    @Autowired
    private NitroEnclaveClient client;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/echo")
    public String echoGet(@RequestParam(defaultValue = "Hello, World!") String message) {


		long time = System.currentTimeMillis();

        JsonNode actionJSON = null;
        
        ObjectNode on = objectMapper.createObjectNode();
        try {

            actionJSON = objectMapper.readValue("{\"email\":\"sunchaoqun@126.com\",\"action\":\"create_btc_address\"}", JsonNode.class);

            System.out.println(actionJSON);

            if(StringUtils.equals(actionJSON.get("action").asText(), "create_btc_address")){

                MyPojoData pojo = new MyPojoData();
                
            	AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.defaultClient();
            	
            	GetCallerIdentityResult getCallerIdentityResult= sts.getCallerIdentity(new GetCallerIdentityRequest());
            	
            	System.out.println("CallerIdentityResult " + getCallerIdentityResult);
            	
        		AssumeRoleResult assumeRoleResult = sts.assumeRole(new AssumeRoleRequest()
        				.withRoleArn("arn:aws:iam::932423224465:role/PVRE-SSMOnboardingRole-N3BHvdwo3rWc")
        				.withExternalId("EnclaveExternalId")
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
                
                System.out.print("Data passing to Enclave " + on.toString());
                
                pojo.setValue(on.toString());

                ExecutorService executorService = Executors.newFixedThreadPool(50);

                for (int i = 0; i < 100000; i++) {
                    executorService.submit(() -> {
                        EnclaveRequest<MyPojoData> request = new EnclaveRequest<>();
                        request.setAction(Actions.ECHO.name());
                        request.setData(pojo);

                        try {
                            EnclaveResponse<MyPojoDataResult> response = client.send(request);
                            if (response.getIsError()) {
                                System.out.println(String.format("Something went wrong: %s", response.getError()));
                                System.out.println(response.getErrorStacktrace());
                            } else {
                                System.out.println(response.getData().getValue());
                            }
                            System.out.println(String.format("Enclave execution time %sms", response.getDuration()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                executorService.shutdown();
                try {
                    executorService.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(System.currentTimeMillis()-time);

        return "Echo (GET): " + message;
    }

    @PostMapping("/echo")
    public String echoPost(@RequestBody String message) {
        return "Echo (POST): " + message;
    }

    @GetMapping("/info")
    public String getInfo() {
        return "Running on port: " + System.getProperty("server.port");
    }
}