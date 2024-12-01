package com.mostlynobody.aoc.y24.api.sessioncookie.update;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.SessionCookieJson;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SerdeImport(SessionCookieJson.class)
public class FunctionRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    JsonMapper objectMapper;
    DynamoDbClient dynamoDbClient;

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
            PutItemRequest request = PutItemRequest.builder().tableName("AdventOfCodeCookie").item(item).build();
            dynamoDbClient.putItem(request);

            response.setStatusCode(200);
            return response;
        } catch (AwsServiceException | SdkClientException e) {
            response.setStatusCode(500);
            response.setBody(e.getMessage());
            return response;
        }
    }
}
