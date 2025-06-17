import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

public class RideTheBus {
    // Karten-Datenstrukturen
    private static class Card {
        public enum Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
        public enum Rank { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE }
        
        private Suit suit;
        private Rank rank;
        
        public Card(Suit suit, Rank rank) {
            this.suit = suit;
            this.rank = rank;
        }
        
        public int getValue() {
            // Ace = 14, King = 13, Queen = 12, Jack = 11, etc.
            return rank.ordinal() + 2;
        }
        
        public String getDisplayName() {
            String rankStr = "";
            switch(rank) {
                case ACE: rankStr = "A"; break;
                case JACK: rankStr = "J"; break;
                case QUEEN: rankStr = "Q"; break;
                case KING: rankStr = "K"; break;
                default: rankStr = String.valueOf(getValue());
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
        
        public boolean isRed() {
            return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
        }
    }
    
    // Spielzustand
    private static ArrayList<Card> deck;
    private static ArrayList<Card> drawnCards;
    private static int currentBet = 50;
    private static int currentRound = 0;
    private static int currentMultiplier = 1;
    private static boolean gameActive = false;
    private static javax.swing.Timer roundTimer;
    private static int timeLeft = 10;
    
    // UI-Komponenten
    private static JLabel roundLabel;
    private static JLabel betLabel;
    private static JLabel multiplierLabel;
    private static JLabel instructionLabel;
    private static JLabel timerLabel;
    private static JTextField betField;
    private static JPanel cardsPanel;
    private static JPanel buttonsPanel;
    private static JButton startButton;
    private static JButton cashOutButton;
    private static JButton redButton;
    private static JButton blackButton;
    private static JButton higherButton;
    private static JButton lowerButton;
    private static JButton insideButton;
    private static JButton outsideButton;
    private static JButton heartsButton;
    private static JButton diamondsButton;
    private static JButton clubsButton;
    private static JButton spadesButton;
    
    public static JPanel createRideTheBusPanel(Runnable onBack) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 50, 0)); // Dunkelgrün wie Filz
        
        // Top panel mit Back-Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 20));
        backButton.addActionListener(e -> onBack.run());
        topPanel.add(backButton, BorderLayout.WEST);
        
        // Game Info Panel
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setOpaque(false);
        
        roundLabel = new JLabel("RIDE THE BUS - Ready to Start");
        roundLabel.setFont(new Font("Arial", Font.BOLD, 36));
        roundLabel.setForeground(Color.YELLOW);
        
        betLabel = new JLabel("Bet: $50");
        betLabel.setFont(new Font("Arial", Font.BOLD, 28));
        betLabel.setForeground(Color.WHITE);
        
        multiplierLabel = new JLabel("Multiplier: 1x");
        multiplierLabel.setFont(new Font("Arial", Font.BOLD, 28));
        multiplierLabel.setForeground(Color.CYAN);
        
        JLabel betTextLabel = new JLabel("Bet Amount ($10-$500):");
        betTextLabel.setFont(new Font("Arial", Font.BOLD, 24));
        betTextLabel.setForeground(Color.WHITE);
        
        betField = new JTextField("50", 8);
        betField.setFont(new Font("Arial", Font.PLAIN, 24));
        
        infoPanel.add(betTextLabel);
        infoPanel.add(betField);
        infoPanel.add(betLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(multiplierLabel);
        
        topPanel.add(roundLabel, BorderLayout.CENTER);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Haupt-Spielbereich
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setOpaque(false);
        
        // Instructions und Timer
        JPanel instructionPanel = new JPanel(new BorderLayout());
        instructionPanel.setOpaque(false);
        
        instructionLabel = new JLabel("Welcome to Ride the Bus! Set your bet and press Start to begin.", JLabel.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.BOLD, 32));
        instructionLabel.setForeground(Color.WHITE);
        
