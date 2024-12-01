package com.mostlynobody.aoc.y24.shared;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record SessionCookie(@JsonProperty("id") String id,
                            @Nullable @JsonProperty("value") String value) {
}