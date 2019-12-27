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
 1- la p-ième page du manuscrit: texte.get(p).get(0).get(0).valeur;
 2- la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(0).valeur;
 3- le n-ième mot de la p-ième page du manuscrit: texte.get(p).get(0).get(n).valeur;
 4- le n-ième mot de la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(n).valeur;
 5- le n-ième mot du manuscrit: texte.get(0).get(0).get(n).valeur;
 6- le n-ième mot de la l-ième ligne du manuscrit: texte.get(0).get(l).get(n).valeur;
 7- la l-ième ligne du manuscrit: texte.get(0).get(l).get(0).valeur;
 8- tout le manuscrit: texte.get(0).get(0).get(0).valeur;
 
 Pour avoir accès à d'autres propriétés d'un mot voir la classe mot.java .
 
Information concernant le fonctionnement du programme :
 1. Les mots sont classées par espace ou retour à la ligne.
 2. Sur la 1ère boucle for, ce n'est pas sur un mot que l'on tombe en premier, mais sur une page : la première !
    Pourquoi ? Car nous avons déclaré les numéros de page, de ligne et de mot à 0 et non à de "réels" valeurs. Notre premier numéro pour un mot ou une ligne est donc pour nous 010001. Tandis que celui d'une page, nous est 110101. C'est pour ça que l'on est dirigé sur une page et non un mot ou une ligne.
    Ainsi, en premier, nous ajoutons la première page du manuscrit à l'ArrayList texte !
    --> On ajoute la page avant d'ajouter son premier mot.
    Ainsi, on peut, sur les prochaines itérations, faire : texte.size()-1 sans ajouter sur l'index 0 de texte mais bien sur le bon index, la bonne page!
 */

public class get_data {
    
    
    public static void main(String[] args) throws ParserConfigurationException, SAXException, DocumentException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String data_name_file = "GA20032.xml";
        Document document = builder.parse(new File("../../data/" + data_name_file));
        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();
        
        NodeList point_de_depart = document.getElementsByTagName("ab");
        NodeList balises = point_de_depart.item(0).getChildNodes();
        
        int page = 0;
        int numero_de_ligne = 0;
        int numero_de_mot = 0;
        String manuscrit = "";
        final boolean FINAL = true; // On récupère le manuscrit tel qu'on peut le lire aujourd'hui, c'est-à-dire avec toutes les corrections de scribe. Si ça avait été false, on aurait pris le texte du manuscrit avant toute correction.
        ArrayList<ArrayList<ArrayList<mot>>> texte = new ArrayList<ArrayList<ArrayList<mot>>>();
        ArrayList<ArrayList<mot>> page1 = new ArrayList<ArrayList<mot>>();
        ArrayList<mot> ligne1 = new ArrayList<mot>();
        page1.add(ligne1);
        texte.add(page1);
        
        // Pour les <w> dans les <app>.
        boolean in_app = false;
        int nbr_of_rdg_visited = 0;
        int dernier_indice_du_mot_en_rdg = 0;
        
        // On considère qu'une balise <app> reprèsente une réctification scribale.
        // Pour les <rdg>, on considère que parmi plusieurs étapes de corrections (<rdg>), celle qui "a le dernier mot" càd celle a afficher, est celle qui est premier enfant de <app>.
    
        int nombreAoterSurNumerotationPourRenumerotation = 0;
                
        ArrayList<ArrayList<ArrayList<mot>>> corrections = new ArrayList<ArrayList<ArrayList<mot>>>(); // Toutes les corrections.
        
        // Pour les numéros de mot.
        String milestone = "000000";
        String pos_in_milestone = "00";
        
        // boolean stop_debugging = false;
        
