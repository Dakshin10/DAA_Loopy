
import java.util.*;

public class Backtracking {
    private Greedy greedy = new Greedy();
    private Validator validator = new Validator();

    public Edge pickBestUnknownEdge(Board board) {
        List<Edge> unknown = new ArrayList<>();
        for (Edge e : board.getAllEdges()) {
            if (e.isUnknown())
                unknown.add(e);
        }
        if (unknown.isEmpty())
            return null;

        // Sort unknown edges to pick the most constrained ones first
        EdgeSorter.sortEdges(unknown, board);
        return unknown.get(0);
    }

    public boolean solve(Board board) {
        return solve(board, PlayerType.NONE);
    }

    public boolean solve(Board board, PlayerType solverType) {
        // Save state of all unknown edges before making any moves
        Map<Edge, EdgeStatus> snapshot = new HashMap<>();
        for (Edge e : board.getAllEdges()) {
            if (e.isUnknown()) {
                snapshot.put(e, EdgeStatus.UNKNOWN);
            }
        }

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
            restore(snapshot);
            return false;
        }

        EdgeStatus[] options = { EdgeStatus.ACTIVE, EdgeStatus.INACTIVE };
        for (EdgeStatus s : options) {
            e.setState(s, solverType);
            if (validator.validatePartial(board)) {
                if (solve(board, solverType))
                    return true;
            }
            // The recursive call to solve() will have restored its own mess,
            // but we still need to clear 'e' and its subsequent greedy moves.
            e.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
        }

        restore(snapshot);
        return false;
    }

    private void restore(Map<Edge, EdgeStatus> snapshot) {
        for (Map.Entry<Edge, EdgeStatus> entry : snapshot.entrySet()) {
            entry.getKey().setState(entry.getValue(), PlayerType.NONE);
        }
    }
}
