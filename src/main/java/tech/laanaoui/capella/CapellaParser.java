package tech.laanaoui.capella;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class CapellaParser {

    public static void main(String[] args) {
        // Example usage
        String filePath = "Communication.capella"; // Change to the actual file path
        JSONObject result = parseCapellaFileToJson(filePath);
        System.out.println(result.toString(2));
    }

    /**
     * Parse the Capella file and return JSON structure.
     * Parent object is "ownedModelRoots". We skip the usual XML tags.
     */
    public static JSONObject parseCapellaFileToJson(String filePath) {
        JSONObject result = new JSONObject();
        try {
            File xmlFile = new File(filePath);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Find the 'ownedModelRoots' node
            NodeList nodeList = doc.getElementsByTagName("ownedModelRoots");
            if (nodeList.getLength() > 0) {
                // Build JSON from first 'ownedModelRoots' node
                result = buildJsonFromNode(nodeList.item(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Build JSON by:
     *  - "attributes" object for the tag's attributes.
     *  - each child stored under the child's tag name.
     *  - "attributes" must appear first in the order.
     */
    private static JSONObject buildJsonFromNode(Node node) {
        // Use a LinkedHashMap to preserve insertion order: "attributes" first, then children
        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();

        // Build the "attributes" object
        JSONObject attrJson = new JSONObject();
        if (node.hasAttributes()) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                attrJson.put(attr.getNodeName(), attr.getNodeValue());
            }
        }
        jsonMap.put("attributes", attrJson);

        // Group children by tag name
        Map<String, JSONArray> childMap = new LinkedHashMap<>();
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String childTagName = child.getNodeName();
                childMap.computeIfAbsent(childTagName, k -> new JSONArray())
                        .put(buildJsonFromNode(child));
            }
        }

        // Place each child array into the JSON map
        for (Map.Entry<String, JSONArray> entry : childMap.entrySet()) {
            // If a tag has only one element, put a single object, otherwise put an array
            jsonMap.put(entry.getKey(),
                    entry.getValue().length() == 1
                            ? entry.getValue().getJSONObject(0)
                            : entry.getValue());
        }

        // Return as a JSONObject
        return new JSONObject(jsonMap);
    }
}