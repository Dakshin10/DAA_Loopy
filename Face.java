
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a face (clue constraint) in the Slitherlink graph.
 * A face is a planar region bounded by exactly 4 edges.
 * This is a pure constraint model with no coordinate information.
 */
public class Face {
    private final int id;
    private final int clue; // -1 if no clue
    private final List<Edge> edges; // Exactly 4 edges forming the face boundary

    /**
     * Creates a new face with the given ID, clue value, and bounding edges.
     * 
     * @param id Unique identifier for this face
     * @param clue The clue value (0-3), or -1 if no clue
     * @param edges List of exactly 4 edges forming the face boundary
     */
    public Face(int id, int clue, List<Edge> edges) {
        if (edges.size() != 4) {
            throw new IllegalArgumentException("Face must have exactly 4 edges, got " + edges.size());
        }
        this.id = id;
        this.clue = clue;
        this.edges = new ArrayList<>(edges);
    }

    /**
     * Gets the unique ID of this face.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the clue value for this face.
     * 
     * @return The clue (0-3), or -1 if no clue
     */
    public int getClue() {
        return clue;
    }

    /**
     * Gets the edges forming the boundary of this face.
     * 
     * @return Unmodifiable list of 4 edges
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Counts the number of ACTIVE edges around this face.
     * 
     * @return Number of active edges (0-4)
     */
    public int activeCount() {
        int count = 0;
        for (Edge e : edges) {
            if (e != null && e.isActive())
                count++;
        }
        return count;
    }

    /**
     * Counts the number of UNKNOWN edges around this face.
     * 
     * @return Number of unknown edges (0-4)
     */
    public int unknownCount() {
        int count = 0;
        for (Edge e : edges) {
            if (e != null && e.isUnknown())
                count++;
        }
        return count;
    }

    /**
     * Checks if this face's clue constraint is satisfied.
     * A face is satisfied if it has no clue, or if the number of active edges equals the clue.
     * 
     * @return true if satisfied, false otherwise
     */
    public boolean isSatisfied() {
        if (clue < 0)
            return true; // No clue = always satisfied
        return activeCount() == clue;
    }

    /**
     * Checks if this face's clue constraint is violated.
     * A face is violated if the number of active edges exceeds the clue,
     * or if it's impossible to satisfy the clue with remaining unknown edges.
     * 
     * @return true if violated, false otherwise
     */
    public boolean isViolated() {
        if (clue < 0)
            return false; // No clue = never violated
        
        int active = activeCount();
        int unknown = unknownCount();
        
        // Too many active edges
        if (active > clue)
            return true;
        
        // Impossible to reach clue value
        if (active + unknown < clue)
            return true;
        
        return false;
    }

    @Override
    public String toString() {
        if (clue < 0) {
            return String.format("Face[%d]: no clue, active=%d", id, activeCount());
        }
        return String.format("Face[%d]: clue=%d, active=%d, unknown=%d", 
                             id, clue, activeCount(), unknownCount());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Face)) return false;
        Face other = (Face) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
