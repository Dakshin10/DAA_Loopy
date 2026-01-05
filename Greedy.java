import java.util.*;

public class Greedy {

    public Validator validator = null;

    public void solve(Board board) {
        if (validator == null)
            return;

        boolean changed;

        do {
            changed = false;

            // ----------- GREEDY PROPAGATION -----------
            for (Cell[] row : board.cells) {
                for (Cell cell : row) {
                    if (applyCell(cell, board))
                        changed = true;
                }
            }

            // ----------- FINALIZATION CHECK -----------
            if (noUnknownEdges(board) && isSingleCycle(board)) {
                finalizeLoop(board);
                return; // STOP — loop is finalized
            }

        } while (changed);
    }

    // ================= GREEDY CELL LOGIC =================

    private boolean applyCell(Cell cell, Board board) {
        if (cell.clue < 0)
            return false;

        int active = 0, unknown = 0;
        Edge[] edges = { cell.top, cell.bottom, cell.left, cell.right };

        for (Edge e : edges) {
            if (e == null)
                continue;
            if (e.isActive())
                active++;
            else if (e.isUnknown())
                unknown++;
        }

        boolean changed = false;

        // Rule 1: clue satisfied → rest INACTIVE
        if (active == cell.clue) {
            for (Edge e : edges) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.INACTIVE, board))
                        changed = true;
                }
            }
        }

        // Rule 2: remaining edges forced ACTIVE
        if (unknown == cell.clue - active) {
            for (Edge e : edges) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.ACTIVE, board))
                        changed = true;
                }
            }
        }

        return changed;
    }

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

    private boolean noUnknownEdges(Board board) {
        for (Edge e : board.getAllEdges()) {
            if (e.isUnknown())
                return false;
        }
        return true;
    }

    private boolean isSingleCycle(Board board) {
        // Find first ACTIVE edge
        Edge start = null;
        for (Edge e : board.getAllEdges()) {
            if (e.isActive()) {
                start = e;
                break;
            }
        }
        if (start == null)
            return false;

        // Traverse ACTIVE edges
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
                int degree = 0;
                for (Edge e : n.edges) {
                    if (e.isActive()) {
                        degree++;
                        if (!visited.contains(e))
                            st.push(e);
                    }
                }

                // Loop invariant violation
                if (degree != 2)
                    return false;
            }
        }

        // Ensure all ACTIVE edges are in this one component
        for (Edge e : board.getAllEdges()) {
            if (e.isActive() && !visited.contains(e))
                return false;
        }

        return true;
    }

    private void finalizeLoop(Board board) {
        // Break non-loop edges
        for (Edge e : board.getAllEdges()) {
            if (!e.isActive())
                e.broken = true;
        }

        // Break non-loop nodes
        for (Node[] row : board.nodes) {
            for (Node n : row) {
                int deg = 0;
                for (Edge e : n.edges) {
                    if (e.isActive())
                        deg++;
                }

                if (deg != 2) {
                    n.broken = true;
                }
            }
        }

        System.out.println("Loop Finalized!");
    }
}
