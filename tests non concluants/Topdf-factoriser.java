import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import com.itextpdf.text.DocumentException;
import java.io.IOException;

public class Topdf {

    int page = 0;
    static int numero_de_ligne = 0;
    static int numero_de_mot = 0;
    static String manuscrit = "";
    static Element eElement;
    static boolean PrendreLeMot;

    public static void main(String[] args) throws ParserConfigurationException, SAXException, DocumentException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File("GA20032.xml"));
        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();

        // On récupère tous les mots.
        NodeList nList = document.getElementsByTagName("w");

        final boolean FINAL = true; // On récupère le manuscrit tel qu'on peut le lire aujourd'hui, c'est-à-dire avec toutes les corrections de scribe. Si ça avait été false, on aurait pris le texte du manuscrit avant toute correction.

        for (int temp = 0; temp < nList.getLength(); temp++)
        {
            Node node = nList.item(temp);

            // Pour les corrections scribales
            PrendreLeMot = ! isTheWordToIgnore(node, FINAL);


            if (node.getNodeType() == Node.ELEMENT_NODE)
            { // Je n'ai pas trouvé de cas où ça ne rentrer pas dans cette boucle.
                eElement = (Element) node;

                int prochain_numero_de_mot = numero_de_mot + 1;
                String prochain_mot = Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne) + String.format("%02d", prochain_numero_de_mot);

                int prochain_numero_de_ligne = numero_de_ligne + 1;
                String prochaine_ligne = Integer.toString(page) + "1" + String.format("%02d", prochain_numero_de_ligne) + "01";

                int prochain_numero_de_page = page + 1;
                String prochaine_page = Integer.toString(prochain_numero_de_page) + "10101";


                if(eElement.getAttribute("n").equals(prochain_mot)) {
                    new_beginning("");
                    numero_de_mot++;
                }
                else if(eElement.getAttribute("n").equals(prochaine_ligne)) {
                    new_beginning("line");
                    numero_de_ligne++;
                    numero_de_mot = 1;

                }
                else if(eElement.getAttribute("n").equals(prochaine_page)) {
                    new_beginning("page");
                    page++;
                    numero_de_ligne = 1;
                    numero_de_mot = 1;
                }
            }
        }

        CreerPDF pdf = new CreerPDF(manuscrit, "032");
        pdf.generer();
    }

    static void new_beginning(String type){ // true as line and false as page.

        String tag = "pb";
        String value = "\n\n";

        if(type.equals("line")) {
            tag = "lb";
            value = "\n";
        }

        if ( (type.equals("line") || type.equals("page")) && eElement.getElementsByTagName(tag).getLength() == 1 ) {
            NodeList parties = eElement.getChildNodes();
            if(PrendreLeMot) manuscrit += " ";
            for (int tmp = 0; tmp < parties.getLength(); tmp++) {
                Node partie = parties.item(tmp);
                if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive sur <lb/>.
                {
                    if(partie.getNodeName() == tag) manuscrit += value;
                    Element ePartie = (Element) partie;
                    manuscrit += ePartie.getTextContent();
                }
                else if (PrendreLeMot && partie.getNodeType() == Node.TEXT_NODE) // Quand on arrive sur une des deux parties du mot séparé par <lb/>.
                    manuscrit += partie.getTextContent();
            }
        }
        else if (PrendreLeMot) {
            if (type.equals("line")) manuscrit += value;
            else if (type.equals("page") && page !=0) manuscrit += value;
            else manuscrit += " ";
            manuscrit += eElement.getTextContent();
        }
    }

static boolean isTheWordToIgnore(Node given, boolean finalMode) {
return (given.getParentNode().getNodeName() == "rdg" && ( (Element) given.getParentNode() ).getAttribute("type").equals("orig") && ( (Element) given.getParentNode().getPreviousSibling() ).getAttribute("type").equals("corr") && finalMode);
}

}
