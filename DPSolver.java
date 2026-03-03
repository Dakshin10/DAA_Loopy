import java.util.*;

/**
 * DPSolver implements a Profile Dynamic Programming (Broken Line) approach.
 * 
 * State is encoded into a 64-bit long:
 * - Bits 0-35: Connectivity (12 slots, 3 bits each) - Bracket representation
 * (0: none, 1: (, 2: ))
 * - Bits 36-55: Cell partial sums (10 slots, 2 bits each) - current edge count
 * for cells.
 * - Bit 56: Loop closed flag (1 bit)
 */
public class DPSolver {
    private Board board;
    private int R, C;
    private Validator validator;

    private static class ParentInfo {
        long prevState;
        boolean v_out, h_out;

        ParentInfo(long ps, boolean v, boolean h) {
            this.prevState = ps;
            this.v_out = v;
            this.h_out = h;
        }
    }

    public boolean solve(Board board) {
        this.board = board;
        this.R = board.rows;
        this.C = board.cols;
        this.validator = new Validator();

        if (C > 10) {
            System.err.println("DP Solver only supports width up to 10.");
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<Long, ParentInfo>[] dp = new HashMap[(R + 1) * (C + 1) + 1];
        for (int i = 0; i < dp.length; i++)
            dp[i] = new HashMap<>();

        dp[0].put(0L, null);

        for (int r = 0; r <= R; r++) {
            for (int c = 0; c <= C; c++) {
                int nodeIdx = r * (C + 1) + c;
                Map<Long, ParentInfo> current = dp[nodeIdx];
                if (current.isEmpty())
                    continue;

                Map<Long, ParentInfo> next = dp[nodeIdx + 1];

                for (Map.Entry<Long, ParentInfo> entry : current.entrySet()) {
                    long state = entry.getKey();

                    int s_c = (int) ((state >> (c * 3)) & 7); // Left (h_in)
                    int s_c1 = (int) ((state >> ((c + 1) * 3)) & 7); // Top (v_in)
                    boolean loopClosed = ((state >> 56) & 1) != 0;

                    for (int v_out_bit = 0; v_out_bit <= 1; v_out_bit++) {
                        for (int h_out_bit = 0; h_out_bit <= 1; h_out_bit++) {
                            boolean v_out = (v_out_bit == 1 && r < R);
                            boolean h_out = (h_out_bit == 1 && c < C);

                            // Respect existing board state
                            if (r < R) {
                                Edge ve = board.vedge[r][c];
                                if (ve.getStatus() == EdgeStatus.ACTIVE && !v_out)
                                    continue;
                                if (ve.getStatus() == EdgeStatus.INACTIVE && v_out)
                                    continue;
                            }
                            if (c < C) {
                                Edge he = board.hedge[r][c];
                                if (he.getStatus() == EdgeStatus.ACTIVE && !h_out)
                                    continue;
                                if (he.getStatus() == EdgeStatus.INACTIVE && h_out)
                                    continue;
                            }

                            int deg = (s_c != 0 ? 1 : 0) + (s_c1 != 0 ? 1 : 0) + (v_out ? 1 : 0) + (h_out ? 1 : 0);
                            if (deg != 0 && deg != 2)
                                continue;
                            if (loopClosed && deg > 0)
                                continue;

                            // Cell verification check
                            if (r > 0 && c < C) {
                                int clue = board.cells[r - 1][c].clue;
                                int pSum = (int) ((state >> (36 + c * 2)) & 3);
                                if (clue != -1 && (pSum + (h_out ? 1 : 0)) != clue)
                                    continue;
                            }

                            // Connectivity update
                            long nextConn = state & 0xFFFFFFFFFL;
                            nextConn &= ~(7L << (c * 3));
                            nextConn &= ~(7L << ((c + 1) * 3));
                            boolean nextLoopClosed = loopClosed;

                            if (s_c == 0 && s_c1 == 0) {
                                if (v_out && h_out) {
                                    nextConn = setBracket(nextConn, c, 1);
                                    nextConn = setBracket(nextConn, c + 1, 2);
                                }
                            } else if (s_c != 0 && s_c1 == 0) {
                                if (v_out && !h_out)
                                    nextConn = setBracket(nextConn, c, s_c);
                                else if (!v_out && h_out)
                                    nextConn = setBracket(nextConn, c + 1, s_c);
                            } else if (s_c == 0 && s_c1 != 0) {
                                if (v_out && !h_out)
                                    nextConn = setBracket(nextConn, c, s_c1);
                                else if (!v_out && h_out)
                                    nextConn = setBracket(nextConn, c + 1, s_c1);
                            } else { // both active
                                if (!v_out && !h_out) {
                                    if (s_c == 1 && s_c1 == 1) {
                                        int p = findPartner(state, c + 1, 2);
                                        nextConn = setBracket(nextConn, p, 1);
                                    } else if (s_c == 2 && s_c1 == 2) {
                                        int p = findPartner(state, c, 1);
                                        nextConn = setBracket(nextConn, p, 2);
                                    } else if (s_c == 1 && s_c1 == 2) {
                                        if (isCompatibleClosure(nextConn))
                                            nextLoopClosed = true;
                                        else
                                            continue;
                                    } else { // 2, 1
                                        // simply merge
                                    }
                                } else
                                    continue;
                            }

                            long nextState = nextConn;
                            if (nextLoopClosed)
                                nextState |= (1L << 56);

                            // Cell sums update
                            if (c < C) {
                                long sVal = (h_out ? 1 : 0) + (v_out ? 1 : 0);
                                nextState |= (sVal << (36 + c * 2));
                            }
                            if (c > 0) {
                                long oldSumPrev = (state >> (36 + (c - 1) * 2)) & 3;
                                long newSumPrev = oldSumPrev + (v_out ? 1 : 0);
                                if (newSumPrev > 3)
                                    continue;
                                if (r < R && board.cells[r][c - 1].clue != -1
                                        && newSumPrev > board.cells[r][c - 1].clue)
                                    continue;
                                nextState &= ~(3L << (36 + (c - 1) * 2));
                                nextState |= (newSumPrev << (36 + (c - 1) * 2));
                            }
                            for (int i = 0; i < C; i++) {
                                if (i != c && i != (c - 1)) {
                                    nextState |= (state & (3L << (36 + i * 2)));
                                }
                            }

                            // end of row shift
                            if (c == C) {
                                if (h_out_bit != 0)
                                    continue;
                                long shiftedConn = (nextConn << 3) & 0xFFFFFFFFFL;
                                nextState = (nextState & ~0xFFFFFFFFFL) | shiftedConn;
                            }

                            next.put(nextState, new ParentInfo(state, v_out, h_out));
                        }
                    }
                }
            }
        }

        int lastIdx = (R + 1) * (C + 1);
        for (long finalState : dp[lastIdx].keySet()) {
            if (((finalState & 0xFFFFFFFFFL) == 0) && (((finalState >> 56) & 1) != 0)) {
                backtrack(dp, finalState);
                if (validator.validateFinal(board))
                    return true;
            }
        }
        return false;
    }

    private int findPartner(long conn, int pos, int type) {
        int count = 0;
        if (type == 2) {
            for (int i = pos; i < 12; i++) {
                int v = (int) ((conn >> (i * 3)) & 7);
                if (v == 1)
                    count++;
                else if (v == 2)
                    count--;
                if (count == 0)
                    return i;
            }
        } else {
            for (int i = pos; i >= 0; i--) {
                int v = (int) ((conn >> (i * 3)) & 7);
                if (v == 2)
                    count++;
                else if (v == 1)
                    count--;
                if (count == 0)
                    return i;
            }
        }
        return -1;
    }

    private long setBracket(long conn, int pos, int val) {
        if (pos < 0 || pos >= 12)
            return conn;
        return (conn & ~(7L << (pos * 3))) | ((long) val << (pos * 3));
    }

    private boolean isCompatibleClosure(long conn) {
        for (int i = 0; i < 12; i++) {
            if (((conn >> (i * 3)) & 7) != 0)
                return false;
        }
        return true;
    }

    private void backtrack(Map<Long, ParentInfo>[] dp, long finalState) {
        long cur = finalState;
        for (int r = R; r >= 0; r--) {
            for (int c = C; c >= 0; c--) {
                int nodeIdx = r * (C + 1) + c;
                ParentInfo info = dp[nodeIdx + 1].get(cur);
                if (info == null)
                    return;
                if (r < R) {
                    if (info.v_out)
                        board.vedge[r][c].setActive(PlayerType.CPU);
                    else
                        board.vedge[r][c].setInactive();
                }
                if (c < C) {
                    if (info.h_out)
                        board.hedge[r][c].setActive(PlayerType.CPU);
                    else
                        board.hedge[r][c].setInactive();
                }
                cur = info.prevState;
            }
        }
    }
}
