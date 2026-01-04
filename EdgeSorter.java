import java.util.Comparator;
import java.util.List;

/**
 * Utility to sort edges based on their importance.
 *
 * Overall Time Complexity: O(E log E) average, O(E²) worst case
 * Overall Space Complexity: O(log E) average (recursion stack)
 * where E = number of edges
 */
public class EdgeSorter {

    /**
     * Sorts edges using QuickSort.
     *
     * Time Complexity: O(E log E) average, O(E²) worst case
     * Space Complexity: O(log E)
     */
    public static void sortEdges(List<Edge> edges, Board board) {
        if (edges == null || edges.isEmpty())
            return;
        quickSort(edges, 0, edges.size() - 1, board);
    }

    /**
     * Recursive QuickSort implementation.
     *
     * Time Complexity: O(E log E) average, O(E²) worst case
     * Space Complexity: O(log E) due to recursion stack
     */
    private static void quickSort(List<Edge> edges, int low, int high, Board board) {
        if (low < high) {
            int pi = partition(edges, low, high, board);
            quickSort(edges, low, pi - 1, board);
            quickSort(edges, pi + 1, high, board);
        }
    }

    /**
     * Partition step of QuickSort.
     *
     * Time Complexity: O(E)
     * Space Complexity: O(1)
     */
    private static int partition(List<Edge> edges, int low, int high, Board board) {
        Edge pivot = edges.get(high);
        int pivotScore = getEdgeScore(pivot, board);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            if (getEdgeScore(edges.get(j), board) < pivotScore) {
                i++;
                Edge temp = edges.get(i);
                edges.set(i, edges.get(j));
                edges.set(j, temp);
            }
        }

        Edge temp = edges.get(i + 1);
        edges.set(i + 1, edges.get(high));
        edges.set(high, temp);

        return i + 1;
    }

    /**
     * Computes importance score for an edge.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private static int getEdgeScore(Edge e, Board board) {
        int score = 100;

        int r1 = e.a.row, c1 = e.a.col;
        int r2 = e.b.row, c2 = e.b.col;

        if (r1 == r2) { // Horizontal edge
            int r = r1;
            int c = Math.min(c1, c2);

            if (r > 0)
                score -= getCellCluePriority(board.cells[r - 1][c]);

            if (r < board.rows)
                score -= getCellCluePriority(board.cells[r][c]);

        } else { // Vertical edge
            int r = Math.min(r1, r2);
            int c = c1;

            if (c > 0)
                score -= getCellCluePriority(board.cells[r][c - 1]);

            if (c < board.cols)
                score -= getCellCluePriority(board.cells[r][c]);
        }

        if (e.a.degree == 1 || e.b.degree == 1)
            score -= 30;

        return score;
    }

    /**
     * Returns priority weight for a cell clue.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private static int getCellCluePriority(Cell cell) {
        if (cell.clue == -1)
            return 0;
        if (cell.clue == 3)
            return 40;
        if (cell.clue == 0)
            return 50;
        if (cell.clue == 1 || cell.clue == 2)
            return 20;
        return 0;
    }
}
