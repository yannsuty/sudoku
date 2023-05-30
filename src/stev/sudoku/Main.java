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

public class Main {
    private static String sudokus[] = {
            "3##8#1##22#1#3#6#4###2#4###8#9###1#6#6#####5#7#2###4#9###5#9###9#4#8#7#56##1#7##3"
    };

    public static void main(String[] args) throws TimeoutException, ContradictionException {
        Sudoku sudoku = new Sudoku();
        sudoku.initialize(args[0]);
        System.out.println(sudoku);

        // Export this formula as an array of clauses
        int[][] clauses = sudoku.modelize().getClauses();

        System.out.println(sudoku.solve(clauses));
    }
}
