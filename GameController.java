
public class GameController {
    private Board board;
    private Validator validator = new Validator();
    private DPSolver dpSolver = new DPSolver();

    public GameController(Board b) {
        this.board = b;
    }

    public boolean makeHumanMove(Edge e, EdgeStatus newState) {
        return makeMove(e, newState, PlayerType.HUMAN);
    }

    public boolean makeMove(Edge e, EdgeStatus newState, PlayerType player) {
        e.setState(newState, player);
        board.printAllEdges();
        return true;
    }

    /**
     * Executes the DP solver to find a complete solution.
     * In this implementation, the CPU completes the puzzle.
     */
    public boolean executeCpuMove() {
        if (!validator.validatePartial(board)) {
            return false;
        }
        return solve();
    }

    public boolean solve() {
        return dpSolver.solve(board);
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
