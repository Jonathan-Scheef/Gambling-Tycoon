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
    private static JPanel topPanel; // Top-Panel als statische Variable

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Gambling Tycoon");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true); // Kein Rahmen
            java.awt.GraphicsDevice gd = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                frame.setResizable(false);
                gd.setFullScreenWindow(frame);
            } else {
                frame.setSize(1920, 1080);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            frame.setLayout(new BorderLayout());

            // Money counter am oberen Rand
            moneyLabel = new JLabel("Money: $" + money);
            moneyLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 48));
            topPanel = new JPanel(new BorderLayout());
            topPanel.add(Box.createHorizontalGlue(), BorderLayout.CENTER);
            topPanel.add(moneyLabel, BorderLayout.EAST);
            frame.add(topPanel, BorderLayout.NORTH);

            // Main menu panel
            mainMenuPanel = createMainMenuPanel();
            frame.add(mainMenuPanel, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }

    private static void addEscapeKeyAction(JPanel panel, Runnable action) {
        panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(javax.swing.KeyStroke.getKeyStroke("ESCAPE"), "escapeAction");
        panel.getActionMap().put("escapeAction", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                action.run();
            }
        });
    }

    private static JPanel createMainMenuPanel() {
        JPanel buttonPanel = new JPanel(null) {
            @Override
            public void doLayout() {
                int n = getComponentCount();
                int width = getWidth();
                int height = getHeight();
                int bottomPadding = (int) (height * 0.10); // 10% Padding unten
                int availableHeight = height - bottomPadding;
                int buttonHeight = availableHeight / (n * 2);
                int buttonWidth = (int) (width * 0.7);
                int y = availableHeight / (n + 1) - buttonHeight / 2;
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
        addEscapeKeyAction(buttonPanel, () -> System.exit(0));
        return buttonPanel;
    }

    private static JPanel createGamePanel(String gameName) {
        if (gameName.equals("Blackjack")) {
            return Blackjack.createBlackjackPanel(() -> showMainMenuPanel());
        }
        if (gameName.equals("Slot Machine")) {
            return SlotMachine.createSlotMachinePanel(() -> showMainMenuPanel());
        }
        JLabel label = new JLabel(gameName + " (Game UI goes here)", JLabel.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);
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

    private static void setCenterPanel(JPanel panel) {
        java.awt.Container contentPane = frame.getContentPane();
        if (contentPane.getComponentCount() > 1) {
            // Entferne das aktuelle Center-Panel (Index 1, da Index 0 das Top-Panel ist)
            contentPane.remove(1);
        }
        contentPane.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void showMainMenuPanel() {
        setCenterPanel(mainMenuPanel);
    }

    private static void showBlackjackPanel() {
        if (blackjackPanel == null) {
            blackjackPanel = createBlackjackPanel();
            addEscapeKeyAction(blackjackPanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(blackjackPanel);
    }

    private static void showRideTheBusPanel() {
        if (rideTheBusPanel == null) {
            rideTheBusPanel = createRideTheBusPanel();
            addEscapeKeyAction(rideTheBusPanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(rideTheBusPanel);
    }

    private static void showSlotMachinePanel() {
        if (slotMachinePanel == null) {
            slotMachinePanel = createSlotMachinePanel();
            addEscapeKeyAction(slotMachinePanel, GamblingTycoon::showMainMenuPanel);
        }
        setCenterPanel(slotMachinePanel);
    }

    private static JPanel createBlackjackPanel() {
        return Blackjack.createBlackjackPanel(GamblingTycoon::showMainMenuPanel);
    }

    private static JPanel createSlotMachinePanel() {
        return SlotMachine.createSlotMachinePanel(GamblingTycoon::showMainMenuPanel);
    }

    private static JPanel createRideTheBusPanel() {
        // Platzhalter-Panel mit Escape-Key, da RideTheBus noch nicht implementiert ist
        JPanel panel = new JPanel(new java.awt.BorderLayout());
        javax.swing.JLabel label = new javax.swing.JLabel("Ride The Bus (noch nicht implementiert)", javax.swing.SwingConstants.CENTER);
        panel.add(label, java.awt.BorderLayout.CENTER);
        addEscapeKeyAction(panel, GamblingTycoon::showMainMenuPanel);
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
