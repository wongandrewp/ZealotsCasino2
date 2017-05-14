package io.zipcoder.zealotscasino;

import java.util.ArrayList;

/**
 * Created by denniskalaygian on 5/10/17.
 */
public class BlackJackDealer implements CardDealer {

    private int dealerHandValue;
    private int playerHandValue;
    private boolean gameRunning;
    private double insuranceValue;

    private BlackJackHand player;
    private BlackJackHand dealerHand;
    private Deck deck;

    public BlackJackDealer() {
        deck = new Deck();
        deck.buildDeck();
    }

    public void setInsuranceValue(double insuranceValue) {
        this.insuranceValue = insuranceValue;
    }

    public void setDealerHand(){
        dealerHand = new BlackJackHand();
    }

    public void setPlayerHandValue(int value){
        playerHandValue = value;
    }

    @Override
    public void play(Player player) {
        gameRunning = true;
        while(gameRunning){
            if(player.getWallet() < 20){
                UserInput.display("You only have $" + player.getWallet() + ", you should probably save that money for the bus.");
                break;
            }
            displayPlayerWallet(player);
            takeTurn(player);
            String playAgain = UserInput.getStringInput("Play again? Yes / No");
            if((playAgain.equalsIgnoreCase("yes"))){
                gameRunning = true;
                deck.buildDeck();
            }
            Hand hand = new Hand();
            player.setHand(hand);
        }
    }

    public void displayPlayerWallet(Player player){
        UserInput.display("You have $" + player.getWallet() + " remaining.");
    }

    public void takeTurn(Player player) {
        protectedBetProcess(player);
        buildPlayersHands(player);
        if(checkIfSplit(player)){
            split(player);
        }else {
            assertBlackJack(player);
            if (playerHandValue != 21) {
                buildDealerHand(player);
                hitProcess(player);
                evaluateResult(player);
            }
        }
    }

    private void protectedBetProcess(Player player){
        while(true) {
            try {
                player.makeBet(UserInput.getDoubleInput("Place your bet! (Minimum $20.00) "));
                break;
            } catch (Exception e) {
                UserInput.display("Enter a valid number. ");
            }
        }
    }

    private void buildPlayersHands(Player player){
        initializeHands(player);
        dealHandTo(player);
        determinePlayerHandValue(player.getHand());
    }

    private void buildDealerHand(Player player){
        dealHandToDealer(player);
        determineDealerHandValue(dealerHand);
    }

    public void initializeHands(Player player) {
        player.setHand(new BlackJackHand());
        setDealerHand();
        initializePlayerHandValue();
        initializeDealerHandValue();
    }

    private BlackJackHand toBlackJackHand(Hand hand) {
        if (hand instanceof BlackJackHand) {
            return (BlackJackHand) hand;
        }
        return null;
    }

    public void initializeDealerHandValue() {
        dealerHandValue = 0;
    }


    public void initializePlayerHandValue(){
        playerHandValue = 0;
    }

    private void displayLoseGame() {
        UserInput.display("Busted!");
    }

    private void displayBlackJack() {
        UserInput.display("BlackJack!");
    }

    public void determinePlayerHandValue(Hand hand) {
        playerHandValue = toBlackJackHand(hand).examineHandValue();
    }

    public void determineDealerHandValue(Hand hand){
        dealerHandValue = toBlackJackHand(hand).examineHandValue();
    }

    @Override
    public void dealCardTo(Player player) {
        Card card = deck.surrenderCard();
        Hand currentHand = player.getHand();
        currentHand.receiveCard(card);
        determinePlayerHandValue(currentHand);
    }

    private void dealCardToDealer() {
        Card card = deck.surrenderCard();
        Hand currentHand = dealerHand;
        currentHand.receiveCard(card);
        determineDealerHandValue(currentHand);
    }

    @Override
    public void dealHandTo(Player player) {
        for (int i = 0; i < 2; i++) dealCardTo(player);
        userDisplayHand(player);
    }

    private void dealHandToDealer(Player player) {
        for (int i = 0; i < 2; i++) dealCardToDealer();
        displayDealerCardUp();
        if(dealerHand.getCards().get(0).getFaceValue() == "ACE"){
            protectedInsuranceRequest(player);
        }
    }

    private void displayDealerCardUp(){
        UserInput.display("Exposed card of dealer: " + dealerHand.getCards().get(0));
    }

    @Override
    public void pay(Player player, double payOut) {
        player.collectWinnings(payOut);
    }

