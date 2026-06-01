package snakenladder;

import javax.swing.*;
import project.Utils;
import java.awt.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class game_board extends JFrame {

    int numPlayers;
    int boardSize;
    int rows;
    int cols;

    JPanel boardPanel;
    JLabel[] playerTokens;
    JLabel[] playerLabels;
    JButton powerUpButton;
    JButton shieldButton;
    JButton reverseButton;
    JLabel diceLabel;
    JLayeredPane layeredPane;

    // Game state variables
    int currentPlayer = 0;              // Index of current player's turn
    int[] playerPositions;              // Array to store each player's position
    boolean[] playerStuck;              // Array to track if players are stuck
    Random rand = new Random();         // Random number generator
    int randomPlayer, randomPlayer1, randomPlayer2;                   // Player chosen for random power-up chance
    JButton rollDiceButton;             // Button to roll dice
    private java.util.ArrayList<Timer> countdownTimers = new java.util.ArrayList<>();  // List to track active countdown timers

    int[][] snakes;
	int[][] ladders;

    // Power-up variables
    int sharedExtraDice = 0;
    int sharedShield = 0;
    int sharedReverse = 0;

    // Snake interaction variables
    private int lastSnakeHead = -1;     // Store the last snake head position landed on
    private int currentSnakeIndex = -1; // Index of current snake being interacted with
    private JLabel countdownLabel;      // Label for reverse power-up countdown

    // Add these new fields at the class level
    private static final int[][] POSITION_OFFSETS = {
        {0, 0},       // Center (for single token)
        {-25, -25},   // Upper left
        {25, -25},    // Upper right
        {-25, 25},    // Lower left
        {25, 25}      // Lower right
    };

    // Add these new fields at the class level
    private int[] playerPlacements;  // Array to store final rankings (0 means not finished)
    private int currentPlacement = 1;  // Counter for tracking current placement
    private int playersFinished = 0;   // Counter for number of players who have finished

    public game_board(int numPlayers, int boardSize) {
        this.numPlayers = numPlayers;
        this.boardSize = boardSize;
        
        rows = (boardSize == 100) ? 10 : 5;
        cols = 10;
        
        // Initialize placements array
        playerPlacements = new int[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            playerPlacements[i] = 0;  // 0 means not finished yet
        }

		if (boardSize == 100) {
	        setContentPane(new JLabel(new ImageIcon("res/100tile.png")));
            snakes = new int[][]{{98, 23}, {89, 14}, {86, 56}};
            ladders = new int[][]{{4, 25}, {13, 46}, {39, 85}};
        } else {
            setContentPane(new JLabel(new ImageIcon("res/50tile.png")));
            snakes = new int[][]{{47, 12}, {44, 17}, {38, 20}};
            ladders = new int[][]{{3, 22}, {8, 26}, {29, 35}};
        }


        setTitle("Snake and Ladders");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window        
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(null);
        
        // Title
        JLabel titleLabel = new JLabel("Snake And Ladders", SwingConstants.CENTER);
        titleLabel.setBounds(6, 55, 800, 100);
        titleLabel.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        getContentPane().add(titleLabel);
        
        // Sub title
        JLabel subtitle = new JLabel("Power Up And Special Tile", SwingConstants.CENTER);
        subtitle.setBounds(6, 100, 800, 100);
        subtitle.setFont(Utils.getCustomFont("res/font1.ttf", 40));
        getContentPane().add(subtitle);

        JLabel title = new JLabel("Power Ups", SwingConstants.CENTER);
        title.setFont(Utils.getCustomFont("res/font1.ttf", 40));
        title.setBounds(9, 200, 800, 100);
        getContentPane().add(title);

        layeredPane = new JLayeredPane();
        layeredPane.setBounds(775, 5, 1090, 1040);
        getContentPane().add(layeredPane);

        boardPanel = new JPanel(new GridLayout(rows, cols));
        boardPanel.setBounds(0, 0, 1090, 1040);
        boardPanel.setOpaque(false);
        boardPanel.setVisible(false);
        layeredPane.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        drawBoard();

        powerUpButton = new Rounded3DButton("Extra Dice (0)");
        powerUpButton.setEnabled(false);
        powerUpButton.setBounds(200, 300, 400, 80);
        powerUpButton.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        getContentPane().add(powerUpButton);
        powerUpButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/dice.wav"); // Play sound on click
            if (sharedExtraDice > 0 && currentPlayer == randomPlayer) {
                sharedExtraDice--;
                powerUpButton.setEnabled(false);
                rollDiceWithoutChangingTurn();
                updatePowerUpDisplay();
                savePowerUpCounts();
            }
        });

        shieldButton = new Rounded3DButton("Shield (0)");
        shieldButton.setEnabled(false);
        shieldButton.setBounds(200, 390, 400, 80);
        shieldButton.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        getContentPane().add(shieldButton);
        shieldButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/shield.wav"); // Play sound on click
            if (sharedShield > 0 && currentPlayer == randomPlayer1 && playerStuck[currentPlayer]) {
                countdownLabel.setVisible(false);
                for (Timer timer : countdownTimers) {
                    timer.stop();
                }
                countdownTimers.clear();
                sharedShield--;
                shieldButton.setEnabled(false);
                playerStuck[currentPlayer] = false;
                updatePowerUpDisplay();
                savePowerUpCounts();
                JOptionPane.showMessageDialog(this, "Shield activated! You won't miss your turn!");
                // Change turn
                findNextUnfinishedPlayer();
                updateSidePanelHighlight();
                // After using shield, enable roll dice button for the current player
                rollDiceButton.setEnabled(true);
                
                // Reset random player after shield use
                randomPlayer1 = -1;
            } else if (currentPlayer == randomPlayer1) {
                // Only handle invalid shield usage if it's the random player's turn
                JOptionPane.showMessageDialog(this, "Cannot use shield at this time!");
            }
        });

        reverseButton = new Rounded3DButton("Reverse (0)");
        reverseButton.setEnabled(false);
        reverseButton.setBounds(200, 480, 400, 80);
        reverseButton.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        getContentPane().add(reverseButton);
        reverseButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/shield.wav"); // Play sound on click
            if (sharedReverse > 0 && currentSnakeIndex >= 0 && currentPlayer == randomPlayer2) {
                // Hide countdown and stop any running countdown timer
                countdownLabel.setVisible(false);
                for (Timer timer : countdownTimers) {
                    timer.stop();
                }
                countdownTimers.clear();

                // Change background based on which snake was triggered
                String backgroundFile = "";
                if (boardSize == 100) {
                    switch (currentSnakeIndex) {
                        case 0: // Snake 98->23
                            backgroundFile = "res/100_head3.png";
                            playerPositions[currentPlayer] = 98 + 2; 
                            // Show winner message
                            JOptionPane.showMessageDialog(this, 
                                "🎉 Congratulations! Player " + (currentPlayer + 1) + " Wins! 🎉",
                                "Game Over",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // Disable all controls
                            rollDiceButton.setEnabled(false);
                            powerUpButton.setEnabled(false);
                            shieldButton.setEnabled(false);
                            reverseButton.setEnabled(false);
                            dispose();
                            new MainMenu();
                            
                            break;
                        case 1: // Snake 89->14
                            backgroundFile = "res/100_head2.png";
                            playerPositions[currentPlayer] = 89 + 3; 
                            break;
                        case 2: // Snake 86->56
                            backgroundFile = "res/100_head1.png";
                            playerPositions[currentPlayer] = 86 + 9;
                            break;
                    }
                } else {
                    switch (currentSnakeIndex) {
                        case 0: // Snake 47->12
                            backgroundFile = "res/50_head3.png";
                            playerPositions[currentPlayer] = 47 + 2; 
                            break;
                        case 1: // Snake 44->17
                            backgroundFile = "res/50_head2.png";
                            playerPositions[currentPlayer] = 44 + 2; 
                            break;
                        case 2: // Snake 38->20
                            backgroundFile = "res/50_head1.png";
                            playerPositions[currentPlayer] = 38 + 5; 
                            break;
                    }
                }

                // Store all components
                Component[] components = getContentPane().getComponents();
                
                // Remove all components temporarily
                getContentPane().removeAll();
                
                // Set the new background
                setContentPane(new JLabel(new ImageIcon(backgroundFile)));
                getContentPane().setLayout(null);
                
                // Re-add all original components
                for (Component comp : components) {
                    if (comp != null && !(comp instanceof JLabel && comp.getBounds().equals(new Rectangle(0, 0, getWidth(), getHeight())))) {
                        getContentPane().add(comp);
                    }
                }

                // Update player position and UI
                updatePlayerToken(currentPlayer);

                // Use up reverse power
                sharedReverse--;
                updatePowerUpDisplay();
                savePowerUpCounts();

                // Reset states
                lastSnakeHead = -1;
                currentSnakeIndex = -1;
                reverseButton.setEnabled(false);

                // Change turn
                findNextUnfinishedPlayer();

                // Create a timer to restore the original background after a short delay
                Timer restoreTimer = new Timer(1000, evt -> {
                    ((Timer)evt.getSource()).stop();
                    restoreOriginalBackground();
                });
                restoreTimer.setRepeats(false);
                restoreTimer.start();

                // Re-enable roll dice button only after reverse is no longer available
                rollDiceButton.setEnabled(true);

                // Refresh the frame
                revalidate();
                repaint();
            }
        });

        playerTokens = new JLabel[numPlayers];
        playerPositions = new int[numPlayers];
        playerStuck = new boolean[numPlayers];
        String[] playerIcons = {"res/1.png", "res/2.png", "res/3.png", "res/4.png"};

        loadPowerUpCounts();

        // Title "PLAYERS"
        JLabel playersTitle = new JLabel("PLAYERS", SwingConstants.CENTER);
        playersTitle.setBounds(9, 560, 500, 100);
        playersTitle.setFont(Utils.getCustomFont("res/font1.ttf", 40));
        getContentPane().add(playersTitle);

        // 2x2 panel for player icons
        JPanel iconGrid = new JPanel(new GridLayout(2, 2, 5, 5));
        iconGrid.setBounds(420, 570, 200, 200);
        iconGrid.setOpaque(false);
        getContentPane().add(iconGrid);

        // Add token icons to side panel and board
        for (int i = 0; i < numPlayers; i++) {
            JLabel icon = new JLabel(new ImageIcon(playerIcons[i]));
            iconGrid.add(icon);
            playerTokens[i] = new JLabel(new ImageIcon(playerIcons[i]));
            playerTokens[i].setSize(50, 50); // Make tokens slightly smaller to fit better
            layeredPane.add(playerTokens[i], JLayeredPane.PALETTE_LAYER);
        }


        diceLabel = new JLabel(new ImageIcon("res/die1.png"));
        diceLabel.setBounds(450, 800, 100, 100);
        getContentPane().add(diceLabel);

        rollDiceButton = new Rounded3DButton("Roll Dice");
        rollDiceButton.setBounds(350, 900, 300, 80);
        rollDiceButton.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        getContentPane().add(rollDiceButton);
        rollDiceButton.addActionListener(e -> {
            if (!reverseButton.isEnabled()) {
                rollDice();
            }
        });

        JButton menuButton = new Rounded3DButton("Menu");
        menuButton.setBounds(10, 10, 200, 40);
        menuButton.setFont(Utils.getCustomFont("res/font1.ttf", 30));
        getContentPane().add(menuButton);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem exitItem = new JMenuItem("Exit");
        menu.add(exitItem);

        menuButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
        	menu.show(menuButton, 0, menuButton.getHeight());
        });

        exitItem.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            dispose();
            new MainMenu();
        });

        // Initialize countdown label
        countdownLabel = new JLabel("");
        countdownLabel.setFont(Utils.getCustomFont("res/font1.ttf", 30));
        countdownLabel.setBounds(200, 262, 400, 40);
        countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countdownLabel.setVisible(false);
        getContentPane().add(countdownLabel);

        setVisible(true);
        setFocusable(true);
        updatePowerUpDisplay();

        // Check for initial power-up eligibility for the first player
        checkPowerUpEligibility();
    }
    
    private void checkPowerUpEligibility() {
        // Don't check power-ups for finished players
        if (playerPlacements[currentPlayer] > 0) {
            powerUpButton.setEnabled(false);
            return;
        }

        // Only check if power-up button is not already enabled
        if (!powerUpButton.isEnabled()) {
            // Check for power-up eligibility with 30% chance
            if (sharedExtraDice > 0 && rand.nextInt(100) < 30) {
                randomPlayer = rand.nextInt(numPlayers);
                // Only assign to players who haven't finished
                while (playerPlacements[randomPlayer] > 0) {
                    randomPlayer = rand.nextInt(numPlayers);
                }
                System.out.println("Extra Dice Random Player: " + (randomPlayer + 1));
                if (randomPlayer == currentPlayer) {
                    powerUpButton.setEnabled(true);
                }
            } else {
                powerUpButton.setEnabled(false);
            }
        }
    }

    private void rollDiceWithoutChangingTurn() {
        new Thread(() -> {
            BackgroundMusic.playSound("res/dice.wav"); // Play sound on click
            try {
                int result = rand.nextInt(6) + 1;
                for (int i = 0; i < 10; i++) {
                    int temp = rand.nextInt(6) + 1;
                    diceLabel.setIcon(new ImageIcon("res/die" + temp + ".png"));
                    Thread.sleep(50);
                }
                diceLabel.setIcon(new ImageIcon("res/die" + result + ".png"));

                // Move player but do NOT switch turn
                movePlayer(result, false);
                
                // Enable roll button after the extra dice movement is complete
                if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                    rollDiceButton.setEnabled(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }


    private void rollDice() {
        // Don't allow rolling if player has finished or reverse button is enabled
        if (reverseButton.isEnabled() || playerPlacements[currentPlayer] > 0) {
            return;
        }
        
        // Disable roll button immediately
        rollDiceButton.setEnabled(false);
        
        // Disable power-up button if it was enabled but not used
        powerUpButton.setEnabled(false);
        
        new Thread(() -> {
            try {
                BackgroundMusic.playSound("res/dice.wav"); // Play sound on click
                int result = rand.nextInt(6) + 1;
                for (int i = 0; i < 10; i++) {
                    int temp = rand.nextInt(6) + 1;
                    diceLabel.setIcon(new ImageIcon("res/die" + temp + ".png"));
                    Thread.sleep(50);
                }
                diceLabel.setIcon(new ImageIcon("res/die" + result + ".png"));

                // Move player after dice animation
                movePlayer(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }


    private void movePlayer(int steps) {
        movePlayer(steps, true);
        updateSidePanelHighlight();
    }
    
    private void movePlayer(int steps, boolean changeTurn) {
        if (playerStuck[currentPlayer]) {
            JOptionPane.showMessageDialog(this, "Player " + (currentPlayer + 1) + " is stuck this turn!");
            playerStuck[currentPlayer] = false;
            // Change turn after showing stuck message
            if (changeTurn) {
                findNextUnfinishedPlayer();
                rollDiceButton.setEnabled(true);
            } else {
                // If not changing turn (after extra dice), enable roll button
                if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                    rollDiceButton.setEnabled(true);
                }
            }
        } else {
            final int startPosition = playerPositions[currentPlayer];
            
            // Calculate final position with bounce back if overshooting
            int targetPosition = startPosition + steps;
            if (targetPosition > boardSize) {
                // Calculate how many steps we went past boardSize
                int extraSteps = targetPosition - boardSize;
                // Bounce back those extra steps
                targetPosition = boardSize - extraSteps;
                
                // Show bounce back message
                JOptionPane.showMessageDialog(this, 
                    "Overshot! Bouncing back from " + boardSize + " to " + targetPosition);
            }
            
            // Create list of positions to animate through
            java.util.List<Integer> animationPositions = new java.util.ArrayList<>();
            
            // Forward movement to boardSize (or target if less than boardSize)
            for (int pos = startPosition + 1; pos <= Math.min(boardSize, startPosition + steps); pos++) {
                animationPositions.add(pos);
            }
            
            // If we need to bounce back
            if (startPosition + steps > boardSize) {
                // Add positions for the bounce back movement
                for (int pos = boardSize - 1; pos >= targetPosition; pos--) {
                    animationPositions.add(pos);
                }
            }
            
            // Update the position immediately
            playerPositions[currentPlayer] = targetPosition;
            
            // Keep roll button disabled during animation
            rollDiceButton.setEnabled(false);
            
            // Start the step-by-step animation with the calculated positions
            animatePlayerMovementWithPath(currentPlayer, animationPositions, () -> {
                // First check if player has won
                if (playerPositions[currentPlayer] == boardSize) {
                    handleWinCondition();
                    return;
                }

                boolean landedOnSnake = false;
                for (int i = 0; i < snakes.length; i++) {
                    int[] s = snakes[i];
                    if (playerPositions[currentPlayer] == s[0]) {
                        lastSnakeHead = s[0];
                        currentSnakeIndex = i;
                        landedOnSnake = true;
                        
                        if (sharedReverse > 0 && rand.nextInt(100) < 80) {
                            randomPlayer2 = currentPlayer;
                            System.out.println("Reverse Random Player: " + (randomPlayer2 + 1));
                            rollDiceButton.setEnabled(false);
                            reverseButton.setEnabled(true);
                            startCountdown(s, changeTurn);
                        } else {
                            BackgroundMusic.playSound("res/snake.wav"); // Play sound on click
                            // Instant snake movement if not lucky or no reverse available
                            JOptionPane.showMessageDialog(this, "Down the snake you go!");

                            playerPositions[currentPlayer] = s[1];
                            updatePlayerTokenInstant(currentPlayer);
                            
                            // Change turn only if changeTurn is true
                            if (changeTurn) {
                                findNextUnfinishedPlayer();
                                rollDiceButton.setEnabled(true);
                            } else {
                                // If not changing turn (after extra dice), enable roll button
                                if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                                    rollDiceButton.setEnabled(true);
                                }
                            }
                        }
                        break;
                    }
                }

                if (!landedOnSnake) {
                    lastSnakeHead = -1;
                    currentSnakeIndex = -1;
                    reverseButton.setEnabled(false);
                    countdownLabel.setVisible(false);
                    
                    for (int[] l : ladders) {
                        if (playerPositions[currentPlayer] == l[0]) {
                            BackgroundMusic.playSound("res/shield.wav"); // Play sound on click
                            JOptionPane.showMessageDialog(this, "Yay! Ladder!");
                            // Instant ladder movement
                            playerPositions[currentPlayer] = l[1];
                            updatePlayerTokenInstant(currentPlayer);
                            
                            // Check for win after ladder movement
                            if (playerPositions[currentPlayer] == boardSize) {
                                handleWinCondition();
                                return;
                            }
                            break;
                        }
                    }

                    // Handle trap tiles
                    if (playerPositions[currentPlayer] % 9 == 0) {
                        if (sharedShield > 0 && rand.nextInt(100) < 80) {
                            randomPlayer1 = currentPlayer;
                            System.out.println("Shield Random Player: " + (randomPlayer1 + 1));
                            JOptionPane.showMessageDialog(this, "Oh no! Trap tile.\nLucky! You can use a shield to avoid missing a turn!");
                            shieldButton.setEnabled(true);
                            playerStuck[currentPlayer] = true;
                            rollDiceButton.setEnabled(false);
                            
                            // Start the shield countdown
                            startShieldCountdown(changeTurn);
                        } else {
                            BackgroundMusic.playSound("res/snake.wav"); // Play sound on click
                            int trappedPlayer = currentPlayer;  // Store the trapped player
                            JOptionPane.showMessageDialog(this, "Oh no! Player " + (trappedPlayer + 1) + " landed on a trap tile and will miss their next turn!");
                            
                            // Mark the player as stuck for their next turn
                            playerStuck[trappedPlayer] = true;
                            
                            // Change to next player's turn if changeTurn is true
                            if (changeTurn) {
                                findNextUnfinishedPlayer();
                                rollDiceButton.setEnabled(true);
                            } else {
                                // If not changing turn (after extra dice), enable roll button
                                if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                                    rollDiceButton.setEnabled(true);
                                }
                            }
                        }
                    } else if (changeTurn) {
                        // Normal turn change if no special tile was hit
                        findNextUnfinishedPlayer();
                        if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                            rollDiceButton.setEnabled(true);
                        }
                    } else {
                        // If not changing turn (after extra dice), enable roll button
                        if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                            rollDiceButton.setEnabled(true);
                        }
                    }
                }
            });
        }
    }
    
    private void updateSidePanelHighlight() {
        Component[] components = getContentPane().getComponents();
        JPanel iconGrid = null;
        for (Component comp : components) {
            if (comp instanceof JPanel && comp.getBounds().x == 420 && comp.getBounds().y == 570) {
                iconGrid = (JPanel) comp;
                break;
            }
        }
        
        if (iconGrid != null) {
            Component[] icons = iconGrid.getComponents();
            for (int i = 0; i < icons.length; i++) {
                JLabel icon = (JLabel) icons[i];
                if (playerPlacements[i] > 0) {
                    // Player has finished - show placement
                    icon.setOpaque(true);
                    icon.setBackground(new Color(144, 238, 144, 128)); // Light green
                    icon.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
                    // Add placement number
                    icon.setText(String.valueOf(playerPlacements[i]));
                    icon.setHorizontalTextPosition(JLabel.CENTER);
                    icon.setVerticalTextPosition(JLabel.BOTTOM);
                } else if (i == currentPlayer) {
                    // Current player
                    icon.setOpaque(true);
                    icon.setBackground(new Color(255, 255, 0, 128)); // Semi-transparent yellow
                    icon.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                    icon.setText("");
                } else {
                    // Regular player
                    icon.setOpaque(false);
                    icon.setBackground(null);
                    icon.setBorder(null);
                    icon.setText("");
                }
            }
            iconGrid.repaint();
        }
    }

    private void animatePlayerMovement(int player, int fromPos, int toPos, Runnable onComplete) {
        if (fromPos >= toPos) {
            onComplete.run();
            return;
        }
        
        // Disable roll button during animation
        rollDiceButton.setEnabled(false);
        
        // Create array of positions to animate through
        java.util.List<Integer> positions = new java.util.ArrayList<>();
        for (int i = fromPos; i <= toPos; i++) {
            positions.add(i);
        }
        
        // Create timer for animation with faster speed (400ms per step)
        Timer animationTimer = new Timer(400, null);
        final int[] currentIndex = {0};
        
        animationTimer.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            if (currentIndex[0] < positions.size()) {
                int currentPos = positions.get(currentIndex[0]);
                playerPositions[player] = currentPos;
                
                // Adjust all tokens on the current tile
                adjustTokenPositionsInTile(currentPos);
                
                // Highlight current tile
                highlightCurrentTile((currentPos - 1) / cols, (currentPos - 1) % cols);
                
                currentIndex[0]++;
                
                // If this is the last position
                if (currentIndex[0] >= positions.size()) {
                    animationTimer.stop();
                    clearTileHighlights();
                    onComplete.run();
                }
            }
        });
        
        animationTimer.start();
    }

    private void highlightCurrentTile(int row, int col) {
        clearTileHighlights();
        if (boardPanel != null) {
            Component[] components = boardPanel.getComponents();
            int index = row * cols + (row % 2 == 1 ? (cols - 1 - col) : col);
            if (index >= 0 && index < components.length) {
                JPanel tile = (JPanel) components[index];
                tile.setBackground(new Color(255, 255, 0, 100));
                tile.setOpaque(true);
            }
        }
    }

    private void clearTileHighlights() {
        if (boardPanel != null) {
            for (Component comp : boardPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel tile = (JPanel) comp;
                    tile.setBackground(null);
                    tile.setOpaque(false);
                }
            }
        }
        boardPanel.repaint();
    }

    private void adjustTokenPositionsInTile(int tilePosition) {
        // Get all players on this tile
        java.util.List<Integer> playersOnTile = new java.util.ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            if (playerPositions[i] == tilePosition) {
                playersOnTile.add(i);
            }
        }

        // If only one player, center the token
        if (playersOnTile.size() == 1) {
            int player = playersOnTile.get(0);
            updatePlayerTokenPosition(player, tilePosition, 0); // Center position
            return;
        }

        // If multiple players, distribute them around the tile
        for (int i = 0; i < playersOnTile.size(); i++) {
            int player = playersOnTile.get(i);
            updatePlayerTokenPosition(player, tilePosition, i + 1); // i+1 to skip center position
        }
    }

    private void updatePlayerTokenPosition(int player, int position, int offsetIndex) {
        if (position <= 0 || position > boardSize) return;

        int tileWidth = 1090 / cols;
        int tileHeight = 1040 / rows;
        
        int r = (position - 1) / cols;
        int c = (position - 1) % cols;
        
        if ((r % 2) == 1) {
            c = cols - 1 - c;
        }
        
        int screenRow = rows - 1 - r;
        
        // Calculate base position (center of tile)
        int baseX = c * tileWidth + (tileWidth - 100) / 2;
        int baseY = screenRow * tileHeight + (tileHeight - 100) / 2;

        // Apply offset based on position index
        int[] offset = POSITION_OFFSETS[offsetIndex];
        int finalX = baseX + offset[0];
        int finalY = baseY + offset[1];
        
        playerTokens[player].setBounds(finalX, finalY, 100, 100);
    }

    private void updatePlayerToken(int player) {
        int pos = playerPositions[player];
        if (pos <= 0 || pos > boardSize) return;

        // After updating the token's position, adjust all tokens on the same tile
        adjustTokenPositionsInTile(pos);
    }

    private void updatePlayerTokenInstant(int player) {
        int pos = playerPositions[player];
        if (pos <= 0 || pos > boardSize) return;

        // After updating the token's position, adjust all tokens on the same tile
        adjustTokenPositionsInTile(pos);
    }

    private void updatePowerUpDisplay() {
        powerUpButton.setText("Extra Dice (" + sharedExtraDice + ")");
        shieldButton.setText("Shield (" + sharedShield + ")");
        reverseButton.setText("Reverse (" + sharedReverse + ")");
    }

    private void drawBoard() {
        boardPanel.removeAll();
        boolean leftToRight = true;
        int tileNum = boardSize;
        JPanel[][] tiles = new JPanel[rows][cols];

        for (int r = 0; r < rows; r++) {
            JPanel[] rowTiles = new JPanel[cols];
            for (int c = 0; c < cols; c++) {
                int index = leftToRight ? c : cols - 1 - c;
                JPanel tile = new JPanel();
                tile.setLayout(new BorderLayout());
                JLabel num = new JLabel(String.valueOf(tileNum), SwingConstants.CENTER);
                num.setForeground(Color.BLACK);
                tile.add(num);
                if (tileNum % 9 == 0) {
                    tile.add(new JLabel(new ImageIcon("res/web.png")), BorderLayout.CENTER);
                }
                rowTiles[index] = tile;
                tiles[r][index] = tile;
                tileNum--;
            }
            for (JPanel t : rowTiles) {
                boardPanel.add(t);
            }
            leftToRight = !leftToRight;
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void loadPowerUpCounts() {
        try {
            File file = new File("res/power.txt");
            if (file.exists()) {
                List<String> lines = Files.readAllLines(Paths.get("res/power.txt"));
                if (!lines.isEmpty()) {
                    String[] parts = lines.get(0).trim().split(",");
                    sharedExtraDice = Integer.parseInt(parts[0]);
                    if (parts.length > 1) {
                        sharedShield = Integer.parseInt(parts[1]);
                    }
                    if (parts.length > 2) {
                        sharedReverse = Integer.parseInt(parts[2]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePowerUpCounts() {
        try {
            Files.write(Paths.get("res/power.txt"), 
                (sharedExtraDice + "," + sharedShield + "," + sharedReverse).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCountdown(int[] snakePosition, boolean changeTurn) {
        countdownLabel.setVisible(true);
        countdownLabel.setBounds(200, 262, 400, 40);
        Timer countTimer = new Timer(1000, null);
        countdownTimers.add(countTimer);  // Add timer to the list
        
        int[] timeLeft = {5}; // Using array to allow modification in lambda
        
        countTimer.addActionListener(e -> {
            timeLeft[0]--;
            if (timeLeft[0] > 0) {
                rollDiceButton.setEnabled(false);  // Keep roll dice button disabled during countdown
            } else {
                countdownLabel.setVisible(false);
                countTimer.stop();
                countdownTimers.remove(countTimer);  // Remove timer from list
                
                // Time's up - move down the snake
                playerPositions[currentPlayer] = snakePosition[1];
                updatePlayerToken(currentPlayer);
                reverseButton.setEnabled(false);
                
                JOptionPane.showMessageDialog(this, "Down the snake you go!");
                
                // Change turn and enable roll button for next player
                if (changeTurn) {
                    findNextUnfinishedPlayer();
                    rollDiceButton.setEnabled(true);
                }
            }
        });
        
        countdownLabel.setText(" 5");
        countTimer.start();
    }

    private void restoreOriginalBackground() {
        // Store all components
        Component[] components = getContentPane().getComponents();
        
        // Remove all components temporarily
        getContentPane().removeAll();
        
        // Set back to original background based on board size
        if (boardSize == 100) {
            setContentPane(new JLabel(new ImageIcon("res/100tile.png")));
        } else {
            setContentPane(new JLabel(new ImageIcon("res/50tile.png")));
        }
        getContentPane().setLayout(null);
        
        // Re-add all original components
        for (Component comp : components) {
            if (comp != null && !(comp instanceof JLabel && comp.getBounds().equals(new Rectangle(0, 0, getWidth(), getHeight())))) {
                getContentPane().add(comp);
            }
        }
        
        // Refresh the frame
        revalidate();
        repaint();
    }

    // Add new method for animating through a specific path of positions
    private void animatePlayerMovementWithPath(int player, java.util.List<Integer> positions, Runnable onComplete) {
        if (positions.isEmpty()) {
            onComplete.run();
            return;
        }
        
        // Disable roll button during animation
        rollDiceButton.setEnabled(false);
        
        // Create timer for animation with faster speed (400ms per step)
        Timer animationTimer = new Timer(400, null);
        final int[] currentIndex = {0};
        
        animationTimer.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            if (currentIndex[0] < positions.size()) {
                int currentPos = positions.get(currentIndex[0]);
                playerPositions[player] = currentPos;
                
                // Adjust all tokens on the current tile
                adjustTokenPositionsInTile(currentPos);
                
                // Highlight current tile
                highlightCurrentTile((currentPos - 1) / cols, (currentPos - 1) % cols);
                
                currentIndex[0]++;
                
                // If this is the last position
                if (currentIndex[0] >= positions.size()) {
                    animationTimer.stop();
                    clearTileHighlights();
                    onComplete.run();
                }
            }
        });
        
        animationTimer.start();
    }

    private void handleWinCondition() {
        // Record the placement for the current player
        playerPlacements[currentPlayer] = currentPlacement;
        playersFinished++;
        currentPlacement++;

        BackgroundMusic.playSound("res/powerup.wav"); // Play sound on click
        // Show immediate placement message
        JOptionPane.showMessageDialog(this, 
            "🎉 Player " + (currentPlayer + 1) + " finished in place #" + playerPlacements[currentPlayer] + "! 🎉");

        // Check if only one player is left unfinished
        if (playersFinished == numPlayers - 1) {
            // Find the last unfinished player
            int lastPlayer = -1;
            for (int i = 0; i < numPlayers; i++) {
                if (playerPlacements[i] == 0) {
                    lastPlayer = i;
                    break;
                }
            }
            
            // Assign last place to the remaining player
            playerPlacements[lastPlayer] = currentPlacement;
            playersFinished++;
            
            // Show message for last place
            JOptionPane.showMessageDialog(this, 
                "Player " + (lastPlayer + 1) + " takes " + currentPlacement + "th place!");
            
            // Show final results
            showFinalResults();
        } else {
            // Move to next unfinished player
            findNextUnfinishedPlayer();
        }
    }

    private void showFinalResults() {
        BackgroundMusic.playSound("res/powerup.wav"); // Play sound on click
        StringBuilder finalResults = new StringBuilder("Final Rankings:\n\n");
        // Create array of player indices and sort by placement
        Integer[] playerIndices = new Integer[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            playerIndices[i] = i;
        }
        java.util.Arrays.sort(playerIndices, (a, b) -> playerPlacements[a] - playerPlacements[b]);
        
        // Build results message with appropriate medals/formatting
        for (int i = 0; i < numPlayers; i++) {
            int player = playerIndices[i];
            String medal = "";
            switch (playerPlacements[player]) {
                case 1: medal = "🥇"; break;
                case 2: medal = "🥈"; break;
                case 3: medal = "🥉"; break;
                default: medal = "  "; break;
            }
            finalResults.append(medal)
                       .append(" ")
                       .append(playerPlacements[player])
                       .append("th Place: Player ")
                       .append(player + 1);
            
            // Add position indicator for the last player
            if (playerPlacements[player] == numPlayers) {
                finalResults.append(" (Last Position)");
            }
            finalResults.append("\n");
        }

        // Show final results
        JOptionPane.showMessageDialog(this, 
            finalResults.toString(),
            "Game Over - Final Results",
            JOptionPane.INFORMATION_MESSAGE);

        // Disable all controls
        rollDiceButton.setEnabled(false);
        powerUpButton.setEnabled(false);
        shieldButton.setEnabled(false);
        reverseButton.setEnabled(false);
        
        // Return to main menu
        dispose();
        new MainMenu();
    }

    private void findNextUnfinishedPlayer() {
        // Count how many players are still unfinished
        int unfinishedCount = 0;
        int lastUnfinishedPlayer = -1;
        
        // Find all unfinished players
        for (int i = 0; i < numPlayers; i++) {
            if (playerPlacements[i] == 0) {
                unfinishedCount++;
                lastUnfinishedPlayer = i;
            }
        }

        // If only one player is left unfinished, automatically assign last place
        if (unfinishedCount == 1) {
            currentPlayer = lastUnfinishedPlayer;
            playerPlacements[currentPlayer] = numPlayers; // Assign last place
            playersFinished++;
            
            // Show message for last place
            JOptionPane.showMessageDialog(this, 
                "Player " + (currentPlayer + 1) + " takes last place!");
            
            // Show final results
            showFinalResults();
            return;
        }

        // Find next unfinished player
        int startingPlayer = currentPlayer;
        do {
            currentPlayer = (currentPlayer + 1) % numPlayers;
            // If we've gone full circle and found no unfinished players, break
            if (currentPlayer == startingPlayer) {
                break;
            }
        } while (playerPlacements[currentPlayer] > 0);

        // Update UI and controls
        updateSidePanelHighlight();
        powerUpButton.setEnabled(false);
        checkPowerUpEligibility();
        if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
            rollDiceButton.setEnabled(true);
        }
    }

    private void startShieldCountdown(boolean changeTurn) {
        countdownLabel.setVisible(true);
        countdownLabel.setBounds(200, 262, 400, 40);
        Timer countTimer = new Timer(1000, null);
        countdownTimers.add(countTimer);  // Add timer to the list
        
        int[] timeLeft = {5}; // Using array to allow modification in lambda
        
        countTimer.addActionListener(e -> {
            timeLeft[0]--;
            if (timeLeft[0] > 0) {
                countdownLabel.setText(" " + timeLeft[0]);
                rollDiceButton.setEnabled(false);  // Keep roll dice button disabled during countdown
            } else {
                countdownLabel.setVisible(false);
                countTimer.stop();
                countdownTimers.remove(countTimer);  // Remove timer from list
                
                shieldButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Shield opportunity expired!");
                
                if (changeTurn) {
                    findNextUnfinishedPlayer();
                    rollDiceButton.setEnabled(true);
                    randomPlayer1 = -1;  // Reset random player if shield wasn't used
                } else {
                    // If not changing turn (after extra dice), enable roll button
                    if (!reverseButton.isEnabled() && !shieldButton.isEnabled()) {
                        rollDiceButton.setEnabled(true);
                    }
                }
            }
        });
        
        countdownLabel.setText(" 5");
        countTimer.start();
    }

    static class Rounded3DButton extends JButton {
        private boolean hover = false;
        private boolean pressed = false;

        public Rounded3DButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setForeground(Color.BLACK);
            setFont(new Font("Arial", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                public void mousePressed(java.awt.event.MouseEvent evt) {
                    pressed = true;
                    repaint();
                }

                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    pressed = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            Color topColor, bottomColor;

            if (pressed) {
                topColor = new Color(200, 200, 200);
                bottomColor = new Color(180, 180, 180);
            } else if (hover) {
                topColor = new Color(230, 230, 230);
                bottomColor = new Color(200, 200, 200);
            } else {
                topColor = new Color(255, 255, 255);
                bottomColor = new Color(220, 220, 220);
            }

            GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, height, bottomColor);
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, width, height, 20, 20);

            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(160, 160, 160));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }

        public void updateUI() {
            super.updateUI();
            setOpaque(false);
        }
    }
}