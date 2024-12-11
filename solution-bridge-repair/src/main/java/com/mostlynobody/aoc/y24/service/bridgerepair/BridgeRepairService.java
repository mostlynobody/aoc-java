package com.mostlynobody.aoc.y24.service.bridgerepair;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;


public class BridgeRepairService implements SolutionService {

    private static final Pattern INPUT_REGEX = Pattern.compile("\\d+");

    // We love performance
    private static final HashMap<Integer, List<List<Character>>> permutationLookupSilver = new HashMap<>();
    private static final HashMap<Integer, List<List<Character>>> permutationLookupGold = new HashMap<>();

    @Override
    public SolutionJson solve(String rawInput) {
        List<Calibration> calibrations = Utils.removeLastLineBreak(rawInput).lines().map(Calibration::new).toList();

        var silver = calibrations.stream().filter(Calibration::checkSilver).mapToLong(it -> it.output).sum();
        var gold = calibrations.stream().filter(Calibration::checkGold).mapToLong(it -> it.output).sum();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private List<List<Character>> getPermutations(int size, char... operators) {
        int operatorCount = operators.length;
        var permutations = new ArrayList<List<Character>>();
        for (int i = 0; i < (int) Math.pow(operatorCount, size); i++) permutations.add(new ArrayList<>());
        for (int n = 0; n < size; n++) {
            for (int i = 0; i < permutations.size(); i++) {
                permutations.get(i).add(operators[(int) ((i / Math.pow(operatorCount, n)) % operatorCount)]);
            }
        }

        return permutations;
    }

    private long getDigitCount(long n) {
        if (n == 0) return 1;
        return (long) (Math.log10(n) + 1);
    }


    private class Calibration {
        final Long output;
        List<Long> inputs;

        public Calibration(String line) {
            inputs = INPUT_REGEX.matcher(line).results().map(MatchResult::group).map(Long::parseLong).toList();
            output = inputs.getFirst();
            inputs = inputs.subList(1, inputs.size());

            if (!permutationLookupSilver.containsKey(inputs.size() - 1)) {
                permutationLookupSilver.put(inputs.size() - 1, getPermutations(inputs.size() - 1, '+', '*'));
                permutationLookupGold.put(inputs.size() - 1, getPermutations(inputs.size() - 1, '+', '*', '|'));
            }
        }

        public boolean checkSilver() {
            return checkIfPossible(permutationLookupSilver.get(inputs.size() - 1));
        }

        public boolean checkGold() {
            return checkIfPossible(permutationLookupGold.get(inputs.size() - 1));
        }

        private boolean checkIfPossible(List<List<Character>> permutations) {
            for (List<Character> permutation : permutations) {
                Iterator<Long> inputIt = inputs.iterator();
                Iterator<Character> operatorIt = permutation.iterator();
                long result = inputIt.next();
                while (inputIt.hasNext()) {
                    long nextNum = inputIt.next();
                    switch (operatorIt.next()) {
                        case '+' -> result += nextNum;
                        case '*' -> result *= nextNum;
                        case '|' -> result = (result * (long) Math.pow(10, getDigitCount(nextNum))) + nextNum;
                        default -> throw new IllegalStateException("Illegal operator");
                    }
                }

                if (result == output) return true;
            }

            return false;
        }
    }
}
