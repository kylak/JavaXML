import java.io.FileNotFoundException;import java.io.FileOutputStream;import com.itextpdf.text.Document;import com.itextpdf.text.DocumentException;import com.itextpdf.text.Paragraph;import com.itextpdf.text.pdf.PdfWriter;import com.itextpdf.text.Font;import com.itextpdf.text.pdf.BaseFont;import java.io.IOException;import com.itextpdf.text.Element;import com.itextpdf.text.pdf.PdfPCell;import com.itextpdf.text.pdf.PdfPTable;import com.itextpdf.text.Phrase;import com.itextpdf.text.pdf.PdfContentByte;import java.io.BufferedWriter;import java.io.FileWriter;import java.util.ArrayList;import java.util.Arrays;/*    6. Fixer la taille du tableau selon les dimensions du codex ? (Ainsi, nous ne devrions plus avoir de problème en Matthieu 28:15-20, Marc 16:17-20).    7. Faire du stretching par la largeur du tableau de façon à prendre la bonne partie de la page ?    (S'assurer qu'il n'y ait pas de souci avec la page la plus large du fichier.)/*/* Voici comment accèder à l'ArrayList texte : Pour avoir : 1- la p-ième page du manuscrit: texte.get(p).get(0).get(0).valeur; 2- la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(0).valeur; 3- le n-ième mot de la p-ième page du manuscrit: texte.get(p).get(0).get(n).valeur; 4- le n-ième mot de la l-ième ligne de la p-ième page du manuscrit: texte.get(p).get(l).get(n).valeur; 5- le n-ième mot du manuscrit: texte.get(0).get(0).get(n).valeur; 6- le n-ième mot de la l-ième ligne du manuscrit: texte.get(0).get(l).get(n).valeur;  7- la l-ième ligne du manuscrit: texte.get(0).get(l).get(0).valeur; 8- tout le manuscrit: texte.get(0).get(0).get(0).valeur;  Les mots sont classées par espace ou retour à la ligne.  Pour la prochaine fois : 1. Enregistrer les corrections scribales et les afficher sur une page en fin du pdf. 3. Mettre le sommaire de chaque livre côte à côte sur la même page, et mettre en bas de cette page de sommaire la page de correction scribale et son numéro de page. 3. Ajouter une légende (en quelle lange ? anglais, koinè, français ?) pour la mise en page (que siginife les chiffres, en gras, plus grands, les overlines, etc… ) ? 4. Ajouter une page informative, genre liste des versets non présents dans ce manuscrit et quels sont les corrections qui ont étés intégrés directement, et les concitoyens, etc… 5. Fixer la taille du tableau selon les dimensions du codex ? (Ainsi, nous ne devrions plus avoir de problème en Matthieu 28:15-20, Marc 16:17-20). 6. Faire du stretching par la largeur du tableau de façon à prendre la bonne partie de la page ? (S'assurer qu'il n'y ait pas de souci avec la page la plus large du fichier.) 7. (Détails) Rendre la taille des numéros de page plus grand ? 8. Faire en sorte que lorsque l'on clique sur un "lien" du "toc", on arive sur la baseline de la ligne du dessus et non sur celle vers laquelle le lien pointe, ainsi, on devrait pouvoir voir intégralement le numéro du chapitre, il ne devrait plus être coupé. "Petite" question : "grand" padding-top seulement si nominasacra ? 9. Nettoyer le dossier, et rédiger le code en anglais?w 10. Avoir un autre format que pdf pour pouvoir faire des annotations et copiers-collers sur les mots et aussi trouver la définition d'un mot. 11. Les concitoyens.  Ce que l'on pourrait améliorer sur la structure XML : - Mettre les dimensions du codex dans des balises spécifiques pour pouvoir les récupérer et les utiliser de façon plus sûr, cela permettrait d'avoir les dimensions de notre tableau tex selon celles dans les données XML. - Ajouter les "blancs" (ou espace) présent dans le manuscrit (quitte à ne pas les afficher pour certain), cela permettrait d'avoir un résultat encore plus proche du codex, conservant l'"aération" de la mise en page, autrement dit la mise en page, cela peut s'avérer d'autant plus pratique si on souhaite ne pas ajouter d'espace entre les mots, en les distinguant par leur couleur. De toutes les manières, ajouter les blancs, devrait, si je ne me trompe pas, rendre la mise en page plus esthétique. - Avoir les titres que les manuscrits donnent eux-mêmes aux livres. */class CreerPDF {    ArrayList<ArrayList<ArrayList<mot>>> texte = new ArrayList<ArrayList<ArrayList<mot>>>();    String nom = "";    Font font;    public CreerPDF(ArrayList<ArrayList<ArrayList<mot>>> given_texte, String nom) throws DocumentException, IOException {        texte = given_texte;        this.nom = nom;        creerUneFont();    }       void creerUneFont() throws DocumentException, IOException {       BaseFont koine = BaseFont.createFont("/Users/gustavberloty/Library/Fonts/KoineGreek.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);       koine.setSubset(true);       font = new Font(koine, 13f);    }    public void generer() throws IOException    {        Document document = new Document();        try        {            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(nom + ".pdf"));            /*ParagraphBorder border = new ParagraphBorder();            writer.setPageEvent(border);*/            document.open();                        class Longerline {                int page;                int line;                                public Longerline(int p, int l) {                    this.page = p;                    this.line = l;                }                                public String getText(ArrayList<ArrayList<ArrayList<mot>>> texte) {                    return texte.get(page).get(line).get(0).valeur;                }                                public float getWidth(PdfContentByte pcb, ArrayList<ArrayList<ArrayList<mot>>> texte) {                    return pcb.getEffectiveStringWidth(getText(texte), true);                }            }                        Longerline maxLine = new Longerline(1,1);                        // Pour trouver la ligne la plus longue du manuscrit                for(int i=1; i < texte.size(); i++){                    for(int j = 1; j < texte.get(i).size(); j++) {                        if(texte.get(i).get(j).get(0).valeur.length() > maxLine.getText(texte).length()){                            maxLine.page = i;                            maxLine.line = j;                        }                    }                }                        // Ci-dessous : ne marche pas à cause des balises tex.            // System.out.println("La ligne la plus longue trouvée dans ce manuscrit est à la page " + maxLine.page + " et à la ligne " + maxLine.line + ".\nVoici ce qui est écrit:\n" + maxLine.getText(texte) + "\nCette ligne contient " + maxLine.getText(texte).length() + " caractères qui ont été comptés.");                                    String str = "% !TEX encoding = UTF-8 Unicode\n \\documentclass[a4paper, 11pt]{article}\n \\usepackage[utf8]{inputenc}\n \\usepackage[french]{babel}\n \\usepackage[T1]{fontenc}\n \\usepackage{fontspec}\n \\usepackage{lmodern}\n \\usepackage{array}\n \\usepackage{verbatim}\n  \\font\\myfont=cmr12 at 21pt\n \\title{{\\myfont ``GA 032''}}\n \\usepackage{layout}\n \\usepackage[nomarginpar, margin=0.7in]{geometry}\n \\pagestyle{plain}\n\n % pour afficher dans le \"toc\", des lignes en pointillées entre les chapitres des livres et leur numéro de page.\n\\usepackage{tocloft}\n\\renewcommand{\\cftsecleader}{\\cftdotfill{\\cftdotsep}}\n\n % suppress page number in toc for parts\n\\cftpagenumbersoff{part}\n\n \\usepackage{polyglossia}\n \\setmainlanguage{french}\n \\setotherlanguage{greek}\n \\newfontfamily\\greekfont{KoineGreek}\n\\newcommand\\Pheader{\\rule[-2ex]{0pt}{5ex}}\n\\newsavebox\\TBox\n\\def\\textoverline#1{\\savebox\\TBox{#1}%\n\\makebox[0pt][l]{#1}\\rule[1.1\\ht\\TBox]{\\wd\\TBox}{0.7pt}}\n % amélioration : ajouter un \"padding\" sur le tabular + agrandir le tabular et son contenu.\n\n\\usepackage{pageslts}\n\\usepackage{fancyhdr}\n\\makeatletter\n\\newcommand{\\nospace}[1]{\\nofrench@punctuation\\texttt{#1}\\french@punctuation}\n\\makeatother\n\\let\\oldtabular\\tabular\\renewcommand{\\tabular}{\\large\\selectfont\\oldtabular} %fontsize{17pt}{20.5pt}\n\n\\usepackage[hidelinks]{hyperref}\n\n\\newcounter{gospelbook}\n\\setcounter{gospelbook}{1}\n\\newcommand{\\mygospelbook}[1]\n{\\setcounter{gospelchapter}{1}\\phantomsection\\addcontentsline{toc}{part}{#1}#1}\n\n\\newcommand{\\agospelbook}[1]{\\addtocontents{toc}{\\protect\\newpage}\\mygospelbook{#1}}\n\n\\newcounter{gospelchapter}\n\\newcommand{\\mygospelchapter}{\\phantomsection\\addcontentsline{toc}{section}{\\thegospelchapter}\\LARGE\\bfseries\\thegospelchapter\\refstepcounter{gospelchapter}}\n\n % for finals N\n\\newcommand{\\finalN}[1]{\\textoverline{#1~~}} \n\n\n\\begin{document}\n\\renewcommand{\\contentsname}{Sommaire}\n %\\layout\n \\maketitle % affiche le nom du manuscrit.\n\\pagenumbering{roman}\n\\thispagestyle{empty}\\clearpage\\setcounter{page}{1}\n\\newpage\n\\foreignlanguage{greek}{\\tableofcontents}\n\\clearpage\\pagenumbering{arabic}\\setcounter{page}{1}\n";            BufferedWriter writer2 = new BufferedWriter(new FileWriter("test.tex"));            writer2.write(str);                        String livre_01 = "ευαγγελιον κατα μαθθαιον";            String livre_02 = "ευαγγελιον κατα μαρκον";            String livre_03 = "ευαγγελιον κατα λουκαν";            String livre_04 = "ευαγγελιον κατα ιωαννην";                        String chapitre_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(2, 4)));            String chapitre_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(2, 4)));            String verset_actuel_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(4, 6)));            String verset_actuel_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(4, 6)));            String numero_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(0).get(0).get(0).numero).substring(6, 8)));            String numero_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(1).get(1).get(texte.get(1).get(1).size()-1).numero).substring(6, 8)));                        String previous_livre = "";            boolean first_book = true;                        for (int i=1; i < texte.size(); i++) {                                /*document.newPage();                PdfPCell header = new PdfPCell(new Phrase("Livre chapitre"));                header.setHorizontalAlignment(Element.ALIGN_CENTER);                header.setVerticalAlignment(Element.ALIGN_MIDDLE);                PdfPTable body = new PdfPTable(1);                PdfPTable corpsCorps = new PdfPTable(3);                corpsCorps.setWidths(new int[]{ 1, 20, 1});*/                                String livre_actuel = "";                                String chapitre_actuel = Integer.toString(Integer.parseInt((texte.get(i).get(0).get(0).numero).substring(2, 4)));                String verset_actuel = Integer.toString(Integer.parseInt((texte.get(i).get(0).get(0).numero).substring(4, 6)));                                // System.out.println();                if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("01")) livre_actuel = livre_01;                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("02")) livre_actuel = livre_02;                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("03")) livre_actuel = livre_03;                else if ((texte.get(i).get(1).get(1).numero).substring(0, 2).equals("04")) livre_actuel = livre_04;                                if (first_book) {                    writer2.write("\\clearpage\n\\newpage\n {\n \\setlength\\arrayrulewidth{1pt}\n\\begin{table}\n\\begin{center}\n\\begin{tabular}{ccc|l|ccc}\n\\cline{4-4} \\\\ [-1em]\n\\multicolumn{7}{c}{\\mygospelbook{\\foreignlanguage{greek}{" +  livre_actuel + "}} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-1em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{4-4} \\\\\n\\cline{4-4}\n&  &  & &  &  & \\\\ [-0.9em]\n");                    first_book = false;                }                else if (!previous_livre.equals(livre_actuel)) {                    writer2.write("\\clearpage\n\\newpage\n {\n \\setlength\\arrayrulewidth{1pt}\n\\begin{table}\n\\begin{center}\n\\begin{tabular}{ccc|l|ccc}\n\\cline{4-4} \\\\ [-1em]\n\\multicolumn{7}{c}{\\agospelbook{\\foreignlanguage{greek}{" +  livre_actuel + "}} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-1em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{4-4} \\\\\n\\cline{4-4}\n&  &  & &  &  & \\\\ [-0.9em]\n");                }                else {                    writer2.write("\\clearpage\n\\newpage\n {\n \\setlength\\arrayrulewidth{1pt}\n\\begin{table}\n\\begin{center}\n\\begin{tabular}{ccc|l|ccc}\n\\cline{4-4} \\\\ [-1em]\n\\multicolumn{7}{c}{\\foreignlanguage{greek}{" +  livre_actuel + "} \\textbf{(\\nospace{" + chapitre_actuel +":" + verset_actuel + "})} } \\\\ \\\\ [-1em] % Si on veut ajouter les bordures latérales, remplacer {7}{c} par {7}{|c|}\n\\cline{4-4} \\\\\n\\cline{4-4}\n&  &  & &  &  & \\\\ [-0.9em]\n");                }                                previous_livre = livre_actuel;                                for (int j = 1; j < texte.get(i).size(); j++) { // On créer de nouvelles (3) cellules de tableau pour chaque ligne et on remplit ces cellules par le numéro de verset si présent et le texte de la ligne.                    //corpsCorps.addCell(Integer.toString(j+1));                    // corpsCorps.addCell("phrase très très longue eeeeeeeeeeeeeeeeeeeeeeeeeee test");                    // System.out.println(texte.get(i).get(j).get(0).valeur);                    //corpsCorps.addCell(Integer.toString(j+1));                                        // On remplace le macron sur le dernier caractère d'une ligne par un macron correspondant.                    String words = (texte.get(i).get(j).get(0).valeur).replaceAll("(.)\u0305$", "\\\\finalN{$1}");                    // System.out.println("\\finnalN{test}");                                        String prev_CMG = chapitre_mot_a_gauche;                    chapitre_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(2, 4)));                    String prev_CMD = chapitre_mot_a_droite;                    chapitre_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(2, 4)));                    String prev_VAG = verset_actuel_a_gauche;                    verset_actuel_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(4, 6)));                    String prev_VAD = verset_actuel_a_droite;                    verset_actuel_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(4, 6)));                    String prev_MAG = numero_mot_a_gauche;                    numero_mot_a_gauche = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(1).numero).substring(6, 8)));                    String prev_MAD = numero_mot_a_droite;                    numero_mot_a_droite = Integer.toString(Integer.parseInt((texte.get(i).get(j).get(texte.get(i).get(j).size()-1).numero).substring(6, 8)));                                        // ------------------------                                        String LEFTincrementGospelChapterTeXNumber = " ";                    int k = Integer.parseInt(prev_CMD);                    while ( k < Integer.parseInt(chapitre_mot_a_gauche) - 1) {                        LEFTincrementGospelChapterTeXNumber += "\\refstepcounter{gospelchapter} ";                        k++;                    }                                        // I know it's a bit strange                    String RIGHTincrementGospelChapterTeXNumber = " ";                    k = Integer.parseInt(chapitre_mot_a_gauche);                    while (k < Integer.parseInt(chapitre_mot_a_droite) - 1) {                        RIGHTincrementGospelChapterTeXNumber += "\\refstepcounter{gospelchapter} ";                        k++;                    }                                        if (!chapitre_mot_a_gauche.equals(prev_CMD) && !chapitre_mot_a_droite.equals(chapitre_mot_a_gauche)) {                        writer2.write("&"+LEFTincrementGospelChapterTeXNumber+"\\mygospelchapter &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &"+RIGHTincrementGospelChapterTeXNumber+"\\mygospelchapter &  \\\\\n");                        continue;                    }                    if (!(chapitre_mot_a_gauche.equals(prev_CMD)) || (i == 1 && j == i) ) { // La deuxième condition signifie, si on est sur la 1ère ligne de la 1ère page.                        // on affiche le numéro du verset à gauche                        writer2.write("&"+LEFTincrementGospelChapterTeXNumber+"\\mygospelchapter &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &  &  \\\\\n");                        continue;                    }                    if (!chapitre_mot_a_droite.equals(chapitre_mot_a_gauche)) {                        // on affiche le numéro du verset à droite                        writer2.write("&  & " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &"+RIGHTincrementGospelChapterTeXNumber+"\\mygospelchapter &  \\\\\n");                        continue;                    }                    // ------------------------                                        if (!(verset_actuel_a_gauche.equals(prev_VAD))) {                        // on affiche le numéro du verset à gauche                        writer2.write("& \\textbf{" + verset_actuel_a_gauche + "} &  & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &  &  \\\\\n");                        continue;                    }                    if (!verset_actuel_a_droite.equals(verset_actuel_a_gauche)) {                        // on affiche le numéro du verset à droite                        writer2.write("&  & " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " & \\textbf{" + verset_actuel_a_droite + "} &  \\\\\n");                        continue;                    }                    writer2.write("&  & " + numero_mot_a_gauche + " & \\foreignlanguage{greek}{" + words + "} & " + numero_mot_a_droite + " &  &  \\\\\n");                }                                writer2.write("[0.2em]\n\\cline{4-4}\n\\end{tabular}\n\\end{center}\n\\end{table}\n}\n");                                /*PdfPCell corps = new PdfPCell(corpsCorps);                PdfPCell footer = new PdfPCell(new Phrase("p." + (i+1)));                footer.setHorizontalAlignment(Element.ALIGN_CENTER);                footer.setVerticalAlignment(Element.ALIGN_MIDDLE);                                body.addCell(header);                body.addCell(corps);                body.addCell(footer);                                document.add(body);*/                            }            writer2.write("\\end{document}");            writer2.close();            // grec                        //document.close();        } catch (DocumentException e)        {            e.printStackTrace();        } catch (FileNotFoundException e)        {            e.printStackTrace();        }    }}