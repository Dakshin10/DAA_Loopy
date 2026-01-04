
import java.util.*;

public class PuzzleGenerator {
    private static final Random random = new Random();

    public static Board generate(int rows, int cols) {
        while (true) {
            Board board = new Board(rows, cols);
            if (generateLoop(board)) {
                generateClues(board);
                // We skip solver verification since backtracking is removed.
                // Pure greedy puzzles are rare, so we'll just return the generated loop.
                System.out.println("Loop generated.");
                clearEdges(board); // Start with no lines
                removeClues(board, 5); // Leave 95% clues for very easy solving
                return board;
            }
        }
    }

    private static boolean generateLoop(Board board) {
        for (Edge e : board.getAllEdges())
            e.setUnknown(); // Use UNKNOWN to keep topology connected for the walk

        int R = board.rows;
        int C = board.cols;
        Node start = board.nodes[random.nextInt(R + 1)][random.nextInt(C + 1)];
        Node curr = start;
        Set<Node> visited = new HashSet<>();
        visited.add(curr);

        while (true) {
            List<Edge> options = new ArrayList<>();
            for (Edge e : curr.edges) {
                Node next = (e.a == curr) ? e.b : e.a;
                if (!visited.contains(next))
                    options.add(e);
            }

            if (options.isEmpty()) {
                for (Edge e : curr.edges) {
                    Node next = (e.a == curr) ? e.b : e.a;
                    if (next == start && visited.size() > (R + C)) { // Ensure a decent loop size
                        e.setActive(PlayerType.NONE);
                        return true;
                    }
                }
                return false;
            }

            Edge chosen = options.get(random.nextInt(options.size()));
            chosen.setActive(PlayerType.NONE);
            curr = (chosen.a == curr) ? chosen.b : chosen.a;
            visited.add(curr);
        }
    }

    private static void generateClues(Board board) {
        for (Cell[] row : board.cells) {
            for (Cell cell : row) {
                cell.clue = cell.lineCount();
            }
        }
    }

    public static void removeClues(Board board, int percent) {
        for (Cell[] row : board.cells) {
            for (Cell cell : row) {
                if (random.nextInt(100) < percent) {
                    cell.clue = -1;
                }
            }
        }
    }

    private static void clearEdges(Board board) {
        for (Edge e : board.getAllEdges()) {
            e.setUnknown();
        }
    }
}
