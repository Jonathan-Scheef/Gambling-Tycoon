import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SlotMachine {
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

        // Center panel for slot machine graphics (placeholder)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        JLabel slotLabel = new JLabel("[Slot Machine Graphic Here]");
        slotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton leverButton = new JButton("Pull Lever");
        leverButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(slotLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(leverButton);
        centerPanel.add(Box.createVerticalGlue());
        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom panel for betting controls
        JPanel bottomPanel = new JPanel();
        JLabel betLabel = new JLabel("Bet Amount:");
        JTextField betField = new JTextField("10", 5);
        JButton betButton = new JButton("Place Bet");
        bottomPanel.add(betLabel);
        bottomPanel.add(betField);
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
                slotLabel.setFont(f);
                leverButton.setFont(f);
                betLabel.setFont(f);
                betField.setFont(f);
                betButton.setFont(f);
                backButton.setFont(new Font("Arial", Font.PLAIN, Math.max(12, fontSize / 2)));
            }
        });
        return panel;
    }
}