    private void takeHit(Player player){
        dealCardTo(player);
        userDisplayHand(player);
    }

    private void userDisplayHand(Player player) {
        StringBuilder outPut = new StringBuilder(1000);
        ArrayList<Card> cards = player.getHand().getCards();
        for (Card card : cards) {
            outPut.append(card);
            outPut.append("\n");
        }
        outPut.append("\nTotal Player value: " + playerHandValue);
        UserInput.display(outPut);
    }

    private boolean checkIfPlayerHit(){
        UserInput in = new UserInput();
        String hit = in.getStringInput("If you would like another card, enter HIT ");
        if(hit.equalsIgnoreCase("hit")){
            return true;
        }
        return false;
    }

    private void decideWinner(Player player){
        if((dealerHandValue - playerHandValue) > 0 && dealerHandValue < 22){
            UserInput.display("Dealer wins with a hand value of: " + dealerHandValue);
        } else if(dealerHandValue == playerHandValue){
            UserInput.display("Tie!");
            pay(player, player.getBet().getBetValue());
        } else{
            UserInput.display("Player wins, dealer hand was " + dealerHandValue);
            pay(player, player.getBet().getBetValue() * 2);
        }
    }

    private boolean checkStatus(Player player, double bet){
        BlackJackHand handAssertions = toBlackJackHand(player.getHand());
        boolean bust = handAssertions.checkBust();
        boolean blackJack = handAssertions.checkBlackJack();
        if(bust){
            displayLoseGame();
            gameRunning = false;
            return false;
        }
        if(blackJack){
            pay(player, bet*2);
            gameRunning = false;
            return false;
        }
        boolean hit = checkIfPlayerHit();
        return hit;
    }

    public void assertBlackJack(Player player) {
        if (playerHandValue == 21) {
            displayBlackJack();
            pay(player, player.getBet().getBetValue() * 3);
            gameRunning = false;
        }
    }

    private boolean requestInsuranceValue(){
        String answer = UserInput.getStringInput("Take insurance? Enter Yes if you would like to. " );
        if(answer.equalsIgnoreCase("yes")){
            return true;
        }
        return false;
    }

    private void hitProcess(Player player){
        boolean hit = checkIfPlayerHit();
        while(hit){
            takeHit(player);
            hit = checkStatus(player, player.getBet().getBetValue());
        }
    }

    private void evaluateResult(Player player){
        if(dealerHandValue == 21){
            payPlayer(player);
        }else {
            checkIfDealerHit();
            payPlayer(player);
        }
    }

    private void checkIfDealerHit() {
        if (dealerHandValue < 17) {
            dealCardToDealer();
        }
    }

    // We should definitely move this out
    private void payPlayer(Player player){
        if(dealerHandValue == 21 && insuranceValue != 0){
            UserInput.display("Dealer had BlackJack - good call! ");
            pay(player, insuranceValue);
        }
        if(gameRunning == true) {
            decideWinner(player);
            gameRunning = false;
        }
    }

    private void protectedInsuranceRequest(Player player){
        boolean response = requestInsuranceValue();
        while(response){
            try {
                setInsuranceValue(UserInput.getDoubleInput("How much would you like to put on it?"));
                double postInsuranceWallet = player.getWallet() - insuranceValue;
                player.setWallet(postInsuranceWallet);
                break;
            }catch(Exception e){
                UserInput.display("Please enter a valid number.");
            }
        }
    }

    public boolean checkIfSplit(Player player){
        if(player.getHand().getCards().get(0).getFaceValue().equals(player.getHand().getCards().get(1).getFaceValue())){
            String splitDecision = UserInput.getStringInput("Type YES if you would like to split. ");
            if(splitDecision.equalsIgnoreCase("yes")){
                return true;
            }
            return false;
        }
        return false;
    }

    public void split(Player player){
        player.setWallet(player.getWallet() + player.getBet().getBetValue());
        player.makeBet(player.getBet().getBetValue() * 2);
        ArrayList<Card> cards = new ArrayList<>();
        for(int i = 0; i < player.getHand().getCards().size(); i++){
            cards.add(player.getHand().getCards().get(i));
        }
        for(Card card : cards){
            Hand hand = new Hand();
            playerHandValue = 0;
            player.setHand(hand);
            player.getHand().receiveCard(card);
            dealCardTo(player);
            assertBlackJack(player);
            if(playerHandValue != 21) {
                buildDealerHand(player);
                hitProcess(player);
                evaluateResult(player);
                dealerHandValue = 0;
            }
        }
    }
}
