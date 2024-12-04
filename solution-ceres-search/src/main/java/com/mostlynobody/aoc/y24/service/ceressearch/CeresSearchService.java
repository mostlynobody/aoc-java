package com.mostlynobody.aoc.y24.service.ceressearch;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.Arrays;

public class CeresSearchService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        String[] lines = Utils.removeLastLineBreak(rawInput).lines().toArray(String[]::new);
        WordField wf = new WordField(lines);

        int silver = 0, gold = 0;
        for (int x = 0; x < wf.width; x++) {
            for (int y = 0; y < wf.height; y++) {
                silver += wf.checkForXMAS(x, y);
                if (wf.checkForXShapedMAS(x, y)) gold++;
            }
        }

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private static class WordField {
        // all directions needed for part 1
        private static final int[][] DIRECTIONS = {{0, 1}, {0, -1}, {1, 0}, {1, 1}, {1, -1}, {-1, 0}, {-1, 1}, {-1, -1}};

        // the three explicit directions needed for part 2
        private static final int[] DOWN_RIGHT = {1, 1};
        private static final int[] DOWN_LEFT = {-1, 1};
        private static final int[] UP_RIGHT = {1, -1};

        String[] lines;
        int height, width;

        WordField(String[] lines) {
            this.lines = lines;
            this.height = lines.length;
            this.width = lines[0].length();
        }

        int checkForXMAS(int x, int y) {
            return (int) Arrays.stream(DIRECTIONS).map(it -> checkForWordInDirection(x, y, "XMAS", it))
                    .filter(it -> it).count();
        }

        boolean checkForXShapedMAS(int x, int y) {
            // The easiest way is to check for "MAS" or SAM" in one direction and its two neighbouring directions
            for (String word : new String[]{"SAM", "MAS"}) {
                if (checkForWordInDirection(x, y, word, DOWN_RIGHT) &&
                        (checkForWordInDirection(x + 2, y, word, DOWN_LEFT) ||
                                checkForWordInDirection(x, y + 2, word, UP_RIGHT))) {
                    return true;
                }
            }
            return false;
        }

        private boolean checkForWordInDirection(int x, int y, String word, int[] direction) {
            for (char c : word.toCharArray()) {
                if (this.getChar(x, y) != c) return false;
                x += direction[0];
                y += direction[1];
            }
            return true;
        }

        private char getChar(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) return ' ';
            else return lines[y].charAt(x);
        }
    }
}
