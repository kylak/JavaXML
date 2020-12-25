/* Licence :
This file is copyright © 2020 by Gustav Berloty (creator of this file) released under the CC BY-NC-SA 4.0 licence :
https://creativecommons.org/licenses/by-nc-sa/4.0/
*/

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import java.io.IOException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.*;
import java.util.List;
import java.util.Arrays;
import java.util.stream.*;
import java.lang.reflect.Array;
import java.util.LinkedList;
import name.fraser.neil.plaintext.diff_match_patch;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

    /*
 !!! Un numéro de mot est composé de 8 chiffres : les 2 premiers chiffres correspondent au numéro de livre, les 2 suivants au numéro de chapitre, les 2 suivants ceux-ci au numéro de verset et les 2 suivants ceux-ci les numéro de mots.

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
 On peut remplacer le .valeur par .scribe pour savoir quel scribe a écrit le mot en question.
 
 Les mots sont classées par espace ou retour à la ligne.
 */


class data_to_tex {
    
    boolean printedMode; //à modifier dans l'autre fichier.
    
    ArrayList<ArrayList<ArrayList<mot>>> texte = new ArrayList<ArrayList<ArrayList<mot>>>();
    String nom = "";
    Font font;
    ArrayList<ArrayList<ArrayList<mot>>> corrections = new ArrayList<ArrayList<ArrayList<mot>>>();

    public data_to_tex(ArrayList<ArrayList<ArrayList<mot>>> given_texte, ArrayList<ArrayList<ArrayList<mot>>> given_corrections, String nom, boolean printedMode) throws DocumentException, IOException {
        texte = given_texte;
        this.nom = nom;
        creerUneFont();
        corrections = given_corrections;
        
        if(printedMode) {}
    }
    
