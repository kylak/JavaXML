import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import org.xml.sax.SAXException;
import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/*
 Voici comment accèder à l'ArrayList texte :
 Pour avoir :
 - la p-ième page du manuscrit: texte.get(p).get(0).get(0);
 - la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(0);
 - le n-ième mot de la p-ième page du manuscrit: texte.get(p).get(0).get(n);
 - le n-ième mot de la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(n);
 - le n-ième mot du manuscrit: texte.get(0).get(0).get(n);
 - le n-ième mot de la l-ième ligne du manuscrit: texte.get(0).get(l).get(n);
 - la l-ième ligne du manuscrit: texte.get(0).get(l).get(0);
 - tout le manuscrit: texte.get(0).get(0).get(0);
 
 Pour la prochaine fois :
 1. Associer à chaque mot son numéro.
 
Information concernant le fonctionnement du programme :
 1. Les mots sont classées par espace ou retour à la ligne.
 2. Sur la 1ère boucle for, ce n'est pas sur un mot que l'on tombe en premier, mais sur une page : la première !
    Pourquoi ? Car nous avons déclaré les numéros de page, de ligne et de mot à 0 et non à de "réels" valeurs. Notre premier numéro pour un mot ou une ligne est donc pour nous 010001. Tandis que celui d'une page, nous est 110101. C'est pour ça que l'on est dirigé sur une page et non un mot ou une ligne.
    Ainsi, en premier, nous ajoutons la première page du manuscrit à l'ArrayList texte !
    --> On ajoute la page avant d'ajouter son premier mot.
    Ainsi, on peut, sur les prochaines itérations, faire : texte.size()-1 sans ajouter sur l'index 0 de texte mais bien sur le bon index, la bonne page!
 */

public class Topdf {

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
            { // Je n'ai pas trouvé de cas où ça ne rentrait pas dans cette boucle.
                
                Element eElement = (Element) node;

                int prochain_numero_de_mot = numero_de_mot + 1;
                String prochain_mot = Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne) + String.format("%02d", prochain_numero_de_mot);

                int prochain_numero_de_ligne = numero_de_ligne + 1;
                String prochaine_ligne = Integer.toString(page) + "1" + String.format("%02d", prochain_numero_de_ligne) + "01";

                int prochain_numero_de_page = page + 1;
                String prochaine_page = Integer.toString(prochain_numero_de_page) + "10101";

