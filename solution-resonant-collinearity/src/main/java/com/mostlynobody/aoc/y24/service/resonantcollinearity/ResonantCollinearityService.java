package com.mostlynobody.aoc.y24.service.resonantcollinearity;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;
import com.mostlynobody.aoc.y24.shared.utils.Vector2D;

import java.util.*;

public class ResonantCollinearityService implements SolutionService {

    int height, width;

    @Override
    public SolutionJson solve(String rawInput) {
        String[] lines = Utils.removeLastLineBreak(rawInput).lines().toArray(String[]::new);
        height = lines.length;
        width = lines[0].length();

        Map<Character, List<Vector2D>> antennaMap = new HashMap<>();
        Set<Vector2D> antinodes = new HashSet<>();
        Set<Vector2D> extendedAntinodes = new HashSet<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = lines[y].charAt(x);
                if (c != '.') {
                    var antennas = antennaMap.getOrDefault(c, new ArrayList<>());
                    antennas.add(new Vector2D(x, y));
                    antennaMap.put(c, antennas);
                }
            }
        }

        for (List<Vector2D> antennas : antennaMap.values()) {
            for (int i = 0; i < antennas.size() - 1; i++) {
                for (int j = i + 1; j < antennas.size(); j++) {
                    Vector2D a1 = antennas.get(i), a2 = antennas.get(j);
                    var distVec = a2.sub(a1);
                    antinodes.add(a1.sub(distVec));
                    antinodes.add(a2.add(distVec));

                    // calculate extended antinodes
                    var extendedAntinode = a1;
                    while (isInBounds(extendedAntinode)) {
                        extendedAntinodes.add(extendedAntinode);
                        extendedAntinode = extendedAntinode.sub(distVec);
                    }

                    extendedAntinode = a2;
                    while (isInBounds(extendedAntinode)) {
                        extendedAntinodes.add(extendedAntinode);
                        extendedAntinode = extendedAntinode.add(distVec);
                    }
                }
            }
        }

        var silver = antinodes.stream().filter(this::isInBounds).count();
        var gold = extendedAntinodes.stream().filter(this::isInBounds).count();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private boolean isInBounds(Vector2D v) {
        return v.x() >= 0 && v.y() >= 0 && v.x() < width && v.y() < height;
    }
}
