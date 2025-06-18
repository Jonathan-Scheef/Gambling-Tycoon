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

/**
 * SlotMachine - Implementation of a classic casino slot machine game
 * 
 * This class creates a visual slot machine with 3 reels and 9 different symbols.
 * Players can set their bet amount and pull the lever to spin the reels.
 * Winnings are calculated based on matching symbols with different payout rates.
 * The game includes realistic graphics and sound effects for an authentic casino experience.
 */
public class SlotMachine {
    // Available symbols for the slot machine reels
    private static final String[] SYMBOLS = {
        "7.png", "Bar.png", "Diamant.png", "Glocke.png", "Herz.png", 
        "Hufeisen.png", "Kirsche.png", "Wassermelone.png", "Zitrone.png"
    };
    private static final String SLOT_IMAGE = "assets/SlotMachine.png";
    private static final String HEBEL_IMAGE = "assets/Hebel.png";
    
    // Payout multipliers for each symbol (from 7 to Zitrone)
    // Values are balanced to give approximately 95% return to player
    private static final int[] SYMBOL_VALUES = {35, 30, 25, 23, 21, 20, 18, 16, 14};
    private static JLabel resultLabel;        // Displays win results and messages
    private static int currentBet = 10;       // Current bet amount

    /**
     * Creates and returns the main Slot Machine game panel
     * Sets up the visual slot machine with reels, lever, and betting controls
     * 
     * @param onBack Callback function to return to main menu
     * @return JPanel containing the complete Slot Machine game interface
     */
    public static JPanel createSlotMachinePanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel with back button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        panel.add(topPanel, BorderLayout.NORTH);

        // Load slot machine and lever images
        ImageIcon slotIcon = new ImageIcon(SLOT_IMAGE);
        ImageIcon leverIcon = new ImageIcon(HEBEL_IMAGE);
        int slotW = slotIcon.getIconWidth();
        int slotH = slotIcon.getIconHeight();
        int leverW = leverIcon.getIconWidth();
        int leverH = leverIcon.getIconHeight();

