package tech.laanaoui.capella;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CapellaParser {

    public static void main(String[] args) {
        try {
            // Parse the XML file
            File inputFile = new File("Communication.capella");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // TAGS CONTAINING "summary" ATTRIBUTE
            Properties properties = new Properties();
            properties.load(new FileInputStream("values.properties"));
            String encryptValues = properties.getProperty("ENCRYPT_VALUES");
            String decryptValues = properties.getProperty("DECRYPT_VALUES");
            List<String> SUMMARIES_TO_ENCRYPT = List.of(encryptValues.split("\\|"));
            List<String> SUMMARIES_TO_DECRYPT =  List.of(decryptValues.split("\\|"));

            NodeList elements = doc.getElementsByTagName("*");
            List<Element> elementsWhereShouldEncrypt= new ArrayList<>();
            List<Element> elementsWhereShouldDecrypt= new ArrayList<>();

            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                if (element.hasAttribute("summary")
                        && (SUMMARIES_TO_DECRYPT.contains(element.getAttribute("summary").toLowerCase()))) {
                    elementsWhereShouldDecrypt.add(element);
                }
                if (element.hasAttribute("summary")
                        && (SUMMARIES_TO_ENCRYPT.contains(element.getAttribute("summary").toLowerCase()))) {
                    elementsWhereShouldEncrypt.add(element);
                }
            }

            System.out.println("Elements where should encrypt: ");
            for (Element element : elementsWhereShouldEncrypt) {
                printElement(element);
            }
            System.out.println("-------------------------------------------------");
            System.out.println("Elements where should decrypt: ");
            for (Element element : elementsWhereShouldDecrypt) {
                printElement(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printElement(Element element) {
        String name = element.getAttribute("name");
        String id = element.getAttribute("id");
        String summary = element.getAttribute("summary");

        System.out.println(" - "  + element.getTagName() + " with name \""+ name + "\" and id \"" + id +"\" (" + summary + ")");
    }
}