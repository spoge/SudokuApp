package no.hioa.sudokuapp.information;

/**
 * Created by Sondre on 10.11.2014.
 *
 * Used to store coordinates & values for middle-calculations
 */
public class Square {

    public int y, x, value = 0;

    public Square(int y, int x, int value) {
        this.y = y;
        this.x = x;
        this.value = value;
    }

    public Square(int y, int x) {
        this.y = y;
        this.x = x;
    }

    public boolean equals(int y, int x) {
        if(this.y == y && this.x == x) return true;
        return false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return "y = " + y + ", x = " + x + ", value = " + value;
    }
}
