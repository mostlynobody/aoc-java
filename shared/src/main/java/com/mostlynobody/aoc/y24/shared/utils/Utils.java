package com.mostlynobody.aoc.y24.shared.utils;

public class Utils {

    public static String removeLastLineBreak(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (input.endsWith("\r\n")) {
            return input.substring(0, input.length() - 2);
        } else if (input.endsWith("\n")) {
            return input.substring(0, input.length() - 1);
        }

        return input;
    }
}
