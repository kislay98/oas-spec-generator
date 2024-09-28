package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

public @Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class ConfigDefinition {
    private String configName;
    private String type;
    private String importance;
    private String doc;
    private String defaultValue;
}
