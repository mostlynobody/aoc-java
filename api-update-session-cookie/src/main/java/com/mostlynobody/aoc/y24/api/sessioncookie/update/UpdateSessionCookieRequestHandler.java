package com.mostlynobody.aoc.y24.api.sessioncookie.update;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookieJson;
import io.micronaut.context.annotation.Value;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SerdeImport(SessionCookieJson.class)
public class UpdateSessionCookieRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateSessionCookieRequestHandler.class);

    @Value("${dynamodb.table.name:AdventOfCodeCookie}")
    String tableName;

    @Value("${dynamodb.region:eu-north-1}")
    String region;

    @Inject
    JsonMapper objectMapper;

    DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(region)).build();


    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        SessionCookieJson sessionCookie;
        String requestBody = input.getBody();

        try {
            sessionCookie = objectMapper.readValue(requestBody, SessionCookieJson.class);
        } catch (IOException e) {
            response.setStatusCode(400);
            response.setBody(e.getMessage());
            return response;
        }

        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s("adventofcode.com").build());
            item.put("value", AttributeValue.builder().s(sessionCookie.value()).build());
            PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(item).build();
            dynamoDbClient.putItem(request);

            response.setStatusCode(200);
            return response;
        } catch (DynamoDbException e) {
            logger.error("DynamoDB error: ", e);
            response.setStatusCode(500);
            response.setBody("{\"error\": \"Internal Server Error.\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            response.setStatusCode(500);
            response.setBody("{\"error\": \"Internal Server Error.\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        }
    }
}
