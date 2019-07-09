import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import com.itextpdf.text.DocumentException;
import java.io.IOException;

public class GetXMLData {

    Document XMLdocument; // yes
    String nom_fichier = ""; // yes
    NodeList wordsFromFile; // yes
    String manuscrit = ""; // yes
    final boolean FINAL = true; // On récupère le manuscrit tel qu'on peut le lire aujourd'hui, c'est-à-dire avec toutes les corrections de scribe. Si ça avait été false, on aurait pris le texte du manuscrit avant toute correction.

    Element word = null;
    boolean PrendreLeMot = true;
    int page = 0;
    int numero_de_ligne = 0;
    int numero_de_mot = 0;

    public GetXMLData (String nom_fichier) {
      this.nom_fichier = nom_fichier;          // "GA20032.xml"
    }

    void start () throws ParserConfigurationException, SAXException, DocumentException, IOException {
      readTheFile(nom_fichier);
      wordsFromFile = XMLdocument.getElementsByTagName("w"); // On récupère tous les mots du fichier XML (que l'on vient de lire).
      addWords();
      CreerPDF pdf = new CreerPDF(manuscrit, "032"); // 032
      pdf.generer();
    }
    void readTheFile (String nomFichier) throws ParserConfigurationException, SAXException, DocumentException, IOException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      XMLdocument = builder.parse(new File(nomFichier));
      XMLdocument.getDocumentElement().normalize();
      Element root = XMLdocument.getDocumentElement();
    }
    void addWords () {
      for (int temp = 0; temp < wordsFromFile.getLength(); temp++)
      {
          Node b_word = wordsFromFile.item(temp);
          PrendreLeMot = ! isTheWordToIgnore(b_word, FINAL); // Tous les mots ne sont pas forcément à prendre à cause des corrections scribales.
          addWord(b_word);
      }
    }
    static boolean isTheWordToIgnore(Node given, boolean finalMode) {
      return (given.getParentNode().getNodeName() == "rdg" && ( (Element) given.getParentNode() ).getAttribute("type").equals("orig") && ( (Element) given.getParentNode().getPreviousSibling() ).getAttribute("type").equals("corr") && finalMode);
    }
    void addWord (Node bloc_word) {
      if (bloc_word.getNodeType() == Node.ELEMENT_NODE)
      { // Je n'ai pas trouvé de cas où ça ne rentrer pas dans cette boucle.
          word = (Element) bloc_word;

          if(word.getAttribute("n").equals(next_word_number("same_line"))) {
              new_beginning("");
              numero_de_mot++;
          }
          else if(word.getAttribute("n").equals(next_word_number("new_line"))) {
              new_beginning("line");
              numero_de_ligne++;
              numero_de_mot = 1;

          }
          else if(word.getAttribute("n").equals(next_word_number("new_page"))) {
              new_beginning("page");
              page++;
              numero_de_ligne = 1;
              numero_de_mot = 1;
          }
      }
    }
    String next_word_number (String word_place) {
      if (word_place.equals("same line"))
        return Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne) + String.format("%02d", numero_de_mot + 1); // numero_de_mot + 1 corresponds to the next word number.
      else if (word_place.equals("new line"))
        return Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne + 1) + "01"; // numero_de_ligne + 1 corresponds to the next line number.
      else if (word_place.equals("new page"))
        return Integer.toString(page + 1) + "10101"; // page + 1 corresponds to the next page number.
      return ""; // This instruction shouldn't be exectued.
    }
    void new_beginning(String type) { // true as line and false as page.

        String tag = "pb";
        String value = "\n\n";

        if(type.equals("line")) {
            tag = "lb";
            value = "\n";
        }

        if ( (type == "line" || type == "page") && word.getElementsByTagName(tag).getLength() == 1 ) {
            NodeList parties = word.getChildNodes();
            if(PrendreLeMot) {manuscrit += " ";}
            for (int tmp = 0; tmp < parties.getLength(); tmp++) {
                Node partie = parties.item(tmp);
                if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive sur <lb/>.
                {
                    if(partie.getNodeName() == tag) {manuscrit += value;}
                    Element ePartie = (Element) partie;
                    manuscrit += ePartie.getTextContent();
                }
                else if (PrendreLeMot && partie.getNodeType() == Node.TEXT_NODE) {// Quand on arrive sur une des deux parties du mot séparé par <lb/>.
                    manuscrit += partie.getTextContent();}
            }
        }
        else if (PrendreLeMot) {
            if (type.equals("line")) {manuscrit += value;}
            else if (type.equals("page") && page !=0) {manuscrit += value;}
            else {manuscrit += " ";}
            manuscrit += word.getTextContent();
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, DocumentException, IOException {
      GetXMLData test = new GetXMLData("GA20032.xml");
      test.start();
    }

}
