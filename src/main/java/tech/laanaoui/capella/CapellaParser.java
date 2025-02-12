package tech.laanaoui.capella;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tech.laanaoui.capella.elements.OwnedFunction;
import tech.laanaoui.capella.elements.OwnedFunctionalChainInvolvements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static tech.laanaoui.capella.utils.AppUtils.*;

public class CapellaParser {



    public static void main(String[] args) {
        try {
            // Parse the XML file
            File inputFile = new File("Communication.capella");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // We only interested by the physical architecture layer of the model
            Element rootElement = null;
            NodeList architectures = doc.getElementsByTagName("ownedArchitectures");
            for (int i = 0; i < architectures.getLength(); i++) {
                Element element = (Element) architectures.item(i);
                if (element.hasAttribute(XSI_TYPE) && element.getAttribute(XSI_TYPE).equals(PHYSICAL_ARCHITECTURE_TYPE)) {
                    rootElement = element;
                    break;
                }
            }

            if (rootElement == null) {
                System.out.println(ERROR_LAYER_NOT_FOUND);
                return;
            }

            List<OwnedFunction> functions = parseOwnedFunctions(rootElement);
            List<OwnedFunctionalChainInvolvements> involvements = parseOwnedFunctionalChainInvolvements(rootElement);

            // replace the function id in the involvements with the function name
            for (OwnedFunctionalChainInvolvements involvement : involvements) {
                for (OwnedFunction function : functions) {
                    if (function.id().equals(involvement.getFunction())) {
                        // if function name is empty use null
                        involvement.setFunction(function.name().isEmpty() ? null : function.name());
                    }
                }
            }

            printChains(involvements);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<OwnedFunction> parseOwnedFunctions(Element rootElement) {
        List<OwnedFunction> functions = new ArrayList<>();
        NodeList functionNodes = rootElement.getElementsByTagName("ownedFunctions");
        for (int i = 0; i < functionNodes.getLength(); i++) {
            Element element = (Element) functionNodes.item(i);
            String id = element.getAttribute("id");
            String name = element.getAttribute("name");
            functions.add(new OwnedFunction(id, name));
        }
        return functions;
    }

    private static List<OwnedFunctionalChainInvolvements> parseOwnedFunctionalChainInvolvements(Element rootElement) {
        List<OwnedFunctionalChainInvolvements> involvements = new ArrayList<>();
        NodeList componentNodes = rootElement.getElementsByTagName("ownedFunctionalChainInvolvements");
        for (int i = 0; i < componentNodes.getLength(); i++) {
            Element element = (Element) componentNodes.item(i);
            String id = element.getAttribute("id");
            String summary = element.getAttribute("summary");
            // remove the # at the start of the attribute
            String function = element.getAttribute("involved").substring(1);
            String chain = element.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            involvements.add(new OwnedFunctionalChainInvolvements(id, summary, function, chain));
        }
        return involvements;
    }

    private static void printChains(List<OwnedFunctionalChainInvolvements> involvements) {
        List<String> chains = new ArrayList<>();
        for (OwnedFunctionalChainInvolvements involvement : involvements) {
            if (!chains.contains(involvement.getChain())) {
                chains.add(involvement.getChain());
            }
        }

        for (String chain : chains) {
            System.out.println(chain + " :");
            for (OwnedFunctionalChainInvolvements involvement : involvements) {
                if (involvement.getChain().equals(chain)) {
                    System.out.println(" - " + involvement);
                }
            }
            System.out.println("--------------------\n\n");
        }
    }
}