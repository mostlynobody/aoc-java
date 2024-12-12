package com.mostlynobody.aoc.y24.shared.utils;

import java.util.Objects;

public record Vector2D(double x, double y) {

    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    public Vector2D sub(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Vector2D vector2D)) return false;
        return Double.compare(x, vector2D.x) == 0 && Double.compare(y, vector2D.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
