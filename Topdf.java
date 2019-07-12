import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// L'ArrayList ne gère pas les mot coupés. Et on ne s'est pas occupé non plus de l'index 0 de ces ArrayList.

public class Topdf {

    static String nomina_sacra = "";

    public static void main(String[] args) throws ParserConfigurationException, SAXException, DocumentException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File("GA20032.xml"));
        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        // On récupère tous les mots.
        NodeList nList = document.getElementsByTagName("w");

        int page = 0;
        int numero_de_ligne = 0;
        int numero_de_mot = 0;
        String manuscrit = "";
        final boolean FINAL = true; // On récupère le manuscrit tel qu'on peut le lire aujourd'hui, c'est-à-dire avec toutes les corrections de scribe. Si ça avait été false, on aurait pris le texte du manuscrit avant toute correction.
        ArrayList<ArrayList<ArrayList<String>>> texte = new ArrayList<ArrayList<ArrayList<String>>>();
        ArrayList<ArrayList<String>> page1 = new ArrayList<ArrayList<String>>();
        ArrayList<String> ligne1 = new ArrayList<String>();
        page1.add(ligne1);
        texte.add(page1);
        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);

            // Pour les corrections scribales
            boolean PrendreLeMot = ! isTheWordToIgnore(node, FINAL);
            
            // Pour les nomina sacra
            Node index = node;
            nominasacra(index, manuscrit);

            if (node.getNodeType() == Node.ELEMENT_NODE)
            { // Je n'ai pas trouvé de cas où ça ne rentrer pas dans cette boucle.
                
                Element eElement = (Element) node;

                int prochain_numero_de_mot = numero_de_mot + 1;
                String prochain_mot = Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne) + String.format("%02d", prochain_numero_de_mot);

                int prochain_numero_de_ligne = numero_de_ligne + 1;
                String prochaine_ligne = Integer.toString(page) + "1" + String.format("%02d", prochain_numero_de_ligne) + "01";

                int prochain_numero_de_page = page + 1;
                String prochaine_page = Integer.toString(prochain_numero_de_page) + "10101";


                if(eElement.getAttribute("n").equals(prochain_mot)) {
                    if(PrendreLeMot) {
                        manuscrit += " ";
                    }
                    if(PrendreLeMot) {
                        manuscrit += eElement.getTextContent();
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent());
                    }
                    numero_de_mot++;
                }

                else if(eElement.getAttribute("n").equals(prochaine_ligne)) {
                    if(eElement.getElementsByTagName("lb").getLength() == 1 ) { // Pourquoi avais-je mis ">= 1" au lieu de "== 1" ?
                        NodeList parties = eElement.getChildNodes();
                        if(PrendreLeMot) manuscrit += " ";
                        for (int tmp = 0; tmp < parties.getLength(); tmp++) {
                            Node partie = parties.item(tmp);
                            if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive sur <lb/>
                            {
                                if(partie.getNodeName() == "lb") manuscrit += "\n";
                                Element ePartie = (Element) partie;
                                manuscrit += ePartie.getTextContent();
                            }
                            else if (PrendreLeMot && partie.getNodeType() == Node.TEXT_NODE) // Quand on arrive sur une des deux parties du mot séparé par <lb/>.
                                manuscrit += partie.getTextContent();
                        }
                    }
                    else {
                        if(PrendreLeMot) {
                            manuscrit += "\n";
                        }
                        if(PrendreLeMot) {
                            manuscrit += eElement.getTextContent();
                            texte.get(texte.size()-1).add(new ArrayList<String>());
                            texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent());
                        }
                    }
                    numero_de_ligne++;
                    numero_de_mot = 1;

                }

                else if(eElement.getAttribute("n").equals(prochaine_page)) {
                    if(eElement.getElementsByTagName("pb").getLength() == 1 ) { // Pourquoi avais-je mis ">= 1" au lieu de "== 1" ?
                        //manuscrit += "mot coupé";
                        NodeList parties = eElement.getChildNodes();
                        if(PrendreLeMot) manuscrit += " ";
                        for (int tmp = 0; tmp < parties.getLength(); tmp++) {
                            Node partie = parties.item(tmp);
                            if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive sur <lb/>
                            {
                                if(partie.getNodeName() == "pb") manuscrit += "\n\n";
                                Element ePartie = (Element) partie;
                                manuscrit += ePartie.getTextContent();
                            }
                            else if (PrendreLeMot && partie.getNodeType() == Node.TEXT_NODE) // Quand on arrive sur une des deux parties du mot séparé par <lb/>.
                                manuscrit += partie.getTextContent();
                        }
                    }
                    else {
                        if( page !=0 && PrendreLeMot) {
                            manuscrit += "\n\n";
                        }
                        if(PrendreLeMot) {
                            manuscrit += eElement.getTextContent();
                            texte.add(new ArrayList<ArrayList<String>>());
                            texte.get(texte.size()-1).add(new ArrayList<String>());
                            texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent());
                        }
                    }
                    page++;
                    numero_de_ligne = 1;
                    numero_de_mot = 1;
                }

            }
        }
        CreerPDF pdf = new CreerPDF(manuscrit, "032");
        pdf.generer();
    }

    static void nominasacra (Node index, String manuscrit) {
        if(index.hasChildNodes()) {
            NodeList test = index.getChildNodes();
            for (int i = 0; i < test.getLength(); i++){
                if(test.item(i).hasChildNodes()) {
                    NodeList bb = test.item(i).getChildNodes();
                    for (int j = 0; j < bb.getLength(); j++){
                        nominasacra(bb.item(j), manuscrit);
                    }
                }
                if(test.item(i).getNodeName() == "abbr" && ( (Element) test.item(i) ).getAttribute("type").equals("nominasacra")){
                    test.item(i).setTextContent("\\" + "textoverline{" + ((Element)test).getTextContent() + "}");
                }
            }
        }
    }

    static boolean isTheWordToIgnore(Node given, boolean finalMode) {
        return (given.getParentNode().getNodeName() == "rdg" && ( (Element) given.getParentNode() ).getAttribute("type").equals("orig") && ( (Element) given.getParentNode().getPreviousSibling() ).getAttribute("type").equals("corr") && finalMode);
    }

}
