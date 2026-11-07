
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the game board as a pure graph structure.
 * No 2D arrays - uses Sets for nodes, edges, and faces.
 * Grid dimensions are stored only for rendering purposes.
 */
public class Board {
    private final int rows, cols;
    private final Set<Node> nodes;
    private final Set<Edge> edges;
    private final Set<Face> faces;
    
    // Helper maps for quick lookup
    private final Map<Integer, Node> nodeById;
    private final Map<Integer, Face> faceById;

    /**
     * Creates a new board with the given dimensions.
     * 
     * @param rows Number of rows in the grid
     * @param cols Number of columns in the grid
     */
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.nodes = new HashSet<>();
        this.edges = new HashSet<>();
        this.faces = new HashSet<>();
        this.nodeById = new HashMap<>();
        this.faceById = new HashMap<>();
        
        buildGraph();
    }

    /**
     * Builds the graph topology: nodes, edges, and faces.
     * Node IDs are assigned in row-major order: id = row * (cols+1) + col
     * Face IDs are assigned in row-major order: id = row * cols + col
     */
    private void buildGraph() {
        // Create nodes
        createNodes();
        
        // Create edges (horizontal and vertical)
        createEdges();
        
        // Create faces (cells with clues)
        createFaces();
    }

    /**
     * Creates all nodes in the graph.
     * Node ID = row * (cols+1) + col
     */
    private void createNodes() {
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                int id = i * (cols + 1) + j;
                Node node = new Node(id);
                nodes.add(node);
                nodeById.put(id, node);
            }
        }
    }

    /**
     * Creates all edges in the graph.
     * Edges connect adjacent nodes horizontally and vertically.
     */
    private void createEdges() {
        // Horizontal edges
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j < cols; j++) {
                int id1 = i * (cols + 1) + j;
                int id2 = i * (cols + 1) + (j + 1);
                
                Node n1 = nodeById.get(id1);
                Node n2 = nodeById.get(id2);
                
                Edge edge = new Edge(n1, n2);
                edges.add(edge);
                
                // Add edge to nodes' adjacency lists
                n1.addEdge(edge);
                n2.addEdge(edge);
            }
        }

        // Vertical edges
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= cols; j++) {
                int id1 = i * (cols + 1) + j;
                int id2 = (i + 1) * (cols + 1) + j;
                
                Node n1 = nodeById.get(id1);
                Node n2 = nodeById.get(id2);
                
                Edge edge = new Edge(n1, n2);
                edges.add(edge);
                
                // Add edge to nodes' adjacency lists
                n1.addEdge(edge);
                n2.addEdge(edge);
            }
        }
    }

    /**
     * Creates all faces (cells) in the graph.
     * Each face is bounded by exactly 4 edges.
     * Face ID = row * cols + col
     */
    private void createFaces() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int faceId = i * cols + j;
                
                // Get the four corner nodes
                int topLeftId = i * (cols + 1) + j;
                int topRightId = i * (cols + 1) + (j + 1);
                int bottomLeftId = (i + 1) * (cols + 1) + j;
                int bottomRightId = (i + 1) * (cols + 1) + (j + 1);
                
                Node topLeft = nodeById.get(topLeftId);
                Node topRight = nodeById.get(topRightId);
                Node bottomLeft = nodeById.get(bottomLeftId);
                Node bottomRight = nodeById.get(bottomRightId);
                
                // Find the four edges
                Edge top = findEdgeBetween(topLeft, topRight);
                Edge bottom = findEdgeBetween(bottomLeft, bottomRight);
                Edge left = findEdgeBetween(topLeft, bottomLeft);
                Edge right = findEdgeBetween(topRight, bottomRight);
                
                // Create face with no clue initially (-1)
                Face face = new Face(faceId, -1, java.util.Arrays.asList(top, bottom, left, right));
                faces.add(face);
                faceById.put(faceId, face);
            }
        }
    }

    /**
     * Helper method to find an edge between two nodes.
     */
    private Edge findEdgeBetween(Node n1, Node n2) {
        for (Edge e : n1.getEdges()) {
            if ((e.a == n1 && e.b == n2) || (e.a == n2 && e.b == n1)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Sets the clue value for a face at the given grid position.
     * 
     * @param row Grid row
     * @param col Grid column
     * @param clue Clue value (0-3), or -1 for no clue
     */
    public void setClue(int row, int col, int clue) {
        int faceId = row * cols + col;
        Face oldFace = faceById.get(faceId);
        
        if (oldFace != null) {
            faces.remove(oldFace);
            Face newFace = new Face(faceId, clue, oldFace.getEdges());
            faces.add(newFace);
            faceById.put(faceId, newFace);
        }
    }

    /**
     * Gets the clue value at a grid position.
     * 
     * @param row Grid row
     * @param col Grid column
     * @return Clue value, or -1 if no clue
     */
    public int getClue(int row, int col) {
        int faceId = row * cols + col;
        Face face = faceById.get(faceId);
        return face != null ? face.getClue() : -1;
    }

    /**
     * Gets all nodes in the graph.
     * 
     * @return Unmodifiable set of nodes
     */
    public Set<Node> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    /**
     * Gets all edges in the graph.
     * 
     * @return Unmodifiable set of edges
     */
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * Gets all faces in the graph.
     * 
     * @return Unmodifiable set of faces
     */
    public Set<Face> getFaces() {
        return Collections.unmodifiableSet(faces);
    }

    /**
     * Gets the number of rows (for rendering only).
     */
    public int getRows() {
        return rows;
    }

    /**
     * Gets the number of columns (for rendering only).
     */
    public int getCols() {
        return cols;
    }

    /**
     * Gets a node by its ID.
     * 
     * @param id Node ID
     * @return The node, or null if not found
     */
    public Node getNodeById(int id) {
        return nodeById.get(id);
    }

    /**
     * Gets a face by its ID.
     * 
     * @param id Face ID
     * @return The face, or null if not found
     */
    public Face getFaceById(int id) {
        return faceById.get(id);
    }
}
