package stev.sudoku;

public class Main {
    private static String sudokus[] = {
            "3##8#1##22#1#3#6#4###2#4###8#9###1#6#6#####5#7#2###4#9###5#9###9#4#8#7#56##1#7##3"
    };

    public static void main(String[] args) {
        Sudoku sudoku = new Sudoku();
        sudoku.initialize(sudokus[0]);
        System.out.println(sudoku);
    }
}
