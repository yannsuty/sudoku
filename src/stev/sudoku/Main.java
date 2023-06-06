package stev.sudoku;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

/**
 * Travail de :
 * Diallo Mamadou Sounoussy - DIAM13049900
 * Yann Suty - SUTY01109905
 * Tardy Luca - TARL10060108
 */

public class Main {
    private static String sudokus[] = {
            "3##8#1##22#1#3#6#4###2#4###8#9###1#6#6#####5#7#2###4#9###5#9###9#4#8#7#56##1#7##3"
    };

    public static void main(String[] args){
        // Initialisation de la grille de départ
        Sudoku sudoku = new Sudoku(9);
        sudoku.initialize(args[0]);
        System.out.println(sudoku);

        // Modélisation des contraintes sous forme de clauses
        int[][] clauses = sudoku.modelize().getClauses();

        // Résolution de la grille
        System.out.println(sudoku.solve(clauses));
    }
}
