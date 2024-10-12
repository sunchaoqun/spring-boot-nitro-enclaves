package com.github.mrgatto.simpleecho;

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
import org.springframework.beans.factory.annotation.Autowired;

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

@SpringBootApplication
@ComponentScan({ "com.github.mrgatto" })
@EnableNitroEnclavesHostSide

public class NitroEnclaveHostApplication {
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(NitroEnclaveHostApplication.class, args);
    }
}
