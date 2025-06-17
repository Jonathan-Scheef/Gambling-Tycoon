import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SlotMachine {
    private static final String[] SYMBOLS = {
        "7.png", "Bar.png", "Diamant.png", "Glocke.png", "Herz.png", "Hufeisen.png", "Kirsche.png", "Wassermelone.png", "Zitrone.png"
    };
    private static final String SLOT_IMAGE = "assets/SlotMachine.png";
    private static final String HEBEL_IMAGE = "assets/Hebel.png";    // Gewinnmultiplikatoren für die Symbole (von 7 bis Zitrone) - Erwartungswert ≈ 0.95
    private static final int[] SYMBOL_VALUES = {35, 30, 25, 23, 21, 20, 18, 16, 14};
    private static JLabel resultLabel;
    private static int currentBet = 10;

    /**
     * Returns a simple outline panel for the Slot Machine game.
     * @param onBack Runnable to call when the user wants to go back to the menu
     */
    public static JPanel createSlotMachinePanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        // Top panel with back button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        panel.add(topPanel, BorderLayout.NORTH);

        // Slotmachine- und Hebel-Bilder laden
        ImageIcon slotIcon = new ImageIcon(SLOT_IMAGE);
        ImageIcon leverIcon = new ImageIcon(HEBEL_IMAGE);
        int slotW = slotIcon.getIconWidth();
        int slotH = slotIcon.getIconHeight();
        int leverW = leverIcon.getIconWidth();
        int leverH = leverIcon.getIconHeight();

        // Symbole laden
        ImageIcon[] symbolIcons = new ImageIcon[SYMBOLS.length];
        for (int i = 0; i < SYMBOLS.length; i++) {
            symbolIcons[i] = new ImageIcon("assets/" + SYMBOLS[i]);
        }        // Panel für Slotmachine
        JPanel centerPanel = new JPanel(null) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                int cx = (getWidth() - slotW) / 2;
                int cy = (getHeight() - slotH) / 2;
                // Slotmachine
                g.drawImage(slotIcon.getImage(), cx, cy, slotW, slotH, this);
                // Hebel
                int leverX = cx + slotW - 10;
                int leverY = cy + slotH/4;
                g.drawImage(leverIcon.getImage(), leverX, leverY, leverW, leverH, this);
            }
        };
        centerPanel.setPreferredSize(new Dimension(slotW + leverW + 40, Math.max(slotH, leverH) + 40));
        centerPanel.setOpaque(false);        // Walzen-Symbole
        JLabel[] reels = new JLabel[3];
        // Blöcke zum Verdecken der ursprünglichen Bilder
        JPanel[] topBlocks = new JPanel[3];
        JPanel[] bottomBlocks = new JPanel[3];
        
        for (int i = 0; i < 3; i++) {
            reels[i] = new JLabel();
            reels[i].setSize(120, 120);
            centerPanel.add(reels[i]);
              // Oberer Block
            topBlocks[i] = new JPanel();
            topBlocks[i].setBackground(java.awt.Color.WHITE);
            topBlocks[i].setSize(120, 60);
            centerPanel.add(topBlocks[i]);
            
            // Unterer Block
            bottomBlocks[i] = new JPanel();
            bottomBlocks[i].setBackground(java.awt.Color.WHITE);
            bottomBlocks[i].setSize(120, 60);
            centerPanel.add(bottomBlocks[i]);
        }        // Initiale Positionierung der Blöcke beim Start
        centerPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Sofort beim Anzeigen die Blöcke positionieren
                int cx = (centerPanel.getWidth() - slotW) / 2;
                int cy = (centerPanel.getHeight() - slotH) / 2;
                for (int i = 0; i < 3; i++) {
                    int x = cx + slotW/4 + i*(slotW/4) - 20;
                    int y = cy + slotH/2 - 60;
                    topBlocks[i].setLocation(x, y - 60);
                    bottomBlocks[i].setLocation(x, y + 120);
                    reels[i].setLocation(x, y);
                }
            }
        });

        JPanel betPanel = new JPanel();
        JLabel betLabel = new JLabel("Bet Amount:");
        final JTextField betField = new JTextField("10", 5);
        betPanel.add(betLabel);
        betPanel.add(betField);
        // Hebel-Button auf dem Hebel positionieren (unsichtbar)
        JButton leverButton = new JButton();
        leverButton.setOpaque(false);
        leverButton.setContentAreaFilled(false);
        leverButton.setBorderPainted(false);
        leverButton.setFocusPainted(false);
        leverButton.setText(""); // Kein Text
        leverButton.setBounds(slotW + 10, slotH/4 + 10, leverW - 20, leverH - 20);        centerPanel.add(leverButton);        // Action: Symbole zufällig setzen
        leverButton.addActionListener(e -> {
            // Button Click Sound
            SoundPlayer.playSound("assets/Button Click.wav");
            
            // Verhindere mehrfaches Klicken während Animation
            if (!leverButton.isEnabled()) return;
            
            // Einsatz prüfen
            try {
                currentBet = Integer.parseInt(betField.getText());
                if (currentBet <= 0) {
                    resultLabel.setText("Invalid bet amount!");
                    return;
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("Please enter a valid number!");
                return;
            }
            if (currentBet > GamblingTycoon.getMoney()) {
                resultLabel.setText("Not enough money!");
                return;
            }
            
            // Hebel deaktivieren während Animation
            leverButton.setEnabled(false);
            resultLabel.setText("SPINNING...");
            
            // Einsatz abziehen
            GamblingTycoon.updateMoney(-currentBet);
            
            // Prüfe Game Over nach dem Einsatz
            if (GamblingTycoon.getMoney() <= 0) {
                GamblingTycoon.showGameOverScreen();
                return;
            }
            
            // Hebel-Sound abspielen
            SoundPlayer.playLeverPull();
            
            // Endgültige Ergebnisse berechnen
            Random rand = new Random();
            int[] finalResults = new int[3];
            for (int i = 0; i < 3; i++) {
                finalResults[i] = rand.nextInt(symbolIcons.length);
            }
              // Spinning-Animation starten
            javax.swing.Timer spinTimer = new javax.swing.Timer(100, null);
            final int[] spinCount = {0};
            final int totalSpins = 30; // 3 Sekunden bei 100ms Intervall
            
            spinTimer.addActionListener(spinEvent -> {
                // Zufällige Symbole während Animation
                for (int i = 0; i < 3; i++) {
                    int randomIdx = rand.nextInt(symbolIcons.length);
                    java.awt.Image scaledImage = symbolIcons[randomIdx].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                    reels[i].setIcon(new ImageIcon(scaledImage));
                }
                
                spinCount[0]++;
                
                // Animation beenden und finale Ergebnisse anzeigen
                if (spinCount[0] >= totalSpins) {
                    spinTimer.stop();
                    
                    // Finale Symbole setzen
                    for (int i = 0; i < 3; i++) {
                        java.awt.Image scaledImage = symbolIcons[finalResults[i]].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                        reels[i].setIcon(new ImageIcon(scaledImage));
                    }
                    
                    // Gewinn berechnen
                    int winnings = calculateWinnings(finalResults);
                    if (winnings > 0) {
                        GamblingTycoon.updateMoney(winnings);
                        // Gewinn-Sound abspielen
                        SoundPlayer.playWin();
                        if (finalResults[0] == finalResults[1] && finalResults[1] == finalResults[2]) {
                            resultLabel.setText("JACKPOT! You win $" + winnings + "!");
                        } else {
                            resultLabel.setText("You win $" + winnings + "!");
                        }
                    } else {
                        resultLabel.setText("Try again!");
                    }
                    
                    // Symbole zentrieren
                    int cx = (centerPanel.getWidth() - slotW) / 2;
                    int cy = (centerPanel.getHeight() - slotH) / 2;
                    for (int i = 0; i < 3; i++) {
                        int x = cx + slotW/4 + i*(slotW/4) - 20;
                        int y = cy + slotH/2 - 60;
                        reels[i].setLocation(x, y);
                    }
                    
                    // Hebel wieder aktivieren
                    leverButton.setEnabled(true);
                }
            });
            
            spinTimer.start();
        });
        // Initiale Positionierung der Symbole
        centerPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cx = (centerPanel.getWidth() - slotW) / 2;
                int cy = (centerPanel.getHeight() - slotH) / 2;                for (int i = 0; i < 3; i++) {
                    int x = cx + slotW/4 + i*(slotW/4) - 20; // Noch weiter rechts
                    int y = cy + slotH/2 - 60;
                    reels[i].setLocation(x, y);
                    
                    // Positionierung der Blöcke
                    topBlocks[i].setLocation(x, y - 60); // Über dem Symbol
                    bottomBlocks[i].setLocation(x, y + 120); // Unter dem Symbol
                }
                leverButton.setBounds(cx + slotW - 10, cy + slotH/4, leverW, leverH);
            }
        });        panel.add(centerPanel, BorderLayout.CENTER);

        // 777 beim Start setzen mit Timer
        javax.swing.Timer initialTimer = new javax.swing.Timer(100, e -> {
            for (int i = 0; i < 3; i++) {
                // 7.png Symbol laden und skalieren
                java.awt.Image scaledImage = symbolIcons[0].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                reels[i].setIcon(new ImageIcon(scaledImage));
            }
            ((javax.swing.Timer)((java.awt.event.ActionEvent)e).getSource()).stop(); // Timer nur einmal ausführen
        });
        initialTimer.start();

        // Bottom panel for betting controls (zentriert und höher)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Spacing für die Positionierung
        bottomPanel.add(Box.createVerticalStrut(50), BorderLayout.NORTH); // Abstand nach oben
        bottomPanel.add(betPanel, BorderLayout.CENTER);
        bottomPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH); // Abstand nach unten
        
        panel.add(bottomPanel, BorderLayout.SOUTH);        // Result Label für Gewinn-Anzeige
        resultLabel = new JLabel("Pull the lever to start!");
        resultLabel.setHorizontalAlignment(JLabel.CENTER);
        bottomPanel.add(resultLabel, BorderLayout.SOUTH);        // Dynamic font resizing for all labels and buttons
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = panel.getHeight();
                int w = panel.getWidth();
                int fontSize = Math.max(14, Math.min(w, h) / 20);
                betLabel.setFont(new Font("Arial", Font.BOLD, fontSize));
                betField.setFont(new Font("Arial", Font.PLAIN, fontSize));
                backButton.setFont(new Font("Arial", Font.PLAIN, Math.max(12, fontSize / 2)));
                resultLabel.setFont(new Font("Arial", Font.BOLD, Math.max(16, fontSize)));
            }
        });
        return panel;
    }

    // Gewinnberechnung
    private static int calculateWinnings(int[] reels) {
        // Drei gleiche
        if (reels[0] == reels[1] && reels[1] == reels[2]) {
            return currentBet * SYMBOL_VALUES[reels[0]];
        }
        // Zwei gleiche
        if (reels[0] == reels[1] || reels[1] == reels[2] || reels[0] == reels[2]) {
            int symbol = reels[0] == reels[1] ? reels[0] : (reels[1] == reels[2] ? reels[1] : reels[0]);
            return currentBet * (SYMBOL_VALUES[symbol] / 10);
        }
        return 0;
    }
}
