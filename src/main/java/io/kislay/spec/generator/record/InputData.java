package io.kislay.spec.generator.record;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public record InputData (List<String> urls, String prompt, boolean generateSpecOnly) { }
