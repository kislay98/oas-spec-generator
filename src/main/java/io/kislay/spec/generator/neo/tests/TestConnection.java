package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TestConnection {

    @JsonProperty("api")
    private String api;

    @JsonProperty("data_root")
    private String dataRoot;
}
