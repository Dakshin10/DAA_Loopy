enum PlayerType {
    HUMAN, CPU, NONE
}

public class Edge {
    public Node a;
    public Node b;
    private EdgeStatus state;
    private PlayerType owner = PlayerType.NONE;

    public Edge(Node n1, Node n2) {
        this.a = n1;
        this.b = n2;
        this.state = EdgeStatus.UNKNOWN;
    }

    public void setState(EdgeStatus newState, PlayerType player) {
        if (newState == state)
            return;

        // Logic for Graph Topology:
        // If we are moving TO Inactive, we break the graph connection.
        // If we are moving FROM Inactive (to Unknown/Active), we restore it.
        if (newState == EdgeStatus.INACTIVE) {
            removeFromNodes();
        } else if (state == EdgeStatus.INACTIVE) {
            addToNodes();
        }

        EdgeStatus oldState = state;
        state = newState;
        
        if (state == EdgeStatus.ACTIVE) {
            this.owner = player;
        } else {
            this.owner = PlayerType.NONE;
        }
    }

    // Rewrite of the specific methods to encompass both Degree and Topology
    public void setActive(PlayerType player) {
        if (state == EdgeStatus.ACTIVE)
            return;

        if (state == EdgeStatus.INACTIVE)
            addToNodes(); // Restore topology

        state = EdgeStatus.ACTIVE;
        this.owner = player;
    }

    public void setInactive() {
        if (state == EdgeStatus.INACTIVE)
            return;

        state = EdgeStatus.INACTIVE;
        this.owner = PlayerType.NONE;
        removeFromNodes(); // Break topology
    }

    public void setUnknown() {
        if (state == EdgeStatus.UNKNOWN)
            return;

        if (state == EdgeStatus.INACTIVE)
            addToNodes(); // Restore topology

        state = EdgeStatus.UNKNOWN;
        this.owner = PlayerType.NONE;
    }

    private void addToNodes() {
        a.addEdge(this);
        b.addEdge(this);
    }

    private void removeFromNodes() {
        a.removeEdge(this);
        b.removeEdge(this);
    }

    // Overload for compatibility
    public void setState(EdgeStatus newState) {
        setState(newState, PlayerType.NONE);
    }

    public boolean isActive() {
        return state == EdgeStatus.ACTIVE;
    }

    public boolean isInactive() {
        return state == EdgeStatus.INACTIVE;
    }

    public boolean isUnknown() {
        return state == EdgeStatus.UNKNOWN;
    }

    public EdgeStatus getStatus() {
        return state;
    }

    public PlayerType getOwner() {
        return owner;
    }

    public static void applyState(Edge e, EdgeStatus st, PlayerType p) {
        e.setState(st, p);
    }

    @Override
    public String toString() {
        return "Edge(" + a.getId() + "-" + b.getId() + "): " + state;
    }
}

