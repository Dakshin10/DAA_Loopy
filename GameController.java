
public class GameController {
    private Board board;
    private Validator validator = new Validator();
    private Greedy greedy = new Greedy();

    public GameController(Board b) {
        this.board = b;
        greedy.validator = validator;
    }

    public boolean makeHumanMove(Edge e, EdgeStatus newState) {
        return makeMove(e, newState, PlayerType.HUMAN);
    }

    public boolean makeMove(Edge e, EdgeStatus newState, PlayerType player) {
        e.setState(newState, player);
        board.printAllEdges();
        // Relaxed rules: always allow moves, UI will show errors.
        return true;
    }

    /**
     * Executes a single logical move for the CPU.
     */
    public boolean executeCpuMove() {
        if (!validator.validatePartial(board)) {
            return false;
        }

        // Snapshot state to detect changes
        int initialSetEdges = countSetEdges();

        // 1. Run Greedy Solver (Propagation + Finalization)
        greedy.solve(board);

        if (countSetEdges() != initialSetEdges) {
            return true; // Greedy made progress
        }

        // 2. Fallback to Backtracking-lite
        Backtracking solver = new Backtracking();
        Edge best = solver.pickBestUnknownEdge(board);
        if (best == null)
            return false;

        // Hypothesis: Try ACTIVE
        best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
        // board.printAllEdges(); // Optional logging
        if (!validator.validatePartial(board)) {
            // Must be INACTIVE. Check if that's valid.
            best.setState(EdgeStatus.INACTIVE, PlayerType.CPU);
            if (validator.validatePartial(board)) {
                return true;
            } else {
                best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
                return false;
            }
        }

        // Hypothesis: Try INACTIVE
        best.setState(EdgeStatus.INACTIVE, PlayerType.CPU);
        if (!validator.validatePartial(board)) {
            // Must be ACTIVE.
            best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
            if (validator.validatePartial(board)) {
                return true;
            } else {
                best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
                return false;
            }
        }

        // Proactive guess: ACTIVE
        best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
        if (validator.validatePartial(board)) {
            return true;
        } else {
            best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
            return false;
        }
    }

    private int countSetEdges() {
        int count = 0;
        for (Edge e : board.getAllEdges()) {
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

    public void printBoard() {
        board.print();
    }

    public Board getBoard() {
        return board;
    }
}
