package tech.laanaoui.capella;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            List<String> SUMMARIES_TO_ENCRYPT =  List.of("create, confidentiality=c1", "create, confidentiality=c2",
                    "create, confidentiality=c3","confidentiality=c3", "confidentiality=c1, type=fixed, cyph=c1",
                    "confidentiality=c2, type=fixed, cyph=c1,c2");
            List<String> SUMMARIES_TO_DECRYPT =  List.of("no_cipher c=lowest", "");

            // for each element in the XML file, if the tag has an attribute summary with the value "toto" or "c1" show the entier tag with all its attributes
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
                System.out.println(" - "  + element.getTagName() + " (" + element.getAttribute("id") + ")");
            }
            System.out.println("-------------------------------------------------");
            System.out.println("Elements where should decrypt: ");
            for (Element element : elementsWhereShouldDecrypt) {
                System.out.println(" - "  + element.getTagName() + " (" + element.getAttribute("id") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}