package com.mostlynobody.aoc.y24.solution.historianhysteria;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.shared.records.Input;
import com.mostlynobody.aoc.y24.shared.records.Solution;
import com.mostlynobody.aoc.y24.shared.utils.TestUtils;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SerdeImport(Input.class)
@SerdeImport(Solution.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FunctionRequestHandlerTest {

    private FunctionRequestHandler handler;
    private JsonMapper jsonMapper;

    @BeforeAll
    void setUp() {
        jsonMapper = JsonMapper.createDefault();
        handler = new FunctionRequestHandler();
        handler.objectMapper = jsonMapper;
    }


    @Test
    void testExecute() throws IOException {
        String inputFile = TestUtils.readResourceFile("input");
        String solutionFile = TestUtils.readResourceFile("solution.json");

        Input inputJson = new Input("2024", "01", inputFile);
        String requestBody = jsonMapper.writeValueAsString(inputJson);
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent().withBody(requestBody);

        APIGatewayProxyResponseEvent response = handler.execute(requestEvent);
        assertEquals(200, response.getStatusCode());

        Solution resultJson = jsonMapper.readValue(response.getBody(), Solution.class);
        Solution expectedJson = jsonMapper.readValue(solutionFile, Solution.class);

        assertEquals(resultJson.silver(), expectedJson.silver());
        assertEquals(resultJson.gold(), expectedJson.gold());
    }
}
