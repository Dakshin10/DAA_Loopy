
import java.util.*;

/**
 * Generates random solvable Slitherlink puzzles using graph-based random walk.
 */
public class PuzzleGenerator {
    private static final Random random = new Random();

    /**
     * Generates a new puzzle with the given dimensions.
     * 
     * @param rows Number of rows
     * @param cols Number of columns
     * @return A new board with a generated puzzle
     */
    public static Board generate(int rows, int cols) {
        while (true) {
            Board board = new Board(rows, cols);
            if (generateLoop(board)) {
                generateClues(board);
                System.out.println("Loop generated.");
                clearEdges(board); // Start with no lines
                removeClues(board, 5); // Leave 95% clues for very easy solving
                return board;
            }
        }
    }

    /**
     * Generates a random loop on the board using graph traversal.
     * 
     * @param board The board
     * @return true if successful, false otherwise
     */
    private static boolean generateLoop(Board board) {
        // Reset all edges to UNKNOWN
        for (Edge e : board.getEdges())
            e.setUnknown();

        int R = board.getRows();
        int C = board.getCols();
        
        // Pick a random starting node
        List<Node> nodeList = new ArrayList<>(board.getNodes());
        Node start = nodeList.get(random.nextInt(nodeList.size()));
        Node curr = start;
        
        Set<Node> visited = new HashSet<>();
        visited.add(curr);

        // Random walk to create a loop
        while (true) {
            List<Edge> options = new ArrayList<>();
            
            // Find unvisited neighbors
            for (Edge e : curr.getEdges()) {
                Node next = (e.a == curr) ? e.b : e.a;
                if (!visited.contains(next))
                    options.add(e);
            }

            if (options.isEmpty()) {
                // Try to close the loop
                for (Edge e : curr.getEdges()) {
                    Node next = (e.a == curr) ? e.b : e.a;
                    if (next == start && visited.size() > (R + C)) { // Ensure a decent loop size
                        e.setActive(PlayerType.NONE);
                        return true;
                    }
                }
                return false;
            }

            // Choose a random edge and activate it
            Edge chosen = options.get(random.nextInt(options.size()));
            chosen.setActive(PlayerType.NONE);
            curr = (chosen.a == curr) ? chosen.b : chosen.a;
            visited.add(curr);
        }
    }

    /**
     * Generates clues for all faces based on the active loop.
     * 
     * @param board The board
     */
    private static void generateClues(Board board) {
        // Collect clue data first to avoid ConcurrentModificationException
        List<int[]> clueData = new ArrayList<>();
        
        for (Face face : board.getFaces()) {
            int clue = face.activeCount();
            int faceId = face.getId();
            int row = faceId / board.getCols();
            int col = faceId % board.getCols();
            clueData.add(new int[]{row, col, clue});
        }
        
        // Now apply the clues
        for (int[] data : clueData) {
            board.setClue(data[0], data[1], data[2]);
        }
    }

    /**
     * Randomly removes a percentage of clues to increase difficulty.
     * 
     * @param board The board
     * @param percent Percentage of clues to remove (0-100)
     */
    public static void removeClues(Board board, int percent) {
        // Collect faces to remove clues from
        List<int[]> toRemove = new ArrayList<>();
        
        for (Face face : board.getFaces()) {
            if (random.nextInt(100) < percent) {
                int faceId = face.getId();
                int row = faceId / board.getCols();
                int col = faceId % board.getCols();
                toRemove.add(new int[]{row, col});
            }
        }
        
        // Now remove the clues
        for (int[] data : toRemove) {
            board.setClue(data[0], data[1], -1);
        }
    }

    /**
     * Resets all edges to UNKNOWN state.
     * 
     * @param board The board
     */
    private static void clearEdges(Board board) {
        for (Edge e : board.getEdges()) {
            e.setUnknown();
        }
    }
}
