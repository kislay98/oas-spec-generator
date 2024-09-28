package io.kislay.spec.generator.neo.tests;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AuthConfig {
    private String authType;
    private String headerName;
    private String bearerToken;
    private String username;
    private String password;

    public Map<String, String> getAuthHeaders(MasterContext context) {
        Map<String, String> authHeaders = new HashMap<>();
        if ("BEARER_TOKEN".equalsIgnoreCase(authType) || "API_TOKEN".equalsIgnoreCase(authType)) {
            String user = ParserUtils.extractConfig(bearerToken, context);
            return generateBasicAuthHeaders(user, "");
//            Map<String, String> headers = new LinkedHashMap<>();
//            headers.put(headerName, bearerTokenValue);
//            return headers;
        } else if ("BASIC_AUTH".equalsIgnoreCase(authType)) {
            String userNameInput = ParserUtils.extractConfig(username, context);
            String passwordInput = ParserUtils.extractConfig(password, context);
            return generateBasicAuthHeaders(userNameInput, passwordInput);
        }
        return authHeaders;
    }

    public static Map<String, String> generateBasicAuthHeaders(String username, String password) {
        String authHeader =
                "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", authHeader);
        return header;
    }
}
