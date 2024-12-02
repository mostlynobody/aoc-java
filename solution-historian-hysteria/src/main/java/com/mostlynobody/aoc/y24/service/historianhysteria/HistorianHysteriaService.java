package com.mostlynobody.aoc.y24.service.historianhysteria;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Math.abs;

public class HistorianHysteriaService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        ArrayList<Integer> leftList = new ArrayList<>(), rightList = new ArrayList<>();
        HashMap<Integer, Integer> leftMap = new HashMap<>(), rightMap = new HashMap<>();

        Utils.removeLastLineBreak(rawInput).lines().forEach(line -> {
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

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }
}
