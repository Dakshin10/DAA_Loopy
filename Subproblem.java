import java.util.*;

/**
 * Represents an independent subproblem in the Divide & Conquer solver.
 * A subproblem is a connected region of UNKNOWN edges that can be solved
 * independently without affecting other regions.
 */
public class Subproblem {
    private final int id;
    private final Set<Edge> unknownEdges;
    private final Set<Node> involvedNodes;
    private final Set<Face> involvedFaces;
    
    /**
     * Creates a new subproblem.
     * 
     * @param id Unique identifier for this subproblem
     * @param unknownEdges Set of UNKNOWN edges in this region
     * @param involvedNodes Set of nodes connected to these edges
     * @param involvedFaces Set of faces that constrain these edges
     */
    public Subproblem(int id, Set<Edge> unknownEdges, Set<Node> involvedNodes, Set<Face> involvedFaces) {
        this.id = id;
        this.unknownEdges = new HashSet<>(unknownEdges);
        this.involvedNodes = new HashSet<>(involvedNodes);
        this.involvedFaces = new HashSet<>(involvedFaces);
    }
    
    /**
     * Gets the unique identifier for this subproblem.
     */
    public int getId() {
        return id;
    }
    
    /**
     * Gets all UNKNOWN edges in this subproblem.
     */
    public Set<Edge> getUnknownEdges() {
        return Collections.unmodifiableSet(unknownEdges);
    }
    
    /**
     * Gets all nodes involved in this subproblem.
     */
    public Set<Node> getInvolvedNodes() {
        return Collections.unmodifiableSet(involvedNodes);
    }
    
    /**
     * Gets all faces involved in this subproblem.
     */
    public Set<Face> getInvolvedFaces() {
        return Collections.unmodifiableSet(involvedFaces);
    }
    
    /**
     * Returns the number of UNKNOWN edges in this subproblem.
     */
    public int size() {
        return unknownEdges.size();
    }
    
    /**
     * Checks if this subproblem has no UNKNOWN edges.
     */
    public boolean isEmpty() {
        return unknownEdges.isEmpty();
    }
    
    /**
     * Checks if this subproblem is independent from another subproblem.
     * Two subproblems are independent if they share no edges, nodes, or faces.
     * 
     * @param other The other subproblem to check
     * @return true if the subproblems are independent
     */
    public boolean isIndependent(Subproblem other) {
        // Check for shared edges
        for (Edge e : unknownEdges) {
            if (other.unknownEdges.contains(e)) {
                return false;
            }
        }
        
        // Check for shared nodes
        for (Node n : involvedNodes) {
            if (other.involvedNodes.contains(n)) {
                return false;
            }
        }
        
        // Check for shared faces
        for (Face f : involvedFaces) {
            if (other.involvedFaces.contains(f)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("Subproblem[id=%d, edges=%d, nodes=%d, faces=%d]",
                id, unknownEdges.size(), involvedNodes.size(), involvedFaces.size());
    }
}
