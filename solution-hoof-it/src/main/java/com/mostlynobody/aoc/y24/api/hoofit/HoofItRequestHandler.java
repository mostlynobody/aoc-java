package com.mostlynobody.aoc.y24.api.hoofit;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.service.hoofit.HoofItService;
import com.mostlynobody.aoc.y24.shared.records.InputJson;
import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@SerdeImport(InputJson.class)
@SerdeImport(SolutionJson.class)
public class HoofItRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LoggerFactory.getLogger(HoofItRequestHandler.class);

    JsonMapper objectMapper;
    HoofItService hoofItService;

    @Inject
    public HoofItRequestHandler() {
        this.objectMapper = JsonMapper.createDefault();
        this.hoofItService = new HoofItService();
    }

    HoofItRequestHandler(JsonMapper objectMapper, HoofItService hoofItService) {
        this.objectMapper = objectMapper;
        this.hoofItService = hoofItService;
    }

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        String requestBody = input.getBody();

        if (requestBody == null || requestBody.isEmpty()) {
            response.setStatusCode(400);
            response.setBody("{\"error\": \"Missing or empty request body\"}.\"}");
            return response;
        }

        InputJson inputJson;
        try {
            inputJson = objectMapper.readValue(requestBody, InputJson.class);
        } catch (IOException e) {
            response.setStatusCode(400);
            logger.error(e.toString());
            response.setBody("{\"error\": \"Malformed request body\"}.\"}");
            return response;
        }

        SolutionJson solutionJson = hoofItService.solve(inputJson.value());

        try {
            String responseBody = objectMapper.writeValueAsString(solutionJson);
            response.setStatusCode(200);
            response.setBody(responseBody);
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\": \"Internal Server Error.\"}");
            response.setHeaders(Map.of("Content-Type", "application/json"));
            return response;
        }
    }
}
