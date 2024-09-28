package io.kislay.spec.generator.neo.tests;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.MapUtils;
import org.apache.kafka.common.config.types.Password;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterContext {

    private static final String FIELD_PAGINATION = "pagination";
    public static final String FIELD_SOURCE_CONFIG = "source_config";
    public static final String FIELD_TOKEN = "token";
    public static final String FIELD_OFFSET = "offset";
    public static final String FIELD_TEMP_VARS = "temp_vars";

    @Builder.Default Map<String, Object> contextMap = new HashMap<>();

    Map<String, Object> sourceConfig;

    Map<String, Object> oauthToken;

    Map<String, Object> tokenMeta;

    Map<String, Object> offset;

    Map<String, Object> currentRecord;

    PaginationContext paginationContext;

    Response response;

    Map<String, Object> tempVars;

    public String getValue(String config, String param) {
        switch (config) {
            case FIELD_SOURCE_CONFIG:
                Object value = sourceConfig.get(param);
                if (value instanceof Password) {
                    return ((Password) value).value();
                }
                return String.valueOf(value);
            case FIELD_OFFSET:
                return MapUtils.getString(offset, param, (String)null);
            case FIELD_TEMP_VARS:
                return MapUtils.getString(tempVars, param, (String)null);
            default:
                return null;
        }
    }

    public void put(String config, Object value) {
        contextMap.put(config, value);
    }

    public Object getFromContextMap(String config) {
        if (!contextMap.containsKey(config)) {
            throw new RuntimeException(String.format("Unknown config : %s", config));
        }
        return contextMap.get(config);
    }

    public Object getValueV2(String config, String param) {
        switch (config) {
            case FIELD_SOURCE_CONFIG:
                Object value = sourceConfig.get(param);
                if (value instanceof Password) {
                    return ((Password) value).value();
                }
                return value;
            case FIELD_TOKEN:
                return MapUtils.getObject(oauthToken, param, null);
            case FIELD_OFFSET:
                return MapUtils.getObject(offset, param, null);
            case FIELD_PAGINATION:
                return MapUtils.getString(paginationContext.getContext(), param, (String)null);
            case "current_record":
                return currentRecord;
            case FIELD_TEMP_VARS:
                return MapUtils.getObject(tempVars, param, null);
            default:
                return null;
        }
    }

    @NoArgsConstructor
    public static class PaginationContext {
        @Getter @Setter private Map<String, String> context = new HashMap<>();

        public void update(String config, String value) {
            this.context.put(config, value);
        }
    }

    //    public static void main(String[] args) {
    //        SourceConfig sourceConfig = new SourceConfig() {
    //            @Override
    //            public ConfigDef configDef() {
    //                ConfigDefinition configDefinition = new ConfigDefinition() {
    //                    @Override
    //                    public String configName() {
    //                        return "password";
    //                    }
    //
    //                    @Override
    //                    public ConfigDef.Importance importance() {
    //                        return ConfigDef.Importance.HIGH;
    //                    }
    //
    //                    @Override
    //                    public String doc() {
    //                        return "The password";
    //                    }
    //
    //                    @Override
    //                    public ConfigDef.Type type() {
    //                        return ConfigDef.Type.PASSWORD;
    //                    }
    //
    //                    @Override
    //                    public Object defaultValue() {
    //                        return null;
    //                    }
    //                };
    //                return
    // TemplateBasedSourceConfig.generateConfigDef(Collections.singletonList(configDefinition));
    //            }
    //
    //            @Override
    //            public List<Parameter> displayConfig() {
    //                return Collections.emptyList();
    //            }
    //
    //            @Override
    //            public UniqueSourceIdentifier uniqueSourceIdentifier() {
    //                return new UniqueSourceIdentifier() {
    //                    @Override
    //                    public String identifier() {
    //                        return "null";
    //                    }
    //
    //                    @Override
    //                    public List<Parameter> parameters() {
    //                        return Collections.emptyList();
    //                    }
    //                };
    //            }
    //        };
    //        Map<String, Object> map = new LinkedHashMap<>();
    //        map.put("password", "password");
    //        TemplateBasedSourceConfig templateBasedSourceConfig = new
    // TemplateBasedSourceConfig(sourceConfig, map);
    //        MasterContext context = new MasterContext(templateBasedSourceConfig, null, null);
    //        System.out.println(context.getValue("source_config", "password", true));
    //    }
}
