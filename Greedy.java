import java.util.*;

/**
 * Greedy constraint satisfaction solver.
 * Uses graph-based iteration over faces and nodes.
 */
public class Greedy {

    public Validator validator = null;

    /**
     * Applies greedy constraint propagation until no more changes occur.
     */
    public void solve(Board board) {
        if (validator == null)
            return;

        boolean changed;

        do {
            changed = false;

            // ----------- GREEDY PROPAGATION -----------
            for (Face face : board.getFaces()) {
                if (applyFace(face, board))
                    changed = true;
            }

            // ----------- FINALIZATION CHECK -----------
            if (noUnknownEdges(board) && isSingleCycle(board)) {
                finalizeLoop(board);
                return; // STOP — loop is finalized
            }

        } while (changed);
    }

    /**
     * Makes a single greedy move by applying constraint propagation.
     * Returns true if exactly one edge was changed, false otherwise.
     */
    public boolean solveSingleMove(Board board) {
        if (validator == null)
            return false;

        // Try to find and make one single move
        for (Face face : board.getFaces()) {
            if (face.getClue() < 0)
                continue;

            int active = face.activeCount();
            int unknown = face.unknownCount();
            int clue = face.getClue();

            // Rule 1: clue satisfied → rest INACTIVE
            if (active == clue) {
                for (Edge e : face.getEdges()) {
                    if (e != null && e.isUnknown()) {
                        if (change(e, EdgeStatus.INACTIVE, board)) {
                            return true; // Made one move, return immediately
                        }
                    }
                }
            }

            // Rule 2: remaining edges forced ACTIVE
            if (unknown == clue - active) {
                for (Edge e : face.getEdges()) {
                    if (e != null && e.isUnknown()) {
                        if (change(e, EdgeStatus.ACTIVE, board)) {
                            return true; // Made one move, return immediately
                        }
                    }
                }
            }
        }

        return false; // No move found
    }

    // ================= GREEDY FACE LOGIC =================

    /**
     * Applies constraint propagation for a single face.
     * Returns true if any edge state changed.
     */
    private boolean applyFace(Face face, Board board) {
        if (face.getClue() < 0)
            return false;

        int active = face.activeCount();
        int unknown = face.unknownCount();
        int clue = face.getClue();

        boolean changed = false;

        // Rule 1: clue satisfied → rest INACTIVE
        if (active == clue) {
            for (Edge e : face.getEdges()) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.INACTIVE, board))
                        changed = true;
                }
            }
        }

        // Rule 2: remaining edges forced ACTIVE
        if (unknown == clue - active) {
            for (Edge e : face.getEdges()) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.ACTIVE, board))
                        changed = true;
                }
            }
        }

        return changed;
    }

    /**
     * Attempts to change an edge's state.
     * Validates the change and rolls back if invalid.
     */
    private boolean change(Edge e, EdgeStatus newStatus, Board board) {
        EdgeStatus old = e.getStatus();
        e.setState(newStatus, PlayerType.CPU);

        if (!validator.validatePartial(board)) {
            e.setState(old, PlayerType.NONE);
            return false;
        }
        return true;
    }

    // ================= FINALIZATION LOGIC =================

    /**
     * Checks if all edges have been decided (no UNKNOWN edges).
     */
    private boolean noUnknownEdges(Board board) {
        for (Edge e : board.getEdges()) {
            if (e.isUnknown())
                return false;
        }
        return true;
    }

    /**
     * Checks if all active edges form a single cycle using graph traversal.
     */
    private boolean isSingleCycle(Board board) {
        // Find first ACTIVE edge
        Edge start = null;
        for (Edge e : board.getEdges()) {
            if (e.isActive()) {
                start = e;
                break;
            }
        }
        if (start == null)
            return false;

        // Traverse ACTIVE edges using DFS
        Set<Edge> visited = new HashSet<>();
        Stack<Edge> st = new Stack<>();
        st.push(start);

        while (!st.isEmpty()) {
            Edge cur = st.pop();

            if (visited.contains(cur))
                continue;
            visited.add(cur);

            // Check nodes connected to this edge
            Node[] nodes = { cur.a, cur.b };
            for (Node n : nodes) {
                int degree = n.getActiveDegree();

                // Loop invariant violation
                if (degree != 2)
                    return false;

                // Add unvisited active edges
                for (Edge e : n.getEdges()) {
                    if (e.isActive() && !visited.contains(e))
                        st.push(e);
                }
            }
        }

        // Ensure all ACTIVE edges are in this one component
        for (Edge e : board.getEdges()) {
            if (e.isActive() && !visited.contains(e))
                return false;
        }

        return true;
    }

    /**
     * Marks the loop as finalized (for visualization purposes).
     * This is optional and doesn't affect core logic.
     */
    private void finalizeLoop(Board board) {
        System.out.println("Loop Finalized!");
    }
}
