
public class GameController {
    private Board board;
    private Validator validator = new Validator();
    private Greedy greedy = new Greedy();
    private DivideAndConquerSolver dncSolver;

    public GameController(Board b) {
        this.board = b;
        greedy.validator = validator;
        this.dncSolver = new DivideAndConquerSolver(greedy, validator);
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

        // 2. Fallback to D&C branch suggestion
        // This is a "hint" mode, so we just want one move.
        // We can pick an edge and try one of its states using the D&C logic.
        return solve(); // For now, let's just use solve to complete the puzzle or find next move.
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
        return dncSolver.divideAndConquerSolve(board);
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
