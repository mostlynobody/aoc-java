package com.mostlynobody.aoc.y24.service.rednosedreports;

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
public class RedNosedReportsServiceTest {

    private RedNosedReportsService redNosedReportsService;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setup() {
        redNosedReportsService = new RedNosedReportsService();
        jsonMapper = JsonMapper.createDefault();
    }

    @Test
    void testSolve() throws IOException {
        String input = TestUtils.readResourceFile("input");
        String solutionFile = TestUtils.readResourceFile("solution.json");
        SolutionJson solutionJson = jsonMapper.readValue(solutionFile, SolutionJson.class);

        SolutionJson result = redNosedReportsService.solve(input);

        assertEquals(result.silver(), solutionJson.silver());
        assertEquals(result.gold(), solutionJson.gold());
    }

}
