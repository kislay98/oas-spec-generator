package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

import java.util.List;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class API {
    private String tag;
    private String url;
    private String method;
    private List<PathParam> pathParams;
    private List<QueryParam> queryParams;
    private List<Variable> variables;
    private PaginationRule paginationRule;
}