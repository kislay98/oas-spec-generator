package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaginationRule {
    private String rule;
    private int limit;
    private String dataRoot;
    private int startingPage;
}
