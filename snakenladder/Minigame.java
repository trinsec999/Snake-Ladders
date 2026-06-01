package snakenladder;

import javax.swing.*;

import project.Utils;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Minigame extends JFrame {
    private JPanel mainPanel;
    private JLabel countdownLabel;
    private JLabel score;
    private final int missionLines = 5;
    private tetris gamePanel;
    private JLabel reward; // class-level variable
    private JLabel rewardLabelValue;
    private String rewardType = getRandomReward();


    // Declare startButton at class level so it can be hidden later
    private JButton startButton;

    public Minigame() {
        setTitle("Tetris Minigame");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window        
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(null);

        JLabel background = new JLabel(new ImageIcon("res/tetris2.png")); // background image
        setContentPane(background);
        background.setLayout(null);

        mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        mainPanel.setBounds(500, 200, 1400, 800);
        background.add(mainPanel);
        
        // Title
        JLabel titleLabel = new JLabel("Snake And Ladders", SwingConstants.CENTER);
        titleLabel.setBounds(925, 98, 800, 100);
        titleLabel.setFont(Utils.getCustomFont("res/font1.ttf", 60));
        getContentPane().add(titleLabel);
        
        // Sub title
        JLabel subtitle = new JLabel("Power Up And Special Tile", SwingConstants.CENTER);
        subtitle.setBounds(925, 176, 800, 100);
        subtitle.setFont(Utils.getCustomFont("res/font1.ttf", 45));
        getContentPane().add(subtitle);

        JButton menuButton = new Rounded3DButton("Menu");
        menuButton.setBounds(10, 10, 200, 40);
        menuButton.setFont(Utils.getCustomFont("res/font1.ttf", 30));
        menuButton.setFocusable(false);
        menuButton.addActionListener(e -> showMenu());
        getContentPane().add(menuButton);
        
        JLabel missionLabel = new JLabel("Mission", SwingConstants.CENTER);
        missionLabel.setBounds(250, 160, 800, 100);
        missionLabel.setFont(Utils.getCustomFont("res/font1.ttf", 65));
        getContentPane();
        mainPanel.add(missionLabel);
        
        JLabel clear = new JLabel("Clear "+missionLines+" lines", SwingConstants.CENTER);
        clear.setBounds(530, 270, 500, 100);
        clear.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        getContentPane();
        mainPanel.add(clear);
        
        JLabel scoreLabel = new JLabel("Score", SwingConstants.CENTER);
        scoreLabel.setBounds(390, 380, 500, 100);
        scoreLabel.setFont(Utils.getCustomFont("res/font1.ttf", 60));
        getContentPane();
        mainPanel.add(scoreLabel);
        
        score = new JLabel("0", SwingConstants.CENTER);
        score.setBounds(395, 465, 500, 100);
        score.setFont(Utils.getCustomFont("res/font1.ttf", 60));
        mainPanel.add(score);
        
        JLabel rewardLabel = new JLabel("Reward", SwingConstants.CENTER);
        rewardLabel.setBounds(420, 550, 500, 100);
        rewardLabel.setFont(Utils.getCustomFont("res/font1.ttf", 60));
        getContentPane();
        mainPanel.add(rewardLabel);
        
        rewardLabelValue = new JLabel(rewardType, SwingConstants.LEFT); // Show selected reward
        rewardLabelValue.setBounds(610, 630, 800, 100);
        rewardLabelValue.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        mainPanel.add(rewardLabelValue);


        countdownLabel = new JLabel("Press Start");
        countdownLabel.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        countdownLabel.setForeground(Color.BLACK);
        countdownLabel.setBounds(0, 179, 500, 100);
        mainPanel.add(countdownLabel);

        // Create the start button and center it inside the Tetris area (assumed 300x500 area)
        startButton = new Rounded3DButton("Start");
        startButton.setFont(Utils.getCustomFont("res/font1.ttf", 40));
        startButton.setBounds(0, 255, 300, 70); // centered inside mainPanel (600x600)
        startButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
        	startCountdown();
        });
        mainPanel.add(startButton);

        setVisible(true);
    }

    private void startCountdown() {
        // Hide the start button when countdown starts
        startButton.setVisible(false);

        countdownLabel.setText("3");
        Timer timer = new Timer(1000, new ActionListener() {
            int count = 2;
            public void actionPerformed(ActionEvent e) {
                if (count > 0) {
                    countdownLabel.setText("" + count);
                    count--;
                } else {
                    countdownLabel.setText("GO!");
                    ((Timer) e.getSource()).stop();
                    startGame();
                }
            }
        });
        timer.start();
    }

    private void startGame() {
        countdownLabel.setVisible(false);
        gamePanel = new tetris(this, missionLines);
        gamePanel.setBounds(340, 20, 600, 1000);  // position the tetris gamePanel
        add(gamePanel);
        gamePanel.requestFocusInWindow();
        gamePanel.start();
    }

    public void updateScore(int lines) {
        score.setText(" " + lines);
    }

    public void missionComplete() {
        BackgroundMusic.playSound("res/powerup.wav"); // Play sound on click
        incrementPowerUp(); // Save selected power-up
        JOptionPane.showMessageDialog(this, "🎉 Mission Complete! '" + rewardType + "' power-up added.");
        dispose(); // Close this window
        new MainMenu();
    }

    private void incrementPowerUp() {
        try {
            File file = new File("res/power.txt");
            int extraDiceCount = 0;
            int shieldCount = 0;
            int reverseCount = 0;

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                if (line != null) {
                    String[] parts = line.trim().split(",");
                    if (parts.length >= 3) {
                        extraDiceCount = Integer.parseInt(parts[0]);
                        shieldCount = Integer.parseInt(parts[1]);
                        reverseCount = Integer.parseInt(parts[2]);
                    } else if (parts.length >= 2) {
                        extraDiceCount = Integer.parseInt(parts[0]);
                        shieldCount = Integer.parseInt(parts[1]);
                    }
                }
                reader.close();
            }

            // Increment based on reward
            if (rewardType.equals("Extra Dice")) {
                extraDiceCount++;
            } else if (rewardType.equals("Shield")) {
                shieldCount++;
            } else if (rewardType.equals("Reverse")) {
                reverseCount++;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(extraDiceCount + "," + shieldCount + "," + reverseCount);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRandomReward() {
        int rand = (int)(Math.random() * 3); // 0, 1, or 2
        switch(rand) {
            case 0: return "Extra Dice";
            case 1: return "Shield";
            default: return "Reverse";
        }
    }

    private void showMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem exit = new JMenuItem("Exit to Main");
        JMenuItem pause = new JMenuItem("Pause");
        JMenuItem restart = new JMenuItem("Restart Game");  // New restart option

        exit.addActionListener(e -> {
            dispose();
            new MainMenu();
        });

        pause.addActionListener(e -> {
            if (gamePanel != null) {
                gamePanel.togglePause();
                JOptionPane.showMessageDialog(null, "Paused. Click OK to resume.");
            }
        });

        restart.addActionListener(e -> {
            if (gamePanel != null) {
                // Stop current game
                gamePanel.setVisible(false);
                mainPanel.remove(gamePanel);
                gamePanel = null;
                
                // Reset UI elements
                startButton.setVisible(true);
                countdownLabel.setVisible(true);
                countdownLabel.setText("Press Start");
                score.setText("0");
                
                // Generate new random reward
                rewardType = getRandomReward();
                rewardLabelValue.setText(rewardType);
                
                // Repaint the panel
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });

        menu.add(restart);  // Add restart option first
        menu.add(pause);
        menu.add(exit);
        menu.show(this, 50, 50);
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
