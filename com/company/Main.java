package com.company;

import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static com.company.Main.CLIENT_VERSION;
import static com.company.Main.COIN_START;
import static com.company.Main.randomNum;

public class Main {
    static final int COIN_START = 10;//TODO 0
    static final String CLIENT_VERSION = "0.02";
    private static final int PORT = 8901;
    static int randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);
    static ArrayList<String> names = new ArrayList<>();
    static ArrayList<PrintWriter> writers = new ArrayList<>();
    static ArrayList<Gamer> freePlayer = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("Server is Running");
        try {
            while (true) {
                new Gamer(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}

class Gamer extends Thread {
    Board board = new Board();
    String name;
    Deck simpleDeck = new Deck("defaultDeck");
    Player player = new Player(this, simpleDeck, "", "", 0);//For load deck, then set normal hero by new Player(Gamer _owner, Card _card, Deck _deck, String _playerName)
    GameQueue gameQueue = new GameQueue(this);
    int creatureWhoAttack;
    int creatureWhoAttackTarget;
    public final Object cretureDiedMonitor = new Object();
    public final Object monitor = new Object();
    public final Object queueMonitor = new Object();
    int sufflingConst;
    MyFunction.PlayerStatus status;
    MyFunction.PlayerStatus memPlayerStatus;
    Gamer opponent;
    private Socket socket;
    BufferedReader input;
    PrintWriter output;
    String deckName;
    ArrayList<String> deckList = new ArrayList<>();
    boolean endMuligan = false;
    boolean ready = true;
     int choiceXcolor = 0;
     int choiceXtype = 0;
     String choiceXcreatureType = "";
     int choiceXcost = 0;
     int choiceXcostExactly = 0;
     String choiceXname;

    void printToView(int n, String txt) {
        output.println("#Message(" + n + "," + txt + ")");
        System.out.println(txt);
    }

    void printToView(int n, Color c, String txt) {//Depricated
        output.println("#Message(" + n + "," + txt + ")");
        System.out.println(txt);
    }

    void setPlayerGameStatus(MyFunction.PlayerStatus _status) {
        status = _status;
        output.println("#PlayerStatus("+status.getValue() + ")");
    }

    void sendBoth(String message) {
        System.out.println("Send both:" + message);
        output.println(message);
        opponent.output.println(message);
    }

    boolean isFirstPlayer(String name1, String name2) {
        byte[] b = (name1 + randomNum).getBytes();
        byte[] b2 = (name2 + randomNum).getBytes();
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(b);
            String a = DatatypeConverter.printHexBinary(hash);
            byte[] hash2 = MessageDigest.getInstance("MD5").digest(b2);
            String a2 = DatatypeConverter.printHexBinary(hash2);
            return a.compareTo(a2) >= 0;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    Gamer(Socket socket) {
        this.socket = socket;
    }

    private ArrayList<String> getDeckList() throws IOException {
        ArrayList<String> result = new ArrayList<>();
        String card;
        while (!(card = input.readLine()).equals("$ENDDECK")) {
            result.add(card);
            // System.out.println("Card = "+card);
            player.deck.cards.add(new Card(Card.getCardByName(card)));
        }
        return result;
    }

    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "windows-1251"));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "windows-1251"), true);

            while (true) {
                String command = input.readLine();
                if (command.contains("$IAM")) {
                    ArrayList<String> parameter = getTextBetween(command);
                    String ver = parameter.get(2);
                    if (ver.equals(CLIENT_VERSION)) {
                        name = parameter.get(0);
                        System.out.println(name + " connected.");

                        boolean nameCorrect = false;
                        synchronized (Main.names) {
                            if (!Main.names.contains(name)) {
                                Main.names.add(name);
                                nameCorrect = true;
                            } else {
                                System.out.println("Name already exist.");
                                //Other name?
                            }
                        }
                        deckName = parameter.get(1);
                        deckList = getDeckList();
                        player = new Player(this, player.deck.cards.get(0), player.deck,name);

                        player.creatures = new ArrayList<>(2);


                        output.println("Hello, " + name + ", you going to play " + deckName + " deck.");
                        output.println("Waiting for opponent to connect");
                        //Const for shuffle
                        output.println("$YOUAREOK(" + Main.randomNum + ")");
                        if (nameCorrect) break;
                    } else {
                        output.println("Your client version is depricated! Update it.");
                        output.println("$YOUARENOTOK(" + "Your client version is depricated! Update it." + ")");
                        //Do something with it!
                    }
                }
            }

            Main.writers.add(output);
            Main.freePlayer.add(this);

            boolean pairFounded = false;

            while (true) {
                //If player disconnected before it take pair
                String a = input.readLine();
                //  System.out.println(name + " wait.");
                if (!a.equals("wait")) {
                    System.out.println("Player " + name + " disconnected before.");
                    Main.freePlayer.remove(this);
                }

                for (int i = 0; i < Main.freePlayer.size(); i++) {
                    if (!Main.freePlayer.get(i).name.equals(name) && Main.freePlayer.get(i).name != null) {
                        System.out.println("Pair found: " + name + "/" + Main.freePlayer.get(i).name);
                        opponent = Main.freePlayer.get(i);
                        //opponent.gameQueue = gameQueue;
                        Main.freePlayer.remove(Main.freePlayer.get(i));
                        pairFounded = true;
                    }
                }
                if (pairFounded) break;

                output.println("wait");
            }
            output.println("ok");

            //Get shuffled deck and send to opponent
            opponent.output.println("Your opponent " + name + ", play " + deckList.get(0) + " hero.");
            opponent.output.println("$OPPONENTCONNECTED(" + name + "," + deckList.get(0) +","+COIN_START+ ")");

            //Begin game
            player.deck.cards.remove(0);//Remove hero from deck
            Main.randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);//reroll for next
            player.deck.suffleDeck(Main.randomNum);
            Main.randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);//reroll for next
            player.drawCard();
            player.drawCard();
            player.drawCard();
            player.drawCard();

            player.untappedCoin = Main.COIN_START;
            player.totalCoin = Main.COIN_START;
            status = MyFunction.PlayerStatus.MuliganPhase;
            sendStatus();

            // Repeatedly get commands from the client and process them.
            while (true) {
                String command = input.readLine();
                System.out.println(name + ":" + command);
                if (command.contains("$DISCONNECT")) {
                    System.out.println(name + " normal disconnected.");
                    opponent.output.println("$DISCONNECT");
                    // This client is going down!  Remove it
                    if (name != null) {
                        Main.names.remove(name);
                    }
                    if (output != null) {
                        Main.writers.remove(output);
                    }
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                    break;
                } else if (command.contains("$MULLIGANEND")) {
                    endMuligan = true;
                    ArrayList<String> parameter = MyFunction.getTextBetween(command);
                    int nc = Integer.parseInt(parameter.get(1));
                    status = MyFunction.PlayerStatus.waitingMulligan;
                    for (int i = 0; i < nc; i++) {
                        player.deck.putOnBottomDeck(parameter.get(i + 2));
                        int a = MyFunction.searchCardInHandByName(player.cardInHand, parameter.get(i + 2));
                        player.removeCardFromHand(a);
                    }
                    for (int i = 0; i < nc; i++) player.drawCard();
                    sendStatus();
                    if (opponent.endMuligan) {
                        //START
                        System.out.println("Game for " + name + " and " + opponent.name + " started.");
                        //Choice, who first. Today at random
                        if (isFirstPlayer(name, opponent.name)) {
                            status = MyFunction.PlayerStatus.MyTurn;
                            opponent.status = MyFunction.PlayerStatus.EnemyTurn;
                            player.setNumberPlayer(0);
                            opponent.player.setNumberPlayer(1);
                            player.newTurn();
                        } else {
                            status = MyFunction.PlayerStatus.EnemyTurn;
                            opponent.status = MyFunction.PlayerStatus.MyTurn;
                            player.setNumberPlayer(1);
                            opponent.player.setNumberPlayer(0);
                            opponent.player.newTurn();
                        }
                        sendStatus();
                        opponent.sendStatus();
                        Main.randomNum = ThreadLocalRandom.current().nextInt(100, 999 + 1);//reroll for next
                    }
                } else {
                    ResponseClientMessage responseClientMessage = new ResponseClientMessage(this, command);
                    responseClientMessage.start();
//                    //pause until response ends
//                    synchronized (monitor) {
//                        try {
//                            monitor.wait();
//                        } catch (InterruptedException e2) {
//                            e2.printStackTrace();
//                        }
//                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Player disconnected: " + name);
            //Reconnect?
        } finally {
            System.out.println("Finaly " + name);
            Main.freePlayer.remove(this);
            if (opponent != null) opponent.output.println("$DISCONNECT");
            // This client is going down!  Remove it.
            if (name != null) {
                Main.names.remove(name);
            }
            if (output != null) {
                Main.writers.remove(output);
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

    }


    void sendChoiceSearch(boolean dig,String message){
        //#ChoiceSearchInDeck(PlayerName,CardType,CardColor,CreatureType,CardCost,CardCostExactly,Message).
        System.out.println("Sending choice search to " + player.playerName);
        String s;
        if (!dig) s = "#ChoiceSearchInDeck(";
        else s = "#ChoiceSearchInGraveyard(";
        s+=player.playerName+",";
        s+=choiceXtype+",";
        s+=choiceXcolor+",";
        s+=choiceXcreatureType+",";
        s+=choiceXcost+",";
        s+=choiceXcostExactly+",";
        s+=message+")";
        output.println(s);
    }

    void sendChoiceTarget(String message){
        System.out.println("Sending choice target to " + player.playerName + ", whatAbility= " + MyFunction.ActivatedAbility.whatAbility.getValue());
        String s = "#ChoiceTarget(";
        s+=player.playerName+",";
        s+= status.getValue() + ",";
        s+=player.creatures.indexOf(MyFunction.ActivatedAbility.creature)+",";
        s+=MyFunction.ActivatedAbility.whatAbility.getValue()+",";
        s+=message+")";
        output.println(s);
    }

    void sendUntapAll(){
        output.println("#UntapAll("+this.name+")");
        opponent.output.println("#UntapAll("+this.name+")");
    }

    void sendStatus() {
        System.out.println("Sending status to " + player.playerName + ", status= " + status.getValue());
        String s = "#TotalStatusPlayer(";
        s += player.playerName+",";
        s += status.getValue() + ",";
        s += player.damage + ",";
        s += player.untappedCoin + ",";
        s += player.totalCoin + ",";
        s += player.temporaryCoin+",";
        s += player.owner.opponent.player.untappedCoin + ",";
        s += player.owner.opponent.player.totalCoin + ",";
        s += player.owner.opponent.player.temporaryCoin+",";
        s += player.deck.getCardExpiried() + ",";
        s += player.owner.opponent.player.cardInHand.size()+",";
        s += player.cardInHand.size() + ",";
        for (int i = 0; i < player.cardInHand.size(); i++) {
            s += player.cardInHand.get(i).name + ",";
        }
        s += ")";
        output.println(s);
    }

    public static ArrayList<String> getTextBetween(String fromText) {
        ArrayList<String> rtrn = new ArrayList<>();
        String beforeText = "(";
        fromText = fromText.substring(fromText.indexOf(beforeText) + 1, fromText.length() - 1);
        String[] par = fromText.split(",");
        for (int i = 0; i < par.length; i++)
            rtrn.add(par[i]);
        return rtrn;
    }
}