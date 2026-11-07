
import java.util.*;

/**
 * Backtracking solver with greedy propagation.
 * Uses graph-based edge selection.
 */
public class Backtracking {
    private Greedy greedy = new Greedy();
    private Validator validator = new Validator();

    /**
     * Picks the best unknown edge to try next.
     * Uses simple iteration over the edge set.
     */
    public Edge pickBestUnknownEdge(Board board) {
        List<Edge> unknown = new ArrayList<>();
        for (Edge e : board.getEdges()) {
            if (e.isUnknown())
                unknown.add(e);
        }
        if (unknown.isEmpty())
            return null;

        // Sort unknown edges to pick the most constrained ones first
        EdgeSorter.sortEdges(unknown, board);
        return unknown.get(0);
    }

    /**
     * Solves the board using backtracking with greedy propagation.
     */
    public boolean solve(Board board) {
        return solve(board, PlayerType.NONE);
    }

    /**
     * Solves the board using backtracking with greedy propagation.
     *
     * @param board      The game board
     * @param solverType The player type making the moves
     * @return true if solved, false otherwise
     */
    public boolean solve(Board board, PlayerType solverType) {
        // Save the FULL state of ALL edges before doing anything.
        // This is critical: greedy.solve() will change UNKNOWN edges to
        // ACTIVE/INACTIVE, so we must be able to restore all of them.
        Map<Edge, EdgeStatus> snapshot = takeSnapshot(board);

        greedy.validator = validator;
        greedy.solve(board);

        if (!validator.validatePartial(board)) {
            restore(snapshot);
            return false;
        }

        if (validator.validateFinal(board)) {
            return true;
        }

        Edge e = pickBestUnknownEdge(board);
        if (e == null) {
            // No unknown edges but not solved — dead end
            restore(snapshot);
            return false;
        }

        EdgeStatus[] options = { EdgeStatus.ACTIVE, EdgeStatus.INACTIVE };
        for (EdgeStatus s : options) {
            // Restore to the post-greedy state before each branch attempt,
            // so that each branch starts from the same clean state.
            restore(snapshot);
            greedy.solve(board); // re-apply greedy to get back to the branching point

            e.setState(s, solverType);
            if (validator.validatePartial(board)) {
                if (solve(board, solverType))
                    return true;
            }
        }

        // Neither branch worked — restore and report failure
        restore(snapshot);
        return false;
    }

    /**
     * Takes a snapshot of ALL edge states on the board.
     */
    private Map<Edge, EdgeStatus> takeSnapshot(Board board) {
        Map<Edge, EdgeStatus> snapshot = new HashMap<>();
        for (Edge e : board.getEdges()) {
            snapshot.put(e, e.getStatus());
        }
        return snapshot;
    }

    /**
     * Restores edge states from a snapshot.
     */
    private void restore(Map<Edge, EdgeStatus> snapshot) {
        for (Map.Entry<Edge, EdgeStatus> entry : snapshot.entrySet()) {
            entry.getKey().setState(entry.getValue(), PlayerType.NONE);
        }
    }
}
