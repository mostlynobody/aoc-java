package com.mostlynobody.aoc.y24.service.mullitover;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.utils.TestUtils;
import io.micronaut.json.JsonMapper;
import io.micronaut.serde.annotation.SerdeImport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SerdeImport(SolutionJson.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MullItOverServiceTest {

    private MullItOverService mullItOverService;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setup() {
        mullItOverService = new MullItOverService();
        jsonMapper = JsonMapper.createDefault();
    }

    @Test
    void testSolve() throws IOException {
        String input = TestUtils.readResourceFile("input");
        String solutionFile = TestUtils.readResourceFile("solution.json");
        SolutionJson solutionJson = jsonMapper.readValue(solutionFile, SolutionJson.class);

        SolutionJson result = mullItOverService.solve(input);

        assertEquals(result.silver(), solutionJson.silver());
        assertEquals(result.gold(), solutionJson.gold());
    }
}