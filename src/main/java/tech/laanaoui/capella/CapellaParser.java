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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            linkInvolvementsToFunctions(involvements, functions);

            removeEmptyInvolvements(involvements);
            printChains(involvements);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeEmptyInvolvements(List<OwnedFunctionalChainInvolvements> involvements) {
        List<OwnedFunctionalChainInvolvements> toRemove = new ArrayList<>();
        for (OwnedFunctionalChainInvolvements involvement : involvements) {
            if (involvement.getFunction() == null) {
                toRemove.add(involvement);
            }
        }
        involvements.removeAll(toRemove);
    }


    private static void linkInvolvementsToFunctions(List<OwnedFunctionalChainInvolvements> involvements, List<OwnedFunction> functions) {
    for (OwnedFunctionalChainInvolvements involvement : involvements) {
        boolean functionFound = false;
        for (OwnedFunction function : functions) {
            if (function.id().equals(involvement.getFunction())) {
                involvement.setFunction(function.name().isEmpty() ? null : function.name());
                functionFound = true;
                break;
            }
        }
        if (!functionFound) {
            involvement.setFunction(null);
        }
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


        Map<String, StringBuilder> sb = new HashMap<>();
        for (String chain : chains) {
            sb.put(chain, new StringBuilder());
            System.out.println("\n"+chain + " :");
            for (OwnedFunctionalChainInvolvements involvement : involvements) {
                if(involvement.getSummary().contains("ciph")){
                    //split the value of summary attribute by , and get the value of ciph ex summary="create, confidentiality=C1, ciph:1" => ciph should be true, if ciph is not present then it should be false, if ciph value is 0 then it should be false
                    String[] summaryArray = involvement.getSummary().split(",");
                    boolean ciph = false;
                    for (String s : summaryArray) {
                        if(s.contains("ciph")){
                            String[] ciphArray = s.split(":");
                            if(ciphArray.length > 1){
                                ciph = ciphArray[1].equals("1");
                                break;
                            }
                        }
                    }
                    if (ciph && involvement.getChain().equals(chain)) {
                        sb.get(chain).append("\nSHOULD CIPHER IN FUNCTION: ").append(involvement.getFunction()).append("\n")
                                .append("INTEGRITY CHECK: AtRest").append("\n");
                    }
                }
                if (involvement.getChain().equals(chain)) {
                    System.out.println(" - " + involvement);
                }
            }
            if(!sb.get(chain).isEmpty()){
                System.out.println(sb.get(chain));
            }else {
                System.out.println("\nNO CIPHER OR INTEGRITY CHECK REQUIRED");
            }
            System.out.println("--------------------\n");
        }
    }
}