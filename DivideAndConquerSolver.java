import java.util.*;

/**
 * Divide & Conquer solver for Slitherlink puzzles.
 * 
 * Strategy:
 * 1. DIVIDE: Partition UNKNOWN edges into independent subproblems using Union-Find
 * 2. CONQUER: Solve each subproblem independently using greedy + limited backtracking
 * 3. MERGE: Validate global loop connectivity after all subproblems are solved
 */
public class DivideAndConquerSolver {
    private Greedy greedy = new Greedy();
    private Validator validator = new Validator();
    private static final int MIN_SUBPROBLEM_SIZE = 3;
    private static final int MAX_BACKTRACK_DEPTH = 3;
    
    /**
     * Attempts to solve the board using divide & conquer strategy.
     * 
     * @param board The game board
     * @param solverType The player type making the moves
     * @return true if progress was made, false otherwise
     */
    public boolean solve(Board board, PlayerType solverType) {
        greedy.validator = validator;
        
        // First, apply greedy propagation globally
        greedy.solve(board);
        
        // Check if already solved
        if (validator.validateFinal(board)) {
            return true;
        }
        
        // Divide into subproblems
        List<Subproblem> subproblems = divideIntoSubproblems(board);
        
        // If only one subproblem or too small, fall back to standard solving
        if (subproblems.size() <= 1) {
            return false; // Let caller use standard backtracking
        }
        
        // Filter out tiny subproblems
        subproblems.removeIf(sub -> sub.size() < MIN_SUBPROBLEM_SIZE);
        
        if (subproblems.isEmpty()) {
            return false;
        }
        
        System.out.println("D&C: Found " + subproblems.size() + " independent subproblems");
        
        // Sort by size (solve smallest first)
        subproblems.sort(Comparator.comparingInt(Subproblem::size));
        
        // Solve each subproblem independently
        for (Subproblem sub : subproblems) {
            System.out.println("D&C: Solving " + sub);
            
            if (!solveSubproblem(sub, board, solverType)) {
                System.out.println("D&C: Subproblem " + sub.getId() + " failed");
                return false; // Early termination
            }
        }
        
        // Validate global solution
        if (validator.validateFinal(board)) {
            System.out.println("D&C: Global validation successful!");
            return true;
        }
        
        return false;
    }

