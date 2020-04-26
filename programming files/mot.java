/* Licence :
This file is copyright © 2020 by Gustav Berloty (creator of this file) released under the CC BY-NC-SA 4.0 licence :
https://creativecommons.org/licenses/by-nc-sa/4.0/
*/

class mot {
    String valeur;
    // La variable numero donnera la position du mot dans le le NT : dans quel livre est-il, quel chapitre, quel verset, quel position dans le verset.
    String numero; // Le type est String car on peut avoir a utiliser un "leading zero".
    // Pouvoir signaler si le mot est coupé en 2 et si oui si il s'agit de la première partie du mot ou de la seconde.
    /*
     On pourra ajouter d'autres caractèristiques du mot, comme par exemple :
     - son numéro Strong
     - si c'est un verbe, un nom, un pronom, un adjectif, etc…
     - sa conjugaison
     // ajouter sa position dans le manuscrit grâce à l'ArrayList texte ?
     Nous pourrons ainsi avoir un code couleur selon certaines de ses propriétés.
     */
    String scribe;
    int posManuscript = 0; // use for rectification
        
    public mot(String m) {
        valeur = m;
        scribe = "orig"; // for the original scribe
    }
    
    /*public mot(String n) {
        numero = n;
    }*/
    
    public mot(String m, String n) {
        valeur = m;
        numero = n;
        scribe = "orig";
    }
    
        public mot(String m, String n, String s) {
    	valeur = m;
    	numero = n;
    	scribe = s;
    }
        
    public mot(String m, String n, String s, int givenPos) {
    	valeur = m;
    	numero = n;
    	scribe = s;
    	posManuscript = givenPos;
    }
}


