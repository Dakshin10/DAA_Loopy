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

    public void setActive(PlayerType player) {
        if (state == EdgeStatus.ACTIVE)
            return;
        state = EdgeStatus.ACTIVE;
        this.owner = player;
        a.degree++;
        b.degree++;
    }

    public void setInactive() {
        if (state == EdgeStatus.INACTIVE)
            return;
        if (state == EdgeStatus.ACTIVE) {
            a.degree--;
            b.degree--;
        }
        state = EdgeStatus.INACTIVE;
        this.owner = PlayerType.NONE;
    }

    public void setUnknown() {
        if (state == EdgeStatus.UNKNOWN)
            return;
        if (state == EdgeStatus.ACTIVE) {
            a.degree--;
            b.degree--;
        }
        state = EdgeStatus.UNKNOWN;
        this.owner = PlayerType.NONE;
    }

    public void setState(EdgeStatus newState, PlayerType player) {
        if (newState == state)
            return;
        if (newState == EdgeStatus.ACTIVE)
            setActive(player);
        else if (newState == EdgeStatus.INACTIVE)
            setInactive();
        else
            setUnknown();
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
        return "(" + a.row + "," + a.col + ")-(" + b.row + "," + b.col + "): " + state;
    }
}
