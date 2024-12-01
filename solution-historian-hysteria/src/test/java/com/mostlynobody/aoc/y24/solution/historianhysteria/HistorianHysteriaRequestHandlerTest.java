package com.mostlynobody.aoc.y24.solution.historianhysteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.InputJson;
import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.utils.TestUtils;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SerdeImport(InputJson.class)
@SerdeImport(SolutionJson.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HistorianHysteriaRequestHandlerTest {

    private HistorianHysteriaRequestHandler handler;
    private JsonMapper jsonMapper;

    @BeforeAll
    void setUp() {
        jsonMapper = JsonMapper.createDefault();
        handler = new HistorianHysteriaRequestHandler();
        handler.objectMapper = jsonMapper;
    }


    @Test
    void testExecute() throws IOException {
        String inputFile = TestUtils.readResourceFile("input");
        String solutionFile = TestUtils.readResourceFile("solution.json");

        InputJson inputJson = new InputJson("historian-hysteria", inputFile);
        String requestBody = jsonMapper.writeValueAsString(inputJson);
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);
        assertEquals(200, response.getStatusCode());

        SolutionJson resultJson = jsonMapper.readValue(response.getBody(), SolutionJson.class);
        SolutionJson expectedJson = jsonMapper.readValue(solutionFile, SolutionJson.class);

        assertEquals(resultJson.silver(), expectedJson.silver());
        assertEquals(resultJson.gold(), expectedJson.gold());
    }
}