        timerLabel = new JLabel("", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 48));
        timerLabel.setForeground(Color.RED);
        
        instructionPanel.add(instructionLabel, BorderLayout.CENTER);
        instructionPanel.add(timerLabel, BorderLayout.SOUTH);
        
        // Karten-Bereich
        cardsPanel = new JPanel(new FlowLayout());
        cardsPanel.setOpaque(false);
        
        gamePanel.add(instructionPanel, BorderLayout.NORTH);
        gamePanel.add(cardsPanel, BorderLayout.CENTER);
        
        panel.add(gamePanel, BorderLayout.CENTER);
        
        // Button Panel
        buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setOpaque(false);
        
        createButtons();
        setupInitialButtons();
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        // Initialisierung
        initializeDeck();
        
        return panel;
    }
    
    private static void createButtons() {
        Font buttonFont = new Font("Arial", Font.BOLD, 24);
        Dimension buttonSize = new Dimension(180, 60);
        
        // Control Buttons
        startButton = new JButton("Start Game");
        startButton.setFont(buttonFont);
        startButton.setPreferredSize(buttonSize);
        startButton.addActionListener(e -> startNewGame());
        
        cashOutButton = new JButton("Cash Out");
        cashOutButton.setFont(buttonFont);
        cashOutButton.setPreferredSize(buttonSize);
        cashOutButton.addActionListener(e -> cashOut());
        
        // Round 1 Buttons (Color)
        redButton = new JButton("RED");
        redButton.setFont(buttonFont);
        redButton.setPreferredSize(buttonSize);
        redButton.setBackground(Color.RED);
        redButton.setForeground(Color.WHITE);
        redButton.addActionListener(e -> makeColorChoice(true));
        
        blackButton = new JButton("BLACK");
        blackButton.setFont(buttonFont);
        blackButton.setPreferredSize(buttonSize);
        blackButton.setBackground(Color.BLACK);
        blackButton.setForeground(Color.WHITE);
        blackButton.addActionListener(e -> makeColorChoice(false));
        
        // Round 2 Buttons (Higher/Lower)
        higherButton = new JButton("HIGHER");
        higherButton.setFont(buttonFont);
        higherButton.setPreferredSize(buttonSize);
        higherButton.setBackground(Color.GREEN);
        higherButton.addActionListener(e -> makeHigherLowerChoice(true));
        
        lowerButton = new JButton("LOWER");
        lowerButton.setFont(buttonFont);
        lowerButton.setPreferredSize(buttonSize);
        lowerButton.setBackground(Color.ORANGE);
        lowerButton.addActionListener(e -> makeHigherLowerChoice(false));
        
        // Round 3 Buttons (Inside/Outside)
        insideButton = new JButton("INSIDE");
        insideButton.setFont(buttonFont);
        insideButton.setPreferredSize(buttonSize);
        insideButton.setBackground(Color.BLUE);
        insideButton.setForeground(Color.WHITE);
        insideButton.addActionListener(e -> makeInsideOutsideChoice(true));
        
        outsideButton = new JButton("OUTSIDE");
        outsideButton.setFont(buttonFont);
        outsideButton.setPreferredSize(buttonSize);
        outsideButton.setBackground(Color.MAGENTA);
        outsideButton.setForeground(Color.WHITE);
        outsideButton.addActionListener(e -> makeInsideOutsideChoice(false));
        
        // Round 4 Buttons (Suits)
        heartsButton = new JButton("♥");
        heartsButton.setFont(new Font("Arial", Font.BOLD, 36));
        heartsButton.setPreferredSize(buttonSize);
        heartsButton.setBackground(Color.WHITE);
        heartsButton.setForeground(Color.RED);
        heartsButton.addActionListener(e -> makeSuitChoice(Card.Suit.HEARTS));
        
        diamondsButton = new JButton("♦");
        diamondsButton.setFont(new Font("Arial", Font.BOLD, 36));
        diamondsButton.setPreferredSize(buttonSize);
        diamondsButton.setBackground(Color.WHITE);
        diamondsButton.setForeground(Color.RED);
        diamondsButton.addActionListener(e -> makeSuitChoice(Card.Suit.DIAMONDS));
        
        clubsButton = new JButton("♣");
        clubsButton.setFont(new Font("Arial", Font.BOLD, 36));
        clubsButton.setPreferredSize(buttonSize);
        clubsButton.setBackground(Color.WHITE);
        clubsButton.setForeground(Color.BLACK);
        clubsButton.addActionListener(e -> makeSuitChoice(Card.Suit.CLUBS));
        
        spadesButton = new JButton("♠");
        spadesButton.setFont(new Font("Arial", Font.BOLD, 36));
        spadesButton.setPreferredSize(buttonSize);
        spadesButton.setBackground(Color.WHITE);
        spadesButton.setForeground(Color.BLACK);
        spadesButton.addActionListener(e -> makeSuitChoice(Card.Suit.SPADES));
    }
    
    private static void setupInitialButtons() {
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
    }
    
    private static void initializeDeck() {
        deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(deck);
        drawnCards = new ArrayList<>();
    }
    
    private static void startNewGame() {
        try {
            currentBet = Integer.parseInt(betField.getText());
            if (currentBet < 10 || currentBet > 500) {
                instructionLabel.setText("Bet must be between $10 and $500!");
                return;
            }
            if (currentBet > GamblingTycoon.getMoney()) {
                instructionLabel.setText("Not enough money!");
                return;
            }
        } catch (NumberFormatException ex) {
            instructionLabel.setText("Please enter a valid bet amount!");
            return;
        }
        
        // Einsatz abziehen
        GamblingTycoon.updateMoney(-currentBet);
        betLabel.setText("Bet: $" + currentBet);
        
        // Spiel initialisieren
        if (deck.size() < 10) {
            initializeDeck();
        }
        
        drawnCards.clear();
        currentRound = 1;
        currentMultiplier = 1;
        gameActive = true;
        
        updateDisplay();
        startRound1();
    }
    
    private static void startRound1() {
        currentRound = 1;
        currentMultiplier = 2; // Round 1 multiplier
        roundLabel.setText("ROUND 1: COLOR - Red or Black?");
        instructionLabel.setText("Predict the color of the next card!");
        multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
        
        buttonsPanel.removeAll();
        buttonsPanel.add(redButton);
        buttonsPanel.add(blackButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        startTimer();
    }
    
    private static void makeColorChoice(boolean chooseRed) {
        if (!gameActive || currentRound != 1) return;
        
        stopTimer();
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean isRed = drawnCard.isRed();
        boolean correct = (chooseRed && isRed) || (!chooseRed && !isRed);
        
        updateCardDisplay();
        
        if (correct) {
            instructionLabel.setText("Correct! The card was " + (isRed ? "RED" : "BLACK") + "!");
            proceedToRound2();
        } else {
            instructionLabel.setText("Wrong! The card was " + (isRed ? "RED" : "BLACK") + ". You lose!");
            endGame(false);
        }
    }
    
    private static void proceedToRound2() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 2;
            currentMultiplier = 3; // Round 2 multiplier
            roundLabel.setText("ROUND 2: HIGHER or LOWER?");
            
            Card lastCard = drawnCards.get(drawnCards.size() - 1);
            instructionLabel.setText("Is the next card higher or lower than " + lastCard.getDisplayName() + "?");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
            
            buttonsPanel.removeAll();
            buttonsPanel.add(higherButton);
            buttonsPanel.add(lowerButton);
            buttonsPanel.add(cashOutButton);
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    private static void makeHigherLowerChoice(boolean chooseHigher) {
        if (!gameActive || currentRound != 2) return;
        
        stopTimer();
        Card previousCard = drawnCards.get(drawnCards.size() - 1);
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean isHigher = drawnCard.getValue() > previousCard.getValue();
        boolean correct = (chooseHigher && isHigher) || (!chooseHigher && !isHigher);
        
        // Handle ties (same value)
        if (drawnCard.getValue() == previousCard.getValue()) {
            correct = false; // Ties count as incorrect
        }
        
        updateCardDisplay();
        
        if (correct) {
            instructionLabel.setText("Correct! " + drawnCard.getDisplayName() + " was " + 
                                   (isHigher ? "higher" : "lower") + " than " + previousCard.getDisplayName() + "!");
            proceedToRound3();
        } else {
            if (drawnCard.getValue() == previousCard.getValue()) {
                instructionLabel.setText("Same value! " + drawnCard.getDisplayName() + " = " + previousCard.getDisplayName() + ". You lose!");
            } else {
                instructionLabel.setText("Wrong! " + drawnCard.getDisplayName() + " was " + 
                                       (isHigher ? "higher" : "lower") + " than " + previousCard.getDisplayName() + "!");
            }
            endGame(false);
        }
    }
      private static void proceedToRound3() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 3;
            currentMultiplier = 4; // Round 3 multiplier
            roundLabel.setText("ROUND 3: INSIDE or OUTSIDE?");
            
            Card card1 = drawnCards.get(0);
            Card card2 = drawnCards.get(1);
            // Show the range to the player
            instructionLabel.setText("Will the next card be INSIDE or OUTSIDE " + 
                                   card1.getDisplayName() + " and " + card2.getDisplayName() + "?");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
            
            buttonsPanel.removeAll();
            buttonsPanel.add(insideButton);
            buttonsPanel.add(outsideButton);
            buttonsPanel.add(cashOutButton);
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    private static void makeInsideOutsideChoice(boolean chooseInside) {
        if (!gameActive || currentRound != 3) return;
        
        stopTimer();
        Card card1 = drawnCards.get(0);
        Card card2 = drawnCards.get(1);
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        int low = Math.min(card1.getValue(), card2.getValue());
        int high = Math.max(card1.getValue(), card2.getValue());
        int cardValue = drawnCard.getValue();
        
        boolean isInside = cardValue > low && cardValue < high;
        boolean correct = (chooseInside && isInside) || (!chooseInside && !isInside);
        
        updateCardDisplay();
        
        if (correct) {
            instructionLabel.setText("Correct! " + drawnCard.getDisplayName() + " was " + 
                                   (isInside ? "inside" : "outside") + " the range!");
            proceedToRound4();
        } else {
            instructionLabel.setText("Wrong! " + drawnCard.getDisplayName() + " was " + 
                                   (isInside ? "inside" : "outside") + " the range!");
            endGame(false);
        }
    }
    
    private static void proceedToRound4() {
        javax.swing.Timer delay = new javax.swing.Timer(2000, e -> {
            currentRound = 4;
            currentMultiplier = 20; // Round 4 multiplier - Maximum payout!
            roundLabel.setText("ROUND 4: SUIT - Final Round!");
            instructionLabel.setText("Predict the SUIT of the final card for 20x multiplier!");
            multiplierLabel.setText("Multiplier: " + currentMultiplier + "x");
            
            buttonsPanel.removeAll();
            buttonsPanel.add(heartsButton);
            buttonsPanel.add(diamondsButton);
            buttonsPanel.add(clubsButton);
            buttonsPanel.add(spadesButton);
            buttonsPanel.add(cashOutButton);
            buttonsPanel.revalidate();
            buttonsPanel.repaint();
            
            startTimer();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        delay.setRepeats(false);
        delay.start();
    }
    
    private static void makeSuitChoice(Card.Suit chosenSuit) {
        if (!gameActive || currentRound != 4) return;
        
        stopTimer();
        Card drawnCard = deck.remove(deck.size() - 1);
        drawnCards.add(drawnCard);
        
        boolean correct = drawnCard.suit == chosenSuit;
        
        updateCardDisplay();
        
        if (correct) {
            instructionLabel.setText("JACKPOT! " + drawnCard.getDisplayName() + " - You won 20x your bet!");
            endGame(true);
        } else {
            instructionLabel.setText("Wrong suit! " + drawnCard.getDisplayName() + " - You lose everything!");
            endGame(false);
        }
    }    private static void cashOut() {
        if (!gameActive || currentRound < 2) return;
        
        stopTimer();
        
        // Calculate correct multiplier based on number of drawn cards (completed rounds)
        int completedRounds = drawnCards.size();
        int completedMultiplier;
        
        if (completedRounds == 1) {
            completedMultiplier = 2; // After completing Round 1 (color)
        } else if (completedRounds == 2) {
            completedMultiplier = 3; // After completing Round 2 (higher/lower)
        } else if (completedRounds == 3) {
            completedMultiplier = 4; // After completing Round 3 (inside/outside)
        } else {
            completedMultiplier = 1; // Fallback
        }
        
        int winnings = currentBet * completedMultiplier;
        GamblingTycoon.updateMoney(winnings);
        
        instructionLabel.setText("Cashed out! You won $" + winnings + " (Bet: $" + currentBet + " x " + completedMultiplier + ")!");
        endGameCashOut(); // Use separate method to avoid double money transfer
    }
    
    private static void endGameCashOut() {
        gameActive = false;
        stopTimer();
        
        roundLabel.setText("Well played! You cashed out safely!");
        
        // Reset buttons
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        // Game Over check
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    private static void startTimer() {
        timeLeft = 10;
        timerLabel.setText("Time: " + timeLeft);
        
        if (roundTimer != null) {
            roundTimer.stop();
        }
        
        roundTimer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft);
                
                if (timeLeft <= 0) {
                    stopTimer();
                    instructionLabel.setText("Time's up! You lose!");
                    endGame(false);
                }
            }
        });
        roundTimer.start();
    }
    
    private static void stopTimer() {
        if (roundTimer != null) {
            roundTimer.stop();
        }
        timerLabel.setText("");
    }
      private static void endGame(boolean won) {
        gameActive = false;
        stopTimer();
        
        if (won && currentRound == 4) {
            // Maximum payout for completing all 4 rounds
            int winnings = currentBet * 20; // 20x multiplier for completing all rounds
            GamblingTycoon.updateMoney(winnings);
            roundLabel.setText("🎉 CHAMPION! You completed all 4 rounds! 🎉");
        } else if (!won) {
            roundLabel.setText("Game Over - Better luck next time!");
        } else {
            // This should not happen anymore since cashOut uses its own method
            roundLabel.setText("Game ended!");
        }
        
        // Reset buttons
        buttonsPanel.removeAll();
        buttonsPanel.add(startButton);
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        
        // Game Over check
        if (GamblingTycoon.getMoney() <= 0) {
            GamblingTycoon.showGameOverScreen();
        }
    }
    
    private static void updateDisplay() {
        updateCardDisplay();
    }
    
    private static void updateCardDisplay() {
        cardsPanel.removeAll();
        
        for (int i = 0; i < drawnCards.size(); i++) {
            Card card = drawnCards.get(i);
            JLabel cardLabel = createCardLabel(card);
            cardsPanel.add(cardLabel);
            
            if (i < drawnCards.size() - 1) {
                // Add spacing between cards
                cardsPanel.add(Box.createHorizontalStrut(10));
            }
        }
        
        // Add placeholder for next card if game is active
        if (gameActive && drawnCards.size() < 4) {
            cardsPanel.add(Box.createHorizontalStrut(10));
            JLabel nextCardLabel = createCardBackLabel();
            cardsPanel.add(nextCardLabel);
        }
        
        cardsPanel.revalidate();
        cardsPanel.repaint();
    }
    
    private static JLabel createCardLabel(Card card) {
        JLabel label = new JLabel(card.getDisplayName(), JLabel.CENTER);
        label.setPreferredSize(new Dimension(120, 180));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setForeground(card.getColor());
        label.setFont(new Font("Arial", Font.BOLD, 28));
        return label;
    }
    
    private static JLabel createCardBackLabel() {
        JLabel label = new JLabel("🂠", JLabel.CENTER);
        label.setPreferredSize(new Dimension(120, 180));
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        label.setBackground(new Color(0, 50, 150));
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 36));
        return label;
    }
}
