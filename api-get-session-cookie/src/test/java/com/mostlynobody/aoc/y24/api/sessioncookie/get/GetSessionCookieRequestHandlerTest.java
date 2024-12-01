package com.mostlynobody.aoc.y24.api.sessioncookie.get;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookieJson;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SerdeImport(SessionCookieJson.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetSessionCookieRequestHandlerTest {

    public static final String ENTRY_ID = "adventofcode.com";
    private static final String TABLE_NAME = "AdventOfCodeCookie";

    private LocalStackContainer localstack;
    private DynamoDbClient dynamoDbClient;
    private GetSessionCookieRequestHandler handler;
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
        handler = new GetSessionCookieRequestHandler();
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
                .keySchema(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH) // Partition key
                        .build()).attributeDefinitions(AttributeDefinition.builder().attributeName("id")
                        .attributeType(ScalarAttributeType.S).build())
                .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L)
                        .build()).build();

        dynamoDbClient.createTable(request);
        dynamoDbClient.waiter().waitUntilTableExists(builder -> builder.tableName(TABLE_NAME));
    }

    private void insertSessionCookie(String value) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(GetSessionCookieRequestHandlerTest.ENTRY_ID).build());
        item.put("value", AttributeValue.builder().s(value).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);
    }

    private void deleteSessionCookie() {
        Map<String, AttributeValue> item = Map.of("id", AttributeValue.builder()
                .s(GetSessionCookieRequestHandlerTest.ENTRY_ID)
                .build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder().tableName(TABLE_NAME).key(item).build();
        dynamoDbClient.deleteItem(deleteItemRequest);
    }


    @Test
    void testExecute_SuccessfulRetrieval() throws IOException {
        String value = "some-session-value";
        insertSessionCookie(value);

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        SessionCookieJson sessionCookieJson = new SessionCookieJson(ENTRY_ID, value);
        String expectedJson = jsonMapper.writeValueAsString(sessionCookieJson);

        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void testExecute_ItemNotFound() {
        deleteSessionCookie();

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        assertEquals("{\"error\": \"SessionCookie not found for the provided id.\"}", response.getBody());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void testExecute_GeneralException() throws IOException {
        String value = "some-session-value";
        insertSessionCookie(value);

        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        JsonMapper faultyJsonMapper = Mockito.mock(JsonMapper.class);
        try {
            Mockito.when(faultyJsonMapper.writeValueAsString(Mockito.any(SessionCookieJson.class)))
                    .thenThrow(new RuntimeException("Serialization error"));
        } catch (IOException e) {
            fail("Mock setup failed");
        }

        APIGatewayProxyResponseEvent response;
        try (GetSessionCookieRequestHandler faultyHandler = new GetSessionCookieRequestHandler()) {
            faultyHandler.objectMapper = faultyJsonMapper;
            faultyHandler.dynamoDbClient = dynamoDbClient;

            response = faultyHandler.execute(requestEvent);
        }

        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\": \"Internal Server Error.\"}", response.getBody());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }
}
