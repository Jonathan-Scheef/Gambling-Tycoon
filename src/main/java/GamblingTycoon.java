import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

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
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));

        JButton btnRideTheBus = new JButton("Ride the Bus");
        JButton btnBlackjack = new JButton("Blackjack");
        JButton btnSlotMachine = new JButton("Slot Machine");

        Font bigFont = new Font("Arial", Font.BOLD, 20);
        btnRideTheBus.setFont(bigFont);
        btnBlackjack.setFont(bigFont);
        btnSlotMachine.setFont(bigFont);
        btnRideTheBus.setPreferredSize(new Dimension(250, 50));
        btnBlackjack.setPreferredSize(new Dimension(250, 50));
        btnSlotMachine.setPreferredSize(new Dimension(250, 50));

        btnRideTheBus.addActionListener(e -> showRideTheBusPanel());
        btnBlackjack.addActionListener(e -> showBlackjackPanel());
        btnSlotMachine.addActionListener(e -> showSlotMachinePanel());

        buttonPanel.add(btnRideTheBus);
        buttonPanel.add(btnBlackjack);
        buttonPanel.add(btnSlotMachine);
        return buttonPanel;
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
    private static JPanel createGamePanel(String gameName) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(gameName + " (Game UI goes here)", JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> showMainMenuPanel());
        panel.add(backButton, BorderLayout.SOUTH);
        return panel;
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
