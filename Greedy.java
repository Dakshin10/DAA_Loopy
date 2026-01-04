import java.util.*;

public class Greedy {

    public Validator validator;

    /**
     * Finds and applies the first greedy move possible.
     *
     * Time Complexity: O(N)
     * Space Complexity: O(1)
     * where N = number of cells (rows × cols)
     */
    public boolean applyOneMove(Board board, PlayerType player) {
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                if (applyCellAtomic(board.cells[r][c], board, player)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Applies atomic logic to a single cell.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private boolean applyCellAtomic(Cell cell, Board board, PlayerType player) {
        if (cell.clue == -1)
            return false;

        int active = cell.lineCount();
        int unknown = cell.unknownCount();

        if (active == cell.clue && unknown > 0) {
            if (cell.top != null && cell.top.isUnknown())
                return change(cell.top, EdgeStatus.INACTIVE, board, validator, player);

            if (cell.bottom != null && cell.bottom.isUnknown())
                return change(cell.bottom, EdgeStatus.INACTIVE, board, validator, player);

            if (cell.left != null && cell.left.isUnknown())
                return change(cell.left, EdgeStatus.INACTIVE, board, validator, player);

            if (cell.right != null && cell.right.isUnknown())
                return change(cell.right, EdgeStatus.INACTIVE, board, validator, player);
        }

        if (active + unknown == cell.clue && unknown > 0) {
            if (cell.top != null && cell.top.isUnknown())
                return change(cell.top, EdgeStatus.ACTIVE, board, validator, player);

            if (cell.bottom != null && cell.bottom.isUnknown())
                return change(cell.bottom, EdgeStatus.ACTIVE, board, validator, player);

            if (cell.left != null && cell.left.isUnknown())
                return change(cell.left, EdgeStatus.ACTIVE, board, validator, player);

            if (cell.right != null && cell.right.isUnknown())
                return change(cell.right, EdgeStatus.ACTIVE, board, validator, player);
        }
        return false;
    }

    /**
     * Applies node-based loop constraints.
     *
     * Time Complexity: O(N)
     * Space Complexity: O(1)
     */
    public boolean applyNodeLogic(Board board, PlayerType player) {
        if (validator == null)
            return false;

        for (Node[] row : board.nodes) {
            for (Node node : row) {
                int active = 0;
                List<Edge> unknown = new ArrayList<>();

                for (Edge e : node.edges) {
                    if (e.isActive())
                        active++;
                    else if (e.isUnknown())
                        unknown.add(e);
                }

                if (active == 1 && unknown.size() == 1) {
                    return change(unknown.get(0), EdgeStatus.ACTIVE, board, validator, player);
                }

                if (active == 2 && unknown.size() > 0) {
                    return change(unknown.get(0), EdgeStatus.INACTIVE, board, validator, player);
                }
            }
        }
        return false;
    }

    /**
     * Repeatedly applies greedy logic until no changes occur.
     *
     * Time Complexity: O(N² · V)
     * Space Complexity: O(1)
     * where V = cost of validator.validatePartial()
     */
    public void applyGreedy(Board board, PlayerType player) {
        if (validator == null)
            return;

        boolean changed;
        do {
            changed = false;
            for (Cell[] row : board.cells) {
                for (Cell cell : row) {
                    if (applyCell(cell, board, validator, player)) {
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    /**
     * Applies non-atomic cell logic.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    private boolean applyCell(Cell cell, Board board,
                              Validator validator, PlayerType player) {
        if (cell.clue < 0)
            return false;

        int active = 0, unknown = 0;
        Edge[] edges = { cell.top, cell.bottom, cell.left, cell.right };

        for (Edge e : edges) {
            if (e != null) {
                if (e.isActive())
                    active++;
                else if (e.isUnknown())
                    unknown++;
            }
        }

        boolean changed = false;

        if (active == cell.clue) {
            for (Edge e : edges) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.INACTIVE, board, validator, player))
                        changed = true;
                }
            }
        }

        if (unknown == cell.clue - active) {
            for (Edge e : edges) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.ACTIVE, board, validator, player))
                        changed = true;
                }
            }
        }

        return changed;
    }

    /**
     * Safely applies an edge state change.
     *
     * Time Complexity: O(V)
     * Space Complexity: O(1)
     * where V = cost of validator.validatePartial()
     */
    private boolean change(Edge e, EdgeStatus newStatus,
                           Board board, Validator validator,
                           PlayerType player) {
        EdgeStatus old = e.getStatus();
        e.setState(newStatus, player);

        if (!validator.validatePartial(board)) {
            e.setState(old, PlayerType.NONE);
            return false;
        }

        board.printAllEdges();
        return true;
    }
}
