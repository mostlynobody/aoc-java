package com.mostlynobody.aoc.y24.service.hoofit;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

public class HoofItService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        String[] lines = Utils.removeLastLineBreak(rawInput).lines().toArray(String[]::new);
        HikingMap hikingMap = new HikingMap(lines);
        List<HikingSpot> trailheads = hikingMap.getAllSpots().stream().filter(it -> it.height == 0).toList();

        int silver = 0;
        int gold = 0;
        for (HikingSpot trailhead : trailheads) {
            hikingMap.resetVisits();
            Hiker hiker = new Hiker(trailhead);
            hiker.hike(hikingMap);

            var visitedPeaks = hikingMap.getAllSpots().stream().filter(it -> it.height == 9 && it.visited > 0).toList();
            silver += visitedPeaks.size();
            gold += visitedPeaks.stream().map(it -> it.visited).reduce(0, Integer::sum);
        }

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private record Coordinate(int x, int y) {
        public Coordinate[] neighbours() {
            return new Coordinate[]{
                    new Coordinate(x + 1, y),
                    new Coordinate(x - 1, y),
                    new Coordinate(x, y + 1),
                    new Coordinate(x, y - 1)};
        }
    }


    private static class Hiker {
        Deque<HikingSpot> hikingQueue = new ArrayDeque<>();

        public Hiker(HikingSpot trailhead) {
            trailhead.visited++;
            hikingQueue.add(trailhead);
        }

        public void hike(HikingMap map) {
            while (!hikingQueue.isEmpty()) {
                HikingSpot currentSpot = hikingQueue.removeFirst();
                currentSpot.visited++;

                Arrays.stream(currentSpot.coordinate.neighbours()).map(map::getHikingSpot)
                        .filter(it -> it != null && it.height - currentSpot.height == 1)
                        .forEach(it -> hikingQueue.addLast(it));
            }
        }
    }


    private static class HikingSpot {
        Coordinate coordinate;
        int height;
        int visited = 0;


        public HikingSpot(int height, int x, int y) {
            this.height = height;
            this.coordinate = new Coordinate(x, y);
        }
    }

    private static class HikingMap {
        HikingSpot[][] map;
        int height, width;

        public HikingMap(String[] lines) {
            this.height = lines.length;
            this.width = lines[0].length();
            this.map = new HikingSpot[height][width];

            for (int y = 0; y < height; y++) {
                var line = lines[y].toCharArray();
                for (int x = 0; x < width; x++) {
                    map[y][x] = new HikingSpot(line[x] - '0', x, y);
                }
            }
        }

        public List<HikingSpot> getAllSpots() {
            return Arrays.stream(map).flatMap(Stream::of).toList();
        }

        public void resetVisits() {
            for (HikingSpot[] line : map) for (HikingSpot spot : line) spot.visited = 0;
        }

        public HikingSpot getHikingSpot(Coordinate coord) {
            if (coord.x < 0 || coord.y < 0 || coord.x >= width || coord.y >= height) return null;
            return map[coord.y][coord.x];
        }
    }
}
