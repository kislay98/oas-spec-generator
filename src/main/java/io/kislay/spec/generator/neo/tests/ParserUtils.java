package io.kislay.spec.generator.neo.tests;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParserUtils {

    // This is only for those use cases where we use double curly braces as encloser for parameters.
    public static String parseString(
            @NonNull String toParse,
            List<PathParam> parameters,
            @NonNull MasterContext context) {
        StringBuilder stringBuilder = new StringBuilder();
        String stringToParse = "";
        for (stringToParse = toParse;
             stringToParse.contains("{");
             stringToParse = stringToParse.substring(stringToParse.indexOf("}}") + 2)) {
            int startIndex = stringToParse.indexOf("{{");
            stringBuilder.append(stringToParse, 0, startIndex);
            String placeHolder = stringToParse.substring(startIndex + 2, stringToParse.indexOf("}}"));
            for (PathParam parameter : parameters) {
                if (placeHolder.equals(parameter.getName())) {
                    stringBuilder.append(parameter.value(context));
                }
            }
        }
        stringBuilder.append(stringToParse);
        return stringBuilder.toString();
    }

    public static List<String> listAllPlaceHoldersInString(String toParse) {
        List<String> placeHolders = new LinkedList<>();
        for (String stringToParse = toParse;
             stringToParse.contains("{{");
             stringToParse = stringToParse.substring(stringToParse.indexOf("}}") + 2)) {
            int startIndex = stringToParse.indexOf("{{");
            String placeHolder = stringToParse.substring(startIndex + 2, stringToParse.indexOf("}}"));
            placeHolders.add(placeHolder);
        }
        return placeHolders;
    }

    public static LinkedHashMap<String, String> parseDisplayConfig(
            @NonNull List<Parameter> parameters, @NonNull MasterContext context) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();

        for (Parameter parameter : parameters) {
            String value = extractConfig((String) parameter.value(context), context);
            if (Objects.isNull(value)) {
                linkedHashMap.put(parameter.getName(), "null");
            } else {
                linkedHashMap.put(parameter.getName(), value);
            }
        }
        return linkedHashMap;
    }

    public static String extractConfigValuesIntoString(String toParse, MasterContext context) {
        StringBuilder stringBuilder = new StringBuilder();
        String stringToParse = "";
        for (stringToParse = toParse;
             stringToParse.contains("{{");
             stringToParse = stringToParse.substring(stringToParse.indexOf("}}") + 2)) {
            int startIndex = stringToParse.indexOf("{{");
            stringBuilder.append(stringToParse, 0, startIndex);
            String placeHolder = stringToParse.substring(startIndex, stringToParse.indexOf("}}") + 2);
            stringBuilder.append(ParserUtils.extractConfig(placeHolder, context));
        }
        stringBuilder.append(stringToParse);
        return stringBuilder.toString();
    }

    public static String extractConfig(String parameter, MasterContext context) {
        if (parameter.contains("{{")) {
            String config = parameter.substring(2, parameter.indexOf("}}"));
            String[] split = config.split("\\.");
            String configuration = split[0];
            String param = split[1];
            return context.getValue(configuration, param);
        } else {
            return parameter;
        }
    }
}
