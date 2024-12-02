package com.mostlynobody.aoc.y24.service.rednosedreports;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

public class RedNosedReportsService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        List<List<Integer>> reports = Utils.removeLastLineBreak(rawInput).lines()
                .map(s -> (Arrays.stream(s.split("\\s+")).map(Integer::parseInt).toList())).toList();

        var silver = reports.stream().filter(this::isReportSafe).count();
        var gold = reports.stream().filter(this::isReportSafeWithDampener).count();
        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private boolean isReportSafeWithDampener(List<Integer> report) {
        if (isReportSafe(report)) return true;

        return IntStream.range(0, report.size()).mapToObj(index -> {
            var dampenedReport = new ArrayList<>(report);
            dampenedReport.remove(index);
            return dampenedReport;
        }).anyMatch(this::isReportSafe);
    }

    private boolean isReportSafe(List<Integer> report) {
        boolean isIncreasing = report.get(0) < report.get(1);
        for (int i = 0; i < report.size() - 1; i++) {
            if (abs(report.get(i) - report.get(i + 1)) > 3 || report.get(i)
                    .equals(report.get(i + 1)) || isIncreasing != report.get(i) < report.get(i + 1)) {
                return false;
            }
        }

        return true;
    }
}
