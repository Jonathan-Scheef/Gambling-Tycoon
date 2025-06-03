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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gambling Tycoon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            // Money counter at the top right
            moneyLabel = new JLabel("Money: $" + money);
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
            topPanel.add(moneyLabel, BorderLayout.EAST);
            frame.add(topPanel, BorderLayout.NORTH);

            // Main menu buttons
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

            buttonPanel.add(btnRideTheBus);
            buttonPanel.add(btnBlackjack);
            buttonPanel.add(btnSlotMachine);

            frame.add(buttonPanel, BorderLayout.CENTER);

            frame.setVisible(true);
        });
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
