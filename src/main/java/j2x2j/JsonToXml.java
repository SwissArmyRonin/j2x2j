package j2x2j;

import static j2x2j.Constants.ATTR_PREFIX;
import static j2x2j.Constants.TEXT_NODE;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.AllArgsConstructor;

/**
 * Simple converter that can transform a {@link JsonNode} to a {@link Document}.
 * <p>
 * The process does not guarantee the order of elements completely, but elements
 * with the same name are guaranteed to preserve their ordering in regards to
 * each other.
 * <p>
 */
@AllArgsConstructor(staticName = "with")
public class JsonToXml {
	/**
	 * Create a converter with the default {@link DocumentBuilder} (using
	 * {@link DocumentBuilderFactory#newInstance()})
	 */
	public static JsonToXml jsonToXml() {
		return new JsonToXml();
	}

	private final DocumentBuilder docBuilder;

	private JsonToXml() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private void appendElement(Document document, Element parent, String key, JsonNode value) {
		Element element = document.createElement(key);
		parent.appendChild(element);
		appendNodes(document, element, value);
	}

	private void appendNodes(Document document, Element parent, JsonNode jsonNode) {
		if (jsonNode.isTextual()) {
			parent.setTextContent(jsonNode.asText());
			return;
		}

		Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
		while (fields.hasNext()) {
			Entry<String, JsonNode> e = fields.next();

			if (e.getKey().equals(TEXT_NODE)) {
				appendText(document, parent, e.getValue());
			} else if (e.getKey().startsWith(ATTR_PREFIX)) {
				Attr attr = document.createAttribute(e.getKey().substring(ATTR_PREFIX.length()));
				attr.setValue(e.getValue().asText());
				parent.setAttributeNode(attr);
			} else {
				if (e.getValue().isArray()) {
					for (JsonNode o : (ArrayNode) e.getValue()) {
						appendElement(document, parent, e.getKey(), o);
					}
				} else {
					appendElement(document, parent, e.getKey(), e.getValue());
				}
			}
		}
	}

	private void appendText(Document document, Element parent, JsonNode o) {
		Text element = document.createTextNode(o.asText());
		parent.appendChild(element);
	}

	/** Convert an JSON Document to XML. */
	public Document convert(JsonNode json) {
		String rootElementName = json.fieldNames().next();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(rootElementName);
		doc.appendChild(rootElement);

		appendNodes(doc, rootElement, json.get(rootElementName));

		return doc;
	}

}
