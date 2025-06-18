import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Blackjack - Implementation of the classic casino card game
 * 
 * This class implements a full Blackjack game where players try to get as close to 21 as possible
 * without going over, while beating the dealer's hand. Includes standard Blackjack rules:
 * - Aces count as 1 or 11 (whichever is better)
 * - Face cards (J, Q, K) count as 10
 * - Player can hit, stand, or double down
 * - Dealer must hit on 16 and stand on 17
 * - Blackjack (21 with first 2 cards) pays 3:2
 */
public class Blackjack {
    /**
     * Card class representing a single playing card
     * Each card has a suit and rank, and can calculate its blackjack value
     */
    private static class Card {
        public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
        public enum Rank { ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING }
        
        private final Suit suit;
        private final Rank rank;
        
        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }
        
        /**
         * Returns the base value of this card in Blackjack
         * Aces are initially valued at 11, but hand calculation adjusts if needed
         * 
         * @return Blackjack value of the card
         */
        public int getValue() {
            return switch(rank) {
                case ACE -> 11;              // Ace can be 1 or 11 - starts as 11
                case TWO -> 2;
                case THREE -> 3;
                case FOUR -> 4;
                case FIVE -> 5;
                case SIX -> 6;
                case SEVEN -> 7;
                case EIGHT -> 8;
                case NINE -> 9;
                case TEN -> 10;
                case JACK, QUEEN, KING -> 10;  // All face cards worth 10
                default -> 0;
            };
        }
        
        /**
         * Returns a display-friendly name for the card with suit symbol
         * 
         * @return String representation like "A♠" or "K♥"
         */
        public String getDisplayName() {
            String rankStr = switch(rank) {
                case ACE -> "A";
                case JACK -> "J";
                case QUEEN -> "Q";
                case KING -> "K";
                default -> String.valueOf(rank.ordinal() + 1);  // Number cards 2-10
            };
            
            // Unicode suit symbols for visual appeal
            String suitStr = switch(suit) {
                case HEARTS -> "♥";
                case DIAMONDS -> "♦";
                case CLUBS -> "♣";
                case SPADES -> "♠";
            };
            
            return rankStr + suitStr;
        }
        
