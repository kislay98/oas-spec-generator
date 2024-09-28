package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class SourceConfig {
    @JsonProperty("config_definitions")
    private List<ConfigDefinition> configDefinitions;
    @JsonProperty("display_config")
    private List<Object> displayConfig;
    @JsonProperty("unique_source_identifier")
    private UniqueSourceIdentifier uniqueSourceIdentifier;
}
