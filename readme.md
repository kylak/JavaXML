Aide pour lire le pdf :   
![alt text](https://github.com/kylak/JavaXML/tree/dev/data/readme-image.png)


- Voici une commande à entrer dans le terminale pour exécuter le programme : 
javac programming\ files/*.java -d generated\ files/java\ class && cd generated\ files/java\ class && java get_data && cd ../..

- Info : 
Le dossier "com" est le dossier où se trouve la librairie itextpdf.

- À faire pour la prochaine fois:

 0. Corriger les erreurs (voir ci-dessous "Autres notes").
 0.1 Numéros de pages à afficher.
 1.1 Ajouter un signe (avec lien) à côté du texte quand il y a une rectification scribales afin que le lecteur soit au courant.
 1.2 Afficher dans les rectifications scribales quel scribe à fait la modification ?
 2. Faire en sorte que lorsque l'on clique sur un "lien" du "toc", on arive sur la baseline de la ligne du dessus et non sur celle vers laquelle le lien pointe, ainsi, on devrait pouvoir voir intégralement le numéro du chapitre, il ne devrait plus être coupé.
 
 4. Nettoyer le dossier, et rédiger le code en anglais? Le développer suivant un modèle MVC et rédiger des tests unitaires, envoyer des messages d'erreurs en cas de problème.?
 5. Ajouter une page informative, genre liste des versets non présents dans ce manuscrit et quels sont les corrections qui ont étés intégrés directement, et les concitoyens, etc…
 6. Ajouter une légende (en quelle lange ? anglais, koinè, français ?) pour la mise en page (que siginife les chiffres, en gras, plus grands, les overlines, etc… ) ?
 
 7. Mettre le sommaire de chaque livre côte à côte sur la même page, et mettre en bas de cette page de sommaire la page de correction scribale et son numéro de page.
 8. Fixer la taille du tableau selon les dimensions du codex ? (Cela pourrait peut-être permettre de mieux gérer Matthieu 28:15-20, Marc 16:17-20?).
 9. Pouvoir ajuster le format des pages du manuscrits de telle sortes à ce qu'elles aient toute la même taille de police d'écriture ?
 10. (Détails) Rendre la taille des numéros de page plus grand ?
 11. Avoir un autre format que pdf pour pouvoir faire des annotations et copiers-collers sur les mots et aussi trouver la définition d'un mot.
 12. Les concitoyens.
 12. Créer un fichier de configuration pour personnaliser le rendu (avoir un toc sur plusieurs pages ou sur une page en multicolonne,
 12. au niveau des corrections: utiliser un simple barre pour barrer les suppressions ou utiliser deux barres qui se croisent(\cancel), afficher les numéros de mots 
 12. pour les corrections ou simplement les chapitres et versets, intégrer les corrections au texte : de tous les scribes, seulement du scribe "", d'aucun scribe, etc…)
 
- Ce que l'on pourrait améliorer sur la structure XML :
 - Mettre les dimensions du codex dans des balises spécifiques pour pouvoir les récupérer et les utiliser de façon plus sûr, cela permettrait d'avoir les dimensions de notre tableau tex selon celles dans les données XML.
 - Ajouter les "blancs" (ou espace) présent dans le manuscrit (quitte à ne pas les afficher pour certain), cela permettrait d'avoir un résultat encore plus proche du codex, conservant l'"aération" de la mise en page, autrement dit la mise en page, cela peut s'avérer d'autant plus pratique si on souhaite ne pas ajouter d'espace entre les mots, en les distinguant par leur couleur. De toutes les manières, ajouter les blancs, devrait, si je ne me trompe pas, rendre la mise en page plus esthétique.
 - Avoir les titres que les manuscrits donnent eux-mêmes aux livres.
 
- Autres notes :
 // Mt 2:17 et Mt 17:25.
 // J'ai remarqué une erreur en Luc 11:4.10 c'est apheiomen et non apeiolomen (le lo est en trop).

Under licence : ![alt text](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png) http://creativecommons.org/licenses/by-nc-sa/4.0/ 


