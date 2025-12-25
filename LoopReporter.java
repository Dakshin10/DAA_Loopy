
public class LoopReporter {

    public static void report(Board board) {
        int count = 0;
        // Count horizontal active edges
        for (int i = 0; i <= board.rows; i++) {
            for (int j = 0; j < board.cols; j++) {
                if (board.hedge[i][j].isActive()) {
                    count++;
                }
            }
        }
        // Count vertical active edges
        for (int i = 0; i < board.rows; i++) {
            for (int j = 0; j <= board.cols; j++) {
                if (board.vedge[i][j].isActive()) {
                    count++;
                }
            }
        }

        System.out.println("\n============================================================");
        System.out.println("              GAME SOLVED! LOOP COMPLETED                ");
        System.out.println("------------------------------------------------------------");
        System.out.printf("             TOTAL LOOP LENGTH: %d edges\n", count);
        System.out.println("============================================================\n");
    }
}
