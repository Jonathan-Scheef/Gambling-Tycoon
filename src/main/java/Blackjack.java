import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Blackjack {
    /**
     * Returns a simple outline panel for the Blackjack game.
     * @param onBack Runnable to call when the user wants to go back to the menu
     */
    public static JPanel createBlackjackPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        // Top panel with back button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        panel.add(topPanel, BorderLayout.NORTH);

        // Center panel for game info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JLabel playerLabel = new JLabel("Player: [cards]");
        JLabel dealerLabel = new JLabel("Dealer: [cards]");
        JLabel statusLabel = new JLabel("Status: Place your bet and start!");
        centerPanel.add(playerLabel);
        centerPanel.add(dealerLabel);
        centerPanel.add(statusLabel);
        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel for actions
        JPanel bottomPanel = new JPanel();
        JButton hitButton = new JButton("Hit");
        JButton standButton = new JButton("Stand");
        JButton betButton = new JButton("Bet");
        bottomPanel.add(hitButton);
        bottomPanel.add(standButton);
        bottomPanel.add(betButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Dynamic font resizing for all labels and buttons
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int h = panel.getHeight();
                int w = panel.getWidth();
                int fontSize = Math.max(14, Math.min(w, h) / 20);
                Font f = new Font("Arial", Font.BOLD, fontSize);
                playerLabel.setFont(f);
                dealerLabel.setFont(f);
                statusLabel.setFont(f);
                hitButton.setFont(f);
                standButton.setFont(f);
                betButton.setFont(f);
                backButton.setFont(new Font("Arial", Font.PLAIN, Math.max(12, fontSize / 2)));
            }
        });
        return panel;
    }
}
