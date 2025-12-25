
import java.util.ArrayList;
import java.util.List;

public class Board {
    public int rows, cols;
    public Edge[][] hedge;
    public Edge[][] vedge;
    public Node[][] nodes;
    public Cell[][] cells;

    public Board(int r, int c) {
        this.rows = r;
        this.cols = c;
        createNodes();
        createEdges();
        createCells();
    }

    private void createNodes() {
        nodes = new Node[rows + 1][cols + 1];
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                nodes[i][j] = new Node(i, j);
            }
        }
    }

    private void createEdges() {
        hedge = new Edge[rows + 1][cols];
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j < cols; j++) {
                Edge e = new Edge(nodes[i][j], nodes[i][j + 1]);
                hedge[i][j] = e;
                nodes[i][j].addEdge(e);
                nodes[i][j + 1].addEdge(e);
            }
        }

        vedge = new Edge[rows][cols + 1];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= cols; j++) {
                Edge e = new Edge(nodes[i][j], nodes[i + 1][j]);
                vedge[i][j] = e;
                nodes[i][j].addEdge(e);
                nodes[i + 1][j].addEdge(e);
            }
        }
    }

    private void createCells() {
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = new Cell(i, j, -1);
                cell.top = hedge[i][j];
                cell.bottom = hedge[i + 1][j];
                cell.left = vedge[i][j];
                cell.right = vedge[i][j + 1];
                cells[i][j] = cell;
            }
        }
    }

    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>((rows + 1) * cols + rows * (cols + 1));
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (hedge[i][j] != null)
                    edges.add(hedge[i][j]);
            }
        }
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= cols; j++) {
                if (vedge[i][j] != null)
                    edges.add(vedge[i][j]);
            }
        }
        return edges;
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print("+");
                Edge e = hedge[i][j];
                if (e != null && e.isActive())
                    System.out.print("---");
                else
                    System.out.print("   ");
            }
            System.out.println("+");

            for (int j = 0; j < cols; j++) {
                Edge e = vedge[i][j];
                if (e != null && e.isActive())
                    System.out.print("|");
                else
                    System.out.print(" ");

                if (cells[i][j].clue == -1)
                    System.out.print("   ");
                else
                    System.out.printf(" %d ", cells[i][j].clue);
            }
            Edge rightEdge = vedge[i][cols];
            if (rightEdge != null && rightEdge.isActive())
                System.out.println("|");
            else
                System.out.println(" ");
        }

        for (int j = 0; j < cols; j++) {
            System.out.print("+");
            Edge e = hedge[rows][j];
            if (e != null && e.isActive())
                System.out.print("---");
            else
                System.out.print("   ");
        }
        System.out.println("+");
    }

    public void printAllEdges() {
        System.out.println("Horizontal edges:");
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(hedge[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("Vertical edges:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j <= cols; j++) {
                System.out.print(vedge[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void printNodes() {
        System.out.println("Nodes and Attributes:");
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                System.out.println(nodes[i][j]);
            }
        }
    }

    public void printErrors() {
        boolean foundError = false;
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                Node node = nodes[i][j];
                int activeDegree = 0;
                for (Edge e : node.edges) {
                    if (e.isActive())
                        activeDegree++;
                }
                if (activeDegree > 2) {
                    if (!foundError) {
                        System.out.println("\n--- TERMINAL RULE VIOLATIONS DETECTED ---");
                        foundError = true;
                    }
                    System.out.print(node);
                }
            }
        }
    }
}