        /**
         * Returns the color this card should be displayed in
         * Hearts and Diamonds are red, Clubs and Spades are black
         * 
         * @return Color.RED for hearts/diamonds, Color.BLACK for clubs/spades
         */
        public Color getColor() {
            return (suit == Suit.HEARTS || suit == Suit.DIAMONDS) ? Color.RED : Color.BLACK;
        }
    }
    
    // Game state variables
    private static ArrayList<Card> deck;           // Current deck of cards
    private static ArrayList<Card> playerHand;     // Player's current hand
    private static ArrayList<Card> dealerHand;     // Dealer's current hand
    private static int currentBet = 10;            // Current bet amount
    private static boolean gameActive = false;     // Whether a game is in progress
    private static boolean dealerTurn = false;    // Whether dealer is currently playing
    
    // UI components that need to be accessed from multiple methods
    private static JLabel playerScoreLabel;        // Shows player's hand value
    private static JLabel dealerScoreLabel;        // Shows dealer's hand value
    private static JLabel resultLabel;             // Shows game results and messages
    private static JLabel betLabel;                // Shows current bet amount
    private static JTextField betField;            // Input field for bet amount
    private static JPanel playerCardsPanel;        // Panel displaying player's cards
    private static JPanel dealerCardsPanel;        // Panel displaying dealer's cards
    private static JButton hitButton;              // Button to request another card
    private static JButton standButton;            // Button to end player's turn
    private static JButton doubleButton;           // Button to double bet and take one card
    private static JButton dealButton;             // Button to start a new hand
    
    /**
     * Creates and returns the main Blackjack game panel
     * Sets up the complete game interface including betting, cards, and control buttons
     * 
     * @param onBack Callback function to return to main menu
     * @return JPanel containing the complete Blackjack game interface
     */
    public static JPanel createBlackjackPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top panel contains back button and betting controls
        JPanel topPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Back to Menu");
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        
        // Betting interface in the center of top panel
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
        
        // Main game area with casino-green background
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(0, 100, 0)); // Dark green felt color
        
        // Dealer section at top of game area
        JPanel dealerPanel = new JPanel(new BorderLayout());
        dealerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3), 
            "DEALER", 
            javax.swing.border.TitledBorder.CENTER, 
            javax.swing.border.TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 36), 
            Color.WHITE));
        dealerPanel.setOpaque(false);  // Transparent to show green background
        dealerScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        dealerScoreLabel.setFont(new Font("Arial", Font.BOLD, 42));
        dealerScoreLabel.setForeground(Color.WHITE);
        dealerCardsPanel = new JPanel(new FlowLayout());
        dealerCardsPanel.setOpaque(false);
        dealerPanel.add(dealerScoreLabel, BorderLayout.NORTH);
        dealerPanel.add(dealerCardsPanel, BorderLayout.CENTER);
        
        // Player section at bottom of game area
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3), 
            "PLAYER", 
            javax.swing.border.TitledBorder.CENTER, 
            javax.swing.border.TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 36), 
            Color.WHITE));
        playerPanel.setOpaque(false);  // Transparent to show green background
        playerScoreLabel = new JLabel("Score: 0", JLabel.CENTER);
        playerScoreLabel.setFont(new Font("Arial", Font.BOLD, 42));
        playerScoreLabel.setForeground(Color.WHITE);
        playerCardsPanel = new JPanel(new FlowLayout());
        playerCardsPanel.setOpaque(false);
        playerPanel.add(playerScoreLabel, BorderLayout.NORTH);
        playerPanel.add(playerCardsPanel, BorderLayout.CENTER);
        
        gamePanel.add(dealerPanel, BorderLayout.NORTH);
        gamePanel.add(playerPanel, BorderLayout.SOUTH);
        
        // Result message label in center of game area
        resultLabel = new JLabel("Press Deal to start!", JLabel.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 48));
        resultLabel.setForeground(Color.YELLOW);  // Bright color to stand out on green background
        gamePanel.add(resultLabel, BorderLayout.CENTER);
        
        panel.add(gamePanel, BorderLayout.CENTER);
        
        // Control buttons panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout());
        dealButton = new JButton("Deal");
        hitButton = new JButton("Hit");
        standButton = new JButton("Stand");
        doubleButton = new JButton("Double Down");
        
        // Set consistent button size and font
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
        
        // Add button click handlers with sound effects
        dealButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            startNewGame();
        });
        hitButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            playerHit();
        });
        standButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            playerStand();
        });
        doubleButton.addActionListener(e -> {
            SoundPlayer.playSound("assets/Button Click.wav");
            playerDouble();
        });
        
        buttonPanel.add(dealButton);
        buttonPanel.add(hitButton);
        buttonPanel.add(standButton);
        buttonPanel.add(doubleButton);
        
        // Initially disable game action buttons until a hand is dealt
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize the deck for the first time
        initializeDeck();
        
        return panel;
    }
    
    /**
     * Creates a fresh, shuffled deck of 52 playing cards
     * Called at game start and when deck runs low on cards
     */
    private static void initializeDeck() {
        deck = new ArrayList<>();
        // Create one card for each combination of suit and rank
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);  // Randomize card order
    }
    
    /**
     * Starts a new Blackjack hand
     * Validates bet amount, deducts money, deals initial cards, and checks for immediate wins
     */
    private static void startNewGame() {
        try {
            // Parse and validate bet amount
            currentBet = Integer.parseInt(betField.getText());
            if (currentBet <= 0) {
                SoundPlayer.playError();
                resultLabel.setText("Invalid bet amount!");
                return;
            }
            if (currentBet > GamblingTycoon.getMoney()) {
                SoundPlayer.playError();
                resultLabel.setText("Not enough money!");
                return;
            }
        } catch (NumberFormatException ex) {
            SoundPlayer.playError();
            resultLabel.setText("Please enter a valid number!");
            return;
        }
        
        // Deduct bet from player's money
        GamblingTycoon.updateMoney(-currentBet);
        betLabel.setText("Bet: $" + currentBet);
        
        // Reshuffle deck if running low on cards
        if (deck == null || deck.size() < 10) {
            showShufflingMessage();
            initializeDeck();
        }
        
        // Clear previous hands and initialize new ones
        if (playerHand == null) {
            playerHand = new ArrayList<>();
        } else {
            playerHand.clear();
        }
        if (dealerHand == null) {
            dealerHand = new ArrayList<>();
        } else {
            dealerHand.clear();
        }
        
        // Ensure deck has enough cards for initial deal
        if (deck.size() < 4) {
            showShufflingMessage();
            initializeDeck();
        }
        
        // Deal initial cards with realistic timing and sound effects
        // Standard Blackjack dealing: Player, Dealer, Player, Dealer
        SoundPlayer.playSound("assets/single card deal.wav");
        playerHand.add(deck.remove(deck.size() - 1));
        
        javax.swing.Timer dealTimer1 = new javax.swing.Timer(300, e -> {
            SoundPlayer.playSound("assets/single card deal.wav");
            dealerHand.add(deck.remove(deck.size() - 1));
            ((javax.swing.Timer)e.getSource()).stop();
        });
        dealTimer1.setRepeats(false);
        dealTimer1.start();
        
        javax.swing.Timer dealTimer2 = new javax.swing.Timer(600, e -> {
            SoundPlayer.playSound("assets/single card deal.wav");
            playerHand.add(deck.remove(deck.size() - 1));
            ((javax.swing.Timer)e.getSource()).stop();
        });
        dealTimer2.setRepeats(false);
        dealTimer2.start();
        
        javax.swing.Timer dealTimer3 = new javax.swing.Timer(900, e -> {
            SoundPlayer.playSound("assets/single card deal.wav");
            dealerHand.add(deck.remove(deck.size() - 1));
            updateDisplay();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        dealTimer3.setRepeats(false);
        dealTimer3.start();
        
        gameActive = true;
        dealerTurn = false;
        
        updateDisplay();
        
        // Enable game action buttons, disable deal button
        dealButton.setEnabled(false);
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
        doubleButton.setEnabled(true);
        
        // Check for immediate Blackjacks (21 with first 2 cards)
        if (getHandValue(playerHand) == 21) {
            if (getHandValue(dealerHand) == 21) {
                GamblingTycoon.updateMoney(currentBet); // Return bet for push
                endGame("Push! Both have Blackjack!");
            } else {
                SoundPlayer.playSound("assets/Button Click.wav");
                GamblingTycoon.updateMoney((int)(currentBet * 2.5)); // 2.5x payout for Blackjack
                endGame("Blackjack! You win!");
            }
        }
        
        resultLabel.setText("Your turn!");
    }
    
    /**
     * Player chooses to take another card ("hit")
     * Adds a card to player's hand and checks for bust or automatic stand
     */
    private static void playerHit() {
        if (!gameActive || dealerTurn) return;
        
        // Ensure deck has cards available
        if (deck.size() < 1) {
            showShufflingMessage();
            initializeDeck();
        }
        
        SoundPlayer.playSound("assets/single card deal.wav");
        playerHand.add(deck.remove(deck.size() - 1));
        updateDisplay();
        
        int playerScore = getHandValue(playerHand);
        if (playerScore > 21) {
            SoundPlayer.playError();
            endGame("Bust! You lose!");
        } else if (playerScore == 21) {
            playerStand(); // Automatically stand on 21
        }
        
        // Can't double down after taking a hit
        doubleButton.setEnabled(false);
    }
    
    /**
     * Player chooses to stand (keep current hand and end turn)
     * Begins dealer's turn following standard Blackjack rules
     */
    private static void playerStand() {
        if (!gameActive) return;
        
        dealerTurn = true;
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        
        // Dealer plays automatically following casino rules
        javax.swing.Timer dealerTimer = new javax.swing.Timer(1000, e -> {
            if (getHandValue(dealerHand) < 17) {
                // Dealer must hit on 16 and below
                if (deck.size() < 1) {
                    showShufflingMessage();
                    initializeDeck();
                }
                SoundPlayer.playSound("assets/single card deal.wav");
                dealerHand.add(deck.remove(deck.size() - 1));
                updateDisplay();
            } else {
                // Dealer stands on 17 and above - determine winner
                ((javax.swing.Timer)e.getSource()).stop();
                
                int playerScore = getHandValue(playerHand);
                int dealerScore = getHandValue(dealerHand);
                
                if (dealerScore > 21) {
                    SoundPlayer.playSound("assets/Button Click.wav");
                    GamblingTycoon.updateMoney(currentBet * 2);  // Return bet + winnings
                    endGame("Dealer busts! You win!");
                } else if (playerScore > dealerScore) {
                    SoundPlayer.playSound("assets/Button Click.wav");
                    GamblingTycoon.updateMoney(currentBet * 2);  // Return bet + winnings
                    endGame("You win!");
                } else if (dealerScore > playerScore) {
                    SoundPlayer.playError();
                    endGame("Dealer wins!");  // Player loses bet (already deducted)
                } else {
                    GamblingTycoon.updateMoney(currentBet);  // Return bet for tie
                    endGame("Push! It's a tie!");
                }
            }
        });
        dealerTimer.start();
    }
    
    /**
     * Player chooses to double down (double bet, take exactly one more card, then stand)
     * Can only be done with the first two cards (before hitting)
     */
    private static void playerDouble() {
        if (!gameActive || dealerTurn || playerHand.size() > 2) return;
        
        // Check if player has enough money to double the bet
        if (currentBet > GamblingTycoon.getMoney()) {
            SoundPlayer.playError();
            resultLabel.setText("Not enough money to double down!");
            return;
        }
        
        // Double the bet by deducting additional amount
        GamblingTycoon.updateMoney(-currentBet);
        currentBet *= 2;
        betLabel.setText("Bet: $" + currentBet);
        
        // Take exactly one card and then automatically stand
        playerHand.add(deck.remove(deck.size() - 1));
        updateDisplay();
        
        if (getHandValue(playerHand) > 21) {
            SoundPlayer.playError();
            endGame("Bust! You lose!");
        } else {
            playerStand();  // Automatically stand after double down
        }
    }
    
    /**
     * Ends the current game and resets UI for next hand
     * Also checks if player has run out of money for game over
     * 
     * @param message Result message to display to player
     */
    private static void endGame(String message) {
        gameActive = false;
        dealerTurn = false;  // Reset dealer turn flag
        resultLabel.setText(message);
        
        // Reset button states for next hand
        dealButton.setEnabled(true);
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
        doubleButton.setEnabled(false);
        
        // Ensure display is updated to show all cards (including dealer's hidden card)
        updateDisplay();
        
        // Check for game over condition
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    /**
     * Calculates the total value of a hand according to Blackjack rules
     * Automatically adjusts Aces from 11 to 1 to prevent busting when possible
     * 
     * @param hand ArrayList of cards to evaluate
     * @return Best possible hand value under Blackjack rules
     */
    private static int getHandValue(ArrayList<Card> hand) {
        int value = 0;
        int aces = 0;
        
        // Sum all card values and count Aces
        for (Card card : hand) {
            if (card.rank == Card.Rank.ACE) {
                aces++;
            }
            value += card.getValue();
        }
        
        // Convert Aces from 11 to 1 if needed to avoid busting
        while (value > 21 && aces > 0) {
            value -= 10;  // Change Ace from 11 to 1 (difference of 10)
            aces--;       // One less Ace available for conversion
        }
        
        return value;
    }
    
    /**
     * Updates the visual display of cards and scores for both player and dealer
     * Handles the logic for showing/hiding dealer's second card during play
     */
    private static void updateDisplay() {
        // Display player's cards
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
        
        // Display dealer's cards
        dealerCardsPanel.removeAll();
        if (dealerHand != null && !dealerHand.isEmpty()) {
            for (int i = 0; i < dealerHand.size(); i++) {
                JLabel cardLabel;
                // Hide dealer's second card until dealer's turn or game ends
                if (i == 1 && gameActive && !dealerTurn) {
                    cardLabel = createCardBackLabel();  // Show card back
                } else {
                    cardLabel = createCardLabel(dealerHand.get(i));  // Show actual card
                }
                dealerCardsPanel.add(cardLabel);
            }
            
            // Show dealer's score (hide during player's turn)
            if (!gameActive || dealerTurn) {
                dealerScoreLabel.setText("Score: " + getHandValue(dealerHand));
            } else {
                dealerScoreLabel.setText("Score: ?");  // Hide score while card is hidden
            }
        } else {
            dealerScoreLabel.setText("Score: 0");
        }
        
        // Refresh the UI components
        playerCardsPanel.revalidate();
        playerCardsPanel.repaint();
        dealerCardsPanel.revalidate();
        dealerCardsPanel.repaint();
    }
    
    /**
     * Creates a visual representation of a playing card
     * 
     * @param card The card to display
     * @return JLabel formatted to look like a playing card
     */
    private static JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.getDisplayName(), JLabel.CENTER);
        label.setPreferredSize(new Dimension(140, 200));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setForeground(card.getColor());  // Red for hearts/diamonds, black for clubs/spades
        label.setFont(new Font("Arial", Font.BOLD, 32));
        return label;
    }
    
    /**
     * Creates a visual representation of a face-down card (card back)
     * Used to hide dealer's second card during player's turn
     * 
     * @return JLabel formatted to look like the back of a playing card
     */
    private static JLabel createCardBackLabel() {
        JLabel label = new JLabel("🂠", JLabel.CENTER);  // Unicode card back symbol
        label.setPreferredSize(new Dimension(140, 200));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(new Color(0, 50, 150));  // Dark blue card back
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 48));
        return label;
    }
    
    /**
     * Displays an animated shuffling message when the deck is being reshuffled
     * Provides visual feedback to the player that cards are being randomized
     */
    private static void showShufflingMessage() {
        resultLabel.setText("Shuffling cards...");
        
        // Create simple animation with dots
        javax.swing.Timer shuffleAnimation = new javax.swing.Timer(150, new ActionListener() {
            private int dots = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                dots = (dots + 1) % 4;  // Cycle through 0-3 dots
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
