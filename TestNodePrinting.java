public class TestNodePrinting {
    public static void main(String[] args) {
        Board board = new Board(2, 2);
        // Activate some edges to change degrees using the correct method name
        board.hedge[0][0].setState(EdgeStatus.ACTIVE);
        board.vedge[0][0].setState(EdgeStatus.ACTIVE);
        board.hedge[0][1].setState(EdgeStatus.ACTIVE); // Third edge for Node[0][1] and Node[0][2]
        // Node[0][1] now has connections from hedge[0][0], hedge[0][1], vedge[0][1]
        // (initially)
        // Wait, vedge[0][1] is NOT active yet.
        // Let's connect Node[0][1] to something else.
        board.vedge[0][1].setState(EdgeStatus.ACTIVE);
        // Node[0][1] is connected to:
        // 1. nodes[0][0] via hedge[0][0]
        // 2. nodes[0][2] via hedge[0][1]
        // 3. nodes[1][1] via vedge[0][1]
        // Total active degree for Node[0][1] = 3.

        board.printNodes();
    }
}
