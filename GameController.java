
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
            // Already in conflict, don't move.
            return false;
        }

        // 1. Try logical deductions (Greedy + Graph)
        if (greedy.applyOneMove(board, PlayerType.CPU))
            return true;
        if (greedy.applyNodeLogic(board, PlayerType.CPU))
            return true;

        // 2. Fallback to Backtracking-lite
        Backtracking solver = new Backtracking();
        Edge best = solver.pickBestUnknownEdge(board);
        if (best == null)
            return false;

        // Hypothesis: Try ACTIVE
        best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
        board.printAllEdges();
        if (!validator.validatePartial(board)) {
            // Must be INACTIVE. Check if that's valid.
            best.setState(EdgeStatus.INACTIVE, PlayerType.CPU);
            board.printAllEdges();
            if (validator.validatePartial(board)) {
                return true;
            } else {
                best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
                board.printAllEdges();
                return false;
            }
        }

        // Hypothesis: Try INACTIVE
        best.setState(EdgeStatus.INACTIVE, PlayerType.CPU);
        board.printAllEdges();
        if (!validator.validatePartial(board)) {
            // Must be ACTIVE. Check if that's valid.
            best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
            board.printAllEdges();
            if (validator.validatePartial(board)) {
                return true;
            } else {
                best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
                board.printAllEdges();
                return false;
            }
        }

        // Proactive guess: ACTIVE
        best.setState(EdgeStatus.ACTIVE, PlayerType.CPU);
        board.printAllEdges();
        if (validator.validatePartial(board)) {
            return true;
        } else {
            best.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
            board.printAllEdges();
            return false;
        }
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
