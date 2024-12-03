package com.mostlynobody.aoc.y24.service.mullitover;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MullItOverService implements SolutionService {

    private static final Pattern MUL_PATTERN = Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)");
    private static final Pattern DO_PATTERN = Pattern.compile("(?:^|do\\(\\)).*?(?:don't\\(\\)|$)", Pattern.DOTALL);

    @Override
    public SolutionJson solve(String rawInput) {
        int silver = calculateMulSum(rawInput);

        Matcher doMatcher = DO_PATTERN.matcher(rawInput);
        int gold = doMatcher.results().mapToInt(it -> calculateMulSum(it.group(0))).sum();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private int calculateMulSum(String input) {
        Matcher mulMatcher = MUL_PATTERN.matcher(input);
        return mulMatcher.results().mapToInt(it -> Integer.parseInt(it.group(1)) * Integer.parseInt(it.group(2))).sum();
    }
}
