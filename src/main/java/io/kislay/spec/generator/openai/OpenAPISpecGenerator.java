package io.kislay.spec.generator.openai;

import static io.kislay.spec.generator.Utils.cleanOpenAPISpec;
import static io.kislay.spec.generator.openai.OpenAIPrompts.FETCH_PAGE_AUTH_INFO_SYSTEM;
import static io.kislay.spec.generator.openai.OpenAIPrompts.MODEL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kislay.spec.generator.FileWriter;
import io.kislay.spec.generator.Utils;
import java.io.IOException;

public class OpenAPISpecGenerator {

    public String generateOpenAPISpec(String markdown, String md5Hash, String userPrompt) {
        String content = getContent(userPrompt, markdown);
        FileWriter.writeToFile("content_" + md5Hash + ".txt", content);
        String systemPrompt = Utils.escapeForJson("""
                You’re an expert at writing OpenAPI 3.0 specifications.
                Your task is to produce an OpenAPI 3.0 specification from the documentation and a goal given by the user.

                Keep this in mind:
                - Include every optional argument and parameter in the specification.
                - Required version headers are very important. E.g the `Notion-Version` header should always be included as a header in the specification

                OUTPUT ONLY JSON!
                """);

        userPrompt = Utils.escapeForJson("My goal as a user, " + userPrompt + ". This is very important to me." +
                "# Here's the information you need:" + content + "\n" +
                "Take a deep breath, think step by step, and reason yourself to the correct answer." +
                "Write the OpenAPI 3.0 specification.");

        String message = String.format("""
        [
            {
                "role": "system",
                "content": "%s"
            },
            {
                "role": "user",
                "content": "%s"
            }
        ]
        """, systemPrompt, userPrompt);
        String response = "";
        String responseFormat = "{\"type\": \"json_object\"}";
        System.out.println("[Thinking] Trying to generate OpenAPI 3.0 specification...");
        response = new OpenAIProcessorClient().processText(message, responseFormat, MODEL, 0, 0);
        String spec = parseContent(response);
        return cleanOpenAPISpec(spec);
    }

    private String getContent(String userPrompt, String markdown) {
        System.out.println("[Thinking] Fetching the relevant information for markdown");
        String escapeMarkdown = Utils.escapeForJson(markdown);
        String message = String.format("""
        [
            {
                "role": "system",
                "content": "%s"
            },
            {
                "role": "user",
                "content": "My goal: %s. Extract relevant information from this documentation: %s"
            }
        ]
        """, FETCH_PAGE_AUTH_INFO_SYSTEM, userPrompt, escapeMarkdown);
        String response = "";
        response = new OpenAIProcessorClient().processText(message, null, MODEL, 0, 0.7f);
        return parseContent(response);
    }

    public static String parseContent(String response) {
        String content = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Parse the JSON string into a JsonNode object
            JsonNode rootNode = objectMapper.readTree(response);

            // Navigate to choices[0].message.content
            String contentText = rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            JsonNode contentNode = objectMapper.readTree(contentText);

            // Get the content as a string
            content = contentNode.toString();
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
        System.out.println("[Success] Got the relevant information!");
        return content;
    }

}
