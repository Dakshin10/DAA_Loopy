import java.util.List;

/**
 * Utility to sort edges based on their importance.
 * Uses graph-based edge scoring instead of coordinate access.
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
     * Computes importance score for an edge using graph-based heuristics.
     * Lower score = higher priority.
     *
     * Time Complexity: O(F) where F is number of faces (typically small)
     * Space Complexity: O(1)
     */
    private static int getEdgeScore(Edge e, Board board) {
        int score = 100;

        // Find faces that contain this edge
        for (Face face : board.getFaces()) {
            if (face.getEdges().contains(e)) {
                score -= getFaceCluePriority(face);
            }
        }

        // Prioritize edges connected to nodes with degree 1 (extend paths)
        if (e.a.getActiveDegree() == 1 || e.b.getActiveDegree() == 1)
            score -= 30;

        return score;
    }

    /**
     * Returns priority weight for a face clue.
     * Higher clues (3, 0) get higher priority.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private static int getFaceCluePriority(Face face) {
        int clue = face.getClue();
        
        if (clue == -1)
            return 0;
        if (clue == 3)
            return 40;
        if (clue == 0)
            return 50;
        if (clue == 1 || clue == 2)
            return 20;
        return 0;
    }
}
