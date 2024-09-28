package io.kislay.spec.generator.openai;

import io.kislay.spec.generator.record.InputText;
import java.io.IOException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Path("/openai")
@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
public class OpenAIProcessorResource {

    public static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient();

    @POST
    @Path("/process")
    public javax.ws.rs.core.Response processText(InputText inputText) {
        try {
            String jsonResponse = null;
            if (inputText.url() != null) {
                jsonResponse = fetchPaginationInfo(inputText.url());
            } else {
                jsonResponse = sendRequestToOpenAI(inputText.prompt());
            }
            return javax.ws.rs.core.Response.ok(jsonResponse).build();
        } catch (IOException e) {
            return javax.ws.rs.core.Response
                    .status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }

    private String fetchPaginationInfo(String url) throws IOException {
        String promt = String.format(OpenAIPrompts.FETCH_PAGINATION_STRATEGIES_SYSTEM, "get pagination info", url);
        return sendRequestToOpenAI(promt);
    }

    private String sendRequestToOpenAI(String inputText) throws IOException {
        // Construct the JSON payload
        String json = String.format(
                "{\"model\": \"gpt-4o-mini-2024-07-18\", \"messages\": [" +
                        "{\"role\": \"user\", \"content\": \"%s\"}]}",
                inputText
        );

        // Create the request body
        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json")
        );

        // Build the request
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + "<key>")
                .addHeader("Content-Type", "application/json")
                .build();

        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            return response.body().string();
        }
    }
}
