
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for printing board state to console.
 * Moved from Board class to separate rendering concerns.
 */
public class BoardPrinter {
    
    /**
     * Prints the board in a grid format using the rendering context.
     * 
     * @param board The game board
     * @param ctx The rendering context for coordinate mapping
     */
    public static void print(Board board, RenderingContext ctx) {
        int rows = board.getRows();
        int cols = board.getCols();
        
        for (int i = 0; i < rows; i++) {
            // Print horizontal edges
            for (int j = 0; j < cols; j++) {
                System.out.print("+");
                
                Node n1 = ctx.getNodeAt(i, j);
                Node n2 = ctx.getNodeAt(i, j + 1);
                Edge e = findEdgeBetween(n1, n2);
                
                if (e != null && e.isActive())
                    System.out.print("---");
                else if (e != null && e.isInactive())
                    System.out.print(" X ");
                else
                    System.out.print("   ");
            }
            System.out.println("+");
            
            // Print vertical edges and face clues
            for (int j = 0; j < cols; j++) {
                Node n1 = ctx.getNodeAt(i, j);
                Node n2 = ctx.getNodeAt(i + 1, j);
                Edge e = findEdgeBetween(n1, n2);
                
                if (e != null && e.isActive())
                    System.out.print("|");
                else if (e != null && e.isInactive())
                    System.out.print("X");
                else
                    System.out.print(" ");
                
                // Print face clue
                Face face = getFaceAt(board, i, j);
                if (face != null && face.getClue() >= 0)
                    System.out.printf(" %d ", face.getClue());
                else
                    System.out.print("   ");
            }
            
            // Right edge
            Node n1 = ctx.getNodeAt(i, cols);
            Node n2 = ctx.getNodeAt(i + 1, cols);
            Edge rightEdge = findEdgeBetween(n1, n2);
            
            if (rightEdge != null && rightEdge.isActive())
                System.out.println("|");
            else if (rightEdge != null && rightEdge.isInactive())
                System.out.println("X");
            else
                System.out.println(" ");
        }
        
        // Print bottom row of horizontal edges
        for (int j = 0; j < cols; j++) {
            System.out.print("+");
            
            Node n1 = ctx.getNodeAt(rows, j);
            Node n2 = ctx.getNodeAt(rows, j + 1);
            Edge e = findEdgeBetween(n1, n2);
            
            if (e != null && e.isActive())
                System.out.print("---");
            else if (e != null && e.isInactive())
                System.out.print(" X ");
            else
                System.out.print("   ");
        }
        System.out.println("+");
    }
    
    /**
     * Prints the graph topology showing active connections.
     * 
     * @param board The game board
     */
    public static void printTopology(Board board) {
        System.out.println("\n--- GRAPH TOPOLOGY (Active Loops) ---");
        
        for (Node node : board.getNodes()) {
            List<String> neighborIds = new ArrayList<>();
            
            for (Edge e : node.getEdges()) {
                if (e.isActive()) {
                    Node other = (e.a == node) ? e.b : e.a;
                    neighborIds.add(String.valueOf(other.getId()));
                }
            }
            
            if (!neighborIds.isEmpty()) {
                System.out.printf("Node[%d]: %s%n", node.getId(), String.join(", ", neighborIds));
            }
        }
        
        System.out.println("----------------------------------------\n");
    }
    
    /**
     * Prints all nodes and their attributes.
     * 
     * @param board The game board
     */
    public static void printNodes(Board board) {
        System.out.println("Nodes and Attributes:");
        for (Node node : board.getNodes()) {
            System.out.println(node);
        }
    }
    
    /**
     * Prints any rule violations (nodes with degree > 2).
     * 
     * @param board The game board
     */
    public static void printErrors(Board board) {
        boolean foundError = false;
        
        for (Node node : board.getNodes()) {
            int activeDegree = 0;
            for (Edge e : node.getEdges()) {
                if (e.isActive())
                    activeDegree++;
            }
            
            if (activeDegree > 2) {
                if (!foundError) {
                    System.out.println("\n--- TERMINAL RULE VIOLATIONS DETECTED ---");
                    foundError = true;
                }
                System.out.print(node);
            }
        }
    }
    
    /**
     * Helper method to find an edge between two nodes.
     */
    private static Edge findEdgeBetween(Node n1, Node n2) {
        if (n1 == null || n2 == null)
            return null;
        
        for (Edge e : n1.getEdges()) {
            if ((e.a == n1 && e.b == n2) || (e.a == n2 && e.b == n1)) {
                return e;
            }
        }
        return null;
    }
    
    /**
     * Helper method to get a face at a specific grid position.
     */
    private static Face getFaceAt(Board board, int row, int col) {
        int id = row * board.getCols() + col;
        for (Face face : board.getFaces()) {
            if (face.getId() == id)
                return face;
        }
        return null;
    }
}
