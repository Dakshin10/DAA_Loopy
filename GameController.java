
public class GameController {
    private Board board;
    private Validator validator = new Validator();
    private HybridDPSolver dpSolver = new HybridDPSolver();

    public GameController(Board b) {
        this.board = b;
    }

    public boolean makeHumanMove(Edge e, EdgeStatus newState) {
        return makeMove(e, newState, PlayerType.HUMAN);
    }

    public boolean makeMove(Edge e, EdgeStatus newState, PlayerType player) {
        EdgeStatus oldState = e.getStatus();
        e.setState(newState, player);

        if (!validator.validatePartial(board)) {
            // Revert illegal move
            e.setState(oldState, PlayerType.NONE);
            return false;
        }

        board.printAllEdges();
        return true;
    }

    public boolean cpuSingleMove() {
        if (!validator.validatePartial(board)) {
            return false;
        }

        // 1. Save current board state
        java.util.List<Edge> allEdges = board.getAllEdges();
        EdgeStatus[] savedStatuses = new EdgeStatus[allEdges.size()];
        PlayerType[] savedOwners = new PlayerType[allEdges.size()];
        for (int i = 0; i < allEdges.size(); i++) {
            Edge e = allEdges.get(i);
            savedStatuses[i] = e.getStatus();
            savedOwners[i] = e.getOwner();
        }

        // 2. Run the solver
        boolean solved = dpSolver.solve(board);
        if (!solved) {
            // Restore if no solution
            for (int i = 0; i < allEdges.size(); i++) {
                Edge e = allEdges.get(i);
                Edge.applyState(e, savedStatuses[i], savedOwners[i]);
            }
            return false;
        }

        // 3. Find one edge that changed from UNKNOWN to ACTIVE/INACTIVE
        Edge moveEdge = null;
        EdgeStatus moveStatus = EdgeStatus.UNKNOWN;

        for (int i = 0; i < allEdges.size(); i++) {
            Edge e = allEdges.get(i);
            EdgeStatus currStatus = e.getStatus(); // What the solver placed
            
            // Revert the edge to its original state no matter what
            Edge.applyState(e, savedStatuses[i], savedOwners[i]);

            if (moveEdge == null && savedStatuses[i] == EdgeStatus.UNKNOWN && currStatus != EdgeStatus.UNKNOWN) {
                moveEdge = e;
                moveStatus = currStatus;
            }
        }

        // 4. Apply exactly that one move
        if (moveEdge != null) {
            return makeMove(moveEdge, moveStatus, PlayerType.CPU);
        }

        return false;
    }

    /**
     * Executes the DP solver to find a complete solution.
     * In this implementation, the CPU completes the puzzle.
     */
    public boolean executeFullSolve() {
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
