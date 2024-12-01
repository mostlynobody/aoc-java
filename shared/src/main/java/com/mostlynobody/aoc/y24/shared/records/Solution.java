package com.mostlynobody.aoc.y24.shared.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Solution(@JsonProperty("silver") String silver,
                       @JsonProperty("gold") String gold) {
}