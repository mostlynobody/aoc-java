package com.mostlynobody.aoc.y24.solution.historianhysteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.Input;
import com.mostlynobody.aoc.y24.shared.records.Solution;
import com.mostlynobody.aoc.y24.shared.utils.Utils;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.Math.abs;

@SerdeImport(Input.class)
@SerdeImport(Solution.class)
public class FunctionRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    JsonMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Input inputJson;

        String requestBody = input.getBody();
        try {
            inputJson = objectMapper.readValue(requestBody, Input.class);
        } catch (IOException e) {
            response.setStatusCode(400);
            response.setBody(e.getMessage());
            return response;
        }

        ArrayList<Integer> leftList = new ArrayList<>(), rightList = new ArrayList<>();
        Utils.removeLastLineBreak(inputJson.value()).lines().forEach(line -> {
            var nums = line.split(" {3}");
            leftList.add(Integer.parseInt(nums[0]));
            rightList.add(Integer.parseInt(nums[1]));
        });

        leftList.sort(Integer::compareTo);
        rightList.sort(Integer::compareTo);

        int silver = 0;
        long gold = 0;
        for (int i = 0; i < leftList.size(); i++) {
            int l = leftList.get(i);

            silver += abs(l - rightList.get(i));
            gold += l * rightList.stream().filter(r -> r == l).count();
        }

        try {
            String responseBody = objectMapper.writeValueAsString(new Solution(String.valueOf(silver), String.valueOf(gold)));
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
