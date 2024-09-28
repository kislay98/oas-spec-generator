package io.kislay.spec.generator.openai;

import static io.kislay.spec.generator.openai.OpenAIProcessorResource.OPENAI_API_URL;

import static io.kislay.spec.generator.openai.OpenAIPrompts.MODEL;
import static io.kislay.spec.generator.openai.OpenAIPrompts.MODEL_O1_PREVIEW;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class OpenAIProcessorClient {

    private static OkHttpClient httpClient;

    public OpenAIProcessorClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Timeout to establish the connection
                .writeTimeout(60, TimeUnit.SECONDS)   // Timeout to send the request
                .readTimeout(240, TimeUnit.SECONDS)    // Timeout to receive the response
                .callTimeout(300, TimeUnit.SECONDS)   // Overall timeout for the call
                .build();
    }

    public static void main(String[] args) {
        new OpenAIProcessorClient().processText("[\n" +
                "    {\n" +
                "        \"role\": \"system\",\n" +
                "        \"content\": \"You’re an expert at writing OpenAPI 3.0 specifications.\\nYour task is to produce an OpenAPI 3.0 specification from the documentation and a goal given by the user.\\n\\nKeep this in mind:\\n- Include every optional argument and parameter in the specification.\\n- Required version headers are very important. E.g the `Notion-Version` header should always be included as a header in the specification\\n\\nOUTPUT ONLY JSON!\\n\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"role\": \"user\",\n" +
                "        \"content\": \"My goal as a user, get all tickets fully. This is very important to me.# Here's the information you need:To achieve your goal of getting all tickets fully from the Zendesk Support API, you'll need to interact with the appropriate endpoint. However, the provided documentation does not directly specify the endpoint for retrieving all tickets. Typically, for such APIs, a common endpoint pattern is something like `/api/v2/tickets.json` for listing resources like tickets.\\n\\nGiven this assumption and based on standard practices in RESTful API design and Zendesk's conventions as hinted in the idempotency example provided, here's a hypothetical approach to retrieve all tickets:\\n\\n### Endpoint and Resource Description\\n\\n- **Path**: `https://{subdomain}.zendesk.com/api/v2/tickets.json`\\n- **HTTP Method**: `GET`\\n\\nThis endpoint is presumed to list all tickets available in your Zendesk Support account.\\n\\n### Request Body Schema\\n\\nFor a `GET` request to retrieve all tickets, typically no request body is required.\\n\\n### Response Schema\\n\\nThe response schema is not explicitly detailed in your documentation snippet. However, based on common patterns in REST APIs and the partial example given for ticket creation, a successful response might look something like this:\\n\\n```json\\n{\\n  \\\"tickets\\\": [\\n    {\\n      \\\"url\\\": \\\"https://{subdomain}.zendesk.com/api/v2/tickets/{id}.json\\\",\\n      \\\"id\\\": {ticket_id},\\n      \\\"subject\\\": \\\"{ticket_subject}\\\",\\n      ...\\n    }\\n  ],\\n  \\\"next_page\\\": \\\"{url_to_next_page_of_results}\\\",\\n  \\\"previous_page\\\": \\\"{url_to_previous_page_of_results}\\\",\\n  \\\"count\\\": {total_number_of_tickets}\\n}\\n```\\n\\nThis schema suggests that each ticket object contains details about individual tickets. The response also includes pagination links (`next_page`, `previous_page`) and a count of total tickets available.\\n\\n### Request Headers\\n\\nBased on the idempotency key example provided:\\n\\n- **Content-Type**: `application/json` (Generally required for POST requests; not typically needed for GET requests)\\n- **Authorization**: Basic Auth or other methods as specified under security and authentication section of the general API introduction.\\n  \\nExample:\\n```plaintext\\nAuthorization: Basic {base64_encoded_credentials}\\n```\\n\\nOr if using Bearer Token:\\n```plaintext\\nAuthorization: Bearer {your_oauth_token}\\n```\\n\\n### Request Cookies\\n\\nThe documentation does not specify using cookies for requests.\\n\\n### Request Query Parameters\\n\\nWhile specific query parameters are not detailed in your snippet, common parameters include:\\n\\n- **page**: For specifying which page of results to retrieve.\\n- **per_page**: For specifying how many results per page should be returned.\\n\\nGiven that pagination is mentioned under general mechanisms and conventions, it's likely you can use these or similar parameters to navigate through large sets of tickets.\\n\\nRemember that actual implementation details may vary slightly based on Zendesk's specific API setup. Always refer directly to their official documentation or imported Postman collection for precise details.\\nTake a deep breath, think step by step, and reason yourself to the correct answer.Write the OpenAPI 3.0 specification.\"\n" +
                "    }\n" +
                "]\n", "{\"type\": \"json_object\"}", MODEL, 0, 0);
    }

    public String processText(String message, String responseFormat, String model, int temperature,
                              float frequencyPenalty) {
        // Construct the JSON payload
        String json = null;
        if (model.equals(MODEL_O1_PREVIEW)) {
            json = constructJsonPayloadForO1(message, model);
        } else if (responseFormat == null) {
            json = constructJsonPayloadWithoutResponseFormat(message, model, temperature, frequencyPenalty);
        } else {
            json = constructJsonPayload(message, responseFormat, model, temperature, frequencyPenalty);
        }

        // Create the request body
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Build the request
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + "<openapi_key>")
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request and get the response
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected code " + response);
            }
            assert response.body() != null;
            String responseBody = streamResponseBody(response.body().byteStream());
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String streamResponseBody(InputStream inputStream) {
        StringBuilder responseBody = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return responseBody.toString(); // Return the complete streamed response as a string
    }

    private String constructJsonPayload(String messages, String responseFormat, String model, int temperature,
                                        float frequencyPenalty) {
        // Constructs the JSON payload based on whether the URL or prompt is provided
        return String.format("{\"messages\":%s," +
                        "\"model\": \"%s\"," +
                        "\"response_format\": %s," +
                        "\"temperature\": %s," +
                        "\"frequency_penalty\": %s}",
                messages, model, responseFormat, temperature, frequencyPenalty);
    }

    private String constructJsonPayloadWithoutResponseFormat(String messages, String model, int temperature,
                                                             float frequencyPenalty) {
        // Constructs the JSON payload based on whether the URL or prompt is provided
        return String.format("{\"messages\":%s," +
                        "\"model\": \"%s\"," +
                        "\"temperature\": %s," +
                        "\"frequency_penalty\": %s}",
                messages, model, temperature, frequencyPenalty);
    }

    private String constructJsonPayloadForO1(String message, String model) {
        return String.format("{\"messages\":%s,\"model\": \"%s\"}", message, model);
    }
}

