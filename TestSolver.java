
public class TestSolver {
    public static void main(String[] args) {
        System.out.println("Starting Divide and Conquer Solver Test...");

        // 1. Setup Board (Small 3x3 for testing)
        Board board = new Board(3, 3);

        // 2. Setup Dependencies
        Validator validator = new Validator();
        Greedy greedy = new Greedy();
        greedy.validator = validator;

        DivideAndConquerSolver solver = new DivideAndConquerSolver(greedy, validator);

        // 3. Set a simple config (e.g. a simple valid loop config via clues)
        // Center 3, corners 2 to force a shape and speed up search.
        board.cells[1][1].clue = 3;
        board.cells[0][0].clue = 2;
        board.cells[0][2].clue = 2;
        board.cells[2][0].clue = 2;
        board.cells[2][2].clue = 2;

        System.out.println("Initial Board with Clues:");
        // board.print();

        long startTime = System.currentTimeMillis();
        boolean result = solver.divideAndConquerSolve(board);
        long endTime = System.currentTimeMillis();

        System.out.println("Solver finished in " + (endTime - startTime) + "ms");
        System.out.println("Result: " + result);

        if (result) {
            System.out.println("Solved Board:");
            // board.print();

            // Verify final state
            if (validator.validateFinal(board)) {
                System.out.println("VALIDATION SUCCESS: The final board state is valid.");
            } else {
                System.out.println("VALIDATION FAILED: The solver returned true but the board is invalid.");
            }
        } else {
            System.out.println("No solution found.");
        }
    }
}
