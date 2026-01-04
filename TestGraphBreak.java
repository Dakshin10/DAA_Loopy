
import java.util.List;

public class TestGraphBreak {
    public static void main(String[] args) {
        System.out.println("TEST: Dynamic Graph Topology");
        
        // 1. Setup
        Node n1 = new Node(0, 0);
        Node n2 = new Node(0, 1);
        Edge e = new Edge(n1, n2);
        n1.addEdge(e);
        n2.addEdge(e);
        
        // 2. Initial State (UNKNOWN)
        check("Initial", n1, 1, EdgeStatus.UNKNOWN);
        
        // 3. Set Active (Should stay connected)
        e.setActive(PlayerType.CPU);
        check("After Set Active", n1, 1, EdgeStatus.ACTIVE);
        
        // 4. Set Inactive (Should BREAK connection)
        e.setInactive();
        check("After Set Inactive", n1, 0, EdgeStatus.INACTIVE);
        
        // 5. Set Unknown (Should RESTORE connection)
        e.setUnknown();
        check("After Set Unknown", n1, 1, EdgeStatus.UNKNOWN);
        
        // 6. Direct State Set Inactive
        e.setState(EdgeStatus.INACTIVE, PlayerType.HUMAN);
        check("Direct Set Inactive", n1, 0, EdgeStatus.INACTIVE);
        
        // 7. Direct State Set Active (Restore + Active)
        e.setState(EdgeStatus.ACTIVE, PlayerType.HUMAN);
        check("Direct Set Active", n1, 1, EdgeStatus.ACTIVE);
    }
    
    private static void check(String step, Node n, int expectedSize, EdgeStatus status) {
        boolean sizeMatch = n.edges.size() == expectedSize;
        String res = sizeMatch ? "PASS" : "FAIL";
        System.out.printf("[%s] %s: Expected Edges=%d, Actual=%d\n", res, step, expectedSize, n.edges.size());
        if (!sizeMatch) {
            System.err.println("FATAL: Topology mismatch.");
            System.exit(1);
        }
    }
}
