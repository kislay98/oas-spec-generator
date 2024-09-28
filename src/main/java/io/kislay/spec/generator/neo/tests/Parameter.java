package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Parameter {
    private int id;
    private String name;
    private String value;
    private DataType dataType;
    private String parameterType;

    public Object value(MasterContext context) {
        String realValue = value.substring(2, value.indexOf("}}"));
        if (!realValue.contains(".")) {
            return this.dataType.value(context.getFromContextMap(realValue));
        }
        String[] split = realValue.split("\\.");
        String param = split[0];
        String config = split[1];
        return this.dataType.value(context.getValueV2(param, config));

    }
}
