import java.util.Comparator;
import java.util.List;

/**
 * Utility to sort edges based on their "importance" for the solver.
 * Uses a QuickSort algorithm as requested.
 */
public class EdgeSorter {

    public static void sortEdges(List<Edge> edges, Board board) {
        if (edges == null || edges.isEmpty())
            return;
        quickSort(edges, 0, edges.size() - 1, board);
    }

    private static void quickSort(List<Edge> edges, int low, int high, Board board) {
        if (low < high) {
            int pi = partition(edges, low, high, board);
            quickSort(edges, low, pi - 1, board);
            quickSort(edges, pi + 1, high, board);
        }
    }

    private static int partition(List<Edge> edges, int low, int high, Board board) {
        Edge pivot = edges.get(high);
        int pivotScore = getEdgeScore(pivot, board);
        int i = (low - 1);

        for (int j = low; j < high; j++) {
            // Lower score (more important) comes first
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
     * Calculates a priority score for an edge.
     * Edges next to cells with cues (especially 3s and 0s) get lower scores (higher
     * priority).
     */
    private static int getEdgeScore(Edge e, Board board) {
        int score = 100; // Default base score

        // Identify adjacent cells using edge node coordinates
        int r1 = e.a.row, c1 = e.a.col;
        int r2 = e.b.row, c2 = e.b.col;

        if (r1 == r2) { // Horizontal edge
            int r = r1;
            int c = Math.min(c1, c2);
            // Cell above
            if (r > 0) {
                score -= getCellCluePriority(board.cells[r - 1][c]);
            }
            // Cell below
            if (r < board.rows) {
                score -= getCellCluePriority(board.cells[r][c]);
            }
        } else { // Vertical edge
            int r = Math.min(r1, r2);
            int c = c1;
            // Cell to the left
            if (c > 0) {
                score -= getCellCluePriority(board.cells[r][c - 1]);
            }
            // Cell to the right
            if (c < board.cols) {
                score -= getCellCluePriority(board.cells[r][c]);
            }
        }

        // graph-based priority: endpoints with degree 1 are highly likely to be
        // extended
        if (e.a.degree == 1 || e.b.degree == 1) {
            score -= 30;
        }

        return score;
    }

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
