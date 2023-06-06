package stev.sudoku;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import stev.booleans.*;
import java.math.*;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Sudoku {
    private char grille[][];
    private int taille;

    public Sudoku(){
        this(9);
    }

    public Sudoku(int t) {
        grille = new char[t][t];
        taille = t;
    }

    public Sudoku(char[][] g, int t){ grille = g; taille = t; }

    public void initialize(String sudoku) {
        for (int i=0;i<taille;i++)
            for (int j=0;j<taille;j++)
                this.grille[i][j]=sudoku.charAt(i*taille+j);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sudoku :\n");

        for (int i=0;i<taille;i++) {
            for (int j = 0; j < taille / 3; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append("|");
            for (int j = taille / 3; j < 2 * taille / 3; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append("|");
            for (int j = 2 * taille / 3; j < taille; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append('\n');
            if (i==taille/3 - 1||i==2 * taille / 3 -1)
                stringBuilder.append("-------------------\n");
        }

        return stringBuilder.toString();
    }

    private int factorielle(int n){
        if(n<=1){
            return 1;
        }
        return n * factorielle(n - 1);
    }

    public BooleanFormula modelize() {
        PropositionalVariable variables[][][] = new PropositionalVariable[taille][taille][taille];

        //initialisation des variables
        //une variable pour chaque nombre possible dans chaque case, donc 9 par case, soit 9 * 9 * 9 pour une grille de taille 9
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                for (int k = 0; k < taille; k++) {
                    variables[i][j][k] = new PropositionalVariable(i + "" + j + "" + k);
                }
            }
        }

        // Modélisation de la première propriété
        ArrayList<BooleanFormula> auPlus1ChiffreParCase = new ArrayList<>();
        ArrayList<BooleanFormula> auMoins1ChiffreParCase = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                ArrayList<BooleanFormula> tousLesChiffresPossiblesParCase = new ArrayList<>();
                for (int k = 0; k < taille; k++) {
                    tousLesChiffresPossiblesParCase.add(variables[i][j][k]);
                    for (int l = k + 1; l < taille; l++) {
                        auPlus1ChiffreParCase.add(new Or(new Not(variables[i][j][k]), new Not(variables[i][j][l])));
                    }
                }
                auMoins1ChiffreParCase.add(new Or(tousLesChiffresPossiblesParCase));
            }
        }

        BooleanFormula prop1 = new And(new And(auPlus1ChiffreParCase), new And(auMoins1ChiffreParCase));
        //System.out.println(prop1);

        // Modélisation de la deuxième propriété
        ArrayList<BooleanFormula> pas2FoisLeMemeChiffreSurUneMemeLigne = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int k = 0; k < taille; k++) {
                for (int j = 0; j < taille; j++) {
                    for (int l = j + 1; l < taille; l++) {
                        pas2FoisLeMemeChiffreSurUneMemeLigne.add(new Or(new Not(variables[i][j][k]), new Not(variables[i][l][k])));
                    }
                }
            }
        }
        BooleanFormula prop2 = new And(pas2FoisLeMemeChiffreSurUneMemeLigne);
        //System.out.println(prop2);


        // Modélisation de la troisième propriété
        ArrayList<BooleanFormula> pas2FoisLeMemeChiffreSurUneMemeColonne = new ArrayList<>();
        for (int j = 0; j < taille; j++) {
            for (int k = 0; k < taille; k++) {
                for (int i = 0; i < taille; i++) {
                    for (int l = i + 1; l < taille; l++) {
                        pas2FoisLeMemeChiffreSurUneMemeColonne.add(new Or(new Not(variables[i][j][k]), new Not(variables[l][j][k])));
                    }
                }
            }
        }
        BooleanFormula prop3 = new And(pas2FoisLeMemeChiffreSurUneMemeColonne);
        //System.out.println(prop3);

        // Modélisation de la quatrième propriété
        ArrayList<BooleanFormula> auMoinsUneFoisChaqueChiffreDansChaqueSousGrille = new ArrayList<>();
        for (int bi = 0; bi < taille / 3; bi++) {
            for (int bj = 0; bj < taille / 3; bj++) {
                for (int k = 0; k < taille; k++) {
                    ArrayList<BooleanFormula> toutesLesCasesDeLaSousGrille = new ArrayList<>();
                    for (int i = bi * taille / 3; i < (bi + 1) * taille / 3; i++) {
                        for (int j = bj * taille / 3; j < (bj + 1) * (taille / 3); j++) {
                            toutesLesCasesDeLaSousGrille.add(variables[i][j][k]);
                        }
                    }
                    auMoinsUneFoisChaqueChiffreDansChaqueSousGrille.add(new Or(toutesLesCasesDeLaSousGrille));
                }
            }
        }
        BooleanFormula prop4 = new And(auMoinsUneFoisChaqueChiffreDansChaqueSousGrille);
        //System.out.println(prop4);

        //Lecture de l'entrée pour respect de la grille de départ
        ArrayList<BooleanFormula> casesPréRemplies = new ArrayList<>();
        for(int i = 0; i < taille; i++){
            for(int j = 0; j < taille; j++){
                if(grille[i][j] != '#'){
                    int valeur = Character.getNumericValue(grille[i][j]);
                    casesPréRemplies.add(variables[i][j][valeur-1]);
                }
            }
        }
        BooleanFormula prop5 = new And(casesPréRemplies);

        // Combinaison de toutes les propriétés
        BooleanFormula bigFormula = new And(prop1, prop2, prop3, prop4, prop5);

        // Affichage de la formule
        //System.out.println(bigFormula);

        // Conversion de la formule en CNF
        BooleanFormula cnf = BooleanFormula.toCnf(bigFormula);

        // Affichage de la formule CNF
        //System.out.println(cnf);

        return cnf;
    }

    public String solve(int[][] clauses){
        final int MAXVAR = taille * taille * taille;

        ISolver solver = SolverFactory.newDefault();

        // prepare the solver to accept MAXVAR variables. MANDATORY for MAXSAT solving
        solver.newVar(MAXVAR);

        for (int i=0;i<clauses.length;i++) {
            try {
                solver.addClause(new VecInt(clauses[i])); // adapt Array to IVecInt
            } catch (ContradictionException e) {
                return "Grille non satisfiable";
            }
        }

        // we are done. Working now on the IProblem interface
        char grilleSol[][] = new char[taille][taille];
        IProblem problem = solver;
        try {
            if (problem.isSatisfiable()) {
                int[] model = problem.model();
                for(int i = 0; i < taille; i ++){
                    for(int j = 0; j < taille; j++){
                        for(int k = 0; k < taille; k++){
                            //System.out.println(model[i * taille * taille + j * taille + k]);
                            if(model[i * taille * taille + j * taille + k] > 0)
                                grilleSol[i][j] = (char) (k + 1 + '0');
                        }
                    }
                }

                Sudoku sol = new Sudoku(grilleSol, taille);
                return sol.toString();
            } else {
                return "Grille non satisfiable";
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "Grille non satisfiable";
    }
}
