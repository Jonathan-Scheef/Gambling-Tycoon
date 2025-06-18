import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * GamblingTycoon - Main class for the Gambling Tycoon casino game application
 * 
 * This class manages the main application window and navigation between different games.
 * It creates a fullscreen application with three gambling games: Blackjack, Ride the Bus, and Slot Machine.
 * The application tracks player money across all games and shows a game over screen when funds are depleted.
 */
public class GamblingTycoon {
    // Player's current money amount - starts at $1000
    private static int money = 1000;
    
    // UI components that need to be accessed from multiple methods
    private static JLabel moneyLabel;          // Displays current money amount
    private static JFrame frame;               // Main application window
    private static JPanel mainMenuPanel;      // Main menu with game selection buttons
    private static JPanel blackjackPanel;     // Blackjack game panel
    private static JPanel rideTheBusPanel;    // Ride the Bus card game panel
    private static JPanel slotMachinePanel;   // Slot machine game panel
    private static JPanel topPanel;           // Top panel containing money display

    /**
     * Main entry point of the application
     * Sets up the fullscreen window and initializes the UI components
     */
    public static void main(String[] args) {        
        SwingUtilities.invokeLater(() -> {
            // Create main application window
            frame = new JFrame("Gambling Tycoon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Configure window for fullscreen operation without decorations
            frame.setUndecorated(true);           // Remove window border and title bar
            frame.setAlwaysOnTop(true);           // Keep application on top of other windows
            frame.setAutoRequestFocus(true);      // Automatically request focus when shown
            frame.setFocusableWindowState(true);  // Allow window to receive focus
            
            // Set up fullscreen display
            java.awt.GraphicsDevice gd = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                frame.setResizable(false);
                gd.setFullScreenWindow(frame);
            } else {
                // Fallback for systems that don't support fullscreen
                frame.setSize(1920, 1080);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            frame.setLayout(new BorderLayout());

            // Create and position the money counter at the top of the screen
            moneyLabel = new JLabel("Money: $" + money);
            moneyLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
            topPanel = new JPanel(new BorderLayout());
            topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);  // Push money display to right
            topPanel.add(moneyLabel, BorderLayout.EAST);
            frame.add(topPanel, BorderLayout.NORTH);
            
            // Create and display the main menu
            mainMenuPanel = createMainMenuPanel();
            frame.add(mainMenuPanel, BorderLayout.CENTER);
            frame.setVisible(true);
            
            // Ensure window stays in focus and on top
            frame.toFront();
            frame.requestFocus();
            
            // Timer to periodically ensure the window remains in focus
            // This prevents other applications from stealing focus
            javax.swing.Timer stayOnTopTimer = new javax.swing.Timer(1000, e -> {
                if (!frame.isActive()) {
                    frame.toFront();
                    frame.requestFocus();
                }
            });
            stayOnTopTimer.start();
        });
    }

