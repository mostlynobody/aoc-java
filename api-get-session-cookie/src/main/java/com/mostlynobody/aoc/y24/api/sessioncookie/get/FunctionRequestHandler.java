package com.mostlynobody.aoc.y24.api.sessioncookie.get;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookie;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;

@SerdeImport(SessionCookie.class)
public class FunctionRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    JsonMapper objectMapper;
    DynamoDbClient dynamoDbClient;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", AttributeValue.builder().s("adventofcode.com").build());
        GetItemRequest request = GetItemRequest.builder().tableName("AdventOfCodeCookie").key(keyToGet).build();
        try {
            Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

            if (returnedItem != null && !returnedItem.isEmpty()) {

                String id = returnedItem.get("id").s();
                String value = returnedItem.get("value").s();

                SessionCookie sessionCookie = new SessionCookie(id, value);

                String responseBody = objectMapper.writeValueAsString(sessionCookie);

                response.setStatusCode(200);
                response.setBody(responseBody);
                response.setHeaders(Map.of("Content-Type", "application/json"));
            } else {
                response.setStatusCode(404);
                response.setBody("{\"error\": \"SessionCookie not found for the provided id.\"}");
                response.setHeaders(Map.of("Content-Type", "application/json"));
            }
        } catch (DynamoDbException e) {
            response.setStatusCode(500);
            response.setBody("{\"error\": \"" + e.getMessage() + "\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\": \"Internal Server Error.\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        }

        return response;
    }
}
