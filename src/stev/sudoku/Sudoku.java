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

import java.io.PrintWriter;

public class Sudoku {
    private char grille[][];

    public Sudoku() {
        grille = new char[9][9];
    }

    public Sudoku(char[][] g){ grille = g; }

    public void initialize(String sudoku) {
        for (int i=0;i<9;i++)
            for (int j=0;j<9;j++)
                this.grille[i][j]=sudoku.charAt(i*9+j);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Sudoku :\n");

        for (int i=0;i<9;i++) {
            for (int j = 0; j < 3; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append("|");
            for (int j = 3; j < 6; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append("|");
            for (int j = 6; j < 9; j++)
                stringBuilder.append(grille[i][j]+" ");
            stringBuilder.append('\n');
            if (i==2||i==5)
                stringBuilder.append("-------------------\n");
        }

        return stringBuilder.toString();
    }

    public BooleanFormula modelize() {
        PropositionalVariable variables[][][] = new PropositionalVariable[9][9][9];

        //initialisation des variables
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                for (int k = 0; k < 9; k++) {
                    variables[i][j][k] = new PropositionalVariable(i + "" + j + "" + k);
                }
            }
        }

        // Modélisation de la première propriété
        BooleanFormula bfAnd12[] = new BooleanFormula[81];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                BooleanFormula bfOr1[] = new BooleanFormula[9];
                for (int k = 0; k < 9; k++) {
                    BooleanFormula bfAnd11[] = new BooleanFormula[9];
                    for (int l = 0; l < 9; l++) {
                        if (l != k) {
                            bfAnd11[l] = new Not(variables[i][j][l]);
                        } else {
                            bfAnd11[l] = variables[i][j][l];
                        }
                    }
                    And and = new And(bfAnd11);
                    bfOr1[k] = and;
                }
                Or or = new Or(bfOr1);
                bfAnd12[i * 9 + j] = or;
            }
        }
        BooleanFormula prop1 = new And(bfAnd12);

        // Modélisation de la deuxième propriété
        BooleanFormula bfAnd22[] = new BooleanFormula[81];
        for (int i = 0; i < 9; i++) {
            for (int k = 0; k < 9; k++) {
                BooleanFormula bfOr2[] = new BooleanFormula[9];
                for (int j = 0; j < 9; j++) {
                    BooleanFormula bfAnd21[] = new BooleanFormula[9];
                    for (int l = 0; l < 9; l++) {
                        if (l != j) {
                            bfAnd21[l] = new Not(variables[i][l][k]);
                        } else {
                            bfAnd21[l] = variables[i][l][k];
                        }
                    }
                    And and = new And(bfAnd21);
                    bfOr2[j] = and;
                }
                Or or = new Or(bfOr2);
                bfAnd22[i * 9 + k] = or;
            }
        }
        BooleanFormula prop2 = new And(bfAnd22);

        // Modélisation de la troisième propriété
        BooleanFormula bfAnd32[] = new BooleanFormula[81];
        for (int j = 0; j < 9; j++) {
            for (int k = 0; k < 9; k++) {
                BooleanFormula bfOr3[] = new BooleanFormula[9];
                for (int i = 0; i < 9; i++) {
                    BooleanFormula bfAnd31[] = new BooleanFormula[9];
                    for (int l = 0; l < 9; l++) {
                        if (l != i) {
                            bfAnd31[l] = new Not(variables[l][j][k]);
                        } else {
                            bfAnd31[l] = variables[l][j][k];
                        }
                    }
                    And and = new And(bfAnd31);
                    bfOr3[i] = and;
                }
                Or or = new Or(bfOr3);
                bfAnd32[j * 9 + k] = or;
            }
        }
        BooleanFormula prop3 = new And(bfAnd32);

        // Modélisation de la quatrième propriété
        BooleanFormula bfAnd42[] = new BooleanFormula[81];
        for (int bi = 0; bi < 3; bi++) {
            for (int bj = 0; bj < 3; bj++) {
                for (int k = 0; k < 9; k++) {
                    BooleanFormula bfOr4[] = new BooleanFormula[9];
                    int count = 0;
                    for (int i = bi * 3; i < (bi + 1) * 3; i++) {
                        for (int j = bj * 3; j < (bj + 1) * 3; j++) {
                            bfOr4[count] = variables[i][j][k];
                            count++;
                        }
                    }
                    Or or = new Or(bfOr4);
                    bfAnd42[bi * 27 + bj * 9 + k] = or;
                }
            }
        }
        BooleanFormula prop4 = new And(bfAnd42);

        // Combinaison de toutes les propriétés
        BooleanFormula bigFormula = new And(prop1, prop2, prop3, prop4);

        // Affichage de la formule
        System.out.println(bigFormula);

        // Conversion de la formule en CNF
        BooleanFormula cnf = BooleanFormula.toCnf(bigFormula);

        // Affichage de la formule CNF
        System.out.println(cnf);

        return cnf;
    }

    public String solve(int[][] clauses){
        final int MAXVAR = 9 * 9 * 9;

        ISolver solver = SolverFactory.newDefault();
        Reader reader = new DimacsReader(solver);
        PrintWriter out = new PrintWriter(System.out,true);

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
        char grilleSol[][] = new char[9][9];
        IProblem problem = solver;
        try {
            if (problem.isSatisfiable()) {
                int[] model = problem.model();
                for(int i = 0; i < 9; i ++){
                    for(int j = 0; j < 9; j++){
                        for(int k = 0; k < 9; k++){
                            System.out.println(model[i * 9 + j * 9 + k]);
                            if(model[i * 9 + j * 9 + k] > 0)
                                grilleSol[i][j] = (char) (k + 1 + '0');
                        }
                    }
                }

                Sudoku sol = new Sudoku(grilleSol);
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
