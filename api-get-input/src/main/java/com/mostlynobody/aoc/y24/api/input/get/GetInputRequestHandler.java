package com.mostlynobody.aoc.y24.api.input.get;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.InputJson;
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
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.util.HashMap;
import java.util.Map;

@SerdeImport(InputJson.class)
public class GetInputRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GetInputRequestHandler.class);

    @Value("${dynamodb.table.name:AdventOfCodeInput}")
    String tableName;

    @Value("${dynamodb.region:eu-north-1}")
    String region;

    @Inject
    JsonMapper objectMapper;

    DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(region)).build();

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        Map<String, String> pathParameters = input.getPathParameters();
        if (pathParameters == null || !pathParameters.containsKey("id")) {
            response.setStatusCode(400);
            response.setBody("Bad Request: parameter is missing.");
            return response;
        }

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", AttributeValue.builder().s(pathParameters.get("id")).build());
        GetItemRequest request = GetItemRequest.builder().tableName(tableName).key(keyToGet).build();

        try {
            Map<String, AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

            if (returnedItem != null && !returnedItem.isEmpty()) {

                String id = returnedItem.get("id").s();
                String value = returnedItem.get("value").s();

                InputJson inputJson = new InputJson(id, value);
                String responseBody = objectMapper.writeValueAsString(inputJson);

                response.setStatusCode(200);
                response.setBody(responseBody);
                response.setHeaders(Map.of("Content-Type", "application/json"));
            } else {
                response.setStatusCode(404);
                response.setBody("{\"error\": \"Input not found for the provided id.\"}");
                response.setHeaders(Map.of("Content-Type", "application/json"));
            }
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

        return response;
    }
}