    /**
     * Makes a single move using divide & conquer strategy.
     * Attempts to make one edge decision from the first available subproblem.
     * 
     * @param board The game board
     * @param solverType The player type making the moves
     * @return true if a move was made, false otherwise
     */
    public boolean solveSingleMove(Board board, PlayerType solverType) {
        greedy.validator = validator;
        
        // Divide into subproblems
        List<Subproblem> subproblems = divideIntoSubproblems(board);
        
        // If no meaningful subproblems, return false
        if (subproblems.size() <= 1) {
            return false;
        }
        
        // Filter out tiny subproblems
        subproblems.removeIf(sub -> sub.size() < MIN_SUBPROBLEM_SIZE);
        
        if (subproblems.isEmpty()) {
            return false;
        }
        
        // Sort by size (solve smallest first)
        subproblems.sort(Comparator.comparingInt(Subproblem::size));
        
        // Try to make one move from the first subproblem
        for (Subproblem sub : subproblems) {
            // Try greedy propagation on this subproblem's faces
            for (Face face : sub.getInvolvedFaces()) {
                if (face.getClue() < 0) continue;
                
                int active = face.activeCount();
                int unknown = face.unknownCount();
                int clue = face.getClue();
                
                // Rule 1: clue satisfied → rest INACTIVE
                if (active == clue) {
                    for (Edge e : face.getEdges()) {
                        if (e != null && e.isUnknown() && sub.getUnknownEdges().contains(e)) {
                            EdgeStatus old = e.getStatus();
                            e.setState(EdgeStatus.INACTIVE, solverType);
                            
                            if (!validator.validatePartial(board)) {
                                e.setState(old, PlayerType.NONE);
                            } else {
                                System.out.println("D&C: Made single move (INACTIVE) in subproblem " + sub.getId());
                                return true; // Made one move
                            }
                        }
                    }
                }
                
                // Rule 2: remaining edges forced ACTIVE
                if (unknown == clue - active) {
                    for (Edge e : face.getEdges()) {
                        if (e != null && e.isUnknown() && sub.getUnknownEdges().contains(e)) {
                            EdgeStatus old = e.getStatus();
                            e.setState(EdgeStatus.ACTIVE, solverType);
                            
                            if (!validator.validatePartial(board)) {
                                e.setState(old, PlayerType.NONE);
                            } else {
                                System.out.println("D&C: Made single move (ACTIVE) in subproblem " + sub.getId());
                                return true; // Made one move
                            }
                        }
                    }
                }
            }
        }
        
        return false; // No move found
    }

    
    /**
     * Divides the board into independent subproblems using Union-Find.
     * 
     * @param board The game board
     * @return List of independent subproblems
     */
    private List<Subproblem> divideIntoSubproblems(Board board) {
        // Collect all UNKNOWN edges
        List<Edge> unknownEdges = new ArrayList<>();
        for (Edge e : board.getEdges()) {
            if (e.isUnknown()) {
                unknownEdges.add(e);
            }
        }
        
        if (unknownEdges.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Initialize Union-Find
        Map<Edge, Edge> parent = new HashMap<>();
        for (Edge e : unknownEdges) {
            parent.put(e, e);
        }
        
        // Unite edges that share nodes or faces
        for (int i = 0; i < unknownEdges.size(); i++) {
            Edge e1 = unknownEdges.get(i);
            
            // Check edges connected through nodes
            for (Edge e2 : e1.a.getEdges()) {
                if (e2.isUnknown() && e2 != e1) {
                    union(parent, e1, e2);
                }
            }
            for (Edge e2 : e1.b.getEdges()) {
                if (e2.isUnknown() && e2 != e1) {
                    union(parent, e1, e2);
                }
            }
            
            // Check edges connected through faces
            for (Face face : board.getFaces()) {
                List<Edge> faceEdges = face.getEdges();
                boolean hasE1 = faceEdges.contains(e1);
                
                if (hasE1) {
                    for (Edge e2 : faceEdges) {
                        if (e2.isUnknown() && e2 != e1) {
                            union(parent, e1, e2);
                        }
                    }
                }
            }
        }
        
        // Group edges by their root
        Map<Edge, Set<Edge>> components = new HashMap<>();
        for (Edge e : unknownEdges) {
            Edge root = find(parent, e);
            components.computeIfAbsent(root, k -> new HashSet<>()).add(e);
        }
        
        // Build subproblems
        List<Subproblem> subproblems = new ArrayList<>();
        int id = 0;
        
        for (Set<Edge> edgeGroup : components.values()) {
            // Collect involved nodes
            Set<Node> nodes = new HashSet<>();
            for (Edge e : edgeGroup) {
                nodes.add(e.a);
                nodes.add(e.b);
            }
            
            // Collect involved faces
            Set<Face> faces = new HashSet<>();
            for (Face face : board.getFaces()) {
                for (Edge e : face.getEdges()) {
                    if (edgeGroup.contains(e)) {
                        faces.add(face);
                        break;
                    }
                }
            }
            
            subproblems.add(new Subproblem(id++, edgeGroup, nodes, faces));
        }
        
        return subproblems;
    }
    
    /**
     * Solves a single subproblem independently.
     * 
     * @param sub The subproblem to solve
     * @param board The game board
     * @param solverType The player type
     * @return true if solved successfully
     */
    private boolean solveSubproblem(Subproblem sub, Board board, PlayerType solverType) {
        // Save state before attempting to solve
        Map<Edge, EdgeStatus> snapshot = new HashMap<>();
        for (Edge e : sub.getUnknownEdges()) {
            snapshot.put(e, e.getStatus());
        }
        
        // Apply greedy propagation focused on this subproblem's faces
        boolean changed;
        do {
            changed = false;
            for (Face face : sub.getInvolvedFaces()) {
                if (face.getClue() < 0) continue;
                
                int active = face.activeCount();
                int unknown = face.unknownCount();
                int clue = face.getClue();
                
                // Rule 1: clue satisfied → rest INACTIVE
                if (active == clue) {
                    for (Edge e : face.getEdges()) {
                        if (e != null && e.isUnknown() && sub.getUnknownEdges().contains(e)) {
                            EdgeStatus old = e.getStatus();
                            e.setState(EdgeStatus.INACTIVE, solverType);
                            
                            if (!validator.validatePartial(board)) {
                                e.setState(old, PlayerType.NONE);
                            } else {
                                changed = true;
                            }
                        }
                    }
                }
                
                // Rule 2: remaining edges forced ACTIVE
                if (unknown == clue - active) {
                    for (Edge e : face.getEdges()) {
                        if (e != null && e.isUnknown() && sub.getUnknownEdges().contains(e)) {
                            EdgeStatus old = e.getStatus();
                            e.setState(EdgeStatus.ACTIVE, solverType);
                            
                            if (!validator.validatePartial(board)) {
                                e.setState(old, PlayerType.NONE);
                            } else {
                                changed = true;
                            }
                        }
                    }
                }
            }
        } while (changed);
        
        // Check if subproblem is fully solved
        boolean allDecided = true;
        for (Edge e : sub.getUnknownEdges()) {
            if (e.isUnknown()) {
                allDecided = false;
                break;
            }
        }
        
        if (allDecided) {
            return true;
        }
        
        // Try limited backtracking if greedy didn't solve it
        return solveWithLimitedBacktracking(sub, board, solverType, 0);
    }
    
    /**
     * Applies limited backtracking to solve remaining UNKNOWN edges in subproblem.
     */
    private boolean solveWithLimitedBacktracking(Subproblem sub, Board board, PlayerType solverType, int depth) {
        if (depth > MAX_BACKTRACK_DEPTH) {
            return false; // Depth limit reached
        }
        
        // Find first UNKNOWN edge in this subproblem
        Edge unknownEdge = null;
        for (Edge e : sub.getUnknownEdges()) {
            if (e.isUnknown()) {
                unknownEdge = e;
                break;
            }
        }
        
        if (unknownEdge == null) {
            return true; // All edges decided
        }
        
        // Try both states
        EdgeStatus[] options = { EdgeStatus.ACTIVE, EdgeStatus.INACTIVE };
        for (EdgeStatus status : options) {
            unknownEdge.setState(status, solverType);
            
            if (validator.validatePartial(board)) {
                // Recursively solve
                if (solveWithLimitedBacktracking(sub, board, solverType, depth + 1)) {
                    return true;
                }
            }
            
            // Backtrack
            unknownEdge.setState(EdgeStatus.UNKNOWN, PlayerType.NONE);
        }
        
        return false;
    }
    
    // Union-Find helper methods
    private Edge find(Map<Edge, Edge> parent, Edge e) {
        if (parent.get(e) != e) {
            parent.put(e, find(parent, parent.get(e)));
        }
        return parent.get(e);
    }
    
    private void union(Map<Edge, Edge> parent, Edge e1, Edge e2) {
        Edge root1 = find(parent, e1);
        Edge root2 = find(parent, e2);
        if (root1 != root2) {
            parent.put(root2, root1);
        }
    }
}
