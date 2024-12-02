package com.mostlynobody.aoc.y24.solution.historianhysteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.InputJson;
import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.utils.Utils;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

@SerdeImport(InputJson.class)
@SerdeImport(SolutionJson.class)
public class HistorianHysteriaRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    JsonMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        InputJson inputJson;

        String requestBody = input.getBody();
        try {
            inputJson = objectMapper.readValue(requestBody, InputJson.class);
        } catch (IOException e) {
            response.setStatusCode(400);
            response.setBody(e.getMessage());
            return response;
        }

        ArrayList<Integer> leftList = new ArrayList<>(), rightList = new ArrayList<>();
        HashMap<Integer, Integer> leftMap = new HashMap<>(), rightMap = new HashMap<>();

        Utils.removeLastLineBreak(inputJson.value()).lines().forEach(line -> {
            var nums = Arrays.stream(line.split(" \\s+")).map(Integer::parseInt).toList();
            leftList.add(nums.get(0));
            rightList.add(nums.get(1));
            leftMap.put(nums.get(0), 1 + leftMap.getOrDefault(nums.get(0), 0));
            rightMap.put(nums.get(1), 1 + rightMap.getOrDefault(nums.get(1), 0));
        });

        leftList.sort(Integer::compareTo);
        rightList.sort(Integer::compareTo);

        int silver = 0;
        for (int i = 0; i < leftList.size(); i++) {
            silver += abs(leftList.get(i) - rightList.get(i));
        }

        long gold = leftMap.entrySet().stream()
                .map(e -> (long) e.getValue() * e.getKey() * rightMap.getOrDefault(e.getKey(), 0))
                .reduce(0L, Long::sum);

        try {
            String responseBody = objectMapper.writeValueAsString(new SolutionJson(String.valueOf(silver), String.valueOf(gold)));
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
