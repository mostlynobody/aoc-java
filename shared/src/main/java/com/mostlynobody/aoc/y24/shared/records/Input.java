package com.mostlynobody.aoc.y24.shared.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Input(@JsonProperty("year") String year,
                    @JsonProperty("day") String day,
                    @Nullable @JsonProperty("value") String value) {
}