                // "PROCHAIN MOT" --------------------------------------------------------------------
                if(eElement.getAttribute("n").equals(prochain_mot)) {
                    
                    if(PrendreLeMot) {
                        manuscrit += " ";
                    }
                    if(PrendreLeMot) {
                        manuscrit += eElement.getTextContent();
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent()); // On ajoute le mot sur la dernière ligne qu'on ait de la dernière page "écrite".
                    }
                    numero_de_mot++;
                    
                }

                // "PROCHAINE LIGNE" -----------------------------------------------------------------
                else if(eElement.getAttribute("n").equals(prochaine_ligne)) {
                    
                    if(eElement.getElementsByTagName("lb").getLength() == 1 ) {
                         manuscrit = mot_coupe(eElement, PrendreLeMot, manuscrit, texte);
                    }
                    else if(PrendreLeMot) {
                        manuscrit += "\n";
                        manuscrit += eElement.getTextContent();
                        classe_et_prepare_une_ligne(texte);
                        texte.get(texte.size()-1).add(new ArrayList<String>());
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent());
                    }
                    numero_de_ligne++;
                    numero_de_mot = 1;
                    
                }

                // "PROCHAINE PAGE" ------------------------------------------------------------------
                else if(eElement.getAttribute("n").equals(prochaine_page)) {
                    
                    if(eElement.getElementsByTagName("pb").getLength() == 1 ) {
                        manuscrit = mot_coupe(eElement, PrendreLeMot, manuscrit, texte);
                    }
                    else if(PrendreLeMot) {
                        if( page !=0 ) manuscrit += "\n\n";
                        manuscrit += eElement.getTextContent();
                        if( page !=0 ) classe_et_prepare_une_page(texte);
                        texte.add(new ArrayList<ArrayList<String>>());
                        texte.get(texte.size()-1).add(new ArrayList<String>());
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(eElement.getTextContent());
                    }
                    page++;
                    numero_de_ligne = 1;
                    numero_de_mot = 1;
                    
                }
            }
        }
        classe_et_prepare_une_page(texte);
        
        for (int a = 1, i = 1; a < texte.size(); a++) {
            for (int c = 1; c < texte.get(a).get(0).size(); c++) {  // Pour: texte.get(0).get(0).get(n); cela affiche le n-ième mot du manuscrit.
                texte.get(0).get(0).add(texte.get(a).get(0).get(c));
            }
            for (int b = 1; b < texte.get(a).size(); b++, i++) {
                ArrayList<String> ligne = new ArrayList<String>();
                for (int c = 0; c < texte.get(a).get(b).size(); c++) {
                    ligne.add(texte.get(a).get(b).get(c)); // Pour: texte.get(0).get(l).get(n); cela affiche le n-ième mot de la l-ième ligne du manuscrit. Et si n = 0 alors cela donne la l-ième ligne du manuscrit.
                }
                texte.get(0).add(ligne);
            }
        }
        texte.get(0).get(0).add(0, manuscrit); // Pour: texte.get(0).get(0).get(0); cela affiche tout le manuscrit.
        
        manuscrit += "\nNombre de page du manuscrit: " + Integer.toString(texte.size()-1) + "\nNombre de ligne dans le manuscrit (sans compter les 4 titres des évangiles): " +  Integer.toString(texte.get(0).size()-1);
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
    
    static String mot_coupe (Element eElement, boolean PrendreLeMot, String manuscript, ArrayList<ArrayList<ArrayList<String>>> texte) {
        NodeList parties = eElement.getChildNodes();
        if(PrendreLeMot) manuscript += " ";
        for (int a = 0; a < parties.getLength(); a++) {
            Node partie = parties.item(a);
            manuscript += partie.getTextContent(); // Pour les parties qui sont entre balises par exemple : <unclear> (voir le α de ιακωβ à la ligne 2 de la page 1).
            if( a == 0 ) texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(partie.getTextContent());
            else if (texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1 >= 0) {
                texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).set(
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1, texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).get(texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1) + partie.getTextContent()
                );
            }
            else texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(partie.getTextContent());
            if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive à l'intersection d'une nouvelle ligne (<lb/>) ou page (<pb/>) ("ELEMENT_NODE").
            {
                if (partie.getNodeName() == "pb") {
                    manuscript += "\n\n";
                    classe_et_prepare_une_page(texte); // Seulement utile pour l'array texte
                    texte.add(new ArrayList<ArrayList<String>>());
                    texte.get(texte.size()-1).add(new ArrayList<String>());
                }
                else if (partie.getNodeName() == "lb") {
                    manuscript += "\n";
                    classe_et_prepare_une_ligne(texte); // Seulement utile pour l'array texte
                    texte.get(texte.size()-1).add(new ArrayList<String>());
                }
            }
        }
        return manuscript;
    }
    
    static void classe_et_prepare_une_ligne (ArrayList<ArrayList<ArrayList<String>>> texte) {
        String toute_la_ligne = "";
        for (int j = 0; j <  texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size(); j++) {
            toute_la_ligne += texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).get(j) + " ";
        }
        toute_la_ligne = toute_la_ligne.substring(0, toute_la_ligne.length());
        toute_la_ligne += "\n";
        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(0, toute_la_ligne);
    }
    
    static void classe_et_prepare_une_page (ArrayList<ArrayList<ArrayList<String>>> texte) {
        classe_et_prepare_une_ligne(texte); // Cette instruction est pour ajouter la dernière ligne de la page que l'on vient de terminer.
        ArrayList<String> tmp = new ArrayList<String>();
        String toute_la_page = "\n";
        for (int i = 0; i < texte.get(texte.size()-1).size(); i++) {
            toute_la_page += texte.get(texte.size()-1).get(i).get(0);
            for (int j = 1; j < texte.get(texte.size()-1).get(i).size(); j++) {
                tmp.add(texte.get(texte.size()-1).get(i).get(j));
            }
        }
        tmp.add(0, toute_la_page);
        texte.get(texte.size()-1).add(0, tmp);
    }
}