    /**
     * Adds ESC key functionality to any panel to perform a specified action
     * This allows users to quickly navigate back or exit using the ESC key
     * 
     * @param panel The panel to add the ESC key binding to
     * @param action The action to perform when ESC is pressed
     */
    private static void addEscapeKeyAction(JPanel panel, Runnable action) {
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "escapeAction");
        panel.getActionMap().put("escapeAction", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    /**
     * Creates the main menu panel with game selection buttons
     * Uses custom layout to center and properly size buttons regardless of screen resolution
     * 
     * @return JPanel containing the main menu interface
     */
    private static JPanel createMainMenuPanel() {
        // Custom panel with null layout for precise button positioning
        JPanel buttonPanel = new JPanel(null) {
            @Override
            public void doLayout() {
                // Calculate button dimensions and positions based on panel size
                int n = getComponentCount();                    // Number of buttons
                int width = getWidth();                         // Panel width
                int height = getHeight();                       // Panel height
                int bottomPadding = (int) (height * 0.10);     // 10% padding at bottom
                int availableHeight = height - bottomPadding;   // Height available for buttons
                int buttonHeight = availableHeight / (n * 2);  // Height per button (with spacing)
                int buttonWidth = (int) (width * 0.7);         // Button width (70% of panel width)
                int y = availableHeight / (n + 1) - buttonHeight / 2;  // Starting Y position
                
                // Position each button with equal spacing
                for (int i = 0; i < n; i++) {
                    getComponent(i).setBounds((width - buttonWidth) / 2, y, buttonWidth, buttonHeight);
                    y += buttonHeight * 2;  // Double spacing between buttons
                }
            }
        };

        // Create game selection buttons
        JButton btnRideTheBus = new JButton("Ride the Bus");
        JButton btnBlackjack = new JButton("Blackjack");
        JButton btnSlotMachine = new JButton("Slot Machine");

        // Add button click handlers to navigate to respective games
        btnRideTheBus.addActionListener(e -> showRideTheBusPanel());
        btnBlackjack.addActionListener(e -> showBlackjackPanel());
        btnSlotMachine.addActionListener(e -> showSlotMachinePanel());
        // Add buttons to the panel
        buttonPanel.add(btnRideTheBus);
        buttonPanel.add(btnBlackjack);
        buttonPanel.add(btnSlotMachine);

        // Dynamic font resizing system - adjusts button text size based on screen resolution
        buttonPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = buttonPanel.getHeight();
                int w = buttonPanel.getWidth();
                // Calculate optimal font size based on panel dimensions
                int fontSize = Math.max(14, Math.min(w, h) / 15);
                java.awt.Font f = new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize);
                // Apply font to all buttons
                btnRideTheBus.setFont(f);
                btnBlackjack.setFont(f);
                btnSlotMachine.setFont(f);
            }
        });
        
        // ESC key exits the application from main menu
        addEscapeKeyAction(buttonPanel, () -> System.exit(0));
        
        return buttonPanel;
    }

    /**
     * Switches the center panel of the main window to display a different game or menu
     * Removes the current center panel and replaces it with the specified panel
     * 
     * @param panel The new panel to display in the center of the window
     */
    private static void setCenterPanel(JPanel panel) {
        java.awt.Container contentPane = frame.getContentPane();
        if (contentPane.getComponentCount() > 1) {
            // Remove the current center panel (index 1, since index 0 is the top panel)
            contentPane.remove(1);
        }
        contentPane.add(panel, BorderLayout.CENTER);
        frame.revalidate();  // Refresh layout
        frame.repaint();     // Redraw components
    }

    /**
     * Returns to the main menu by setting it as the center panel
     */
    private static void showMainMenuPanel() {
        setCenterPanel(mainMenuPanel);
    }

    /**
     * Shows the Blackjack game panel
     * Creates the panel if it doesn't exist and adds ESC key functionality
     */
    private static void showBlackjackPanel() {
        if (blackjackPanel == null) {
            blackjackPanel = createBlackjackPanel();
            // ESC key returns to main menu from Blackjack
            addEscapeKeyAction(blackjackPanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(blackjackPanel);
    }

    /**
     * Shows the Ride the Bus game panel
     * Creates the panel if it doesn't exist and adds ESC key functionality
     */
    private static void showRideTheBusPanel() {
        if (rideTheBusPanel == null) {
            rideTheBusPanel = createRideTheBusPanel();
            // ESC key returns to main menu from Ride the Bus
            addEscapeKeyAction(rideTheBusPanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(rideTheBusPanel);
    }

    /**
     * Shows the Slot Machine game panel
     * Creates the panel if it doesn't exist and adds ESC key functionality
     */
    private static void showSlotMachinePanel() {
        if (slotMachinePanel == null) {
            slotMachinePanel = createSlotMachinePanel();
            // ESC key returns to main menu from Slot Machine
            addEscapeKeyAction(slotMachinePanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(slotMachinePanel);
    }

    /**
     * Creates and returns the Blackjack game panel
     * Delegates to the Blackjack class for actual panel creation
     * 
     * @return JPanel containing the Blackjack game interface
     */
    private static JPanel createBlackjackPanel() {
        return Blackjack.createBlackjackPanel(GamblingTycoon::showMainMenuPanel);
    }

    /**
     * Creates and returns the Slot Machine game panel
     * Delegates to the SlotMachine class for actual panel creation
     * 
     * @return JPanel containing the Slot Machine game interface
     */
    private static JPanel createSlotMachinePanel() {
        return SlotMachine.createSlotMachinePanel(GamblingTycoon::showMainMenuPanel);
    }
    
    /**
     * Creates and returns the Ride the Bus game panel
     * Delegates to the RideTheBus class for actual panel creation
     * 
     * @return JPanel containing the Ride the Bus game interface
     */
    private static JPanel createRideTheBusPanel() {
        return RideTheBus.createRideTheBusPanel(GamblingTycoon::showMainMenuPanel);
    }

    /**
     * Updates the player's money amount and refreshes the display
     * This method is called by game classes when the player wins or loses money
     * If money reaches zero or below, triggers the game over screen
     * 
     * @param amount The amount to add to (positive) or subtract from (negative) current money
     */
    public static void updateMoney(int amount) {
        money += amount;
        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + money);
        }
        
        // Check for game over condition
        if (money <= 0) {
            money = 0;  // Ensure money doesn't go negative
            moneyLabel.setText("Money: $0");
            showGameOverScreen();
        }
    }

    /**
     * Returns the current amount of money the player has
     * Used by game classes to check if player can afford bets
     * 
     * @return Current money amount
     */
    public static int getMoney() {
        return money;
    }

    /**
     * Displays the game over screen when player runs out of money
     * Shows a dramatic game over message with option to restart the game
     */
    public static void showGameOverScreen() {
        JPanel gameOverPanel = new JPanel(new BorderLayout());
        
        // Create game over message with large red text
        JLabel gameOverLabel = new JLabel("GAME OVER", JLabel.CENTER);
        gameOverLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 72));
        gameOverLabel.setForeground(java.awt.Color.RED);
        gameOverLabel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        // Additional message explaining why game ended
        JLabel messageLabel = new JLabel("You ran out of money!", JLabel.CENTER);
        messageLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 36));
        messageLabel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        // Center panel to hold all game over elements with vertical layout
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(Box.createVerticalGlue());  // Push content to center
        centerPanel.add(gameOverLabel);
        centerPanel.add(Box.createVerticalStrut(20));  // Spacing
        centerPanel.add(messageLabel);
        centerPanel.add(Box.createVerticalStrut(40));  // More spacing before button        // Create "Play Again" button to restart the game
        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
        playAgainButton.addActionListener(e -> resetGame());
        
        // Panel to center the button with absolute positioning
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);  // Absolute positioning for precise control
        buttonPanel.add(playAgainButton);
        buttonPanel.setPreferredSize(new Dimension(300, 70));
        buttonPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        // Component listener to position button when panel is resized
        buttonPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                // Center the button within its panel
                int panelWidth = buttonPanel.getWidth();
                int buttonWidth = 180;  // Fixed button width for consistency
                int x = (panelWidth - buttonWidth) / 2;
                playAgainButton.setBounds(x, 15, buttonWidth, 50);
            }
        });
        
        // Add button to center panel
        centerPanel.add(buttonPanel);
        centerPanel.add(Box.createVerticalGlue());  // Push content to center
        
        // Add center panel to game over screen
        gameOverPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ESC key also restarts the game from game over screen
        addEscapeKeyAction(gameOverPanel, () -> resetGame());
        
        // Replace current panel with game over screen
        frame.getContentPane().remove(frame.getContentPane().getComponent(1)); // Remove current panel
        frame.add(gameOverPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
    /**
     * Resets the game to its initial state
     * Restores starting money amount and returns to main menu
     */
    public static void resetGame() {
        money = 1000;  // Reset to starting money amount
        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + money);
        }
        showMainMenu();
    }
    
    /**
     * Returns to the main menu by clearing all panels and recreating the basic layout
     * Used when restarting the game or returning from game over screen
     */
    public static void showMainMenu() {
        frame.getContentPane().removeAll();
        frame.add(topPanel, BorderLayout.NORTH);      // Money counter at top
        frame.add(mainMenuPanel, BorderLayout.CENTER); // Main menu in center
        frame.revalidate();
        frame.repaint();
    }
}