        // Load all symbol images for the reels
        ImageIcon[] symbolIcons = new ImageIcon[SYMBOLS.length];
        for (int i = 0; i < SYMBOLS.length; i++) {
            symbolIcons[i] = new ImageIcon("assets/" + SYMBOLS[i]);
        }        // Main slot machine panel with custom painting for background graphics
        JPanel centerPanel = new JPanel(null) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                int cx = (getWidth() - slotW) / 2;
                int cy = (getHeight() - slotH) / 2;
                // Draw the slot machine background image
                g.drawImage(slotIcon.getImage(), cx, cy, slotW, slotH, this);
                // Draw the lever on the right side
                int leverX = cx + slotW - 10;
                int leverY = cy + slotH/4;
                g.drawImage(leverIcon.getImage(), leverX, leverY, leverW, leverH, this);
            }
        };
        centerPanel.setPreferredSize(new Dimension(slotW + leverW + 40, Math.max(slotH, leverH) + 40));
        centerPanel.setOpaque(false);
        
        // Create reel displays and blocking panels to simulate slot machine windows
        JLabel[] reels = new JLabel[3];
        // Blocking panels hide parts of the slot machine background to create reel windows
        JPanel[] topBlocks = new JPanel[3];
        JPanel[] bottomBlocks = new JPanel[3];
        
        for (int i = 0; i < 3; i++) {
            reels[i] = new JLabel();
            reels[i].setSize(120, 120);
            centerPanel.add(reels[i]);
            
            // Top blocking panel covers area above the visible reel window
            topBlocks[i] = new JPanel();
            topBlocks[i].setBackground(java.awt.Color.WHITE);
            topBlocks[i].setSize(120, 60);
            centerPanel.add(topBlocks[i]);
            
            // Bottom blocking panel covers area below the visible reel window
            bottomBlocks[i] = new JPanel();
            bottomBlocks[i].setBackground(java.awt.Color.WHITE);
            bottomBlocks[i].setSize(120, 60);
            centerPanel.add(bottomBlocks[i]);
        }        // Position elements when panel is first shown
        centerPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Calculate positions immediately when panel becomes visible
                int cx = (centerPanel.getWidth() - slotW) / 2;
                int cy = (centerPanel.getHeight() - slotH) / 2;
                for (int i = 0; i < 3; i++) {
                    int x = cx + slotW/4 + i*(slotW/4) - 20;
                    int y = cy + slotH/2 - 60;
                    topBlocks[i].setLocation(x, y - 60);      // Position above reel
                    bottomBlocks[i].setLocation(x, y + 120);  // Position below reel
                    reels[i].setLocation(x, y);               // Position reel in window
                }
            }
        });

        // Betting controls panel
        JPanel betPanel = new JPanel();
        JLabel betLabel = new JLabel("Bet Amount:");
        final JTextField betField = new JTextField("10", 5);
        betPanel.add(betLabel);
        betPanel.add(betField);
        
        // Invisible lever button positioned over the lever graphic
        JButton leverButton = new JButton();
        leverButton.setOpaque(false);           // Transparent background
        leverButton.setContentAreaFilled(false); // No button fill
        leverButton.setBorderPainted(false);    // No button border
        leverButton.setFocusPainted(false);     // No focus indicator
        leverButton.setText("");                // No button text
        leverButton.setBounds(slotW + 10, slotH/4 + 10, leverW - 20, leverH - 20);
        
        centerPanel.add(leverButton);        // Lever button click handler - starts the slot machine spin
        leverButton.addActionListener(e -> {
            // Play button click sound effect
            SoundPlayer.playSound("assets/Button Click.wav");
            
            // Prevent multiple clicks during animation
            if (!leverButton.isEnabled()) return;
            
            // Validate and process bet amount
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
            
            // Deduct bet from player's money
            GamblingTycoon.updateMoney(-currentBet);
            
            // Disable lever during animation to prevent multiple clicks
            leverButton.setEnabled(false);
            resultLabel.setText("SPINNING...");
            
            // Play lever pull sound effect
            SoundPlayer.playLeverPull();
            
            // Calculate final results for the spin
            Random rand = new Random();
            int[] finalResults = new int[3];
            for (int i = 0; i < 3; i++) {
                finalResults[i] = rand.nextInt(symbolIcons.length);
            }
            
            // Start spinning animation
            javax.swing.Timer spinTimer = new javax.swing.Timer(100, null);
            final int[] spinCount = {0};
            final int totalSpins = 30; // 3 seconds of animation at 100ms intervals
            
            spinTimer.addActionListener(spinEvent -> {
                // Show random symbols during animation for spinning effect
                for (int i = 0; i < 3; i++) {
                    int randomIdx = rand.nextInt(symbolIcons.length);
                    java.awt.Image scaledImage = symbolIcons[randomIdx].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                    reels[i].setIcon(new ImageIcon(scaledImage));
                }
                
                spinCount[0]++;
                
                // End animation and show final results
                if (spinCount[0] >= totalSpins) {
                    spinTimer.stop();
                    
                    // Set final symbols
                    for (int i = 0; i < 3; i++) {
                        java.awt.Image scaledImage = symbolIcons[finalResults[i]].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                        reels[i].setIcon(new ImageIcon(scaledImage));
                    }
                    
                    // Calculate and award winnings
                    int winnings = calculateWinnings(finalResults);
                    if (winnings > 0) {
                        GamblingTycoon.updateMoney(winnings);
                        // Play winning sound effect
                        SoundPlayer.playWin();
                        if (finalResults[0] == finalResults[1] && finalResults[1] == finalResults[2]) {
                            resultLabel.setText("JACKPOT! You win $" + winnings + "!");
                        } else {
                            resultLabel.setText("You win $" + winnings + "!");
                        }
                    } else {
                        resultLabel.setText("Try again!");
                    }
                    
                    // Center symbols in their windows
                    int cx = (centerPanel.getWidth() - slotW) / 2;
                    int cy = (centerPanel.getHeight() - slotH) / 2;
                    for (int i = 0; i < 3; i++) {
                        int x = cx + slotW/4 + i*(slotW/4) - 20;
                        int y = cy + slotH/2 - 60;
                        reels[i].setLocation(x, y);
                        
                        // Reposition blocking panels
                        topBlocks[i].setLocation(x, y - 60);    // Above the symbol
                        bottomBlocks[i].setLocation(x, y + 120); // Below the symbol
                    }
                    
                    // Re-enable lever for next spin
                    leverButton.setEnabled(true);
                    
                    // Check for game over after spin result is processed
                    if (GamblingTycoon.getMoney() <= 0) {
                        // Keep UI in consistent state before showing game over
                        GamblingTycoon.showGameOverScreen();
                    }
                }
            });
            
            spinTimer.start();
        });
        
        // Position elements when panel is resized
        centerPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cx = (centerPanel.getWidth() - slotW) / 2;
                int cy = (centerPanel.getHeight() - slotH) / 2;
                
                for (int i = 0; i < 3; i++) {
                    int x = cx + slotW/4 + i*(slotW/4) - 20; // Position reels across slot machine
                    int y = cy + slotH/2 - 60;
                    reels[i].setLocation(x, y);
                    
                    // Position blocking panels
                    topBlocks[i].setLocation(x, y - 60);     // Above symbol
                    bottomBlocks[i].setLocation(x, y + 120); // Below symbol
                }
                leverButton.setBounds(cx + slotW - 10, cy + slotH/4, leverW, leverH);
            }
        });
        
        panel.add(centerPanel, BorderLayout.CENTER);

        // Set initial symbols to "777" when game starts
        javax.swing.Timer initialTimer = new javax.swing.Timer(100, e -> {
            for (int i = 0; i < 3; i++) {
                // Load and scale the "7" symbol image
                java.awt.Image scaledImage = symbolIcons[0].getImage().getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH);
                reels[i].setIcon(new ImageIcon(scaledImage));
            }
            ((javax.swing.Timer)((java.awt.event.ActionEvent)e).getSource()).stop(); // Run timer only once
        });
        initialTimer.start();

        // Bottom panel for betting controls with proper spacing
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Add spacing for better positioning
        bottomPanel.add(Box.createVerticalStrut(50), BorderLayout.NORTH);  // Space above
        bottomPanel.add(betPanel, BorderLayout.CENTER);
        bottomPanel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH); // Space below
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Result label for displaying winnings and messages
        resultLabel = new JLabel("Pull the lever to start!");
        resultLabel.setHorizontalAlignment(JLabel.CENTER);
        bottomPanel.add(resultLabel, BorderLayout.SOUTH);
        
        // Dynamic font resizing system for responsive text
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

    /**
     * Calculates winnings based on the combination of symbols shown on the reels
     * 
     * Payout structure:
     * - Three matching symbols: bet * symbol value (35x for 7s, down to 14x for lemons)
     * - Two matching symbols: bet * (symbol value / 10) for smaller wins
     * - No matches: 0 (player loses bet)
     * 
     * @param reels Array of 3 integers representing the symbol indices on each reel
     * @return Amount won (0 if no winning combination)
     */
    private static int calculateWinnings(int[] reels) {
        // Check for three matching symbols (jackpot)
        if (reels[0] == reels[1] && reels[1] == reels[2]) {
            return currentBet * SYMBOL_VALUES[reels[0]];
        }
        // Check for two matching symbols (smaller win)
        if (reels[0] == reels[1] || reels[1] == reels[2] || reels[0] == reels[2]) {
            int symbol = reels[0] == reels[1] ? reels[0] : (reels[1] == reels[2] ? reels[1] : reels[0]);
            return currentBet * (SYMBOL_VALUES[symbol] / 10);  // 10% of full payout for two matches
        }
        return 0;  // No winning combination
    }
}
