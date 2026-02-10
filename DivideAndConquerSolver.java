
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a Divide and Conquer solver for Slitherlink using Backtracking.
 * 
 * Paradigm: Divide and Conquer / Backtracking
 * - Divide: Choose an unassigned edge (UNKNOWN).
 * - Conquer: Recursively solve two subproblems (Edge = ACTIVE, Edge =
 * INACTIVE).
 * - Combine: If either subproblem returns true, the solution is found.
 */
public class DivideAndConquerSolver {

    private Greedy greedySolver;
    private Validator validator;

    public DivideAndConquerSolver(Greedy greedy, Validator validator) {
        this.greedySolver = greedy;
        this.validator = validator;
    }

    /**
     * Solves the Slitherlink puzzle using a Divide and Conquer approach.
     * 
     * @param board The game board to solve.
     * @return true if a solution is found, false otherwise.
     */
    public boolean divideAndConquerSolve(Board board) {
        // Find the pivot for our Divide step (an UNKNOWN edge)
        Edge pivot = findUnknownEdge(board);

        // --- BASE CASE ---
        if (pivot == null) {
            // No more edges to divide. This is the "leaf" of the recursion tree.
            // Check if the final state is a valid solution.
            return validator.validateFinal(board);
        }

        // --- DIVIDE & CONQUER ---

        // Branch 1: Try treating the edge as ACTIVE
        if (solveBranch(board, pivot, EdgeStatus.ACTIVE)) {
            return true;
        }

        // Branch 2: Try treating the edge as INACTIVE
        if (solveBranch(board, pivot, EdgeStatus.INACTIVE)) {
            return true;
        }

        // --- COMBINE (Implicit) ---
        // If neither branch yields a solution, this path is a dead end.
        return false;
    }

    /**
     * Helper method to solve a specific branch (subproblem).
     * 
     * @param board  The game board.
     * @param pivot  The edge to set.
     * @param status The status to try (ACTIVE or INACTIVE).
     * @return true if this branch leads to a solution.
     */
    private boolean solveBranch(Board board, Edge pivot, EdgeStatus status) {
        // 1. APPLY STATE (Conquer Step - part 1)
        EdgeStatus oldStatus = pivot.getStatus();
        pivot.setState(status, PlayerType.CPU);

        // 2. CHECK VIOLATIONS (Pruning)
        // If the immediate move creates an invalid state, prune this branch.
        if (!validator.validatePartial(board)) {
            pivot.setState(oldStatus, PlayerType.NONE); // Backtrack
            return false;
        }

        // 3. PROPAGATE CONSTRAINTS (Greedy "Conquer" assistance)
        // We use the Greedy solver to fill in "forced" moves resulting from our choice.
        // crucial: We capture the log of changes to backtrack later.
        List<Edge> changes = greedySolver.applyGreedyWithLog(board, new ArrayList<>());

        // 4. RECURSE (Divide Step - continued)
        if (divideAndConquerSolve(board)) {
            return true; // Solution found! Bubble up true.
        }

        // 5. BACKTRACK (Undo Step)
        // If recursion failed, we must revert the state to exactly how it was before.

        // Revert greedy propagation
        for (Edge e : changes) {
            // We revert to UNKNOWN because greedy only changes UNKNOWN -> ACTIVE/INACTIVE
            // Note: If greedy ever changes ACTIVE <-> INACTIVE, precise history would be
            // needed,
            // but in this game logic, it only resolves unknowns.
            e.setUnknown();
        }

        // Revert the pivot choice
        pivot.setState(oldStatus, PlayerType.NONE);

        return false;
    }

    /**
     * Heuristic to find the best edge to split on.
     * Simple implementation: First UNKNOWN edge.
     * Better implementation: Most constrained edge (adjacent to high clues).
     */
    private Edge findUnknownEdge(Board board) {
        // Optimized: prioritize edges around cells with clues
        for (Cell[] row : board.cells) {
            for (Cell cell : row) {
                if (cell.clue != -1) {
                    if (cell.top.isUnknown())
                        return cell.top;
                    if (cell.bottom.isUnknown())
                        return cell.bottom;
                    if (cell.left.isUnknown())
                        return cell.left;
                    if (cell.right.isUnknown())
                        return cell.right;
                }
            }
        }

        // Fallback: any unknown edge
        for (Edge e : board.getAllEdges()) {
            if (e.isUnknown()) {
                return e;
            }
        }
        return null;
    }
}
