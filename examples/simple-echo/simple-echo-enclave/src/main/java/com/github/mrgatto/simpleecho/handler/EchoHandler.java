package com.github.mrgatto.simpleecho.handler;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.util.Base64;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrgatto.enclave.handler.AbstractActionHandler;
import com.github.mrgatto.enclave.nsm.NsmClient;
import com.github.mrgatto.models.UserAccount;
import com.github.mrgatto.simlpeecho.Actions;
import com.github.mrgatto.simlpeecho.model.MyPojoData;
import com.github.mrgatto.simlpeecho.model.MyPojoDataResult;

@Component
public class EchoHandler extends AbstractActionHandler<MyPojoData, MyPojoDataResult> {

	@Autowired
	private NsmClient nsmClient;

	//	AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
	//		"",
	//		""
	//	));

	// AWSCredentialsProvider awsCredentialsProvider = new InstanceProfileCredentialsProvider(true);
	
	
	AWSCredentialsProvider awsCredentialsProvider;

	private String keyId = "3721a9ea-533e-448a-a61c-3523cb6d1caf";

	private String region = "eu-west-1";

	@Override
	public boolean canHandle(String action) {
		return Actions.ECHO.name().equalsIgnoreCase(action);
	}

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public MyPojoDataResult handle(MyPojoData data) {
	
		JsonNode jsonNode = null;
		
		try {
			jsonNode = objectMapper.readValue(data.getValue(), JsonNode.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 

		awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicSessionCredentials(
			jsonNode.get("AK").asText(),
			jsonNode.get("SK").asText(),
			jsonNode.get("ST").asText()
		));
		
		saveToDynamoDB(jsonNode.get("email").asText());

		String nsmModuleId = this.nsmClient.describeNsm().getModuleId();

		MyPojoDataResult result = new MyPojoDataResult();

		result.setValue("Echo from Enclave " + nsmModuleId + ": " + data.getValue());
		//result.setValue("Echo from Enclave  : " + data.getValue());

		return result;
	}

	private String encrypt(String pureString){

		Process process = null;

		// try {
		// 	process = Runtime.getRuntime().exec("python3 /app/traffic-forwarder.py 127.0.0.1 443 -1 8000");
		// } catch (Exception e) {
		// 	System.out.println(e);
		// }

		AWSKMS AWSKMS_CLIENT = AWSKMSClientBuilder
			.standard()
			.withCredentials(awsCredentialsProvider)
			.withRegion(region)
			.build();

		ByteBuffer plaintext = ByteBuffer.wrap(pureString.getBytes());
		EncryptRequest req = new EncryptRequest().withKeyId(keyId).withPlaintext(plaintext);
		ByteBuffer ciphertext = AWSKMS_CLIENT.encrypt(req).getCiphertextBlob();

		byte[] base64EncodedValue = Base64.encode(ciphertext.array());
		String value = new String(base64EncodedValue, Charset.forName("UTF-8"));
		System.out.println("encrypted value: " + value);

		if(process !=null){
			process.destroy();
		}
		return value;
	}

	private void saveToDynamoDB(String email){

		Process process = null;

		// try {
		// 	process = Runtime.getRuntime().exec("python3 /app/traffic-forwarder.py 127.0.0.1 443 -1 8001");
		// } catch (Exception e) {
		// 	System.out.println(e);
		// }
		
		System.out.println("-------------------------");

		AmazonDynamoDB client = AmazonDynamoDBClientBuilder
			.standard()
			.withCredentials(awsCredentialsProvider)
			.withRegion(region)
			.build();

		DynamoDBMapper mapper = new DynamoDBMapper(client);

		Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();

        eav.put(":val1", new AttributeValue().withS(email));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("email = :val1").withExpressionAttributeValues(eav);

        List<UserAccount> scanResult = mapper.scan(UserAccount.class, scanExpression);

		UserAccount ua = null;

        if(scanResult.size()>0){
            System.out.println("UserAccount UserId " + scanResult.get(0).getUserId());
            ua = scanResult.get(0);

			if(StringUtils.isBlank(ua.getBtcAddress()) || StringUtils.isBlank(ua.getBtcEncryptedPrivateAddressWIF())){
				
				ECKey ecKey = new ECKey();

				Address publicAddress = Address.fromKey(RegTestParams.get(), ecKey, Script.ScriptType.P2WPKH);
				Address privateAddress = Address.fromKey(RegTestParams.get(), ecKey, Script.ScriptType.P2PKH);
				System.out.println("Private key WIF: " + ecKey.getPrivateKeyAsWiF(RegTestParams.get()));
				System.out.println("Public Address: " + publicAddress);
				System.out.println("Private Address:  " + privateAddress);
		
				String value = encrypt(ecKey.getPrivateKeyAsWiF(RegTestParams.get()));

				ua.setBtcAddress(publicAddress.toString());
				ua.setBtcEncryptedPrivateAddressWIF(value);

				mapper.save(ua);
			}
        }
		
		if(process !=null){
			process.destroy();
		}

		System.out.println(ua);
	}

}
