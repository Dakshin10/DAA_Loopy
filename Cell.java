
public class Cell {
    public int row, col, clue;
    public Edge top;
    public Edge bottom;
    public Edge left;
    public Edge right;

    public Cell(int r, int c, int val) {
        this.row = r;
        this.col = c;
        this.clue = val;
    }

    public int lineCount() {
        int count = 0;
        if (top != null && top.isActive())
            count++;
        if (bottom != null && bottom.isActive())
            count++;
        if (left != null && left.isActive())
            count++;
        if (right != null && right.isActive())
            count++;
        return count;
    }

    public int unknownCount() {
        int c = 0;
        if (top != null && top.isUnknown())
            c++;
        if (bottom != null && bottom.isUnknown())
            c++;
        if (left != null && left.isUnknown())
            c++;
        if (right != null && right.isUnknown())
            c++;
        return c;
    }
}