        for (int temp = 0; temp < balises.getLength(); temp++)
        {
            
            Node node = balises.item(temp);
            if(in_app) {
                temp--;
                node = balises.item(temp); // Pour les <w> dans les <app>. Nous restons dans <app>.
            }
            
            if (node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getTagName() == "milestone") { // Pour gérer les numéros de verset.
                // On met à jour le numéro du milestone et sa position.
                milestone = ((Element)node).getAttribute("n");
                pos_in_milestone = "01";
            }
            
            // Générer "le numéro du mot".
            String numeroDuMot = milestone + pos_in_milestone;
            
            if (node.getNodeType() == Node.ELEMENT_NODE && ( ((Element)node).getTagName() == "w" || ((Element)node).getTagName() == "app"))
            {
                
                            if ( (node.getNodeType() == Node.ELEMENT_NODE && ((Element)node).getTagName() == "app")) {
                                in_app = true; // Vaudra false au dernier <w> du dernier <rdg> (le 2ème donc).
                                NodeList balises_dans_app = node.getChildNodes(); // On récupère les balises filles de <app> comme <rdg> par exemple.
                                
                                int the_rdg_index_we_will_use_now = 0;
                                int last_rdg_index = 0;
                                
                                boolean notYet = true;
                                for (int i = 0; i < balises_dans_app.getLength(); i++) {
                                    if( (balises_dans_app.item(i).getNodeType() == Node.ELEMENT_NODE) && ( ((Element)balises_dans_app.item(i)).getTagName() == "rdg" ) ) {
                                        if (i >= nbr_of_rdg_visited && notYet) {the_rdg_index_we_will_use_now = i; notYet = false;}
                                        last_rdg_index = i;
                                    }
                                }
                                
                                // Bloc pour sauvegarder les rectifications scribales.
                                if (dernier_indice_du_mot_en_rdg == 0) { // Si nous sommes dans un nouveau <rdg>, autrement dit à une nouvelle étape.
                                    if (nbr_of_rdg_visited == 0) { // Si nous venons d'entrer dans <app>, autrement dit si nous venons de "découvrir" la correction, nous sommes dans "la première étape".
                                        ArrayList<ArrayList<mot>> NouvelleCorrection = new ArrayList<ArrayList<mot>>();
                                        corrections.add(NouvelleCorrection);
                                    }
                                    ArrayList<mot> NouvelleEtape = new ArrayList<mot>();
                                    corrections.get(corrections.size()-1).add(NouvelleEtape);
                                }
                                
                                NodeList liste_de_mot_dans_le_rdg;
                                if(balises_dans_app.item(the_rdg_index_we_will_use_now) instanceof NodeList && ((NodeList)balises_dans_app.item(the_rdg_index_we_will_use_now)).getLength() >= 1) { // balises_dans_app.item(the_rdg_index_we_will_use_now) correspond à une balise <rdg>, la première ou la seconde (cela dépend du code juste au-dessus).
                                    liste_de_mot_dans_le_rdg = (NodeList)balises_dans_app.item(the_rdg_index_we_will_use_now);
                                    
                                    // On passe toute les balises qui ne sont pas <w> comme par exemple <lb> si présente.
                                    while( // Cette condition est sûrement simplifiable.
                                          dernier_indice_du_mot_en_rdg < liste_de_mot_dans_le_rdg.getLength()
                                          && (
                                              liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg) == null
                                              ||
                                              (
                                                  liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg) != null
                                                  &&
                                                  liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg).getNodeType() != Node.ELEMENT_NODE
                                               )
                                              ||
                                              (
                                                  liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg) != null
                                                  &&
                                                  liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg).getNodeType() == Node.ELEMENT_NODE
                                                  &&
                                                  ((Element)liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg)).getTagName() != "w"
                                               )
                                              )
                                          )
                                    {
                                        dernier_indice_du_mot_en_rdg++;
                                    }
                                    if (the_rdg_index_we_will_use_now > 0) { // => Si nous ne sommes pas dans le "premier" <rdg> (càd celui que l'on retient).
                                        nombreAoterSurNumerotationPourRenumerotation++;
                                    }
                                    if (   dernier_indice_du_mot_en_rdg < liste_de_mot_dans_le_rdg.getLength()
                                        && liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg) != null
                                        && liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg).getNodeType() == Node.ELEMENT_NODE
                                        && ((Element)liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg)).getTagName() == "w" ) {
                                            node = liste_de_mot_dans_le_rdg.item(dernier_indice_du_mot_en_rdg);
                                            corrections.get(corrections.size()-1).get(corrections.get(corrections.size()-1).size()-1).add(new mot(formatNominaSacra(node), numeroDuMot));
                                            dernier_indice_du_mot_en_rdg++;
                                            if (dernier_indice_du_mot_en_rdg >= liste_de_mot_dans_le_rdg.getLength()) { // S'il ne reste plus de <w> dans ce rdg. Càd que celui-ci fut le dernier de ce rdg.
                                                dernier_indice_du_mot_en_rdg = 0;
                                                nbr_of_rdg_visited++;
                                                if (the_rdg_index_we_will_use_now == last_rdg_index) {
                                                    in_app = false;
                                                    nbr_of_rdg_visited = 0;
                                                }
                                            }
                                    }
                                }
                                else { // Dans ce cas, le scribe a fait une suppression, ou alors une insertion.
                                    // On sauvegarde la réctification comme un mot vide.
                                    corrections.get(corrections.size()-1).get(corrections.get(corrections.size()-1).size()-1).add(new mot("", numeroDuMot));
                                    // Le code ci-dessous est expliqué ci-dessus.
                                    dernier_indice_du_mot_en_rdg = 0;
                                    nbr_of_rdg_visited++;
                                    if (the_rdg_index_we_will_use_now == last_rdg_index) {
                                        in_app = false;
                                        nbr_of_rdg_visited = 0;
                                    }
                                    nombreAoterSurNumerotationPourRenumerotation++; // Car lors d'une suppresion, on compte le mot vide qui remplace ce(ux) qui étai(en)t à l'origine.
                                    continue;
                                }
                                
                            }
                
                // Pour les corrections scribales
                boolean PrendreLeMot = ! isTheWordToIgnore(node, FINAL);
                
                // Pour les nomina sacra
                Node index = node;
                nominasacra_or_number(index, manuscrit);
                
                Element eElement = (Element) node;
                

                int prochain_numero_de_mot = numero_de_mot + 1; // - nombreAoterSurNumerotationPourRenumerotation;
                String prochain_mot = Integer.toString(page) + "1" + String.format("%02d", numero_de_ligne) + String.format("%02d", prochain_numero_de_mot);

                int prochain_numero_de_ligne = numero_de_ligne + 1;
                String prochaine_ligne = Integer.toString(page) + "1" + String.format("%02d", prochain_numero_de_ligne) + "01";

                int prochain_numero_de_page = page + 1;
                String prochaine_page = Integer.toString(prochain_numero_de_page) + "10101";
                
                int numeroDeMotRenumerote = Integer.parseInt(eElement.getAttribute("n")) - nombreAoterSurNumerotationPourRenumerotation;
                String numeroDePlacementDuMot = Integer.toString(numeroDeMotRenumerote);
                
                /* if( !stop ) { // debugging code.
                    System.out.println("text: " + eElement.getTextContent() + "\nprochain_mot: " + prochain_mot + "\nnumeroDePlacementDuMot: " + numeroDePlacementDuMot + "\nprochaineligne: " + prochaine_ligne + "\nnombreAoterSurNumerotationPourRenumerotation: " + nombreAoterSurNumerotationPourRenumerotation);
                    if(eElement.getTextContent().equals("ηκουϲθη")) stop = true;
                }*/

                // "PROCHAIN MOT" --------------------------------------------------------------------
                // On pourrait inverser les conditions if et else if mais je préfère les laisser commme tel pour montrer qu'on ne "peut" pas faire n'importe quoi concernant leur ordre.
                if( !eElement.getAttribute("n").equals(prochaine_ligne) && numeroDePlacementDuMot.equals(prochain_mot)) {
                    
                    if(PrendreLeMot) {
                        manuscrit += " ";
                    }
                    if(PrendreLeMot) {
                        manuscrit += eElement.getTextContent();
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(new mot(eElement.getTextContent(), numeroDuMot)); // On ajoute le mot sur la dernière ligne qu'on ait de la dernière page "écrite".
                    }
                    numero_de_mot++;
                    // On incrémente la position du milestone.
                    int tmp = Integer.parseInt(pos_in_milestone);
                    pos_in_milestone = String.format("%02d", (tmp+1));
                    
                }

                // "PROCHAINE LIGNE" -----------------------------------------------------------------
                else if(eElement.getAttribute("n").equals(prochaine_ligne)) {
                    
                    if(eElement.getElementsByTagName("lb").getLength() == 1 ) {
                         manuscrit = mot_coupe(eElement, PrendreLeMot, manuscrit, texte, numeroDuMot);
                    }
                    else if(PrendreLeMot) {
                        manuscrit += "\n";
                        manuscrit += eElement.getTextContent();
                        classe_et_prepare_une_ligne(texte);
                        texte.get(texte.size()-1).add(new ArrayList<mot>());
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(new mot(eElement.getTextContent(), numeroDuMot));
                    }
                    numero_de_ligne++;
                    numero_de_mot = 1;
                    nombreAoterSurNumerotationPourRenumerotation = 0;
                    // On incrémente la position du milestone.
                    int tmp = Integer.parseInt(pos_in_milestone);
                    pos_in_milestone = String.format("%02d", (tmp+1));
                    
                }

                // "PROCHAINE PAGE" ------------------------------------------------------------------
                else if(eElement.getAttribute("n").equals(prochaine_page) || numeroDePlacementDuMot.equals(prochaine_page)) {
                    
                    if(eElement.getElementsByTagName("pb").getLength() == 1 ) {
                        manuscrit = mot_coupe(eElement, PrendreLeMot, manuscrit, texte, numeroDuMot);
                    }
                    else if(PrendreLeMot) {
                        if( page !=0 ) manuscrit += "\n\n";
                        manuscrit += eElement.getTextContent();
                        if( page !=0 ) classe_et_prepare_une_page(texte);
                        texte.add(new ArrayList<ArrayList<mot>>());
                        texte.get(texte.size()-1).add(new ArrayList<mot>());
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(new mot(eElement.getTextContent(), numeroDuMot));
                    }
                    page++;
                    numero_de_ligne = 1;
                    numero_de_mot = 1;
                    nombreAoterSurNumerotationPourRenumerotation = 0;
                    // On incrémente la position du milestone.
                    int tmp = Integer.parseInt(pos_in_milestone);
                    pos_in_milestone = String.format("%02d", (tmp+1));
                }
            }
        }
        classe_et_prepare_une_page(texte);
        
        for (int a = 1, i = 1; a < texte.size(); a++) {
            for (int c = 1; c < texte.get(a).get(0).size(); c++) {  // Pour: texte.get(0).get(0).get(n); cela affiche le n-ième mot du manuscrit.
                texte.get(0).get(0).add(new mot(texte.get(a).get(0).get(c).valeur, texte.get(a).get(0).get(c).numero));
            }
            for (int b = 1; b < texte.get(a).size(); b++, i++) {
                ArrayList<mot> ligne = new ArrayList<mot>();
                for (int c = 0; c < texte.get(a).get(b).size(); c++) {
                    ligne.add(texte.get(a).get(b).get(c)); // Pour: texte.get(0).get(l).get(n); cela affiche le n-ième mot de la l-ième ligne du manuscrit. Et si n = 0 alors cela donne la l-ième ligne du manuscrit.
                }
                texte.get(0).add(ligne);
            }
        }
        texte.get(0).get(0).add(0, new mot(manuscrit, texte.get(1).get(0).get(1).numero)); // Pour: texte.get(0).get(0).get(0); cela affiche tout le manuscrit.
        
        System.out.println("Informations:\nNombre de page du manuscrit: " + Integer.toString(texte.size()-1) + "\nNombre de ligne dans le manuscrit (sans compter les 4 titres des évangiles): " +  Integer.toString(texte.get(0).size()-1));
        
        /* Debugging part for saved corrections
        System.out.println("\nListe des " + corrections.size() + " corrections : ");
        for (ArrayList<ArrayList<mot>> cor_i : corrections) {
            System.out.println("\tNouvelle correction : ");
            for ( ArrayList<mot> etape_i : cor_i) {
                String valeur_etape_i = "";
                for (int i = 0; i < etape_i.size(); i++) {
                   valeur_etape_i += " " + etape_i.get(i).valeur;
                }
                System.out.println("\t\tOn a : " + valeur_etape_i +  " (1er numéro de ce mot: " + etape_i.get(0).numero + ")");
            }
        }
        */
        
        data_to_tex tex = new data_to_tex(texte, corrections, data_name_file.substring(0, data_name_file.length()-4));
        tex.generer();
    }

    static void nominasacra_or_number (Node index, String manuscrit) {
        if(index.hasChildNodes()) {
            NodeList test = index.getChildNodes();
            for (int i = 0; i < test.getLength(); i++){
                if(test.item(i).hasChildNodes()) {
                    NodeList bb = test.item(i).getChildNodes();
                    for (int j = 0; j < bb.getLength(); j++){
                        nominasacra_or_number(bb.item(j), manuscrit);
                    }
                }
                if(test.item(i).getNodeName() == "abbr" && ( ( (Element) test.item(i) ).getAttribute("type").equals("nominasacra") || ( (Element) test.item(i) ).getAttribute("type").equals("number") ) ){
                    test.item(i).setTextContent("\\" + "textoverline{" + ((Element)test).getTextContent() + "}");
                }
            }
        }
    }
    
    static String formatNominaSacra (Node text) {
        if(text.hasChildNodes()) {
            NodeList test = text.getChildNodes();
            for (int i = 0; i < test.getLength(); i++){
                /* if(test.item(i).hasChildNodes()) {
                    NodeList bb = test.item(i).getChildNodes();
                    for (int j = 0; j < bb.getLength(); j++){
                        formatNominaSacra(bb.item(j));
                    }
                } */
                if(test.item(i).getNodeName() == "abbr" && ( ( (Element) test.item(i) ).getAttribute("type").equals("nominasacra") || ( (Element) test.item(i) ).getAttribute("type").equals("number") ) ){
                    return "°" + ((Element)test).getTextContent() + "°";
                }
                else {
                    return ((Element)test).getTextContent();
                }
            }
        }
        return text.getTextContent();
    }

    static boolean isTheWordToIgnore(Node given, boolean finalMode) {
        return (given.getParentNode().getNodeName() == "rdg" && ( (Element) given.getParentNode() ).getAttribute("type").equals("orig") && ( (Element) given.getParentNode().getPreviousSibling() ).getAttribute("type").equals("corr") && finalMode);
    }
    
    static String mot_coupe (Element eElement, boolean PrendreLeMot, String manuscript, ArrayList<ArrayList<ArrayList<mot>>> texte, String numeroDuMot) {
        NodeList parties = eElement.getChildNodes();
        if(PrendreLeMot) manuscript += " ";
        for (int a = 0; a < parties.getLength(); a++) {
            Node partie = parties.item(a);
            manuscript += partie.getTextContent(); // Pour les parties qui sont entre balises par exemple : <unclear> (voir le α de ιακωβ à la ligne 2 de la page 1).
            if( a == 0 ) texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(new mot(partie.getTextContent(), numeroDuMot));
            else if (texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1 >= 0) {
                texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).set(
                        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1, new mot( texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).get(texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size()-1).valeur + partie.getTextContent(), numeroDuMot)
                );
            }
            else texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(new mot(partie.getTextContent(), numeroDuMot));
            if (PrendreLeMot && partie.getNodeType() == Node.ELEMENT_NODE) // Quand on arrive à l'intersection d'une nouvelle ligne (<lb/>) ou page (<pb/>) ("ELEMENT_NODE").
            {
                if (partie.getNodeName() == "pb") {
                    manuscript += "\n\n";
                    classe_et_prepare_une_page(texte); // Seulement utile pour l'array texte
                    texte.add(new ArrayList<ArrayList<mot>>());
                    texte.get(texte.size()-1).add(new ArrayList<mot>());
                }
                else if (partie.getNodeName() == "lb") {
                    manuscript += "\n";
                    classe_et_prepare_une_ligne(texte); // Seulement utile pour l'array texte
                    texte.get(texte.size()-1).add(new ArrayList<mot>());
                }
            }
        }
        return manuscript;
    }
    
    static void classe_et_prepare_une_ligne (ArrayList<ArrayList<ArrayList<mot>>> texte) {
        String toute_la_ligne = "";
        for (int j = 0; j <  texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).size(); j++) {
            toute_la_ligne += texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).get(j).valeur + " ";
        }
        toute_la_ligne = toute_la_ligne.substring(0, toute_la_ligne.length()-1);
        texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).add(0, new mot(toute_la_ligne, texte.get(texte.size()-1).get(texte.get(texte.size()-1).size()-1).get(0).numero));
    }
    
    static void classe_et_prepare_une_page (ArrayList<ArrayList<ArrayList<mot>>> texte) {
        classe_et_prepare_une_ligne(texte); // Cette instruction est pour ajouter la dernière ligne de la page que l'on vient de terminer.
        ArrayList<mot> tmp = new ArrayList<mot>();
        String toute_la_page = "\n";
        for (int i = 0; i < texte.get(texte.size()-1).size(); i++) {
            toute_la_page += texte.get(texte.size()-1).get(i).get(0);
            for (int j = 1; j < texte.get(texte.size()-1).get(i).size(); j++) {
                tmp.add(new mot(texte.get(texte.size()-1).get(i).get(j).valeur, texte.get(texte.size()-1).get(i).get(j).numero));
            }
        }
        tmp.add(0, new mot(toute_la_page, texte.get(texte.size()-1).get(0).get(0).numero));
        texte.get(texte.size()-1).add(0, tmp);
    }
}
