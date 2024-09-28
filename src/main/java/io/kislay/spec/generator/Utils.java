package io.kislay.spec.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kislay.spec.generator.document.TemplateBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Utils {

    public static String getMd5Hash(String url) {
        // Get MD5 MessageDigest instance
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        // Convert URL to bytes and update the MessageDigest
        byte[] hashInBytes = md.digest(url.getBytes(StandardCharsets.UTF_8));

        // Convert the bytes to hexadecimal format
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashInBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static Document getParsedHTML(String html) {
        // Parse the raw HTML string into a Document object
        return Jsoup.parse(html);
    }

    public static String escapeForJson(String htmlContent) {
        if (htmlContent == null) {
            return null;
        }
        return htmlContent
                .replace("\\", "\\\\")  // Escape backslashes
                .replace("\"", "\\\"")   // Escape double quotes
                .replace("\n", "\\n")    // Escape newlines
                .replace("\r", "\\r");   // Escape carriage returns
    }

    public static String cleanOpenAPISpec(String openapiSpecJson) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse the input string into a JsonNode
        try {
            JsonNode openapiSpec = objectMapper.readTree(openapiSpecJson);

            // Remove the "security" field if it exists
            if (openapiSpec.has("security")) {
                ((ObjectNode) openapiSpec).remove("security");
            }

            // Remove the "security" field inside "components"
            if (openapiSpec.has("components") && openapiSpec.get("components").has("security")) {
                ((ObjectNode) openapiSpec.get("components")).remove("security");
            }

            // Adjust the version number in "info"
            JsonNode infoNode = openapiSpec.get("info");
            if (infoNode != null && infoNode.has("version")) {
                String version = infoNode.get("version").asText();
                version = adjustVersion(version);
                ((ObjectNode) infoNode).put("version", version);
            }

            // Recursively delete null properties inside components -> schemas
            JsonNode schemasNode = openapiSpec.path("components").path("schemas");
            recursiveDeleteNoneProperties(schemasNode);

            // Convert angle brackets to curly braces
            convertAngleBracketsToCurlyBraces(openapiSpec);

            // Convert the modified JsonNode back to a string and return it
            System.out.println("[Success] OpenAPI 3.0 specification generated");
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(openapiSpec);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to adjust the version string
    private static String adjustVersion(String version) {
        // Ensure the version has 3 parts (e.g., "1.0.0")
        if (version.chars().filter(ch -> ch == '.').count() < 2) {
            version += ".0";
        }
        return version;
    }

    // Recursive method to delete null properties in a JSON node
    private static void recursiveDeleteNoneProperties(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = ((ObjectNode) node).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getValue().isNull()) {
                    fields.remove(); // Remove null fields
                } else {
                    recursiveDeleteNoneProperties(entry.getValue());
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                recursiveDeleteNoneProperties(arrayElement);
            }
        }
    }

    // Method to convert angle brackets to curly braces in the entire JSON
    private static void convertAngleBracketsToCurlyBraces(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = ((ObjectNode) node).fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode child = entry.getValue();
                if (child.isTextual()) {
                    // Replace angle brackets with curly braces in text values
                    String modifiedText = child.asText().replace("<", "{").replace(">", "}");
                    ((ObjectNode) node).put(entry.getKey(), modifiedText);
                } else {
                    // Recursively apply the same logic for nested structures
                    convertAngleBracketsToCurlyBraces(child);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                convertAngleBracketsToCurlyBraces(arrayElement);
            }
        }
    }

    /**
     * Retrieves and expands the request body schema from the resource.
     */
    public static JsonNode getRequestBodySchema(JsonNode openapiSpec, JsonNode resource) {
        JsonNode requestBodySchema = resource.path("requestBody")
                .path("content")
                .path("application/json")
                .path("schema");
        return expandRefs(openapiSpec, requestBodySchema);
    }

    /**
     * Retrieves and expands the response schema from the resource.
     */
    public static JsonNode getResponseSchema(JsonNode openapiSpec, JsonNode resource) {
        JsonNode responseSchema = resource.path("responses")
                .path("200")
                .path("content")
                .path("application/json")
                .path("schema");
        return expandRefs(openapiSpec, responseSchema);
    }

    public static JsonNode expandRefs(JsonNode yamlDict, JsonNode currentLevel) {
        return expandRefs(yamlDict, currentLevel, true);
    }

    private static JsonNode expandRefs(JsonNode yamlDict, JsonNode currentLevel, boolean isRoot) {
        if (isRoot && (currentLevel == null || currentLevel.isMissingNode())) {
            currentLevel = yamlDict;
        }

        if (currentLevel.isObject()) {
            ObjectNode newObject = JsonNodeFactory.instance.objectNode();
            Iterator<Map.Entry<String, JsonNode>> fields = currentLevel.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                if ("$ref".equals(key)) {
                    JsonNode refContent = traverseYamlDictRef(yamlDict, value.asText());
                    if (refContent != null) {
                        JsonNode expandedRef = expandRefs(yamlDict, refContent, false);
                        if (expandedRef.isObject()) {
                            newObject.setAll((ObjectNode) expandedRef);
                        } else {
                            return expandedRef;
                        }
                    }
                } else {
                    JsonNode expandedValue = expandRefs(yamlDict, value, false);
                    newObject.set(key, expandedValue);
                }
            }
            return newObject;
        } else if (currentLevel.isArray()) {
            ArrayNode newArray = JsonNodeFactory.instance.arrayNode();
            for (JsonNode item : currentLevel) {
                newArray.add(expandRefs(yamlDict, item, false));
            }
            return newArray;
        } else {
            return currentLevel;
        }
    }

    private static JsonNode traverseYamlDictRef(JsonNode yamlDict, String refPath) {
        if (refPath.startsWith("#/")) {
            String[] tokens = refPath.substring(2).split("/");
            JsonNode currentNode = yamlDict;
            for (String token : tokens) {
                // Unescape tokens according to JSON Pointer specification
                token = token.replace("~1", "/").replace("~0", "~");
                currentNode = currentNode.path(token);
                if (currentNode.isMissingNode()) {
                    return null;
                }
            }
            return currentNode;
        } else {
            // Handle external references if necessary
            System.err.println("External references are not supported in this implementation.");
            return null;
        }
    }

    public static String loadFileContent(String resourcePath) {
        try {
            // Get file from resources folder
            URL resource = TemplateBuilder.class.getResource(resourcePath);
            if (resource == null) {
                throw new IllegalArgumentException("File not found: " + resourcePath);
            }
            return new String(Files.readAllBytes(Paths.get(resource.toURI())));
        } catch (IOException | IllegalArgumentException | NullPointerException | URISyntaxException e) {
            throw new RuntimeException("Error opening file in" + resourcePath, e);
        }
    }

    public static ObjectNode createErrorHandlingObject(ObjectMapper mapper, String message, int errorCode, String messagePath, int pollTaskErrorCode) {
        ObjectNode errorObject = mapper.createObjectNode();
        if (message != null) {
            errorObject.put("message", message);
        }
        errorObject.put("error_code", errorCode);
        errorObject.put("message_path", messagePath);
        errorObject.put("poll_task_error_code", pollTaskErrorCode);
        return errorObject;
    }
}
