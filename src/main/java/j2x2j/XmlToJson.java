package j2x2j;

import static j2x2j.Constants.ATTR_PREFIX;
import static j2x2j.Constants.TEXT_NODE;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import lombok.AllArgsConstructor;

/**
 * Simple converter that can transform a {@link Document} to a {@link JsonNode}.
 * <p>
 * The process does not guarantee the order of elements completely, but elements
 * with the same name are guaranteed to preserve their ordering in regards to
 * each other.
 * <p>
 */
@AllArgsConstructor(staticName = "with")
public class XmlToJson {
	/**
	 * Create a converter with the default {@link JsonNodeFactory}
	 * ({@link JsonNodeFactory#instance}).
	 */
	public static XmlToJson xmlToJson() {
		return new XmlToJson();
	}

	private final JsonNodeFactory instance;

	private XmlToJson() {
		this.instance = JsonNodeFactory.instance;
	}

	/** Convert an XML Document to JSON. */
	public JsonNode convert(Document doc) {
		doc.getDocumentElement().normalize();
		Element element = doc.getDocumentElement();
		ObjectNode node = new ObjectNode(instance);
		node.set(element.getTagName(), xmlElementToJsonNode(element));
		return node;
	}

	private JsonNode xmlElementToJsonNode(Element node) {
		// if (node.getNodeType() == Node.TEXT_NODE) {
		// Text text = (Text) node;
		// String textContent = text.getWholeText().trim();
		// return new TextNode(textContent);
		// }
		//

		ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Attr item = (Attr) attributes.item(i);
			objectNode.put(ATTR_PREFIX + item.getName(), item.getValue());
		}
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			short nodeType = item.getNodeType();
			switch (nodeType) {
			case Node.ELEMENT_NODE:
				Element element = (Element) item;
				String fieldName = element.getTagName();
				JsonNode xmlElementToJsonNode = xmlElementToJsonNode(element);

				if (objectNode.has(fieldName)) {
					JsonNode existing = objectNode.get(fieldName);
					if (existing.isArray()) {
						((ArrayNode) existing).add(xmlElementToJsonNode);
					} else {
						ArrayNode array = objectNode.putArray(fieldName);
						array.add(existing);
						array.add(xmlElementToJsonNode);
					}
				} else {
					objectNode.set(fieldName, xmlElementToJsonNode);
				}
				break;
			case Node.TEXT_NODE:
				Text text = (Text) item;
				String textContent = text.getWholeText().trim();
				if (textContent.length() == 0)
					continue;

				if (objectNode.size() > 0) {
					if (objectNode.has(TEXT_NODE)) {
						continue;
					} else {
						objectNode.put(TEXT_NODE, textContent);
					}
					break;
				}

				return new TextNode(textContent);
			default:
			}
		}
		return objectNode;
	}
}
