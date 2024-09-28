package io.kislay.spec.generator.html;

import io.kislay.spec.generator.Utils;
import static io.kislay.spec.generator.Utils.escapeForJson;
import static io.kislay.spec.generator.openai.OpenAIPrompts.ELECT_MAIN_SECTION_SYSTEM;
import static io.kislay.spec.generator.openai.OpenAIPrompts.FETCH_IRRELEVANT_SECTION_SYSTEM;
import static io.kislay.spec.generator.openai.OpenAIPrompts.MODEL;
import static io.kislay.spec.generator.openai.OpenAPISpecGenerator.parseContent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import io.kislay.spec.generator.openai.OpenAIProcessorClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLParser {

  public Map<String, Element> refineHtmlContents(Map<String, String> htmlContents) {
    // Placeholder for processing each HTML content
    Map<String, Element> refinedHTMLs = new HashMap<>();
    for (Map.Entry<String, String> htmlComtentEntry : htmlContents.entrySet()) {
      String url = htmlComtentEntry.getKey();
      String htmlContent = htmlComtentEntry.getValue();
      refinedHTMLs.put(url, refineHtml(url, htmlContent));
    }
    System.out.println("[Success] All urls refined");
    return refinedHTMLs;
  }

  private Element refineHtml(String url, String htmlContent) {
    System.out.println("Refining HTML for: " + url);
    Document parsedHTML = Utils.getParsedHTML(htmlContent);
    String htmlTree = HtmlTreeBuilder.buildHtmlTree(parsedHTML.body(), 0);

    String mainSelector = findMainSectionSelector(htmlTree);
    Element htmlMainContent = parsedHTML.selectFirst(mainSelector);
    if (htmlMainContent == null) {
      throw new RuntimeException("Could not find main content in " + htmlTree);
    }
    String mainContentTree = HtmlTreeBuilder.buildHtmlTree(htmlMainContent, 0);
    // Remove irrelevant sections like navbars, sidebars, advertisements
    List<String> irrelevantSections = getIrrelevantSections(mainContentTree);
    return removeIrrelevantSections(htmlMainContent, irrelevantSections);
  }

  // Method to remove irrelevant sections from the provided HTML element
  public static Element removeIrrelevantSections(Element html, List<String> selectors) {
    for (String selector : selectors) {
      // Select elements based on the CSS selector
      Elements selectedElements = html.select(selector);
      for (Element element : selectedElements) {
        element.remove();
      }
    }
    System.out.println("[Success]Refined HTML");
    return html;
  }

  private List<String> getIrrelevantSections(String htmlContent) {
    String escapedHtmlContent = escapeForJson(htmlContent);
    String messages =
        String.format(
            """
            [{
                        "role": "system",
                        "content": "%s"
             },
             {
                        "role": "user",
                        "content": "Find the section to remove of this page: %s"
              }
           ]
           """,
                FETCH_IRRELEVANT_SECTION_SYSTEM, escapedHtmlContent);
    String content = parseSelectorFromString(messages);
    ObjectMapper objectMapper = new ObjectMapper();
    List<String> selectorNodes = new ArrayList<>();
      try {
        JsonNode selectors = objectMapper.readTree(content).path("selectors");
        selectors.forEach(selector -> {
          String irrelevantSelector = selector.toString().replace("\"", "");
          selectorNodes.add(irrelevantSelector);
          });
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
      return selectorNodes;
  }

  private String findMainSectionSelector(String htmlContent) {
    String escapedHtmlContent = escapeForJson(htmlContent);
    String messages =
    String.format(
      """
      [
          {
              "role": "system",
              "content": "%s"
          },
          {
              "role": "user",
              "content": "Find the main section of this page: %s"
          }
      ]
      """, ELECT_MAIN_SECTION_SYSTEM, escapedHtmlContent);
    String content = parseSelectorFromString(messages);
    ObjectMapper objectMapper = new ObjectMapper();
      try {
          content = objectMapper.readTree(content).path("selector").asText();
          if (content.startsWith("main")) {
            content = content.substring(4);  // Remove the first 4 characters (length of "main")
            content = content.trim();
          }
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    return content;
  }

  private String parseSelectorFromString(String messages) {
    String responseFormat = "{\"type\": \"json_object\"}";
    String response = null;
      response = new OpenAIProcessorClient().processText(messages, responseFormat, MODEL, 0, 0.7f);
      return parseContent(response);
  }

  public String preprocessToMarkdown(String htmlContent) {
    FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
    return converter.convert(htmlContent);
  }

  // Additional utility methods can be added as needed
}
