package snakenladder;

import javax.swing.*;

import project.Utils;

import java.awt.*;

public class MainMenu extends JFrame {

    JComboBox<String> sizeDropdown;

    public MainMenu() {
        setTitle("Snake & Ladders - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize window

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set background image
        setContentPane(new JLabel(new ImageIcon("res/bg1.png")));
        setLayout(null);
        
        // Start background music
        BackgroundMusic.getInstance().play();


        // Title
        JLabel titleLabel = new JLabel("Snake And Ladders", SwingConstants.CENTER);
        titleLabel.setBounds(685, 60, 800, 200);
        titleLabel.setFont(Utils.getCustomFont("res/font1.ttf", 70));
        add(titleLabel);
        
        // Sub title
        JLabel subtitle = new JLabel("Power Up And Special Tile", SwingConstants.CENTER);
        subtitle.setBounds(635, 200, 900, 100);
        subtitle.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(subtitle);
        
        JLabel go = new JLabel("Let's PLAY", SwingConstants.CENTER);
        go.setBounds(460, 370, 500, 100);
        go.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        add(go);

        // Board size label
        JLabel boardLabel = new JLabel("Board Size:");
        boardLabel.setBounds(20, 15, 500, 100);
        boardLabel.setFont(Utils.getCustomFont("res/font1.ttf", 35));
        add(boardLabel);

        // Board size dropdown
        String[] sizes = {"100", "50"};
        sizeDropdown = new RoundedComboBox<>(sizes);
        sizeDropdown.setBounds(20, 90, 180, 40);
        sizeDropdown.setFont(Utils.getCustomFont("res/font1.ttf", 30));
        add(sizeDropdown);

        // Play Button
        JButton playButton = new Rounded3DButton("Play");
        playButton.setBounds(920, 520, 450, 100);
        playButton.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(playButton);

        // Minigame Button
        JButton minigameButton = new Rounded3DButton("Minigame");
        minigameButton.setBounds(920, 670, 450, 100);
        minigameButton.setFont(Utils.getCustomFont("res/font1.ttf", 50));
        add(minigameButton);

        // Help icon
        JLabel helpLabel = new JLabel(new ImageIcon("res/help.png"));
        helpLabel.setBounds(10, 970, 75, 75);
        helpLabel.setToolTipText("<html><b>How to Play:</b><br>"
                + "- Choose board size from dropdown<br>"
                + "- Play starts the Snake & Ladders game<br>"
                + "- Minigame lets you earn power-up!<br>"
                + "- Power-up affect the main game<br>"
                + "- Have fun!</html>");
        helpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        add(helpLabel);


        // Actions
        playButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            int boardSize = Integer.parseInt((String) sizeDropdown.getSelectedItem());
            dispose();
            new player_sec(boardSize);
        });

        minigameButton.addActionListener(e -> {
            BackgroundMusic.playSound("res/button.wav"); // Play sound on click
            dispose();
            new Minigame();
        });

        setVisible(true);
    }

    // Custom rounded 3D white button with hover/press effects
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

    // Custom rounded white dropdown
    static class RoundedComboBox<E> extends JComboBox<E> {
        public RoundedComboBox(E[] items) {
            super(items);
            setOpaque(false);
            setForeground(Color.BLACK);
            setBackground(Color.WHITE);
            setFocusable(false);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g2);
            g2.dispose();
        }

        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(Color.GRAY);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}
