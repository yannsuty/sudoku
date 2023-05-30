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

    public BooleanFormula modelize(){

        PropositionalVariable variables[][][] = new PropositionalVariable[9][9][9];

        //initialisation des variables
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                for(int k = 0; k < 9; k++){
                    variables[i][j][k] = new PropositionalVariable(i + "" + j + "" + k);
                }
            }
        }

        //modélisation propriété 1
        BooleanFormula bfAnd12[] = new BooleanFormula[81];
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                BooleanFormula bfOr1[] = new BooleanFormula[9];
                for(int k = 0; k < 9; k++){
                    BooleanFormula bfAnd11[] = new BooleanFormula[9];
                    for(int l = 0; l < 9; l++){
                        if(l != k){
                            bfAnd11[l] = new Not(variables[i][j][l]);
                        }
                        else{
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
        BooleanFormula bigFormula = new And(bfAnd12);

        // We can print it
        System.out.println(bigFormula);

        // Convert this formula to CNF
        BooleanFormula cnf = BooleanFormula.toCnf(bigFormula);

        // Let's print it again
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
