package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.List;

@Getter
public class Template {

    @JsonProperty("source_config")
    private SourceConfig sourceConfig;

    @JsonProperty("hevo_objects")
    private List<JsonNode> objects;

    @JsonProperty("apis")
    private List<API> apis;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("frequency_execution_policy")
    private JsonNode ingestionFrequencyConfig;

    @JsonProperty("auth_config")
    private AuthConfig authConfig;

    @JsonProperty("test_connection")
    private TestConnection testConnection;

    @JsonProperty("error_handling_rule")
    private List<JsonNode> errorHandlingRules;

    @JsonProperty("functions")
    private List<JsonNode> functions;

}
