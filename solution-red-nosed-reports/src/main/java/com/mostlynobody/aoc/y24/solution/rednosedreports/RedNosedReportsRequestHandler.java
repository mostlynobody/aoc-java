package com.mostlynobody.aoc.y24.solution.rednosedreports;

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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

@SerdeImport(InputJson.class)
@SerdeImport(SolutionJson.class)
public class RedNosedReportsRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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

        List<List<Integer>> reports = Utils.removeLastLineBreak(inputJson.value())
                .lines().map(s -> (Arrays.stream(s.split("\\s+")).map(Integer::parseInt).toList()))
                .toList();

        var silver = reports.stream().filter(this::isReportSafe).count();
        var gold = reports.stream().filter(this::isReportSafeWithDampener).count();

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

    private boolean isReportSafeWithDampener(List<Integer> report) {
        if (isReportSafe(report)) {
            return true;
        }

        return IntStream.range(0, report.size()).mapToObj(index -> {
                    var dampenedReport = new ArrayList<>(report);
                    dampenedReport.remove(index);
                    return dampenedReport;
                })
                .anyMatch(this::isReportSafe);
    }

    private boolean isReportSafe(List<Integer> report) {
        boolean isIncreasing = report.get(0) < report.get(1);
        for (int i = 0; i < report.size() - 1; i++) {
            if (abs(report.get(i) - report.get(i + 1)) > 3
                    || report.get(i).equals(report.get(i + 1))
                    || isIncreasing != report.get(i) < report.get(i + 1)) {
                return false;
            }
        }

        return true;
    }
}
