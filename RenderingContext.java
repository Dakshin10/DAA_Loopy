
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the separation between the graph model and visual rendering.
 * Maps graph elements (Nodes, Edges, Faces) to screen coordinates.
 * This is the ONLY class that should know about grid positions.
 */
public class RenderingContext {
    private final Board board;
    private final int cellSize;
    private final int margin;
    
    // Mapping from graph elements to screen positions
    private final Map<Node, Point> nodePositions;
    private final Map<Face, Rectangle> faceRegions;
    private final Map<Integer, Node> idToNode; // For reverse lookup
    
    /**
     * Creates a rendering context for the given board.
     * 
     * @param board The game board
     * @param cellSize Size of each cell in pixels
     * @param margin Margin around the board in pixels
     */
    public RenderingContext(Board board, int cellSize, int margin) {
        this.board = board;
        this.cellSize = cellSize;
        this.margin = margin;
        this.nodePositions = new HashMap<>();
        this.faceRegions = new HashMap<>();
        this.idToNode = new HashMap<>();
        
        computeLayout();
    }
    
    /**
     * Computes the layout by mapping node IDs to grid positions.
     * Node IDs are assigned in row-major order: id = row * (cols+1) + col
     */
    private void computeLayout() {
        int rows = board.getRows();
        int cols = board.getCols();
        
        // Map each node to its screen position based on ID
        for (Node node : board.getNodes()) {
            int id = node.getId();
            int row = id / (cols + 1);
            int col = id % (cols + 1);
            
            int x = margin + col * cellSize;
            int y = margin + row * cellSize;
            
            nodePositions.put(node, new Point(x, y));
            idToNode.put(id, node);
        }
        
        // Map each face to its screen rectangle
        for (Face face : board.getFaces()) {
            int id = face.getId();
            int row = id / cols;
            int col = id % cols;
            
            int x = margin + col * cellSize;
            int y = margin + row * cellSize;
            
            faceRegions.put(face, new Rectangle(x, y, cellSize, cellSize));
        }
    }
    
    /**
     * Gets the screen position of a node.
     * 
     * @param node The node
     * @return Screen coordinates (x, y)
     */
    public Point getNodePosition(Node node) {
        return nodePositions.get(node);
    }
    
    /**
     * Gets the screen rectangle for a face.
     * 
     * @param face The face
     * @return Screen rectangle
     */
    public Rectangle getFaceRegion(Face face) {
        return faceRegions.get(face);
    }
    
    /**
     * Gets a node by its grid position.
     * 
     * @param row Grid row
     * @param col Grid column
     * @return The node at that position, or null if out of bounds
     */
    public Node getNodeAt(int row, int col) {
        int id = row * (board.getCols() + 1) + col;
        return idToNode.get(id);
    }
    
    /**
     * Finds the edge closest to the given screen coordinates.
     * Uses geometric distance calculation to find the nearest edge.
     * 
     * @param x Screen x coordinate
     * @param y Screen y coordinate
     * @return The closest edge, or null if none within threshold
     */
    public Edge findEdgeAt(int x, int y) {
        Edge closestEdge = null;
        double minDist = 45.0; // Snap threshold in pixels
        
        for (Edge edge : board.getEdges()) {
            Point p1 = nodePositions.get(edge.a);
            Point p2 = nodePositions.get(edge.b);
            
            if (p1 == null || p2 == null)
                continue;
            
            double dist = distanceToSegment(x, y, p1.x, p1.y, p2.x, p2.y);
            
            if (dist < minDist) {
                minDist = dist;
                closestEdge = edge;
            }
        }
        
        return closestEdge;
    }
    
    /**
     * Calculates the distance from a point to a line segment.
     * 
     * @param px Point x
     * @param py Point y
     * @param x1 Segment start x
     * @param y1 Segment start y
     * @param x2 Segment end x
     * @param y2 Segment end y
     * @return Distance in pixels
     */
    private double distanceToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        
        if (dx == 0 && dy == 0) {
            // Degenerate segment (point)
            return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        }
        
        // Parameter t represents position along the segment (0 to 1)
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t)); // Clamp to [0, 1]
        
        // Closest point on segment
        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;
        
        // Distance from point to closest point
        return Math.sqrt((px - closestX) * (px - closestX) + (py - closestY) * (py - closestY));
    }
    
    /**
     * Gets the cell size in pixels.
     */
    public int getCellSize() {
        return cellSize;
    }
    
    /**
     * Gets the margin in pixels.
     */
    public int getMargin() {
        return margin;
    }
}
