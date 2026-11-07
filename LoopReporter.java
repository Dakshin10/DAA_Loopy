
/**
 * Utility to report loop completion statistics.
 * Uses graph-based edge counting.
 */
public class LoopReporter {

    /**
     * Reports the completion of the loop with statistics.
     * 
     * @param board The game board
     */
    public static void report(Board board) {
        int count = 0;
        
        // Count all active edges
        for (Edge e : board.getEdges()) {
            if (e.isActive()) {
                count++;
            }
        }

        System.out.println("\n============================================================");
        System.out.println("              GAME SOLVED! LOOP COMPLETED                ");
        System.out.println("------------------------------------------------------------");
        System.out.printf("             TOTAL LOOP LENGTH: %d edges\n", count);
        System.out.println("============================================================\n");
    }
}
