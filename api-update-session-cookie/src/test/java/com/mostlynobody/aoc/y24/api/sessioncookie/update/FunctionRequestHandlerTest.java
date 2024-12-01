package com.mostlynobody.aoc.y24.api.sessioncookie.update;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookie;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SerdeImport(SessionCookie.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FunctionRequestHandlerTest {

    private static final String TABLE_NAME = "AdventOfCodeCookie";
    private LocalStackContainer localstack;
    private DynamoDbClient dynamoDbClient;
    private FunctionRequestHandler handler;
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
        handler = new FunctionRequestHandler();
        handler.objectMapper = jsonMapper;
        handler.dynamoDbClient = dynamoDbClient;
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
                .keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH) // Partition key
                        .build()).attributeDefinitions(AttributeDefinition.builder().attributeName("id")
                        .attributeType(ScalarAttributeType.S).build())
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L)
                        .build()).build();

        dynamoDbClient.createTable(request);
        dynamoDbClient.waiter().waitUntilTableExists(builder -> builder.tableName(TABLE_NAME));
    }

    @Test
    void testExecute_Success() throws Exception {
        SessionCookie sessionCookie = new SessionCookie("adventofcode.com", "my-cookie");

        String requestBody = jsonMapper.writeValueAsString(sessionCookie);
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent().withBody(requestBody);


        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);
        assertEquals(200, response.getStatusCode());

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s("adventofcode.com").build());
        GetItemRequest getItemRequest = GetItemRequest.builder().tableName(TABLE_NAME).key(key).build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(getItemRequest).item();
        assertNotNull(item);
        assertEquals("adventofcode.com", item.get("id").s());
        assertEquals("my-cookie", item.get("value").s());
    }

    @Test
    void testExecute_InvalidJson() {
        String invalidJson = "{ invalid json }";
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent().withBody(invalidJson);

        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);

        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Unexpected character"));
    }

    @Test
    void testExecute_DynamoDBError() throws IOException {
        DynamoDbClient mockDynamoDbClient = mock(DynamoDbClient.class);
        Mockito.doThrow(SdkClientException.builder().message("DynamoDB error")
                .build()).when(mockDynamoDbClient).putItem(Mockito.any(PutItemRequest.class));
        handler.dynamoDbClient = mockDynamoDbClient;

        SessionCookie sessionCookie = new SessionCookie(null, null);
        String requestBody = jsonMapper.writeValueAsString(sessionCookie);

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent().withBody(requestBody);
        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);

        assertEquals(500, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DynamoDB error", response.getBody());
    }
}
