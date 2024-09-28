package io.kislay.spec.generator.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import static io.kislay.spec.generator.Constants.APIS;
import static io.kislay.spec.generator.Constants.AUTH_CONFIG;
import io.kislay.spec.generator.FileWriter;
import io.kislay.spec.generator.Utils;
import static io.kislay.spec.generator.Utils.*;
import io.kislay.spec.generator.openai.OpenAIProcessorClient;

import static io.kislay.spec.generator.openai.OpenAIPrompts.*;
import static io.kislay.spec.generator.openai.OpenAPISpecGenerator.parseContent;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;


public class TemplateBuilder {

    private final JsonNode openAPISpec;
    private JsonNode templateSchema;
    private final String sourceType;


    public TemplateBuilder(String openAPISpec) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        this.openAPISpec = objectMapper.readTree(openAPISpec);
        File schemaFile = new File("/src/main/resources/neo_schema.json");
        this.templateSchema = objectMapper.readTree(schemaFile);
        this.sourceType = "test_source";
    }

    // Constructor to initialize from a file path (root path of the project)
    public TemplateBuilder(String filePath, String sourceType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(filePath);
        File schemaFile = new File("src/main/resources/neo_schema.json");
        this.openAPISpec = objectMapper.readTree(jsonFile);
        this.templateSchema = objectMapper.readTree(schemaFile);
        this.sourceType = sourceType;
    }

    public ObjectNode getFrequencyExecutionPolicy(ObjectMapper mapper) {
        ObjectNode frequencyExecutionPolicy = mapper.createObjectNode();
        frequencyExecutionPolicy.put("max", 86400000);
        frequencyExecutionPolicy.put("min", 3600000);
        frequencyExecutionPolicy.put("default", 3600000);
        return frequencyExecutionPolicy;
    }

    public ArrayNode getErrorHandlingRule(ObjectMapper mapper) {
        ArrayNode errorHandlingRule = mapper.createArrayNode();
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Invalid query parameters", 400, "$", 2104));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Invalid or missing credentials", 401, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, null, 403, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Not Found", 404, "$", 2105));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Method not allowed", 405, "$", 2106));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Conflict", 406, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Max items exceeded", 413, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Expectation failed", 417, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "API limit has been reached", 429, "$", 2101));
        errorHandlingRule.add(createErrorHandlingObject(mapper, "Internal Error", 501, "$", 2101));
        return errorHandlingRule;
    }

    public ObjectNode getAuthConfig(ObjectMapper mapper) {
        ObjectNode authConfig = mapper.createObjectNode();
        // Extract components and securitySchemes from the OpenAPI specification
        JsonNode componentsNode = this.openAPISpec.path("components");
        JsonNode securitySchemesNode = componentsNode.path("securitySchemes");
        // Collect the security schemes into a list
        ArrayNode secSchemaArray = mapper.createArrayNode();
        if (securitySchemesNode.isObject()) {
            Iterator<JsonNode> elements = securitySchemesNode.elements();
            while (elements.hasNext()) {
                secSchemaArray.add(elements.next());
            }
        }

        if (!secSchemaArray.isEmpty() && !secSchemaArray.get(0).isNull()) {
          JsonNode firstSecSchema = secSchemaArray.get(0);
          String authScheme = firstSecSchema.path("scheme").asText(null);
          String authType = firstSecSchema.path("type").asText(null);
          if ("http".equals(authType) && ("bearer".equals(authScheme) || "basic".equals(authScheme))) {
              AtomicBoolean isBasicAuth = new AtomicBoolean(false);
              if ("basicAuth".equalsIgnoreCase(securitySchemesNode.fieldNames().next())) {
                  isBasicAuth.set(true);
              }
              if (isBasicAuth.get()) {
                  authConfig.put("auth_type", "BASIC_AUTH");
                  authConfig.put("username", "{{source_config.username}}");
                  authConfig.put("password", "{{source_config.password}}");
              } else {
                authConfig.put("auth_type", "BEARER_TOKEN");
                authConfig.put("header_name", "Authorization");
                authConfig.put("bearer_token", "{{source_config.api_token}}");
              }
      } else if ("apiKey".equals(authType)) {
              authConfig.put("auth_type", "API_TOKEN");
              authConfig.put("header_name", "{{source_config.header_name}}");
              authConfig.put("api_token", "{{source_config.api_token}}");
          } else {
            System.out.println("[ERROR!!!]No auth scheme found");
            authConfig.put("auth_type", "NO_AUTH");
          }
        }
        return authConfig;
    }

    public JsonNode getSourceConfig(ObjectMapper mapper, ObjectNode rootNode) throws JsonProcessingException {
        String docsContent = loadFileContent("/source_config.md");
        String sourceConfigExample = loadFileContent("/source_config_example.json");
        String systemMessage = String.format(FETCH_HEVO_SOURCE_CONFIG_SYSTEM, Utils.escapeForJson(docsContent), Utils.escapeForJson(sourceConfigExample));
        String userMessage = String.format(FETCH_HEVO_SOURCE_CONFIG_O1_PREVIEW, Utils.escapeForJson(docsContent), Utils.escapeForJson(sourceConfigExample),
                Utils.escapeForJson(this.openAPISpec.toString()), Utils.escapeForJson(rootNode.toString()));
        String responseFormat = "{\"type\": \"json_object\"}";
        System.out.println("[Thinking] Trying to get information about source config");
        String response = getResponseFromOpenAIO1("""
                 [{
                             "role": "user",
                             "content": "%s"
                  }
                ]
                """, userMessage);
        String parsedResponse = parseContent(response);
        return mapper.readTree(parsedResponse);
    }

    public ArrayNode getHevoObjects(ObjectMapper mapper, ArrayNode apIs) throws JsonProcessingException {
        String docsContent = loadFileContent("/hevo_object.md");
        String objectsExampleOne = loadFileContent("/hevo_object_example.json");
        String objectsExampleTwo = loadFileContent("/hevo_object_example_2.json");
        ArrayNode hevoObjects = mapper.createArrayNode();
        for (JsonNode apI : apIs) {
            String object = fetchHevoObjectFor(apI, docsContent, objectsExampleOne, objectsExampleTwo);
            hevoObjects.add(mapper.readTree(object));
        }
        return hevoObjects;
    }

    String fetchHevoObjectFor(JsonNode apI, String docsContent, String objectsExampleOne, String objectsExampleTwo) {
        String systemMessage = String.format(FETCH_HEVO_OBJECTS_SYSTEM, Utils.escapeForJson(docsContent), Utils.escapeForJson(objectsExampleOne), Utils.escapeForJson(objectsExampleTwo));
        String userMessage = String.format(FETCH_HEVO_OBJECTS_O1_PREVIEW, Utils.escapeForJson(docsContent),
                Utils.escapeForJson(objectsExampleOne), Utils.escapeForJson(objectsExampleTwo),
                Utils.escapeForJson(apI.get("tag").toString()),
                Utils.escapeForJson(this.openAPISpec.toString()));
        String responseFormat = "{\"type\": \"json_object\"}";
        System.out.println("[Thinking] Trying to get information about Hevo object: " + apI.get("tag").toString());
        String response = getResponseFromOpenAIO1("""
                 [
                  {
                             "role": "user",
                             "content": "%s"
                   }
                ]
                """, userMessage);
//                getResponseFromOpenAI("""
//                 [{
//                             "role": "system",
//                             "content": "%s"
//                  },
//                  {
//                             "role": "user",
//                             "content": "%s"
//                   }
//                ]
//                """, systemMessage, userMessage, responseFormat);
        return parseContent(response);
    }

    private static String getResponseFromOpenAI(String format, String systemMessage, String userMessage, String responseFormat) {
        String messages = String.format(
                format,
                systemMessage, userMessage);
        OpenAIProcessorClient client = new OpenAIProcessorClient();
        return client.processText(messages, responseFormat, MODEL, 0, 0);
    }

    private static String getResponseFromOpenAIO1(String format, String userMessage) {
        String messages = String.format(
                format,
                userMessage);
        OpenAIProcessorClient client = new OpenAIProcessorClient();
        return client.processText(messages, null, MODEL_O1_PREVIEW, 0, 0);
    }

    public ArrayNode getAPIs(ObjectMapper mapper) throws JsonProcessingException {
        JsonNode pathsNode = openAPISpec.path("paths");
        JsonNode firstPath = null;
        Iterator<JsonNode> pathsValues = pathsNode.elements();
        if (pathsValues.hasNext()) {
            firstPath = pathsValues.next();
        }
        JsonNode firstResource = null;
        if (firstPath != null) {
            Iterator<JsonNode> firstPathValues = firstPath.elements();
            if (firstPathValues.hasNext()) {
                firstResource = firstPathValues.next();
            }
        }
        JsonNode parametersNode = firstResource.path("parameters");
        JsonNode parameters = expandRefs(openAPISpec, parametersNode);
        JsonNode requestBodySchema = getRequestBodySchema(openAPISpec, firstResource);
        JsonNode responseSchema = getResponseSchema(openAPISpec, firstResource);

        String docsContent = loadFileContent("/api_pagination.md");
        String apiPaginationExampleOne = loadFileContent("/api_pagination_example.json");
        String apiPaginationExampleTwo = loadFileContent("/api_pagination_example_2.json");
        docsContent = String.join(" ", docsContent.split("\n"));
        String systemMessage = String.format(FETCH_API_JSON_SYSTEM, Utils.escapeForJson(docsContent),
                Utils.escapeForJson(apiPaginationExampleOne), Utils.escapeForJson(apiPaginationExampleTwo));
        String userMessage = String.format(FETCH_API_JSON_O1_PREVIEW, Utils.escapeForJson(docsContent),
                Utils.escapeForJson(apiPaginationExampleOne), Utils.escapeForJson(apiPaginationExampleTwo),
                Utils.escapeForJson(parameters.toString()), Utils.escapeForJson(requestBodySchema.toString()),
                Utils.escapeForJson(responseSchema.toString()));
        String responseFormat = "{\"type\": \"json_object\"}";
        System.out.println("[Thinking] Trying to get information about API and pagination");
        String response = getResponseFromOpenAIO1("""
                 [{
                             "role": "user",
                             "content": "%s"
                   }
                ]
                """, userMessage);
        String parseResponse = parseContent(response);
        return (ArrayNode) mapper.readTree(parseResponse).path(APIS);
    }

    public ObjectNode getTestConnection(ObjectMapper mapper, ArrayNode apIs) {
        ObjectNode testConnection = mapper.createObjectNode();
        testConnection.set("api", apIs.get(0).path("tag"));
        testConnection.put("data_root", "$");
        return testConnection;
    }

    public static void main(String[] args) throws IOException {
        TemplateBuilder templateBuilder = new TemplateBuilder("openAPISpecs_aa5f44e2c80af0e536b4d51d34ff4fda.json", "Campaign Monitor");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode apIs = templateBuilder.getAPIs(mapper);
        rootNode.set(AUTH_CONFIG, templateBuilder.getAuthConfig(mapper));
        rootNode.set(APIS, apIs);
        rootNode.put("source_type", templateBuilder.sourceType);
        rootNode.set("hevo_objects", templateBuilder.getHevoObjects(mapper, apIs));
        JsonNode sourceConfig = templateBuilder.getSourceConfig(mapper, rootNode).get("source_config");
        rootNode.set("source_config", sourceConfig);
        rootNode.set("test_connection", templateBuilder.getTestConnection(mapper, apIs));
        rootNode.set("frequency_execution_policy", templateBuilder.getFrequencyExecutionPolicy(mapper));
        rootNode.set("error_handling_rule", templateBuilder.getErrorHandlingRule(mapper));
        System.out.println(rootNode);
        FileWriter.writeToFile("campaign_monitor_connector.json", rootNode.toString());
    }
}
