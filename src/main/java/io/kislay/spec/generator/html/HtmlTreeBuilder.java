package io.kislay.spec.generator.html;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

public class HtmlTreeBuilder {

    // Define the excluded tags
    private static final Set<String> EXCLUDED_TAGS = new HashSet<>();

    static {
        EXCLUDED_TAGS.add("script");
        EXCLUDED_TAGS.add("style");
        // Add any other tags you want to exclude here
    }

    // Method to build the HTML tree
    public static String buildHtmlTree(Element element, int level) {
        StringBuilder ast = new StringBuilder();

        // Check if the element is not in the excluded tags
        element.tagName();
        if (!EXCLUDED_TAGS.contains(element.tagName())) {

            // Create a list to store the element's attributes
            List<String> attributes = new ArrayList<>();

            // Iterate over the element's attributes
            Attributes attrs = element.attributes();
            for (Attribute attr : attrs) {
                String key = attr.getKey();
                String value = attr.getValue();

                // Exclude "style" and "class" attributes
                if (!key.equals("style") && !key.equals("class")) {

                    // If the value is a simple string, just add it to the list
                    if (!value.contains(" ")) {
                        attributes.add(key + "=\"" + value + "\"");
                    } else {
                        // If the value is a space-separated string, filter based on exclusion rules
                        String[] values = value.split(" ");
                        List<String> filteredValues = new ArrayList<>();
                        for (String val : values) {
                            if (!val.startsWith("css-") && !val.startsWith("js-") && !val.startsWith("r-")) {
                                long digitCount = val.chars().filter(Character::isDigit).count();
                                if (digitCount <= 2) {
                                    filteredValues.add(val);
                                }
                            }
                        }
                        if (!filteredValues.isEmpty()) {
                            attributes.add(key + "=\"" + String.join(" ", filteredValues) + "\"");
                        }
                    }
                }
            }

            // Join the attributes into a string
            String attributeStr = String.join(" ", attributes).trim();
            ast.append("<").append(element.tagName());
            if (!attributeStr.isEmpty()) {
                ast.append(" ").append(attributeStr);
            }
            ast.append(">");

            // Recursively build the HTML tree for child elements
            for (Element child : element.children()) {
                if (!EXCLUDED_TAGS.contains(child.tagName())) {
                    ast.append(buildHtmlTree(child, level + 1).trim());
                }
            }

            // Close the tag
            ast.append("</").append(element.tagName()).append(">");
        }

        return ast.toString();
    }
}
