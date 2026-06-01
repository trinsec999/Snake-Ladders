package snakenladder;

import javax.swing.*;

import project.Utils;

import java.awt.*;

public class player_sec extends JFrame {

    int boardSize;

    public player_sec(int boardSize) {
        this.boardSize = boardSize;

        setTitle("Select Number of Players");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window        
        setLocationRelativeTo(null);

        // Background
        setContentPane(new JLabel(new ImageIcon("res/players1.png")));
        setLayout(null);
        
        
        // Title
        JLabel titleLabel = new JLabel("Snake And Ladders", SwingConstants.CENTER);
        titleLabel.setBounds(650, 180, 800, 100);
        titleLabel.setFont(Utils.getCustomFont("res/font1.ttf", 70));
        add(titleLabel);
        
        // Sub title
        JLabel subtitle = new JLabel("Power Up And Special Tile", SwingConstants.CENTER);
        subtitle.setBounds(650, 270, 800, 100);
        subtitle.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(subtitle);

        JLabel title = new JLabel("Select Number of Players", SwingConstants.CENTER);
        title.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        title.setBounds(600, 420, 900, 100);
        add(title);

        // Player buttons with 3D effect
        JButton twoPlayers = new Rounded3DButton("2 Players");
        twoPlayers.setBounds(815, 580, 450, 80);
        twoPlayers.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(twoPlayers);

        JButton threePlayers = new Rounded3DButton("3 Players");
        threePlayers.setBounds(815, 690, 450, 80);
        threePlayers.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(threePlayers);

        JButton fourPlayers = new Rounded3DButton("4 Players");
        fourPlayers.setBounds(815, 810, 450, 80);
        fourPlayers.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(fourPlayers);

        // Button actions
        twoPlayers.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            dispose();
            new game_board(2, boardSize);
        });

        threePlayers.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            dispose();
            new game_board(3, boardSize);
        });

        fourPlayers.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            dispose();
            new game_board(4, boardSize);
        });

        setVisible(true);
    }

    static class Rounded3DButton extends JButton {
        private boolean hover = false;
        private boolean pressed = false;

        public Rounded3DButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
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
