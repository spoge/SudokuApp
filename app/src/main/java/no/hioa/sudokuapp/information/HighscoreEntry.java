package no.hioa.sudokuapp.information;

/**
 * Created by Sondre on 25.11.2014.
 *
 * A single highscore-entry
 */
public class HighscoreEntry {
    private int id;
    private int sudokuId;
    private String name;
    private int score;

    public HighscoreEntry() {

    }

    public HighscoreEntry(int sudokuId, String name, int score) {
        this.sudokuId = sudokuId;
        this.name = name;
        this.score = score;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSudokuId(int sudokuId) {
        this.sudokuId = sudokuId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public int getSudokuId() {
        return sudokuId;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
