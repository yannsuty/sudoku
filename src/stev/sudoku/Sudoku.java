package stev.sudoku;

public class Sudoku {
    private char grille[][];

    public Sudoku() {
        grille = new char[9][9];
    }

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
}
