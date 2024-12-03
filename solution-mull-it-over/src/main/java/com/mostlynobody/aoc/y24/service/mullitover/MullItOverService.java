package com.mostlynobody.aoc.y24.service.mullitover;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MullItOverService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        int silver = getMulSum(rawInput);
        int gold = 0;

        Matcher doMatcher = Pattern.compile("(?:^|do\\(\\)).*?(?:don't\\(\\)|$)", Pattern.DOTALL).matcher(rawInput);
        while (doMatcher.find()) gold += getMulSum(doMatcher.group(0));

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private int getMulSum(String input) {
        int sum = 0;
        Matcher mulMatcher = Pattern.compile("mul\\((\\d{1,3}),(\\d{1,3})\\)").matcher(input);
        while (mulMatcher.find()) sum += Integer.parseInt(mulMatcher.group(1)) * Integer.parseInt(mulMatcher.group(2));
        return sum;
    }
}
