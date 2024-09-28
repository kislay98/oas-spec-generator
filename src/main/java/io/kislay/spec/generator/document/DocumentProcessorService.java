package io.kislay.spec.generator.document;


import io.kislay.spec.generator.FileWriter;
import io.kislay.spec.generator.html.HTMLParser;
import io.kislay.spec.generator.record.InputData;
import io.kislay.spec.generator.Utils;
import io.kislay.spec.generator.openai.OpenAPISpecGenerator;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.nodes.Element;

public class DocumentProcessorService {

    private final OkHttpClient httpClient;

    public DocumentProcessorService() {
        this.httpClient = new OkHttpClient();
    }

    public String process(InputData inputData) {
        Map<String, String> htmlContents = new HashMap<>();
        String md5Hash = Utils.getMd5Hash(String.join(";", inputData.urls()));
        inputData.urls().forEach(url -> htmlContents.put(url, fetchHtmlContent(url)));
        HTMLParser parser = new HTMLParser();
        OpenAPISpecGenerator specGenerator = new OpenAPISpecGenerator();
        Map<String, Element> refinedHtmlContents = parser.refineHtmlContents(htmlContents);
        String combinedRelevantHTMLs = refinedHtmlContents.values().stream().map(Element::outerHtml).collect(Collectors.joining("\n"));
        String markdown = parser.preprocessToMarkdown(combinedRelevantHTMLs);
        FileWriter.writeToFile(md5Hash + ".html", combinedRelevantHTMLs);
        FileWriter.writeToFile( "markdown_" + md5Hash + ".md", markdown);
        String openAPISpecs = specGenerator.generateOpenAPISpec(markdown, md5Hash, inputData.prompt());
        FileWriter.writeToFile("openAPISpecs_" + md5Hash + ".json", openAPISpecs);
        System.out.println("OpenAPISpecs spec file successfully loaded with postfix: " + md5Hash +
                "Use it to generate any type of connector!");
//        try {
//            TemplateBuilder templateBuilder = new TemplateBuilder(openAPISpecs);
//        } catch (IOException e) {
//            throw new RuntimeException("Could not process openAPISpecs as a json", e);
//        }
        return openAPISpecs;
    }

    private String fetchHtmlContent(String url) {
        System.out.println("Fetching html content of " + url);
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch URL: " + url + ", response code: " + response.code());
            }
            System.out.println("[Success] fetched HTML content of " + url);
            assert response.body() != null;
            return response.body().string(); // Store the HTML content in memory
        } catch (IOException e) {
            throw new RuntimeException("Error fetching URL: " + url, e);
        }
    }
}
