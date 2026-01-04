
public class TestTopologyPrint {
    public static void main(String[] args) {
        System.out.println("TEST: Visualizing Broken Topology");

        // 1. Create a 2x2 board
        Board board = new Board(2, 2);

        // 2. Initial State
        System.out.println("\n1. Initial State (All Unknown)");
        board.print();
        board.printTopology();

        // 3. Mark some edges as ACTIVE (Line) and INACTIVE (X)
        // Active top-left horizontal
        board.hedge[0][0].setActive(PlayerType.CPU);

        // Inactive top-left vertical
        board.vedge[0][0].setInactive();

        // Active bottom-right vertical
        board.vedge[1][2].setActive(PlayerType.CPU);

        System.out.println("\n2. Modified State");
        System.out.println("   - Top-Left Horiz: ACTIVE (---)");
        System.out.println("   - Top-Left Vert: INACTIVE ( X ) -> Should be missing from topology");

        board.print();
        board.printTopology();
    }
}
