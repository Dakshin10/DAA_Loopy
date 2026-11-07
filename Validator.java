
import java.util.*;

class DisjointSet {
    public Map<Node, Node> parent = new HashMap<>();
    public Map<Node, Integer> rank = new HashMap<>();

    public Node findParent(Node x) {
        if (parent.get(x) != x) {
            parent.put(x, findParent(parent.get(x)));
        }
        return parent.get(x);
    }

    public void start(Node x) {
        parent.put(x, x);
        rank.put(x, 0);
    }

    public void unite(Node x, Node y) {
        Node px = findParent(x);
        Node py = findParent(y);
        if (px == py)
            return;

        int rx = rank.getOrDefault(px, 0);
        int ry = rank.getOrDefault(py, 0);

        if (rx < ry) {
            parent.put(px, py);
        } else if (rx > ry) {
            parent.put(py, px);
        } else {
            parent.put(py, px);
            rank.put(px, rx + 1);
        }
    }
}

/**
 * Validates the board state using graph-based algorithms.
 * All validation uses graph traversal, not array iteration.
 */
public class Validator {
    
    /**
     * Validates partial state (during gameplay).
     * Checks node degrees, face constraints, and prevents small loops.
     */
    public boolean validatePartial(Board board) {
        return validateNodeDegree(board) && validateFaces(board) && noSmallLoops(board);
    }

    /**
     * Validates final state (win condition).
     * Checks node degrees, exact face satisfaction, and single loop.
     */
    public boolean validateFinal(Board board) {
        if (!validateNodeDegree(board))
            return false;
        if (!validateFaceExact(board))
            return false;
        return singleLoop(board);
    }

    /**
     * Checks that all nodes have degree 0 or 2 (no T-junctions).
     */
    private boolean validateNodeDegree(Board board) {
        for (Node node : board.getNodes()) {
            int degree = node.getActiveDegree();
            if (degree > 2)
                return false;
        }
        return true;
    }

    /**
     * Checks that no face constraint is violated.
     * A face is violated if:
     * - Active edges > clue
     * - Active + unknown < clue (impossible to satisfy)
     */
    private boolean validateFaces(Board board) {
        for (Face face : board.getFaces()) {
            if (face.isViolated())
                return false;
        }
        return true;
    }

    /**
     * Checks that all faces with clues are exactly satisfied.
     */
    private boolean validateFaceExact(Board board) {
        for (Face face : board.getFaces()) {
            if (face.getClue() >= 0 && !face.isSatisfied())
                return false;
        }
        return true;
    }

    /**
     * Checks that all active edges form a single connected loop.
     * Uses Disjoint Set Union (DSU) to verify connectivity.
     */
    private boolean singleLoop(Board board) {
        Set<Node> loopNodes = new HashSet<>();
        DisjointSet ds = new DisjointSet();
        int activeEdgeCount = 0;

        // Count active edges
        for (Edge e : board.getEdges()) {
            if (e.isActive())
                activeEdgeCount++;
        }
        
        System.out.println("activeEdgeCount: " + activeEdgeCount);

        // Find all nodes with degree 2 (part of the loop)
        for (Node node : board.getNodes()) {
            int deg = node.getActiveDegree();

            if (deg != 0 && deg != 2)
                return false;

            if (deg == 2) {
                ds.start(node);
                loopNodes.add(node);
            }
        }

        if (loopNodes.isEmpty())
            return false;

        // Unite all nodes connected by active edges
        for (Edge e : board.getEdges()) {
            if (e.isActive()) {
                ds.unite(e.a, e.b);
            }
        }

        // Check that all loop nodes are in the same connected component
        Node first = loopNodes.iterator().next();
        Node root = ds.findParent(first);
        for (Node node : loopNodes) {
            if (ds.findParent(node) != root)
                return false;
        }

        // Verify edge count matches node count (Euler's formula for a cycle)
        return activeEdgeCount == loopNodes.size();
    }

    /**
     * Prevents small disconnected loops from forming during gameplay.
     * If multiple components exist and any is a complete loop (all degree 2),
     * it's invalid (we want only one final loop).
     */
    private boolean noSmallLoops(Board board) {
        List<Edge> activeEdges = new ArrayList<>();
        for (Edge e : board.getEdges()) {
            if (e.isActive())
                activeEdges.add(e);
        }
        
        if (activeEdges.isEmpty())
            return true;

        DisjointSet ds = new DisjointSet();
        for (Edge e : activeEdges) {
            if (!ds.parent.containsKey(e.a))
                ds.start(e.a);
            if (!ds.parent.containsKey(e.b))
                ds.start(e.b);
            ds.unite(e.a, e.b);
        }

        // Group nodes by connected component
        Map<Node, List<Node>> components = new HashMap<>();
        for (Node n : ds.parent.keySet()) {
            Node root = ds.findParent(n);
            components.computeIfAbsent(root, k -> new ArrayList<>()).add(n);
        }

        if (components.size() <= 1)
            return true;

        // Check if any component is a complete loop
        for (Map.Entry<Node, List<Node>> entry : components.entrySet()) {
            List<Node> nodes = entry.getValue();
            boolean allDegree2 = true;
            
            for (Node n : nodes) {
                int degree = n.getActiveDegree();
                if (degree != 2) {
                    allDegree2 = false;
                    break;
                }
            }

            // If we found a complete loop and there are multiple components, it's invalid
            if (allDegree2) {
                if (components.size() > 1)
                    return false;
            }
        }

        return true;
    }
}
