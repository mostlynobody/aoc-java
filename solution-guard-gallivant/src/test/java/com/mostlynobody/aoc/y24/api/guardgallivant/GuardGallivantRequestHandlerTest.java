package com.mostlynobody.aoc.y24.api.guardgallivant;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mostlynobody.aoc.y24.service.guardgallivant.GuardGallivantService;
import com.mostlynobody.aoc.y24.shared.records.InputJson;
import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SerdeImport(InputJson.class)
@SerdeImport(SolutionJson.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class GuardGallivantRequestHandlerTest {

    private final JsonMapper objectMapper = mock(JsonMapper.class);
    private final GuardGallivantService guardGallivantService = mock(GuardGallivantService.class);

    private final GuardGallivantRequestHandler functionRequestHandler =
            new GuardGallivantRequestHandler(objectMapper, guardGallivantService);

    @Test
    void testExecute_Success() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        String inputJsonString = "{\"value\": \"testInput\"}";
        requestEvent.setBody(inputJsonString);
        InputJson inputJson = new InputJson("testId", "testInput");
        SolutionJson solutionJson = new SolutionJson("testSilver", "testGold");
        String solutionJsonString = "{\"solution\": \"testSolution\"}";

        when(objectMapper.readValue(inputJsonString, InputJson.class)).thenReturn(inputJson);
        when(guardGallivantService.solve("testInput")).thenReturn(solutionJson);
        when(objectMapper.writeValueAsString(solutionJson)).thenReturn(solutionJsonString);

        APIGatewayProxyResponseEvent response = functionRequestHandler.execute(requestEvent);

        assertEquals(200, response.getStatusCode());
        assertEquals(solutionJsonString, response.getBody());

        verify(objectMapper).readValue(inputJsonString, InputJson.class);
        verify(guardGallivantService).solve("testInput");
        verify(objectMapper).writeValueAsString(solutionJson);
        verifyNoMoreInteractions(objectMapper, guardGallivantService);
    }

    @Test
    void testExecute_MissingRequestBody() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody(null);

        APIGatewayProxyResponseEvent response = functionRequestHandler.execute(requestEvent);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Missing or empty request body\"}.\"}", response.getBody());

        verifyNoInteractions(objectMapper, guardGallivantService);
    }

    @Test
    void testExecute_EmptyRequestBody() {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setBody("");

        APIGatewayProxyResponseEvent response = functionRequestHandler.execute(requestEvent);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Missing or empty request body\"}.\"}", response.getBody());

        verifyNoInteractions(objectMapper, guardGallivantService);
    }

    @Test
    void testExecute_MalformedInputJson() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        String malformedJson = "{invalidJson}";
        IOException exception = new IOException("Malformed JSON");
        requestEvent.setBody(malformedJson);

        when(objectMapper.readValue(malformedJson, InputJson.class)).thenThrow(exception);

        APIGatewayProxyResponseEvent response = functionRequestHandler.execute(requestEvent);

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\": \"Malformed request body\"}.\"}", response.getBody());

        verify(objectMapper).readValue(malformedJson, InputJson.class);
        verify(guardGallivantService, never()).solve(anyString());
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    void testExecute_SerializationError() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        String inputJsonString = "{\"value\": \"testInput\"}";
        requestEvent.setBody(inputJsonString);
        InputJson inputJson = new InputJson("testId", "testInput");
        SolutionJson solutionJson = new SolutionJson("testSilver", "testGold");

        when(objectMapper.readValue(inputJsonString, InputJson.class)).thenReturn(inputJson);
        when(guardGallivantService.solve("testInput")).thenReturn(solutionJson);
        when(objectMapper.writeValueAsString(solutionJson)).thenThrow(new RuntimeException("Serialization Failed"));

        APIGatewayProxyResponseEvent response = functionRequestHandler.execute(requestEvent);

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\": \"Internal Server Error.\"}", response.getBody());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(objectMapper).readValue(inputJsonString, InputJson.class);
        verify(guardGallivantService).solve("testInput");
        verify(objectMapper).writeValueAsString(solutionJson);
    }

    @Test
    void testExecute_ServiceThrowsException() throws IOException {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        String inputJsonString = "{\"value\": \"testInput\"}";
        requestEvent.setBody(inputJsonString);
        InputJson inputJson = new InputJson("testId", "testInput");

        when(objectMapper.readValue(inputJsonString, InputJson.class)).thenReturn(inputJson);
        when(guardGallivantService.solve("testInput")).thenThrow(new RuntimeException("Service Failed"));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> functionRequestHandler.execute(requestEvent));

        assertEquals("Service Failed", thrown.getMessage());

        verify(objectMapper).readValue(inputJsonString, InputJson.class);
        verify(guardGallivantService).solve("testInput");
        verify(objectMapper, never()).writeValueAsString(any());
    }
}
