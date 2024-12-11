package com.mostlynobody.aoc.y24.service.guardgallivant;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.*;

public class GuardGallivantService implements SolutionService {

    @Override
    public SolutionJson solve(String rawInput) {
        GuardMap initialMap = new GuardMap(Utils.removeLastLineBreak(rawInput).lines().toArray(String[]::new));
        Coordinate initialGuardPosition = initialMap.findTilePositions('^').getFirst();
        Guard initialGuard = new Guard(initialGuardPosition);

        while (initialGuard.status == GuardStatus.ACTIVE) {
            initialGuard.moveForward(initialMap);
        }

        var silver = initialMap.findTilePositions('X').size();

        List<Coordinate> possibleObstructionLocations = initialMap.findTilePositions('X');
        possibleObstructionLocations.remove(initialGuardPosition);

        var gold = possibleObstructionLocations.stream().filter(obstruction -> {
            GuardMap m = new GuardMap(Utils.removeLastLineBreak(rawInput).lines().toArray(String[]::new));
            m.setTile(obstruction, '#');
            Guard g = new Guard(initialGuardPosition);

            while (g.status == GuardStatus.ACTIVE) {
                g.moveForward(m);
            }
            return g.status == GuardStatus.LOOPING;
        }).count();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    enum Direction {
        UP, RIGHT, DOWN, LEFT;

        Direction turnRight() {
            return switch (this) {
                case UP -> RIGHT;
                case RIGHT -> DOWN;
                case DOWN -> LEFT;
                case LEFT -> UP;
            };
        }

        Coordinate moveCoordinate(Coordinate c) {
            return switch (this) {
                case UP -> new Coordinate(c.x, c.y - 1);
                case RIGHT -> new Coordinate(c.x + 1, c.y);
                case DOWN -> new Coordinate(c.x, c.y + 1);
                case LEFT -> new Coordinate(c.x - 1, c.y);
            };
        }

    }

    enum GuardStatus {
        ACTIVE, LOOPING, EXITED;
    }

    private record Coordinate(int x, int y) {
    }

    private static class Guard {
        Coordinate coordinate;
        Direction direction = Direction.UP;
        GuardStatus status = GuardStatus.ACTIVE;
        Map<Coordinate, Set<Direction>> visitedObstructions = new HashMap<>();

        public Guard(Coordinate coordinate) {
            this.coordinate = coordinate;
        }

        void moveForward(GuardMap guardMap) {
            // mark current position as visited
            guardMap.setTile(coordinate, 'X');

            // try moving
            Coordinate nextCoordinate = direction.moveCoordinate(coordinate);
            char nextTile = guardMap.getTile(nextCoordinate);
            switch (nextTile) {
                case ' ' -> status = GuardStatus.EXITED;
                case '#' -> {
                    Set<Direction> obstructionVisits = visitedObstructions.getOrDefault(nextCoordinate, new HashSet<>());
                    if (obstructionVisits.contains(direction)) {
                        status = GuardStatus.LOOPING;
                    } else {
                        obstructionVisits.add(direction);
                    }
                    visitedObstructions.put(nextCoordinate, obstructionVisits);
                    direction = direction.turnRight();
                }

                default -> this.coordinate = nextCoordinate;
            }
        }
    }

    private static class GuardMap {
        char[][] grid;
        int height, width;

        GuardMap(String[] lines) {
            this.height = lines.length;
            this.width = lines[0].length();
            this.grid = new char[height][width];

            for (int y = 0; y < height; y++) grid[y] = lines[y].toCharArray();
        }

        private char getTile(Coordinate coord) {
            if (coord.x < 0 || coord.x >= width || coord.y < 0 || coord.y >= height) return ' ';
            else return grid[coord.y][coord.x];
        }

        private void setTile(Coordinate coord, char tile) {
            grid[coord.y][coord.x] = tile;
        }

        private List<Coordinate> findTilePositions(char tile) {
            List<Coordinate> positions = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (grid[y][x] == tile) {
                        positions.add(new Coordinate(x, y));
                    }
                }
            }

            return positions;
        }
    }
}