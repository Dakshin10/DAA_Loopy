
import java.util.Map;

public class GameController {
    private Board board;
    private Validator validator = new Validator();
    private Greedy greedy = new Greedy();
    private DivideAndConquerSolver dcSolver = new DivideAndConquerSolver();

    public GameController(Board b) {
        this.board = b;
        greedy.validator = validator;
    }

    public boolean makeHumanMove(Edge e, EdgeStatus newState) {
        return makeMove(e, newState, PlayerType.HUMAN);
    }

    public boolean makeMove(Edge e, EdgeStatus newState, PlayerType player) {
        e.setState(newState, player);
        // Relaxed rules: always allow moves, UI will show errors.
        return true;
    }

    /**
     * Executes a single logical move for the CPU.
     * Tries algorithms in order: Greedy → Divide & Conquer → Backtracking
     * Returns true if exactly one edge was changed, false otherwise.
     */
    public boolean executeCpuMove() {
        if (!validator.validatePartial(board)) {
            return false;
        }

        // Strategy 1: Try Greedy Solver (fastest, most reliable)
        if (greedy.solveSingleMove(board)) {
            System.out.println("CPU: Greedy made a move");
            return true;
        }

        // Strategy 2: Try Divide & Conquer (for independent subproblems)
        if (dcSolver.solveSingleMove(board, PlayerType.CPU)) {
            System.out.println("CPU: D&C made a move");
            return true;
        }

        // Strategy 3: Fallback to Backtracking (deep search)
        Backtracking solver = new Backtracking();
        Edge best = solver.pickBestUnknownEdge(board);
        if (best == null) {
            return false; // No unknown edges left
        }

        // Take a snapshot of the current board state
        Map<Edge, EdgeStatus> snapshot = new java.util.HashMap<>();
        for (Edge e : board.getEdges()) {
            snapshot.put(e, e.getStatus());
        }

        // Run full backtracking search to find a valid solution
        boolean solved = solver.solve(board, PlayerType.CPU);

        if (solved) {
            // Find the first edge that changed from UNKNOWN → something
            // and commit only that one move (the rest will be re-discovered later)
            for (Edge e : board.getEdges()) {
                EdgeStatus before = snapshot.get(e);
                EdgeStatus after  = e.getStatus();
                if (before == EdgeStatus.UNKNOWN && after != EdgeStatus.UNKNOWN) {
                    // Restore the full board, then commit just this one move
                    for (Map.Entry<Edge, EdgeStatus> entry : snapshot.entrySet()) {
                        entry.getKey().setState(entry.getValue(), PlayerType.NONE);
                    }
                    e.setState(after, PlayerType.CPU);
                    System.out.println("CPU: Backtracking chose " + after + " for " + e);
                    return true;
                }
            }
        }

        // Backtracking found no solution — restore and give up
        for (Map.Entry<Edge, EdgeStatus> entry : snapshot.entrySet()) {
            entry.getKey().setState(entry.getValue(), PlayerType.NONE);
        }
        System.out.println("CPU: Backtracking found no valid move");
        return false;
    }

    private int countSetEdges() {
        int count = 0;
        for (Edge e : board.getEdges()) {
            if (e.getStatus() != EdgeStatus.UNKNOWN) {
                count++;
            }
        }
        return count;
    }

    public boolean solve() {
        Backtracking solver = new Backtracking();
        return solver.solve(board, PlayerType.CPU);
    }

    public boolean isSolved() {
        return validator.validateFinal(board);
    }

    public boolean isValid() {
        return validator.validatePartial(board);
    }

    public void printBoard(RenderingContext ctx) {
        BoardPrinter.print(board, ctx);
    }

    public Board getBoard() {
        return board;
    }
}
