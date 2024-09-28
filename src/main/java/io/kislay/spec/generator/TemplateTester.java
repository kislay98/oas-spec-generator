package io.kislay.spec.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kislay.spec.generator.neo.tests.*;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class TemplateTester {
    public static void main(String[] args) throws IOException {
        String templateString = new String(Files.readAllBytes(
                new File("campaign_monitor_connector.json").toPath()));
        ObjectMapper objectMapper = new ObjectMapper();
        Template template = objectMapper.readValue(templateString, Template.class);
        TestConnection testConnection = template.getTestConnection();
        if (Objects.isNull(testConnection)) {
            return;
        }
        String apiTag = testConnection.getApi();
        String dataRoot = testConnection.getDataRoot();
        SourceConfig sourceConfig = template.getSourceConfig();
        API testApi = null;
        for (API api : template.getApis()) {
            if (api.getTag().equals(apiTag)) {
                testApi = api;
                break;
            }
        }
        AuthConfig authConfig = template.getAuthConfig();
        Map<String, Object> sourceConfigInputs = getSourceConfigInputs(sourceConfig);
        MasterContext masterContext = MasterContext.builder().sourceConfig(sourceConfigInputs).oauthToken(null).paginationContext(new MasterContext.PaginationContext()).build();
        Map<String, String> authHeaders = authConfig.getAuthHeaders(masterContext);
        performRequest(testApi, authHeaders, masterContext);
    }

    private static Map<String, Object> getSourceConfigInputs(SourceConfig sourceConfig) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Map<String, Object> sourceConfigValues = new HashMap<>();
        for (ConfigDefinition configDefinition: sourceConfig.getConfigDefinitions()) {
            String defaultValue = configDefinition.getDefaultValue();
            System.out.println("Please enter the value for " + configDefinition.getConfigName() +
                    " (" + configDefinition.getDoc() + ")" +
                    (defaultValue != null ? " [Default: " + defaultValue + "]" : "") + ": ");
            String userInput = reader.readLine();
            if (userInput == null || userInput.trim().isEmpty()) {
                userInput = defaultValue;  // Use default if user input is empty
            }
            System.out.println("Value for " + configDefinition.getConfigName() + " is set to: " + userInput);
            sourceConfigValues.put(configDefinition.getConfigName(), userInput);
        }
        reader.close();
        return sourceConfigValues;
    }


    public static String sendRequest(String urlString, String method, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            // Initialize URL and Connection
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            // Adding headers to the request
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            makeLoadingText("Fetching the request to be fired");

            // Print the Request in a prettified manner
            System.out.println("=== Request ===");
            System.out.println("URL: " + urlString);
            System.out.println("Method: " + method);
            System.out.println("Headers:");
            headers.forEach((key, value) -> System.out.println("  " + key + ": " + value.substring(0, 6) + " ***"));
            System.out.println("===============\n");

            makeLoadingText("Making the API call");
            // Get Response Code and Response Data
            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream()));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();

            // Print the Response in a prettified manner
            System.out.println("=== Response ===");
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response Body:\n" + response.toString());
            System.out.println("================\n");
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("Test Connection Successful");
                System.out.println("================\n");
            } else {
                System.out.println("Test Connection Failed");
                System.out.println("================\n");
            }
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String performRequest(API api, Map<String, String> authHeaders, MasterContext context) {
        Map<String, String> headers = new HashMap<>(authHeaders);
        String method = api.getMethod();
        List<PathParam> params = api.getPathParams();
        String url = ParserUtils.parseString(api.getUrl(), params, context);
        return sendRequest(url, method, headers);
    }

    private static MultivaluedMap<String, String> parseQueryParams(List<QueryParam> queryParameters, MasterContext context) {
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        for (QueryParam parameter : queryParameters) {
            Object value = parameter.value(context);
            if (Objects.isNull(value)) {
                queryParams.put(parameter.getName(), Collections.singletonList("null"));
            } else {
                queryParams.put(parameter.getName(), Collections.singletonList(String.valueOf(value)));
            }
        }
        return queryParams;
    }

    private static void makeLoadingText(String message) {
        System.out.print(message + ": [");

        // Print dots with a delay to simulate loading
        for (int i = 0; i < 5; i++) { // Adjust the number for more/less dots
            try {
                Thread.sleep(500); // Delay in milliseconds (500 ms = 0.5 seconds)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print(".");
        }

        System.out.println("\nDone!"); // To indicate completion
    }
}