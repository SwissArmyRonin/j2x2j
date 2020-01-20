package j2x2j;

import static j2x2j.JsonToXml.jsonToXml;
import static j2x2j.XmlToJson.xmlToJson;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class ConversionTest {
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	@Test
	public void testAntFile() {
		isIdempotent("/example.xml");
	}

	@Test
	public void testSmallFile() {
		isIdempotent("/small.xml");
	}

	private void isIdempotent(String fileName) {
		Document doc = loadXml(fileName);

		JsonNode json = xmlToJson().convert(doc);
		Document xml = jsonToXml().convert(json);
		JsonNode json2 = xmlToJson().convert(xml);

		assertEquals(json, json2);
	}

	@SneakyThrows
	@Test
	public void testJsonToXml() {
		JsonNode json = loadJson("/command.json");
		Document xml = jsonToXml().convert(json);
//		printDocument(xml, System.out);
		JsonNode json2 = xmlToJson().convert(xml);
		assertEquals(json, json2);
	}

	@SneakyThrows
	private Document loadXml(String fileName) {
		InputStream is = ConversionTest.class.getResourceAsStream(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse(is);
	}

	@SneakyThrows
	private JsonNode loadJson(String fileName) {
		InputStream is = ConversionTest.class.getResourceAsStream(fileName);
		return new ObjectMapper().readTree(is);
	}
}