   void creerUneFont() throws DocumentException, IOException {
       BaseFont koine = BaseFont.createFont("/Users/gustavberloty/Library/Fonts/KoineGreek.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
       koine.setSubset(true);
       font = new Font(koine, 13f);
    }

    public void generer() throws IOException
    {
    	int relativeSize = 1; // 1 is the max size, O.5 50% of the max size, etc…
    		
        Document document = new Document();
        try
        {
            // PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nom + ".pdf"));
            /*ParagraphBorder border = new ParagraphBorder();
            writer.setPageEvent(border);*/
            document.open();
            
            class Longerline {
                int page;
                int line;
                
                public Longerline(int p, int l) {
                    this.page = p;
                    this.line = l;
                }
                
                public String getText(ArrayList<ArrayList<ArrayList<mot>>> texte) {
                    return texte.get(page).get(line).get(0).valeur;
                }
                
                public float getWidth(PdfContentByte pcb, ArrayList<ArrayList<ArrayList<mot>>> texte) {
                    return pcb.getEffectiveStringWidth(getText(texte), true);
                }
                
                public String takeOffTexTag(String words) {
                	return (words.replaceAll("\\\\textoverline\\{", "")).replaceAll("}", "");
                }
            }
            
            Longerline maxLine = new Longerline(1,1);
            
            // Pour trouver la ligne la plus longue du manuscrit
                for(int i=1; i < texte.size(); i++){
                    for(int j = 1; j < texte.get(i).size(); j++) {
                        if(maxLine.takeOffTexTag(texte.get(i).get(j).get(0).valeur).length() > maxLine.takeOffTexTag(maxLine.getText(texte)).length()){
                            maxLine.page = i;
                            maxLine.line = j;
                        }
                    }
                }
             
            // Le nombre de caractère maximum qui ait été trouvé sur une ligne du manuscrit.
            int maxLength = maxLine.takeOffTexTag(maxLine.getText(texte)).length();
            
            // Ci-dessous : ne marche pas à cause des balises tex.
            System.out.println("La ligne la plus longue trouvée dans ce manuscrit est à la page " + maxLine.page + " et à la ligne " + maxLine.line + ".\nVoici ce qui est écrit:\n" + maxLine.takeOffTexTag(maxLine.getText(texte)) + "\nCette ligne contient " + maxLength + " caractères qui ont été comptés.");
            
            String titre = nom.substring(0, 2) + " " + nom.substring(4, 7);
            
            String str = "% %Source :\n%Gustav Berloty (author of this file),\n%Alan Bunning (transcripted data and greek font, http://greekcntr.org/),\n%Charles Lang Freer (original manuscript),\n%and the One who created that manuscript …\n%This file is copyright © 2020 by Gustav Berloty released under the CC BY-NC-SA 4.0 licence :\n%https://creativecommons.org/licenses/by-nc-sa/4.0/\n%The greek font \"KoineGreek\" is copyright © 2019 by Alan Bunning released under the\n%Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License\n%(CC BY-NC-ND 4.0).\n\n% !TEX encoding = UTF-8 Unicode\n \\documentclass[a4paper, 12pt]{article}\n \\usepackage[french]{babel}\n\\usepackage{pifont}\n \\usepackage{fontspec}\n \\usepackage{lmodern}\n \\usepackage{titlesec}\n \\usepackage{array, longtable}\n \\footnotesize\n\\usepackage{verbatim}\n \\title{``" + titre + "''}\n \\font\\myfont=cmr11 at 11pt\n \\author{{\\myfont Source : }\\\\ \n{\\myfont Gustav Berloty (author of this file),} \\\\ \n{\\myfont Alan Bunning (transcripted data and greek font, http://greekcntr.org/), }\\\\ \n{\\myfont Charles Lang Freer (original manuscript), }\\\\ \n{\\myfont and the One who created that manuscript …}\\\\ \n{\\myfont This file is copyright © 2020 by Gustav Berloty released under the CC BY-NC-SA 4.0 licence : }\\\\ \n{\\myfont https://creativecommons.org/licenses/by-nc-sa/4.0/ }\\\\ \n{\\myfont The greek font \"KoineGreek\" is copyright © 2019 by Alan Bunning released under the }\\\\ \n{\\myfont Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License }\\\\ \n{\\myfont (CC BY-NC-ND 4.0).}}\n\\usepackage{layout}\n \\usepackage[nomarginpar, margin=0.7in]{geometry}\n \\usepackage{graphicx}\n%\\newcommand{\\tailleDeLaPolice}{14.5}\n%\\usepackage[fontsize=\\tailleDeLaPolice pt]{scrextend}%\n\n  %\\def\\longueurFinalN#1{\\FPdiv\\result{11}{0.7}\n%\\FPdiv\\result{#1}{\\result}\n%\\FPround\\result{\\result}{2}\n%\\num{\\result}}\n\n\\pagestyle{plain}\n\\newfontfamily{\\fontZ}{Verdana}\n\\titleformat{\\part}[block]{\\fontZ\\Large\\filcenter}{}{1em}{}\n\\titlespacing*{\\part}{0pt}{-30pt}{20pt}\n\n\\newcommand{\\newPart}[1]{\n\\part*{#1}\n\\markright{}\n\\phantomsection}\n\n\n\\newcommand{\\newSection}[1]{\n\\section*{\\foreignlanguage{greek}{#1}}\n\\markright{}\n\\phantomsection\n\\addcontentsline{toc}{section}{\\foreignlanguage{greek}{#1}}}\n\n % pour afficher dans le \"toc\", des lignes en pointillées entre les chapitres des livres et leur numéro de page.\n\\usepackage{tocloft}\n\\renewcommand{\\cftsecleader}{\\cftdotfill{\\cftdotsep}}\n\n % suppress page number in toc for parts\n\\cftpagenumbersoff{part}\n\\usepackage{sectsty}\\sectionfont{\\normalfont\\large\\underline}\n \\usepackage{polyglossia}\n \\usepackage{xcolor}\n \\definecolor{corrected}{rgb}{0.0, 0.62, 0.38}\n \\definecolor{error}{rgb}{0.8, 0.25, 0.33}\n \\definecolor{no_change}{rgb}{0, 0, 0}\n \\setmainlanguage{french}\n \\setotherlanguage{greek}\n \\newfontfamily\\greekfont{KoineGreek}\n\\newcommand\\Pheader{\\rule[-2ex]{0pt}{5ex}}\n\\newsavebox\\TBox\n\\def\\textoverline#1{\\savebox\\TBox{#1}%\n\\makebox[0pt][l]{#1}\\rule[1.1\\ht\\TBox]{\\wd\\TBox}{0.7pt}} % prendre la valeur de \\thelongueurFinalN{\\tailleDeLaPolice}\n % amélioration : ajouter un \"padding\" sur le tabular + agrandir le tabular et son contenu.\n\n\\usepackage{pageslts}\n \\usepackage{cancel}\n \\renewcommand{\\CancelColor}{\\color{red}}\n \\usepackage{fancyhdr}\n\\makeatletter\n\\newcommand{\\nospace}[1]{\\nofrench@punctuation\\texttt{#1}\\french@punctuation}\n\\makeatother\n\\let\\oldtabular\\tabular\\renewcommand{\\tabular}{\\large\\selectfont\\oldtabular} %fontsize{17pt}{20.5pt}\n\n\\usepackage[hidelinks]{hyperref}\n\n\\newcounter{gospelbook}\n\\setcounter{gospelbook}{1}\n\\newcommand{\\mygospelbook}[1]\n{\\setcounter{gospelchapter}{1}\\phantomsection\\addcontentsline{toc}{part}{#1}#1}\n\n\\newcommand{\\agospelbook}[1]{\\addtocontents{toc}{\\protect\\newpage}\\mygospelbook{#1}}\n\n\\newcounter{gospelchapter}\n\\newcommand{\\mygospelchapter}{\\phantomsection\\addcontentsline{toc}{section}{\\thegospelchapter}\\LARGE\\bfseries\\thegospelchapter\\refstepcounter{gospelchapter}}\n\n % for finals N\n\\newcommand{\\finalN}[1]{\\textoverline{#1~~}}\n \\newcommand{\\finalNedit}[3]{\\color{#3}{\\textoverline{\\color{#2}{#1}~~} }}\n\\renewcommand*{\\thepage}{\\large\\arabic{page}}\n\n\n\\begin{document}\n\\renewcommand{\\contentsname}{Sommaire}\n %\\layout\n \\maketitle % affiche le nom du manuscrit.\n\\pagenumbering{roman}\n\\thispagestyle{empty}\\clearpage\\setcounter{page}{1}\n\\newpage\n\\foreignlanguage{greek}{\\tableofcontents}\n\\clearpage\\pagenumbering{arabic}\\setcounter{page}{1}%doesn't seem to work\n\\newgeometry{margin=0.7in, hmargin = 0.0in, layouthoffset = -1.9pt, top = 0.0in}\n\\clearpage\n\\setlength\\arrayrulewidth{1pt}\n\\Large\n\\renewcommand\\arraystretch{0.82}\n\\begin{longtable}{cc|l|cc}\n";
            BufferedWriter writer2 = new BufferedWriter(new FileWriter("/Users/gustavberloty/Documents/Github/JavaXML/generated files/tex files/" + nom + ".tex"));
            writer2.write(str);
            
            String livre_01 = "ευαγγελιον κατα μαθθαιον";
            String livre_02 = "ευαγγελιον κατα μαρκον";
            String livre_03 = "ευαγγελιον κατα λουκαν";
            String livre_04 = "ευαγγελιον κατα ιωαννην";
            
            String chapitre_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(2, 4)));
            String chapitre_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(2, 4)));
            String verset_actuel_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(4, 6)));
            String verset_actuel_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(4, 6)));
            String numero_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(6, 8)));
            String numero_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(6, 8)));
            
            String previous_livre = "";
            boolean first_book = true;
            
            // Rectifications scribales computed.
            String last_book = "";
            String last_chapter = "";
            String rectifications_scribales = "";
            rectifications_scribales = "\\end{longtable}\n\\restoregeometry\n\\newpage\n\\newPart{RECTIFICATIONS SCRIBALES}\n\\addtolength{\\hoffset}{-20pt}"; // corrections.size() pour en avoir le nombre.
            for (int j = 0; j < corrections.size(); j++) {
                String[] etape = new String[corrections.get(j).size()];
                int numeroDeReference = Integer.parseInt(corrections.get(j).get(0).get(0).numero);
                int lastNumeroDeReference = 0;
                int posManuscript = corrections.get(j).get(0).get(0).posManuscript;
                int lastPosManuscript = 0;
                boolean mot_coupe = false;
                for (int k = 0; k < corrections.get(j).size(); k++) {
                    etape[k] = "";
                    for (int l = 0; l < corrections.get(j).get(k).size(); l++) {
                    	mot_coupe = corrections.get(j).get(k).get(l).coupe;
                        if (corrections.get(j).get(k).get(l).valeur.equals("")) {
                            etape[k] += corrections.get(j).get(k).get(l).valeur;
                        }
                        else if (etape[k].length() > 1) {
                            etape[k] += " " + corrections.get(j).get(k).get(l).valeur;
                        }
                        else { // Pour ne pas qu'on ait un espace devant le premier mot.
                            etape[k] += corrections.get(j).get(k).get(l).valeur;
                        }
                        if (k == corrections.get(j).size() - 1 && l == corrections.get(j).get(k).size() - 1) {
                        	lastNumeroDeReference = Integer.parseInt(corrections.get(j).get(k).get(l).numero);
                        }
                        if (k == 0 && l == corrections.get(j).get(k).size() - 1) {
                        	lastPosManuscript = corrections.get(j).get(k).get(l).posManuscript;
                        }
                    }
                }
                
                int additionalWord = lastPosManuscript - posManuscript;
                
                // Si mot coupé on ajoute 1 à additionalWord.
                if (mot_coupe) additionalWord++;

                String reference_du_mot_precedent = "";
                String reference_du_mot_suivant = "";
                String mot_precedent = "";
                String mot_suivant = "";
                // Faire une fonction de recherche de mot pour trouver le précédent et le suivant.
                if(getWordIndex(numeroDeReference)-1 != -2) {
                    if (getWordIndex(numeroDeReference)-1 > -1) {
                        mot_precedent = texte.get(0).get(0).get(getWordIndex(numeroDeReference)-1).valeur;
                        reference_du_mot_precedent = texte.get(0).get(0).get(getWordIndex(numeroDeReference)-1).numero;
                    }
                    else { // Dans le cas où le premier mot rectifié est le premier mot du manuscrit.
                        mot_precedent = null;
                        reference_du_mot_precedent = Integer.toString(numeroDeReference);
                    }
                    if (getWordIndex(numeroDeReference) + additionalWord + 1 < texte.get(0).get(0).size()) {
                        mot_suivant = texte.get(0).get(0).get(getWordIndex(numeroDeReference) + additionalWord + 1).valeur;
                        reference_du_mot_suivant = texte.get(0).get(0).get(getWordIndex(numeroDeReference) + additionalWord + 1).numero;
                    }
                    else { // Dans le cas où le premier mot rectifié est le dernier mot du manuscrit.
                        mot_suivant = null;
                        reference_du_mot_suivant = Integer.toString(numeroDeReference + additionalWord);
                    }

                    if (!last_book.equals(reference_du_mot_precedent.substring(0, 2))) {
                        String livre_actuel = "";
                        if (reference_du_mot_precedent.substring(0, 2).equals("01")) livre_actuel = livre_01;
                        else if (reference_du_mot_precedent.substring(0, 2).equals("02")) livre_actuel = livre_02;
                        else if (reference_du_mot_precedent.substring(0, 2).equals("03")) livre_actuel = livre_03;
                        else if (reference_du_mot_precedent.substring(0, 2).equals("04")) livre_actuel = livre_04;
                        rectifications_scribales += " \n\n\\newSection{" + livre_actuel + "} \n";
                        last_book = reference_du_mot_precedent.substring(0, 2);
                    }
                    
                    String reference = "\n\n\\normalsize\\textbf{\\nospace{" + reference_du_mot_precedent.substring(2, 4) + ":" + reference_du_mot_precedent.substring(4, 6) + "}}\\footnotesize.(" + reference_du_mot_precedent.substring(6, 8);
                    if (!reference_du_mot_precedent.substring(2, 4).equals(reference_du_mot_suivant.substring(2, 4))) {
                        reference += ")\\normalsize-\\textbf{\\nospace{" + reference_du_mot_suivant.substring(2, 4) + ":" + reference_du_mot_suivant.substring(4, 6) + "}}\\footnotesize.(" + reference_du_mot_suivant.substring(6, 8) + ") \n\\normalsize";
                    }
                    else if (reference_du_mot_precedent.substring(4, 6).equals(reference_du_mot_suivant.substring(4, 6))) {
                        reference += "-" + reference_du_mot_suivant.substring(6, 8) + ") \n\\normalsize";
                    }
                    else {
                        reference = reference.substring(0, reference.length()-17);
                        reference += "-\\textbf{\\nospace{" + reference_du_mot_suivant.substring(4, 6) + "}}\\hspace{1.5em}\n\\normalsize";
                    }
                    
                    // Mettre en forme la rectification.
                    for (int k = etape.length - 1; k >= 0; k--) {
                        if(k-1 >= 0) {
                            String[] s = {etape[k], etape[k-1]};
                            String[] etapes_mis_en_forme = detect_common_bloc(s);
                            etape[k] = etapes_mis_en_forme[0];
                            etape[k-1] = etapes_mis_en_forme[1];
                        }
                    }
                    for (int k = etape.length - 1; k >= 0; k--) {
                        etape[k] = etape[k].replaceAll("(\\\\foreignlanguage\\{greek\\}\\{.*)(°.+°)(.*\\})", "\\\\textoverline{$1$2$3}");
                        etape[k] = etape[k].replaceAll("\\\\foreignlanguage\\{greek\\}\\{°\\}", "");
                        etape[k] = etape[k].replaceAll("°", "");
                    }
                    
                    String a_correction = reference + " \\foreignlanguage{greek}{" + mot_precedent + "} " + etape[etape.length-1] + " \\foreignlanguage{greek}{" + mot_suivant + "}";
                    for (int l = etape.length - 2; l >= 0; l--) {
                        a_correction += " \n\\ding{222} \\foreignlanguage{greek}{" + mot_precedent + "} " + etape[l] + " \\foreignlanguage{greek}{" + mot_suivant + "}";
                    }
                    texte.get(getPageIndex(posManuscript)).get(getLineIndex(posManuscript)).get(getWordIndex2(posManuscript)).valeur = etape[0];
                    for (int i = posManuscript + 1; i <= lastPosManuscript; i++) {
                    	texte.get(getPageIndex(i)).get(getLineIndex(i)).get(getWordIndex2(i)).valeur = "";
                    }
                    boolean normalCase = true;
                    if (additionalWord > 0) normalCase = false;
                    // rewriteTheLine(getPageIndex(posManuscript), getLineIndex(posManuscript), normalCase, getWordIndex2(lastPosManuscript), additionalWord);
                    System.out.println(texte.get(getPageIndex(posManuscript)).get(getLineIndex(posManuscript)).get(getWordIndex2(posManuscript)).valeur);	// On met à jour le mot dans le texte grec en lui-même.
                    a_correction = a_correction.replaceAll("(.)\u0305", "\\\\finalN{$1} ");
                    rectifications_scribales += a_correction;
                }
            }
            
            for (int i=1; i < texte.size(); i++) {
                
                /*document.newPage();
                PdfPCell header = new PdfPCell(new Phrase("Livre chapitre"));
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                PdfPTable body = new PdfPTable(1);
                PdfPTable corpsCorps = new PdfPTable(3);
                corpsCorps.setWidths(new int[]{ 1, 20, 1});*/
                
                String livre_actuel = "";
                
                String chapitre_actuel = Integer.toString(Integer.parseInt((texte.get(i).get(0).get(0).numero).substring(2, 4)));
                String verset_actuel = Integer.toString(Integer.parseInt((texte.get(i).get(0).get(0).numero).substring(4, 6)));
                
                // System.out.println();
                if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("01")) livre_actuel = livre_01;
                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("02")) livre_actuel = livre_02;
                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("03")) livre_actuel = livre_03;
                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("04")) livre_actuel = livre_04;
                
                if (first_book) {
                    writer2.write("\\cline{3-3} \\\\ [-0.87em]\n\\multicolumn{5}{c}{\\mygospelbook{\\foreignlanguage{greek}{" +  livre_actuel + "}} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-0.97em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{3-3} \\\\\n\\cline{3-3}\n&  & &  & \\\\ [-0.9em]\n");
                    first_book = false;
                }
                else if (!previous_livre.equals(livre_actuel)) {
                    writer2.write("\\cline{3-3} \\\\ [-0.87em]\n\\multicolumn{5}{c}{\\agospelbook{\\foreignlanguage{greek}{" +  livre_actuel + "}} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-0.97em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{3-3} \\\\\n\\cline{3-3}\n & &  &  & \\\\ [-0.9em]\n");
                }
                else {
                    writer2.write("\\cline{3-3} \\\\ [-0.87em]\n\\multicolumn{5}{c}{\\foreignlanguage{greek}{" +  livre_actuel + "} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-0.97em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{3-3} \\\\\n\\cline{3-3}\n & &  &  & \\\\ [-0.9em]\n");
                }
                
                previous_livre = livre_actuel;
                
                for (int j = 1; j < texte.get(i).size(); j++) { // On créer de nouvelles (3) cellules de tableau pour chaque ligne et on remplit ces cellules par le numéro de verset si présent et le texte de la ligne.
                    //corpsCorps.addCell(Integer.toString(j+1));
                    // corpsCorps.addCell("phrase très très longue eeeeeeeeeeeeeeeeeeeeeeeeeee test");
                    // System.out.println(texte.get(i).get(j).get(0).valeur);
                    //corpsCorps.addCell(Integer.toString(j+1));
                    
                    // On remplace le macron sur le dernier caractère d'une ligne par un macron correspondant.
                    String words = (texte.get(i).get(j).get(0).valeur).replaceAll("(.)\u0305$", "\\\\finalN{$1}");
                    // System.out.println("\\finnalN{test}");
                    
                    String prev_CMG = chapitre_mot_a_gauche;
                    chapitre_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(2, 4)));
                    String prev_CMD = chapitre_mot_a_droite;
                    chapitre_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(2, 4)));
                    String prev_VAG = verset_actuel_a_gauche;
                    verset_actuel_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(4, 6)));
                    String prev_VAD = verset_actuel_a_droite;
                    verset_actuel_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(4, 6)));
                    String prev_MAG = numero_mot_a_gauche;
                    numero_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(6, 8)));
                    String prev_MAD = numero_mot_a_droite;
                    numero_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(6, 8)));
                    
                    // ------------------------
                    
                    String LEFTincrementGospelChapterTeXNumber = " ";
                    int k = Integer.parseInt(prev_CMD);
                    while ( k < Integer.parseInt(chapitre_mot_a_gauche) - 1) {
                        LEFTincrementGospelChapterTeXNumber += "\\refstepcounter{gospelchapter} ";
                        k++;
                    }
                    
                    // I know it's a bit strange
                    String RIGHTincrementGospelChapterTeXNumber = " ";
                    k = Integer.parseInt(chapitre_mot_a_gauche);
                    while (k < Integer.parseInt(chapitre_mot_a_droite) - 1) {
                        RIGHTincrementGospelChapterTeXNumber += "\\refstepcounter{gospelchapter} ";
                        k++;
                    }
                    
                    if (!chapitre_mot_a_gauche.equals(prev_CMD) && !chapitre_mot_a_droite.equals(chapitre_mot_a_gauche)) {
                        writer2.write(LEFTincrementGospelChapterTeXNumber+"\\mygospelchapter &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &"+RIGHTincrementGospelChapterTeXNumber+"\\mygospelchapter \\\\\n");
                        continue;
                    }
                    if (!(chapitre_mot_a_gauche.equals(prev_CMD)) || (i == 1 && j == i) ) { // La deuxième condition signifie, si on est sur la 1ère ligne de la 1ère page.
                        // on affiche le numéro du verset à gauche
                        writer2.write(LEFTincrementGospelChapterTeXNumber+"\\mygospelchapter &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &  \\\\\n");
                        continue;
                    }
                    if (!chapitre_mot_a_droite.equals(chapitre_mot_a_gauche)) {
                        // on affiche le numéro du verset à droite
                        writer2.write("& " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &"+RIGHTincrementGospelChapterTeXNumber+"\\mygospelchapter \\\\\n");
                        continue;
                    }
                    // ------------------------
                    
                    if (!(verset_actuel_a_gauche.equals(prev_VAD))) {
                        // on affiche le numéro du verset à gauche
                        writer2.write("\\textbf{" + verset_actuel_a_gauche + "} &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " & \\\\\n");
                        continue;
                    }
                    if (!verset_actuel_a_droite.equals(verset_actuel_a_gauche)) {
                        // on affiche le numéro du verset à droite
                        writer2.write("& " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " & \\textbf{" + verset_actuel_a_droite + "} \\\\\n");
                        continue;
                    }
                    writer2.write("& " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &  \\\\\n");
                }
                
                String rectScr = "";
                
                writer2.write("[0.2em]\n\\cline{3-3}\n\n" + rectScr + "\\newpage\n");
                
                /*PdfPCell corps = new PdfPCell(corpsCorps);
                PdfPCell footer = new PdfPCell(new Phrase("p." + (i+1)));
                footer.setHorizontalAlignment(Element.ALIGN_CENTER);
                footer.setVerticalAlignment(Element.ALIGN_MIDDLE);
                
                body.addCell(header);
                body.addCell(corps);
                body.addCell(footer);
                
                document.add(body);*/
                
            }
            
            writer2.write(rectifications_scribales);
            writer2.write("\n\n\\end{document}");
            writer2.close();
            // grec
            
            //document.close();

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    int getWordIndex(int word_number) {
        for (int i = 0; i< texte.get(0).get(0).size(); i++) {
            if (Integer.parseInt(texte.get(0).get(0).get(i).numero) == word_number) {
                return i;
            }
        }
        return -1;
    }
    
    
   int getWordIndex2(int word_number) {
    	String w = Integer.toString(word_number);
    	if (w.length() == 8) { System.out.println("numéro : " + w + "\nword: " + Integer.parseInt(w.substring(6, 8))); return Integer.parseInt(w.substring(6, 8)); }
    	else if (w.length() == 7) { System.out.println("numéro : " + w + "\nword: " + Integer.parseInt(w.substring(5, 7))); return Integer.parseInt(w.substring(5, 7)); }
    	else { System.out.println("numéro : " + w + "\nword: " + Integer.parseInt(w.substring(4, 6))); return Integer.parseInt(w.substring(4, 6)); }
    }
    
    int getLineIndex(int word_number) {
    	String w = Integer.toString(word_number);
    	if (w.length() == 8) { System.out.println("numéro : " + w + "\nline: " + Integer.parseInt(w.substring(4, 6))); return Integer.parseInt(w.substring(4, 6)); }
    	else if (w.length() == 7) { System.out.println("numéro : " + w + "\nline: " + Integer.parseInt(w.substring(3, 5))); return Integer.parseInt(w.substring(3, 5)); }
    	else { System.out.println("numéro : " + w + "\nline: " + Integer.parseInt(w.substring(2, 4))); return Integer.parseInt(w.substring(2, 4)); }
    }
    
    int getPageIndex(int word_number) {
	    String w = Integer.toString(word_number);
    	if (w.length() == 8) { System.out.println("numéro : " + w + "\npage: " + Integer.parseInt(w.substring(0, 3))); return Integer.parseInt(w.substring(0, 3)); }
    	else if (w.length() == 7) { System.out.println("numéro : " + w + "\npage: " + Integer.parseInt(w.substring(0, 2))); return Integer.parseInt(w.substring(0, 2)); }
    	else { System.out.println("numéro : " + w + "\npage: " + Integer.parseInt(w.substring(0, 1))); return Integer.parseInt(w.substring(0, 1)); }
    }
    
    void rewriteTheLine (int pageNumber, int lineNumber, boolean normalCase, int exception, int addition) {
    	texte.get(pageNumber).get(lineNumber).get(0).valeur = "";
    	for (int i = 1; i < texte.get(pageNumber).get(lineNumber).size(); i++) {
    		texte.get(pageNumber).get(lineNumber).get(0).valeur += texte.get(pageNumber).get(lineNumber).get(i).valeur;
    		if (!normalCase && (exception - addition < i && i <= exception)) System.out.print("");
    		else texte.get(pageNumber).get(lineNumber).get(0).valeur += " ";
    	}
    	texte.get(pageNumber).get(lineNumber).get(0).valeur = (texte.get(pageNumber).get(lineNumber).get(0).valeur).substring(0, (texte.get(pageNumber).get(lineNumber).get(0).valeur).length()-1);
    }
    
    /*
     String text1 = "ABCDELMN";
     String text2 = "ABCFGLMN";
     DiffMatchPatch dmp = new DiffMatchPatch();
     LinkedList<Diff> diff = dmp.diffMain(text1, text2, false);
     // résultat :
     [Diff(EQUAL,"ABC"), Diff(DELETE,"DE"), Diff(INSERT,"FG"), Diff(EQUAL,"LMN")]
    */
    
    // Ne gère pas si il y a une différence entre nominasacras. voir formatNominaSacra() dans get_data.java.
    
    String[] detect_common_bloc(String[] s) {
        diff_match_patch dmp = new diff_match_patch();
        boolean  crossOption = false;
        ArrayList<String> returnBlocsAL = new ArrayList<>();
        LinkedList<diff_match_patch.Diff> diff = dmp.diff_main(s[0], s[1]);
        String s0 = "";
        String s1 = "";
            for (int i = 0; i < diff.size(); i++) {
                String new_value = diff.get(i).text;
                String first_value = diff.get(i).text;
                diff_match_patch.Operation operation = diff.get(i).operation;
                boolean[] equal = new boolean[2];
                boolean[] insert = new boolean[2];
                boolean[] delete = new boolean[2];
                String[] color = new String[2];
                int m = i;
                if (i+1 < diff.size() && diff.get(i+1).text.equals("\u0305")) {
                    for (int g = 0; g < 2; g++) {
                        if (diff.get(m).operation == diff_match_patch.Operation.INSERT) {
                            if (m % 2 == 0) {
                                color[g] = "error";
                            }
                            else {
                                color[g] = "corrected";
                            }
                            insert[g] = true;
                        }
                        else if (diff.get(m).operation == diff_match_patch.Operation.DELETE) {
                            if (m % 2 != 0) {
                                color[g] = "corrected";
                            }
                            else {
                                color[g] = "error";
                            }
                            delete[g] = true;
                        }
                        else if (diff.get(m).operation == diff_match_patch.Operation.EQUAL) {
                            color[g] = "no_change";
                            equal[g] = true;
                        }
                        m++;
                    }
                    char new_value_LastChar = new_value.charAt(new_value.length()-1);
                    if (new_value.length() == 1) {
                         new_value = "\\finalNedit{" + new_value_LastChar + "}{" + color[0] + "}{" + color[1] + "}";
                    }
                    else {
                         String new_value_firstPart = new_value.substring(0, new_value.length());
                         new_value = new_value_firstPart + "\\finalNedit{" + new_value_LastChar + "}{" + color[0] + "}{" + color[1] + "}";
                    }
                    i++;
                }
                if (operation == diff_match_patch.Operation.INSERT) {
                    if (delete[1]) {
                        s0 += "\\foreignlanguage{greek}{" + new_value + "}"; // peut-être faudra-t-il décaler vers la gauche dans certain cas.
                        s1 += "{\\color{corrected}{\\foreignlanguage{greek}{" + first_value + "}}}";
                    }
                    else if (equal[1]) {
                        s0 += "\\foreignlanguage{greek}{\\finalNedit{}{" + color[0] + "}{" + color[1] + "}}";
                        s1 += "\\foreignlanguage{greek}{" + new_value + "}";
                    }
                    else if (insert[1]) {
                        s1 += "\\foreignlanguage{greek}{" + new_value + "}";
                    }
                    else {
                        s1 += "{\\color{corrected}{\\foreignlanguage{greek}{" + first_value + "}}}";
                    }
                }
                else if (operation == diff_match_patch.Operation.DELETE) {
                    if (equal[1]) {
                        s0 += "\\foreignlanguage{greek}{" + new_value + "}";
                        s1 += "\\foreignlanguage{greek}{\\finalNedit{}{" + color[0] + "}{" + color[1] + "}}"; // peut-être faudra-t-il décaler vers la gauche dans certain cas.
                    }
                    else if (insert[1]) {
                        s0 += "{\\color{error}{\\foreignlanguage{greek}{" + first_value + "}}}";
                        s1 += "\\foreignlanguage{greek}{\\finalNedit{}{" + color[0] + "}{" + color[1] + "}}"; // peut-être faudra-t-il décaler vers la gauche dans certain cas.
                    }
                    else if (delete[1]){
                        s0 += "\\foreignlanguage{greek}{" + new_value + "}";
                    }
                    else {
                        if (crossOption) {
                            s0 += "\\xcancel{\\foreignlanguage{greek}{" + first_value + "}}";
                        }
                        else {
                            s0 += "{\\color{error}{\\foreignlanguage{greek}{" + first_value + "}}}";
                        }
                    }
                }
                else if (operation == diff_match_patch.Operation.EQUAL) {
                    if (delete[1]){
                        s0 += "\\foreignlanguage{greek}{" + new_value + "}";
                        s1 += "\\foreignlanguage{greek}{" + first_value + "}";
                    }
                    else if (insert[1]) {
                        s0 += "\\foreignlanguage{greek}{" + first_value + "}";
                        s1 += "\\foreignlanguage{greek}{" + new_value + "}";
                    }
                    else if (equal[1]) {
                        s0 += "\\foreignlanguage{greek}{" + new_value + "}";
                        s1 += "\\foreignlanguage{greek}{" + new_value + "}";
                    }
                    else {
                        s0 += "\\foreignlanguage{greek}{" + first_value + "}";
                        s1 += "\\foreignlanguage{greek}{" + first_value + "}";
                    }
                }
            }
        System.out.println(diff);
        returnBlocsAL.add(s0);
        returnBlocsAL.add(s1);
        String[] returnBlocs = new String[returnBlocsAL.size()];
        returnBlocs = returnBlocsAL.toArray(returnBlocs);
        return returnBlocs;
    }
    
}

