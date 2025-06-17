import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Blackjack {
    // Karten-Datenstrukturen
    private static class Card {
        public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
        public enum Rank { ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }
        
        private Suit suit;
        private Rank rank;
        
        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }        public int getValue() {
            switch(rank) {
                case ACE: return 11; // Ace kann 1 oder 11 sein
                case TWO: return 2;
                case THREE: return 3;
                case FOUR: return 4;
                case FIVE: return 5;
                case SIX: return 6;
                case SEVEN: return 7;
                case EIGHT: return 8;
                case NINE: return 9;
                case TEN: return 10;
                case JACK: return 10;
                case QUEEN: return 10;
                case KING: return 10;
                default: return 0;
            }
        }
        
        public String getDisplayName() {
            String rankStr = "";
            switch(rank) {
                case ACE: rankStr = "A"; break;
                case JACK: rankStr = "J"; break;
                case QUEEN: rankStr = "Q"; break;
                case KING: rankStr = "K"; break;
                default: rankStr = String.valueOf(rank.ordinal() + 1);
            }
            
            String suitStr = "";
            switch(suit) {
                case HEARTS: suitStr = "♥"; break;
                case DIAMONDS: suitStr = "♦"; break;
                case CLUBS: suitStr = "♣"; break;
                case SPADES: suitStr = "♠"; break;
            }
            
            return rankStr + suitStr;
        }
        
        public Color getColor() {
            return (suit == Suit.HEARTS || suit == Suit.DIAMONDS) ? Color.RED : Color.BLACK;
        }
    }
    
    // Spielzustand
    private static ArrayList<Card> deck;
    private static ArrayList<Card> playerHand;
    private static ArrayList<Card> dealerHand;
    private static int currentBet = 10;
    private static boolean gameActive = false;
    private static boolean dealerTurn = false;
    
    // UI-Komponenten
    private static JLabel playerScoreLabel;
    private static JLabel dealerScoreLabel;
    private static JLabel resultLabel;
    private static JLabel betLabel;
    private static JTextField betField;
    private static JPanel playerCardsPanel;
    private static JPanel dealerCardsPanel;
    private static JButton hitButton;
    private static JButton standButton;
    private static JButton doubleButton;
    private static JButton dealButton;
    
    public static JPanel createBlackjackPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel mit Back-Button
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
          // Game Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        betLabel = new JLabel("Bet: $10");
        betLabel.setFont(new Font("Arial", Font.BOLD, 32));
        betField = new JTextField("10", 8);
        betField.setFont(new Font("Arial", Font.PLAIN, 28));
        JLabel betTextLabel = new JLabel("Bet Amount:");
        betTextLabel.setFont(new Font("Arial", Font.BOLD, 28));
        infoPanel.add(betTextLabel);
        infoPanel.add(betField);
        infoPanel.add(betLabel);
        topPanel.add(infoPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Haupt-Spielbereich
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(0, 100, 0)); // Dunkelgrün wie Filz        // Dealer-Bereich
        JPanel dealerPanel = new JPanel(new BorderLayout());
        dealerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3), 
            "DEALER", 
            javax.swing.border.TitledBorder.CENTER, 
            javax.swing.border.TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 36), 
            Color.WHITE));
        dealerPanel.setOpaque(false);
        dealerScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        dealerScoreLabel.setFont(new Font("Arial", Font.BOLD, 42));
        dealerScoreLabel.setForeground(Color.WHITE);
        dealerCardsPanel = new JPanel(new FlowLayout());
        dealerCardsPanel.setOpaque(false);
        dealerPanel.add(dealerScoreLabel, BorderLayout.NORTH);
        dealerPanel.add(dealerCardsPanel, BorderLayout.CENTER);
        
        // Player-Bereich
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3), 
            "PLAYER", 
            javax.swing.border.TitledBorder.CENTER, 
            javax.swing.border.TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 36), 
            Color.WHITE));
        playerPanel.setOpaque(false);
        playerScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 42));
        playerScoreLabel.setForeground(Color.WHITE);
        playerCardsPanel = new JPanel(new FlowLayout());
        playerCardsPanel.setOpaque(false);
        playerPanel.add(playerScoreLabel, BorderLayout.NORTH);
        playerPanel.add(playerCardsPanel, BorderLayout.CENTER);
        
        gamePanel.add(dealerPanel, BorderLayout.NORTH);
        gamePanel.add(playerPanel, BorderLayout.SOUTH);
        
        // Result Label in der Mitte
        resultLabel = new JLabel("Press Deal to start!", JLabel.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 48));
        resultLabel.setForeground(Color.YELLOW);
        gamePanel.add(resultLabel, BorderLayout.CENTER);
        
        panel.add(gamePanel, BorderLayout.CENTER);
          // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        dealButton = new JButton("Deal");
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        doubleButton = new JButton("Double Down");
          // Button-Größe und Font setzen
        Font buttonFont = new Font("Arial", Font.BOLD, 28);
        Dimension buttonSize = new Dimension(200, 70);
        
        dealButton.setFont(buttonFont);
        dealButton.setPreferredSize(buttonSize);
        hitButton.setFont(buttonFont);
        hitButton.setPreferredSize(buttonSize);
        standButton.setFont(buttonFont);
        standButton.setPreferredSize(buttonSize);
        doubleButton.setFont(buttonFont);
        doubleButton.setPreferredSize(buttonSize);
        
        // Button-Aktionen
        dealButton.addActionListener(e -> startNewGame());
        hitButton.addActionListener(e -> playerHit());
        standButton.addActionListener(e -> playerStand());
        doubleButton.addActionListener(e -> playerDouble());
        
        buttonPanel.add(dealButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(doubleButton);
        
        // Buttons initial deaktivieren
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialisierung
        initializeDeck();
        
        return panel;
    }
    
    private static void initializeDeck() {
        deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
    }
    
    private static void startNewGame() {
        try {
            currentBet = Integer.parseInt(betField.getText());
            if (currentBet <= 0) {
                resultLabel.setText("Invalid bet amount!");
                return;
            }
            if (currentBet > GamblingTycoon.getMoney()) {
                resultLabel.setText("Not enough money!");
                return;
            }
        } catch (NumberFormatException ex) {
            resultLabel.setText("Please enter a valid number!");
            return;
        }
        
        // Einsatz abziehen
        GamblingTycoon.updateMoney(-currentBet);
        betLabel.setText("Bet: $" + currentBet);        // Deck neu mischen wenn weniger als 10 Karten oder leer
        if (deck == null || deck.size() < 10) {
            showShufflingMessage();
            initializeDeck();
        }
          // Hände leeren und neu initialisieren
        if (playerHand == null) {
            playerHand = new ArrayList<>();
        } else {
            playerHand.clear();
        }
        if (dealerHand == null) {
            dealerHand = new ArrayList<>();
        } else {
            dealerHand.clear();
        }        // Karten austeilen (mit Sicherheitsprüfung)
        if (deck.size() < 4) {
            showShufflingMessage();
            initializeDeck();
        }
        
        playerHand.add(deck.remove(deck.size() - 1));
        dealerHand.add(deck.remove(deck.size() - 1));
        playerHand.add(deck.remove(deck.size() - 1));
        dealerHand.add(deck.remove(deck.size() - 1));
        
        gameActive = true;
        dealerTurn = false;
        
        updateDisplay();
        
        // Buttons aktivieren/deaktivieren
        dealButton.setEnabled(false);
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        doubleButton.setEnabled(true);
        
        // Prüfe auf Blackjack
        if (getHandValue(playerHand) == 21) {
            if (getHandValue(dealerHand) == 21) {
                endGame("Push! Both have Blackjack!");
                GamblingTycoon.updateMoney(currentBet); // Einsatz zurück
            } else {
                endGame("Blackjack! You win!");
                GamblingTycoon.updateMoney((int)(currentBet * 2.5)); // 2.5x Auszahlung für Blackjack
            }
        }
        
        resultLabel.setText("Your turn!");
    }
      private static void playerHit() {
        if (!gameActive || dealerTurn) return;        // Deck-Check vor Hit
        if (deck.size() < 1) {
            showShufflingMessage();
            initializeDeck();
        }
        
        playerHand.add(deck.remove(deck.size() - 1));
        updateDisplay();
        
        int playerScore = getHandValue(playerHand);
        if (playerScore > 21) {
            endGame("Bust! You lose!");
        } else if (playerScore == 21) {
            playerStand(); // Automatisch stehen bei 21
        }
        
        // Double Down nach Hit deaktivieren
        doubleButton.setEnabled(false);
    }
    
    private static void playerStand() {
        if (!gameActive) return;
        
        dealerTurn = true;
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);        // Dealer spielt
        javax.swing.Timer dealerTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getHandValue(dealerHand) < 17) {                    // Deck-Check vor Dealer-Hit
                    if (deck.size() < 1) {
                        showShufflingMessage();
                        initializeDeck();
                    }
                    dealerHand.add(deck.remove(deck.size() - 1));
                    updateDisplay();
                } else {
                    ((javax.swing.Timer)e.getSource()).stop();
                    
                    // Gewinner bestimmen
                    int playerScore = getHandValue(playerHand);
                    int dealerScore = getHandValue(dealerHand);
                    
                    if (dealerScore > 21) {
                        endGame("Dealer busts! You win!");
                        GamblingTycoon.updateMoney(currentBet * 2);
                    } else if (playerScore > dealerScore) {
                        endGame("You win!");
                        GamblingTycoon.updateMoney(currentBet * 2);
                    } else if (dealerScore > playerScore) {
                        endGame("Dealer wins!");
                    } else {
                        endGame("Push! It's a tie!");
                        GamblingTycoon.updateMoney(currentBet); // Einsatz zurück
                    }
                }
            }
        });
        dealerTimer.start();
    }
    
    private static void playerDouble() {
        if (!gameActive || dealerTurn || playerHand.size() > 2) return;
        
        // Prüfe ob genug Geld für Double Down
        if (currentBet > GamblingTycoon.getMoney()) {
            resultLabel.setText("Not enough money to double down!");
            return;
        }
        
        // Einsatz verdoppeln
        GamblingTycoon.updateMoney(-currentBet);
        currentBet *= 2;
        betLabel.setText("Bet: $" + currentBet);
        
        // Eine Karte ziehen und dann stehen
        playerHand.add(deck.remove(deck.size() - 1));
        updateDisplay();
        
        if (getHandValue(playerHand) > 21) {
            endGame("Bust! You lose!");
        } else {
            playerStand();
        }
    }
      private static void endGame(String message) {
        gameActive = false;
        dealerTurn = false; // Dealer-Turn explizit zurücksetzen
        resultLabel.setText(message);
        
        // Buttons zurücksetzen
        dealButton.setEnabled(true);
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        
        // Display aktualisieren um sicherzustellen, dass alle Karten sichtbar sind
        updateDisplay();
        
        // Game Over prüfen
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    private static int getHandValue(ArrayList<Card> hand) {
        int value = 0;
        int aces = 0;
        
        for (Card card : hand) {
            if (card.rank == Card.Rank.ACE) {
                aces++;
            }
            value += card.getValue();
        }
        
        // Asse von 11 auf 1 reduzieren wenn nötig
        while (value > 21 && aces > 0) {
            value -= 10;
            aces--;
        }
        
        return value;
    }
      private static void updateDisplay() {
        // Player-Karten anzeigen
        playerCardsPanel.removeAll();
        if (playerHand != null) {
            for (Card card : playerHand) {
                JLabel cardLabel = createCardLabel(card);
                playerCardsPanel.add(cardLabel);
            }
            playerScoreLabel.setText("Score: " + getHandValue(playerHand));
        } else {
            playerScoreLabel.setText("Score: 0");
        }
        
        // Dealer-Karten anzeigen
        dealerCardsPanel.removeAll();
        if (dealerHand != null && dealerHand.size() > 0) {
            for (int i = 0; i < dealerHand.size(); i++) {
                JLabel cardLabel;
                // Zweite Karte nur verdeckt wenn Spiel aktiv und noch nicht Dealer-Turn
                if (i == 1 && gameActive && !dealerTurn) {
                    cardLabel = createCardBackLabel();
                } else {
                    cardLabel = createCardLabel(dealerHand.get(i));
                }
                dealerCardsPanel.add(cardLabel);
            }
            
            // Dealer-Score anzeigen
            if (!gameActive || dealerTurn) {
                dealerScoreLabel.setText("Score: " + getHandValue(dealerHand));
            } else {
                dealerScoreLabel.setText("Score: ?");
            }
        } else {
            // Fallback wenn dealerHand leer oder null ist
            dealerScoreLabel.setText("Score: 0");
        }
        
        // UI aktualisieren
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
    }private static JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.getDisplayName(), JLabel.CENTER);
        label.setPreferredSize(new Dimension(140, 200));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setForeground(card.getColor());
        label.setFont(new Font("Arial", Font.BOLD, 32));
        return label;
    }
    
    private static JLabel createCardBackLabel() {
        JLabel label = new JLabel("🂠", JLabel.CENTER);
        label.setPreferredSize(new Dimension(140, 200));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(new Color(0, 50, 150));
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 48));
        return label;
    }
    
    private static void showShufflingMessage() {
        resultLabel.setText("Shuffling cards...");
        
        // Kurze Animation mit Timer
        javax.swing.Timer shuffleAnimation = new javax.swing.Timer(150, new ActionListener() {
            private int dots = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                dots = (dots + 1) % 4;
                String message = "Shuffling cards";
                for (int i = 0; i < dots; i++) {
                    message += ".";
                }
                resultLabel.setText(message);
                
                if (dots == 3) {
                    ((javax.swing.Timer)e.getSource()).stop();
                }
            }
        });
        shuffleAnimation.start();
    }
}
