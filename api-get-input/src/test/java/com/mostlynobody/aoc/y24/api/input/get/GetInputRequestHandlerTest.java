package com.mostlynobody.aoc.y24.api.input.get;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookieJson;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SerdeImport(SessionCookieJson.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetInputRequestHandlerTest {

    private static final String TABLE_NAME = "AdventOfCodeInput";

    private LocalStackContainer localstack;
    private DynamoDbClient dynamoDbClient;
    private GetInputRequestHandler handler;
    private JsonMapper jsonMapper;

    @BeforeAll
    void setUp() {
        localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.1.0")).withServices(LocalStackContainer.Service.DYNAMODB);
        localstack.start();

        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())))
                .region(Region.of(localstack.getRegion())).build();
        createTable();

        jsonMapper = JsonMapper.createDefault();
        handler = new GetInputRequestHandler();
        handler.objectMapper = jsonMapper;
        handler.dynamoDbClient = dynamoDbClient;
        handler.tableName = TABLE_NAME;
    }

    @AfterAll
    void tearDown() {
        if (dynamoDbClient != null) {
            dynamoDbClient.close();
        }
        if (localstack != null) {
            localstack.stop();
        }
    }

    private void createTable() {
        CreateTableRequest request = CreateTableRequest.builder().tableName(TABLE_NAME)
                .keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder().attributeName("id")
                        .attributeType(ScalarAttributeType.S).build())
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L)
                        .build())
                .build();

        dynamoDbClient.createTable(request);
        dynamoDbClient.waiter().waitUntilTableExists(builder -> builder.tableName(TABLE_NAME));
    }

    @Test
    void testGetExistingItem() {
        String testId = "testId";
        String testValue = "testValue";
        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", AttributeValue.builder().s(testId).build());
        itemValues.put("value", AttributeValue.builder().s(testValue).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(itemValues)
                .build();
        dynamoDbClient.putItem(putItemRequest);

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", testId);
        requestEvent.setPathParameters(pathParameters);

        APIGatewayProxyResponseEvent responseEvent = handler.execute(requestEvent);

        assertEquals(200, responseEvent.getStatusCode());
        assertEquals("application/json", responseEvent.getHeaders().get("Content-Type"));
        String expectedBody = "{\"id\":\"" + testId + "\",\"value\":\"" + testValue + "\"}";
        assertEquals(expectedBody, responseEvent.getBody());
    }

    @Test
    void testGetNonExistingItem() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "nonExistingId");
        requestEvent.setPathParameters(pathParameters);

        APIGatewayProxyResponseEvent responseEvent = handler.execute(requestEvent);

        assertEquals(404, responseEvent.getStatusCode());
        assertEquals("application/json", responseEvent.getHeaders().get("Content-Type"));
        assertEquals("{\"error\": \"Input not found for the provided id.\"}", responseEvent.getBody());
    }

    @Test
    void testMissingIdParameter() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();

        APIGatewayProxyResponseEvent responseEvent = handler.execute(requestEvent);

        assertEquals(400, responseEvent.getStatusCode());
        assertEquals("Bad Request: parameter is missing.", responseEvent.getBody());
    }

    @Test
    void testDynamoDbException() {
        String originalTableName = handler.tableName;
        handler.tableName = "NonExistingTable";
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "anyId");
        requestEvent.setPathParameters(pathParameters);

        APIGatewayProxyResponseEvent responseEvent = handler.execute(requestEvent);

        assertEquals(500, responseEvent.getStatusCode());
        assertEquals("application/json", responseEvent.getHeaders().get("Content-Type"));
        assertEquals("{\"error\": \"Internal Server Error.\"}", responseEvent.getBody());

        handler.tableName = originalTableName;
    }
}
