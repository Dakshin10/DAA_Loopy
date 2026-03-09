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

    // Enhanced Color Palette
    private static final Color BG_COLOR = new Color(15, 15, 20); // Deeper dark
    private static final Color PANEL_BG = new Color(25, 25, 35); // Subtle blue tint
    private static final Color HEADER_BG = new Color(20, 20, 28);
    private static final Color DOT_COLOR = new Color(90, 90, 100);
    private static final Color DOT_ACTIVE = new Color(180, 180, 200);
    private static final Color HUMAN_COLOR = new Color(255, 215, 0); // Gold
    private static final Color CPU_COLOR = new Color(0, 191, 255); // Deep Sky Blue
    private static final Color INACTIVE_COLOR = new Color(255, 255, 255, 40);
    private static final Color CLUE_COLOR_DIM = new Color(130, 130, 140);
    private static final Color CLUE_COLOR_OK = new Color(76, 175, 80); // Material Green
    private static final Color CLUE_COLOR_ERR = new Color(244, 67, 54); // Material Red
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color ACCENT_PURPLE = new Color(156, 39, 176);
    private static final Color ACCENT_PINK = new Color(233, 30, 99);
    private static final String FONT_NAME = "Segoe UI";

    private GameController gameController;
    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel turnLabel;
    private JPanel instructionsPanel;
    private boolean instructionsVisible = false;
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
        printGraphState();
        solved = false;
        isHumanTurn = true;

        cpuTimer = new Timer(1000, e -> {
            if (!solved && !isHumanTurn) {
                try {
                    if (gameController.cpuSingleMove()) {
                        checkGameStatus();
                        printGraphState(); // Print graph details after CPU move
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

    private void printGraphState() {
        Board board = gameController.getBoard();
        System.out.println("\n================ GRAPH STATE UPDATE ================");
        board.printAllEdges();
        board.printTopology();
        System.out.println("====================================================\n");
    }

    private void initUI() {
        setTitle("Slitherlink: Human vs CPU");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(15, 15));

        // Header Panel with Gradient
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gradient = new GradientPaint(
                    0, 0, HEADER_BG,
                    0, getHeight(), BG_COLOR
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 15, 25));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("SLITHERLINK");
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Human vs CPU");
        subtitleLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(150, 150, 160));
        subtitleLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        titlePanel.add(subtitleLabel);
        
        header.add(titlePanel, BorderLayout.WEST);

        turnLabel = new JLabel("YOUR TURN", SwingConstants.RIGHT);
        turnLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        turnLabel.setForeground(HUMAN_COLOR);
        header.add(turnLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Center panel with board and instructions
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_COLOR);
        
        boardPanel = new BoardPanel();
        
        // Wrap boardPanel in GridBagLayout to center it when resizing
        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);
        boardWrapper.add(boardPanel);
        
        centerPanel.add(boardWrapper, BorderLayout.CENTER);
        
        // Instructions Panel (collapsible)
        instructionsPanel = createInstructionsPanel();
        instructionsPanel.setVisible(false);
        centerPanel.add(instructionsPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(10, 30, 30, 30));

        statusLabel = new JLabel("Connect the dots! Human (Yellow) vs CPU (Blue)");
        statusLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(BG_COLOR);

        JButton btnNew = createStyledButton("NEW GAME", ACCENT_PURPLE);
        btnNew.addActionListener(e -> {
            initGame();
            statusLabel.setText("New puzzle generated! Your turn.");
            statusLabel.setForeground(TEXT_COLOR);
            updateTurnIndicator();
            boardPanel.repaint();
        });

        JButton btnHint = createStyledButton("CPU HELP", ACCENT_PINK);
        btnHint.addActionListener(e -> {
            if (!solved && isHumanTurn) {
                isHumanTurn = false;
                statusLabel.setText("CPU is thinking...");
                updateTurnIndicator();
                cpuTimer.start();
            }
        });
        
        JButton btnHelp = createStyledButton("RULES", new Color(0, 150, 136));
        btnHelp.addActionListener(e -> {
            instructionsVisible = !instructionsVisible;
            instructionsPanel.setVisible(instructionsVisible);
            btnHelp.setText(instructionsVisible ? "CLOSE" : "RULES");
            pack();
        });

        JButton btnSolve = createStyledButton("SOLVE PUZZLE", CPU_COLOR);
        btnSolve.addActionListener(e -> {
            if (!solved) {
                isHumanTurn = false;
                statusLabel.setText("Solving puzzle...");
                updateTurnIndicator();
                
                // execute full solve
                if (gameController.executeFullSolve()) {
                    checkGameStatus();
                    printGraphState();
                    gameController.getBoard().printErrors();
                } else {
                    statusLabel.setText("No solution possible from current state.");
                    statusLabel.setForeground(CLUE_COLOR_ERR);
                }
                boardPanel.repaint();
            }
        });

        btnPanel.add(btnNew);
        btnPanel.add(btnHint);
        btnPanel.add(btnSolve);
        btnPanel.add(btnHelp);
        footer.add(btnPanel, BorderLayout.CENTER);

        add(footer, BorderLayout.SOUTH);

        pack();
        setSize(520, 720);
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.brighter(), 2),
                    BorderFactory.createEmptyBorder(11, 23, 11, 23)
                ));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1),
                    BorderFactory.createEmptyBorder(12, 24, 12, 24)
                ));
            }
        });
        return btn;
    }
    
    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(60, 60, 80)),
            new EmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel title = new JLabel("HOW TO PLAY");
        title.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        
        panel.add(Box.createVerticalStrut(12));
        
        String[] rules = {
            "OBJECTIVE: Create a single closed loop by connecting dots",
            "",
            "CLUES: Numbers (0-3) show how many edges surround that cell",
            "   - Green = Satisfied  - Red = Violated  - Gray = Incomplete",
            "",
            "CONTROLS:",
            "   - Left Click = Add edge to loop (Yellow)",
            "   - Right Click = Mark edge as excluded (X)",
            "   - Click Again = Reset to unknown",
            "",
            "WIN CONDITIONS:",
            "   - All clues satisfied",
            "   - Each dot has 0 or 2 connections (no T-junctions)",
            "   - All edges form ONE connected loop",
            "",
            "CPU moves are shown in Blue. Use 'CPU HELP' for hints or 'SOLVE PUZZLE' to finish!"
        };
        
        for (String rule : rules) {
            JLabel label = new JLabel(rule);
            label.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
            label.setForeground(rule.isEmpty() ? TEXT_COLOR : new Color(200, 200, 210));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            if (!rule.isEmpty()) {
                panel.add(Box.createVerticalStrut(4));
            }
        }
        
        return panel;
    }

    private void updateTurnIndicator() {
        if (solved) {
            turnLabel.setText("LOOP COMPLETE");
            turnLabel.setForeground(CLUE_COLOR_OK);
        } else if (isHumanTurn) {
            turnLabel.setText("YOUR TURN");
            turnLabel.setForeground(HUMAN_COLOR);
        } else {
            turnLabel.setText("CPU THINKING...");
            turnLabel.setForeground(CPU_COLOR);
        }
    }

    private void checkGameStatus() {
        if (gameController.isSolved()) {
            solved = true;
            statusLabel.setText("Victory! The loop is closed perfectly!");
            statusLabel.setForeground(CLUE_COLOR_OK);
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
                    SlitherlinkApp.this.printGraphState(); // Print graph details after human move
                    board.printErrors();
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

            // Draw Dots with enhanced visuals
            for (int i = 0; i <= board.rows; i++) {
                for (int j = 0; j <= board.cols; j++) {
                    Node node = board.nodes[i][j];
                    int deg = 0;
                    for (Edge e : node.edges) {
                        if (e.isActive())
                            deg++;
                    }

                    if (deg > 2) {
                        g2d.setColor(CLUE_COLOR_ERR);
                    } else if (deg > 0) {
                        g2d.setColor(DOT_ACTIVE);
                    } else {
                        g2d.setColor(DOT_COLOR);
                    }

                    int dotX = PADDING + j * CELL_SIZE - DOT_RADIUS;
                    int dotY = PADDING + i * CELL_SIZE - DOT_RADIUS;
                    
                    // Subtle glow for active dots
                    if (deg > 0 && deg <= 2) {
                        g2d.setColor(new Color(180, 180, 200, 30));
                        g2d.fillOval(dotX - 2, dotY - 2, DOT_RADIUS * 2 + 4, DOT_RADIUS * 2 + 4);
                        g2d.setColor(DOT_ACTIVE);
                    }
                    
                    g2d.fillOval(dotX, dotY, DOT_RADIUS * 2, DOT_RADIUS * 2);
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
                } catch (Throwable e) {
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
