package org.craftercms.core.util.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * Custom Jackson serializer for {@link org.dom4j.Document}.
 *
 * <p/>
 * The following are the conversion patterns used between XML and JSON:
 * <p/>
 * XML                              JSON                                            Access
 * ---                              ----                                            ------
 * <e/>                             "e": null                                       o.e
 * <e>text</e>                      "e": "text"                                     o.e
 * <e name="value" />               "e": { "name": "value" }                        o.e["name"]
 * <e name="value">text</e>         "e": { "name": "value", "text": "text" }        o.e["name"] o.e["text"]
 * <e><a>text</a><b>text</b></e>    "e": { "a": "text", "b": "text" }               o.e.a o.e.b
 * <e><a>text</a><a>text</a></e>	"e": { "a": ["text", "text"] }                  o.e.a[0] o.e.a[1]
 * <e>text<a>text</a></e>           "e": { "text": "text", "a": "text" }            o.e["text"] o.e.a
 * <e>text<a>text</a>text</e>       "e": { "text": ["text", "text"], "a": "text" }  o.e["text"][0] o.e["text"][1]
 * o.e.a
 * <p/>
 * <b>IMPORTANT:</b> XML Namespaces are ALWAYS ignored.
 *
 * @author avasquez
 */
public class Dom4jDocumentJsonSerializer extends JsonSerializer<Document> {

    public static final String TEXT_JSON_KEY = "text";

    @Override
    public void serialize(Document doc, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeFieldName(doc.getRootElement().getName());

        elementToJson(doc.getRootElement(), jsonGenerator);

        jsonGenerator.writeEndObject();
    }

    @Override
    public Class<Document> handledType() {
        return Document.class;
    }

    private void elementToJson(Element element, JsonGenerator jsonGenerator) throws IOException {
        boolean objectStarted = false;

        if (element.attributeCount() > 0) {
            List<Attribute> attributes = element.attributes();

            jsonGenerator.writeStartObject();

            objectStarted = true;

            for (Attribute attribute : attributes) {
                jsonGenerator.writeStringField(attribute.getName(), attribute.getValue());
            }
        }

        if (!element.hasContent()) {
            if (!objectStarted) {
                jsonGenerator.writeNull();
            }
        } else if (element.isTextOnly()) {
            if (!objectStarted) {
                jsonGenerator.writeString(element.getText());
            } else {
                jsonGenerator.writeStringField(TEXT_JSON_KEY, element.getText());
            }
        } else {
            if (!objectStarted) {
                jsonGenerator.writeStartObject();

                objectStarted = true;
            }

            if (element.hasMixedContent()) {
                List<String> textContent = getTextContentFromMixedContent(element);

                if (textContent.size() > 1) {
                    jsonGenerator.writeArrayFieldStart(TEXT_JSON_KEY);

                    for (String text : textContent) {
                        jsonGenerator.writeString(text);
                    }

                    jsonGenerator.writeEndArray();
                } else if (textContent.size() == 1) {
                    jsonGenerator.writeStringField(TEXT_JSON_KEY, textContent.get(0));
                }
            }

            Map<String, List<Element>> children = getChildren(element);
            for (Map.Entry<String, List<Element>> entry : children.entrySet()) {
                if (entry.getValue().size() > 1) {
                    jsonGenerator.writeArrayFieldStart(entry.getKey());

                    for (Element child : entry.getValue()) {
                        elementToJson(child, jsonGenerator);
                    }

                    jsonGenerator.writeEndArray();
                } else {
                    jsonGenerator.writeFieldName(entry.getKey());

                    elementToJson(entry.getValue().get(0), jsonGenerator);
                }
            }
        }

        if (objectStarted) {
            jsonGenerator.writeEndObject();
        }
    }

    private List<String> getTextContentFromMixedContent(Element element) {
        List<Node> content = element.content();
        List<String> textContent = new ArrayList<String>();

        for (Node node : content) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getText();
                if (StringUtils.isNotBlank(text)) {
                    textContent.add(text);
                }
            }
        }

        return textContent;
    }

    private Map<String, List<Element>> getChildren(Element element) {
        Map<String, List<Element>> groupedChildren = new LinkedHashMap<>();
        List<Element> children = element.elements();

        for (Element child : children) {
            if (groupedChildren.containsKey(child.getName())) {
                groupedChildren.get(child.getName()).add(child);
            } else {
                List<Element> elements = new ArrayList<>();
                elements.add(child);

                groupedChildren.put(child.getName(), elements);
            }
        }

        return groupedChildren;
    }

}
