
import java.util.*;
import java.util.Scanner;

class DisjointSet {
    public Map<Node, Node> parent = new HashMap<>();
    public Map<Node, Integer> rank = new HashMap<>();

    public Node findParent(Node x) {
        if (parent.get(x) != x) {
            parent.put(x, findParent(parent.get(x)));
        }
        return parent.get(x);
    }

    public void start(Node x) {
        parent.put(x, x);
        rank.put(x, 0);
    }

    public void unite(Node x, Node y) {
        Node px = findParent(x);
        Node py = findParent(y);
        if (px == py)
            return;

        int rx = rank.getOrDefault(px, 0);
        int ry = rank.getOrDefault(py, 0);

        if (rx < ry) {
            parent.put(px, py);
        } else if (rx > ry) {
            parent.put(py, px);
        } else {
            parent.put(py, px);
            rank.put(px, rx + 1);
        }
    }
}

public class Validator {
    public boolean validatePartial(Board board) {
        return validateNodeDegree(board) && validateCells(board) && noSmallLoops(board);
    }

    public boolean validateFinal(Board board) {
        if (!validateNodeDegree(board))
            return false;
        if (!validateCellExact(board))
            return false;
        return singleLoop(board);
    }

    private boolean validateNodeDegree(Board board) {
        for (Node[] row : board.nodes) {
            for (Node node : row) {
                int degree = 0;
                for (Edge e : node.edges) {
                    if (e.isActive())
                        degree++;
                }
                if (degree > 2)
                    return false;
            }
        }
        return true;
    }

    private boolean validateCells(Board board) {
        for (Cell[] row : board.cells) {
            for (Cell cell : row) {
                if (cell.clue < 0)
                    continue;

                int active = 0, unknown = 0;
                Edge[] edges = { cell.top, cell.bottom, cell.left, cell.right };

                for (Edge e : edges) {
                    if (e != null) {
                        if (e.isActive())
                            active++;
                        else if (e.isUnknown())
                            unknown++;
                    }
                }

                if (active > cell.clue)
                    return false;
                if (active + unknown < cell.clue)
                    return false;
            }
        }
        return true;
    }

    private boolean validateCellExact(Board board) {
        for (Cell[] row : board.cells) {
            for (Cell cell : row) {
                if (cell.clue < 0)
                    continue;
                if (cell.lineCount() != cell.clue)
                    return false;
            }
        }
        return true;
    }

    private boolean singleLoop(Board board) {
        Set<Node> loopNodes = new HashSet<>();
        DisjointSet ds = new DisjointSet();
        int activeEdgeCount = 0;

        List<Edge> allEdges = board.getAllEdges();
        for (Edge e : allEdges) {
            if (e.isActive())
                activeEdgeCount++;
        }
        System.out.println("activeEdgeCount: " + activeEdgeCount);

        for (Node[] row : board.nodes) {
            for (Node node : row) {
                int deg = 0;
                for (Edge e : node.edges) {
                    if (e.isActive())
                        deg++;
                }

                if (deg != 0 && deg != 2)
                    return false;

                if (deg == 2) {
                    ds.start(node);
                    loopNodes.add(node);
                }
            }
        }

        if (loopNodes.isEmpty())
            return false;

        for (Edge e : allEdges) {
            if (e.isActive()) {
                ds.unite(e.a, e.b);
            }
        }

        Node first = loopNodes.iterator().next();
        Node root = ds.findParent(first);
        for (Node node : loopNodes) {
            if (ds.findParent(node) != root)
                return false;
        }

        return activeEdgeCount == loopNodes.size();
    }

    private boolean noSmallLoops(Board board) {
        List<Edge> activeEdges = new ArrayList<>();
        for (Edge e : board.getAllEdges()) {
            if (e.isActive())
                activeEdges.add(e);
        }
        if (activeEdges.isEmpty())
            return true;

        DisjointSet ds = new DisjointSet();
        for (Edge e : activeEdges) {
            if (!ds.parent.containsKey(e.a))
                ds.start(e.a);
            if (!ds.parent.containsKey(e.b))
                ds.start(e.b);
            ds.unite(e.a, e.b);
        }

        Map<Node, List<Node>> components = new HashMap<>();
        for (Node n : ds.parent.keySet()) {
            Node root = ds.findParent(n);
            components.computeIfAbsent(root, k -> new ArrayList<>()).add(n);
        }

        if (components.size() <= 1)
            return true;

        for (Map.Entry<Node, List<Node>> entry : components.entrySet()) {
            List<Node> nodes = entry.getValue();
            boolean allDegree2 = true;
            for (Node n : nodes) {
                int degree = 0;
                for (Edge e : n.edges) {
                    if (e.isActive())
                        degree++;
                }
                if (degree != 2) {
                    allDegree2 = false;
                    break;
                }
            }

            if (allDegree2) {
                if (components.size() > 1)
                    return false;
            }
        }

        return true;
    }
}
