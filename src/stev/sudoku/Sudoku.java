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
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                for (int k = 0; k < taille; k++) {
                    variables[i][j][k] = new PropositionalVariable(i + "" + j + "" + k);
                }
            }
        }

        // Modélisation de la première propriété
        BooleanFormula[] bfAnd1 = new BooleanFormula[taille * taille * factorielle(taille) / (2 * factorielle(taille - 2))];
        BooleanFormula[] bfOr1 = new BooleanFormula[taille * taille];
        int m = 0;
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                BooleanFormula[] bf = new BooleanFormula[taille];
                for (int k = 0; k < taille; k++) {
                    bf[k] = variables[i][j][k];
                    for (int l = k + 1; l < taille; l++) {
                        bfAnd1[m] = new Or(new Not(variables[i][j][k]), new Not(variables[i][j][l]));
                        m++;
                    }
                }
                bfOr1[i * taille + j] = new Or(bf);
            }
        }

        BooleanFormula prop1 = new And(bfAnd1);
        BooleanFormula auMoins1 = new And(bfOr1);
        prop1 = new And(prop1, auMoins1);
        //System.out.println(prop1);



        // Modélisation de la deuxième propriété
        BooleanFormula[] bfAnd2 = new BooleanFormula[taille * taille * factorielle(taille) / (2 * factorielle(taille - 2))];
        int m2 = 0;
        for (int i = 0; i < taille; i++) {
            for (int k = 0; k < taille; k++) {
                for (int j = 0; j < taille; j++) {
                    for (int l = j + 1; l < taille; l++) {
                        bfAnd2[m2] = new Or(new Not(variables[i][j][k]), new Not(variables[i][l][k]));
                        m2++;
                    }
                }
            }
        }
        BooleanFormula prop2 = new And(bfAnd2);

        //System.out.println(prop2);


        // Modélisation de la troisième propriété
        BooleanFormula[] bfAnd3 = new BooleanFormula[taille * taille * factorielle(taille) / (2 * factorielle(taille - 2))];
        int m3 = 0;
        for (int j = 0; j < taille; j++) {
            for (int k = 0; k < taille; k++) {
                for (int i = 0; i < taille; i++) {
                    for (int l = i + 1; l < taille; l++) {
                        bfAnd3[m3] = new Or(new Not(variables[i][j][k]), new Not(variables[l][j][k]));
                        m3++;
                    }
                }
            }
        }
        BooleanFormula prop3 = new And(bfAnd3);
        //System.out.println(prop3);

        // Modélisation de la quatrième propriété
        BooleanFormula bfAnd42[] = new BooleanFormula[taille * taille];
        for (int bi = 0; bi < taille / 3; bi++) {
            for (int bj = 0; bj < taille / 3; bj++) {
                for (int k = 0; k < taille; k++) {
                    BooleanFormula bfOr4[] = new BooleanFormula[taille];
                    int count = 0;
                    for (int i = bi * taille / 3; i < (bi + 1) * taille / 3; i++) {
                        for (int j = bj * taille / 3; j < (bj + 1) * (taille / 3); j++) {
                            bfOr4[count] = variables[i][j][k];
                            count++;
                        }
                    }
                    BooleanFormula or = new Or(bfOr4);
                    bfAnd42[bi * taille * 3 + bj * taille + k] = or;
                }
            }
        }
        BooleanFormula prop4 = new And(bfAnd42);
        //System.out.println(prop4);

        //Lecture de l'entrée pour respect de la grille de départ
        ArrayList<BooleanFormula> bfAnd5 = new ArrayList<>();
        for(int i = 0; i < taille; i++){
            for(int j = 0; j < taille; j++){
                if(grille[i][j] != '#'){
                    int valeur = Character.getNumericValue(grille[i][j]);
                    bfAnd5.add(variables[i][j][valeur-1]);
                }
            }
        }

        BooleanFormula prop5 = new And(bfAnd5);

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

        // Feed the solver using Dimacs format, using arrays of int
        // (best option to avoid dependencies on SAT4J IVecInt)
        for (int i=0;i<clauses.length;i++) {
            try {
                solver.addClause(new VecInt(clauses[i])); // adapt Array to IVecInt
            } catch (ContradictionException e) {
                return "Unsatisfiable";
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
                return "Unsatisfiable";
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return "Unsatisfiable";
    }
}
