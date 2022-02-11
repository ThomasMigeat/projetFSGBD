package fr.miage.fsgbd;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;


import java.io.IOException;
import java.util.ArrayList;

import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.stream.Stream;


/**
 * @author Galli Gregory, Mopolo Moke Gabriel
 * @param <Type>
 */
public class BTreePlus<Type> implements java.io.Serializable {
    private Noeud<Type> racine;
    
    public BTreePlus(int u, Executable e) {
        racine = new Noeud<Type>(u, e, null);
    }

    public void afficheArbre() {
        racine.afficheNoeud(true, 0);
    }

    
    private Noeud<Type> previous;
    private boolean refresh;
    public boolean addValeur(Type v) {
        refresh = false;
        this.previous =null;
        System.out.println("Ajout de la valeur : " + v.toString());
        if (racine.contient(v) == null) {
            Noeud<Type> newRacine = racine.addValeur(v);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }
    public boolean addValeur(Type v,int ligne) {
        refresh = false;
        this.previous =null;
        System.out.println("Ajout de la valeur : " + v.toString());
        if (racine.contient(v) == null) {
            Noeud<Type> newRacine = racine.addValeur(v,ligne);
            if (racine != newRacine)
                racine = newRacine;
            return true;
        }
        return false;
    }


    public void removeValeur(Type v) {
        refresh = false;
        this.previous =null;
        System.out.println("Retrait de la valeur : " + v.toString());
        if (racine.contient(v) != null) {
            Noeud<Type> newRacine = racine.removeValeur(v, false);
            if (racine != newRacine)
                racine = newRacine;
        }
    }
    public void searchLine() throws IOException {
        Long tempsSeq = (long)0, tmpIndex= (long)0, minSequentiel= (long)9999, maxSequentiel= (long)0, indexMin= (long)9999, indexMax = (long)0;
        ArrayList<Integer> lesvaleurs = new ArrayList<>();
        String id;
        try(BufferedReader br = new BufferedReader(new FileReader("Dataset.txt"))) {
            int line = 0;
            String ligne;
            while ((ligne = br.readLine()) != null) {
                line++;
                switch (line % 100) {
                    case 0 -> {
                        id = ligne.substring(0, ligne.indexOf(","));
                        int v = Integer.parseInt(id);
                        lesvaleurs.add(v);
                    }
                }
            }
        }
        System.out.println("\u001B[36m"+"\nRecherche des lignes avec la methode sequentielle: ");
        for (int i = 0, lesvaleursSize = lesvaleurs.size(); i < lesvaleursSize; i++) {
            Integer value = lesvaleurs.get(i);
            Long start = System.nanoTime();
            try (BufferedReader br = new BufferedReader(new FileReader("Dataset.txt"))) {
                String ligne;
                while ((ligne = br.readLine()) != null) {
                    id = ligne.substring(0, ligne.indexOf(","));
                    if (Integer.parseInt(id) == (int) value) {
                        Long end = System.nanoTime();
                        Long total = (end - start) / 1000;
                        System.out.println(ligne + " " + total + " microsecondes");
                        tempsSeq = tempsSeq + total;
                        if (total > maxSequentiel) maxSequentiel = total;
                        else if (total < minSequentiel) minSequentiel = total;
                        break;
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        System.out.println("\nTemps de la recherche sequentielle : "+tempsSeq+ " microsecondes");
        String ligne;

        System.out.println("\u001B[31m"+"\nRecherche des lignes avec la methode d'indexation: ");
        for (int i = 0, lesvaleursSize = lesvaleurs.size(); i < lesvaleursSize; i++) {
            Integer value = lesvaleurs.get(i);
            Long start = System.nanoTime();
            try (Stream<String> lignes = Files.lines(Paths.get("Dataset.txt"))) {
                int testNoeud = Noeud.pointeurs.get(value);
                ligne = lignes.skip(testNoeud).findFirst().get();
            }
            Long end = System.nanoTime();
            Long total = (end - start) / 1000;
            System.out.println(ligne + " " + total + " microsecondes");
            tmpIndex = tmpIndex + total;
            if (total > indexMax) indexMax = total;
            else if (total < indexMin) indexMin = total;

        }
        System.out.println("\nTemps de la recherche indexee : "+tmpIndex+ " microsecondes");

        System.out.println("\u001B[33m"+"\nTemps total de la recherche de maniere sequentielle : "+tempsSeq+" microsecondes");
        System.out.println("\u001B[35m"+"En moyenne une recherche sequentielle prends " +tempsSeq/100 +" microsecondes");
        System.out.println("\u001B[32m"+"La plus courte recherche est de "+ minSequentiel +" microsecondes");
        System.out.println("\u001B[32m"+"La plus longue recherche est de "+ maxSequentiel +" microsecondes");

        System.out.println("\u001B[33m"+"\nTemps total de la recherche de maniere indexee : "+tmpIndex+ " microsecondes");
        System.out.println("\u001B[35m"+"En moyenne une recherche indexee prends " +tmpIndex/100 +" microsecondes ");
        System.out.println("\u001B[32m"+"La plus courte recherche est de "+ indexMin +" microsecondes ");
        System.out.println("\u001B[32m"+"La plus longue recherche est de "+ indexMax +" microsecondes");

    }



    public DefaultMutableTreeNode bArbreToJTree() {
        if(previous!= null)
            refresh = true;
        return bArbreToJTree(racine);
    }

    private DefaultMutableTreeNode bArbreToJTree(Noeud<Type> root) {
        StringBuilder csv = new StringBuilder();
        if(root.fils.size()==0) {
            if (previous != null && !refresh)
                previous.next = root;
            previous = root;

        }
        ArrayList<Type> keys = root.keys;
        for (int i = 0, keysSize = keys.size(); i < keysSize; i++) {
            Type cle = keys.get(i);
            csv.append(cle.toString()).append(" ");
        }
        DefaultMutableTreeNode root2 = new DefaultMutableTreeNode(csv.toString(), true);
        root.fils.stream().map(this::bArbreToJTree).forEach(root2::add);
        return root2;
    }

}
