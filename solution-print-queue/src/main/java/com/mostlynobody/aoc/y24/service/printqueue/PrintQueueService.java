package com.mostlynobody.aoc.y24.service.printqueue;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class PrintQueueService implements SolutionService {

    private final HashMap<Integer, PageNumber> pageNumbers = new HashMap<>();


    @Override
    public SolutionJson solve(String rawInput) {
        String[] inputParts = Utils.removeLastLineBreak(rawInput).split("\n\n");
        List<int[]> rules = inputParts[0].lines()
                .map(it -> Arrays.stream(it.split("\\|")).mapToInt(Integer::parseInt).toArray()).toList();


        for (int[] rule : rules) {
            PageNumber lower = pageNumbers.getOrDefault(rule[0], new PageNumber(rule[0]));
            PageNumber higher = pageNumbers.getOrDefault(rule[1], new PageNumber(rule[1]));
            higher.precursors.add(rule[0]);
            pageNumbers.put(rule[0], lower);
            pageNumbers.put(rule[1], higher);
        }

        List<List<Integer>> updates = inputParts[1].lines()
                .map(it -> Arrays.stream((it.split(","))).map(Integer::parseInt).toList()).toList();

        var silver = updates.stream().filter(this::checkUpdate).mapToInt(it -> it.get((it.size() / 2))).sum();
        var gold = updates.stream().filter(it -> !checkUpdate(it)).map(this::orderUpdate)
                .mapToInt(it -> it.get((it.size() / 2))).sum();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private List<Integer> orderUpdate(List<Integer> update) {
        List<PageNumber> orderedPageNumbers = new ArrayList<>(update.stream()
                .map(it -> new PageNumber(pageNumbers.get(it), update)).toList());

        orderedPageNumbers.sort((Comparator.comparingInt(o -> o.precursors.size())));

        return orderedPageNumbers.stream().map(it -> it.number).collect(Collectors.toList());
    }

    private boolean checkUpdate(List<Integer> update) {
        for (int i = 0; i < update.size(); i++) {
            PageNumber pageNumber = pageNumbers.get(update.get(i));
            for (int j = i + 1; j < update.size(); j++) {
                if (pageNumber.precursors.contains(update.get(j))) return false;
            }
        }

        return true;
    }

    private static class PageNumber {
        final int number;
        Set<Integer> precursors = new HashSet<>();

        public PageNumber(int number) {
            this.number = number;
        }

        public PageNumber(PageNumber source, List<Integer> subset) {
            this.number = source.number;
            this.precursors.addAll(source.precursors);
            this.precursors.retainAll(subset);
        }
    }
}
