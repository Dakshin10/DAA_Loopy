import java.util.*;

public class Greedy {
    public Validator validator;

    /**
     * Finds and applies the first greedy move possible.
     * Returns true if a move was found and applied.
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

    private boolean applyCellAtomic(Cell cell, Board board, PlayerType player) {
        if (cell.clue == -1)
            return false;
        int active = cell.lineCount();
        int unknown = cell.unknownCount();

        if (active == cell.clue && unknown > 0) {
            // Set all unknown to INACTIVE
            if (cell.top != null && cell.top.isUnknown()) {
                if (change(cell.top, EdgeStatus.INACTIVE, board, validator, player))
                    return true;
            }
            if (cell.bottom != null && cell.bottom.isUnknown()) {
                if (change(cell.bottom, EdgeStatus.INACTIVE, board, validator, player))
                    return true;
            }
            if (cell.left != null && cell.left.isUnknown()) {
                if (change(cell.left, EdgeStatus.INACTIVE, board, validator, player))
                    return true;
            }
            if (cell.right != null && cell.right.isUnknown()) {
                if (change(cell.right, EdgeStatus.INACTIVE, board, validator, player))
                    return true;
            }
        }
        if (active + unknown == cell.clue && unknown > 0) {
            // Set all unknown to ACTIVE
            if (cell.top != null && cell.top.isUnknown()) {
                if (change(cell.top, EdgeStatus.ACTIVE, board, validator, player))
                    return true;
            }
            if (cell.bottom != null && cell.bottom.isUnknown()) {
                if (change(cell.bottom, EdgeStatus.ACTIVE, board, validator, player))
                    return true;
            }
            if (cell.left != null && cell.left.isUnknown()) {
                if (change(cell.left, EdgeStatus.ACTIVE, board, validator, player))
                    return true;
            }
            if (cell.right != null && cell.right.isUnknown()) {
                if (change(cell.right, EdgeStatus.ACTIVE, board, validator, player))
                    return true;
            }
        }
        return false;
    }

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
                    // Must continue the path
                    if (change(unknown.get(0), EdgeStatus.ACTIVE, board, validator, player))
                        return true;
                }
                if (active == 2 && unknown.size() > 0) {
                    // Path already complete, other edges must be inactive
                    if (change(unknown.get(0), EdgeStatus.INACTIVE, board, validator, player))
                        return true;
                }
            }
        }
        return false;
    }

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

    private boolean applyCell(Cell cell, Board board, Validator validator, PlayerType player) {
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
                    if (change(e, EdgeStatus.INACTIVE, board, validator, player)) {
                        changed = true;
                    }
                }
            }
        }

        if (unknown == cell.clue - active) {
            for (Edge e : edges) {
                if (e != null && e.isUnknown()) {
                    if (change(e, EdgeStatus.ACTIVE, board, validator, player)) {
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    private boolean change(Edge e, EdgeStatus newStatus, Board board, Validator validator, PlayerType player) {
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
