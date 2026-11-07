
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a vertex (node) in the Slitherlink graph.
 * Pure graph representation with no coordinate information.
 */
public class Node {
    private final int id;
    private final List<Edge> edges;

    /**
     * Creates a new node with the given unique ID.
     * 
     * @param id Unique identifier for this node
     */
    public Node(int id) {
        this.id = id;
        this.edges = new ArrayList<>();
    }

    /**
     * Gets the unique ID of this node.
     */
    public int getId() {
        return id;
    }

    /**
     * Adds an edge to this node's adjacency list.
     * 
     * @param e The edge to add
     */
    public void addEdge(Edge e) {
        if (!edges.contains(e)) {
            edges.add(e);
        }
    }

    /**
     * Removes an edge from this node's adjacency list.
     * 
     * @param e The edge to remove
     */
    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    /**
     * Gets the edges incident to this node.
     * 
     * @return Unmodifiable list of edges
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Calculates the active degree of this node.
     * Active degree is the number of ACTIVE edges connected to this node.
     * 
     * @return Active degree (0-4 typically)
     */
    public int getActiveDegree() {
        int degree = 0;
        for (Edge e : edges) {
            if (e.isActive())
                degree++;
        }
        return degree;
    }

    @Override
    public String toString() {
        int activeDegree = getActiveDegree();
        
        if (activeDegree > 2) {
            return String.format(
                    "\n____________________________________________________________\n" +
                            "!!! ERROR: Node[%d] VIOLATION: degree=%d !!!\n" +
                            "____________________________________________________________\n",
                    id, activeDegree);
        }
        return String.format("Node[%d]: activeDegree=%d, totalEdges=%d", id, activeDegree, edges.size());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

