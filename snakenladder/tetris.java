//tetris
package snakenladder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class tetris extends JPanel implements ActionListener {
    private Timer timer;
    private boolean isPaused = false;
    private Shape currentPiece;
    private int curX = 5;
    private int curY = 0;
    private int linesCleared = 0;
    private int missionLines;
    private Minigame parent;
    private final int BOARD_WIDTH = 13;  // Grid width
    private final int BOARD_HEIGHT = 23;  // Grid height
    private boolean[][] grid = new boolean[BOARD_WIDTH][BOARD_HEIGHT];
    private final int CELL_WIDTH = 40;   // Width of each cell
    private final int CELL_HEIGHT = 40;  // Height of each cell  

    public tetris(Minigame parent, int missionLines) {
        this.parent = parent;
        this.missionLines = missionLines;
        setBackground(Color.BLACK);
        setFocusable(true);
        setPreferredSize(new Dimension(600, 1000));
      
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                BackgroundMusic.playSound("res/button.wav"); // Play sound on click
                if (isPaused) return;
                if (e.getKeyCode() == KeyEvent.VK_DOWN && isValidMove(0, 1)) curY++;
                if (e.getKeyCode() == KeyEvent.VK_LEFT && isValidMove(-1, 0)) curX--;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT && isValidMove(1, 0)) curX++;
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    tryRotate();
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    while (isValidMove(0, 1)) {
                        curY++;
                    }
                    placePiece();
                    spawnNewPiece();
                }
                repaint();
            }
        });

        timer = new Timer(500, this);
        spawnNewPiece();
    }

    public void start() {
        timer.start();
    }

    public void togglePause() {
        isPaused = !isPaused;
    }

    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;

        if (isValidMove(0, 1)) {
            curY++;
        } else {
            placePiece();
            spawnNewPiece();
        }
        repaint();
    }

    private void spawnNewPiece() {
        currentPiece = new Shape();
        currentPiece.setRandomShape();
        // Spawn in the middle of the board
        curX = BOARD_WIDTH / 2;
        curY = 0;
    }

    private void tryRotate() {
        // Store original coordinates and position
        int[][] originalCoords = new int[4][2];
        for (int i = 0; i < 4; i++) {
            originalCoords[i][0] = currentPiece.x(i);
            originalCoords[i][1] = currentPiece.y(i);
        }
        int originalX = curX;

        // Try normal rotation
        currentPiece.rotate();
        
        // Try wall kicks in this order: 0, +1, -1, +2, -2
        int[] kicks = {0, 1, -1, 2, -2};
        boolean rotationSuccessful = false;
        
        for (int kick : kicks) {
            if (isValidMove(kick, 0)) {
                curX += kick;
                rotationSuccessful = true;
                break;
            }
        }

        // If no valid position found, restore original state
        if (!rotationSuccessful) {
            currentPiece.rotateBack();
            curX = originalX;
        }
    }

    private boolean isValidMove(int dx, int dy) {
        for (int i = 0; i < 4; i++) {
            int newX = curX + currentPiece.x(i) + dx;
            int newY = curY + currentPiece.y(i) + dy;
            if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) return false;
            if (grid[newX][newY]) return false;
        }
        return true;
    }

    private void placePiece() {
        for (int i = 0; i < 4; i++) {
            int x = curX + currentPiece.x(i);
            int y = curY + currentPiece.y(i);
            if (y >= 0 && x >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT) {
                grid[x][y] = true;
            }
        }

        int clearedNow = clearLines();
        linesCleared += clearedNow;

        parent.updateScore(linesCleared);

        if (linesCleared >= missionLines) {
            timer.stop();
            parent.missionComplete();
        }
    }

    private int clearLines() {
        int cleared = 0;
        for (int row = 0; row < BOARD_HEIGHT; row++) {
            if (isLineFull(row)) {
                removeLine(row);
                shiftLinesDown(row);
                cleared++;
            }
        }
        return cleared;
    }

    private boolean isLineFull(int row) {
        for (int col = 0; col < BOARD_WIDTH; col++) {
            if (!grid[col][row]) return false;
        }
        return true;
    }

    private void removeLine(int row) {
        for (int col = 0; col < BOARD_WIDTH; col++) {
            grid[col][row] = false;
        }
    }

    private void shiftLinesDown(int row) {
        for (int r = row; r > 0; r--) {
            for (int col = 0; col < BOARD_WIDTH; col++) {
                grid[col][r] = grid[col][r - 1];
            }
        }
        for (int col = 0; col < BOARD_WIDTH; col++) {
            grid[col][0] = false;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Calculate the starting position to center the board
        int startX = (getWidth() - (BOARD_WIDTH * CELL_WIDTH)) / 2;
        int startY = (getHeight() - (BOARD_HEIGHT * CELL_HEIGHT)) / 2;

        // Draw the grid
        g.setColor(Color.DARK_GRAY);
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                g.drawRect(startX + x * CELL_WIDTH, startY + y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                if (grid[x][y]) {
                    g.setColor(Color.BLUE);
                    g.fillRect(startX + x * CELL_WIDTH, startY + y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
                    g.setColor(Color.DARK_GRAY);
                }
            }
        }

        // Draw the current piece
        g.setColor(Color.GREEN);
        for (int i = 0; i < 4; i++) {
            int x = curX + currentPiece.x(i);
            int y = curY + currentPiece.y(i);
            if (y >= 0 && x >= 0 && x < BOARD_WIDTH && y < BOARD_HEIGHT) {
                g.fillRect(startX + x * CELL_WIDTH, startY + y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT);
            }
        }
    }
}