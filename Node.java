
import java.util.ArrayList;
import java.util.List;

public class Node {
    public int row, col;
    public int degree;
    public boolean broken = false;
    public List<Edge> edges;

    public Node(int r, int c) {
        this.row = r;
        this.col = c;
        this.degree = 0;
        this.edges = new ArrayList<>();
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    @Override
    public String toString() {
        int activeDegree = 0;
        for (Edge e : edges) {
            if (e.isActive())
                activeDegree++;
        }
        if (activeDegree > 2) {
            return String.format(
                    "\n____________________________________________________________\n" +
                            "!!! ERROR: Node[%d][%d] VIOLATION: degree=%d !!!\n" +
                            "____________________________________________________________\n",
                    row, col, activeDegree);
        }
        return String.format("Node[%d][%d]: degree=%d, edges=%d", row, col, activeDegree, edges.size());
    }
}
