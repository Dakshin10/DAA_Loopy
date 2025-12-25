import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SlitherlinkApp extends JFrame {

    private static final int CELL_SIZE = 60;
    private static final int PADDING = 60;
    private static final int DOT_RADIUS = 6; // Slightly larger for better visuals
    private static final int LINE_THICKNESS = 6;

    // Colors
    private static final Color BG_COLOR = new Color(18, 18, 18);
    private static final Color PANEL_BG = new Color(30, 30, 30);
    private static final Color DOT_COLOR = new Color(80, 80, 80);
    private static final Color HUMAN_COLOR = new Color(255, 235, 59); // Premium Yellow
    private static final Color CPU_COLOR = new Color(3, 169, 244); // Electric Blue
    private static final Color INACTIVE_COLOR = new Color(255, 255, 255, 30); // Subtle 'X'
    private static final Color CLUE_COLOR_DIM = new Color(120, 120, 120);
    private static final Color CLUE_COLOR_OK = new Color(139, 195, 74); // Vibrant Green
    private static final Color CLUE_COLOR_ERR = new Color(255, 82, 82); // Vibrant Red
    private static final Color TEXT_COLOR = new Color(245, 245, 245);
    private static final String FONT_NAME = Font.SANS_SERIF;

    private GameController gameController;
    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private boolean solved = false;
    private boolean isHumanTurn = true;
    private Timer cpuTimer;

    public SlitherlinkApp() {
        initGame();
        initUI();
    }

    private void initGame() {
        Board board = PuzzleGenerator.generate(5, 5);
        gameController = new GameController(board);
        board.printAllEdges();
        board.printNodes();
        solved = false;
        isHumanTurn = true;

        cpuTimer = new Timer(1000, e -> {
            if (!solved && !isHumanTurn) {
                try {
                    if (gameController.executeCpuMove()) {
                        checkGameStatus();
                        gameController.getBoard().printErrors();
                    }
                    isHumanTurn = true;
                    updateTurnIndicator();
                    boardPanel.repaint();
                } catch (Exception ex) {
                    System.err.println("CPU Logic Error: " + ex.getMessage());
                    ex.printStackTrace();
                    isHumanTurn = true;
                }
            }
        });
        cpuTimer.setRepeats(false);
    }

    private void initUI() {
        setTitle("Slitherlink: Human vs CPU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));

        // Header Panel
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("SLITHERLINK", SwingConstants.LEFT);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);

        turnLabel = new JLabel("YOUR TURN", SwingConstants.RIGHT);
        turnLabel.setFont(new Font(FONT_NAME, Font.BOLD, 18));
        turnLabel.setForeground(HUMAN_COLOR);
        header.add(turnLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(10, 30, 30, 30));

        statusLabel = new JLabel("Connect the dots! Human (Yellow) vs CPU (Blue)");
        statusLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(BG_COLOR);

        JButton btnNew = createStyledButton("NEW GAME", new Color(103, 58, 183)); // Deep Purple
        btnNew.addActionListener(e -> {
            initGame();
            statusLabel.setText("New puzzle generated.");
            statusLabel.setForeground(TEXT_COLOR);
            updateTurnIndicator();
            boardPanel.repaint();
        });

        JButton btnHint = createStyledButton("CPU HELP", new Color(255, 64, 129)); // Vivid Magenta
        btnHint.addActionListener(e -> {
            if (!solved && isHumanTurn) {
                isHumanTurn = false;
                updateTurnIndicator();
                cpuTimer.start();
            }
        });

        btnPanel.add(btnNew);
        btnPanel.add(btnHint);
        footer.add(btnPanel, BorderLayout.CENTER);

        add(footer, BorderLayout.SOUTH);

        pack();
        setSize(520, 720);
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void updateTurnIndicator() {
        if (solved) {
            turnLabel.setText("O LOOP COMPLETE");
            turnLabel.setForeground(CLUE_COLOR_OK);
        } else if (isHumanTurn) {
            turnLabel.setText("O YOUR TURN");
            turnLabel.setForeground(HUMAN_COLOR);
        } else {
            turnLabel.setText("O CPU THINKING...");
            turnLabel.setForeground(CPU_COLOR);
        }
    }

    private void checkGameStatus() {
        if (gameController.isSolved()) {
            solved = true;
            statusLabel.setText("Victory! The loop is closed.");
            statusLabel.setForeground(new Color(118, 255, 3));
            updateTurnIndicator();
            LoopReporter.report(gameController.getBoard());
        }
    }

    private class BoardPanel extends JPanel {
        private int hoverRow = -1;
        private int hoverCol = -1;
        private boolean hoverIsH = false;

        public BoardPanel() {
            setPreferredSize(new Dimension(CELL_SIZE * 5 + PADDING * 2, CELL_SIZE * 5 + PADDING * 2));
            setBackground(PANEL_BG);

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (solved)
                        return;
                    handleMouseClick(e.getX(), e.getY(), SwingUtilities.isRightMouseButton(e));
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (solved)
                        return;
                    updateHover(e.getX(), e.getY());
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverRow = -1;
                    hoverCol = -1;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private void handleMouseClick(int x, int y, boolean isRight) {
            if (!isHumanTurn) {
                // If the user attempts to click while CPU is moving,
                // we only permit UNDOING current human moves.
                // But for now, let's just allow all human interactions
                // and if it's a positive move, it will queue/restart the CPU turn.
            }

            Board board = gameController.getBoard();
            Edge target = findEdgeAt(x, y, board);
            if (target != null) {
                EdgeStatus current = target.getStatus();
                EdgeStatus next;

                if (isRight) {
                    next = (current == EdgeStatus.INACTIVE) ? EdgeStatus.UNKNOWN : EdgeStatus.INACTIVE;
                } else {
                    next = (current == EdgeStatus.ACTIVE) ? EdgeStatus.UNKNOWN : EdgeStatus.ACTIVE;
                }

                if (gameController.makeHumanMove(target, next)) {
                    checkGameStatus();
                    board.printErrors(); // Real-time terminal feedback
                    if (!solved && next == EdgeStatus.ACTIVE) {
                        isHumanTurn = false;
                        updateTurnIndicator();
                        cpuTimer.restart();
                    } else if (!solved) {
                        isHumanTurn = true;
                        cpuTimer.stop();
                        updateTurnIndicator();
                    }
                    repaint();
                } else {
                    statusLabel.setText("Rule breaking! Try another spot.");
                    statusLabel.setForeground(CLUE_COLOR_ERR);
                }
            }
        }

        private void updateHover(int x, int y) {
            Board board = gameController.getBoard();
            Edge e = findEdgeAt(x, y, board);
            if (e == null) {
                hoverRow = -1;
            } else {
                // Determine if it's horizontal or vertical for visual feedback
                for (int i = 0; i <= board.rows; i++) {
                    for (int j = 0; j < board.cols; j++) {
                        if (board.hedge[i][j] == e) {
                            hoverRow = i;
                            hoverCol = j;
                            hoverIsH = true;
                            return;
                        }
                    }
                }
                for (int i = 0; i < board.rows; i++) {
                    for (int j = 0; j <= board.cols; j++) {
                        if (board.vedge[i][j] == e) {
                            hoverRow = i;
                            hoverCol = j;
                            hoverIsH = false;
                            return;
                        }
                    }
                }
            }
        }

        private Edge findEdgeAt(int x, int y, Board board) {
            Edge closest = null;
            double minDist = 45.0; // Increased snap distance to eliminate cell dead zones

            // Check Horizontal Edges
            for (int i = 0; i <= board.rows; i++) {
                for (int j = 0; j < board.cols; j++) {
                    int x1 = PADDING + j * CELL_SIZE;
                    int x2 = x1 + CELL_SIZE;
                    int ey = PADDING + i * CELL_SIZE;

                    // Distance from point (x,y) to horizontal segment (x1,ey)-(x2,ey)
                    double dist;
                    if (x < x1)
                        dist = Math.sqrt(Math.pow(x - x1, 2) + Math.pow(y - ey, 2));
                    else if (x > x2)
                        dist = Math.sqrt(Math.pow(x - x2, 2) + Math.pow(y - ey, 2));
                    else
                        dist = Math.abs(y - ey);

                    if (dist < minDist) {
                        minDist = dist;
                        closest = board.hedge[i][j];
                    }
                }
            }

            // Check Vertical Edges
            for (int i = 0; i < board.rows; i++) {
                for (int j = 0; j <= board.cols; j++) {
                    int ex = PADDING + j * CELL_SIZE;
                    int y1 = PADDING + i * CELL_SIZE;
                    int y2 = y1 + CELL_SIZE;

                    // Distance from point (x,y) to vertical segment (ex,y1)-(ex,y2)
                    double dist;
                    if (y < y1)
                        dist = Math.sqrt(Math.pow(x - ex, 2) + Math.pow(y - y1, 2));
                    else if (y > y2)
                        dist = Math.sqrt(Math.pow(x - ex, 2) + Math.pow(y - y2, 2));
                    else
                        dist = Math.abs(x - ex);

                    if (dist < minDist) {
                        minDist = dist;
                        closest = board.vedge[i][j];
                    }
                }
            }
            return closest;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Board board = gameController.getBoard();

            // Draw clues
            g2d.setFont(new Font(FONT_NAME, Font.BOLD, 22));
            for (int i = 0; i < board.rows; i++) {
                for (int j = 0; j < board.cols; j++) {
                    Cell cell = board.cells[i][j];
                    if (cell.clue != -1) {
                        String s = String.valueOf(cell.clue);
                        FontMetrics fm = g2d.getFontMetrics();
                        int lc = cell.lineCount();

                        if (lc == cell.clue)
                            g2d.setColor(CLUE_COLOR_OK);
                        else if (lc > cell.clue)
                            g2d.setColor(CLUE_COLOR_ERR);
                        else
                            g2d.setColor(CLUE_COLOR_DIM);

                        int cx = PADDING + j * CELL_SIZE + CELL_SIZE / 2 - fm.stringWidth(s) / 2;
                        int cy = PADDING + i * CELL_SIZE + CELL_SIZE / 2 + fm.getAscent() / 2 - 2;
                        g2d.drawString(s, cx, cy);
                    }
                }
            }

            // Draw Edges
            for (int i = 0; i <= board.rows; i++) {
                for (int j = 0; j < board.cols; j++) {
                    drawEdge(g2d, board.hedge[i][j], PADDING + j * CELL_SIZE, PADDING + i * CELL_SIZE, true,
                            (hoverIsH && hoverRow == i && hoverCol == j));
                }
            }
            for (int i = 0; i < board.rows; i++) {
                for (int j = 0; j <= board.cols; j++) {
                    drawEdge(g2d, board.vedge[i][j], PADDING + j * CELL_SIZE, PADDING + i * CELL_SIZE, false,
                            (!hoverIsH && hoverRow == i && hoverCol == j));
                }
            }

            // Draw Dots
            for (int i = 0; i <= board.rows; i++) {
                for (int j = 0; j <= board.cols; j++) {
                    Node node = board.nodes[i][j];
                    int deg = 0;
                    for (Edge e : node.edges) {
                        if (e.isActive())
                            deg++;
                    }

                    if (deg > 2)
                        g2d.setColor(CLUE_COLOR_ERR);
                    else
                        g2d.setColor(DOT_COLOR);

                    g2d.fillOval(PADDING + j * CELL_SIZE - DOT_RADIUS, PADDING + i * CELL_SIZE - DOT_RADIUS,
                            DOT_RADIUS * 2, DOT_RADIUS * 2);
                }
            }
        }

        private void drawEdge(Graphics2D g2d, Edge e, int x, int y, boolean horizontal, boolean isHovered) {
            if (e.isActive()) {
                Color c = e.getOwner() == PlayerType.CPU ? CPU_COLOR : HUMAN_COLOR;

                g2d.setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Glow effect
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
                g2d.setStroke(new BasicStroke(LINE_THICKNESS + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (horizontal)
                    g2d.drawLine(x + 2, y, x + CELL_SIZE - 2, y);
                else
                    g2d.drawLine(x, y + 2, x, y + CELL_SIZE - 2);

                // Main line
                g2d.setColor(c);
                g2d.setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (horizontal)
                    g2d.drawLine(x + 2, y, x + CELL_SIZE - 2, y);
                else
                    g2d.drawLine(x, y + 2, x, y + CELL_SIZE - 2);

            } else if (e.isInactive()) {
                g2d.setColor(INACTIVE_COLOR);
                g2d.setStroke(new BasicStroke(2));
                int cx = horizontal ? x + CELL_SIZE / 2 : x;
                int cy = horizontal ? y : y + CELL_SIZE / 2;
                int r = 4;
                g2d.drawLine(cx - r, cy - r, cx + r, cy + r);
                g2d.drawLine(cx + r, cy - r, cx - r, cy + r);
            } else if (isHovered) {
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.setStroke(new BasicStroke(LINE_THICKNESS - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                if (horizontal)
                    g2d.drawLine(x + 5, y, x + CELL_SIZE - 5, y);
                else
                    g2d.drawLine(x, y + 5, x, y + CELL_SIZE - 5);
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Starting SlitherlinkApp...");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("Look and Feel set.");
        } catch (Exception e) {
            System.err.println("Could not set Look and Feel: " + e.getMessage());
        }

        try {
            System.out.println("Invoking UI creation...");
            SwingUtilities.invokeLater(() -> {
                try {
                    SlitherlinkApp app = new SlitherlinkApp();
                    System.out.println("Application instance created.");
                    app.setVisible(true);
                    System.out.println("Application window visible.");
                } catch (Exception e) {
                    System.err.println("CRITICAL UI ERROR: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }
            });
        } catch (Exception e) {
            System.err.println("SWING LAUNCH ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
