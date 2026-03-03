import java.util.*;

public class HybridDPSolver {
    private Board board;
    private int R, C;
    private boolean[][] visited;
    private int[] hMasks;
    private Validator validator = new Validator();

    public boolean solve(Board board) {
        this.board = board;
        this.R = board.rows;
        this.C = board.cols;

        if (C > 10) {
            System.err.println("HybridDPSolver only supports width up to 10.");
            return false;
        }

        this.visited = new boolean[R + 1][1 << (C + 1)];
        this.hMasks = new int[R + 1];

        resetBoard();

        return dfs(0, 0);
    }

    private boolean dfs(int row, int vMask) {
        if (visited[row][vMask])
            return false;

        if (row == R) {
            for (int hr = 0; hr < (1 << C); hr++) {
                if (!matchHedge(R, hr))
                    continue;
                if (!checkLastNodeRow(vMask, hr))
                    continue;
                if (!checkCells(R - 1, hMasks[R - 1], hr, vMask))
                    continue;

                applyHedges(R, hr);
                if (validator.validateFinal(board))
                    return true;
                undoHedges(R);
            }
            visited[row][vMask] = true;
            return false;
        }

        for (int hr = 0; hr < (1 << C); hr++) {
            if (!matchHedge(row, hr))
                continue;

            int nextVMask = computeNextVMask(vMask, hr);
            if (nextVMask == -1)
                continue;
            if (!matchVedge(row, nextVMask))
                continue;

            if (row > 0 && !checkCells(row - 1, hMasks[row - 1], hr, vMask))
                continue;

            hMasks[row] = hr;
            applyRow(row, hr, nextVMask);

            if (dfs(row + 1, nextVMask))
                return true;

            undoRow(row);
        }

        visited[row][vMask] = true;
        return false;
    }

    private boolean matchHedge(int r, int mask) {
        for (int c = 0; c < C; c++) {
            EdgeStatus s = board.hedge[r][c].getStatus();
            boolean active = ((mask >> c) & 1) == 1;
            if (s == EdgeStatus.ACTIVE && !active)
                return false;
            if (s == EdgeStatus.INACTIVE && active)
                return false;
        }
        return true;
    }

    private boolean matchVedge(int r, int mask) {
        for (int c = 0; c <= C; c++) {
            EdgeStatus s = board.vedge[r][c].getStatus();
            boolean active = ((mask >> c) & 1) == 1;
            if (s == EdgeStatus.ACTIVE && !active)
                return false;
            if (s == EdgeStatus.INACTIVE && active)
                return false;
        }
        return true;
    }

    private int computeNextVMask(int vMask, int hMask) {
        int nextMask = 0;
        for (int c = 0; c <= C; c++) {
            int v_in = (vMask >> c) & 1;
            int h_left = (c > 0) ? ((hMask >> (c - 1)) & 1) : 0;
            int h_right = (c < C) ? ((hMask >> c) & 1) : 0;

            int sum = v_in + h_left + h_right;
            if (sum == 0) {
            } else if (sum == 1)
                nextMask |= (1 << c);
            else if (sum == 2) {
            } else
                return -1;
        }
        return nextMask;
    }

    private boolean checkLastNodeRow(int vMask, int hMask) {
        for (int c = 0; c <= C; c++) {
            int v_in = (vMask >> c) & 1;
            int h_left = (c > 0) ? ((hMask >> (c - 1)) & 1) : 0;
            int h_right = (c < C) ? ((hMask >> c) & 1) : 0;
            int sum = v_in + h_left + h_right;
            if (sum != 0 && sum != 2)
                return false;
        }
        return true;
    }

    private boolean checkCells(int r, int hTop, int hBot, int vMask) {
        for (int c = 0; c < C; c++) {
            int clue = board.cells[r][c].clue;
            if (clue == -1)
                continue;
            int count = 0;
            if (((hTop >> c) & 1) == 1)
                count++;
            if (((hBot >> c) & 1) == 1)
                count++;
            if (((vMask >> c) & 1) == 1)
                count++;
            if (((vMask >> (c + 1)) & 1) == 1)
                count++;
            if (count != clue)
                return false;
        }
        return true;
    }

    private void applyRow(int r, int hMask, int vMask) {
        for (int c = 0; c < C; c++) {
            Edge e = board.hedge[r][c];
            if (e.getOwner() == PlayerType.HUMAN)
                continue;
            if (((hMask >> c) & 1) == 1)
                e.setActive(PlayerType.CPU);
            else
                e.setInactive();
        }
        for (int c = 0; c <= C; c++) {
            Edge e = board.vedge[r][c];
            if (e.getOwner() == PlayerType.HUMAN)
                continue;
            if (((vMask >> c) & 1) == 1)
                e.setActive(PlayerType.CPU);
            else
                e.setInactive();
        }
    }

    private void undoRow(int r) {
        for (int c = 0; c < C; c++) {
            Edge e = board.hedge[r][c];
            if (e.getOwner() == PlayerType.CPU)
                e.setUnknown();
        }
        for (int c = 0; c <= C; c++) {
            Edge e = board.vedge[r][c];
            if (e.getOwner() == PlayerType.CPU)
                e.setUnknown();
        }
    }

    private void applyHedges(int r, int hMask) {
        for (int c = 0; c < C; c++) {
            Edge e = board.hedge[r][c];
            if (e.getOwner() == PlayerType.HUMAN)
                continue;
            if (((hMask >> c) & 1) == 1)
                e.setActive(PlayerType.CPU);
            else
                e.setInactive();
        }
    }

    private void undoHedges(int r) {
        for (int c = 0; c < C; c++) {
            Edge e = board.hedge[r][c];
            if (e.getOwner() == PlayerType.CPU)
                e.setUnknown();
        }
    }

    private void resetBoard() {
        for (Edge e : board.getAllEdges()) {
            if (e.getOwner() == PlayerType.CPU)
                e.setUnknown();
        }
    }
}
