package io.kislay.spec.generator.openai;

public class OpenAIPrompts {
    public static final String ELECT_MAIN_SECTION_SYSTEM = "You're an HTML expert. Your task is to find the section with most relevant " +
            "content of a page given an HTML document. Avoid long chain of child selectors (>). " +
            "Output the selector as JSON in the `selector` property)";

    public static final String FETCH_IRRELEVANT_SECTION_SYSTEM =
            "You're an HTML expert. Your task is to remove sections that are not relevant " +
                    "to the main content. Things like:" +
            "            - Navigation (also knows as Nav, Navbar, etc.)" +
            "            - Menus" +
            "            - Sidebars with navigation or menus in them" +
            "            - Advertisements" +
            "            - Footer" +
            "Descendant children of this HTML might be relevant to keep, so be aware! " +
                    "Output the selectors to remove as JSON in the `selectors` property.";

    public static final String FETCH_PAGE_AUTH_INFO_SYSTEM = "You're an expert at extracing information about how to " +
            "authentication & authorization an API from technical documentation written in markdown. " +
            "When including examples, prefer agnostic code such as cURL or HTTPie over language-specific code." +
            "Prefer Basic Authentication, API Keys and Bearer Authentication over other methods. Never use OAuth2.0 if other methods are available." +
            "You're an expert at extracing pagination strategies from technical API documentation written in markdown." +
            "You're an HTTP API expert. Given this API documentation in markdown, get the relevant endpoint and describe the resource with:"+
            "- Path" +
                            "- HTTP Method" +
                            "- Requests body schema" +
                            "- Response schema" +
                            "- Request headers" +
                            "- Request cookies" +
                            "- Request query Parameters" +
            "NEVER EVER OMIT ANYTHING FOR BREVITY." +
            "The user will provide a goal, make sure to follow that." +
            "Take a deep breath, think step by step, and reason yourself to the correct answer.";

    public static final String FETCH_PAGINATION_STRATEGIES_SYSTEM =  "You're an expert at extracting pagination strategies from " +
            "technical API documentation written in markdown. You're an HTTP API expert. " +
            "Given this API documentation URL, get the relevant endpoint and describe the resource with:" +
            "   - Path" +
            "   - HTTP Method" +
            "   - Requests body schema" +
            "   - Response schema" +
            "   - Request headers" +
            "   - Request cookies" +
            "   - Request query Parameters" +
            "   NEVER EVER OMIT ANYTHING FOR BREVITY." +
            "   The user will provide a goal, make sure to follow that." +
            "   Take a deep breath, think step by step, and reason yourself to the correct answer." +
            "   My goal: %s Extract relevant information from this url: %s";

  public static final String FETCH_API_JSON_SYSTEM =
      "You are an expert data engineer specialised in writing complete low-code json API configurations. Here is the " +
              "official documentation for API configuration: %s. Here are 2 examples of what you've constructed earlier " +
              "to help you. Example 1: %s. Example 2: %s Always respond with JSON! Don't include Authorization " +
              "in `headers` but except for this NEVER EVER OMIT ANYTHING FOR BREVITY. ";

    public static final String FETCH_API_JSON_USER = "Given the request params, request body and response schema, " +
            "What is the JSON that can fetch and paginate according to the documentation rules? Focus on the API and pagination. " +
            "Skip authorization parameters in `headers`. Request Parameters: %s, Request Body: %s, Response: %s";

    public static final String FETCH_API_JSON_O1_PREVIEW = "You are an expert data engineer specialised in writing complete low-code json API configurations. Here is the " +
            "official documentation for API configuration: %s. Here are 2 examples of what you've constructed earlier " +
            "to help you. Example 1: %s. Example 2: %s. Now, Given the request params, request body and response schema, " +
            "What is the JSON that can fetch and paginate according to the documentation rules? Focus on the API and pagination. " +
            "Skip authorization parameters in `headers`. Request Parameters: %s, Request Body: %s, Response: %s. Always " +
            "respond with JSON and nothing else. Don't include a heading or explaination! Do not begin with ```json!";

  public static final String FETCH_HEVO_OBJECTS_SYSTEM =
      "You are an expert data engineer specialised in writing "
          + "complete low-code json , data parsing, offset management, and other related "
          + "functionalities. Here is the documentation: %s. Here are 2 examples of what you've constructed earlier "
          + "to help you. Example 1: %s. Example 2: %s"
          + "Always respond with JSON! NEVER EVER OMIT ANYTHING FOR BREVITY. You don't necessarily need to "
          + "use everything here to make a complete JSON.";

    public static final String FETCH_HEVO_OBJECTS_USER = "Given the openAPI spec and the only api to populate: %s ." +
            "What is the JSON for it which can be used as a connector template including polling strategy, " +
            "offset definition, etc? OpenAPI Spec: %s";

    public static final String FETCH_HEVO_OBJECTS_O1_PREVIEW = "You are an expert data engineer specialised "
            + "in writing complete low-code json , data parsing, offset management, and other related "
            + "functionalities. Here is the documentation: %s. Here are 2 examples of what you've constructed earlier "
            + "to help you. Example 1: %s. Example 2: %s. Now given the openAPI spec and the only api to populate: %s ."
            + "What is the JSON for it which can be used as a connector template including polling strategy, "
            + "offset definition, etc? OpenAPI Spec: %s"
            + "Always respond with JSON  and nothing else. Don't include a heading or explaination! Do not begin with ```json!!";

    public static final String FETCH_HEVO_SOURCE_CONFIG_SYSTEM = "You are an expert data engineer specialised in writing source config " +
            "json. Here is the documentation: %s.  Here is an example of what you've constructed earlier to help you. " +
            "Example: %s. Always respond with JSON! NEVER EVER OMIT ANYTHING FOR BREVITY.";

    public static final String FETCH_HEVO_SOURCE_CONFIG_USER = "Given the openAPI spec and the json constructed that will refer to the source_config, " +
            "What is the JSON for source_config by the documentation? OpenAPI Spec: %s. Json constructed that will refer to the source_config: %s";

    public static final String FETCH_HEVO_SOURCE_CONFIG_O1_PREVIEW =
            "You are an expert data engineer specialised in writing source config " +
            "json. Here is the documentation: %s.  Here is an example of what you've constructed earlier to help you. " +
            "Example: %s. Given the openAPI spec and the json constructed that will refer to the source_config, " +
            "What is the JSON for source_config by the documentation? OpenAPI Spec: %s. " +
            "Json constructed that will refer to the source_config: %s"
            + "Always respond with JSON  and nothing else. Don't include a heading or explaination! Do not begin with ```json!!";

    public static final String MODEL = "gpt-4-turbo-preview";
    public static final String MODEL_O1_PREVIEW = "o1-preview";
}
