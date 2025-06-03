import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GamblingTycoon {
    private static int money = 1000; // Starting money
    private static JLabel moneyLabel;
    private static JFrame frame;
    private static JPanel mainMenuPanel;
    private static JPanel blackjackPanel;
    private static JPanel rideTheBusPanel;
    private static JPanel slotMachinePanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Gambling Tycoon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            // Money counter at the top right
            moneyLabel = new JLabel("Money: $" + money);
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
            topPanel.add(moneyLabel, BorderLayout.EAST);
            frame.add(topPanel, BorderLayout.NORTH);

            // Main menu panel
            mainMenuPanel = createMainMenuPanel();
            frame.add(mainMenuPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }

    private static JPanel createMainMenuPanel() {
        JPanel buttonPanel = new JPanel(null) {
            @Override
            public void doLayout() {
                int n = getComponentCount();
                int width = getWidth();
                int height = getHeight();
                int buttonHeight = height / (n * 2);
                int buttonWidth = (int) (width * 0.7);
                int y = height / (n + 1) - buttonHeight / 2;
                for (int i = 0; i < n; i++) {
                    getComponent(i).setBounds((width - buttonWidth) / 2, y, buttonWidth, buttonHeight);
                    y += buttonHeight * 2;
                }
            }
        };

        JButton btnRideTheBus = new JButton("Ride the Bus");
        JButton btnBlackjack = new JButton("Blackjack");
        JButton btnSlotMachine = new JButton("Slot Machine");

        btnRideTheBus.addActionListener(e -> showRideTheBusPanel());
        btnBlackjack.addActionListener(e -> showBlackjackPanel());
        btnSlotMachine.addActionListener(e -> showSlotMachinePanel());

        buttonPanel.add(btnRideTheBus);
        buttonPanel.add(btnBlackjack);
        buttonPanel.add(btnSlotMachine);

        // Dynamic font resizing for all buttons
        buttonPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = buttonPanel.getHeight();
                int w = buttonPanel.getWidth();
                int fontSize = Math.max(14, Math.min(w, h) / 15);
                java.awt.Font f = new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize);
                btnRideTheBus.setFont(f);
                btnBlackjack.setFont(f);
                btnSlotMachine.setFont(f);
            }
        });
        return buttonPanel;
    }

    private static JPanel createGamePanel(String gameName) {
        JPanel panel = new JPanel(new BorderLayout());
        // Proportional label
        JLabel label = new JLabel(gameName + " (Game UI goes here)", JLabel.CENTER);
        panel.add(label, BorderLayout.CENTER);
        // Back to Menu button at the top left, proportional size
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> showMainMenuPanel());
        topPanel.add(backButton, BorderLayout.WEST);
        panel.add(topPanel, BorderLayout.NORTH);
        // Dynamic font resizing for label and button
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = panel.getHeight();
                int w = panel.getWidth();
                int fontSize = Math.max(14, Math.min(w, h) / 15);
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize));
                backButton.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, Math.max(12, fontSize / 2)));
            }
        });
        return panel;
    }

    private static void showMainMenuPanel() {
        frame.getContentPane().removeAll();
        // Money counter at the top right
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        topPanel.add(moneyLabel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(mainMenuPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void showBlackjackPanel() {
        if (blackjackPanel == null) blackjackPanel = createGamePanel("Blackjack");
        showGamePanel(blackjackPanel);
    }
    private static void showRideTheBusPanel() {
        if (rideTheBusPanel == null) rideTheBusPanel = createGamePanel("Ride the Bus");
        showGamePanel(rideTheBusPanel);
    }
    private static void showSlotMachinePanel() {
        if (slotMachinePanel == null) slotMachinePanel = createGamePanel("Slot Machine");
        showGamePanel(slotMachinePanel);
    }
    private static void showGamePanel(JPanel gamePanel) {
        frame.getContentPane().removeAll();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
        topPanel.add(moneyLabel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(gamePanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Call this method to update the money counter from anywhere
    public static void updateMoney(int amount) {
        money += amount;
        if (moneyLabel != null) {
            moneyLabel.setText("Money: $" + money);
        }
    }

    // Optionally, a method to get current money
    public static int getMoney() {
        return money;
    }
}
